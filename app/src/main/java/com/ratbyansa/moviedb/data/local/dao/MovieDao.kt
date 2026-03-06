package com.ratbyansa.moviedb.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ratbyansa.moviedb.data.local.entity.MovieEntity

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    // Mengembalikan PagingSource agar terintegrasi langsung dengan Paging 3
    @Query("SELECT * FROM movies WHERE genreId = :genreId ORDER BY id ASC")
    fun getMoviesByGenre(genreId: Int): PagingSource<Int, MovieEntity>

    @Query("DELETE FROM movies WHERE genreId = :genreId")
    suspend fun clearMoviesByGenre(genreId: Int)
}