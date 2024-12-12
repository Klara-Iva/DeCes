package com.example.deces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun CalendarScreen2(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val events = remember { mutableListOf<Event>() }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        firestore.collection("events")  .whereEqualTo("city", CameraBounds.selectedCityName).get().addOnSuccessListener { documents ->
            events.clear()
            documents.forEach { document ->
                val id = document.id
                val title = document.getString("name") ?: "Unknown Event"
                val startDateTimestamp = document.getTimestamp("startDate")
                val endDateTimestamp = document.getTimestamp("endDate")

                val startDate = startDateTimestamp?.toDate() ?: Date()
                val endDate = endDateTimestamp?.toDate() ?: Date()

                val currentMonthStart = getFirstDayOfCurrentMonth()
                val currentMonthEnd = getEndOfCurrentMonth()

                if (startDate.after(currentMonthStart) && startDate.before(currentMonthEnd)) {
                    events.add(Event(id, title, startDate, endDate))
                }
            }
            isLoading.value = false
        }
    }

    if (isLoading.value) {
        LoadingIndicator()
    } else {
        val startDate = getStartOfCurrentMonth()
        val endDate = getEndOfCurrentMonth()

        Timeline(events, startDate, endDate,navController)
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text("Loading...", fontSize = 24.sp, color = Color.White)
    }
}


@Composable
fun Timeline(events: List<Event>, startDate: Date, endDate: Date,navController: NavController) {
    val totalDays = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()
    Box(
        modifier = Modifier
            .background(Color(0xFF291b11))
            .fillMaxSize()
    ) {
        Column {
            Text(
                text = "Mjesečni pregled događaja",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(24.dp)
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFF291b11))
                    .fillMaxSize()
                    .padding(2.dp)
            ) {
            LazyColumn {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        ) {

                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        val calendar = Calendar.getInstance()
                                        calendar.time = startDate
                                        val startDay = calendar.get(Calendar.DAY_OF_MONTH)

                                        val daysInMonth =
                                            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                                        for (i in startDay - 1 until daysInMonth) {
                                            val currentDayMillis =
                                                startDate.time + (i * 1000 * 60 * 60 * 24)
                                            val newCalendar = Calendar.getInstance()
                                            newCalendar.timeInMillis = currentDayMillis
                                            val dayString = (i + 1).toString()

                                            Box(
                                                modifier = Modifier
                                                    .width(40.dp)
                                                    .padding(2.dp)


                                            ) {
                                                Text(
                                                    text = dayString,
                                                    modifier = Modifier.align(Alignment.Center),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold

                                                )
                                            }
                                        }
                                    }

                                    val colorPalette = listOf(
                                        Color(0xFFB2805B), // Tamno narančasta
                                        Color(0xFFB4815C), // Tamno narančasta
                                        Color(0xFF754324), // Plava
                                        Color(0xFF6a5240), // Ljubičasta
                                        Color(0xFF5f4436)  // Zelena
                                    )

                                    for ((index, event) in events.withIndex()) {
                                        val startOffset = getEventStartOffset(event.startDate, startDate)
                                        val eventStartInCurrentMonth = maxOf(event.startDate, startDate)
                                        val eventEndInCurrentMonth = event.endDate
                                        if (eventEndInCurrentMonth.before(eventStartInCurrentMonth)) continue
                                        val eventDuration =
                                            ((eventEndInCurrentMonth.time - eventStartInCurrentMonth.time) / (1000 * 60 * 60 * 24)).toInt()

                                        // Koristi boju iz palete na temelju indeksa događaja
                                        val color = colorPalette[index % colorPalette.size]

                                        Row(modifier = Modifier.fillMaxWidth()) {

                                            Box(
                                                modifier = Modifier
                                                    .padding(vertical = 8.dp)
                                                    .padding(start = (startOffset * 40).dp)
                                                    .width(((eventDuration + 1) * 40).dp)
                                                    .height(30.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(color) // Koristi boju iz palete
                                                    .align(Alignment.CenterVertically)
                                                    .clickable {
                                                        // Navigacija na detalje događaja
                                                        navController.navigate("eventDetail/${event.id}")
                                                    }
                                            ) {
                                                Text(
                                                    text = event.title,
                                                    modifier = Modifier
                                                        .align(Alignment.Center)
                                                        .padding(horizontal = 4.dp),
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }



                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}

fun getEventStartOffset(eventStartDate: Date, startDateOfMonth: Date): Int {
    val startCalendar = Calendar.getInstance().apply {
        time = eventStartDate
    }
    val startOfMonthCalendar = Calendar.getInstance().apply {
        time = startDateOfMonth
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val startOffsetMillis = if (startCalendar.timeInMillis < startOfMonthCalendar.timeInMillis) {
        0L
    } else {
        startCalendar.timeInMillis - startOfMonthCalendar.timeInMillis
    }

    return (startOffsetMillis / (1000 * 60 * 60 * 24)).toInt()
}


data class Event(
    val id: String,
    val title: String, val startDate: Date, val endDate: Date
)

fun getStartOfCurrentMonth(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    return calendar.time
}

fun getEndOfCurrentMonth(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    return calendar.time
}

fun getFirstDayOfCurrentMonth(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}


