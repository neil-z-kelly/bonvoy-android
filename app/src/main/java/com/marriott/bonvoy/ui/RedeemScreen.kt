package com.marriott.bonvoy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marriott.bonvoy.data.BackendException
import com.marriott.bonvoy.data.Hotel

@Composable
fun RedeemScreen(
    hotel: Hotel,
    onBack: () -> Unit,
    viewModel: RedeemViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var nights by rememberSaveable { mutableIntStateOf(2) }
    val totalPoints = hotel.pointsPerNight * nights

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(BonvoyColors.Ink)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewModel.reset(); onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = BonvoyColors.White)
            }
            Text("Redeem points", style = MaterialTheme.typography.titleLarge, color = BonvoyColors.White)
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = BonvoyColors.White), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    BrandBadge(hotel.brand)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(hotel.name, style = MaterialTheme.typography.titleMedium)
                        Text(hotel.city, color = BonvoyColors.Grey, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = BonvoyColors.White), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Nights", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { if (nights > 1) nights-- }, shape = RoundedCornerShape(4.dp)) { Text("−", color = BonvoyColors.Ink) }
                        Text("  $nights  ", style = MaterialTheme.typography.titleLarge)
                        OutlinedButton(onClick = { if (nights < 7) nights++ }, shape = RoundedCornerShape(4.dp)) { Text("+", color = BonvoyColors.Ink) }
                    }
                    HorizontalDivider(color = BonvoyColors.Stone)
                    Row {
                        Text("${hotel.pointsPerNight.formatPoints()} pts × $nights", modifier = Modifier.weight(1f), color = BonvoyColors.Grey)
                        Text("${totalPoints.formatPoints()} pts", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            when (val s = state) {
                RedeemState.Idle -> {
                    Button(
                        onClick = { viewModel.redeem(hotel, nights) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BonvoyColors.Ink),
                    ) { Text("REDEEM ${totalPoints.formatPoints()} POINTS", style = MaterialTheme.typography.labelLarge) }
                }
                is RedeemState.Failure -> {
                    RedemptionUnavailableCard(
                        error = s.error,
                        onRetry = { viewModel.redeem(hotel, nights) },
                        onBackToSearch = { viewModel.reset(); onBack() },
                    )
                }
                RedeemState.Loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BonvoyColors.Ink)
                }
                is RedeemState.Success -> Card(
                    colors = CardDefaults.cardColors(containerColor = BonvoyColors.White),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, null, tint = BonvoyColors.Gold, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("You're booked!", style = MaterialTheme.typography.headlineMedium)
                        Text("Confirmation ${s.result.confirmation}", color = BonvoyColors.Grey)
                        Text("New balance: ${s.result.newBalance.formatPoints()} pts")
                    }
                }
            }
        }
    }
}

@Composable
private fun RedemptionUnavailableCard(
    error: Throwable,
    onRetry: () -> Unit,
    onBackToSearch: () -> Unit,
) {
    val body = if (error is BackendException) {
        "We couldn't complete your redemption right now. Your points haven't been touched — please try again in a few minutes."
    } else {
        "We couldn't reach Marriott Bonvoy. Check your connection and try again."
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BonvoyColors.White),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = BonvoyColors.Gold,
                modifier = Modifier.size(40.dp),
            )
            Text("Points redemption is temporarily unavailable", style = MaterialTheme.typography.headlineMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = BonvoyColors.Grey)
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BonvoyColors.Ink),
            ) {
                Text("RETRY", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onBackToSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(4.dp),
            ) {
                Text("BACK TO HOTEL SEARCH", style = MaterialTheme.typography.labelLarge)
            }
            HorizontalDivider(color = BonvoyColors.Stone)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Support details", style = MaterialTheme.typography.labelSmall, color = BonvoyColors.Grey)
                if (error is BackendException) {
                    Text(
                        "Error: ${error.errorName ?: "HTTP ${error.statusCode}"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BonvoyColors.Grey,
                        fontSize = 12.sp,
                    )
                    error.errorMessage?.let {
                        Text(
                            "Message: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BonvoyColors.Grey,
                            fontSize = 12.sp,
                        )
                    }
                    error.requestId?.let {
                        Text(
                            "Request ID: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BonvoyColors.Grey,
                            fontSize = 12.sp,
                        )
                    }
                } else {
                    Text(
                        "Error: ${error::class.java.simpleName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BonvoyColors.Grey,
                        fontSize = 12.sp,
                    )
                    error.message?.let {
                        Text(
                            "Message: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BonvoyColors.Grey,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}
