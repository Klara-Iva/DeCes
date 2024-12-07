package com.example.deces

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date


@Composable
fun AllEventsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var locations by remember { mutableStateOf(listOf<Location>()) }
    var allLocations by remember { mutableStateOf(listOf<Location>()) }
    var favoriteLocations by remember { mutableStateOf(listOf<String>()) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid


    LaunchedEffect(Unit) {
        fetchLocationsFromFirestore { result ->
            allLocations = result
            locations = result
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf8f7f7))
            .absolutePadding(5.dp, 5.dp, 5.dp, 0.dp)
    ) {
        TextField(
            maxLines = 1, value = searchQuery, onValueChange = {
                searchQuery = it
                locations = if (searchQuery.isEmpty()) {
                    allLocations
                } else {
                    allLocations.filter { location ->
                        location.name.contains(searchQuery, ignoreCase = true)
                    }
                }
            },

            label = { Text("Search Events") }, modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()
        ) {
            items(locations) { location ->
                val isFavorite = favoriteLocations.contains(location.id)
                LocationCard(location, navController, isFavorite, userId) { updatedFavorites ->
                    favoriteLocations = updatedFavorites
                }
            }
        }
    }
}

@Composable
fun LocationCard(
    location: Location,
    navController: NavController,
    isFavorite: Boolean,
    userId: String?,
    onFavoriteUpdated: (List<String>) -> Unit
) {
    var animationScale by remember { mutableFloatStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = animationScale, animationSpec = tween(durationMillis = 100)
    )
    val db = Firebase.firestore

    var localIsFavorite by remember { mutableStateOf(isFavorite) }

    fun toggleFavorite() {
        userId?.let { uid ->
            val userRef = db.collection("users").document(uid)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val favorites =
                    snapshot.get("favourites") as? MutableList<String> ?: mutableListOf()
                if (localIsFavorite) {
                    favorites.remove(location.id)
                } else {
                    favorites.add(location.id)
                }
                transaction.update(userRef, "favourites", favorites)
            }.addOnSuccessListener {
                localIsFavorite = !localIsFavorite
                animationScale = 1.2f

            }
        }
    }
    LaunchedEffect(animationScale) {
        if (animationScale > 1f) {
            kotlinx.coroutines.delay(100)
            animationScale = 1f
        }
    }


    Card(

        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        modifier = Modifier
            .fillMaxWidth()

            .padding(8.dp)
            .height(250.dp)
            .clickable {
                navController.navigate("eventDetail/${location.id}")
            }) {
        Column(modifier = Modifier.background(Color(0xFFFFFFFF))) {


            Box {
                Image(
                    painter = rememberAsyncImagePainter(location.photo1),
                    contentScale = ContentScale.Crop,
                    contentDescription = location.name,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                )

            }
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        location.name, maxLines = 2,

                        fontSize = 16.sp, textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    location.description,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFFFa1a1a1),
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(3.dp)


                )
            }
        }
    }
}

fun fetchLocationsFromFirestore(onResult: (List<Location>) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("events").get().addOnSuccessListener { documents ->
            val locations = documents.mapNotNull { doc ->
                val name = doc.getString("name")
                val imageUrl = doc.getString("photo1")
                val description = doc.getString("description")
                val startdate = doc.getTimestamp("startdate")?.toDate() ?: Date()
                val dateFormat = SimpleDateFormat("dd.MM.yyyy.") // Format: 08.12.2024.
                val formattedDate = dateFormat.format(startdate)
                if (name != null && imageUrl != null && description != null) {
                    Location(doc.id, name, description, imageUrl, formattedDate)
                } else {
                    null
                }
            }
            onResult(locations)
        }.addOnFailureListener {
            onResult(emptyList())
        }
}





