package com.ratbyansa.moviedb.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.ktor.websocket.Frame

@Composable
fun ErrorStateUI(message: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Something went wrong", style = MaterialTheme.typography.titleMedium)
        Text(text = message ?: "Unknown error", color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) { Frame.Text("Retry") }
    }
}