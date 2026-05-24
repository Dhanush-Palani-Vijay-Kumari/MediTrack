package com.meditrack.ui.appointments

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
import com.meditrack.data.local.entity.AppointmentEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    navController: NavController,
    viewModel: AppointmentsViewModel = hiltViewModel()
) {
    var doctorName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("") }
    var timeStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val nameError = doctorName.isBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add appointment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.Close, "Close")
                    }
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
            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text("Doctor / clinic name *") },
                isError = nameError && doctorName.isNotEmpty(),
                leadingIcon = { Icon(Icons.Outlined.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = specialty,
                onValueChange = { specialty = it },
                label = { Text("Specialty (e.g. Cardiology)") },
                leadingIcon = { Icon(Icons.Outlined.LocalHospital, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location / address") },
                leadingIcon = { Icon(Icons.Outlined.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Date") },
                    placeholder = { Text("2026-05-30") },
                    leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = timeStr,
                    onValueChange = { timeStr = it },
                    label = { Text("Time") },
                    placeholder = { Text("10:30") },
                    leadingIcon = { Icon(Icons.Outlined.AccessTime, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (!nameError) {
                        val epochMillis = runCatching {
                            val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
                            LocalDateTime.of(date, time)
                                .atZone(ZoneId.systemDefault())
                                .toInstant().toEpochMilli()
                        }.getOrElse {
                            // Default to 1 week from now if parse fails
                            System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
                        }
                        viewModel.addAppointment(
                            AppointmentEntity(
                                doctorName = doctorName.trim(),
                                specialty = specialty.trim(),
                                location = location.trim(),
                                dateTime = epochMillis,
                                notes = notes.trim()
                            )
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save appointment")
            }
        }
    }
}
