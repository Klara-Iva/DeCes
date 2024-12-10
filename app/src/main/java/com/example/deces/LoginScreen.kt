package com.example.deces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
                text = "Prijava",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("EMAIL", color = Color.White) },
                modifier = Modifier
                    .width(280.dp)
                    .padding(vertical = 8.dp)
                    .background(Color(0xFF8A6D57), shape = RoundedCornerShape(51)),
                singleLine = true,
                textStyle = TextStyle(color = Color.White)
            )

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("LOZINKA", color = Color.White) },
                modifier = Modifier
                    .width(280.dp)
                    .padding(vertical = 8.dp)
                    .background(Color(0xFF8A6D57), shape = RoundedCornerShape(51)),
                singleLine = true,
                textStyle = TextStyle(color = Color.White),
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
                                    // Navigate to the home screen or other screen
                                    navController.navigate(BottomNavigationItems.Screen3.route)
                                } else {
                                    // Handle login error
                                    val errorMessage = task.exception?.localizedMessage ?: "Login failed"
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
