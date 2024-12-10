package com.example.deces

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess


@Composable
fun AllEventsScreen(navController: NavController) {

    BackHandler {
        exitProcess(0)
    }

    //related to map screen, must be initialized here
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    CameraBounds.getCoordinatesFromBase(currentUser!!.uid)
    //

    var searchQuery by remember { mutableStateOf("") }
    var locations by remember { mutableStateOf(listOf<Location>()) }
    var allLocations by remember { mutableStateOf(listOf<Location>()) }

    LaunchedEffect(Unit) {
        fetchLocationsFromFirestore { result ->
            allLocations = result
            locations = result
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF291b11))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "",
                tint = Color(0xFFf58845),
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = CameraBounds.selectedCityName,
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            TextField(
                maxLines = 1, value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    locations = if (searchQuery.isEmpty()) {
                        allLocations
                    } else {
                        allLocations.filter { location ->
                            location.name.contains(searchQuery, ignoreCase = true)
                        }
                    }
                },

                label = { Text("Pretraži događaje", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF8a6d57), shape = RoundedCornerShape(50)),

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.White
                    )
                },
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Ovo bi vas moglo zanimati: ",
            fontSize = 20.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(start = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            items(locations) { location ->
                LocationCard(location, navController)
            }
        }
    }
}

@Composable
fun LocationCard(
    location: Location,
    navController: NavController,
) {
    var animationScale by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(animationScale) {
        if (animationScale > 1f) {
            delay(100)
            animationScale = 1f
        }
    }

    Card(
        colors = CardColors(
            containerColor = Color(0xFF402c1c),
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth() // Zauzmi punu širinu
            .height(120.dp) // Postavi fiksnu visinu za testiranje
            .clickable {
                navController.navigate("eventDetail/${location.id}")
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight() // Osigurava da Row rasteže cijelu visinu
        ) {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(location.photo1),
                    contentScale = ContentScale.Crop,
                    contentDescription = location.name,
                    modifier = Modifier
                        .height(120.dp)
                        .width(120.dp)
                )
            }
            Column(
                modifier = Modifier
                    .padding(top = 16.dp, start = 8.dp, bottom = 8.dp)
                    .fillMaxHeight() // Omogućuje raspodjelu prostora unutar stupca
            ) {
                Text(
                    location.name,
                    maxLines = 2,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    location.startdate,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start
                )
                Spacer(
                    modifier = Modifier.weight(1f) // Guranje ratinga prema dnu
                )
                Text(
                    "(${location.rating}) ☆",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    color = Color(0xFFf58845)
                )
            }
        }
    }

}


//TODO filtriranje po kategorijama
fun fetchLocationsFromFirestore(onResult: (List<Location>) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("events")
        .whereEqualTo("city", CameraBounds.selectedCityName)
        .get()
        .addOnSuccessListener { documents ->
            val locations = documents.mapNotNull { doc ->
                val name = doc.getString("name")
                val imageUrl = doc.getString("photo1")
                val description = doc.getString("description")
                val startdate = doc.getTimestamp("startdate")?.toDate() ?: Date()
                val dateFormat = SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault())
                val formattedDate = dateFormat.format(startdate)
                val rating = doc.getDouble("rating") ?: 0.0
                if (name != null && imageUrl != null && description != null) {
                    Location(doc.id, name, description, imageUrl, formattedDate, rating)
                } else {
                    null
                }
            }
            onResult(locations)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}





