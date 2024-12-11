package com.example.deces.bottomnavigationbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.sharp.Favorite
import androidx.compose.material.icons.sharp.FavoriteBorder
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavigationItems(
    val route: String, val title: String? = null, val icon: ImageVector? = null
) {
    object MapScreen : BottomNavigationItems(
        route = "maproute", title = "Map", icon = Icons.Outlined.LocationOn
    )

    object CalendarScreen : BottomNavigationItems(
        route = "calendarroute", title = "Calendar", icon = Icons.Outlined.DateRange
    )

    object EventsHomeScreen : BottomNavigationItems(
        route = "eventshomeroute", title = "Home", icon = Icons.Outlined.Home
    )

    object FavouritesScreen : BottomNavigationItems(
        route = "favouritesroute", title = "Favourites", icon = Icons.Sharp.FavoriteBorder
    )

    object ProfileScreen : BottomNavigationItems(
        route = "profileroute", title = "Profile", icon = Icons.Outlined.AccountCircle
    )
}