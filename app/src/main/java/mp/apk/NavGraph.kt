package mp.apk

import ResultScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import mp.apk.presentation.components.BottomNavBar
import mp.apk.presentation.components.BottomNavItem
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import mp.apk.presentation.screens.LibraryScreen
import mp.apk.presentation.screens.MapScreen
import mp.apk.presentation.screens.ScanScreen
import mp.apk.viewmodel.LibraryViewModel
import mp.apk.viewmodel.ScanViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(R.string.library, Icons.AutoMirrored.Filled.LibraryBooks, "library"),
        BottomNavItem(R.string.scan, Icons.Default.Camera, "scan")
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val context = LocalContext.current
    val scanViewModel: ScanViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                items = items,
                currentRoute = currentRoute ?: "",
                onItemClick = { navController.navigate(it) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "library",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("library") { LibraryScreen(navController) }
            composable("scan") { ScanScreen(scanViewModel, navController) }
            composable("map") {MapScreen(context = context, viewModel =libraryViewModel) }
            composable(
                route = "result/{scanId}",
                arguments = listOf(navArgument("scanId") {
                    type = NavType.IntType
                })
            ) { backStackEntry ->
                val scanId = backStackEntry.arguments?.getInt("scanId") ?: return@composable
                ResultScreen(
                    scanId = scanId,
                    viewModel = hiltViewModel()
                )
            }
            composable("result") { ResultScreen(scanViewModel) }
        }
    }
}
