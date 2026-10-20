package com.example.data

import android.util.Log
import com.example.model.Appointment
import com.example.model.PatientProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Universal Cloud Bridge for Dr. Balaji Homeo Care
 * Synchronizes real-time appointment bookings and status between:
 * 1. Client Web Portal (Netlify)
 * 2. Doctor Android Application (AI Studio)
 */
class ClinicFirestoreService {

    companion object {
        private const val TAG = "ClinicCloudService"
        // Universal live synchronization object shared with Web Portal
        private const val CLOUD_SYNC_URL = "https://api.restful-api.dev/objects/ff808181a09d98f701a156f8c9b52440"

        // Firestore REST parameters
        private const val PROJECT_ID = "gen-lang-client-0498394744"
        private const val DATABASE_ID = "ai-studio-android-homeocli-a8bd5b9a-3f9e-4fb2-bdf2-754ca5ca5eee"
        private const val API_KEY = "AIzaSyAKyeBOsKoLXy6xCgQoTImsl0u5oVikekU"
        private const val BASE_URL =
            "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/$DATABASE_ID/documents"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun savePatientProfile(profile: PatientProfile): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Sync to Cloud Master DB
            val currentList = fetchAppointments().toMutableList()
            syncAppointmentsToCloud(currentList, profile)

            // 2. Secondary Firestore attempt
            try {
                val url = "$BASE_URL/users/${profile.id}?key=$API_KEY"
                val fields = JSONObject().apply {
                    put("fullName", JSONObject().put("stringValue", profile.fullName))
                    put("email", JSONObject().put("stringValue", profile.email))
                    put("mobile", JSONObject().put("stringValue", profile.mobile))
                    put("age", JSONObject().put("integerValue", profile.age.toString()))
                    put("gender", JSONObject().put("stringValue", profile.gender))
                    put("bloodGroup", JSONObject().put("stringValue", profile.bloodGroup))
                    put("city", JSONObject().put("stringValue", profile.city))
                    put("address", JSONObject().put("stringValue", profile.address))
                    put("emergencyContactName", JSONObject().put("stringValue", profile.emergencyContactName))
                    put("emergencyContactPhone", JSONObject().put("stringValue", profile.emergencyContactPhone))
                    put("chronicConditions", JSONObject().put("stringValue", profile.chronicConditions))
                    put("allergies", JSONObject().put("stringValue", profile.allergies))
                    put("preferredConsultMode", JSONObject().put("stringValue", profile.preferredConsultMode))
                    put("healthCardId", JSONObject().put("stringValue", profile.healthCardId))
                }
                val request = Request.Builder()
                    .url(url)
                    .patch(JSONObject().put("fields", fields).toString().toRequestBody(jsonMediaType))
                    .build()
                client.newCall(request).execute().close()
            } catch (_: Exception) { }

            true
        } catch (e: Exception) {
            Log.w(TAG, "Error saving patient profile", e)
            false
        }
    }

    suspend fun saveAppointment(appointment: Appointment): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch current cloud state to ensure no overwrite of other bookings
            val currentList = fetchAppointments().toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == appointment.id || it.appointmentId == appointment.id }

            if (existingIndex != -1) {
                currentList[existingIndex] = appointment
            } else {
                currentList.add(0, appointment)
            }

            // 2. Push updated list to universal cloud sync endpoint
            val success = syncAppointmentsToCloud(currentList)
            Log.d(TAG, "saveAppointment to Universal Cloud: $success for ${appointment.id} (${appointment.status})")

            // 3. Background secondary sync to Firestore REST
            try {
                val url = "$BASE_URL/appointments/${appointment.id}?key=$API_KEY"
                val fields = JSONObject().apply {
                    put("id", JSONObject().put("stringValue", appointment.id))
                    put("appointmentId", JSONObject().put("stringValue", appointment.id))
                    put("patientId", JSONObject().put("stringValue", appointment.patientId))
                    put("patientName", JSONObject().put("stringValue", appointment.patientName))
                    put("patientMobile", JSONObject().put("stringValue", appointment.patientMobile))
                    put("patientEmail", JSONObject().put("stringValue", appointment.patientEmail))
                    put("symptoms", JSONObject().put("stringValue", appointment.symptoms))
                    put("consultationType", JSONObject().put("stringValue", appointment.consultationType))
                    put("date", JSONObject().put("stringValue", appointment.date))
                    put("time", JSONObject().put("stringValue", appointment.time))
                    put("status", JSONObject().put("stringValue", appointment.status))
                    put("doctorName", JSONObject().put("stringValue", appointment.doctorName))
                    put("clinicalNotes", JSONObject().put("stringValue", appointment.clinicalNotes))
                    put("prescription", JSONObject().put("stringValue", appointment.prescription))
                    put("createdAt", JSONObject().put("integerValue", appointment.createdAt.toString()))
                    put("updatedAt", JSONObject().put("integerValue", appointment.updatedAt.toString()))
                }
                val request = Request.Builder()
                    .url(url)
                    .patch(JSONObject().put("fields", fields).toString().toRequestBody(jsonMediaType))
                    .build()
                client.newCall(request).execute().close()
            } catch (_: Exception) { }

            success
        } catch (e: Exception) {
            Log.w(TAG, "Error saving appointment", e)
            false
        }
    }

    private fun syncAppointmentsToCloud(list: List<Appointment>, profile: PatientProfile? = null): Boolean {
        try {
            val apptsArray = JSONArray()
            list.forEach { a ->
                val obj = JSONObject().apply {
                    put("id", a.id)
                    put("appointmentId", a.appointmentId)
                    put("patientId", a.patientId)
                    put("patientName", a.patientName)
                    put("patientMobile", a.patientMobile)
                    put("patientPhone", a.patientMobile)
                    put("patientEmail", a.patientEmail)
                    put("symptoms", a.symptoms)
                    put("reasonForVisit", a.symptoms)
                    put("consultationType", a.consultationType)
                    put("date", a.date)
                    put("appointmentDate", a.date)
                    put("time", a.time)
                    put("timeSlot", a.time)
                    put("status", a.status)
                    put("doctorName", a.doctorName)
                    put("clinicalNotes", a.clinicalNotes)
                    put("prescription", a.prescription)
                    put("createdAt", a.createdAt)
                    put("updatedAt", a.updatedAt)
                }
                apptsArray.put(obj)
            }

            val syncData = JSONObject().apply {
                put("appointments", apptsArray)
                put("updatedAt", System.currentTimeMillis())
                if (profile != null) {
                    put("lastProfile", JSONObject().apply {
                        put("fullName", profile.fullName)
                        put("mobile", profile.mobile)
                        put("email", profile.email)
                    })
                }
            }

            val payload = JSONObject().apply {
                put("name", "dr_balaji_homeo_cloud_v2")
                put("data", JSONObject().put("sync_json", syncData.toString()))
            }

            val request = Request.Builder()
                .url(CLOUD_SYNC_URL)
                .put(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                return response.isSuccessful
            }
        } catch (e: Exception) {
            Log.w(TAG, "syncAppointmentsToCloud failed", e)
            return false
        }
    }

    suspend fun fetchAppointments(): List<Appointment> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(CLOUD_SYNC_URL)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val root = JSONObject(bodyStr)
                val dataObj = root.optJSONObject("data") ?: return@withContext emptyList()
                val syncJsonStr = dataObj.optString("sync_json", "")
                if (syncJsonStr.isEmpty()) return@withContext emptyList()

                val syncJson = JSONObject(syncJsonStr)
                val apptsArray = syncJson.optJSONArray("appointments") ?: return@withContext emptyList()

                val list = mutableListOf<Appointment>()
                for (i in 0 until apptsArray.length()) {
                    val obj = apptsArray.getJSONObject(i)
                    val id = obj.optString("id", obj.optString("appointmentId", "HM-2026-0000"))
                    val patientName = obj.optString("patientName", "Patient")
                    val patientMobile = obj.optString("patientMobile", obj.optString("patientPhone", ""))
                    val patientEmail = obj.optString("patientEmail", "")
                    val symptoms = obj.optString("symptoms", obj.optString("reasonForVisit", ""))
                    val consultType = obj.optString("consultationType", "IN_CLINIC")
                    val date = obj.optString("date", obj.optString("appointmentDate", ""))
                    val time = obj.optString("time", obj.optString("timeSlot", ""))
                    val status = obj.optString("status", "PENDING")
                    val doctor = obj.optString("doctorName", "Dr. Balaji")
                    val notes = obj.optString("clinicalNotes", "")
                    val prescription = obj.optString("prescription", "")
                    val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())

                    list.add(
                        Appointment(
                            id = id,
                            appointmentId = id,
                            patientName = patientName,
                            patientMobile = patientMobile,
                            patientEmail = patientEmail,
                            symptoms = symptoms,
                            consultationType = consultType,
                            date = date,
                            time = time,
                            status = status,
                            doctorName = doctor,
                            clinicalNotes = notes,
                            prescription = prescription,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                    )
                }
                list.sortedByDescending { it.createdAt }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchAppointments failed", e)
            emptyList()
        }
    }
}
