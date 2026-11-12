package com.aibuilt.theapp

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ui: UiState,
    onGenreSelected: (Int?) -> Unit,
    onMovieClick: (TmdbMovie) -> Unit,
    onRefresh: () -> Unit,
    onOpenSearch: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = ui.loading && ui.trending.isNotEmpty(),
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .background(RhBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 18.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("REELHOUSE", color = RhGold, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(
                            "Updated " + relativeTime(ui.lastUpdated),
                            color = RhTextDim,
                            fontSize = 11.sp
                        )
                    }
                    if (ui.loading) {
                        CircularProgressIndicator(
                            color = RhGold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = RhGold)
                    }
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = RhGold)
                    }
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    item {
                        GenreChip("All", ui.genreFilter == null) { onGenreSelected(null) }
                    }
                    items(OfficialGenres) { g ->
                        GenreChip(g.second, ui.genreFilter == g.first) { onGenreSelected(g.first) }
                    }
                }
            }

            if (ui.error != null && ui.trending.isEmpty()) {
                item { ErrorState(ui.error!!) { onRefresh() } }
            }

            if (ui.genreFilter != null) {
                item {
                    Column(Modifier.padding(top = 6.dp)) {
                        Text(
                            genreName(ui.genreFilter),
                            color = RhTextDim,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
                        )
                    }
                }
                if (ui.genreLoading) {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(6) { PosterSkeleton(Modifier.width(130.dp).height(195.dp)) }
                        }
                    }
                } else if (ui.genreMovies.isEmpty()) {
                    item {
                        EmptyState(
                            Icons.Filled.Search,
                            "Nothing in this genre yet",
                            "Try another genre, or refresh to pull the latest films."
                        )
                    }
                } else {
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(gridHeight(ui.genreMovies.size)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            userScrollEnabled = false
                        ) {
                            items(ui.genreMovies, key = { it.id }) { m ->
                                PosterTile(m, width = 106) { onMovieClick(m) }
                            }
                        }
                    }
                }
            } else {
                item {
                    HeroRow(ui, onMovieClick)
                }
                item {
                    MovieRail(
                        "Trending this week",
                        ui.trending,
                        ui.loading,
                        onMovieClick
                    )
                }
                item {
                    MovieRail(
                        "Now playing",
                        ui.nowPlaying,
                        ui.loading,
                        onMovieClick
                    )
                }
                item {
                    MovieRail(
                        "New releases",
                        ui.newReleases,
                        ui.loading,
                        onMovieClick
                    )
                }
                item {
                    MovieRail(
                        "Action",
                        ui.allMovies().filter { it.genreIds.contains(28) },
                        ui.loading,
                        onMovieClick
                    )
                }
                item {
                    MovieRail(
                        "Comedy",
                        ui.allMovies().filter { it.genreIds.contains(35) },
                        ui.loading,
                        onMovieClick
                    )
                }
                item {
                    MovieRail(
                        "Drama",
                        ui.allMovies().filter { it.genreIds.contains(18) },
                        ui.loading,
                        onMovieClick
                    )
                }
                item {
                    MovieRail(
                        "Science Fiction",
                        ui.allMovies().filter { it.genreIds.contains(878) },
                        ui.loading,
                        onMovieClick
                    )
                }
            }

            item { AttributionLine() }
        }
    }
}

private fun gridHeight(count: Int): androidx.compose.ui.unit.Dp {
    val rows = (count + 2) / 3
    val perRow = 106 + 40
    return (rows * perRow).dp
}

@Composable
private fun HeroRow(ui: UiState, onMovieClick: (TmdbMovie) -> Unit) {
    val featured = ui.trending.firstOrNull()
    if (featured == null) {
        if (ui.loading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(180.dp)
            ) {
                PosterSkeleton(Modifier.fillMaxSize())
            }
        }
        return
    }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Featured today", color = RhTextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(RhSurfaceHigh, RoundedCornerShape(16.dp))
                .clickable { onMovieClick(featured) }
        ) {
            coil3.compose.AsyncImage(
                model = featured.backdropUrl.ifBlank { featured.posterUrl },
                contentDescription = featured.title,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(
                                androidx.compose.ui.graphics.Color.Transparent,
                                RhBackground
                            )
                        )
                    )
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    featured.title,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    ratingText(featured.rating) + if (featured.year > 0) "  •  ${featured.year}" else "",
                    color = RhGold,
                    fontSize = 12.sp
                )
            }
            RatingBadge(featured.rating, Modifier.align(Alignment.TopEnd).padding(10.dp))
        }
    }
}

@Composable
fun GenreChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .background(
                if (selected) RhGold else RhSurfaceHigh,
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            color = if (selected) RhBackground else RhTextDim,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
