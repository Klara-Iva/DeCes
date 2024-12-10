package com.example.deces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.deces.bottomnavigationbar.BottomNavigationItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF291B11)) // Background color: #291b11
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Text
            Text(
                text = "Prijavi se",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Nemaš račun? ",
                    fontSize = 14.sp,
                    color = Color(0xFFB3A9A1),
                )

                Text(text = " Registriraj se",
                    fontSize = 14.sp,
                    color = Color(0xFFf58845),
                    modifier = Modifier.clickable {
                        navController.navigate("Register")
                    })
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Email Input
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("EMAIL") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(300.dp)
                    .padding(vertical = 8.dp)
                    .border(
                        width = 0.dp, color = Color.Transparent
                    )
                    .background(
                        Color(0xFF8A6D57), shape = RoundedCornerShape(51)
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
                ),
                singleLine = true,
            )

            // Password Input
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("LOZINKA") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(300.dp)
                    .padding(vertical = 8.dp)
                    .border(
                        width = 0.dp, color = Color.Transparent
                    )
                    .background(
                        Color(0xFF8A6D57), shape = RoundedCornerShape(51)
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
                ),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("waitscreenroute")
                                } else {
                                    val errorMessage =
                                        task.exception?.localizedMessage ?: "Login failed"
                                    println("Error: $errorMessage")
                                }
                            }
                    } else {
                        println("Please fill all fields.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58845)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Prijava", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
