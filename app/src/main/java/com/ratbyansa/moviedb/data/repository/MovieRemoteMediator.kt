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
import com.ratbyansa.moviedb.data.remote.model.toEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediator(
    private val genreId: Long,
    private val database: MovieDatabase,
    private val ktorClient: HttpClient
) : RemoteMediator<Long, MovieEntity>() {

    // LOGIKA ANTI BONCOS:
    // Jika data di lokal sudah ada, jangan paksa refresh dari API saat aplikasi dibuka
    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Long, MovieEntity>
    ): MediatorResult {
        return try {
            val page: Long = when (loadType) {
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

            val movies = response.results
            val endOfPaginationReached = movies.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().clearRemoteKeys()
                    database.movieDao().clearMoviesByGenre(genreId)
                }

                val prevKey = if (page == 1L) null else page - 1L
                val nextKey = if (endOfPaginationReached) null else page + 1

                val movieEntities = movies.map { it.toEntity(genreId) }

                val keys = movieEntities.map {
                    RemoteKeysEntity(movieId = it.id, prevKey = prevKey?.minus(1), nextKey = nextKey, genreId = genreId)
                }
                database.remoteKeysDao().insertAll(keys)
                database.movieDao().insertAll(movieEntities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Long, MovieEntity>): RemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { movie ->
            database.remoteKeysDao().remoteKeysMovieId(movie.id)
        }
    }
}