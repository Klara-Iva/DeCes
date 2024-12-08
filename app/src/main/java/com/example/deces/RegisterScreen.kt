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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.deces.bottomnavigationbar.BottomNavigationItems
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

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
                text = "Kreiraj novi racun!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Vec imas racun?",
                    fontSize = 16.sp,
                    color = Color(0xFFB3A9A1) // Slightly lighter color for subtitle
                )
                TextButton(onClick = { /* Handle navigation or action */ }) {
                    Text(
                        text = "Prijavi se",
                        fontSize = 16.sp,
                        color = Color(0xFFF58845), // Link color
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("IME", color = Color.White) },// Label inside the TextField
                modifier = Modifier
                    .width(280.dp)
                    .padding(vertical = 8.dp)
                    .background(Color(0xFF8A6D57), shape = RoundedCornerShape(51)), // Background color and rounded corners
                singleLine = true,
                textStyle = TextStyle(color = Color.White)
            )

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("EMAIL", color = Color.White) },// Label inside the TextField
                modifier = Modifier
                    .width(280.dp)
                    .padding(vertical = 8.dp)
                    .background(Color(0xFF8A6D57), shape = RoundedCornerShape(51)), // Background color and rounded corners
                singleLine = true,
                textStyle = TextStyle(color = Color.White)
            )

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("LOZINKA", color = Color.White) },// Label inside the TextField
                modifier = Modifier
                    .width(280.dp)
                    .padding(vertical = 8.dp)
                    .background(Color(0xFF8A6D57), shape = RoundedCornerShape(51)), // Background color and rounded corners
                singleLine = true,
                textStyle = TextStyle(color = Color.White)
            )

            // Birth Date Input
            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("DATUM RODJENJA", color = Color.White) },// Label inside the TextField
                modifier = Modifier
                    .width(280.dp)
                    .padding(vertical = 8.dp)
                    .background(Color(0xFF8A6D57), shape = RoundedCornerShape(51)), // Background color and rounded corners
                singleLine = true,
                textStyle = TextStyle(color = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Registration Button
            Button(
                onClick = {
                    println("Name: $name, Email: $email, Password: $password, BirthDate: $birthDate")
                    val auth = FirebaseAuth.getInstance()
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Navigate to another screen or show a success message
                                    navController.navigate(BottomNavigationItems.Screen3.route)
                                } else {
                                    // Handle registration error
                                    println("Error: ${task.exception?.message}")
                                }
                            }
                    } else {
                        // Handle empty fields
                        println("Please fill all fields.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58845)), // Button color
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Registracija", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}