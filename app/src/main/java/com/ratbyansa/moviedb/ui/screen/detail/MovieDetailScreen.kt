package com.ratbyansa.moviedb.ui.screen.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ratbyansa.moviedb.data.remote.model.CastDto
import com.ratbyansa.moviedb.data.remote.model.GenreDto
import com.ratbyansa.moviedb.data.remote.model.MovieDetailResponse
import com.ratbyansa.moviedb.ui.common.UiState
import com.ratbyansa.moviedb.ui.screen.common.ErrorBottomSheet
import com.ratbyansa.moviedb.ui.screen.detail.component.ActionButtons
import com.ratbyansa.moviedb.ui.screen.detail.component.BackdropImage
import com.ratbyansa.moviedb.ui.screen.detail.component.CastSection
import com.ratbyansa.moviedb.ui.screen.detail.component.GenreChipsSection
import com.ratbyansa.moviedb.ui.screen.detail.component.MetadataSection
import com.ratbyansa.moviedb.ui.screen.detail.component.SynopsisSection
import com.ratbyansa.moviedb.ui.screen.detail.component.TitleSection
import com.ratbyansa.moviedb.ui.screen.detail.component.TopAppBarButtons
import com.ratbyansa.moviedb.ui.viewmodel.MovieDetailViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Long,
    viewModel: MovieDetailViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.detailState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showEmptyError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(movieId) {
        viewModel.getMovieDetail(movieId)
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            errorMsg = (uiState as UiState.Error).message ?: "Unknown Error"
            showEmptyError = true
        }
    }

    if (showEmptyError) {
        ErrorBottomSheet(
            errorMessage = errorMsg,
            onDismiss = { showEmptyError = false },
            onRetry = { viewModel.getMovieDetail(movieId) }
        )
    }

    when (uiState) {
        is UiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Success -> {
            val movie = (uiState as UiState.Success<MovieDetailResponse>).data

            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    // TopBar hanya berisi tombol, background transparan yang berubah jadi solid saat scroll
                    TopAppBarButtons(
                        isFavorite = isFavorite,
                        scrollBehavior = scrollBehavior,
                        onBackClick = onBackClick,
                        onFavoriteClick = { viewModel.toggleFavorite(movie) }
                    )
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    item {
                        BackdropImage(
                            backdropPath = movie.backdropPath ?: "",
                            onPlayClick = {
                                // Tambahkan logika navigasi Anda di sini, contoh:
                                // navController.navigate("player/${movie.id}")
                            }
                        )
                    }

                    item {
                        TitleSection(
                            posterPath = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                            title = movie.title,
                            tagline = movie.tagline ?: ""
                        )
                    }

                    item { MetadataSection(movie) }
                    item { GenreChipsSection(movie.genres) }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SynopsisSection(movie.overview)
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        movie.credits?.cast?.let { CastSection(it) }
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        ActionButtons()
                        Spacer(modifier = Modifier.height(128.dp))
                    }
                }
            }
        }
        is UiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { viewModel.getMovieDetail(movieId) }) {
                    Text("Muat Ulang")
                }
            }
        }
        else -> {}
    }
}