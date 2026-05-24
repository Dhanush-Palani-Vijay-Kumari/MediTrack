package com.meditrack.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meditrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    var name by remember { mutableStateOf("Dhanush") }
    var age by remember { mutableStateOf("27") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var emergencyContact by remember { mutableStateOf("") }
    var editMode by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    TextButton(onClick = { editMode = !editMode }) {
                        Text(if (editMode) "Done" else "Edit")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar + name
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Blue600),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = White,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(10.dp))
                if (editMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                } else {
                    Text(name, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Blood group: $bloodGroup",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Health info card
            SectionCard("Health info") {
                if (editMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = age, onValueChange = { age = it },
                            label = { Text("Age") }, modifier = Modifier.weight(1f), singleLine = true
                        )
                        OutlinedTextField(
                            value = bloodGroup, onValueChange = { bloodGroup = it },
                            label = { Text("Blood group") }, modifier = Modifier.weight(1f), singleLine = true
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = emergencyContact, onValueChange = { emergencyContact = it },
                        label = { Text("Emergency contact") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                } else {
                    ProfileRow(Icons.Outlined.Cake, "Age", "$age years")
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    ProfileRow(Icons.Outlined.Bloodtype, "Blood group", bloodGroup)
                    if (emergencyContact.isNotBlank()) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        ProfileRow(Icons.Outlined.Phone, "Emergency contact", emergencyContact)
                    }
                }
            }

            // Settings card
            SectionCard("Settings") {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Notifications, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Notifications", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.DarkMode, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Dark mode", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                }
            }

            // App info card
            SectionCard("About") {
                ProfileRow(Icons.Outlined.Info, "Version", "1.0.0")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                ProfileRow(Icons.Outlined.Security, "Privacy policy", "")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                ProfileRow(Icons.Outlined.BugReport, "Send feedback", "")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(14.dp), content = content)
        }
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (value.isNotBlank()) {
            Text(value, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Icon(Icons.Outlined.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp))
        }
    }
}
