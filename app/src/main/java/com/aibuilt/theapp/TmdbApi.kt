package com.aibuilt.theapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/** A film as described by TMDB. It may not have a playable source. */
data class TmdbMovie(
    val id: Int,
    val title: String,
    val year: Int,
    val overview: String,
    val rating: Float,
    val posterPath: String,
    val backdropPath: String,
    val genreIds: List<Int> = emptyList(),
    val runtime: Int = 0,
    val cast: List<String> = emptyList()
) {
    val posterUrl: String get() = if (posterPath.isBlank()) "" else TMDB_IMAGE_BASE + posterPath
    val backdropUrl: String get() = if (backdropPath.isBlank()) "" else TMDB_BACKDROP_BASE + backdropPath
}

sealed class TmdbResult<out T> {
    data class Success<T>(val value: T) : TmdbResult<T>()
    data class Failure(val message: String) : TmdbResult<Nothing>()
}

class TmdbApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun get(apiKey: String, path: String, params: Map<String, String> = emptyMap()): TmdbResult<JsonObject> {
        val url = StringBuilder("https://api.themoviedb.org/3$path?api_key=$apiKey&language=en-US")
        params.forEach { (k, v) -> url.append("&").append(k).append("=").append(v) }
        return try {
            val request = Request.Builder().url(url.toString()).header("Accept", "application/json").build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                when {
                    response.code == 401 -> TmdbResult.Failure("That TMDB key was not accepted. Check it in Profile → Settings.")
                    response.code == 429 -> TmdbResult.Failure("TMDB is rate limiting us right now. Wait a minute, then retry.")
                    response.code >= 500 -> TmdbResult.Failure("TMDB is having trouble. Try again in a moment.")
                    !response.isSuccessful -> TmdbResult.Failure("TMDB returned an error (${response.code}). Retry in a moment.")
                    body.isBlank() -> TmdbResult.Failure("TMDB sent an empty answer. Retry in a moment.")
                    else -> TmdbResult.Success(json.parseToJsonElement(body).jsonObject)
                }
            }
        } catch (e: Exception) {
            TmdbResult.Failure("Could not reach TMDB. Check your connection and retry.")
        }
    }

    private fun optString(obj: JsonObject, key: String): String =
        obj[key]?.jsonPrimitive?.contentOrNullSafe() ?: ""

    private fun optInt(obj: JsonObject, key: String): Int =
        obj[key]?.jsonPrimitive?.intOrNull ?: 0

    private fun optDouble(obj: JsonObject, key: String): Double =
        obj[key]?.jsonPrimitive?.contentOrNullSafe()?.toDoubleOrNull() ?: 0.0

    private fun parseMovie(obj: JsonObject): TmdbMovie {
        val date = optString(obj, "release_date")
        val year = date.take(4).toIntOrNull() ?: 0
        val genres: List<Int> = obj["genre_ids"]?.let { el ->
            runCatching { el.jsonArray.mapNotNull { it.jsonPrimitive.intOrNull } }.getOrDefault(emptyList())
        } ?: emptyList()
        return TmdbMovie(
            id = optInt(obj, "id"),
            title = optString(obj, "title").ifBlank { optString(obj, "original_title") },
            year = year,
            overview = optString(obj, "overview"),
            rating = optDouble(obj, "vote_average").toFloat(),
            posterPath = optString(obj, "poster_path"),
            backdropPath = optString(obj, "backdrop_path"),
            genreIds = genres,
            runtime = optInt(obj, "runtime")
        )
    }

    private fun parseList(result: JsonObject): List<TmdbMovie> =
        result["results"]?.let { el ->
            runCatching { el.jsonArray.map { parseMovie(it.jsonObject) } }.getOrDefault(emptyList())
        } ?: emptyList()

    suspend fun trending(key: String): TmdbResult<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        when (val r = get(key, "/trending/movie/week")) {
            is TmdbResult.Success -> TmdbResult.Success(parseList(r.value))
            is TmdbResult.Failure -> r
        }
    }

    suspend fun nowPlaying(key: String): TmdbResult<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        when (val r = get(key, "/movie/now_playing")) {
            is TmdbResult.Success -> TmdbResult.Success(parseList(r.value))
            is TmdbResult.Failure -> r
        }
    }

    /** "New releases" uses the discover endpoint sorted by release date descending. */
    suspend fun newReleases(key: String): TmdbResult<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        when (val r = get(key, "/discover/movie", mapOf("sort_by" to "primary_release_date.desc", "vote_count.gte" to "20"))) {
            is TmdbResult.Success -> TmdbResult.Success(parseList(r.value))
            is TmdbResult.Failure -> r
        }
    }

    suspend fun popular(key: String): TmdbResult<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        when (val r = get(key, "/movie/popular")) {
            is TmdbResult.Success -> TmdbResult.Success(parseList(r.value))
            is TmdbResult.Failure -> r
        }
    }

    suspend fun topRated(key: String): TmdbResult<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        when (val r = get(key, "/movie/top_rated")) {
            is TmdbResult.Success -> TmdbResult.Success(parseList(r.value))
            is TmdbResult.Failure -> r
        }
    }

    suspend fun byGenre(key: String, genreId: Int): TmdbResult<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        when (val r = get(key, "/discover/movie", mapOf("with_genres" to genreId.toString(), "sort_by" to "popularity.desc"))) {
            is TmdbResult.Success -> TmdbResult.Success(parseList(r.value))
            is TmdbResult.Failure -> r
        }
    }

    suspend fun search(key: String, query: String): TmdbResult<List<TmdbMovie>> = withContext(Dispatchers.IO) {
        when (val r = get(key, "/search/movie", mapOf("query" to java.net.URLEncoder.encode(query, "UTF-8")))) {
            is TmdbResult.Success -> TmdbResult.Success(parseList(r.value))
            is TmdbResult.Failure -> r
        }
    }

    suspend fun details(key: String, movieId: Int): TmdbResult<TmdbMovie> = withContext(Dispatchers.IO) {
        when (val r = get(key, "/movie/$movieId")) {
            is TmdbResult.Success -> TmdbResult.Success(parseMovie(r.value))
            is TmdbResult.Failure -> r
        }
    }

    suspend fun credits(key: String, movieId: Int): TmdbResult<List<String>> = withContext(Dispatchers.IO) {
        when (val r = get(key, "/movie/$movieId/credits")) {
            is TmdbResult.Success -> {
                val cast: List<String> = r.value["cast"]?.let { el ->
                    runCatching {
                        el.jsonArray.mapNotNull { item ->
                            val o = item.jsonObject
                            o["name"]?.jsonPrimitive?.contentOrNullSafe()?.takeIf { it.isNotBlank() }
                        }.take(8)
                    }.getOrDefault(emptyList())
                } ?: emptyList()
                TmdbResult.Success(cast)
            }
            is TmdbResult.Failure -> r
        }
    }

    suspend fun genres(key: String): TmdbResult<List<Pair<Int, String>>> = withContext(Dispatchers.IO) {
        when (val r = get(key, "/genre/movie/list")) {
            is TmdbResult.Success -> {
                val list: List<Pair<Int, String>> = r.value["genres"]?.let { el ->
                    runCatching {
                        el.jsonArray.mapNotNull { item ->
                            val o = item.jsonObject
                            val id = o["id"]?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
                            val name = o["name"]?.jsonPrimitive?.contentOrNullSafe() ?: return@mapNotNull null
                            id to name
                        }
                    }.getOrDefault(emptyList())
                } ?: emptyList()
                TmdbResult.Success(list)
            }
            is TmdbResult.Failure -> r
        }
    }
}

private fun kotlinx.serialization.json.JsonPrimitive.contentOrNullSafe(): String? =
    if (this.isString) this.content else this.content.takeIf { it != "null" }

/** Serialises the cached catalogue so it survives between launches. */
fun encodeCatalogue(
    trending: List<TmdbMovie>,
    nowPlaying: List<TmdbMovie>,
    newReleases: List<TmdbMovie>
): String {
    val obj = JsonObject(
        mapOf(
            "trending" to JsonArray(trending.map { encodeMovie(it) }),
            "nowPlaying" to JsonArray(nowPlaying.map { encodeMovie(it) }),
            "newReleases" to JsonArray(newReleases.map { encodeMovie(it) })
        )
    )
    return obj.toString()
}

private fun encodeMovie(m: TmdbMovie): JsonObject = JsonObject(
    mapOf(
        "id" to kotlinx.serialization.json.JsonPrimitive(m.id),
        "title" to kotlinx.serialization.json.JsonPrimitive(m.title),
        "year" to kotlinx.serialization.json.JsonPrimitive(m.year),
        "overview" to kotlinx.serialization.json.JsonPrimitive(m.overview),
        "rating" to kotlinx.serialization.json.JsonPrimitive(m.rating),
        "poster" to kotlinx.serialization.json.JsonPrimitive(m.posterPath),
        "backdrop" to kotlinx.serialization.json.JsonPrimitive(m.backdropPath),
        "genres" to JsonArray(m.genreIds.map { kotlinx.serialization.json.JsonPrimitive(it) })
    )
)

data class CachedCatalogue(
    val trending: List<TmdbMovie>,
    val nowPlaying: List<TmdbMovie>,
    val newReleases: List<TmdbMovie>
)

fun decodeCatalogue(raw: String): CachedCatalogue? {
    if (raw.isBlank()) return null
    return runCatching {
        val root = Json.parseToJsonElement(raw).jsonObject
        fun list(key: String): List<TmdbMovie> =
            root[key]?.jsonArray?.map { el ->
                val o = el.jsonObject
                TmdbMovie(
                    id = o["id"]?.jsonPrimitive?.intOrNull ?: 0,
                    title = o["title"]?.jsonPrimitive?.contentOrNullSafe() ?: "",
                    year = o["year"]?.jsonPrimitive?.intOrNull ?: 0,
                    overview = o["overview"]?.jsonPrimitive?.contentOrNullSafe() ?: "",
                    rating = o["rating"]?.jsonPrimitive?.contentOrNullSafe()?.toFloatOrNull() ?: 0f,
                    posterPath = o["poster"]?.jsonPrimitive?.contentOrNullSafe() ?: "",
                    backdropPath = o["backdrop"]?.jsonPrimitive?.contentOrNullSafe() ?: "",
                    genreIds = o["genres"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()
                )
            } ?: emptyList()
        CachedCatalogue(list("trending"), list("nowPlaying"), list("newReleases"))
    }.getOrNull()
}
