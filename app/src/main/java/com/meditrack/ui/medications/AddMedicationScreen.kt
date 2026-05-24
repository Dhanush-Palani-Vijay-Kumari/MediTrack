package com.meditrack.ui.medications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.meditrack.data.local.entity.MedicationEntity
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    navController: NavController,
    viewModel: MedicationsViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("Tablet") }
    var condition by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("ONCE_DAILY") }
    var morningTime by remember { mutableStateOf("08:00") }
    var afternoonTime by remember { mutableStateOf("") }
    var eveningTime by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("With meal") }
    var totalPills by remember { mutableStateOf("30") }
    var remindersEnabled by remember { mutableStateOf(true) }
    var selectedColor by remember { mutableStateOf("BLUE") }

    val formOptions = listOf("Tablet", "Capsule", "Liquid", "Injection", "Patch")
    val frequencyOptions = listOf(
        "ONCE_DAILY" to "Once daily",
        "TWICE_DAILY" to "Twice daily",
        "THREE_TIMES" to "Three times daily",
        "WEEKLY" to "Weekly"
    )
    val colorOptions = listOf("BLUE", "GREEN", "AMBER", "PURPLE", "CORAL")

    var formExpanded by remember { mutableStateOf(false) }
    var freqExpanded by remember { mutableStateOf(false) }

    val nameError = name.isBlank()
    val dosageError = dosage.isBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add medication") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.Close, "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (!nameError && !dosageError) {
                                val pills = totalPills.toIntOrNull() ?: 30
                                val med = MedicationEntity(
                                    name = name.trim(),
                                    dosage = dosage.trim(),
                                    form = form,
                                    condition = condition.trim(),
                                    frequencyType = frequency,
                                    morningTime = morningTime.ifBlank { null },
                                    afternoonTime = afternoonTime.ifBlank { null },
                                    eveningTime = eveningTime.ifBlank { null },
                                    instructions = instructions.trim(),
                                    startDate = LocalDate.now()
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant().toEpochMilli(),
                                    totalPills = pills,
                                    pillsRemaining = pills,
                                    isActive = true,
                                    remindersEnabled = remindersEnabled,
                                    color = selectedColor
                                )
                                viewModel.addMedication(med)
                                navController.popBackStack()
                            }
                        }
                    ) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medication name *") },
                isError = nameError && name.isNotEmpty(),
                leadingIcon = { Icon(Icons.Outlined.Medication, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Dosage
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage (e.g. 500mg) *") },
                isError = dosageError && dosage.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Form dropdown
            ExposedDropdownMenuBox(expanded = formExpanded, onExpandedChange = { formExpanded = it }) {
                OutlinedTextField(
                    value = form,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Form") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(formExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(expanded = formExpanded, onDismissRequest = { formExpanded = false }) {
                    formOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { form = option; formExpanded = false }
                        )
                    }
                }
            }

            // Condition
            OutlinedTextField(
                value = condition,
                onValueChange = { condition = it },
                label = { Text("Treats condition (e.g. Type 2 Diabetes)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Frequency dropdown
            ExposedDropdownMenuBox(expanded = freqExpanded, onExpandedChange = { freqExpanded = it }) {
                OutlinedTextField(
                    value = frequencyOptions.first { it.first == frequency }.second,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency") },
                    leadingIcon = { Icon(Icons.Outlined.Schedule, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(freqExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(expanded = freqExpanded, onDismissRequest = { freqExpanded = false }) {
                    frequencyOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                frequency = key
                                freqExpanded = false
                                // Auto-set times based on frequency
                                when (key) {
                                    "ONCE_DAILY"   -> { morningTime = "08:00"; afternoonTime = ""; eveningTime = "" }
                                    "TWICE_DAILY"  -> { morningTime = "08:00"; afternoonTime = ""; eveningTime = "20:00" }
                                    "THREE_TIMES"  -> { morningTime = "08:00"; afternoonTime = "14:00"; eveningTime = "20:00" }
                                    "WEEKLY"       -> { morningTime = "09:00"; afternoonTime = ""; eveningTime = "" }
                                }
                            }
                        )
                    }
                }
            }

            // Times
            SectionLabel("Reminder times")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = morningTime,
                    onValueChange = { morningTime = it },
                    label = { Text("Morning") },
                    placeholder = { Text("08:00") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = afternoonTime,
                    onValueChange = { afternoonTime = it },
                    label = { Text("Afternoon") },
                    placeholder = { Text("14:00") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = eveningTime,
                    onValueChange = { eveningTime = it },
                    label = { Text("Evening") },
                    placeholder = { Text("20:00") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Instructions
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                placeholder = { Text("e.g. With breakfast") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Total pills
            OutlinedTextField(
                value = totalPills,
                onValueChange = { totalPills = it },
                label = { Text("Total pills / doses") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Outlined.Numbers, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Color picker
            SectionLabel("Color label")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colorOptions.forEach { c ->
                    val (bg, _) = colorForMed(c)
                    FilterChip(
                        selected = selectedColor == c,
                        onClick = { selectedColor = c },
                        label = { Text(c.take(1)) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = bg,
                            selectedContainerColor = bg
                        )
                    )
                }
            }

            // Reminders toggle
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Enable reminders", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "WorkManager will notify you before each dose",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = remindersEnabled, onCheckedChange = { remindersEnabled = it })
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!nameError && !dosageError) {
                        val pills = totalPills.toIntOrNull() ?: 30
                        val med = MedicationEntity(
                            name = name.trim(),
                            dosage = dosage.trim(),
                            form = form,
                            condition = condition.trim(),
                            frequencyType = frequency,
                            morningTime = morningTime.ifBlank { null },
                            afternoonTime = afternoonTime.ifBlank { null },
                            eveningTime = eveningTime.ifBlank { null },
                            instructions = instructions.trim(),
                            startDate = LocalDate.now()
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant().toEpochMilli(),
                            totalPills = pills,
                            pillsRemaining = pills,
                            isActive = true,
                            remindersEnabled = remindersEnabled,
                            color = selectedColor
                        )
                        viewModel.addMedication(med)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save medication")
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
