package com.ratbyansa.moviedb.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.ratbyansa.moviedb.data.local.MovieDatabase
import com.ratbyansa.moviedb.data.local.entity.GenreEntity
import com.ratbyansa.moviedb.data.local.entity.MovieEntity
import com.ratbyansa.moviedb.data.remote.model.GenreListResponse
import com.ratbyansa.moviedb.data.remote.model.MovieDetailResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MovieRepository(
    private val database: MovieDatabase,
    private val ktorClient: HttpClient
) {
    suspend fun getGenres(): Flow<List<GenreEntity>> {
        val count = database.genreDao().getGenreCount()
        if (count == 0L) {
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
    fun getMoviesByGenre(genreId: Long): Flow<PagingData<MovieEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 2,
                maxSize = 49,
                enablePlaceholders = false
            ),
            remoteMediator = MovieRemoteMediator(genreId, database, ktorClient),
            pagingSourceFactory = { database.movieDao().getMoviesByGenre(genreId.toInt()) as PagingSource<Long, MovieEntity> }
        ).flow
    }

    fun getMovieDetail(movieId: Long): Flow<Result<MovieDetailResponse>> = flow {
        try {
            val response = ktorClient.get("movie/$movieId") {
                url {
                    parameters.append("append_to_response", "credits")
                }
            }.body<MovieDetailResponse>()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}