package com.example.deces

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
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
import coil.compose.rememberImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


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

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // HorizontalPager with images
                        HorizontalPager(state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .zIndex(0f),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp)
                        ) {
                            Text(
                                text = loc["name"] as String,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                                modifier = Modifier
                                    .weight(2f)
                                    .wrapContentHeight()
                                    .padding(end = 8.dp),
                                color = Color.White,
                                lineHeight = 48.sp
                            )
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
                                    .clip(RoundedCornerShape(90.dp))
                                    .background(Color(0xFFf58845))
                            ) {
                                Text(
                                    fontStyle = FontStyle.Italic,
                                    text = loc["category"] as String,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 40.dp, vertical = 4.dp
                                    ),
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        RatingDisplay(documentId)

                        Text(
                            text = loc["description"] as String,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(25.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Justify,
                            color = Color(0xFFa1a1a1),
                            lineHeight = 20.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = Color(0xFFf58845),
                                    disabledContentColor = Color.Gray,
                                    containerColor = Color(0xFFf58845),
                                    disabledContainerColor = Color.Gray
                                ), onClick = {
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
                                    text = "Find on map", color = Color.Black
                                )
                            }
                        }

                        val reviews = remember { mutableStateListOf<Map<String, String>>() }


                        LaunchedEffect(documentId) {
                            FirebaseFirestore.getInstance().collection("events")
                                .document(documentId).collection("reviews").get()
                                .addOnSuccessListener { snapshot ->
                                    reviews.clear()
                                    snapshot.documents.mapNotNullTo(reviews) { doc ->
                                        mapOf(
                                            "user" to (doc.getString("user")
                                                ?: "Nepoznati korisnik"),
                                            "title" to (doc.getString("title") ?: "Bez naslova"),
                                            "description" to (doc.getString("description") ?: ""),
                                            "date" to (doc.getString("date") ?: ""),

                                        )

                                    }
                                }
                        }

                        if (reviews.isNotEmpty()) {
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
                                    var profilePictureUrl by remember { mutableStateOf("") }

                                    LaunchedEffect(userId) {
                                        userId?.let { id ->
                                            db.collection("users").document(id).get()
                                                .addOnSuccessListener { userDoc ->
                                                    userName = userDoc.getString("name") ?: "Nepoznati korisnik"
                                                    profilePictureUrl = userDoc.getString("profilePicture") ?: ""
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
                                        Row(){
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Gray)
                                            ) {
                                                if (profilePictureUrl.isNotEmpty()) {
                                                    Image(
                                                        painter = rememberImagePainter(
                                                            profilePictureUrl
                                                        ),
                                                        contentDescription = "Profile Picture",
                                                        modifier = Modifier.size(40.dp)
                                                        .clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "Profile Icon",
                                                        modifier = Modifier.size(24.dp),
                                                        tint = Color.White
                                                    )
                                                }
                                            }

                                            Column(
                                            modifier = Modifier.padding(start = 10.dp)
                                        )
                                        {
                                            Text(
                                                text = userName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White,

                                            )
                                            Text(
                                                text = review["date"] ?: "",
                                                fontSize = 14.sp,
                                                color = Color.White,

                                            )
                                            Text(
                                                text = review["title"] ?: "",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 19.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(vertical = 10.dp)
                                            )

                                            Text(
                                                text = review["description"] ?: "",
                                                fontSize = 12.sp,
                                                color = Color(0xFFa1a1a1),
                                                textAlign = TextAlign.Justify

                                            )
                                        }
                                     }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(top = 40.dp)
                                .background(Color(0xFF322013), shape = RoundedCornerShape(21.dp))
                                .padding(20.dp)
                        ) {
                            var reviewTitle by remember { mutableStateOf("") }
                            var reviewDescription by remember { mutableStateOf("") }
                            val isFormValid =
                                reviewTitle.isNotBlank() && reviewDescription.isNotBlank()
                            val context = LocalContext.current

                            userId?.let {
                                RatingSection(
                                    userId = it,
                                    documentId = documentId,
                                    navController = navController
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Napisi recenziju",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            TextField(
                                value = reviewTitle,
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

                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

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
                                Button(
                                    onClick = {
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                                        val dateFormat =
                                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                        val todayDate = dateFormat.format(Date()).toString()
                                        if (userId != null) {
                                            val reviewData = mapOf(
                                                "user" to userId,
                                                "title" to reviewTitle,
                                                "description" to reviewDescription,
                                                "date" to todayDate
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
                                    colors = ButtonDefaults.buttonColors(
                                        contentColor = Color(0xFFf58845),
                                        disabledContentColor = Color.Gray,
                                        containerColor = Color(0xFFf58845),
                                        disabledContainerColor = Color.Gray
                                    ),

                                    enabled = isFormValid,
                                ) {
                                    Text(text = "Spremi recenziju", color = Color.White)
                                }
                            }


                        }
                        Spacer(modifier = Modifier.height(60.dp))
                    }

                }


            }

        }
    }
}


@Composable
fun RatingSection(
    userId: String?, documentId: String, navController: NavController
) {
    var rating by remember { mutableStateOf(0f) }
    var isRated by remember { mutableStateOf(false) } // Provjera je li ocijenjeno
    val context = LocalContext.current

    // Dohvaćanje postojeće ocjene
    LaunchedEffect(key1 = userId, key2 = documentId) {
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRatingRef = db.collection("users").document(userId).collection("ratedEvents")
                .document(documentId)

            userRatingRef.get().addOnSuccessListener { document ->
                val existingRating = document.getDouble("rating") ?: 0.0
                rating = existingRating.toFloat()
                isRated = existingRating > 0.0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(Color(0xFF322013), shape = RoundedCornerShape(21.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Ocijeni događaj",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        var gestureRating by remember { mutableStateOf(0f) }
        var isDragging by remember { mutableStateOf(false) }

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .pointerInput(Unit) {
                if (!isRated) {
                    detectDragGestures(onDragStart = { isDragging = true }, onDragEnd = {
                        isDragging = false
                        rating = roundToNearestHalf(gestureRating)
                    }, onDragCancel = { isDragging = false }, onDrag = { change, _ ->
                        val positionX = change.position.x.coerceIn(0f, size.width.toFloat())
                        val maxStars = 5f
                        val calculatedRating = (positionX / size.width) * maxStars
                        gestureRating = calculatedRating.coerceIn(0f, maxStars)
                    })
                }
            }) {
            val displayRating = if (isDragging) roundToNearestHalf(gestureRating) else rating
            Row(
                modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..5) {
                    val fillRatio = when {
                        displayRating >= i -> 1f
                        displayRating > i - 1 -> displayRating - (i - 1)
                        else -> 0f
                    }

                    Canvas(modifier = Modifier.size(40.dp)) {
                        drawStar(fillRatio)
                    }
                }
            }
        }

        Text(
            text = "Trenutni odabir: ${String.format("%.1f", rating)}",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))
        if (!isRated) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color(0xFFf58845),
                    disabledContentColor = Color.Gray,
                    containerColor = Color(0xFFf58845),
                    disabledContainerColor = Color.Gray
                ),
                onClick = {
                    if (userId != null) {
                        val db = FirebaseFirestore.getInstance()

                        val userRatingRef =
                            db.collection("users").document(userId).collection("ratedEvents")
                                .document(documentId)
                        val userRatingData = mapOf("rating" to rating)

                        userRatingRef.set(userRatingData).addOnSuccessListener {
                            val eventRef = db.collection("events").document(documentId)
                            eventRef.get().addOnSuccessListener { document ->
                                val ratingSum = (document.getDouble("ratingSum") ?: 0.0) + rating
                                val ratingCount = (document.getLong("ratingCount") ?: 0L) + 1

                                val updatedData = mapOf(
                                    "ratingSum" to ratingSum,
                                    "ratingCount" to ratingCount,
                                    "rating" to if (ratingCount > 0) ratingSum / ratingCount else 0.0
                                )

                                eventRef.update(updatedData).addOnSuccessListener {
                                    Toast.makeText(
                                        context, "Ocjena uspješno spremljena!", Toast.LENGTH_SHORT
                                    ).show()
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        context, "Greška pri spremanju ocjene!", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Greška pri spremanju korisničke ocjene!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                shape = RoundedCornerShape(50),
                enabled = rating > 0f,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Spremi ocjenu", color = Color.White)
            }
        } else {
            Text(
                text = "Već ste ocijenili ovaj događaj.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

fun roundToNearestHalf(value: Float): Float {
    return (value * 2).roundToInt() / 2f
}

fun DrawScope.drawStar(fillRatio: Float) {
    drawPath(
        path = createStarPath(), color = Color(0xFF754324), style = Fill
    )
    drawPath(
        path = createStarPath(), color = Color(0xFFf58845), style = Fill, alpha = fillRatio
    )
}


fun createStarPath(): Path {
    val path = Path()
    val centerX = 50f
    val centerY = 50f
    val outerRadius = 40f
    val innerRadius = 20f
    val points = 10

    val angleStep = (2 * Math.PI / points).toFloat()
    val startAngle = -Math.PI / 2

    for (i in 0 until points) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = startAngle + i * angleStep
        val x = (centerX + radius * Math.cos(angle)).toFloat()
        val y = (centerY + radius * Math.sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}


@Composable
fun RatingDisplay(eventId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var rating by remember { mutableStateOf(0f) }

    LaunchedEffect(eventId) {
        db.collection("events").document(eventId).get().addOnSuccessListener { document ->
            rating = (document.getDouble("rating") ?: 0.0).toFloat()
        }.addOnFailureListener {
            Toast.makeText(context, "Greška pri dohvaćanju ocjene", Toast.LENGTH_SHORT).show()
        }
    }

    val fullStars = rating.toInt()
    val halfStar = if (rating % 1 >= 0.5) 1 else 0
    val emptyStars = 5 - (fullStars + halfStar)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp)
            .padding(top = 8.dp)
    ) {

        repeat(fullStars) {
            Canvas(modifier = Modifier.size(15.dp)) {
                drawStar2(1f)
            }
        }

        if (halfStar == 1) {
            Canvas(modifier = Modifier.size(15.dp)) {
                drawStar2(0.5f)
            }
        }

        repeat(emptyStars) {
            Canvas(modifier = Modifier.size(15.dp)) {
                drawStar2(0f)
            }
        }

        Text(
            text = "(${String.format("%.1f", rating)})",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 4.dp),
            fontSize = 10.sp
        )
    }
}

fun DrawScope.drawStar2(fillRatio: Float) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val outerRadius = size.width / 2
    val innerRadius = size.width / 4
    val points = 10
    val angleStep = (2 * Math.PI / points).toFloat()
    val startAngle = -Math.PI / 2

    val path = Path()
    for (i in 0 until points) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = startAngle + i * angleStep
        val x = (centerX + radius * Math.cos(angle)).toFloat()
        val y = (centerY + radius * Math.sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    drawPath(path = path, color = Color(0xFF754324))
    drawPath(path = path, color = Color(0xFFf58845), alpha = fillRatio)
}
