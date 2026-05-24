package com.meditrack

import com.meditrack.data.local.dao.AppointmentDao
import com.meditrack.data.local.entity.AppointmentEntity
import com.meditrack.data.repository.AppointmentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentRepositoryTest {

    private lateinit var dao: AppointmentDao
    private lateinit var repository: AppointmentRepository

    private val futureAppt = AppointmentEntity(
        id = 1L,
        doctorName = "Dr. Mehra",
        specialty = "Cardiology",
        location = "City Hospital",
        dateTime = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L, // 3 days from now
        notes = "",
        status = "UPCOMING"
    )

    @Before
    fun setup() {
        dao = mock()
        repository = AppointmentRepository(dao)
    }

    @Test
    fun `insertAppointment delegates to dao`() = runTest {
        whenever(dao.insertAppointment(any())).thenReturn(1L)
        val id = repository.insertAppointment(futureAppt)
        assertEquals(1L, id)
        verify(dao).insertAppointment(futureAppt)
    }

    @Test
    fun `deleteAppointment delegates to dao`() = runTest {
        repository.deleteAppointment(futureAppt)
        verify(dao).deleteAppointment(futureAppt)
    }

    @Test
    fun `daysUntil returns correct positive value`() {
        val threeDaysMs = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L
        val days = repository.daysUntil(threeDaysMs)
        assertTrue("Expected ~3 days but got $days", days in 2..3)
    }

    @Test
    fun `daysUntil returns 0 for past date`() {
        val past = System.currentTimeMillis() - 1000L
        assertEquals(0, repository.daysUntil(past))
    }

    @Test
    fun `getUpcomingAppointments filters correctly`() = runTest {
        whenever(dao.getUpcomingAppointments(any())).thenReturn(flowOf(listOf(futureAppt)))
        repository.getUpcomingAppointments().collect { list ->
            assertEquals(1, list.size)
            assertEquals("Dr. Mehra", list[0].doctorName)
        }
    }
}
