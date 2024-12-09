package com.example.deces

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()

    // Hardkodirani podaci za prijavu
    val email = "privatno.privremeni@gmail.com"
    val password = "test123"

    // UI za ekran
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Kliknite za prijavu",
                fontSize = 20.sp,
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Gumb za prijavu
            Button(
                onClick = {
                    // Prijava putem hardkodiranih podataka
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Ako je prijava uspješna, preusmjeri na sljedeći ekran
                                navController.navigate("home") // Zamijeni s vašom početnom stranicom
                            } else {
                                // Ako prijava nije uspješna, prikazujemo poruku
                                Toast.makeText(
                                    navController.context,
                                    "Prijava nije uspjela: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Prijavi se", color = Color.White)
            }
        }
    }
}
