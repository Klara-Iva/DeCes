package com.example.deces

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.deces.ui.theme.DecesTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DisplayLocationName()
        }
    }
}

@Composable
fun DisplayLocationName() {
    // Firebase Firestore instance
    val db = FirebaseFirestore.getInstance()
    var locationName by remember { mutableStateOf("Loading...") } // State za prikaz imena

    // Dohvaćanje podataka iz Firestore-a
    LaunchedEffect(Unit) {
        db.collection("locations")
            .document("jXNMiLbpBsyiMzI5Asp5") // ID dokumenta
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    locationName = document.getString("name") ?: "Name not found"
                } else {
                    locationName = "Document not found"
                }
            }
            .addOnFailureListener { exception ->
                locationName = "Error: ${exception.message}"
            }
    }

    // Prikaz podataka u Jetpack Compose UI
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Location Name:")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = locationName)

            // Integracija s Google Mapom
            val cameraPositionState = rememberCameraPositionState {
                position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                    LatLng(37.7749, -122.4194), 12f) // Početna pozicija mape (San Francisco)
            }

            GoogleMap(
                cameraPositionState = cameraPositionState,
                modifier = Modifier.fillMaxSize()
            ) {
            }
        }
    }
}
