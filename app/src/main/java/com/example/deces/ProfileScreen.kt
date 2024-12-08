package com.example.deces

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Screen5() {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val currentUser = firebaseAuth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        if (currentUser != null) {
            // Korisnik je logiran
            Text(
                text = "Welcome, ${currentUser.email ?: "Unknown User"}", fontSize = 18.sp
            )
        } else {
            // Korisnik nije logiran
            Text(
                text = "You are not logged in.", fontSize = 18.sp
            )
        }
    }
}
