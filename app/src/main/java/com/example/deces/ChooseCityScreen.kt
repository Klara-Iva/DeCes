package com.example.deces

import android.app.Activity
import android.graphics.Camera
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.deces.bottomnavigationbar.BottomNavigationItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ChooseCityScreen(navController: NavController, fromProfile: Boolean) {
    val firestore = FirebaseFirestore.getInstance()
    val cities = remember { mutableStateListOf<String>() } // To store city names
    var selectedCity by remember { mutableStateOf("") } // Selected city
    var expanded by remember { mutableStateOf(false) } // Dropdown state
    val context = LocalContext.current

    // Fetch city names from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("availableCities").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val name = document.getString("name")
                if (name != null) cities.add(name)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Greška pri dohvaćanju gradova: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF291B11))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Title
            Text(
                text = "Odaberi mjesto",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Odaberi grad za prikaz događaja u blizini",
                fontSize = 16.sp,
                color = Color(0xFFB3A9A1)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                var buttonWidth by remember { mutableStateOf(0) }
                var selectedCityName by remember { mutableStateOf(CameraBounds.selectedCityName) }

                Button(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A6D57)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            buttonWidth = coordinates.size.width
                        }
                ) {
                    Text(
                        text = if (selectedCityName.isEmpty()) "Odaberi" else selectedCityName,
                        color = Color.White
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
                        DropdownMenuItem(
                            onClick = {
                                selectedCityName = city
                                CameraBounds.selectedCityName = city
                                expanded = false
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = {
                                Text(text = city, color = Color.White)
                            }
                        )
                        if (index != cities.size - 1) {
                            Divider(
                                color = Color(0xFF291b11),
                                thickness = 1.dp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next Button
            Button(
                onClick = {
                    if (CameraBounds.selectedCityName.isNotEmpty()) {
                        val auth = FirebaseAuth.getInstance()
                        val currentUser = auth.currentUser

                        if (currentUser != null) {
                            firestore.collection("users").document(currentUser.uid)
                                .update("chosenCity", CameraBounds.selectedCityName)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Grad uspješno spremljen: ${CameraBounds.selectedCityName}", Toast.LENGTH_SHORT).show()
                                    if (fromProfile) {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate("chooseInterests")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Greška pri spremanju grada: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Greška: Korisnik nije prijavljen.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Greška: Niste odabrali grad.", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58845)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Spremi odabir", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

