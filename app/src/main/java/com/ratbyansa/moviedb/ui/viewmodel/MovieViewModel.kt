package com.ratbyansa.moviedb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ratbyansa.moviedb.data.local.entity.MovieEntity
import com.ratbyansa.moviedb.data.repository.MovieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _currentGenreId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val moviePagingData: Flow<PagingData<MovieEntity>> = _currentGenreId
        .filterNotNull()
        .flatMapLatest { genreId ->
            repository.getMoviesByGenre(genreId)
        }
        .cachedIn(viewModelScope)

    fun setGenre(genreId: Long) {
        if (_currentGenreId.value == genreId) return
        _currentGenreId.value = genreId
    }
}