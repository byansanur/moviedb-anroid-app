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
import androidx.compose.material3.LargeTopAppBar
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
import com.ratbyansa.moviedb.ui.screen.ErrorBottomSheet
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
                    // A. BACKDROP IMAGE (Sekarang di dalam list)
                    item {
                        BackdropImage(
                            backdropPath = movie.backdropPath ?: "",
                            onPlayClick = {
                                // Tambahkan logika navigasi Anda di sini, contoh:
                                // navController.navigate("player/${movie.id}")
                            }
                        )
                    }

                    // B. TITLE SECTION (Diberi offset negatif agar naik menumpuk backdrop)
                    item {
                        TitleSection(
                            posterPath = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                            title = movie.title,
                            tagline = movie.tagline ?: ""
                        )
                    }

                    // C. KONTEN LAINNYA
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
                        Spacer(modifier = Modifier.height(128.dp)) // Ruang ekstra bawah
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

@Composable
fun BackdropImage(
    backdropPath: String,
    onPlayClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w780$backdropPath",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Gradient overlay bawah agar menyatu dengan background gelap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                        startY = 400f
                    )
                )
        )
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .size(60.dp)
                .clickable { onPlayClick() },
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.5f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Trailer",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarButtons(
    isFavorite: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 8.dp).background(Color.Black.copy(0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
        },
        actions = {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.padding(end = 8.dp).background(Color.Black.copy(0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun TitleSection(posterPath: String, title: String, tagline: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster Film Kecil
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .size(width = 100.dp, height = 150.dp)
                .offset(y = (-20).dp) // Membuat poster sedikit naik ke area backdrop
        ) {
            AsyncImage(
                model = posterPath,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Judul dan Tagline
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (tagline.isNotEmpty()) {
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun MetadataSection(movie: MovieDetailResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(String.format("%.1f", movie.voteAverage), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

        Text("  •  ", color = Color.Gray)

        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(movie.formattedRuntime, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Text("  •  ", color = Color.Gray)

        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(movie.releaseYear, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
fun GenreChipsSection(genres: List<GenreDto>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.forEach { genre ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = genre.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SynopsisSection(overview: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Synopsis",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = overview,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp,
            // Jika tidak expand, batasi maksimal 4 baris
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = if (isExpanded) "Read less" else "Read more",
            color = Color(0xFF29B6F6),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable { isExpanded = !isExpanded } // Toggle state
        )
    }
}

@Composable
fun CastSection(cast: List<CastDto>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cast", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("See all", color = Color(0xFF29B6F6), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            cast.take(10).forEach { actor ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w185${actor.profilePath}",
                        contentDescription = actor.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = actor.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Button(
            onClick = { /* Get Tickets */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Get Tickets", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { /* Read Reviews */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Read Reviews", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}