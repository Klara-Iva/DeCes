package com.example.deces

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.deces.ui.theme.DecesTheme
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.deces.bottomnavigationbar.BottomBar
import com.example.deces.bottomnavigationbar.NavigationGraph
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

private const val RC_SIGN_IN = 9001


class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController // Define navController as a property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DecesTheme {
                navController = rememberNavController() // Initialize navController here
                val currentRoute by navController.currentBackStackEntryAsState()
                var isBottomBarVisible by remember { mutableStateOf(true) }
                val onBottomBarVisibilityChanged: (Boolean) -> Unit = { isVisible ->
                    isBottomBarVisible = isVisible
                }
                Scaffold(bottomBar = {
                    if (isBottomBarVisible) {
                        BottomBar(navController = navController, state = true)
                    }
                }) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NavigationGraph(
                            navController = navController,
                            onBottomBarVisibilityChanged = onBottomBarVisibilityChanged
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                    if (account.email != currentUserEmail) {
                        firebaseAuthWithGoogle(idToken) { userExists ->
                            if (userExists) {
                                navController.navigate("waitscreenroute") {
                                    popUpTo("waitscreenroute") { inclusive = true }
                                }
                            } else {
                                navController.navigate("chooseCity") {
                                    popUpTo("chooseCity") { inclusive = true }
                                }
                            }
                        }
                    } else {
                        navController.navigate("waitscreenroute") {
                            popUpTo("waitscreenroute") { inclusive = true }
                        }
                    }
                }
            } catch (e: ApiException) {
                println("Google Sign-In failed: ${e.message}")
            }
        }
    }
    
    private fun firebaseAuthWithGoogle(
        idToken: String,
        onComplete: (Boolean) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val uid = currentUser.uid
                    val email = currentUser.email ?: ""

                    firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                onComplete(true) // User exists
                            } else {
                                val newUser = hashMapOf(
                                    "name" to (currentUser.displayName ?: "Unknown"),
                                    "email" to email,
                                    "chosenCity" to "",
                                    "interests" to emptyList<String>() // Empty interests array
                                )
                                firestore.collection("users").document(uid).set(newUser)
                                    .addOnSuccessListener {
                                        println("New user created successfully!")
                                        onComplete(false) // New user created
                                    }
                                    .addOnFailureListener {
                                        println("Failed to create user: ${it.message}")
                                    }
                            }
                        }
                        .addOnFailureListener {
                            println("Error fetching user: ${it.message}")
                        }
                }
            } else {
                println("Google Sign-In failed: ${task.exception?.message}")
            }
        }
    }
}
