package com.ratbyansa.moviedb.di

import com.ratbyansa.moviedb.data.repository.FavoriteRepository
import com.ratbyansa.moviedb.data.repository.MovieRepository
import com.ratbyansa.moviedb.data.repository.ReviewRepository
import com.ratbyansa.moviedb.data.repository.SearchMovieRepository
import com.ratbyansa.moviedb.data.repository.VideoRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { MovieRepository(get(), get()) }
    single { SearchMovieRepository(get(), get()) }
    single { FavoriteRepository(get()) }
    single { ReviewRepository(get()) }
    single { VideoRepository(get()) }
}