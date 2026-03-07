package com.ratbyansa.moviedb.data.remote.model

import com.ratbyansa.moviedb.data.local.entity.MovieEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieResponse(
    val results: List<MovieDto>,
    val page: Int,
    @SerialName("total_pages") val totalPages: Int
)

@Serializable
data class MovieDto(
    val id: Long,
    val title: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("vote_average") val voteAverage: Double
)

fun MovieDto.toEntity(genreId: Long): MovieEntity {
    return MovieEntity(
        id = this.id,
        title = this.title,
        overview = this.overview,
        posterPath = this.posterPath,
        backdropPath = this.backdropPath,
        releaseDate = this.releaseDate,
        voteAverage = this.voteAverage,
        genreId = genreId
    )
}