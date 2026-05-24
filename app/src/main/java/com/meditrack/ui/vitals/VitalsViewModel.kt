package com.meditrack.ui.vitals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.data.local.entity.VitalsEntity
import com.meditrack.data.repository.VitalStatus
import com.meditrack.data.repository.VitalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VitalsUiState(
    val latestVitals: VitalsEntity? = null,
    val recentVitals: List<VitalsEntity> = emptyList(),
    val selectedRange: Int = 7,   // days
    val isLoading: Boolean = true,
    val showLogSheet: Boolean = false
)

@HiltViewModel
class VitalsViewModel @Inject constructor(
    private val repository: VitalsRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(7)

    val uiState: StateFlow<VitalsUiState> = combine(
        repository.getLatestVitals(),
        _selectedRange.flatMapLatest { days -> repository.getVitalsSince(days) },
        _selectedRange
    ) { latest, recent, range ->
        VitalsUiState(
            latestVitals = latest,
            recentVitals = recent,
            selectedRange = range,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VitalsUiState())

    fun setRange(days: Int) { _selectedRange.value = days }

    fun logVitals(
        systolic: Int?, diastolic: Int?,
        heartRate: Int?, bloodSugar: Float?,
        weight: Float?, oxygenSaturation: Int?,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertVitals(
                VitalsEntity(
                    timestamp = 0L,   // set by repo
                    dateKey = "",     // set by repo
                    systolic = systolic,
                    diastolic = diastolic,
                    heartRate = heartRate,
                    bloodSugar = bloodSugar,
                    weight = weight,
                    oxygenSaturation = oxygenSaturation,
                    notes = notes
                )
            )
        }
    }

    fun deleteVitals(vitals: VitalsEntity) {
        viewModelScope.launch { repository.deleteVitals(vitals) }
    }

    fun getBpStatus(s: Int, d: Int): VitalStatus = repository.getBpStatus(s, d)
    fun getHrStatus(bpm: Int): VitalStatus = repository.getHeartRateStatus(bpm)
    fun getBsStatus(bs: Float): VitalStatus = repository.getBloodSugarStatus(bs)
}
