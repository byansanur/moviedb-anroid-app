package com.ratbyansa.moviedb.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ratbyansa.moviedb.data.local.dao.GenreDao
import com.ratbyansa.moviedb.data.local.dao.MovieDao
import com.ratbyansa.moviedb.data.local.dao.RemoteKeysDao
import com.ratbyansa.moviedb.data.local.entity.GenreEntity
import com.ratbyansa.moviedb.data.local.entity.MovieEntity
import com.ratbyansa.moviedb.data.local.entity.RemoteKeysEntity

@Database(
    entities = [GenreEntity::class, MovieEntity::class, RemoteKeysEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun genreDao(): GenreDao
    abstract fun movieDao(): MovieDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}