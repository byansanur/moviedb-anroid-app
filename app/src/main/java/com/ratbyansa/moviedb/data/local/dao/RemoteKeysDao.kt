package com.ratbyansa.moviedb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ratbyansa.moviedb.data.local.entity.RemoteKeysEntity

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeysEntity>)

    @Query("SELECT * FROM remote_keys WHERE movieId = :movieId")
    suspend fun remoteKeysMovieId(movieId: Int): RemoteKeysEntity?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}