package com.ratbyansa.moviedb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ratbyansa.moviedb.data.remote.model.MovieDetailResponse
import com.ratbyansa.moviedb.data.repository.FavoriteRepository
import com.ratbyansa.moviedb.data.repository.MovieRepository
import com.ratbyansa.moviedb.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val repository: MovieRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _detailState = MutableStateFlow<UiState<MovieDetailResponse>>(UiState.Loading)
    val detailState = _detailState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    fun getMovieDetail(movieId: Long) {
        _detailState.value = UiState.Loading
        viewModelScope.launch {
            _isFavorite.value = favoriteRepository.isFavorite(movieId)
            repository.getMovieDetail(movieId).collect { result ->
                result.fold(
                    onSuccess = { data -> _detailState.value = UiState.Success(data) },
                    onFailure = { e -> _detailState.value = UiState.Error(e.message ?: "Unknown Error") }
                )
            }
        }
    }

    fun toggleFavorite(movie: MovieDetailResponse) {
        viewModelScope.launch {
            if (_isFavorite.value) {
                favoriteRepository.removeFromFavorite(movie.id)
                _isFavorite.value = false
            } else {
                favoriteRepository.addToFavorite(movie)
                _isFavorite.value = true
            }
        }
    }
}