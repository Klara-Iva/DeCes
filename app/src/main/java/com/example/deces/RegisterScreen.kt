package com.example.deces

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF291B11))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Text
            Text(
                text = "Kreiraj novi račun",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Vec imas račun?",
                    fontSize = 14.sp,
                    color = Color(0xFFB3A9A1)
                )
                TextButton(onClick = { navController.navigate("login") }) {
                    Text(
                        text = "Prijavi se",
                        fontSize = 14.sp,
                        color = Color(0xFFF58845),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("IME", color = Color.White) },
                modifier = Modifier
                    .width(300.dp)
                    .padding(vertical = 8.dp)
                    .background(
                        Color(0xFF8A6D57),
                        shape = RoundedCornerShape(50)
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

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("EMAIL", color = Color.White) },
                modifier = Modifier
                    .width(300.dp)
                    .padding(vertical = 8.dp)
                    .background(
                        Color(0xFF8A6D57),
                        shape = RoundedCornerShape(50)
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
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("LOZINKA", color = Color.White) },
                modifier = Modifier
                    .width(300.dp)
                    .padding(vertical = 8.dp)
                    .background(
                        Color(0xFF8A6D57),
                        shape = RoundedCornerShape(50)
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

            var birthDate by remember { mutableStateOf("") }
            val context = LocalContext.current
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val openDatePicker = {
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                        birthDate = "$selectedDay.${selectedMonth + 1}.$selectedYear"
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }

            OutlinedTextField(
                value = birthDate,
                onValueChange = {},
                label = { Text("DATUM ROĐENJA", color = Color.White) },
                modifier = Modifier
                    .width(300.dp)
                    .padding(vertical = 8.dp)
                    .height(60.dp)
                    .background(
                        Color(0xFF8A6D57),
                        shape = RoundedCornerShape(50)
                    ),
                readOnly = true,
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
                trailingIcon = {
                    IconButton(onClick = openDatePicker) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date Picker",
                            tint = Color.White
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Registration Button
            Button(
                onClick = {
                    println("Name: $name, Email: $email, Password: $password, BirthDate: $birthDate")
                    val auth = FirebaseAuth.getInstance()
                    if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && birthDate.isNotEmpty()) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context,"Uspješna registracija!",Toast.LENGTH_SHORT).show()
                                    val currentUser = auth.currentUser
                                    if (currentUser != null) {
                                        val userData = hashMapOf(
                                            "name" to name,
                                            "email" to email,
                                            "chosenCity" to "",
                                            "interests" to emptyList<String>(),
                                            "birthdate" to birthDate.toString()
                                        )
                                        firestore.collection("users").document(currentUser.uid)
                                            .set(userData).addOnSuccessListener {
                                                firestore.collection("users")
                                                    .document(currentUser.uid).collection("reviews")
                                                    .add(hashMapOf("initialized" to true))
                                                    .addOnCompleteListener {
                                                        navController.navigate("chooseCity")
                                                    }
                                            }.addOnFailureListener { e ->
                                                println("Error adding user: ${e.message}")
                                            }
                                    } else {
                                        println("Error: User not logged in even after registration.")
                                    }
                                } else {
                                    println("Error: ${task.exception?.message}")
                                }
                            }
                    } else {
                        Toast.makeText(context,"Popunite sva polja!",Toast.LENGTH_SHORT).show()
                        println("Please fill all fields.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58845)), // Button color
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .width(280.dp)
                    .height(50.dp)
            ) {
                Text("Registracija", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}


@Composable
fun DatePickerExample() {
    var date by remember { mutableStateOf("") }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val openDatePicker = {
        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                date = "$selectedDay/${selectedMonth + 1}/$selectedYear" // Format the date
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    OutlinedTextField(
        value = date,
        onValueChange = {},
        label = { Text("Datum rođenja", color = Color.White) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(16.dp),
        readOnly = true, // Prevent text input directly

        trailingIcon = {
            IconButton(onClick = openDatePicker) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date Picker",
                    tint = Color.White
                )
            }
        }
    )
}
