package com.ratbyansa.moviedb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ratbyansa.moviedb.data.local.entity.FavoriteMovieEntity
import com.ratbyansa.moviedb.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(private val favoriteRepository: FavoriteRepository) : ViewModel() {

    private val _favoriteMovies = MutableStateFlow<List<FavoriteMovieEntity>>(emptyList())
    val favoriteMovies: StateFlow<List<FavoriteMovieEntity>> = _favoriteMovies.asStateFlow()

    init {
        fetchFavoriteMovies()
    }

    private fun fetchFavoriteMovies() {
        viewModelScope.launch {
            favoriteRepository.getFavoriteMovies().collect { list ->
                _favoriteMovies.value = list
            }
        }
    }
}