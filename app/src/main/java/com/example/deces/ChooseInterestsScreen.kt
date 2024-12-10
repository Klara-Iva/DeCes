package com.example.deces

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.deces.bottomnavigationbar.BottomNavigationItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.accompanist.flowlayout.FlowRow


@Composable
fun ChooseInterestsScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid // Get the currently logged-in user's ID
    val allInterests = remember { mutableStateListOf<String>() } // All available interests
    val selectedInterests = remember { mutableStateListOf<String>() } // User-selected interests

    // Fetch all interests from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("all_interests").document("interests").get()
            .addOnSuccessListener { document ->
                val interests = document.get("interests") as? List<String>
                if (interests != null) {
                    allInterests.addAll(interests)
                }
            }
            .addOnFailureListener {
                println("Error fetching interests: ${it.message}")
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
                text = "Odaberi do 5 interesa",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Personaliziraj događaje odabirom interesa",
                fontSize = 16.sp,
                color = Color(0xFFB3A9A1)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Display Interests as Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                allInterests.forEach { interest ->
                    Button(
                        onClick = {
                            if (selectedInterests.contains(interest)) {
                                selectedInterests.remove(interest) // Deselect if already selected
                            } else if (selectedInterests.size < 5) {
                                selectedInterests.add(interest) // Add to selected if less than 5
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedInterests.contains(interest)) Color(0xFFF58845) else Color(0xFF8A6D57)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(text = interest, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Finish Registration Button
            Button(
                onClick = {
                    if (userId != null) { // Ensure userId is not null
                        // Save selected interests to Firestore
                        firestore.collection("users").document(userId)
                            .update("interests", selectedInterests)
                            .addOnSuccessListener {
                                println("Interests saved successfully!")
                                navController.navigate("home") // Navigate to the next screen
                            }
                            .addOnFailureListener { e ->
                                println("Error saving interests: ${e.message}")
                            }
                    } else {
                        println("Error: User not logged in.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58845)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Završi registraciju", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
