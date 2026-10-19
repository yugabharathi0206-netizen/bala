package com.example.model

/**
 * Data models for Dr. Balaji Homeo Care clinic app
 */

data class PatientProfile(
    val id: String = "p_yugabharathi",
    val fullName: String = "Yugabharathi",
    val email: String = "yugabharathi0206@gmail.com",
    val mobile: String = "+91 98401 23456",
    val age: Int = 26,
    val gender: String = "Male",
    val bloodGroup: String = "O+",
    val city: String = "Chennai",
    val address: String = "No. 14, 2nd Main Road, Anna Nagar, Chennai - 600040",
    val emergencyContactName: String = "S. Raman",
    val emergencyContactPhone: String = "+91 98401 99887",
    val chronicConditions: String = "Allergic Rhinitis, Occasional Migraine",
    val allergies: String = "Dust mite, Pollen, Sulfa antibiotics",
    val preferredConsultMode: String = "IN_CLINIC", // IN_CLINIC or ONLINE
    val avatarColorIndex: Int = 0,
    val healthCardId: String = "DBHC-2026-8821",
    val appointmentRemindersEnabled: Boolean = true,
    val wellnessTipsEnabled: Boolean = true,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

data class Appointment(
    val id: String,
    val appointmentId: String = id,
    val patientId: String = "p_yugabharathi",
    val patientName: String,
    val patientMobile: String,
    val patientEmail: String = "",
    val symptoms: String,
    val reasonForVisit: String = symptoms,
    val consultationType: String = "IN_CLINIC", // IN_CLINIC or ONLINE
    val date: String, // YYYY-MM-DD
    val appointmentDate: String = date,
    val time: String, // e.g. "10:30 AM"
    val timeSlot: String = time,
    val status: String = "PENDING", // PENDING, CONFIRMED, COMPLETED, CANCELLED
    val doctorName: String = "Dr. Balaji",
    val doctorId: String = "doc_balaji",
    val clinicalNotes: String = "",
    val prescription: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ClinicalSpecialty(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val shortDesc: String,
    val fullDesc: String,
    val commonConditions: List<String>,
    val classicalApproach: String
)

data class HomeoRemedy(
    val name: String,
    val commonName: String,
    val category: String,
    val primaryIndications: List<String>,
    val keynoteModalities: String,
    val clinicalAdvice: String
)

enum class AppScreen(val title: String) {
    HOME("Dr. Balaji Homeo"),
    BOOK("Book Consult"),
    APPOINTMENTS("My Appointments"),
    GUIDE("Homeo Guide"),
    PROFILE("My Profile"),
    DOCTOR_CONSOLE("Doctor Console")
}

enum class UserRole {
    PATIENT,
    DOCTOR
}
