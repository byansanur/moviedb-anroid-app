package com.ratbyansa.moviedb.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ratbyansa.moviedb.data.local.MovieDatabase
import com.ratbyansa.moviedb.data.local.entity.MovieEntity
import com.ratbyansa.moviedb.data.local.entity.RemoteKeysEntity
import com.ratbyansa.moviedb.data.remote.model.MovieResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediator(
    private val genreId: Int,
    private val database: MovieDatabase,
    private val ktorClient: HttpClient
) : RemoteMediator<Int, MovieEntity>() {

    // LOGIKA ANTI BONCOS:
    // Jika data di lokal sudah ada, jangan paksa refresh dari API saat aplikasi dibuka
    override suspend fun initialize(): InitializeAction {
        return InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey ?: return MediatorResult.Success(
                        endOfPaginationReached = remoteKeys != null
                    )
                    nextKey
                }
            }

            // Hit API hanya jika dibutuhkan oleh Paging 3
            val response = ktorClient.get("discover/movie") {
                url {
                    parameters.append("with_genres", genreId.toString())
                    parameters.append("page", page.toString())
                }
            }.body<MovieResponse>()

            val endOfPaginationReached = response.results.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    // Kita tidak menghapus semua, hanya yang spesifik genre ini
                    database.remoteKeysDao().clearRemoteKeys()
                    database.movieDao().clearMoviesByGenre(genreId)
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = response.results.map {
                    RemoteKeysEntity(movieId = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                val entities = response.results.map { dto ->
                    MovieEntity(
                        id = dto.id,
                        title = dto.title,
                        overview = dto.overview,
                        posterPath = dto.posterPath,
                        backdropPath = dto.backdropPath,
                        releaseDate = dto.releaseDate,
                        voteAverage = dto.voteAverage,
                        genreId = genreId // Penting untuk filter lokal
                    )
                }

                database.remoteKeysDao().insertAll(keys)
                database.movieDao().insertAll(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, MovieEntity>): RemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { movie ->
            database.remoteKeysDao().remoteKeysMovieId(movie.id)
        }
    }
}