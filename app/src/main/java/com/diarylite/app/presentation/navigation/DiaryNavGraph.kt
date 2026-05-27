package com.diarylite.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

    NavHost(
        navController = navController,
        startDestination = DiaryRoute.Home,
    ) {
        composable(DiaryRoute.Home) {
            HomeScreen(
                viewModel = viewModel,
                onWriteToday = { navController.navigate(DiaryRoute.addEntry()) },
                onOpenEntries = { navController.navigate(DiaryRoute.Entries) },
                onOpenCalendar = { navController.navigate(DiaryRoute.Calendar) },
                onOpenSearch = { navController.navigate(DiaryRoute.Search) },
                onOpenSettings = { navController.navigate(DiaryRoute.Settings) },
                onOpenEntry = { navController.navigate(DiaryRoute.detailEntry(it)) },
            )
        }
        composable(DiaryRoute.Entries) {
            EntriesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate(DiaryRoute.addEntry()) },
                onOpenEntry = { navController.navigate(DiaryRoute.detailEntry(it)) },
            )
        }
        composable(DiaryRoute.Calendar) {
            CalendarScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddForDate = { navController.navigate(DiaryRoute.addEntry(it)) },
                onOpenEntry = { navController.navigate(DiaryRoute.detailEntry(it)) },
            )
        }
        composable(
            route = DiaryRoute.AddEntryWithDate,
            arguments = listOf(
                navArgument("entryDateEpochDay") {
                    type = NavType.LongType
                    defaultValue = Long.MIN_VALUE
                },
            ),
        ) { backStackEntry ->
            val rawDate = backStackEntry.arguments?.getLong("entryDateEpochDay") ?: Long.MIN_VALUE
            EditorScreen(
                viewModel = viewModel,
                entryId = null,
                initialDateEpochDay = rawDate.takeIf { it != Long.MIN_VALUE },
                onBack = { navController.popBackStack() },
                onSaved = { entryId ->
                    navController.navigate(DiaryRoute.detailEntry(entryId)) {
                        popUpTo(DiaryRoute.AddEntryWithDate) { inclusive = true }
                    }
                },
            )
        }
        composable(DiaryRoute.AddEntry) {
            EditorScreen(
                viewModel = viewModel,
                entryId = null,
                initialDateEpochDay = null,
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
                initialDateEpochDay = null,
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
                onBack = { navController.popBackStack() },
                onOpenEntry = { navController.navigate(DiaryRoute.detailEntry(it)) },
            )
        }
        composable(DiaryRoute.Settings) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
