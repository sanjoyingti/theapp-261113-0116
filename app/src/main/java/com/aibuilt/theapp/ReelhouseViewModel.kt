package com.aibuilt.theapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeRail(val title: String, val movies: List<TmdbMovie>)

data class UiState(
    val loading: Boolean = true,
    val error: String? = null,
    val hasKey: Boolean = false,
    val lastUpdated: Long = 0L,
    val genreFilter: Int? = null,
    val trending: List<TmdbMovie> = emptyList(),
    val nowPlaying: List<TmdbMovie> = emptyList(),
    val newReleases: List<TmdbMovie> = emptyList(),
    val genreMovies: List<TmdbMovie> = emptyList(),
    val genreLoading: Boolean = false,
    val genreEmpty: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<TmdbMovie> = emptyList(),
    val searchLoading: Boolean = false,
    val searchError: String? = null,
    val searchDone: Boolean = false,
    val builtIn: List<PlayableMovie> = BuiltInMovies
) {
    fun allMovies(): List<TmdbMovie> = (trending + nowPlaying + newReleases + genreMovies + searchResults)
        .distinctBy { it.id }
}

class ReelhouseViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SettingsStore(app)
    private val api = TmdbApi()

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private var searchJob: Job? = null
    private var loadedKey: String? = null

    init {
        viewModelScope.launch {
            store.settings.collect { s ->
                val keyChanged = s.tmdbKey != loadedKey
                _settings.value = s
                _ui.value = _ui.value.copy(hasKey = s.tmdbKey.isNotBlank())
                if (keyChanged) {
                    loadedKey = s.tmdbKey
                    if (s.tmdbKey.isNotBlank()) {
                        loadCatalogue(forceRefresh = false, key = s.tmdbKey)
                    } else {
                        val cached = decodeCatalogue(s.catalogueJson)
                        _ui.value = _ui.value.copy(
                            loading = false,
                            trending = cached?.trending ?: emptyList(),
                            nowPlaying = cached?.nowPlaying ?: emptyList(),
                            newReleases = cached?.newReleases ?: emptyList(),
                            lastUpdated = s.catalogueTime
                        )
                    }
                }
            }
        }
    }

    // ---------- settings ----------

    fun setMovieServer(value: String) {
        viewModelScope.launch { store.setMovieServer(value) }
    }

    fun setTmdbKey(value: String) {
        viewModelScope.launch {
            store.setTmdbKey(value.trim())
            if (value.isBlank()) {
                _ui.value = _ui.value.copy(
                    error = null, loading = false,
                    trending = emptyList(), nowPlaying = emptyList(), newReleases = emptyList()
                )
            }
        }
    }

    fun toggleMyList(id: String) {
        viewModelScope.launch { store.toggleMyList(id) }
    }

    fun saveResume(id: String, position: Long) {
        viewModelScope.launch {
            val current = _settings.value.resume.toMutableMap()
            if (position < 5000L) current.remove(id) else current[id] = position
            store.saveResume(current)
        }
    }

    // ---------- catalogue ----------

    fun refresh() {
        val key = _settings.value.tmdbKey
        if (key.isBlank()) {
            _ui.value = _ui.value.copy(
                loading = false,
                error = null,
                lastUpdated = _settings.value.catalogueTime
            )
            return
        }
        viewModelScope.launch { loadCatalogue(forceRefresh = true, key = key) }
    }

    private suspend fun loadCatalogue(forceRefresh: Boolean, key: String) {
        val cached = decodeCatalogue(_settings.value.catalogueJson)
        if (!forceRefresh && cached != null) {
            _ui.value = _ui.value.copy(
                loading = false,
                trending = cached.trending,
                nowPlaying = cached.nowPlaying,
                newReleases = cached.newReleases,
                lastUpdated = _settings.value.catalogueTime
            )
        } else if (!forceRefresh) {
            _ui.value = _ui.value.copy(loading = true, error = null)
        } else {
            _ui.value = _ui.value.copy(loading = _ui.value.trending.isEmpty(), error = null)
        }

        val trending = api.trending(key)
        if (trending is TmdbResult.Failure) {
            _ui.value = _ui.value.copy(loading = false, error = trending.message)
            return
        }
        val nowPlaying = api.nowPlaying(key)
        val newOnes = api.newReleases(key)

        val t = (trending as TmdbResult.Success).value
        val n = (nowPlaying as? TmdbResult.Success)?.value ?: cached?.nowPlaying ?: emptyList()
        val r = (newOnes as? TmdbResult.Success)?.value ?: cached?.newReleases ?: emptyList()

        val time = System.currentTimeMillis()
        store.saveCatalogue(encodeCatalogue(t, n, r), time)

        _ui.value = _ui.value.copy(
            loading = false,
            error = null,
            trending = t,
            nowPlaying = n,
            newReleases = r,
            lastUpdated = time
        )
    }

    fun selectGenre(genreId: Int?) {
        if (genreId == null) {
            _ui.value = _ui.value.copy(genreFilter = null, genreMovies = emptyList(), genreEmpty = false)
            return
        }
        _ui.value = _ui.value.copy(genreFilter = genreId, genreLoading = true, genreEmpty = false, genreMovies = emptyList())
        val key = _settings.value.tmdbKey
        if (key.isBlank()) {
            val fallback = BuiltInMovies.filter { m -> m.genres.any { genreId(it) == genreId } }
                .map { it.toTmdbMovie() }
            _ui.value = _ui.value.copy(genreLoading = false, genreMovies = fallback, genreEmpty = fallback.isEmpty())
            return
        }
        viewModelScope.launch {
            when (val r = api.byGenre(key, genreId)) {
                is TmdbResult.Success -> _ui.value = _ui.value.copy(
                    genreLoading = false, genreMovies = r.value, genreEmpty = r.value.isEmpty()
                )
                is TmdbResult.Failure -> _ui.value = _ui.value.copy(
                    genreLoading = false, genreMovies = emptyList(), genreEmpty = true, error = r.message
                )
            }
        }
    }

    fun search(query: String) {
        _ui.value = _ui.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _ui.value = _ui.value.copy(searchResults = emptyList(), searchLoading = false, searchDone = false, searchError = null)
            return
        }
        val key = _settings.value.tmdbKey
        if (key.isBlank()) {
            val local = BuiltInMovies.filter {
                it.title.contains(query, true) || it.genres.any { g -> g.contains(query, true) }
            }.map { it.toTmdbMovie() }
            _ui.value = _ui.value.copy(searchResults = local, searchLoading = false, searchDone = true, searchError = null)
            return
        }
        searchJob = viewModelScope.launch {
            _ui.value = _ui.value.copy(searchLoading = true, searchError = null, searchDone = false)
            delay(350)
            when (val r = api.search(key, query)) {
                is TmdbResult.Success -> _ui.value = _ui.value.copy(
                    searchLoading = false, searchResults = r.value, searchDone = true, searchError = null
                )
                is TmdbResult.Failure -> _ui.value = _ui.value.copy(
                    searchLoading = false, searchResults = emptyList(), searchDone = true, searchError = r.message
                )
            }
        }
    }

    /** Loads full details plus cast for the detail sheet. */
    suspend fun loadDetails(movie: TmdbMovie): TmdbMovie {
        val key = _settings.value.tmdbKey
        if (key.isBlank() || movie.id == 0) return movie
        val detailed = api.details(key, movie.id)
        val cast = api.credits(key, movie.id)
        val base = (detailed as? TmdbResult.Success)?.value ?: movie
        val castNames = (cast as? TmdbResult.Success)?.value ?: emptyList()
        return base.copy(
            cast = castNames,
            genreIds = base.genreIds.ifEmpty { movie.genreIds },
            posterPath = base.posterPath.ifBlank { movie.posterPath },
            backdropPath = base.backdropPath.ifBlank { movie.backdropPath }
        )
    }

    fun findPlayable(movie: TmdbMovie): PlayableMovie? {
        val server = _settings.value.movieServer
        val builtIn = BuiltInMovies.firstOrNull {
            it.id == movie.id.toString() || it.title.equals(movie.title, true)
        }
        if (builtIn != null) return builtIn
        if (server.isNotBlank()) {
            val base = server.trimEnd('/')
            val path = if (base.contains("{id}")) base.replace("{id}", movie.id.toString())
            else "$base/${movie.id}"
            return PlayableMovie(
                id = movie.id.toString(),
                title = movie.title,
                year = movie.year,
                genres = movie.genreIds.map { genreName(it) },
                rating = movie.rating,
                overview = movie.overview,
                url = path,
                posterUrl = movie.posterUrl,
                backdropUrl = movie.backdropUrl,
                cast = movie.cast,
                runtimeMinutes = movie.runtime
            )
        }
        return null
    }
}

fun PlayableMovie.toTmdbMovie(): TmdbMovie = TmdbMovie(
    id = id.hashCode(),
    title = title,
    year = year,
    overview = overview,
    rating = rating,
    posterPath = "",
    backdropPath = "",
    genreIds = genres.map { genreId(it) },
    runtime = runtimeMinutes,
    cast = cast
)
