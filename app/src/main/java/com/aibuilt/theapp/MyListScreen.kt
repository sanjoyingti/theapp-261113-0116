package com.aibuilt.theapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyListScreen(
    ui: UiState,
    savedIds: Set<String>,
    onMovieClick: (TmdbMovie) -> Unit
) {
    val saved = ui.allMovies().filter { savedIds.contains(it.id.toString()) }
    Column(
        Modifier
            .fillMaxSize()
            .background(RhBackground)
    ) {
        Text(
            "My List",
            color = RhGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 10.dp)
        )
        if (saved.isEmpty()) {
            EmptyState(
                Icons.Filled.BookmarkBorder,
                "Your list is empty",
                "Tap My List on any film to save it here for later."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(saved, key = { it.id }) { m ->
                    PosterTile(m, width = 104) { onMovieClick(m) }
                }
            }
        }
    }
}
