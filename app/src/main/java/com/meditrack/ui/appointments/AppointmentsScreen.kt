package com.meditrack.ui.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.meditrack.data.local.entity.AppointmentEntity
import com.meditrack.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    navController: NavController,
    viewModel: AppointmentsViewModel = hiltViewModel()
) {
    val upcoming by viewModel.upcoming.collectAsStateWithLifecycle()
    val all by viewModel.all.collectAsStateWithLifecycle()
    var showAll by remember { mutableStateOf(false) }
    val displayList = if (showAll) all else upcoming

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Appointments") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_appointment") }) {
                Icon(Icons.Outlined.Add, "Add appointment")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showAll,
                        onClick = { showAll = false },
                        label = { Text("Upcoming") }
                    )
                    FilterChip(
                        selected = showAll,
                        onClick = { showAll = true },
                        label = { Text("All") }
                    )
                }
            }

            if (displayList.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.CalendarMonth, null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            Text("No appointments scheduled",
                                style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(6.dp))
                            Text("Tap + to add your next appointment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(displayList) { appt ->
                    AppointmentCard(
                        appt = appt,
                        daysUntil = viewModel.daysUntil(appt.dateTime),
                        onComplete = { viewModel.completeAppointment(appt) },
                        onDelete = { viewModel.deleteAppointment(appt) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appt: AppointmentEntity,
    daysUntil: Int,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val dtFmt = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy · h:mm a")
    val dateStr = Instant.ofEpochMilli(appt.dateTime)
        .atZone(ZoneId.systemDefault())
        .format(dtFmt)

    val isCompleted = appt.status == "COMPLETED"
    val isSoon = daysUntil <= 1 && !isCompleted

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant
                             else MaterialTheme.colorScheme.surface
        ),
        border = if (isSoon) androidx.compose.foundation.BorderStroke(1.5.dp, Blue600) else null
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Purple50),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LocalHospital, null, tint = Purple600,
                    modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(appt.doctorName, style = MaterialTheme.typography.titleSmall)
                    if (isCompleted) {
                        Box(
                            Modifier.clip(RoundedCornerShape(4.dp)).background(Green50)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Done", style = MaterialTheme.typography.labelSmall, color = Green600)
                        }
                    }
                }
                Text(appt.specialty, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(dateStr, style = MaterialTheme.typography.bodySmall,
                    color = if (isSoon) Blue600 else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSoon) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.padding(top = 2.dp))
                if (appt.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Outlined.LocationOn, null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(appt.location, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (!isCompleted) {
                    Text(
                        when (daysUntil) {
                            0 -> "Today"
                            1 -> "Tomorrow"
                            else -> "In $daysUntil days"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Blue600,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!isCompleted) {
                    IconButton(onClick = onComplete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.CheckCircle, "Mark done",
                            tint = Teal400, modifier = Modifier.size(22.dp))
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
