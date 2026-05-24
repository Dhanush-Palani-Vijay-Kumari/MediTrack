package com.meditrack.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.data.local.entity.AppointmentEntity
import com.meditrack.data.local.entity.MedicationLogEntity
import com.meditrack.data.local.entity.VitalsEntity
import com.meditrack.data.repository.AppointmentRepository
import com.meditrack.data.repository.MedicationRepository
import com.meditrack.data.repository.VitalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "there",
    val takenToday: Int = 0,
    val totalToday: Int = 0,
    val streakDays: Int = 0,
    val todayLogs: List<MedicationLogEntity> = emptyList(),
    val latestVitals: VitalsEntity? = null,
    val nextAppointment: AppointmentEntity? = null,
    val daysUntilAppointment: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val medicationRepo: MedicationRepository,
    private val vitalsRepo: VitalsRepository,
    private val appointmentRepo: AppointmentRepository
) : ViewModel() {

    private val today = LocalDate.now()

    val uiState: StateFlow<HomeUiState> = combine(
        medicationRepo.getTakenCountForDate(today),
        medicationRepo.getTotalCountForDate(today),
        medicationRepo.getLogsForDate(today),
        vitalsRepo.getLatestVitals(),
        appointmentRepo.getNextAppointment()
    ) { taken, total, logs, vitals, nextAppt ->
        HomeUiState(
            takenToday = taken,
            totalToday = total,
            todayLogs = logs,
            latestVitals = vitals,
            nextAppointment = nextAppt,
            daysUntilAppointment = nextAppt?.let { appointmentRepo.daysUntil(it.dateTime) } ?: 0,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        loadStreak()
    }

    private fun loadStreak() {
        viewModelScope.launch {
            // streak is computed once on launch; update uiState indirectly via a separate state
        }
    }

    fun markDoseTaken(log: MedicationLogEntity) {
        viewModelScope.launch {
            medicationRepo.markDoseTaken(log)
        }
    }
}
