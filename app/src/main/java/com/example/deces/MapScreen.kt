package com.example.deces

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.deces.bottomnavigationbar.BottomNavigationItems
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.UiSettings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {

    if (FirebaseAuth.getInstance().currentUser == null) {
        navController.navigate("home")
    } else {
        val context = LocalContext.current
        var marker: Marker?
        val markers = mutableListOf<Marker?>()
        val availableCities = remember { mutableStateListOf<City>() }
        var hasLocationPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val locationPermissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
                onResult = { granted ->
                    hasLocationPermission = granted
                })

        LaunchedEffect(key1 = true) {
            if (!hasLocationPermission) {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            FirebaseFirestore.getInstance().collection("availableCities").get()
                .addOnSuccessListener { documents ->
                    for (document in documents.documents) {
                        val city = document.toObject(City::class.java)
                        city?.let { availableCities.add(it) }
                    }
                }

        }
        BackHandler {
            (context as ComponentActivity).finish()
        }



        if (hasLocationPermission) {
            Column {

                val isDropDownExpanded = remember { mutableStateOf(false) }
                val itemPosition = remember { mutableStateOf(0) }

                var cities by remember { mutableStateOf<List<City>>(emptyList()) }

                LaunchedEffect(Unit) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("availableCities")
                        .get()
                        .addOnSuccessListener { result ->
                            cities = result.map {
                                val name = it.getString("name") ?: "Unknown"
                                val lat = it.getDouble("latitude") ?: 0.0
                                val lng = it.getDouble("longitude") ?: 0.0
                                City(name, lat, lng)
                            }
                        }
                        .addOnFailureListener { exception ->

                        }
                }

                // Koristimo Column za layout
                Column(
                    modifier = Modifier.fillMaxWidth().padding(50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Box {
                        // Dodajemo Row za dropdown button
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                isDropDownExpanded.value = true
                            }
                        ) {

                            Text(text = CameraBounds.selectedCityName)
                        }

                        // Dropdown Menu
                        DropdownMenu(
                            expanded = isDropDownExpanded.value,
                            onDismissRequest = {
                                isDropDownExpanded.value = false
                            }
                        ) {
                            cities.forEachIndexed { index, city ->

                                DropdownMenuItem(
                                    text={
                                        Text(text = city.name)
                                    },
                                    onClick = {
                                        isDropDownExpanded.value = false
                                        itemPosition.value = index // Postavi odabrani grad

                                        // Uzmi lat i lng gradova
                                        val latitude = city.latitude
                                        val longitude = city.longitude
                                        CameraBounds.selectedCityName=city.name

                                        // Ažuriraj CameraBounds s novim koordinatama
                                        CameraBounds.setCoordinates(latitude, longitude)

                                        // Ažuriraj poziciju kamere
                                        val cameraPosition = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 13.7f)
                                        CameraBounds.setCameraPosition(cameraPosition)
                                        navController.navigate(BottomNavigationItems.MapScreen.route)


                                    }
                                )
                            }
                        }
                    }
                }

                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            onCreate(null)
                            onResume()
                            getMapAsync { googleMap ->
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraBounds.getCameraPosition()
                                    )
                                )
                                googleMap.setOnCameraMoveListener {
                                    CameraBounds.setCameraPosition(
                                        googleMap.cameraPosition
                                    )
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

                                FirebaseFirestore.getInstance().collection("events").get()
                                    .addOnSuccessListener { documents ->
                                        for (document in documents.documents) {
                                            val coordinates = LatLng(
                                                document.data!!["latitude"].toString().toDouble(),
                                                document.data!!["longitude"].toString().toDouble()
                                            )
                                            locations.add(MapMarker(document.id, coordinates))
                                        }
                                    }.addOnCompleteListener {
                                        for (location in locations) {
                                            val myMarker =
                                                googleMap.addMarker(
                                                    MarkerOptions().position(
                                                        location.cordinates
                                                    )
                                                )
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
                                                ).icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                        BitmapDescriptorFactory.HUE_AZURE
                                                    )
                                                ).title("Its here!")


                                            )

                                            for (mark in markers) {
                                                if (marker!!.position == mark?.position) marker!!.tag =
                                                    mark.tag

                                            }
                                            googleMap.setOnMapClickListener {
                                                marker!!.remove()
                                            }

                                            CameraBounds.showSpecifiedLocationOnMap = false
                                            marker?.showInfoWindow()

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
                        }
                    }, modifier = Modifier.fillMaxSize()
                )
}

        } else {

            Text(
                text = "Permission not granted for accessing location.",
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center
            )
        }
    }
}


data class MapMarker(
    var id: String, var cordinates: LatLng
)
    data class City(
        val name: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
    )