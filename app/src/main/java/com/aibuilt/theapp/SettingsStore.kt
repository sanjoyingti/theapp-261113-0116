package com.aibuilt.theapp

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "reelhouse_prefs")

object Keys {
    val MOVIE_SERVER = stringPreferencesKey("movie_server")
    val TMDB_KEY = stringPreferencesKey("tmdb_key")
    val MY_LIST = stringPreferencesKey("my_list")
    val RESUME = stringPreferencesKey("resume_positions")
    val SIGNED_IN = stringPreferencesKey("signed_in_email")
    val CATALOGUE = stringPreferencesKey("catalogue_json")
    val CATALOGUE_TIME = longPreferencesKey("catalogue_time")
}

data class UserSettings(
    val movieServer: String = "",
    val tmdbKey: String = "",
    val myList: Set<String> = emptySet(),
    val resume: Map<String, Long> = emptyMap(),
    val signedInEmail: String = "",
    val catalogueJson: String = "",
    val catalogueTime: Long = 0L
)

private fun decodeResume(raw: String): Map<String, Long> =
    raw.split(";")
        .filter { it.contains(":") }
        .mapNotNull { entry ->
            val key = entry.substringBeforeLast(":")
            val value = entry.substringAfterLast(":").toLongOrNull() ?: 0L
            if (key.isBlank()) null else key to value
        }
        .toMap()

private fun encodeResume(map: Map<String, Long>): String =
    map.entries.joinToString(";") { "${it.key}:${it.value}" }

fun Preferences.toSettings(): UserSettings = UserSettings(
    movieServer = this[Keys.MOVIE_SERVER] ?: "",
    tmdbKey = this[Keys.TMDB_KEY] ?: "",
    myList = (this[Keys.MY_LIST] ?: "").split(",").filter { it.isNotBlank() }.toSet(),
    resume = decodeResume(this[Keys.RESUME] ?: ""),
    signedInEmail = this[Keys.SIGNED_IN] ?: "",
    catalogueJson = this[Keys.CATALOGUE] ?: "",
    catalogueTime = this[Keys.CATALOGUE_TIME] ?: 0L
)

class SettingsStore(private val context: Context) {

    val settings: Flow<UserSettings> = context.dataStore.data.map { it.toSettings() }

    suspend fun setMovieServer(value: String) {
        context.dataStore.edit { it[Keys.MOVIE_SERVER] = value }
    }

    suspend fun setTmdbKey(value: String) {
        context.dataStore.edit { it[Keys.TMDB_KEY] = value }
    }

    suspend fun setSignedIn(email: String) {
        context.dataStore.edit { it[Keys.SIGNED_IN] = email }
    }

    suspend fun signOut() {
        context.dataStore.edit { it[Keys.SIGNED_IN] = "" }
    }

    suspend fun toggleMyList(id: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[Keys.MY_LIST] ?: "")
                .split(",")
                .filter { it.isNotBlank() }
                .toMutableSet()
            if (!current.add(id)) current.remove(id)
            prefs[Keys.MY_LIST] = current.joinToString(",")
        }
    }

    suspend fun saveResume(map: Map<String, Long>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.RESUME] = encodeResume(map)
        }
    }

    suspend fun saveCatalogue(json: String, time: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CATALOGUE] = json
            prefs[Keys.CATALOGUE_TIME] = time
        }
    }
}
