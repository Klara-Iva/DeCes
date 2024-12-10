package com.example.deces

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
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
import java.io.InputStream


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

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
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
                    db.collection("availableCities").get().addOnSuccessListener { result ->
                            cities = result.map {
                                val name = it.getString("name") ?: "Unknown"
                                val lat = it.getDouble("latitude") ?: 0.0
                                val lng = it.getDouble("longitude") ?: 0.0
                                val zoom = it.getString("zoom") ?: "13.7f"
                                City(name, lat, lng, zoom)
                            }
                        }.addOnFailureListener { exception ->

                        }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                ) {
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

                                    try {
                                        val styleInputStream: InputStream = resources.openRawResource(R.raw.dark_map_style)
                                        val styleString = styleInputStream.bufferedReader().use { it.readText() }
                                        val mapStyleOptions = MapStyleOptions(styleString)

                                        val success = googleMap.setMapStyle(mapStyleOptions)
                                        if (!success) {
                                            android.util.Log.e("MapStyle", "Failed to apply dark style")
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("MapStyle", "Error loading map style", e)
                                    }


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
                                                val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pin3)

                                                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 57, 100, false)

                                                val pinIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

                                                val myMarker = googleMap.addMarker(
                                                    MarkerOptions().position(location.cordinates)
                                                        .icon(pinIcon)
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
                                                    ).title("It's here!")
                                                )

                                                for (mark in markers) {
                                                    if (marker!!.position == mark?.position) marker!!.tag = mark.tag
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

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(2f),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .zIndex(3f)
                                .background(Color.Transparent),
                        ){
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .clickable {
                                        isDropDownExpanded.value = true
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF291B11)
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .wrapContentWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = CameraBounds.selectedCityName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isDropDownExpanded.value) Icons.Default.KeyboardArrowDown else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 65.dp, end = 20.dp, bottom = 10.dp)
                            .zIndex(3f),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        DropdownMenu(
                            expanded = isDropDownExpanded.value,
                            onDismissRequest = { isDropDownExpanded.value = false },
                            modifier = Modifier
                                .background(
                                    Color(0xFF8A6D57),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .align(Alignment.TopEnd)
                        ) {
                            cities.forEachIndexed { index, city ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = city.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        isDropDownExpanded.value = false
                                        itemPosition.value = index

                                        val latitude = city.latitude
                                        val longitude = city.longitude
                                        CameraBounds.selectedCityName = city.name
                                        val zoom = city.zoom
                                        CameraBounds.setCoordinates(latitude, longitude)
                                        val cameraPosition = CameraPosition.fromLatLngZoom(
                                            LatLng(latitude, longitude), zoom.toFloat()
                                        )
                                        CameraBounds.setCameraPosition(cameraPosition)
                                        navController.navigate(BottomNavigationItems.MapScreen.route)
                                    }
                                )
                                if (index != cities.size - 1) {
                                    Divider(
                                        color = Color(0xFF291b11),
                                        thickness = 1.dp,
                                    )
                                }
                            }
                        }
                    }

                }
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
    val longitude: Double = 0.0,
    val zoom: String = ""
)