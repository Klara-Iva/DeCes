package com.example.deces

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Screen5(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser // Trenutno prijavljeni korisnik
    val userName = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) } // Za praćenje stanja dijaloga
    val isDialogOpen = remember { mutableStateOf(false) }
    val newUserName = remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf("") }
    val isDialogOpen3 = remember { mutableStateOf(false) }
    val newProfilePictureUrl = remember { mutableStateOf("") }


    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    userName.value = documentSnapshot.get("name").toString()
                    profilePictureUrl = documentSnapshot.getString("profilePicture") ?: ""

                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        navController.context,
                        "Greška pri dohvaćanju podataka: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
    if (isDialogOpen.value) {
        AlertDialog(onDismissRequest = { isDialogOpen.value = false },
            title = { Text(text = "Uredi korisničko ime") },
            text = {
                Column {
                    BasicTextField(value = newUserName.value,
                        onValueChange = { newUserName.value = it },
                        modifier = Modifier
                            .background(Color.White)
                            .padding(10.dp)
                            .fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.padding(8.dp)) {
                                if (newUserName.value.isEmpty()) {
                                    Text("Unesite novo ime", color = Color.Gray)
                                }
                                innerTextField()
                            }
                        })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val uid = user?.uid
                    if (uid != null && newUserName.value.isNotEmpty()) {
                        firestore.collection("users").document(uid)
                            .update("name", newUserName.value).addOnSuccessListener {
                                userName.value =
                                    newUserName.value // Ažurira korisničko ime u aplikaciji
                                isDialogOpen.value = false // Zatvara popup
                                Toast.makeText(
                                    navController.context,
                                    "Korisničko ime ažurirano",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.addOnFailureListener { exception ->
                                Toast.makeText(
                                    navController.context,
                                    "Greška pri ažuriranju: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }) {
                    Text("Spremi")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogOpen.value = false }) {
                    Text("Otkaži")
                }
            })
    }
    if (showDialog.value) {
        // Popup za potvrdu slanja e-maila za promjenu lozinke
        AlertDialog(onDismissRequest = { showDialog.value = false },
            title = { Text("Potvrda") },
            text = { Text("Jeste li sigurni da želite poslati mail za promjenu lozinke?") },
            confirmButton = {
                TextButton(onClick = {
                    auth.currentUser?.let {
                        // Ako je korisnik prijavljen, šaljemo email za promjenu lozinke
                        auth.sendPasswordResetEmail(it.email!!).addOnSuccessListener {
                            Toast.makeText(
                                navController.context,
                                "E-mail za promjenu lozinke poslan!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener { exception ->
                            Toast.makeText(
                                navController.context,
                                "Greška: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    showDialog.value = false
                }) {
                    Text("Da")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Odustani")
                }
            })
    }

    if (isDialogOpen3.value) {
        AlertDialog(
            onDismissRequest = { isDialogOpen3.value = false },
            title = { Text(text = "Unesite URL nove profilne slike") },
            text = {
                Column {
                    BasicTextField(
                        value = newProfilePictureUrl.value,
                        onValueChange = { newProfilePictureUrl.value = it },
                        modifier = Modifier
                            .background(Color.White)
                            .padding(10.dp)
                            .fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.padding(8.dp)) {
                                if (newProfilePictureUrl.value.isEmpty()) {
                                    Text("Unesite URL nove slike", color = Color.Gray)
                                }
                                innerTextField()
                            }
                        })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val uid = user?.uid
                    if (uid != null && newProfilePictureUrl.value.isNotEmpty()) {
                        firestore.collection("users").document(uid)
                            .update("profilePicture", newProfilePictureUrl.value)
                            .addOnSuccessListener {
                                profilePictureUrl = newProfilePictureUrl.value
                                isDialogOpen3.value = false
                                Toast.makeText(
                                    navController.context,
                                    "URL profilne slike ažuriran",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    navController.context,
                                    "Greška pri ažuriranju: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }) {
                    Text("Spremi")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogOpen3.value = false }) {
                    Text("Otkaži")
                }
            })
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Gornji dio pozadine (narančasta boja)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.4f) // 40% visine
                .background(Color(0xFFFFA259)) // Gornja boja
        )

        // Donji dio pozadine (smeđa boja)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.6f) // Preostala visina
                .background(Color(0xFF291b11)) // Donja boja
                .align(Alignment.BottomStart)
        )

        // Glavni sadržaj
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 65.dp)
        ) {
            // Profilna slika i korisničko ime
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(color = Color.Gray)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                isDialogOpen3.value = true
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {

                if (profilePictureUrl.isNotEmpty()) {
                    Image(
                        painter = rememberImagePainter(
                            profilePictureUrl
                        ),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
            }

            Text(
                text = userName.value,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 20.dp, bottom = 40.dp)
            )

            // Izbornik
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(
                        color = Color(0xFF382315), shape = RoundedCornerShape(21.dp)
                    )
                    .fillMaxWidth()
            ) {
                MenuItem(icon = Icons.Default.Edit,
                    title = "Uredi korisničko ime",
                    onClick = { isDialogOpen.value = true } // Otvara popup
                )
                Divider(color = Color(0xFF6a5240), thickness = 1.dp)
                MenuItem(icon = Icons.Default.Lock,
                    title = "Promijeni lozinku",
                    onClick = { showDialog.value = true })
                Divider(color = Color(0xFF6a5240), thickness = 1.dp)
                MenuItem(icon = Icons.Default.Settings, title = "Uredi preference")
                Divider(color = Color(0xFF6a5240), thickness = 1.dp)
                MenuItem(
                    icon = Icons.Default.LocationOn,
                    title = "Uredi lokaciju",
                    onClick = { navController.navigate("chooseCity?fromProfile=true") }
                )
                Divider(color = Color(0xFF6a5240), thickness = 1.dp)
                MenuItem(icon = Icons.Default.ExitToApp, title = "Odjava", onClick = {
                    auth.signOut() // Odjava
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                })
            }
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, title: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onClick?.invoke() }, // Pozivanje onClick funkcije
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFA259),
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 16.dp)
            )
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color(0xFFf58845),
            modifier = Modifier
                .size(35.dp)
                .padding(end = 16.dp)
        )
    }
}

