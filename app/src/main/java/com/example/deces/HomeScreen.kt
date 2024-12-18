package com.example.deces

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlin.system.exitProcess

private const val RC_SIGN_IN = 9001

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    BackHandler {
        exitProcess(0)
    }

    // Configure Google Sign-In
    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id)) // Add Web Client ID from Firebase
                .requestEmail()
                .build() // No need for setAccountName(null)
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF291B11))
    ) {
        Column(
            modifier = Modifier

                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(350.dp)
            )

            Text(
                text = "Dobrodošli!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Odaberite način prijave", fontSize = 16.sp, color = Color(0xFFB3A9A1)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = { navController.navigate("register") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58845)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp)
            ) {
                Text("Kreiraj novi račun", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            Button(
                onClick = { navController.navigate("login") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58845)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp)
            ) {
                Text("Prijavi se", fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Login Button
            Button(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    // Launch the account picker
                    (context as? Activity)?.startActivityForResult(signInIntent, RC_SIGN_IN)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF58845)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp)
            ) {
                Text("Prijava putem Google računa", fontSize = 14.sp, color = Color.White)
            }

        }
    }
}
