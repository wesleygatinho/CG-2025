package br.edu.historiaviva.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.edu.historiaviva.ui.screens.ArRoute
import br.edu.historiaviva.ui.screens.ArDemoRoute
import br.edu.historiaviva.ui.screens.DetailRoute
import br.edu.historiaviva.ui.screens.GalleryRoute
import br.edu.historiaviva.ui.screens.WelcomeRoute

@Composable
fun App(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.Welcome
    ) {
        composable(Routes.Welcome) {
            WelcomeRoute(onStart = { navController.navigate(Routes.Gallery) })
        }
        composable(Routes.Gallery) {
            GalleryRoute(
                onCharacterSelected = { id -> navController.navigate(Routes.detail(id)) }
            )
        }
        composable(Routes.Detail) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("id")
            DetailRoute(
                characterId = characterId,
                onBack = { navController.popBackStack() },
                onOpenAr = { id -> navController.navigate(Routes.ar(id)) }
            )
        }
        composable(Routes.Ar) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("id")
            ArRoute(
                characterId = characterId,
                onBack = { navController.popBackStack() },
                onOpenInfo = { id -> navController.navigate(Routes.detail(id)) },
                onOpenDemo = { id -> navController.navigate(Routes.arDemo(id)) }
            )
        }
        composable(Routes.ArDemo) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("id")
            ArDemoRoute(
                characterId = characterId,
                onBack = { navController.popBackStack() },
                onOpenInfo = { id -> navController.navigate(Routes.detail(id)) }
            )
        }
    }
}
