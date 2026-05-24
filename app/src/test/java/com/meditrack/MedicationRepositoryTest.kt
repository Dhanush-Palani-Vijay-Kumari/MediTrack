package com.meditrack

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.meditrack.data.local.dao.MedicationDao
import com.meditrack.data.local.entity.MedicationEntity
import com.meditrack.data.repository.MedicationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationRepositoryTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var dao: MedicationDao
    private lateinit var repository: MedicationRepository

    private val sampleMed = MedicationEntity(
        id = 1L,
        name = "Metformin",
        dosage = "500mg",
        form = "Tablet",
        condition = "Type 2 Diabetes",
        frequencyType = "TWICE_DAILY",
        morningTime = "08:00",
        afternoonTime = null,
        eveningTime = "20:00",
        instructions = "With meal",
        startDate = System.currentTimeMillis(),
        totalPills = 60,
        pillsRemaining = 58,
        isActive = true,
        remindersEnabled = true,
        color = "BLUE"
    )

    @Before
    fun setup() {
        dao = mock()
        repository = MedicationRepository(dao)
    }

    @Test
    fun `getActiveMedications returns flow from dao`() = runTest {
        whenever(dao.getActiveMedications()).thenReturn(flowOf(listOf(sampleMed)))
        val flow = repository.getActiveMedications()
        flow.collect { meds ->
            assertEquals(1, meds.size)
            assertEquals("Metformin", meds[0].name)
        }
    }

    @Test
    fun `insertMedication calls dao and returns id`() = runTest {
        whenever(dao.insertMedication(any())).thenReturn(42L)
        val id = repository.insertMedication(sampleMed)
        assertEquals(42L, id)
        verify(dao).insertMedication(sampleMed)
    }

    @Test
    fun `markDoseTaken updates log and decrements pills`() = runTest {
        val log = com.meditrack.data.local.entity.MedicationLogEntity(
            id = 1L,
            medicationId = 1L,
            scheduledTime = System.currentTimeMillis(),
            takenTime = null,
            status = "PENDING",
            dateKey = "2026-05-23"
        )
        repository.markDoseTaken(log)
        verify(dao).updateLog(argThat { status == "TAKEN" && takenTime != null })
        verify(dao).decrementPills(1L)
    }

    @Test
    fun `calculateStreak returns 0 when no dates`() = runTest {
        whenever(dao.getDistinctTakenDates()).thenReturn(emptyList())
        val streak = repository.calculateStreak()
        assertEquals(0, streak)
    }

    @Test
    fun `deleteMedication calls dao`() = runTest {
        repository.deleteMedication(sampleMed)
        verify(dao).deleteMedication(sampleMed)
    }
}
