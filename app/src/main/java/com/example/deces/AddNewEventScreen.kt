package com.example.deces

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(300.dp)
            .padding(vertical = 8.dp)
            .border(
                width = 0.dp, color = Color.Transparent
            )
            .background(
                Color(0xFF8A6D57), shape = RoundedCornerShape(30.dp)
            ),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        )
    )
}


@Composable
fun AddNewEventScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedcity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDateTimestamp by remember { mutableStateOf<Long?>(null) }
    var endDateTimestamp by remember { mutableStateOf<Long?>(null) }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var photo1 by remember { mutableStateOf("") }
    var photo2 by remember { mutableStateOf("") }
    var photo3 by remember { mutableStateOf("") }
    var photo4 by remember { mutableStateOf("") }
    var photo5 by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    var selectedInterest by remember { mutableStateOf<String?>(null) }

    val firestore = FirebaseFirestore.getInstance()
    val cities = remember { mutableStateListOf<String>() } // To store city names
    var selectedCity by remember { mutableStateOf("") } // Selected city

    LaunchedEffect(Unit) {
        firestore.collection("availableCities").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val name = document.getString("name")
                if (name != null) cities.add(name)
            }
        }.addOnFailureListener {
            println("Error fetching cities: ${it.message}")
        }
    }

    fun showDateTimePicker(onDateTimeSelected: (Long) -> Unit) {
        DatePickerDialog(
            navController.context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    navController.context, { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                        onDateTimeSelected(calendar.timeInMillis)
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF291B11))
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                "Kreiraj novi događaj",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 20.dp),
                color = Color.White
            )
        }

        item {
            CustomTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name (required)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            var buttonWidth by remember { mutableStateOf(0) }

            var expanded by remember { mutableStateOf(false) }
            var interests by remember { mutableStateOf<List<String>>(emptyList()) }


            // Fetch interests from Firebase Firestore
            LaunchedEffect(Unit) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("all_interests").document("interests").get()
                    .addOnSuccessListener { document ->
                        val fetchedInterests = document.get("interests") as? List<String>
                        if (fetchedInterests != null) {
                            interests = fetchedInterests
                        }
                    }.addOnFailureListener {
                        // Handle error if needed
                        interests = emptyList()
                    }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                // Button to trigger Dropdown
                Button(onClick = { expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A6D57)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            buttonWidth = coordinates.size.width
                        }) {
                    Text(
                        text = ( selectedInterest ?: "Select Category (required)"),
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                // Dropdown menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current) { buttonWidth.toDp() })
                        .background(Color(0xFF8A6D57))
                ){
                    interests.forEach { interest ->
                        DropdownMenuItem(onClick = {
                            selectedInterest = interest
                            expanded = false
                        }, text = {
                            Text(text = interest)
                        })
                    }
                }
            }
        }

        item {
            var expanded by remember { mutableStateOf(false) }
            var buttonWidth by remember { mutableStateOf(0) }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A6D57)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            buttonWidth = coordinates.size.width
                        }) {
                    Text(
                        text = if (selectedCity.isEmpty()) "Select city (required)" else selectedCity,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current) { buttonWidth.toDp() })
                        .background(Color(0xFF8A6D57))
                ) {
                    cities.forEachIndexed { index, city ->
                        DropdownMenuItem(onClick = {
                            selectedCity = city
                            expanded = false
                        }, modifier = Modifier.fillMaxWidth(), text = {
                            Text(text = city, color = Color.White)
                        })
                        if (index != cities.size - 1) {
                            Divider(
                                color = Color(0xFF291b11),
                                thickness = 1.dp,
                            )
                        }
                    }
                }
            }
        }

        item {
            CustomTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (required)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    showDateTimePicker { timestamp -> startDateTimestamp = timestamp }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)

            ) {
                Text(startDateTimestamp?.let { "Start Date: ${Date(it)}" }
                    ?: "Select Start Date (required)", modifier = Modifier.padding(8.dp))
            }
        }

        item {
            Button(
                onClick = {
                    showDateTimePicker { timestamp -> endDateTimestamp = timestamp }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp)
            ) {
                Text(endDateTimestamp?.let { "End Date: ${Date(it)}" }
                    ?: "Select End Date (required)", modifier = Modifier.padding(8.dp))
            }
        }

        item {
            CustomTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = "Latitude (required)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            CustomTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = "Longitude (required)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            CustomTextField(
                value = photo1,
                onValueChange = { photo1 = it },
                label = "Photo 1 URL (recommended)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            CustomTextField(
                value = photo2,
                onValueChange = { photo2 = it },
                label = "Photo 2 URL (optional)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            CustomTextField(
                value = photo3,
                onValueChange = { photo3 = it },
                label = "Photo 3 URL  (optional)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            CustomTextField(
                value = photo4,
                onValueChange = { photo4 = it },
                label = "Photo 4 URL  (optional)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            CustomTextField(
                value = photo5,
                onValueChange = { photo5 = it },
                label = "Photo 5 URL  (optional)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    val event = mapOf(
                        "name" to name,
                        "category" to selectedInterest,
                        "city" to selectedCity,
                        "description" to description,
                        "startDate" to startDateTimestamp?.let { Timestamp(Date(it)) },
                        "endDate" to endDateTimestamp?.let { Timestamp(Date(it)) },
                        "latitude" to latitude.toDoubleOrNull(),
                        "longitude" to longitude.toDoubleOrNull(),
                        "photo1" to photo1.takeIf { it.isNotEmpty() },
                        "photo2" to photo2.takeIf { it.isNotEmpty() },
                        "photo3" to photo3.takeIf { it.isNotEmpty() },
                        "photo4" to photo4.takeIf { it.isNotEmpty() },
                        "photo5" to photo5.takeIf { it.isNotEmpty() },
                        "rating" to 0.0,
                        "ratingCount" to 0,
                        "ratingSum" to 0
                    )

                    val db = FirebaseFirestore.getInstance()
                    val eventRef = db.collection("events").document()
                    eventRef.set(event).addOnSuccessListener {
                        Toast.makeText(
                            navController.context,
                            "Događaj uspješno spremljen!",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                    }.addOnFailureListener { exception ->
                        Toast.makeText(
                            navController.context,
                            "Greška pri spremanju događaja: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Spremi Event")
            }
        }
    }
}

