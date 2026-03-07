package com.ratbyansa.moviedb.ui.navigation

sealed class Screen(val route: String) {
    object GenreList : Screen("genre_list")

    // Rute dengan argumen: genreId dan genreName untuk Judul Toolbar
    object MovieList : Screen("movie_list/{genreId}/{genreName}") {
        fun createRoute(genreId: Long, genreName: String) = "movie_list/$genreId/$genreName"
    }

    object MovieDetail : Screen("movie_detail/{movieId}") {
        fun createRoute(movieId: Long) = "movie_detail/$movieId"
    }

    object MovieSearch: Screen("search") {
        fun createRoute() = "search"
    }

    object UserReviews : Screen("user_reviews/{movieId}") {
        fun createRoute(movieId: Long) = "user_reviews/$movieId"
    }

    object TrailerPlayer : Screen("trailer_player/{videoKey}") {
        fun createRoute(videoKey: String) = "trailer_player/$videoKey"
    }
}