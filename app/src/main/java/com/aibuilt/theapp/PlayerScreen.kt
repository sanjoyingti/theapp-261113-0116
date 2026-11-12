package com.aibuilt.theapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import android.view.ViewGroup

@Composable
fun PlayerScreen(
    movie: PlayableMovie,
    startPosition: Long,
    onBack: () -> Unit,
    onProgress: (Long) -> Unit
) {
    val context = LocalContext.current
    var isReady by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val player = remember(movie.url) {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(DefaultDataSource.Factory(context)))
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(movie.url))
                prepare()
                if (startPosition > 0) seekTo(startPosition)
                playWhenReady = true
            }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) isReady = true
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                errorText = "This film could not be played. Check the source in Profile → Settings, then retry."
                isReady = true
            }
        }
        player.addListener(listener)
        onDispose {
            onProgress(player.currentPosition)
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            onProgress(player.currentPosition)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isReady && errorText == null) {
            CircularProgressIndicator(color = RhGold, modifier = Modifier.align(Alignment.Center))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(alpha = 0.4f))) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                movie.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f)).padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (errorText != null) {
            Column(
                Modifier
                    .align(Alignment.Center)
                    .padding(28.dp)
                    .background(RhSurface.copy(alpha = 0.95f), androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(errorText!!, color = Color(0xFFF3F3F6), fontSize = 14.sp)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.Button(
                        onClick = {
                            errorText = null
                            isReady = false
                            player.prepare()
                            player.playWhenReady = true
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = RhGold, contentColor = RhBackground
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                    androidx.compose.material3.OutlinedButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            }
        }
    }
}
