package com.ratbyansa.moviedb.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.ratbyansa.moviedb.data.local.entity.SearchHistoryEntity
import com.ratbyansa.moviedb.ui.screen.common.FavoriteQuickActionRow
import com.ratbyansa.moviedb.ui.screen.movie.component.MovieItem
import com.ratbyansa.moviedb.ui.theme.recentSearchColors
import com.ratbyansa.moviedb.ui.viewmodel.FavoriteViewModel
import com.ratbyansa.moviedb.ui.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBackClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
    favoriteViewModel: FavoriteViewModel = koinViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults = viewModel.searchResult.collectAsLazyPagingItems()
    val history by viewModel.searchHistory.collectAsState()

    val favorites by favoriteViewModel.favoriteMovies.collectAsState()

    val isResultsEmpty by remember(searchResults.loadState, searchResults.itemCount) {
        derivedStateOf {
            searchResults.loadState.refresh is LoadState.NotLoading &&
                    searchResults.itemCount == 0 &&
                    searchQuery.isNotEmpty()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = {
                            Text("Search movies...", style = MaterialTheme.typography.bodyMedium)
                        },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .heightIn(min = 40.dp, max = 48.dp),
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                searchQuery.isEmpty() -> {
                    // Gunakan LazyColumn agar Recent Search dan Favorites bisa di-scroll bersama
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            if (history.isEmpty()) {
                                // Placeholder jika benar-benar kosong semua
                                if (favorites.isEmpty()) {
                                    EmptySearchPlaceholder(message = "Search your favorite movies")
                                }
                            } else {
                                RecentSearchList(
                                    history = history,
                                    onItemClick = { viewModel.onSearchQueryChanged(it) },
                                    onDeleteClick = { viewModel.deleteHistory(it) }
                                )
                            }
                        }

                        // Tampilkan Favorit jika ada
                        if (favorites.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                FavoriteQuickActionRow(
                                    favorites = favorites,
                                    onMovieClick = onMovieClick
                                )
                            }
                        }
                    }
                }
                isResultsEmpty -> {
                    EmptySearchPlaceholder(
                        message = "No results found for \"$searchQuery\"",
                        showSearchIcon = false
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            count = searchResults.itemCount,
                            key = searchResults.itemKey { it.id }
                        ) { index ->
                            val movie = searchResults[index]
                            if (movie != null) {
                                MovieItem(movie = movie, onClick = onMovieClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySearchPlaceholder(
    message: String,
    showSearchIcon: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showSearchIcon) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Text("🔍", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentSearchList(
    history: List<SearchHistoryEntity>,
    onItemClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Searches",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            history.forEachIndexed { index, item ->
                val bgColor = remember(item.keys) {
                    recentSearchColors[index % recentSearchColors.size]
                }

                AssistChip(
                    onClick = { onItemClick(item.keys) },
                    label = {
                        Text(
                            text = item.keys,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onDeleteClick(item.keys) },
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = bgColor,
                    ),
                    border = null
                )
            }
        }
    }
}