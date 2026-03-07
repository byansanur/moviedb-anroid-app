package com.ratbyansa.moviedb.ui.screen.detail.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ratbyansa.moviedb.data.remote.model.MovieDetailResponse

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