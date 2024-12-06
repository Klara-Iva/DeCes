package com.example.deces.bottomnavigationbar

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.deces.AllEventsScreen
import com.example.deces.EventDetailScreen
import com.example.deces.MapScreen
import com.example.deces.Screen2
import com.example.deces.Screen3
import com.example.deces.Screen4
import com.example.deces.Screen5

@Composable
fun NavigationGraph(navController: NavHostController, onBottomBarVisibilityChanged: (Boolean) -> Unit) {
    NavHost(navController, startDestination = BottomNavigationItems.Screen3.route) {
        composable(BottomNavigationItems.Screen3.route) {
            onBottomBarVisibilityChanged(true)
            AllEventsScreen(navController)
        }
        composable(BottomNavigationItems.Screen2.route) {
            onBottomBarVisibilityChanged(true)
            Screen2()
        }
        composable(BottomNavigationItems.MapScreen.route) {
            onBottomBarVisibilityChanged(false)
            MapScreen(navController)
        }
        composable(BottomNavigationItems.Screen4.route) {
            onBottomBarVisibilityChanged(true)
            Screen4()
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
    }
}