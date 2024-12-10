package com.example.deces

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun FavouritesScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = Firebase.firestore

    var favoriteLocations by remember { mutableStateOf<List<Location>>(emptyList()) }

    LaunchedEffect(userId) {
        userId?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                val favorites = document.get("favourites") as? List<String> ?: emptyList()
                fetchFavoriteLocations(favorites) { locations ->
                    favoriteLocations = locations
                }
            }
        }
    }

    fun removeFromFavorites(locationId: String) {
        userId?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                val favorites =
                    document.get("favourites") as? MutableList<String> ?: mutableListOf()
                favorites.remove(locationId)
                db.collection("users").document(uid).update("favourites", favorites)
                    .addOnSuccessListener {
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                val updatedFavorites =
                                    document.get("favourites") as? List<String> ?: emptyList()
                                fetchFavoriteLocations(updatedFavorites) { locations ->
                                    favoriteLocations = locations
                                }
                            }
                    }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8e8072)),

        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Gornji Box s krugom i ikonom
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(90.dp))
                    .padding(top = 10.dp, bottom = 50.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF989185))
                        .align(Alignment.Center)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_favourite),
                        contentDescription = "Favorites",
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.Center),
                        colorFilter = ColorFilter.tint(Color(0xFF74390f))
                    )
                }
            }

            // Drugi Box sa sadržajem i pozadinom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 21.dp, topEnd = 21.dp))
                    .background(Color(0xFF291b11))
                    .weight(1f) // Koristimo weight da popuni preostali prostor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Tekst na vrhu
                    Text(
                        color = Color.White,
                        text = "Favorites",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp, bottom = 8.dp)
                    )

                    if (favoriteLocations.isEmpty()) {
                        // Prikaz ako nema lokacija
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No favorite events found!",
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    } else {
                        // LazyColumn za kartice
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(favoriteLocations) { location ->
                                LocationItem(location = location,
                                    navController = navController,
                                    removeFromFavorites = { removeFromFavorites(location.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

fun fetchFavoriteLocations(favoriteIds: List<String>, callback: (List<Location>) -> Unit) {
    val db = Firebase.firestore
    val locations = mutableListOf<Location>()

    if (favoriteIds.isEmpty()) {
        callback(emptyList())
        return
    }

    favoriteIds.forEach { id ->
        db.collection("events").document(id).get().addOnSuccessListener { document ->
            document?.let {
                val date = it.getTimestamp("startdate")?.toDate() ?: Date()
                val dateFormat = SimpleDateFormat("dd.MM.yyyy.") // Format: 08.12.2024.
                val formattedDate = dateFormat.format(date)

                val location = Location(
                    id = it.id,
                    name = it.getString("name") ?: "",
                    description = it.getString("description") ?: "",
                    photo1 = it.getString("photo1") ?: "",
                    startdate = formattedDate,
                    rating = it.getDouble("rating") ?: 0.0
                )

                locations.add(location)
                if (locations.size == favoriteIds.size) {
                    callback(locations)
                }
            }
        }
    }
}

@Composable
fun LocationItem(
    location: Location, navController: NavController, removeFromFavorites: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable {
                navController.navigate("eventDetail/${location.id}")
            },
        elevation = CardDefaults.cardElevation(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF5F4436))
                .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(location.photo1),
                contentDescription = location.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .padding(0.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = location.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                    color = Color.White,
                    maxLines = 2
                )
                Text(
                    text = location.startdate,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = Color.White,
                    maxLines = 2
                )
            }
            Image(painter = painterResource(id = R.drawable.ic_favourite),
                colorFilter = ColorFilter.tint(Color(0xFFf58845)),
                contentDescription = "Favorite",
                modifier = Modifier
                    .absolutePadding(10.dp, 0.dp, 10.dp, 0.dp)
                    .size(40.dp)
                    .noRippleClickable {
                        removeFromFavorites()
                    })
        }
    }
}
