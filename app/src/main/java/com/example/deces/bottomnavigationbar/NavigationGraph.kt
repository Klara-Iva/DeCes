package com.example.deces.bottomnavigationbar

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.deces.AllEventsScreen
import com.example.deces.CalendarScreen
import com.example.deces.EventDetailScreen
import com.example.deces.FavouritesScreen
import com.example.deces.HomeScreen
import com.example.deces.MapScreen
import com.example.deces.RegisterScreen

import com.example.deces.Screen3

import com.example.deces.Screen5

@Composable
fun NavigationGraph(navController: NavHostController, onBottomBarVisibilityChanged: (Boolean) -> Unit) {
    NavHost(navController, startDestination = "home") {

        composable(BottomNavigationItems.Screen3.route) {
            onBottomBarVisibilityChanged(true)
            AllEventsScreen(navController)
        }
        composable(BottomNavigationItems.Screen2.route) {
            onBottomBarVisibilityChanged(true)
            CalendarScreen()
        }
        composable(BottomNavigationItems.MapScreen.route) {
            onBottomBarVisibilityChanged(true)
            MapScreen(navController)
        }
        composable(BottomNavigationItems.Screen4.route) {
            onBottomBarVisibilityChanged(true)
            FavouritesScreen(navController)
        }
        composable(BottomNavigationItems.Screen5.route) {
            onBottomBarVisibilityChanged(true)
            Screen5()
        }
        composable(
            route = "eventDetail/{documentId}",
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments!!.getString("documentId")
            onBottomBarVisibilityChanged(false)
            if (documentId != null) {
                EventDetailScreen(navController= navController,documentId = documentId)
            }
        }

        composable("home") {
            onBottomBarVisibilityChanged(false)
            HomeScreen(navController = navController)

        }
        composable("register") {
            onBottomBarVisibilityChanged(false)
            RegisterScreen(navController = navController)

        }


    }
}