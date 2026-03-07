package com.ratbyansa.moviedb.data.repository

import com.ratbyansa.moviedb.data.local.dao.FavoriteDao
import com.ratbyansa.moviedb.data.local.entity.FavoriteMovieEntity
import com.ratbyansa.moviedb.data.remote.model.MovieDetailResponse
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(
    private val favoriteDao: FavoriteDao
) {

    suspend fun addToFavorite(movie: MovieDetailResponse) {
        val entity = FavoriteMovieEntity(
            id = movie.id,
            title = movie.title,
            posterPath = movie.posterPath,
            backdropPath = movie.backdropPath,
            voteAverage = movie.voteAverage,
            releaseDate = movie.releaseDate
        )
        favoriteDao.insertFavorite(entity)
    }

    suspend fun removeFromFavorite(movieId: Long) {
        favoriteDao.deleteFavoriteById(movieId)
    }

    suspend fun isFavorite(movieId: Long): Boolean {
        return favoriteDao.isFavorite(movieId)
    }

    fun getFavoriteMovies(): Flow<List<FavoriteMovieEntity>> {
        return favoriteDao.getAllFavorites()
    }
}