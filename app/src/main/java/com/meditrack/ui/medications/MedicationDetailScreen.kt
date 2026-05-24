package com.meditrack.ui.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.meditrack.data.local.dao.AdherenceRow
import com.meditrack.data.local.entity.MedicationEntity
import com.meditrack.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailScreen(
    medId: Long,
    navController: NavController,
    viewModel: MedicationsViewModel = hiltViewModel()
) {
    val medication by viewModel.getMedicationById(medId)
        .collectAsStateWithLifecycle(initialValue = null)
    val adherence by viewModel.getAdherence(medId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    medication?.let { med ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Medication detail") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Outlined.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* edit */ }) {
                            Icon(Icons.Outlined.Edit, "Edit")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { MedHeroCard(med) }
                item { MetaRow(med) }
                item { ScheduleCard(med) }
                item { ReminderCard(med, onToggle = { viewModel.toggleReminder(med) }) }
                item { AdherenceCard(adherence) }
            }
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MedHeroCard(med: MedicationEntity) {
    val (_, iconTint) = colorForMed(med.color)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blue600)
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Medication, null, tint = White, modifier = Modifier.size(30.dp))
            }
            Column {
                Text(med.name, style = MaterialTheme.typography.headlineSmall, color = White)
                Text(
                    "${med.dosage} · ${med.form}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.75f)
                )
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(med.condition)
                    Chip(if (med.isActive) "Active" else "Inactive")
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(White.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = White)
    }
}

@Composable
private fun MetaRow(med: MedicationEntity) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetaCard(Icons.Outlined.AccessTime, "Frequency",
            med.frequencyType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            Purple50, Purple600, Modifier.weight(1f))
        MetaCard(Icons.Outlined.CalendarMonth, "Started",
            formatDate(med.startDate), Blue50, Blue600, Modifier.weight(1f))
        MetaCard(Icons.Outlined.Refresh, "Refill",
            "${med.pillsRemaining} left",
            if (med.pillsRemaining <= 10) Red50 else Green50,
            if (med.pillsRemaining <= 10) Red600 else Green600,
            Modifier.weight(1f))
    }
}

@Composable
private fun MetaCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, value: String,
    bg: Color, tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp))
            Text(value, style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun ScheduleCard(med: MedicationEntity) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Schedule", style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 12.dp))
            med.morningTime?.let {
                ScheduleRow(Icons.Outlined.WbSunny, "Morning dose", "$it · ${med.instructions}",
                    Green50, Green600, true)
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
            }
            med.afternoonTime?.let {
                ScheduleRow(Icons.Outlined.WbCloudy, "Afternoon dose", "$it · ${med.instructions}",
                    Blue50, Blue600, false)
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
            }
            med.eveningTime?.let {
                ScheduleRow(Icons.Outlined.Nightlight, "Evening dose", "$it · ${med.instructions}",
                    Purple50, Purple600, false)
            }
        }
    }
}

@Composable
private fun ScheduleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String, subtitle: String,
    bg: Color, tint: Color, isTaken: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isTaken) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Teal400),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.Check, null, tint = White, modifier = Modifier.size(14.dp)) }
        } else {
            Text("Pending", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReminderCard(med: MedicationEntity, onToggle: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Reminders", style = MaterialTheme.typography.titleSmall)
                Switch(checked = med.remindersEnabled, onCheckedChange = { onToggle() })
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Notifications, null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Push notification 15 min before",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Alarm, null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("WorkManager fires even when app is closed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AdherenceCard(adherence: List<AdherenceRow>) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")

    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Adherence this week", style = MaterialTheme.typography.titleSmall)
                val pct = if (adherence.isNotEmpty()) {
                    val total = adherence.sumOf { it.total }
                    val taken = adherence.sumOf { it.taken }
                    if (total > 0) (taken * 100 / total) else 0
                } else 0
                Text("$pct%", style = MaterialTheme.typography.titleSmall, color = Blue600,
                    fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val paddedAdherence = (adherence + List(7) { null }).take(7)
                days.forEachIndexed { idx, day ->
                    val row = paddedAdherence.getOrNull(idx)
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        val barColor = when {
                            row == null -> MaterialTheme.colorScheme.surfaceVariant
                            row.taken == row.total && row.total > 0 -> Teal400
                            row.taken > 0 -> Amber200
                            else -> Red400
                        }
                        val fraction = if (row != null && row.total > 0)
                            row.taken.toFloat() / row.total else 0f

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((48 * fraction.coerceAtLeast(0.15f)).dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                        Text(day, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
}
