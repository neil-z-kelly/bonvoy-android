package com.marriott.bonvoy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marriott.bonvoy.data.DemoData
import com.marriott.bonvoy.data.Hotel
import com.marriott.bonvoy.data.Stay
import java.text.NumberFormat
import java.util.Locale

private enum class Tab(val label: String) { Home("Home"), Search("Find & Reserve"), Account("Account") }

fun Int.formatPoints(): String = NumberFormat.getIntegerInstance(Locale.US).format(this)

@Composable
fun HomeScreen(onSignOut: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var redeemHotel by rememberSaveable { mutableStateOf<String?>(null) }

    val hotelToRedeem = redeemHotel?.let { name -> DemoData.hotels.firstOrNull { it.name == name } }
    if (hotelToRedeem != null) {
        RedeemScreen(
            hotel = hotelToRedeem,
            onBack = { redeemHotel = null },
            onBackToSearch = {
                redeemHotel = null
                tab = Tab.Search
            },
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = BonvoyColors.White) {
                Tab.entries.forEach { t ->
                    val selected = tab == t
                    NavigationBarItem(
                        selected = selected,
                        onClick = { tab = t },
                        label = { Text(t.label) },
                        icon = {
                            val icon = when (t) {
                                Tab.Home -> if (selected) Icons.Filled.Hotel else Icons.Outlined.Hotel
                                Tab.Search -> if (selected) Icons.Filled.Search else Icons.Outlined.Search
                                Tab.Account -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
                            }
                            Icon(icon, contentDescription = t.label)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BonvoyColors.White,
                            selectedTextColor = BonvoyColors.Ink,
                            indicatorColor = BonvoyColors.Ink,
                            unselectedIconColor = BonvoyColors.Grey,
                            unselectedTextColor = BonvoyColors.Grey,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                Tab.Home -> HomeTab(onRedeem = { tab = Tab.Search })
                Tab.Search -> SearchTab(onSelectHotel = { redeemHotel = it.name })
                Tab.Account -> AccountTab(onSignOut = onSignOut)
            }
        }
    }
}

@Composable
private fun HomeTab(onRedeem: () -> Unit) {
    val member = DemoData.member
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(BonvoyColors.Ink)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                BonvoyWordmark(onDark = true, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(24.dp))
                Text(
                    "Good evening, ${member.firstName}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BonvoyColors.White,
                )
                Text(
                    member.tier.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = BonvoyColors.Gold,
                )
                Spacer(Modifier.height(20.dp))
                PointsCard(points = member.points, onRedeem = onRedeem)
            }
        }
        item {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text("Your Elite status", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { member.nightsThisYear / (member.nightsThisYear + member.nightsToNextTier).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = BonvoyColors.Gold,
                    trackColor = BonvoyColors.Stone,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${member.nightsThisYear} nights this year · ${member.nightsToNextTier} to Ambassador Elite",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            SectionHeader("Upcoming stays")
        }
        items(DemoData.stays.filter { it.upcoming }) { StayRow(it) }
        item { SectionHeader("Past stays") }
        items(DemoData.stays.filter { !it.upcoming }) { StayRow(it) }
    }
}

@Composable
private fun PointsCard(points: Int, onRedeem: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BonvoyColors.White),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("POINTS BALANCE", style = MaterialTheme.typography.labelSmall, color = BonvoyColors.Grey)
            Spacer(Modifier.height(4.dp))
            Text(points.formatPoints(), style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRedeem,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BonvoyColors.Ink),
                    modifier = Modifier.weight(1f),
                ) { Text("REDEEM POINTS", style = MaterialTheme.typography.labelLarge) }
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f),
                ) { Text("ACTIVITY", style = MaterialTheme.typography.labelLarge, color = BonvoyColors.Ink) }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 8.dp),
    )
}

@Composable
private fun StayRow(stay: Stay) {
    Column(Modifier.padding(horizontal = 24.dp)) {
        Row(Modifier.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            BrandBadge(stay.brand)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stay.hotel, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${stay.city} · ${stay.dates}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Confirmation ${stay.confirmation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(color = BonvoyColors.Stone)
    }
}

@Composable
fun BrandBadge(brand: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(BonvoyColors.Ink, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            brand.split(" ").filter { it.first().isLetter() }.take(2).joinToString("") { it.first().uppercase() },
            color = BonvoyColors.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SearchTab(onSelectHotel: (Hotel) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = DemoData.hotels.filter {
        query.isBlank() || it.name.contains(query, true) || it.city.contains(query, true) || it.brand.contains(query, true)
    }
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(BonvoyColors.Ink)
                .statusBarsPadding()
                .padding(24.dp),
        ) {
            Text("Find & Reserve", style = MaterialTheme.typography.headlineMedium, color = BonvoyColors.White)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Where to next?") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BonvoyColors.White,
                    unfocusedContainerColor = BonvoyColors.White,
                    focusedBorderColor = BonvoyColors.White,
                    unfocusedBorderColor = BonvoyColors.White,
                ),
            )
        }
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)) {
            items(results) { hotel -> HotelRow(hotel, onClick = { onSelectHotel(hotel) }) }
        }
    }
}

@Composable
private fun HotelRow(hotel: Hotel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BonvoyColors.White),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            BrandBadge(hotel.brand)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(hotel.name, style = MaterialTheme.typography.titleMedium)
                Text(hotel.city, style = MaterialTheme.typography.bodyMedium, color = BonvoyColors.Grey)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = BonvoyColors.Gold, modifier = Modifier.size(14.dp))
                    Text(" ${hotel.rating}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${hotel.pointsPerNight.formatPoints()} pts", fontWeight = FontWeight.SemiBold)
                Text("or $${hotel.ratePerNight}/night", style = MaterialTheme.typography.bodyMedium, color = BonvoyColors.Grey)
            }
        }
    }
}

@Composable
private fun AccountTab(onSignOut: () -> Unit) {
    val m = DemoData.member
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Account", style = MaterialTheme.typography.headlineMedium)
        Card(colors = CardDefaults.cardColors(containerColor = BonvoyColors.White)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${m.firstName} ${m.lastName}", style = MaterialTheme.typography.titleLarge)
                Text("Member #${m.memberNumber}", color = BonvoyColors.Grey)
                Text(m.tier, color = BonvoyColors.Gold, fontWeight = FontWeight.SemiBold)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = BonvoyColors.White)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Backend", style = MaterialTheme.typography.titleMedium)
                Text(com.marriott.bonvoy.BuildConfig.BACKEND_BASE_URL, color = BonvoyColors.Grey)
            }
        }
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)) {
            Text("SIGN OUT", color = Color.Black, style = MaterialTheme.typography.labelLarge)
        }
    }
}
