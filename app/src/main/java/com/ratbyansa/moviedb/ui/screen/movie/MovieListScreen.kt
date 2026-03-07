package com.ratbyansa.moviedb.ui.screen.movie

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ratbyansa.moviedb.data.local.entity.MovieEntity
import com.ratbyansa.moviedb.ui.common.ErrorStateUI
import com.ratbyansa.moviedb.ui.screen.movie.component.MovieItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    genreName: String,
    moviePagingItems: LazyPagingItems<MovieEntity>,
    onMovieClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("$genreName Movies", fontWeight = FontWeight.Bold) }, navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            null
                        )
                    }
                }, actions = {
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(
                            Icons.Default.Menu,
                            null
                        )
                    }
                })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Search Bar (Sesuai Desain image_df0a21.jpg)
            item {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Search $genreName movies...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(24.dp)
                )
            }

            items(
                count = moviePagingItems.itemCount,
                key = moviePagingItems.itemKey { it.id },
                contentType = moviePagingItems.itemContentType { "movie" }
            ) { index ->
                val movie = moviePagingItems[index]
                if (movie != null) {
                    key(movie.id) {
                        MovieItem(movie = movie, onClick = onMovieClick)
                    }
                }
            }

            // Penanganan State Loading & Error (Story 7)
            moviePagingItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item { Box(Modifier.fillParentMaxSize()) {
                            CircularProgressIndicator(
                                Modifier.align(Alignment.Center)
                            )
                        }}
                    }
                    loadState.append is LoadState.Loading -> {
                        item { Box(Modifier.fillMaxWidth().padding(16.dp)) { CircularProgressIndicator(Modifier.align(Alignment.Center)) } }
                    }
                    loadState.refresh is LoadState.Error -> {
                        val e = loadState.refresh as LoadState.Error
                        item { ErrorStateUI(message = e.error.localizedMessage) { retry() } }
                    }
                }
            }
        }
    }
}