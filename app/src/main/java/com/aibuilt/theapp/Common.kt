package com.aibuilt.theapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

val RhGold = Color(0xFFE8B23A)
val RhBackground = Color(0xFF0B0B0F)
val RhSurface = Color(0xFF15151C)
val RhSurfaceHigh = Color(0xFF1E1E27)
val RhTextDim = Color(0xFF9A9AA8)

@Composable
fun ReelhouseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = RhGold,
            onPrimary = Color(0xFF151004),
            secondary = RhGold,
            background = RhBackground,
            onBackground = Color(0xFFF3F3F6),
            surface = RhSurface,
            onSurface = Color(0xFFF3F3F6),
            surfaceVariant = RhSurfaceHigh,
            onSurfaceVariant = RhTextDim,
            error = Color(0xFFE5484D)
        ),
        content = content
    )
}

/** A movie the app can actually play. */
data class PlayableMovie(
    val id: String,
    val title: String,
    val year: Int,
    val genres: List<String>,
    val rating: Float,
    val overview: String,
    val url: String,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val cast: List<String> = emptyList(),
    val runtimeMinutes: Int = 0
)

/** Built-in public domain catalogue used when no TMDB key is set. */
val BuiltInMovies: List<PlayableMovie> = listOf(
    PlayableMovie(
        id = "bigbuck",
        title = "Big Buck Bunny",
        year = 2008,
        genres = listOf("Animation", "Comedy", "Family"),
        rating = 8.1f,
        overview = "A gentle giant rabbit is pushed too far by three bullying rodents. Big Buck Bunny is a Blender Foundation open movie, released to the world under a Creative Commons licence.",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        posterUrl = "https://peach.blender.org/wp-content/uploads/title_anouncement.jpg"
    ),
    PlayableMovie(
        id = "sintel",
        title = "Sintel",
        year = 2010,
        genres = listOf("Animation", "Fantasy", "Adventure"),
        rating = 7.6f,
        overview = "A lone girl wanders a harsh world in search of the dragon she once befriended. A Blender Foundation open movie released under a Creative Commons licence.",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        posterUrl = "https://durian.blender.org/wp-content/uploads/2010/06/sintel_poster.jpg"
    ),
    PlayableMovie(
        id = "tears",
        title = "Tears of Steel",
        year = 2012,
        genres = listOf("Science Fiction", "Drama"),
        rating = 6.8f,
        overview = "Amsterdam, 2050: a group of scientists try to save the world from robots using memories of love. A Blender Foundation open movie under a Creative Commons licence.",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        posterUrl = "https://mango.blender.org/wp-content/uploads/2013/05/01_thom_celia_bridge.jpg"
    ),
    PlayableMovie(
        id = "elephantsdream",
        title = "Elephants Dream",
        year = 2006,
        genres = listOf("Animation", "Science Fiction", "Fantasy"),
        rating = 6.5f,
        overview = "Two characters explore an infinite mechanical world. The first Blender Foundation open movie, released under a Creative Commons licence.",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        posterUrl = "https://orange.blender.org/wp-content/themes/orange/images/media/postcard_poster_small.jpg"
    ),
    PlayableMovie(
        id = "forbiggerfun",
        title = "For Bigger Fun",
        year = 2013,
        genres = listOf("Comedy", "Adventure"),
        rating = 6.4f,
        overview = "A short public sample clip from the Google video bucket, used here to demonstrate playback.",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
    ),
    PlayableMovie(
        id = "subaru",
        title = "Subaru Outback On Street And Dirt",
        year = 2012,
        genres = listOf("Documentary"),
        rating = 6.0f,
        overview = "A short public sample clip from the Google video bucket, used here to demonstrate playback.",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
    )
)

/** The 19 official TMDB genres. */
val OfficialGenres: List<Pair<Int, String>> = listOf(
    28 to "Action",
    12 to "Adventure",
    16 to "Animation",
    35 to "Comedy",
    80 to "Crime",
    99 to "Documentary",
    18 to "Drama",
    10751 to "Family",
    14 to "Fantasy",
    36 to "History",
    27 to "Horror",
    10402 to "Music",
    9648 to "Mystery",
    10749 to "Romance",
    878 to "Science Fiction",
    10770 to "TV Movie",
    53 to "Thriller",
    10752 to "War",
    37 to "Western"
)

fun genreName(id: Int): String = OfficialGenres.firstOrNull { it.first == id }?.second ?: "Drama"

fun genreId(name: String): Int = OfficialGenres.firstOrNull { it.second == name }?.first ?: 18

fun formatRuntime(minutes: Int): String {
    if (minutes <= 0) return ""
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

fun relativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return "never"
    val diff = System.currentTimeMillis() - timestamp
    if (diff < 0) return "just now"
    val mins = diff / 60000L
    return when {
        mins < 1 -> "just now"
        mins < 60 -> "$mins min ago"
        mins < 60 * 24 -> "${mins / 60} hr ago"
        else -> "${mins / (60 * 24)} days ago"
    }
}

fun ratingText(value: Float): String {
    val r = (value * 10).roundToInt() / 10.0
    return "★ " + String.format(Locale.US, "%.1f", r)
}

fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(millis))

const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500"
const val TMDB_BACKDROP_BASE = "https://image.tmdb.org/t/p/w780"
const val TMDB_ATTRIBUTION =
    "This product uses the TMDB API but is not endorsed or certified by TMDB."
