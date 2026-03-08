package com.ratbyansa.moviedb.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.ratbyansa.moviedb.data.remote.model.MovieDetailResponse
import com.ratbyansa.moviedb.ui.screen.GenreScreen
import com.ratbyansa.moviedb.ui.screen.SearchScreen
import com.ratbyansa.moviedb.ui.screen.detail.MovieDetailScreen
import com.ratbyansa.moviedb.ui.screen.movie.MovieListScreen
import com.ratbyansa.moviedb.ui.screen.review.ReviewScreen
import com.ratbyansa.moviedb.ui.screen.trailer.TrailerPlayerScreen
import com.ratbyansa.moviedb.ui.viewmodel.FavoriteViewModel
import com.ratbyansa.moviedb.ui.viewmodel.GenreViewModel
import com.ratbyansa.moviedb.ui.viewmodel.MovieViewModel
import com.ratbyansa.moviedb.ui.viewmodel.ReviewViewModel
import com.ratbyansa.moviedb.ui.viewmodel.VideoViewModel
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.GenreList.route,
        modifier = modifier
    ) {
        composable(Screen.GenreList.route) {
            val viewModel: GenreViewModel = koinViewModel()
            val favoriteViewModel: FavoriteViewModel = koinViewModel()
            GenreScreen(
                viewModel = viewModel,
                onGenreClick = { genre ->
                    navController.navigate(Screen.MovieList.createRoute(genre.id, genre.name))
                },
                onSearchClick = {
                    navController.navigate(Screen.MovieSearch.createRoute())
                },
                favoriteViewModel = favoriteViewModel,
                onMovieClick = {
                    navController.navigate(Screen.MovieDetail.createRoute(it))
                }
            )
        }

        composable(Screen.MovieSearch.route) {
            val favViewModel: FavoriteViewModel = koinViewModel()
            SearchScreen(
                viewModel = koinViewModel(),
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movieId -> navController.navigate(Screen.MovieDetail.createRoute(movieId)) },
                favoriteViewModel = favViewModel
            )
        }

        composable(
            route = Screen.MovieList.route,
            arguments = listOf(
                navArgument("genreId") { type = NavType.LongType },
                navArgument("genreName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val genreId = backStackEntry.arguments?.getLong("genreId") ?: 0
            val genreName = backStackEntry.arguments?.getString("genreName") ?: ""
            val viewModel: MovieViewModel = koinViewModel()

            // Panggil fungsi untuk ambil data berdasarkan genreId
            LaunchedEffect(genreId) {
                viewModel.setGenre(genreId)
            }
            val movies = viewModel.moviePagingData.collectAsLazyPagingItems()
            MovieListScreen(
                genreName = genreName,
                moviePagingItems = movies,
                onMovieClick = { movieId ->
                    navController.navigate(Screen.MovieDetail.createRoute(movieId))
                },
                onBackClick = { navController.popBackStack() },
                onNavigateToSearch = {
                    navController.navigate(Screen.MovieSearch.createRoute())
                }
            )
        }

        composable(
            route = Screen.MovieDetail.route,
            arguments = listOf(navArgument("movieId") { type = NavType.LongType })
        ) { backStackEntry ->
            // Implementasi DetailScreen nanti
            val movieId = backStackEntry.arguments?.getLong("movieId") ?: 0L
            val videoViewModel: VideoViewModel = koinViewModel()
            MovieDetailScreen(
                movieId = movieId,
                videoViewModel = videoViewModel,
                onBackClick = { navController.popBackStack() },
                onSeeReview = { movieJson ->
                    navController.navigate(Screen.UserReviews.createRoute(movieJson))
                },
                onNavigateToPlayer = { videoKey, movieJson ->
                    navController.navigate(Screen.TrailerPlayer.createRoute(videoKey, movieJson))
                }
            )
        }

        composable(
            route = Screen.UserReviews.route,
            arguments = listOf(navArgument("movieData") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieJson = backStackEntry.arguments?.getString("movieData") ?: ""
            val movie = Json.decodeFromString<MovieDetailResponse>(Uri.decode(movieJson))

            ReviewScreen(
                movie = movie,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TrailerPlayer.route,
            arguments = listOf(
                navArgument("videoKey") { type = NavType.StringType },
                navArgument("movieData") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rawVideoKey = backStackEntry.arguments?.getString("videoKey") ?: ""
            val videoKey = Uri.decode(rawVideoKey) // Tambahkan baris ini!

            val rawMovieJson = backStackEntry.arguments?.getString("movieData") ?: ""
            val movieJson = Uri.decode(rawMovieJson)

            val movie = if (movieJson.isNotEmpty()) {
                Json.decodeFromString<MovieDetailResponse>(movieJson)
            } else {
                null // Berikan penanganan null jika perlu
            }

            if (movie != null) {
                TrailerPlayerScreen(
                    videoKey = videoKey,
                    movie = movie,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}