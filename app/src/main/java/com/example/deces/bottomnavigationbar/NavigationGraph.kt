package com.example.deces.bottomnavigationbar

import WaitScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.deces.AddNewEventScreen
import com.example.deces.AllEventsScreen
import com.example.deces.CalendarEventScreen
import com.example.deces.CalendarScreen
import com.example.deces.ChangeInterestsScreen
import com.example.deces.ChooseCityScreen
import com.example.deces.ChooseInterestsScreen
import com.example.deces.EventDetailScreen
import com.example.deces.FavouritesScreen
import com.example.deces.HomeScreen
import com.example.deces.LoginScreen
import com.example.deces.MapScreen
import com.example.deces.RegisterScreen
import com.example.deces.Screen5
import java.util.Date

@Composable
fun NavigationGraph(navController: NavHostController, onBottomBarVisibilityChanged: (Boolean) -> Unit) {
    NavHost(navController, startDestination = "waitscreenroute") {

        composable("eventshomeroute") {
            onBottomBarVisibilityChanged(true)
            AllEventsScreen(navController)
        }
        composable("calendarroute") {
            onBottomBarVisibilityChanged(true)
            CalendarScreen(navController)
        }
        composable("maproute") {
            onBottomBarVisibilityChanged(true)
            MapScreen(navController)
        }
        composable("favouritesroute") {
            onBottomBarVisibilityChanged(true)
            FavouritesScreen(navController)
        }
        composable("profileroute") {
            onBottomBarVisibilityChanged(true)
            Screen5(navController)
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
        composable("login") {
            onBottomBarVisibilityChanged(false)
            LoginScreen(navController = navController)
        }
        composable(
            route = "chooseCity?fromProfile={fromProfile}",
            arguments = listOf(navArgument("fromProfile") { defaultValue = "false" })
        ) { backStackEntry ->
            val fromProfile = backStackEntry.arguments?.getString("fromProfile") == "true"
            ChooseCityScreen(navController, fromProfile)
        }

        composable("chooseInterests") {
            onBottomBarVisibilityChanged(false)
            ChooseInterestsScreen(navController = navController)

        }
        composable("changeinterests") {
            onBottomBarVisibilityChanged(false)
            ChangeInterestsScreen(navController = navController)
        }
        composable("waitscreenroute") {
            onBottomBarVisibilityChanged(false)
            WaitScreen(navController = navController)
        }
        composable(
            route = "calendarEvent/{date}",
            arguments = listOf(navArgument("date") { type = NavType.LongType })
        ) { backStackEntry ->
            val dateMillis = backStackEntry.arguments?.getLong("date")
            if (dateMillis != null) {
                val date = Date(dateMillis)
                CalendarEventScreen(navController = navController, date = date)
            }
        composable("AddNewEventScreen") {
            onBottomBarVisibilityChanged(false)
            AddNewEventScreen(navController = navController)
        }
    }
}