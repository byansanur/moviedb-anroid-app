package com.ratbyansa.moviedb.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ratbyansa.moviedb.data.local.entity.GenreEntity
import com.ratbyansa.moviedb.ui.common.UiState
import com.ratbyansa.moviedb.ui.screen.common.AllGenresBottomSheet
import com.ratbyansa.moviedb.ui.screen.common.ErrorBottomSheet
import com.ratbyansa.moviedb.ui.screen.common.FavoriteQuickActionRow
import com.ratbyansa.moviedb.ui.viewmodel.FavoriteViewModel
import com.ratbyansa.moviedb.ui.viewmodel.GenreViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreScreen(
    viewModel: GenreViewModel,
    onGenreClick: (GenreEntity) -> Unit,
    onSearchClick: () -> Unit,
    favoriteViewModel: FavoriteViewModel = koinViewModel(),
    onMovieClick: (Long) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val uiState by viewModel.genreState.collectAsState()
    val favorites by favoriteViewModel.favoriteMovies.collectAsState()

    var showEmptyError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

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
            onRetry = { viewModel.getGenres() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TMDB Explorer", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { onSearchClick() }) {
                        Icon(Icons.Default.Search, "Global Search")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("DISCOVER", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Text(
                        "Browse by Genre",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        "Explore our extensive collection of movies categorized by genre. Find exactly what you're looking for with you're in the mood for.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            when (uiState) {
                is UiState.Loading -> {
                    item {
                        Box(Modifier.fillParentMaxHeight(0.6f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is UiState.Success -> {
                    val allGenres = (uiState as UiState.Success<List<GenreEntity>>).data

                    val displayedGenres = allGenres.take(5)
                    val rows = displayedGenres.chunked(2)

                    rows.forEachIndexed { rowIndex, rowItems ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { genre ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        GenreCard(genre = genre, onClick = { onGenreClick(genre) })
                                    }
                                }

                                if (rowIndex == 2 && rowItems.size == 1) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        SeeMoreCard(onClick = { showBottomSheet = true }) // Trigger BottomSheet
                                    }
                                } else if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (favorites.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            FavoriteQuickActionRow(
                                favorites = favorites.take(10),
                                onMovieClick = { movieId -> onMovieClick(movieId) }
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Oops! Terjadi kesalahan koneksi.")
                            Button(onClick = { viewModel.getGenres() }) { Text("Coba Lagi") }
                        }
                    }
                }
                else -> {}
            }
        }
    }
    if (showBottomSheet && uiState is UiState.Success) {
        AllGenresBottomSheet(
            allGenres = (uiState as UiState.Success).data,
            onGenreClick = { genre ->
                showBottomSheet = false
                onGenreClick(genre)
            },
            onDismiss = { showBottomSheet = false }
        )
    }
}

@Composable
fun SeeMoreCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), // Abu-abu terang
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "See More",
                style = MaterialTheme.typography.titleMedium,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


fun getGenreStyle(name: String): Triple<Color, ImageVector, String> {
    return when (name) {
        "Action" -> Triple(Color(0xFFE57373), Icons.Default.Search, "High energy & stunts")
        "Comedy" -> Triple(Color(0xFFFFB74D), Icons.Default.Build, "Laugh out loud")
        "Drama" -> Triple(Color(0xFF9575CD), Icons.Default.AccountBox, "Emotional narratives")
        "Science Fiction" -> Triple(Color(0xFF64B5F6), Icons.Default.Star, "Future & space")
        "Horror" -> Triple(Color(0xFF4DB6AC), Icons.Default.Face, "Thrills & chills")
        "Romance" -> Triple(Color(0xFFF06292), Icons.Default.Favorite, "Love stories")
        else -> Triple(Color(0xFF81C784), Icons.Default.Email, "Explore collection")
    }
}

@Composable
fun GenreCard(
    genre: GenreEntity,
    onClick: () -> Unit
) {
    val genreStyle = remember(genre.id) { getGenreStyle(genre.name) }
    val (bgColor, icon, tagline) = genreStyle

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.BottomEnd)
                    .alpha(0.3f),
                tint = Color.White
            )

            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = genre.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}