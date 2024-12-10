package com.example.deces


import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore

object CameraBounds {
    lateinit var camerapostion: CameraPosition
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var zoom: Float = 12.7f
    var showSpecifiedLocationOnMap = false
    var selectedCityName: String = "Osijek"

    fun getCoordinatesFromBase(uid: String) {

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get().addOnSuccessListener { result ->
            selectedCityName = result["chosenCity"].toString()
        }.addOnCompleteListener {
            db.collection("availableCities").get().addOnSuccessListener { result ->
                for (document in result.documents) {
                    val cityName = document.getString("name")
                    if (cityName == selectedCityName) {
                        latitude = document.getDouble("latitude") ?: 0.0
                        longitude = document.getDouble("longitude") ?: 0.0
                        zoom = document.getString("zoom")?.toFloat() ?: 12.7f
                        camerapostion =
                            CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), zoom)
                    }
                }
            }
        }
    }

    fun setCoordinates(lat: Double, lng: Double) {
        latitude = lat
        longitude = lng
    }

    fun setCameraPosition(position: CameraPosition) {
        camerapostion = position


    }

    fun getCameraPosition(): CameraPosition {
        return camerapostion
    }
}