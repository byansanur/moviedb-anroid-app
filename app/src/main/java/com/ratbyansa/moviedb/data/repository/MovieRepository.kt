package com.ratbyansa.moviedb.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ratbyansa.moviedb.data.local.MovieDatabase
import com.ratbyansa.moviedb.data.local.entity.GenreEntity
import com.ratbyansa.moviedb.data.local.entity.MovieEntity
import com.ratbyansa.moviedb.data.remote.model.GenreListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private val database: MovieDatabase,
    private val ktorClient: HttpClient
) {
    suspend fun getGenres(): Flow<List<GenreEntity>> {
        val count = database.genreDao().getGenreCount()
        if (count == 0) {
            // Hit API hanya jika lokal kosong
            val response = ktorClient.get("genre/movie/list").body<GenreListResponse>()

            // Mapping DTO ke Entity Room
            val entities = response.genres.map {
                GenreEntity(id = it.id, name = it.name)
            }

            database.genreDao().insertGenres(entities)
        }
        return database.genreDao().getAllGenres()
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getMoviesByGenre(genreId: Int): Flow<PagingData<MovieEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 2),
            remoteMediator = MovieRemoteMediator(genreId, database, ktorClient),
            pagingSourceFactory = { database.movieDao().getMoviesByGenre(genreId) }
        ).flow
    }
}