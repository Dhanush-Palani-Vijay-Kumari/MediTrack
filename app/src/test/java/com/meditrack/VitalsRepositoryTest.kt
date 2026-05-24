package com.meditrack

import com.meditrack.data.local.dao.VitalsDao
import com.meditrack.data.local.entity.VitalsEntity
import com.meditrack.data.repository.VitalStatus
import com.meditrack.data.repository.VitalsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class VitalsRepositoryTest {

    private lateinit var dao: VitalsDao
    private lateinit var repository: VitalsRepository

    @Before
    fun setup() {
        dao = mock()
        repository = VitalsRepository(dao)
    }

    // ── BP Status ─────────────────────────────────────────────────────────────

    @Test
    fun `getBpStatus normal range`() {
        assertEquals(VitalStatus.NORMAL, repository.getBpStatus(115, 75))
    }

    @Test
    fun `getBpStatus elevated`() {
        assertEquals(VitalStatus.ELEVATED, repository.getBpStatus(125, 75))
    }

    @Test
    fun `getBpStatus watch`() {
        assertEquals(VitalStatus.WATCH, repository.getBpStatus(135, 85))
    }

    @Test
    fun `getBpStatus high`() {
        assertEquals(VitalStatus.HIGH, repository.getBpStatus(145, 95))
    }

    // ── Heart Rate Status ─────────────────────────────────────────────────────

    @Test
    fun `getHeartRateStatus normal`() {
        assertEquals(VitalStatus.NORMAL, repository.getHeartRateStatus(72))
    }

    @Test
    fun `getHeartRateStatus watch low`() {
        assertEquals(VitalStatus.WATCH, repository.getHeartRateStatus(55))
    }

    @Test
    fun `getHeartRateStatus high`() {
        assertEquals(VitalStatus.HIGH, repository.getHeartRateStatus(120))
    }

    // ── Blood Sugar Status ────────────────────────────────────────────────────

    @Test
    fun `getBloodSugarStatus normal`() {
        assertEquals(VitalStatus.NORMAL, repository.getBloodSugarStatus(90f))
    }

    @Test
    fun `getBloodSugarStatus watch`() {
        assertEquals(VitalStatus.WATCH, repository.getBloodSugarStatus(110f))
    }

    @Test
    fun `getBloodSugarStatus high`() {
        assertEquals(VitalStatus.HIGH, repository.getBloodSugarStatus(140f))
    }

    // ── Insert ────────────────────────────────────────────────────────────────

    @Test
    fun `insertVitals populates timestamp and dateKey`() = runTest {
        whenever(dao.insertVitals(any())).thenAnswer { invocation ->
            val entity = invocation.getArgument<VitalsEntity>(0)
            assert(entity.timestamp > 0) { "timestamp not set" }
            assert(entity.dateKey.isNotBlank()) { "dateKey not set" }
            1L
        }
        repository.insertVitals(
            VitalsEntity(
                timestamp = 0L, dateKey = "",
                systolic = 120, diastolic = 80,
                heartRate = 72, bloodSugar = 100f,
                weight = null, oxygenSaturation = null
            )
        )
        verify(dao).insertVitals(any())
    }
}
