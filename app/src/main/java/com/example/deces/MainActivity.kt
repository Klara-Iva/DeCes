package com.example.deces

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.deces.ui.theme.DecesTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.deces.bottomnavigationbar.BottomBar
import com.example.deces.bottomnavigationbar.BottomNavigationItems
import com.example.deces.bottomnavigationbar.NavigationGraph


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DecesTheme {
                val navController: NavHostController =
                    rememberNavController() // Kreiraj navController

                // Provjerava koji je trenutni ekran
                val currentRoute by navController.currentBackStackEntryAsState()

                // Varijabla za upravljanje vidljivošću BottomBar-a
                var isBottomBarVisible by remember { mutableStateOf(true) }

                // Postavljanje funkcije za promjenu vidljivosti BottomBar-a
                val onBottomBarVisibilityChanged: (Boolean) -> Unit = { isVisible ->
                    isBottomBarVisible = isVisible
                }

                // Postavlja uvjet za prikazivanje BottomBar-a
                isBottomBarVisible = when (currentRoute?.destination?.route) {
                    BottomNavigationItems.MapScreen.route -> false // Sakrij BottomBar na MapScreen
                    else -> true // Prikazuj BottomBar na ostalim ekranima
                }

                Scaffold(bottomBar = {
                    // Ako je BottomBar vidljiv, prikazat ćemo ga
                    if (isBottomBarVisible) {
                        BottomBar(navController = navController, state = true)
                    }
                }) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        // Prosljeđivanje funkcije za upravljanje vidljivošću BottomBar-a
                        NavigationGraph(
                            navController = navController,
                            onBottomBarVisibilityChanged = onBottomBarVisibilityChanged
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun Screen3() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Screen 333"
        )
    }
}




