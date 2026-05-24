package com.meditrack.ui.vitals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meditrack.data.local.entity.VitalsEntity
import com.meditrack.data.repository.VitalStatus
import com.meditrack.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsScreen(viewModel: VitalsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vitals tracker") },
                actions = {
                    Button(
                        onClick = { showLogSheet = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) { Text("+ Log") }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Range selector
            RangeSelector(state.selectedRange) { viewModel.setRange(it) }

            // BP Chart card
            state.latestVitals?.let { latest ->
                BpChartCard(latest, state.recentVitals, viewModel)
            } ?: EmptyVitalsCard { showLogSheet = true }

            // Mini vitals grid
            state.latestVitals?.let { latest ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    latest.heartRate?.let { hr ->
                        MiniVitalCard(
                            label = "Heart rate",
                            value = "$hr bpm",
                            status = viewModel.getHrStatus(hr),
                            sparkData = state.recentVitals.mapNotNull { it.heartRate?.toFloat() },
                            color = Teal400,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    latest.bloodSugar?.let { bs ->
                        MiniVitalCard(
                            label = "Blood sugar",
                            value = "${bs.toInt()} mg/dL",
                            status = viewModel.getBsStatus(bs),
                            sparkData = state.recentVitals.mapNotNull { it.bloodSugar },
                            color = Amber400,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Weight and SpO2
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    latest.weight?.let { w ->
                        SimpleMetricCard("Weight", "${w}kg", modifier = Modifier.weight(1f))
                    }
                    latest.oxygenSaturation?.let { spo2 ->
                        SimpleMetricCard("SpO₂", "$spo2%", modifier = Modifier.weight(1f))
                    }
                }
            }

            // History list
            if (state.recentVitals.isNotEmpty()) {
                SectionHeader("History")
                state.recentVitals.take(10).forEach { vitals ->
                    VitalsHistoryRow(vitals, onDelete = { viewModel.deleteVitals(vitals) })
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLogSheet) {
        LogVitalsSheet(
            onDismiss = { showLogSheet = false },
            onSave = { s, d, hr, bs, w, spo2, notes ->
                viewModel.logVitals(s, d, hr, bs, w, spo2, notes)
                showLogSheet = false
            }
        )
    }
}

@Composable
private fun RangeSelector(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(7 to "7 days", 30 to "30 days", 90 to "3 months").forEach { (days, label) ->
            FilterChip(
                selected = selected == days,
                onClick = { onSelect(days) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun BpChartCard(latest: VitalsEntity, history: List<VitalsEntity>, vm: VitalsViewModel) {
    val sys = latest.systolic
    val dia = latest.diastolic
    val status = if (sys != null && dia != null) vm.getBpStatus(sys, dia) else VitalStatus.NORMAL

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Blood pressure",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (sys != null && dia != null) {
                        Text(
                            "$sys",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "/ $dia mmHg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text("–", style = MaterialTheme.typography.headlineLarge)
                    }
                }
                StatusBadge(status)
            }

            Spacer(Modifier.height(12.dp))

            // Sparkline chart
            val bpData = history.mapNotNull { it.systolic?.toFloat() }
            if (bpData.size >= 2) {
                SparklineChart(
                    data = bpData,
                    color = Blue600,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            val trend = if (bpData.size >= 2) bpData.last() - bpData.first() else 0f
            Text(
                if (trend < 0) "↓ ${(-trend).toInt()} pts vs period start · trending well"
                else if (trend > 0) "↑ ${trend.toInt()} pts vs period start · monitor closely"
                else "Stable vs period start",
                style = MaterialTheme.typography.bodySmall,
                color = if (trend <= 0) Green600 else Red600,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SparklineChart(data: List<Float>, color: Color, modifier: Modifier = Modifier) {
    val fillColor = color.copy(alpha = 0.12f)
    androidx.compose.foundation.Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val min = data.min()
        val max = data.max()
        val range = (max - min).coerceAtLeast(1f)
        val w = size.width
        val h = size.height
        val step = w / (data.size - 1)

        val linePath = Path()
        val fillPath = Path()

        data.forEachIndexed { i, v ->
            val x = i * step
            val y = h - ((v - min) / range) * h * 0.8f - h * 0.1f
            if (i == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        val lastX = (data.size - 1) * step
        fillPath.lineTo(lastX, h)
        fillPath.close()

        drawPath(fillPath, brush = Brush.verticalGradient(
            listOf(fillColor, Color.Transparent)
        ))
        drawPath(linePath, color = color, style = Stroke(width = 4f, cap = StrokeCap.Round))

        // End dot
        val lastY = h - ((data.last() - min) / range) * h * 0.8f - h * 0.1f
        drawCircle(color = color, radius = 8f, center = androidx.compose.ui.geometry.Offset(lastX, lastY))
    }
}

@Composable
private fun MiniVitalCard(
    label: String, value: String, status: VitalStatus,
    sparkData: List<Float>, color: Color, modifier: Modifier = Modifier
) {
    Card(
        modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 4.dp))
            if (sparkData.size >= 2) {
                SparklineChart(data = sparkData, color = color,
                    modifier = Modifier.fillMaxWidth().height(36.dp))
            }
            Spacer(Modifier.height(4.dp))
            StatusBadge(status)
        }
    }
}

@Composable
private fun SimpleMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun StatusBadge(status: VitalStatus) {
    val (bg, textColor, label) = when (status) {
        VitalStatus.NORMAL   -> Triple(Green50, Green600, "Normal")
        VitalStatus.ELEVATED -> Triple(Amber50, Amber600, "Elevated")
        VitalStatus.WATCH    -> Triple(Amber50, Amber600, "Watch")
        VitalStatus.HIGH     -> Triple(Red50, Red600, "High")
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = textColor,
            fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
}

@Composable
private fun VitalsHistoryRow(vitals: VitalsEntity, onDelete: () -> Unit) {
    val timeFmt = DateTimeFormatter.ofPattern("d MMM · h:mm a")
    val time = Instant.ofEpochMilli(vitals.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(timeFmt)

    Card(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(time, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    vitals.systolic?.let { s ->
                        vitals.diastolic?.let { d -> MetricPill("BP $s/$d") }
                    }
                    vitals.heartRate?.let { MetricPill("HR $it") }
                    vitals.bloodSugar?.let { MetricPill("BS ${it.toInt()}") }
                    vitals.weight?.let { MetricPill("${it}kg") }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.DeleteOutline, "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MetricPill(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EmptyVitalsCard(onLog: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.MonitorHeart, null, modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("No vitals logged yet", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onLog) { Text("Log your first reading") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogVitalsSheet(
    onDismiss: () -> Unit,
    onSave: (Int?, Int?, Int?, Float?, Float?, Int?, String) -> Unit
) {
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var bloodSugar by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var spo2 by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Log vitals", style = MaterialTheme.typography.headlineSmall)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = systolic, onValueChange = { systolic = it },
                    label = { Text("Systolic\n(mmHg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), singleLine = true
                )
                OutlinedTextField(
                    value = diastolic, onValueChange = { diastolic = it },
                    label = { Text("Diastolic\n(mmHg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), singleLine = true
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = heartRate, onValueChange = { heartRate = it },
                    label = { Text("Heart rate\n(bpm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), singleLine = true
                )
                OutlinedTextField(
                    value = bloodSugar, onValueChange = { bloodSugar = it },
                    label = { Text("Blood sugar\n(mg/dL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), singleLine = true
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f), singleLine = true
                )
                OutlinedTextField(
                    value = spo2, onValueChange = { spo2 = it },
                    label = { Text("SpO₂ (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), singleLine = true
                )
            }
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Button(
                onClick = {
                    onSave(
                        systolic.toIntOrNull(), diastolic.toIntOrNull(),
                        heartRate.toIntOrNull(), bloodSugar.toFloatOrNull(),
                        weight.toFloatOrNull(), spo2.toIntOrNull(),
                        notes
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save reading") }
        }
    }
}
