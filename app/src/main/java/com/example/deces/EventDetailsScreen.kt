package com.example.deces

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay


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
                .padding(0.dp)
                .background(Color(0xFF291b11)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                if (images.isNotEmpty()) {
                    val pagerState =
                        rememberPagerState(initialPage = 0, pageCount = { images.size })

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // HorizontalPager with images
                        HorizontalPager(state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .zIndex(0f), // Set zIndex lower to place images behind the Row
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

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 30.dp)
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
                    }
                }
            }


            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .offset(y = (-20).dp)
                        .clip(RoundedCornerShape(topStart = 21.dp, topEnd = 21.dp))
                        .background(Color(0xFF291b11))


                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 20.dp),

                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {


                        // Title and Favorite Icon in the same row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start, // Rasporedite elemente s lijeva na desno
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp)
                        ) {
                            // Title with wrapContentWidth to let it take only as much space as needed
                            Text(
                                text = loc["name"] as String,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                                modifier = Modifier
                                    .weight(2f)
                                    .wrapContentHeight()  // Tekst zauzima samo onoliko prostora koliko mu treba
                                    .padding(end = 8.dp),
                                color = Color.White,
                                lineHeight = 48.sp
                            )

                            // Favorite Icon, always visible
                            Image(painter = painterResource(id = if (isFavorite) R.drawable.ic_favourite else R.drawable.ic_favourite_border),
                                contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                                colorFilter = ColorFilter.tint(Color(0xFFf58845)),
                                modifier = Modifier
                                    .size(55.dp)
                                    .scale(animatedScale)
                                    .noRippleClickable { toggleFavorite() })
                        }

                        Row(
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp)
                                .padding(top = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(21.dp))
                                    .background(Color(0xFFf58845))
                            ) {
                                Text(
                                    text = loc["category"] as String,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 30.dp, vertical = 8.dp
                                    ),
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Text(
                            text = loc["description"] as String,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(25.dp),
                            textAlign = TextAlign.Justify,
                            color = Color(0xFFa1a1a1),
                            lineHeight = 20.sp
                        )

                        Row(
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
                                        longitude =
                                            document.data!!["longitude"].toString().toDouble()
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

                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Find on map"
                                )
                            }


                        }

                        //reviews

                        Column(

                            modifier = Modifier
                                .fillMaxWidth()

                                .padding(horizontal = 20.dp)
                                .padding(top=40.dp)
                                .background(Color(0xFF322013), shape = RoundedCornerShape(21.dp))
                                .padding(20.dp)
                        ) {
                            // Title input for review
                            var reviewTitle by remember { mutableStateOf("") }
                            var reviewDescription by remember { mutableStateOf("") }
                            val isFormValid =
                                reviewTitle.isNotBlank() && reviewDescription.isNotBlank()
                            val context = LocalContext.current
                            Text(
                                text = "Napisi recenziju",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            TextField(value = reviewTitle,
                                onValueChange = { reviewTitle = it },
                                label = { Text("Naslov") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFf58845),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    cursorColor = Color.White
                                ),

                                shape = RoundedCornerShape(8.dp) // Zaobljeni rubovi
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Input for review description (larger text area)
                            TextField(
                                value = reviewDescription,
                                onValueChange = { reviewDescription = it },
                                label = { Text("Opis") },

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFf58845),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    cursorColor = Color.White
                                ),
                                maxLines = 10
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Button to save the review
                                Button(
                                    onClick = {
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                                        if (userId != null) {
                                            val reviewData = mapOf(
                                                "user" to userId,
                                                "title" to reviewTitle,
                                                "description" to reviewDescription
                                            )
                                            val reviewsRef =
                                                FirebaseFirestore.getInstance().collection("events")
                                                    .document(documentId).collection("reviews")
                                            val newReviewRef =
                                                reviewsRef.document() // Generate random UID for review
                                            newReviewRef.set(reviewData).addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "Recenzija uspješno spremljena!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                reviewTitle = ""
                                                reviewDescription = ""
                                            }.addOnFailureListener {
                                                Toast.makeText(
                                                    context,
                                                    "Greška pri spremanju recenzije",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(50),
                                    enabled = isFormValid,
                                ) {
                                    Text(text = "Spremi recenziju", color = Color.White)
                                }
                            }
                        }
                    }
                }

                val reviews = remember { mutableStateListOf<Map<String, String>>() }

                LaunchedEffect(documentId) {
                    FirebaseFirestore.getInstance()
                        .collection("events")
                        .document(documentId)
                        .collection("reviews")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            reviews.clear()
                            snapshot.documents.mapNotNullTo(reviews) { doc ->
                                mapOf(
                                    "user" to (doc.getString("user") ?: "Nepoznati korisnik"),
                                    "title" to (doc.getString("title") ?: "Bez naslova"),
                                    "description" to (doc.getString("description") ?: "")
                                )
                            }
                        }
                }


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Recenzije",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    reviews.forEach { review ->

                        val userId = review["user"] as? String
                        var userName by remember { mutableStateOf("Nepoznati korisnik") }

                        LaunchedEffect(userId) {
                            userId?.let { id ->
                                db.collection("users").document(id).get().addOnSuccessListener { userDoc ->
                                    userName = userDoc.getString("name") ?: "Nepoznati korisnik"
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF3A2A1E))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = userName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = review["title"] ?: "Bez naslova",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = review["description"] ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFFa1a1a1),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}


