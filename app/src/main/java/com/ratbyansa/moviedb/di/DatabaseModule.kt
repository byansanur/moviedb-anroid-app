package com.ratbyansa.moviedb.di

import android.content.Context
import androidx.room.Room
import com.ratbyansa.moviedb.BuildConfig
import com.ratbyansa.moviedb.data.local.MovieDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun provideDatabase(context: Context): MovieDatabase {
    System.loadLibrary("sqlcipher")
    val secretKey = BuildConfig.dbKeySecret
    val passphrase = secretKey.toByteArray(Charsets.UTF_8)
    val factory = SupportOpenHelperFactory(passphrase)

    return Room.databaseBuilder(
        context,
        MovieDatabase::class.java,
        "moviesapp"
    )
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration()
        .build()
}

val databaseModule = module {
    single { provideDatabase(androidContext()) }

    // Menyediakan DAO secara spesifik agar mudah di-inject ke Repository
    single { get<MovieDatabase>().movieDao() }
    single { get<MovieDatabase>().genreDao() }
    single { get<MovieDatabase>().remoteKeysDao() }
    single { get<MovieDatabase>().searchHistoryDao() }
    single { get<MovieDatabase>().favoriteDao() }
}