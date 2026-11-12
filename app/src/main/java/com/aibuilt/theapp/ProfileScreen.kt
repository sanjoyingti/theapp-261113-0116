package com.aibuilt.theapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    settings: UserSettings,
    onSaveServer: (String) -> Unit,
    onSaveTmdbKey: (String) -> Unit,
    onSignOut: () -> Unit
) {
    var server by remember(settings.movieServer) { mutableStateOf(settings.movieServer) }
    var tmdb by remember(settings.tmdbKey) { mutableStateOf(settings.tmdbKey) }

    Column(
        Modifier
            .fillMaxSize()
            .background(RhBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Profile",
            color = RhGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 18.dp, bottom = 14.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .background(RhSurface, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = RhGold, modifier = Modifier.size(34.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    settings.signedInEmail.ifBlank { "guest@reelhouse.app" },
                    color = Color(0xFFF3F3F6),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text("Signed in", color = RhTextDim, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(22.dp))
        Text("SETTINGS", color = RhTextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = tmdb,
            onValueChange = { tmdb = it },
            label = { Text("TMDB API key") },
            placeholder = { Text("Paste your v3 API key", color = RhTextDim) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = RhSurface,
                unfocusedContainerColor = RhSurface,
                focusedTextColor = Color(0xFFF3F3F6),
                unfocusedTextColor = Color(0xFFF3F3F6)
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Powers trending films, genres, posters and descriptions. Without a key the app uses its small built-in public-domain catalogue.",
            color = RhTextDim,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { onSaveTmdbKey(tmdb.trim()) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RhGold, contentColor = RhBackground)
        ) {
            Text("Save TMDB key", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(Modifier.height(22.dp))
        OutlinedTextField(
            value = server,
            onValueChange = { server = it },
            label = { Text("Movie server") },
            placeholder = { Text("https://my-server.example/films", color = RhTextDim) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = RhSurface,
                unfocusedContainerColor = RhSurface,
                focusedTextColor = Color(0xFFF3F3F6),
                unfocusedTextColor = Color(0xFFF3F3F6)
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Your own licensed source. Use {id} where the film number should go, e.g. https://my-server/{id}.mp4",
            color = RhTextDim,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { onSaveServer(server.trim()) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RhSurfaceHigh, contentColor = Color(0xFFF3F3F6))
        ) {
            Text("Save server", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(Modifier.height(26.dp))
        OutlinedButton(
            onClick = onSignOut,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sign out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(Modifier.height(22.dp))
        Text(TMDB_ATTRIBUTION, color = RhTextDim, fontSize = 10.sp)
        Spacer(Modifier.height(8.dp))
        Text("Reelhouse plays only public-domain films and your own licensed sources.", color = RhTextDim, fontSize = 10.sp)
        Spacer(Modifier.height(28.dp))
    }
}
