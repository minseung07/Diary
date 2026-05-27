package com.diarylite.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.diarylite.app.R
import com.diarylite.app.presentation.DiaryViewModel
import com.diarylite.app.presentation.screen.CalendarScreen
import com.diarylite.app.presentation.screen.DetailScreen
import com.diarylite.app.presentation.screen.EditorScreen
import com.diarylite.app.presentation.screen.EntriesScreen
import com.diarylite.app.presentation.screen.HomeScreen
import com.diarylite.app.presentation.screen.SearchScreen
import com.diarylite.app.presentation.screen.SettingsScreen

@Composable
fun DiaryNavGraph(viewModel: DiaryViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomBar: @Composable () -> Unit = {
        DiaryBottomBar(
            currentRoute = currentRoute,
            onNavigate = navController::navigateTopLevel,
        )
    }

    NavHost(
        navController = navController,
        startDestination = DiaryRoute.Home,
    ) {
        composable(DiaryRoute.Home) {
            HomeScreen(
                viewModel = viewModel,
                onWriteToday = { navController.navigate(DiaryRoute.addEntry()) },
                onOpenEntries = { navController.navigateTopLevel(DiaryRoute.Entries) },
                onOpenSearch = { navController.navigateTopLevel(DiaryRoute.Search) },
                onOpenSettings = { navController.navigateTopLevel(DiaryRoute.Settings) },
                onOpenEntry = { navController.navigate(DiaryRoute.detailEntry(it)) },
                bottomBar = bottomBar,
            )
        }
        composable(DiaryRoute.Entries) {
            EntriesScreen(
                viewModel = viewModel,
                onAdd = { navController.navigate(DiaryRoute.addEntry()) },
                onOpenEntry = { navController.navigate(DiaryRoute.detailEntry(it)) },
                bottomBar = bottomBar,
            )
        }
        composable(DiaryRoute.Calendar) {
            CalendarScreen(
                viewModel = viewModel,
                onOpenEntry = { navController.navigate(DiaryRoute.detailEntry(it)) },
                bottomBar = bottomBar,
            )
        }
        composable(DiaryRoute.AddEntry) {
            EditorScreen(
                viewModel = viewModel,
                entryId = null,
                onBack = { navController.popBackStack() },
                onSaved = { entryId ->
                    navController.navigate(DiaryRoute.detailEntry(entryId)) {
                        popUpTo(DiaryRoute.AddEntry) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = DiaryRoute.EditEntry,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
            EditorScreen(
                viewModel = viewModel,
                entryId = entryId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = DiaryRoute.DetailEntry,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
            DetailScreen(
                viewModel = viewModel,
                entryId = entryId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(DiaryRoute.editEntry(entryId)) },
                onDeleted = { navController.popBackStack() },
            )
        }
        composable(DiaryRoute.Search) {
            SearchScreen(
                viewModel = viewModel,
                onOpenEntry = { navController.navigate(DiaryRoute.detailEntry(it)) },
                bottomBar = bottomBar,
            )
        }
        composable(DiaryRoute.Settings) {
            SettingsScreen(
                viewModel = viewModel,
                bottomBar = bottomBar,
            )
        }
    }
}

@Composable
private fun DiaryBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        topLevelDestinations.forEach { destination ->
            val label = stringResource(destination.labelRes)
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = label,
                    )
                },
                label = { Text(label) },
            )
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(
        route = route,
        navOptions = navOptions {
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        },
    )
}

private data class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(DiaryRoute.Calendar, R.string.calendar_bottom_nav, Icons.Default.CalendarMonth),
    TopLevelDestination(DiaryRoute.Home, R.string.home_bottom_nav, Icons.Default.Home),
    TopLevelDestination(DiaryRoute.Entries, R.string.entries_bottom_nav, Icons.AutoMirrored.Filled.List),
)
