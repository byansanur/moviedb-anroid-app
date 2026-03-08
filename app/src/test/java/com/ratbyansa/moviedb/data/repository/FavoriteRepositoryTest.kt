package com.ratbyansa.moviedb.data.repository

import app.cash.turbine.test
import com.ratbyansa.moviedb.data.local.dao.FavoriteDao
import com.ratbyansa.moviedb.data.local.entity.FavoriteMovieEntity
import com.ratbyansa.moviedb.data.remote.model.MovieDetailResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoriteRepositoryTest {

    private lateinit var repository: FavoriteRepository
    private val favoriteDao: FavoriteDao = mockk()

    @Before
    fun setup() {
        repository = FavoriteRepository(favoriteDao)
    }

    @Test
    fun `addToFavorite should call insertFavorite on Dao`() = runTest {
        // Arrange
        val movieDetail = MovieDetailResponse(
            id = 1,
            title = "Test Movie",
            overview = "Overview",
            posterPath = "/path",
            backdropPath = "/back",
            voteAverage = 8.5,
            releaseDate = "2023-01-01"
        )
        coEvery { favoriteDao.insertFavorite(any()) } returns Unit

        // Act
        repository.addToFavorite(movieDetail)

        // Assert
        coVerify { 
            favoriteDao.insertFavorite(match { 
                it.id == movieDetail.id && it.title == movieDetail.title 
            }) 
        }
    }

    @Test
    fun `removeFromFavorite should call deleteFavoriteById on Dao`() = runTest {
        // Arrange
        val movieId = 1L
        coEvery { favoriteDao.deleteFavoriteById(movieId) } returns Unit

        // Act
        repository.removeFromFavorite(movieId)

        // Assert
        coVerify { favoriteDao.deleteFavoriteById(movieId) }
    }

    @Test
    fun `isFavorite should return result from Dao`() = runTest {
        // Arrange
        val movieId = 1L
        coEvery { favoriteDao.isFavorite(movieId) } returns true

        // Act
        val result = repository.isFavorite(movieId)

        // Assert
        assertTrue(result)
        coVerify { favoriteDao.isFavorite(movieId) }
    }

    @Test
    fun `getFavoriteMovies should return flow of favorites from Dao`() = runTest {
        // Arrange
        val favorites = listOf(
            FavoriteMovieEntity(1, "Movie 1", null, null, 8.0, "2023")
        )
        every { favoriteDao.getAllFavorites() } returns flowOf(favorites)

        // Act & Assert
        repository.getFavoriteMovies().test {
            val result = awaitItem()
            assertEquals(favorites, result)
            awaitComplete()
        }
        coVerify { favoriteDao.getAllFavorites() }
    }
}
