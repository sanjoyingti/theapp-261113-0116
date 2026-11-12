package com.aibuilt.theapp

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun RatingBadge(value: Float, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(RhGold.copy(alpha = 0.92f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = RhBackground, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(2.dp))
        Text(
            String.format(java.util.Locale.US, "%.1f", value),
            color = RhBackground,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PosterSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "sk")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        modifier
            .background(RhSurfaceHigh.copy(alpha = alpha), RoundedCornerShape(12.dp))
    )
}

@Composable
fun PosterTile(
    movie: TmdbMovie,
    width: Int = 130,
    onClick: () -> Unit
) {
    var failed by remember(movie.id) { mutableStateOf(false) }
    Column(
        Modifier
            .width(width.dp)
            .clickable { onClick() }
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .background(RhSurfaceHigh, RoundedCornerShape(12.dp))
        ) {
            if (movie.posterUrl.isNotBlank() && !failed) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = { failed = true }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        movie.title.take(1).uppercase(),
                        color = RhGold,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            RatingBadge(
                movie.rating,
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            movie.title,
            color = Color(0xFFF3F3F6),
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            if (movie.year > 0) movie.year.toString() else "",
            color = RhTextDim,
            fontSize = 11.sp
        )
    }
}

@Composable
fun MovieRail(
    title: String,
    movies: List<TmdbMovie>,
    loading: Boolean,
    onMovieClick: (TmdbMovie) -> Unit
) {
    if (!loading && movies.isEmpty()) return
    Column(Modifier.padding(bottom = 18.dp)) {
        Text(
            title,
            color = Color(0xFFF3F3F6),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (loading && movies.isEmpty()) {
                items(6) { PosterSkeleton(Modifier.width(130.dp).aspectRatio(2f / 3f)) }
            } else {
                items(movies, key = { it.id }) { m ->
                    PosterTile(m) { onMovieClick(m) }
                }
            }
        }
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = RhTextDim, modifier = Modifier.size(44.dp))
        Spacer(Modifier.height(10.dp))
        Text(title, color = Color(0xFFF3F3F6), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text(body, color = RhTextDim, fontSize = 13.sp)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Something went wrong", color = Color(0xFFF3F3F6), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text(message, color = RhTextDim, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = RhGold, contentColor = RhBackground),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Retry", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AttributionLine(modifier: Modifier = Modifier) {
    Text(
        TMDB_ATTRIBUTION,
        color = RhTextDim,
        fontSize = 10.sp,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun DetailSheet(
    movie: TmdbMovie,
    isInList: Boolean,
    hasSource: Boolean,
    loadingDetails: Boolean,
    onClose: () -> Unit,
    onPlay: () -> Unit,
    onOpenServer: () -> Unit,
    onToggleList: () -> Unit
) {
    var details by remember(movie.id) { mutableStateOf(movie) }

    androidx.compose.material3.Surface(
        color = RhBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Box(Modifier.fillMaxWidth().height(240.dp)) {
                    if (details.backdropUrl.isNotBlank()) {
                        AsyncImage(
                            model = details.backdropUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(Modifier.fillMaxSize().background(RhSurfaceHigh))
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, RhBackground.copy(alpha = 0.2f), RhBackground)
                                )
                            )
                    )
                    Row(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            "✕",
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .clickable { onClose() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            item {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        details.title,
                        color = Color(0xFFF3F3F6),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (details.year > 0) {
                            Text("${details.year}", color = RhTextDim, fontSize = 13.sp)
                            Text("  •  ", color = RhTextDim, fontSize = 13.sp)
                        }
                        val rt = formatRuntime(details.runtime)
                        if (rt.isNotBlank()) {
                            Text(rt, color = RhTextDim, fontSize = 13.sp)
                            Text("  •  ", color = RhTextDim, fontSize = 13.sp)
                        }
                        Text(ratingText(details.rating), color = RhGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                    if (details.genreIds.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(details.genreIds.size) { idx ->
                                val gid = details.genreIds[idx]
                                Text(
                                    genreName(gid),
                                    color = RhGold,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .background(RhSurfaceHigh, RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    if (loadingDetails) {
                        CircularProgressIndicator(color = RhGold, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.height(10.dp))
                    }
                    Text(
                        details.overview.ifBlank { "No description available for this title." },
                        color = Color(0xFFD2D2DA),
                        fontSize = 14.sp
                    )
                    if (details.cast.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text("Cast", color = Color(0xFFF3F3F6), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(details.cast.joinToString(", "), color = RhTextDim, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (hasSource) {
                            Button(
                                onClick = onPlay,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RhGold, contentColor = RhBackground)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Play", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onOpenServer,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Open in My Server", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = onToggleList,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (isInList) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (isInList) "In My List" else "My List", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (!hasSource) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "This title needs a licensed source before it can play. Point the app at your own library in Profile → Settings and it will play from there.",
                            color = RhTextDim,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    AttributionLine(Modifier.padding(horizontal = 0.dp))
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
