package com.aibuilt.theapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchScreen(
    ui: UiState,
    onQueryChange: (String) -> Unit,
    onMovieClick: (TmdbMovie) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(RhBackground)
    ) {
        Text(
            "Search",
            color = RhGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 10.dp)
        )
        OutlinedTextField(
            value = ui.searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search films, genres…", color = RhTextDim) },
            leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Search, contentDescription = null, tint = RhTextDim) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = RhSurface,
                unfocusedContainerColor = RhSurface,
                focusedTextColor = androidx.compose.ui.graphics.Color(0xFFF3F3F6),
                unfocusedTextColor = androidx.compose.ui.graphics.Color(0xFFF3F3F6)
            )
        )
        Spacer(Modifier.height(12.dp))

        when {
            ui.searchLoading -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(9) { PosterSkeleton(Modifier.fillMaxWidth().aspectRatio(2f / 3f)) }
                }
            }
            ui.searchError != null -> {
                ErrorState(ui.searchError!!) { onQueryChange(ui.searchQuery) }
            }
            ui.searchDone && ui.searchResults.isEmpty() -> {
                EmptyState(
                    Icons.Filled.Search,
                    "No films found",
                    "Nothing matched “${ui.searchQuery}”. Try a different title or genre."
                )
            }
            ui.searchResults.isEmpty() && ui.searchQuery.isBlank() -> {
                EmptyState(
                    Icons.Filled.Search,
                    "Find something to watch",
                    "Search by film title, or try a genre like “horror”."
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ui.searchResults, key = { it.id }) { m ->
                        PosterTile(m, width = 104) { onMovieClick(m) }
                    }
                }
            }
        }
    }
}
