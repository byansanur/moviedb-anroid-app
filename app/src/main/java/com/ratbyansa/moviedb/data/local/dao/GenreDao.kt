package com.ratbyansa.moviedb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ratbyansa.moviedb.data.local.entity.GenreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<GenreEntity>)

    @Query("SELECT * FROM genres")
    fun getAllGenres(): Flow<List<GenreEntity>>

    @Query("SELECT COUNT(*) FROM genres")
    suspend fun getGenreCount(): Long
}