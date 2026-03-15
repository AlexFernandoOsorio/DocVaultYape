package com.example.docvaultyape.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.docvaultyape.presentation.screens.detail.DocumentDetailScreen
import com.example.docvaultyape.presentation.screens.home.HomeScreen

@Composable
fun DocVaultNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(onDocumentClick = { navController.navigate(Screen.Detail.createRoute(it)) })
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) {
            val documentId = it.arguments?.getString("documentId") ?: return@composable
            DocumentDetailScreen(documentId = documentId, onNavigateBack = { navController.popBackStack() })
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{documentId}") {
        fun createRoute(documentId: String) = "detail/$documentId"
    }
}
