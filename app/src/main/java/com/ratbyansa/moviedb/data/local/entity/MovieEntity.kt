package com.ratbyansa.moviedb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    // Kita simpan ID genre yang sedang dibuka agar bisa memfilter data saat offline
    val genreId: Int
)