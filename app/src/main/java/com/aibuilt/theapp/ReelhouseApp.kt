package com.aibuilt.theapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun ReelhouseApp() {
    ReelhouseTheme {
        val context = LocalContext.current
        val vm: ReelhouseViewModel = viewModel()
        val ui by vm.ui.collectAsState()
        val settings by vm.settings.collectAsState()
        val scope = rememberCoroutineScope()

        var tab by remember { mutableStateOf(0) }
        var playing by remember { mutableStateOf<PlayableMovie?>(null) }
        var detail by remember { mutableStateOf<TmdbMovie?>(null) }
        var detailLoading by remember { mutableStateOf(false) }
        var showServerHint by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            vm.refresh()
        }

        Box(Modifier.fillMaxSize().background(RhBackground)) {
            val current = playing
            if (current != null) {
                PlayerScreen(
                    movie = current,
                    startPosition = settings.resume[current.id] ?: 0L,
                    onBack = { playing = null },
                    onProgress = { pos -> vm.saveResume(current.id, pos) }
                )
                return@Box
            }

            if (settings.signedInEmail.isBlank() && !settings.signedInEmail.isNotBlank()) {
                // shown below via state
            }

            Column(Modifier.fillMaxSize()) {
                if (settings.signedInEmail.isBlank()) {
                    SignInScreen { email ->
                        scope.launch { SettingsStore(context).setSignedIn(email) }
                    }
                } else {
                    Box(Modifier.weight(1f)) {
                        when (tab) {
                            0 -> HomeScreen(
                                ui = ui,
                                onGenreSelected = vm::selectGenre,
                                onMovieClick = { m ->
                                    detail = m
                                    detailLoading = true
                                    scope.launch {
                                        val full = vm.loadDetails(m)
                                        if (detail?.id == full.id) detail = full
                                        detailLoading = false
                                    }
                                },
                                onRefresh = vm::refresh,
                                onOpenSearch = { tab = 1 }
                            )

                            1 -> SearchScreen(
                                ui = ui,
                                onQueryChange = vm::search,
                                onMovieClick = { m ->
                                    detail = m
                                    detailLoading = true
                                    scope.launch {
                                        val full = vm.loadDetails(m)
                                        if (detail?.id == full.id) detail = full
                                        detailLoading = false
                                    }
                                }
                            )

                            2 -> MyListScreen(
                                ui = ui,
                                savedIds = settings.myList,
                                onMovieClick = { m ->
                                    detail = m
                                    detailLoading = true
                                    scope.launch {
                                        val full = vm.loadDetails(m)
                                        if (detail?.id == full.id) detail = full
                                        detailLoading = false
                                    }
                                }
                            )

                            else -> ProfileScreen(
                                settings = settings,
                                onSaveServer = vm::setMovieServer,
                                onSaveTmdbKey = { key ->
                                    vm.setTmdbKey(key)
                                },
                                onSignOut = {
                                    scope.launch { SettingsStore(context).signOut() }
                                }
                            )
                        }
                    }

                    NavigationBar(containerColor = RhSurface) {
                        NavigationBarItem(
                            selected = tab == 0,
                            onClick = { tab = 0 },
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.size(20.dp)) },
                            label = { Text("Home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RhGold,
                                selectedTextColor = RhGold,
                                unselectedIconColor = RhTextDim,
                                unselectedTextColor = RhTextDim,
                                indicatorColor = RhSurfaceHigh
                            )
                        )
                        NavigationBarItem(
                            selected = tab == 1,
                            onClick = { tab = 1 },
                            icon = { Icon(Icons.Filled.Search, contentDescription = "Search", modifier = Modifier.size(20.dp)) },
                            label = { Text("Search") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RhGold,
                                selectedTextColor = RhGold,
                                unselectedIconColor = RhTextDim,
                                unselectedTextColor = RhTextDim,
                                indicatorColor = RhSurfaceHigh
                            )
                        )
                        NavigationBarItem(
                            selected = tab == 2,
                            onClick = { tab = 2 },
                            icon = { Icon(Icons.Filled.Bookmark, contentDescription = "My List", modifier = Modifier.size(20.dp)) },
                            label = { Text("My List") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RhGold,
                                selectedTextColor = RhGold,
                                unselectedIconColor = RhTextDim,
                                unselectedTextColor = RhTextDim,
                                indicatorColor = RhSurfaceHigh
                            )
                        )
                        NavigationBarItem(
                            selected = tab == 3,
                            onClick = { tab = 3 },
                            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile", modifier = Modifier.size(20.dp)) },
                            label = { Text("Profile") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RhGold,
                                selectedTextColor = RhGold,
                                unselectedIconColor = RhTextDim,
                                unselectedTextColor = RhTextDim,
                                indicatorColor = RhSurfaceHigh
                            )
                        )
                    }
                }
            }

            val shownDetail = detail
            if (shownDetail != null) {
                DetailSheet(
                    movie = shownDetail,
                    isInList = settings.myList.contains(shownDetail.id.toString()),
                    hasSource = vm.findPlayable(shownDetail) != null,
                    loadingDetails = detailLoading,
                    onClose = { detail = null },
                    onPlay = {
                        val playable = vm.findPlayable(shownDetail)
                        if (playable != null) {
                            detail = null
                            playing = playable
                        }
                    },
                    onOpenServer = {
                        detail = null
                        tab = 3
                        showServerHint = true
                    },
                    onToggleList = { vm.toggleMyList(shownDetail.id.toString()) }
                )
            }
        }
    }
}
