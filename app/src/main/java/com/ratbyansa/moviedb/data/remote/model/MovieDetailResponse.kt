package com.ratbyansa.moviedb.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailResponse(
    val id: Long,
    val title: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    val runtime: Int = 0, // Dalam satuan menit
    val tagline: String = "",
    val genres: List<GenreDto> = emptyList(),
    val credits: CreditsResponse? = null
) {
    // Fungsi pembantu untuk memformat data ke UI
    val releaseYear: String get() = releaseDate.take(4)
    val formattedRuntime: String get() {
        val hours = runtime / 60
        val minutes = runtime % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}

@Serializable
data class CreditsResponse(
    val cast: List<CastDto> = emptyList()
)

@Serializable
data class CastDto(
    val id: Long,
    val name: String,
    val character: String,
    @SerialName("profile_path") val profilePath: String? = null
)