package com.meditrack.ui.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.data.local.dao.AdherenceRow
import com.meditrack.data.local.entity.MedicationEntity
import com.meditrack.data.local.entity.MedicationLogEntity
import com.meditrack.data.repository.MedicationRepository
import com.meditrack.util.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    val medications: StateFlow<List<MedicationEntity>> =
        repository.getActiveMedications()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getMedicationById(id: Long): Flow<MedicationEntity?> = repository.getMedicationById(id)

    fun getLogsForMedication(id: Long): Flow<List<MedicationLogEntity>> =
        repository.getLogsForMedication(id)

    fun getAdherence(id: Long): Flow<List<AdherenceRow>> =
        repository.getAdherenceForMedication(id, LocalDate.now().minusDays(6))

    fun addMedication(med: MedicationEntity) {
        viewModelScope.launch {
            val id = repository.insertMedication(med)
            scheduler.scheduleMedicationReminders(med.copy(id = id))
        }
    }

    fun updateMedication(med: MedicationEntity) {
        viewModelScope.launch {
            repository.updateMedication(med)
            scheduler.cancelMedicationReminders(med.id)
            if (med.remindersEnabled) scheduler.scheduleMedicationReminders(med)
        }
    }

    fun deleteMedication(med: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(med)
            scheduler.cancelMedicationReminders(med.id)
        }
    }

    fun toggleReminder(med: MedicationEntity) {
        updateMedication(med.copy(remindersEnabled = !med.remindersEnabled))
    }
}
