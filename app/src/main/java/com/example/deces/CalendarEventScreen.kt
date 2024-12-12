package com.example.deces

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

@Composable
fun CalendarEventScreen(navController: NavController, date: Date) {

    //related to map screen, must be initialized here
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    CameraBounds.getCoordinatesFromBase(currentUser!!.uid)

    var locations by remember { mutableStateOf(listOf<Location>()) }
    var allLocations by remember { mutableStateOf(listOf<Location>()) }

    LaunchedEffect(Unit) {
        fetchDatedEventsFromFirestore({ result ->
            allLocations = result
            locations = result
        }, date)
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

        Text(
            text = "Datum pretraživanja ${date.toString()}",
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
                DateCard(location, location.startdate, navController)
            }
        }
    }
}

@Composable
fun DateCard(
    location: Location,
    date : String,
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

fun fetchDatedEventsFromFirestore(onResult: (List<Location>) -> Unit, date: Date) {
    // First, fetch all locations based on the selected city
    fetchLocationsFromFirestore { locations ->
        // Date format used to parse start and end dates from the string
        val dateFormat = SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault())

        // Print out the selected date for debugging
        Log.d("EventFiltering", "Selected Date: ${dateFormat.format(date)}")

        // Filter events that are active on the selected date
        val filteredLocations = locations.filter { location ->
            // Convert startdate string to Date
            val startDate = location.startdate.let { dateFormat.parse(it) }
            val endDate = location.enddate?.let { dateFormat.parse(it) }

            // Log the parsed start and end dates
            Log.d("EventFiltering", "Event: ${location.name}")
            Log.d("EventFiltering", "Start Date: ${location.startdate} -> ${startDate}")
            Log.d("EventFiltering", "End Date: ${location.enddate} -> ${endDate}")

            // Check if the event is active on the selected date
            val isActiveOnSelectedDate = startDate != null &&
                    startDate <= date &&
                    (endDate == null || endDate >= date)

            // Log the result of the filtering condition for each event
            Log.d("EventFiltering", "Is Active on Selected Date: $isActiveOnSelectedDate")

            isActiveOnSelectedDate
        }

        // Return filtered locations (events that are active on the selected date)
        onResult(filteredLocations)
    }
}
