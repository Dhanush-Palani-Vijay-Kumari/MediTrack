package com.meditrack.ui.medications

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.meditrack.data.local.entity.MedicationEntity
import com.meditrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(
    navController: NavController,
    viewModel: MedicationsViewModel = hiltViewModel()
) {
    val medications by viewModel.medications.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Medications") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_medication") }) {
                Icon(Icons.Outlined.Add, "Add medication")
            }
        }
    ) { padding ->
        if (medications.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Medication, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("No medications yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("Tap + to add your first medication",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(medications) { med ->
                    MedicationCard(
                        medication = med,
                        onClick = { navController.navigate("medication_detail/${med.id}") },
                        onDelete = { viewModel.deleteMedication(med) }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationCard(
    medication: MedicationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }
    val (iconBg, iconTint) = colorForMed(medication.color)

    Card(
        onClick = onClick,
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
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Medication, null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(medication.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${medication.dosage} · ${medication.form}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    medication.frequencyType.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (medication.remindersEnabled) {
                    Icon(Icons.Outlined.Notifications, null,
                        tint = Blue600, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${medication.pillsRemaining} left",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (medication.pillsRemaining <= 10) Red600
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun colorForMed(color: String): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> =
    when (color) {
        "GREEN"  -> Green50 to Green600
        "AMBER"  -> Amber50 to Amber400
        "PURPLE" -> Purple50 to Purple600
        "CORAL"  -> androidx.compose.ui.graphics.Color(0xFFFAECE7) to androidx.compose.ui.graphics.Color(0xFF993C1D)
        else     -> Blue50 to Blue600
    }
