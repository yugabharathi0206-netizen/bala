package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HomeoCareRepository
import com.example.model.AppScreen
import com.example.model.Appointment
import com.example.model.ClinicalSpecialty
import com.example.model.HomeoRemedy
import com.example.model.PatientProfile
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeoCareViewModel(
    private val repository: HomeoCareRepository = HomeoCareRepository()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.PATIENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    val patientProfile: StateFlow<PatientProfile> = repository.patientProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, PatientProfile())

    val appointments: StateFlow<List<Appointment>> = repository.appointments
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val clinicalSpecialties: List<ClinicalSpecialty> = repository.getClinicalSpecialties()
    val homeoRemedies: List<HomeoRemedy> = repository.getHomeoRemedies()

    private val _selectedSpecialty = MutableStateFlow<ClinicalSpecialty?>(null)
    val selectedSpecialty: StateFlow<ClinicalSpecialty?> = _selectedSpecialty.asStateFlow()

    private val _selectedRemedy = MutableStateFlow<HomeoRemedy?>(null)
    val selectedRemedy: StateFlow<HomeoRemedy?> = _selectedRemedy.asStateFlow()

    // Booking draft
    private val _consultType = MutableStateFlow("IN_CLINIC")
    val consultType: StateFlow<String> = _consultType.asStateFlow()

    private val todayFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val _bookingDate = MutableStateFlow(todayFormatted)
    val bookingDate: StateFlow<String> = _bookingDate.asStateFlow()

    private val _bookingTimeSlot = MutableStateFlow("10:30 AM")
    val bookingTimeSlot: StateFlow<String> = _bookingTimeSlot.asStateFlow()

    private val _symptomsText = MutableStateFlow("")
    val symptomsText: StateFlow<String> = _symptomsText.asStateFlow()

    private val _isBookingSubmitting = MutableStateFlow(false)
    val isBookingSubmitting: StateFlow<Boolean> = _isBookingSubmitting.asStateFlow()

    private val _bookingSuccessAppointment = MutableStateFlow<Appointment?>(null)
    val bookingSuccessAppointment: StateFlow<Appointment?> = _bookingSuccessAppointment.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _appointmentFilter = MutableStateFlow("ALL")
    val appointmentFilter: StateFlow<String> = _appointmentFilter.asStateFlow()

    init {
        // Initial cloud sync
        syncWithCloud()
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(4000)
                try {
                    repository.refreshCloudData()
                } catch (_: Exception) {}
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun toggleRole() {
        val newRole = if (_currentRole.value == UserRole.PATIENT) UserRole.DOCTOR else UserRole.PATIENT
        _currentRole.value = newRole
        _toastMessage.value = if (newRole == UserRole.DOCTOR) {
            "Switched to Dr. Balaji (Doctor Console)"
        } else {
            "Switched to Patient Portal"
        }
        if (newRole == UserRole.DOCTOR && _currentScreen.value == AppScreen.BOOK) {
            _currentScreen.value = AppScreen.DOCTOR_CONSOLE
        }
    }

    fun selectSpecialty(specialty: ClinicalSpecialty?) {
        _selectedSpecialty.value = specialty
    }

    fun selectRemedy(remedy: HomeoRemedy?) {
        _selectedRemedy.value = remedy
    }

    fun setConsultType(type: String) {
        _consultType.value = type
    }

    fun setBookingDate(dateStr: String) {
        _bookingDate.value = dateStr
    }

    fun setBookingDateDaysOffset(offsetDays: Int) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        _bookingDate.value = df.format(cal.time)
    }

    fun setBookingTimeSlot(slot: String) {
        _bookingTimeSlot.value = slot
    }

    fun setSymptomsText(text: String) {
        _symptomsText.value = text
    }

    fun appendSymptomTag(tag: String) {
        val current = _symptomsText.value.trim()
        _symptomsText.value = if (current.isEmpty()) tag else "$current, $tag"
    }

    fun setAppointmentFilter(filter: String) {
        _appointmentFilter.value = filter
    }

    fun confirmBooking() {
        val symptoms = _symptomsText.value.trim()
        if (symptoms.isEmpty()) {
            _toastMessage.value = "Please describe your symptoms or health concern."
            return
        }

        viewModelScope.launch {
            _isBookingSubmitting.value = true
            try {
                val profile = patientProfile.value
                val newAppt = repository.bookAppointment(
                    patientName = profile.fullName,
                    patientMobile = profile.mobile,
                    patientEmail = profile.email,
                    symptoms = symptoms,
                    consultType = _consultType.value,
                    date = _bookingDate.value,
                    time = _bookingTimeSlot.value
                )
                _bookingSuccessAppointment.value = newAppt
                _symptomsText.value = ""
                _toastMessage.value = "Appointment Booked! Token: ${newAppt.id}"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to book: ${e.message}"
            } finally {
                _isBookingSubmitting.value = false
            }
        }
    }

    fun dismissBookingSuccess() {
        _bookingSuccessAppointment.value = null
        _currentScreen.value = AppScreen.APPOINTMENTS
    }

    fun saveProfile(updated: PatientProfile) {
        viewModelScope.launch {
            _isSyncing.value = true
            val success = repository.updateProfile(updated)
            _isSyncing.value = false
            _toastMessage.value = if (success) {
                "Profile updated & synced to Clinic Cloud!"
            } else {
                "Profile saved locally (Offline mode)."
            }
        }
    }

    fun updateAppointmentStatus(appointmentId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointmentId, newStatus)
            _toastMessage.value = "Appointment updated to $newStatus"
        }
    }

    fun addPrescription(appointmentId: String, prescriptionText: String, clinicalNotes: String) {
        viewModelScope.launch {
            repository.addPrescription(appointmentId, prescriptionText, clinicalNotes)
            _toastMessage.value = "Prescription & notes recorded successfully."
        }
    }

    fun syncWithCloud() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.refreshCloudData()
            } catch (_: Exception) {
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
