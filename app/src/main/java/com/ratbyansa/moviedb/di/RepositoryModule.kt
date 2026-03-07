package com.ratbyansa.moviedb.di

import com.ratbyansa.moviedb.data.repository.MovieRepository
import com.ratbyansa.moviedb.data.repository.SearchMovieRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { MovieRepository(get(), get()) }
    single { SearchMovieRepository(get(), get()) }
}