package com.ratbyansa.moviedb.di

import android.content.Context
import androidx.room.Room
import com.ratbyansa.moviedb.data.local.MovieDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun provideDatabase(context: Context): MovieDatabase {
    return Room.databaseBuilder(
        context,
        MovieDatabase::class.java,
        "moviesapp"
    )
        .fallbackToDestructiveMigration()
        .build()
}

val databaseModule = module {
    single { provideDatabase(androidContext()) }

    // Menyediakan DAO secara spesifik agar mudah di-inject ke Repository
    single { get<MovieDatabase>().movieDao() }
    single { get<MovieDatabase>().remoteKeysDao() }
}