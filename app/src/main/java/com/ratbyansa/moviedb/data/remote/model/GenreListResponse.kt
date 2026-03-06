package com.ratbyansa.moviedb.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenreListResponse(
    @SerialName("genres")
    val genres: List<GenreDto>
)

@Serializable
data class GenreDto(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String
)