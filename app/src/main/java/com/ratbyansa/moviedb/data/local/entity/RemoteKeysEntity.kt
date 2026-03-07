package com.ratbyansa.moviedb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeysEntity(
    @PrimaryKey
    val movieId: Long,
    val prevKey: Long?,
    val nextKey: Long?,
    val genreId: Long?
)