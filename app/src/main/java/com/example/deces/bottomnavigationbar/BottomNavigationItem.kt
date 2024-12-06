package com.example.deces.bottomnavigationbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavigationItems(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null
) {
    object MapScreen : BottomNavigationItems(
        route = "MapScreen",
        title = "Map",
        icon = Icons.Outlined.LocationOn
    )
    object Screen2 : BottomNavigationItems(
        route = "screen2",
        title = "Calendar",
        icon = Icons.Outlined.DateRange
    )
    object Screen3 : BottomNavigationItems(
        route = "screen3",
        title = "Home",
        icon = Icons.Outlined.Home
    )
    object Screen4 : BottomNavigationItems(
        route = "screen4",
        title = "Favourites",
        icon = Icons.Outlined.Favorite
    )
    object Screen5 : BottomNavigationItems(
        route = "screen5",
        title = "Profile",
        icon = Icons.Outlined.AccountCircle
    )
}