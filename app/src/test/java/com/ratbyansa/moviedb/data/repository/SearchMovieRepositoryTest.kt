package com.ratbyansa.moviedb.data.repository

import app.cash.turbine.test
import com.ratbyansa.moviedb.data.local.dao.SearchHistoryDao
import com.ratbyansa.moviedb.data.local.entity.SearchHistoryEntity
import io.ktor.client.HttpClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchMovieRepositoryTest {

    private lateinit var repository: SearchMovieRepository
    private val httpClient: HttpClient = mockk()
    private val historyDao: SearchHistoryDao = mockk()

    @Before
    fun setup() {
        repository = SearchMovieRepository(httpClient, historyDao)
    }

    @Test
    fun `saveSearchHistory should call insertSearch on Dao for non-blank query`() = runTest {
        // Arrange
        val query = "Action"
        coEvery { historyDao.insertSearch(any()) } returns Unit

        // Act
        repository.saveSearchHistory(query)

        // Assert
        coVerify { historyDao.insertSearch(match { it.keys == query }) }
    }

    @Test
    fun `saveSearchHistory should not call insertSearch on Dao for blank query`() = runTest {
        // Arrange
        val query = " "

        // Act
        repository.saveSearchHistory(query)

        // Assert
        coVerify(exactly = 0) { historyDao.insertSearch(any()) }
    }

    @Test
    fun `getSearchHistory should return flow from Dao`() = runTest {
        // Arrange
        val history = listOf(SearchHistoryEntity("Action"))
        every { historyDao.getRecentSearches() } returns flowOf(history)

        // Act & Assert
        repository.getSearchHistory().test {
            val result = awaitItem()
            assertEquals(history, result)
            awaitComplete()
        }
    }

    @Test
    fun `deleteHistory should call deleteSearch on Dao`() = runTest {
        // Arrange
        val query = "Action"
        coEvery { historyDao.deleteSearch(query) } returns Unit

        // Act
        repository.deleteHistory(query)

        // Assert
        coVerify { historyDao.deleteSearch(query) }
    }
}
