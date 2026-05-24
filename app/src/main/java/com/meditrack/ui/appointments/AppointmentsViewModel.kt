package com.meditrack.ui.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditrack.data.local.entity.AppointmentEntity
import com.meditrack.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val repository: AppointmentRepository
) : ViewModel() {

    val upcoming: StateFlow<List<AppointmentEntity>> =
        repository.getUpcomingAppointments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val all: StateFlow<List<AppointmentEntity>> =
        repository.getAllAppointments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAppointment(appt: AppointmentEntity) {
        viewModelScope.launch { repository.insertAppointment(appt) }
    }

    fun deleteAppointment(appt: AppointmentEntity) {
        viewModelScope.launch { repository.deleteAppointment(appt) }
    }

    fun completeAppointment(appt: AppointmentEntity) {
        viewModelScope.launch {
            repository.updateAppointment(appt.copy(status = "COMPLETED"))
        }
    }

    fun daysUntil(dateTimeMillis: Long) = repository.daysUntil(dateTimeMillis)
}
