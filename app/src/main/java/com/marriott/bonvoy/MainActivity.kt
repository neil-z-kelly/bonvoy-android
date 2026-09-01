package com.marriott.bonvoy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.marriott.bonvoy.ui.BonvoyTheme
import com.marriott.bonvoy.ui.HomeScreen
import com.marriott.bonvoy.ui.SignInScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BonvoyTheme { BonvoyApp() }
        }
    }
}

@Composable
private fun BonvoyApp() {
    var signedIn by rememberSaveable { mutableStateOf(false) }
    if (signedIn) {
        HomeScreen(onSignOut = { signedIn = false })
    } else {
        SignInScreen(onSignIn = { signedIn = true })
    }
}
