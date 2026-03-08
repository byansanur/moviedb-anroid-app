package com.ratbyansa.moviedb.data.repository

import app.cash.turbine.test
import com.ratbyansa.moviedb.data.remote.model.MovieVideo
import com.ratbyansa.moviedb.data.remote.model.MovieVideoResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoRepositoryTest {

    private lateinit var repository: VideoRepository

    @Test
    fun `fetchVideos should return success when network call is successful`() = runTest {
        // Arrange
        val movieId = 123L
        val videoResponse = MovieVideoResponse(
            id = 123,
            results = listOf(
                MovieVideo(
                    id = "1",
                    key = "abc",
                    name = "Trailer",
                    site = "YouTube",
                    type = "Trailer",
                    official = true,
                    publishedAt = "2023-01-01"
                )
            )
        )
        val jsonResponse = Json.encodeToString(videoResponse)

        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.endsWith("movie/$movieId/videos")) {
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

        repository = VideoRepository(httpClient)

        // Act & Assert
        repository.fetchVideos(movieId).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(videoResponse.results.size, result.getOrNull()?.results?.size)
            assertEquals(videoResponse.results[0].key, result.getOrNull()?.results?.get(0)?.key)
            awaitComplete()
        }
    }

    @Test
    fun `fetchVideos should return failure when network call fails`() = runTest {
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

        repository = VideoRepository(httpClient)

        // Act & Assert
        repository.fetchVideos(movieId).test {
            val result = awaitItem()
            assertTrue(result.isFailure)
            awaitComplete()
        }
    }
}
