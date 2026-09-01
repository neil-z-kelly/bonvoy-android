package com.marriott.bonvoy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marriott.bonvoy.R

@Composable
fun BonvoyWordmark(modifier: Modifier = Modifier, onDark: Boolean = false) {
    val color = if (onDark) BonvoyColors.White else BonvoyColors.Ink
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(R.drawable.marriott_wordmark),
            contentDescription = "Marriott",
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier.width(150.dp),
        )
        Text(
            text = "BONVOY",
            color = color,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Light,
            fontSize = 22.sp,
            letterSpacing = 7.sp,
        )
    }
}

@Composable
fun SignInScreen(onSignIn: () -> Unit) {
    var email by remember { mutableStateOf("neil.kelly@example.com") }
    var password by remember { mutableStateOf("••••••••") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BonvoyColors.Ink),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 56.dp, bottom = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            BonvoyWordmark(onDark = true)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                )
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Sign in", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Earn and redeem points across 30+ extraordinary brands.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email or member number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(),
            )
            Button(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BonvoyColors.Ink),
            ) {
                Text("SIGN IN", style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = {}, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Forgot password?", color = BonvoyColors.Ink)
            }
            HorizontalDivider(color = BonvoyColors.Stone)
            TextButton(onClick = onSignIn, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Join Marriott Bonvoy", color = BonvoyColors.Gold, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BonvoyColors.Ink,
    focusedLabelColor = BonvoyColors.Ink,
    cursorColor = BonvoyColors.Ink,
)
