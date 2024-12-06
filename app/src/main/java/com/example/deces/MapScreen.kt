package com.example.deces

import android.Manifest
import android.content.pm.PackageManager

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.UiSettings
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.deces.bottomnavigationbar.BottomBar
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    var marker: Marker?
    val markers = mutableListOf<Marker?>()


    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    BackHandler {
        (context as ComponentActivity).finish()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasLocationPermission) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        onCreate(null)
                        onResume()
                        getMapAsync { googleMap ->
                            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraBounds.getCameraPosition()))
                            googleMap.setOnCameraMoveListener {
                                CameraBounds.setCameraPosition(googleMap.cameraPosition)
                            }
                            googleMap.isMyLocationEnabled = true

                            googleMap.setMapStyle(
                                MapStyleOptions(
                                    """
                                [
                                    {
                                        "featureType": "poi",
                                        "elementType": "all",
                                        "stylers": [
                                            { "visibility": "off" }
                                        ]
                                    },
                                    {
                                        "featureType": "road",
                                        "elementType": "geometry",
                                        "stylers": [
                                            { "visibility": "simplified" }
                                        ]
                                    }
                                ]
                                """.trimIndent()
                                )
                            )

                            val uiSettings: UiSettings = googleMap.uiSettings
                            uiSettings.isZoomControlsEnabled = true
                            val locations = mutableListOf<MapMarker>()

                            FirebaseFirestore.getInstance().collection("events")
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents.documents) {
                                        val coordinates = LatLng(
                                            document.data!!["latitude"].toString().toDouble(),
                                            document.data!!["longitude"].toString().toDouble()
                                        )
                                        locations.add(MapMarker(document.id, coordinates))
                                    }
                                }
                                .addOnCompleteListener {
                                    for (location in locations) {
                                        val myMarker =
                                            googleMap.addMarker(MarkerOptions().position(location.cordinates))
                                        myMarker!!.tag = location.id
                                        markers.add(myMarker)
                                    }
                                    if (CameraBounds.showSpecifiedLocationOnMap) {

                                        marker = googleMap.addMarker(
                                            MarkerOptions().position(
                                                LatLng(
                                                    CameraBounds.latitude,
                                                    CameraBounds.longitude
                                                )
                                            )
                                                .icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_AZURE
                                                    )
                                                )
                                                .title("Its here!")


                                        )

                                        for (mark in markers) {
                                            if (marker!!.position == mark?.position)
                                                marker!!.tag = mark.tag

                                        }
                                        googleMap.setOnMapClickListener {
                                            marker!!.remove()
                                        }

                                        CameraBounds.showSpecifiedLocationOnMap = false
                                        marker?.showInfoWindow()

                                    }

                                }

                            googleMap.setOnMarkerClickListener { marker ->
                                val documentId = marker.tag as? String
                                if (documentId != null) {
                                    navController.navigate("eventDetail/$documentId")
                                }
                                true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            BottomBar(
                navController = navController, state = true, modifier = Modifier
                    .padding(bottom = 10.dp, start = 5.dp, end = 5.dp, top = 5.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color(android.graphics.Color.parseColor("#382315")))
                    .height(60.dp)
                    .zIndex(1f)
                    .align(Alignment.BottomCenter)
            )

        } else {
            Text(
                text = "Permission not granted for accessing location.",
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center
            )
        }
    }
}


data class Location(
    val id: String,
    val name: String,
    val description: String,
    val photo1: String
)

data class MapMarker(
    var id: String,
    var cordinates: LatLng
)