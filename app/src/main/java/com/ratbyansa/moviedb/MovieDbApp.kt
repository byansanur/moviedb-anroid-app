package com.ratbyansa.moviedb

import android.app.Application
import com.ratbyansa.moviedb.di.databaseModule
import com.ratbyansa.moviedb.di.networkModule
import com.ratbyansa.moviedb.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class MovieDbApp: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MovieDbApp)
            modules(networkModule, databaseModule, repositoryModule)
        }
    }
}