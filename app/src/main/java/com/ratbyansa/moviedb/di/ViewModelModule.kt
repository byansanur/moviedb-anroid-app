package com.ratbyansa.moviedb.di

import com.ratbyansa.moviedb.ui.viewmodel.FavoriteViewModel
import com.ratbyansa.moviedb.ui.viewmodel.GenreViewModel
import com.ratbyansa.moviedb.ui.viewmodel.MovieDetailViewModel
import com.ratbyansa.moviedb.ui.viewmodel.MovieViewModel
import com.ratbyansa.moviedb.ui.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { GenreViewModel(get()) }
    viewModel { MovieViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { MovieDetailViewModel(get(), get()) }
    viewModel { FavoriteViewModel(get()) }
}