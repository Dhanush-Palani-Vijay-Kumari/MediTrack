package com.meditrack.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.meditrack.data.local.entity.AppointmentEntity
import com.meditrack.data.local.entity.MedicationLogEntity
import com.meditrack.data.local.entity.VitalsEntity
import com.meditrack.data.repository.VitalStatus
import com.meditrack.data.repository.VitalsRepository
import com.meditrack.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { HeroCard(state, navController) }
        item {
            Text(
                "Today's medications",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (state.todayLogs.isEmpty()) {
            item {
                EmptyMedsCard(navController)
            }
        } else {
            items(state.todayLogs) { log ->
                MedLogCard(
                    log = log,
                    onTake = { viewModel.markDoseTaken(log) },
                    onClick = { navController.navigate("medication_detail/${log.medicationId}") }
                )
            }
        }
        state.latestVitals?.let {
            item { LatestVitalsCard(it) }
        }
        state.nextAppointment?.let {
            item { NextAppointmentCard(it, state.daysUntilAppointment) }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun HeroCard(state: HomeUiState, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blue600)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        greeting(),
                        style = MaterialTheme.typography.bodySmall,
                        color = White.copy(alpha = 0.75f)
                    )
                    Text(
                        state.userName,
                        style = MaterialTheme.typography.titleLarge,
                        color = White,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.userName.take(2).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(White.copy(alpha = 0.15f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Today's meds", "${state.takenToday}/${state.totalToday}")
                Divider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(0.5.dp),
                    color = White.copy(alpha = 0.3f)
                )
                StatItem("Streak", "${state.streakDays}d 🔥")
                Divider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(0.5.dp),
                    color = White.copy(alpha = 0.3f)
                )
                StatItem("Next appt", "${state.daysUntilAppointment}d")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = White.copy(alpha = 0.8f))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = White,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun MedLogCard(
    log: MedicationLogEntity,
    onTake: () -> Unit,
    onClick: () -> Unit
) {
    val isTaken = log.status == "TAKEN"
    val isDue = log.status == "PENDING" && isPastDue(log.scheduledTime)

    val borderColor = when {
        isDue -> Blue600
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isDue) androidx.compose.foundation.BorderStroke(1.5.dp, Blue600) else null,
        onClick = onClick
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isTaken) Teal50 else Blue50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Medication,
                    contentDescription = null,
                    tint = if (isTaken) Teal600 else Blue600,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "Medication ${log.medicationId}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    formatTime(log.scheduledTime) + if (isDue) " · Due now" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDue) Blue600 else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isDue) FontWeight.Medium else FontWeight.Normal
                )
            }
            if (isTaken) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Teal400),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Check, null, tint = White, modifier = Modifier.size(16.dp))
                }
            } else if (isDue) {
                Button(
                    onClick = onTake,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text("Take", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun LatestVitalsCard(vitals: VitalsEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Latest vitals", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Today, ${formatTimestamp(vitals.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                vitals.systolic?.let { sys ->
                    vitals.diastolic?.let { dia ->
                        VitalChip("BP", "$sys/$dia", VitalStatus.NORMAL)
                    }
                }
                vitals.heartRate?.let { hr ->
                    VitalChip("Heart rate", "$hr bpm", VitalStatus.NORMAL)
                }
                vitals.bloodSugar?.let { bs ->
                    val status = if (bs < 100) VitalStatus.NORMAL else VitalStatus.WATCH
                    VitalChip("Blood sugar", "${bs.toInt()}", status)
                }
            }
        }
    }
}

@Composable
private fun VitalChip(label: String, value: String, status: VitalStatus) {
    val bg = when (status) {
        VitalStatus.NORMAL -> Green50
        VitalStatus.ELEVATED, VitalStatus.WATCH -> Amber50
        VitalStatus.HIGH -> Red50
    }
    val textColor = when (status) {
        VitalStatus.NORMAL -> Green600
        VitalStatus.ELEVATED, VitalStatus.WATCH -> Amber600
        VitalStatus.HIGH -> Red600
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(bg)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(status.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall, color = textColor)
        }
    }
}

@Composable
private fun NextAppointmentCard(appt: AppointmentEntity, daysUntil: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Purple50),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = Purple600, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("${appt.doctorName} – ${appt.specialty}", style = MaterialTheme.typography.titleSmall)
                Text(
                    formatAppointmentDate(appt.dateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyMedsCard(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Medication,
                null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "No medications scheduled today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { navController.navigate("add_medication") }) {
                Text("Add medication")
            }
        }
    }
}

private fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

private fun isPastDue(scheduledTime: Long): Boolean =
    scheduledTime <= System.currentTimeMillis()

private fun formatTime(epochMillis: Long): String {
    val time = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("h:mm a"))
}

private fun formatTimestamp(epochMillis: Long): String {
    val time = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("h:mm a"))
}

private fun formatAppointmentDate(epochMillis: Long): String {
    val dt = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return dt.format(DateTimeFormatter.ofPattern("EEEE, d MMM · h:mm a"))
}
