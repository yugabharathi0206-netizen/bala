package com.example

import com.example.data.HomeoCareRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testPatientProfileDefaultAndModification() = runBlocking {
        val repo = HomeoCareRepository()
        val defaultProfile = repo.patientProfile.value

        assertEquals("Yugabharathi", defaultProfile.fullName)
        assertEquals("yugabharathi0206@gmail.com", defaultProfile.email)
        assertEquals("O+", defaultProfile.bloodGroup)

        val updated = defaultProfile.copy(
            fullName = "Yugabharathi S.",
            city = "Chennai Central",
            chronicConditions = "Migraine & Dust Allergy"
        )
        repo.updateProfile(updated)

        val retrieved = repo.patientProfile.value
        assertEquals("Yugabharathi S.", retrieved.fullName)
        assertEquals("Chennai Central", retrieved.city)
        assertEquals("Migraine & Dust Allergy", retrieved.chronicConditions)
    }

    @Test
    fun testBookAppointmentAndStatusUpdate() = runBlocking {
        val repo = HomeoCareRepository()
        val initialCount = repo.appointments.value.size

        val booked = repo.bookAppointment(
            patientName = "Yugabharathi",
            patientMobile = "+91 98401 23456",
            patientEmail = "yugabharathi0206@gmail.com",
            symptoms = "Allergic sneezing and watery eyes in cold breeze",
            consultType = "IN_CLINIC",
            date = "2026-10-25",
            time = "10:30 AM"
        )

        assertNotNull(booked.id)
        assertTrue(booked.id.startsWith("HM-2026-"))
        assertEquals(initialCount + 1, repo.appointments.value.size)

        repo.updateAppointmentStatus(booked.id, "CONFIRMED")
        val updatedAppt = repo.appointments.value.first { it.id == booked.id }
        assertEquals("CONFIRMED", updatedAppt.status)
    }

    @Test
    fun testClinicalSpecialtiesAndRemediesCatalog() {
        val repo = HomeoCareRepository()
        val specialties = repo.getClinicalSpecialties()
        val remedies = repo.getHomeoRemedies()

        assertTrue(specialties.isNotEmpty())
        assertTrue(remedies.isNotEmpty())
        assertTrue(specialties.any { it.title.contains("Skin") })
        assertTrue(remedies.any { it.name.contains("Arnica") })
    }
}
