package com.ratbyansa.moviedb.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ratbyansa.moviedb.data.local.entity.FavoriteMovieEntity

@Composable
fun FavoriteQuickActionRow(
    favorites: List<FavoriteMovieEntity>,
    onMovieClick: (Long) -> Unit
) {
    if (favorites.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Your Favorites",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { movie ->
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .clickable { onMovieClick(movie.id) }
                    ) {
                        Card(shape = RoundedCornerShape(8.dp)) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w185${movie.posterPath}",
                                contentDescription = movie.title,
                                modifier = Modifier.height(150.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}