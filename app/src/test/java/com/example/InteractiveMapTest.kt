package com.example

import com.example.data.model.MechanicEntity
import com.example.ui.components.calculateMapDistanceKm
import com.example.ui.components.calculateMapEtaMinutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InteractiveMapTest {

    @Test
    fun testHaversineDistanceCalculation() {
        // Customer in Pekalongan (-6.8887, 109.6753)
        // Mechanic nearby (-6.8850, 109.6720)
        val dist = calculateMapDistanceKm(-6.8887, 109.6753, -6.8850, 109.6720)
        assertTrue("Distance should be greater than 0", dist > 0.0)
        assertTrue("Distance should be under 2.0 km", dist < 2.0)
    }

    @Test
    fun testEtaMinutesCalculation() {
        val etaShort = calculateMapEtaMinutes(1.0)
        assertTrue("ETA should be at least 4 minutes", etaShort >= 4)
        val etaLong = calculateMapEtaMinutes(10.0)
        assertTrue("ETA for 10km should be around 25-35 minutes", etaLong in 25..35)
    }

    @Test
    fun testMechanicStatusFiltering() {
        val mechanics = listOf(
            MechanicEntity(
                mechanicId = "m1",
                fullName = "Budi Santoso",
                phone = "081234567891",
                email = "budi@test.com",
                address = "Pekalongan",
                skills = "Servis Mesin",
                experienceYears = 5,
                workLocation = "Pekalongan Barat",
                status = "ACTIVE"
            ),
            MechanicEntity(
                mechanicId = "m2",
                fullName = "Eko Nugroho",
                phone = "081234567892",
                email = "eko@test.com",
                address = "Pekalongan",
                skills = "Kelistrikan",
                experienceYears = 7,
                workLocation = "Pekalongan Timur",
                status = "BUSY"
            ),
            MechanicEntity(
                mechanicId = "m3",
                fullName = "Fajar Setiawan",
                phone = "081234567893",
                email = "fajar@test.com",
                address = "Pekalongan",
                skills = "Tambal Ban",
                experienceYears = 3,
                workLocation = "Pekalongan Selatan",
                status = "OFFLINE"
            )
        )

        val activeList = mechanics.filter { it.status == "ACTIVE" }
        val busyList = mechanics.filter { it.status == "BUSY" }
        val offlineList = mechanics.filter { it.status == "OFFLINE" }

        assertEquals(1, activeList.size)
        assertEquals(1, busyList.size)
        assertEquals(1, offlineList.size)
    }
}
