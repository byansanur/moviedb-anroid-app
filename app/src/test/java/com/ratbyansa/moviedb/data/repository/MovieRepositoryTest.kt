package com.ratbyansa.moviedb.data.repository

import app.cash.turbine.test
import com.ratbyansa.moviedb.data.local.MovieDatabase
import com.ratbyansa.moviedb.data.local.dao.GenreDao
import com.ratbyansa.moviedb.data.local.entity.GenreEntity
import com.ratbyansa.moviedb.data.remote.model.GenreDto
import com.ratbyansa.moviedb.data.remote.model.GenreListResponse
import com.ratbyansa.moviedb.data.remote.model.MovieDetailResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MovieRepositoryTest {

    private lateinit var repository: MovieRepository
    private val database: MovieDatabase = mockk()
    private val genreDao: GenreDao = mockk()

    @Before
    fun setup() {
        every { database.genreDao() } returns genreDao
    }

    @Test
    fun `getGenres should fetch from remote and save to local when local is empty`() = runTest {
        // Arrange
        val genresDto = listOf(GenreDto(1, "Action"), GenreDto(2, "Comedy"))
        val response = GenreListResponse(genresDto)
        val jsonResponse = Json.encodeToString(response)

        val mockEngine = MockEngine { _ ->
            respond(
                content = jsonResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        repository = MovieRepository(database, httpClient)

        val expectedEntities = genresDto.map { GenreEntity(it.id, it.name) }

        coEvery { genreDao.getGenreCount() } returns 0L
        coEvery { genreDao.insertGenres(any()) } returns Unit
        every { genreDao.getAllGenres() } returns flowOf(expectedEntities)

        // Act & Assert
        repository.getGenres().test {
            val result = awaitItem()
            assertEquals(expectedEntities, result)
            awaitComplete()
        }

        coVerify { genreDao.getGenreCount() }
        coVerify { genreDao.insertGenres(expectedEntities) }
        coVerify { genreDao.getAllGenres() }
    }

    @Test
    fun `getGenres should return local data when local is not empty`() = runTest {
        // Arrange
        val httpClient: HttpClient = mockk()
        repository = MovieRepository(database, httpClient)

        val existingEntities = listOf(GenreEntity(1, "Action"))

        coEvery { genreDao.getGenreCount() } returns 1L
        every { genreDao.getAllGenres() } returns flowOf(existingEntities)

        // Act & Assert
        repository.getGenres().test {
            val result = awaitItem()
            assertEquals(existingEntities, result)
            awaitComplete()
        }

        coVerify { genreDao.getGenreCount() }
        coVerify(exactly = 0) { genreDao.insertGenres(any()) }
        coVerify { genreDao.getAllGenres() }
    }

    @Test
    fun `getMovieDetail should return success when network call is successful`() = runTest {
        // Arrange
        val movieId = 123L
        val movieDetail = MovieDetailResponse(
            id = movieId,
            title = "Test Movie",
            overview = "Overview"
        )
        val jsonResponse = Json.encodeToString(movieDetail)

        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.endsWith("movie/$movieId")) {
                respond(
                    content = jsonResponse,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respond(content = "", status = HttpStatusCode.NotFound)
            }
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        repository = MovieRepository(database, httpClient)

        // Act & Assert
        repository.getMovieDetail(movieId).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(movieDetail.title, result.getOrNull()?.title)
            awaitComplete()
        }
    }

    @Test
    fun `getMovieDetail should return failure when network call fails`() = runTest {
        // Arrange
        val movieId = 123L
        val mockEngine = MockEngine { _ ->
            respond(content = "Error", status = HttpStatusCode.InternalServerError)
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        repository = MovieRepository(database, httpClient)

        // Act & Assert
        repository.getMovieDetail(movieId).test {
            val result = awaitItem()
            assertTrue(result.isFailure)
            awaitComplete()
        }
    }
}
