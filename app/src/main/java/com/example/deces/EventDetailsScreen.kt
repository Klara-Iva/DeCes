package com.example.deces

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Label
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await


@Composable
fun Chip(
    // 1
    label: String,
    // 2
    isSelected: Boolean,
    // 3

    // 4
    modifier: Modifier = Modifier
) {
    // 5
    val backgroundColor = Color(0xFFe99e72)

    // 6
    val textColor = Color.Black

    // 7
    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(percent = 100))
            .background(backgroundColor)
            .height(40.dp)
    ) {
        // 8
        Text(
            fontSize = 16.sp,
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )
    }
}


@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Composable
fun EventDetailScreen(documentId: String, navController: NavController) {


    var location by remember { mutableStateOf<Map<String, Any>?>(null) }
    var images by remember { mutableStateOf(listOf<String>()) }
    var isFavorite by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    var animationScale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = animationScale, animationSpec = tween(durationMillis = 100)
    )


    LaunchedEffect(documentId) {
        db.collection("events").document(documentId).get().addOnSuccessListener { document ->
                location = document.data
                images = (1..5).mapNotNull { document.getString("photo$it") }
            }

        userId?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { document ->
                    val favorites = document.get("favourites") as? List<String> ?: emptyList()
                    isFavorite = favorites.contains(documentId)
                }
        }
    }

    fun toggleFavorite() {
        userId?.let { uid ->
            val userRef = db.collection("users").document(uid)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val favorites =
                    snapshot.get("favourites") as? MutableList<String> ?: mutableListOf()
                if (isFavorite) {
                    favorites.remove(documentId)
                } else {
                    favorites.add(documentId)
                }
                transaction.update(userRef, "favourites", favorites)
            }.addOnSuccessListener {
                isFavorite = !isFavorite
                // Trigger animation
                animationScale = 1.2f
            }
        }
    }

    LaunchedEffect(animationScale) {
        if (animationScale > 1f) {
            delay(100)
            animationScale = 1f
        }
    }

    location?.let { loc ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF291b11)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                if (images.isNotEmpty()) {
                    val pagerState =
                        rememberPagerState(initialPage = 0, pageCount = { images.size })

                    HorizontalPager(state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        pageContent = { page ->
                            Image(
                                painter = rememberAsyncImagePainter(images[page]),
                                contentDescription = "Event Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        })

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(images.size) { index ->
                            val color =
                                if (pagerState.currentPage == index) Color.LightGray else Color.Black
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .padding(4.dp)
                            )
                            if (index < images.size - 1) {
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                // Title and Favorite Icon in the same row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = loc["name"] as String,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp),
                        color = Color.White
                    )

                    Image(painter = painterResource(id = if (isFavorite) R.drawable.ic_favourite else R.drawable.ic_favourite_border),
                        contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                        colorFilter = ColorFilter.tint(Color(0xFFf58845)),
                        modifier = Modifier
                            .size(55.dp)
                            .scale(animatedScale)
                            .noRippleClickable { toggleFavorite() })
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.Start,// NE ZELI BIT ULIJEVO
                ) {
                    Chip(
                        loc["category"] as String, true, modifier = Modifier
                    )
                }
            }

            item {
                Text(
                    text = loc["description"] as String,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(25.dp),
                    textAlign = TextAlign.Justify,
                    color = Color(0xFFa1a1a1),
                    lineHeight = 20.sp
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(

                        onClick = {
                            var latitude: Double = 0.0
                            var longitude: Double = 0.0
                            val docRef = db.collection("events").document(documentId)
                            docRef.get().addOnSuccessListener { document ->
                                    latitude = document.data!!["latitude"].toString().toDouble()
                                    longitude = document.data!!["longitude"].toString().toDouble()
                                }.addOnCompleteListener {
                                    CameraBounds.showSpecifiedLocationOnMap = true
                                    val cameraPosition = CameraPosition.fromLatLngZoom(
                                        LatLng(latitude, longitude), 19f
                                    )
                                    CameraBounds.camerapostion = cameraPosition
                                    CameraBounds.showSpecifiedLocationOnMap = true
                                    CameraBounds.setCoordinates(latitude, longitude)
                                    navController.navigate("MapScreen") {
                                        popUpTo("MapScreen") {
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                        },

                        modifier = Modifier.padding(end = 8.dp), shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Find on map"
                        )
                    }
                    Spacer(modifier = Modifier.width(100.dp))

                }
            }
        }
    }
}





