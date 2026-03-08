package com.ratbyansa.moviedb.ui.screen.detail

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ratbyansa.moviedb.data.remote.model.MovieDetailResponse
import com.ratbyansa.moviedb.ui.common.UiState
import com.ratbyansa.moviedb.ui.screen.common.ErrorBottomSheet
import com.ratbyansa.moviedb.ui.screen.detail.component.ActionButtons
import com.ratbyansa.moviedb.ui.screen.detail.component.BackdropImage
import com.ratbyansa.moviedb.ui.screen.detail.component.CastBottomSheet
import com.ratbyansa.moviedb.ui.screen.detail.component.CastSection
import com.ratbyansa.moviedb.ui.screen.detail.component.GenreChipsSection
import com.ratbyansa.moviedb.ui.screen.detail.component.MetadataSection
import com.ratbyansa.moviedb.ui.screen.detail.component.SynopsisSection
import com.ratbyansa.moviedb.ui.screen.detail.component.TitleSection
import com.ratbyansa.moviedb.ui.screen.detail.component.TopAppBarButtons
import com.ratbyansa.moviedb.ui.viewmodel.MovieDetailViewModel
import com.ratbyansa.moviedb.ui.viewmodel.VideoViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Long,
    viewModel: MovieDetailViewModel = koinViewModel(),
    videoViewModel: VideoViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onSeeReview: (String) -> Unit,
    onNavigateToPlayer: (String, String) -> Unit
) {
    val uiState by viewModel.detailState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    val trailerKey by videoViewModel.trailerKey.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showEmptyError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    // STATE UNTUK BOTTOM SHEET CAST
    var showCastSheet by remember { mutableStateOf(false) }

    LaunchedEffect(movieId) {
        viewModel.getMovieDetail(movieId)
        videoViewModel.getMovieTrailer(movieId)
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
                        .padding()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    item {
                        BackdropImage(
                            backdropPath = movie.backdropPath ?: "",
                            onPlayClick = {
                                goToTrailer(trailerKey, movie, onNavigateToPlayer)
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
                        movie.credits?.cast?.let { castList ->
                            CastSection(
                                cast = castList,
                                onSeeAllClick = { showCastSheet = true } // Munculkan sheet
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        ActionButtons(
                            onReadReviewsClick = {
                                val movieJson = Json.encodeToString(movie)
                                val encodedJson = Uri.encode(movieJson)
                                onSeeReview(encodedJson)
                            },
                            onWatchTrailer = {
                                goToTrailer(trailerKey, movie, onNavigateToPlayer)
                            }
                        )
                        Spacer(modifier = Modifier.height(128.dp))
                    }
                }
            }
            if (showCastSheet && movie.credits?.cast != null) {
                CastBottomSheet(
                    castList = movie.credits.cast,
                    onDismiss = { showCastSheet = false }
                )
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

private fun goToTrailer(
    trailerKey: String?,
    movie: MovieDetailResponse,
    onNavigateToPlayer: (String, String) -> Unit
) {
    trailerKey?.let { key ->
        // Encode movie json
        val movieJson = Json.encodeToString(movie)
        val encodedJson = Uri.encode(movieJson)

        // PENTING: Encode juga video key-nya!
        val encodedKey = Uri.encode(key)

        // Kirim data yang sudah di encode
        onNavigateToPlayer(encodedKey, encodedJson)
    }
}