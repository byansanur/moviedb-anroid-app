package com.ratbyansa.moviedb.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.ratbyansa.moviedb.ui.screen.GenreScreen
import com.ratbyansa.moviedb.ui.screen.movie.MovieListScreen
import com.ratbyansa.moviedb.ui.viewmodel.GenreViewModel
import com.ratbyansa.moviedb.ui.viewmodel.MovieViewModel
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
            GenreScreen(
                viewModel = viewModel,
                onGenreClick = { genre ->
                    navController.navigate(Screen.MovieList.createRoute(genre.id, genre.name))
                }
            )
        }

        composable(
            route = Screen.MovieList.route,
            arguments = listOf(
                navArgument("genreId") { type = NavType.IntType },
                navArgument("genreName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val genreId = backStackEntry.arguments?.getInt("genreId") ?: 0
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
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MovieDetail.route,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) {
            // Implementasi DetailScreen nanti
        }
    }
}