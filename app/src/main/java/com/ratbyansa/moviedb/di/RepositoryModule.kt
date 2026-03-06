package com.ratbyansa.moviedb.di

import com.ratbyansa.moviedb.data.repository.MovieRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { MovieRepository(get(), get()) }
}