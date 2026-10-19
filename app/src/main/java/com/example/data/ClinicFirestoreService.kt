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
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Direct REST bridge to the shared Dr. Balaji Homeo Care Firestore database
 * Connected with https://bright-fairy-c15840.netlify.app
 */
class ClinicFirestoreService {

    companion object {
        private const val TAG = "ClinicFirestore"
        private const val PROJECT_ID = "gen-lang-client-0498394744"
        private const val DATABASE_ID = "ai-studio-android-homeocli-a8bd5b9a-3f9e-4fb2-bdf2-754ca5ca5eee"
        private const val API_KEY = "AIzaSyAKyeBOsKoLXy6xCgQoTImsl0u5oVikekU"
        private const val BASE_URL =
            "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/$DATABASE_ID/documents"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun savePatientProfile(profile: PatientProfile): Boolean = withContext(Dispatchers.IO) {
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
                put("updatedAt", JSONObject().put("integerValue", System.currentTimeMillis().toString()))
            }

            val bodyJson = JSONObject().put("fields", fields).toString()
            val requestBody = bodyJson.toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url(url)
                .patch(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val success = response.isSuccessful
                Log.d(TAG, "savePatientProfile HTTP ${response.code}: $success")
                success
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error saving patient profile to Firestore", e)
            false
        }
    }

    suspend fun saveAppointment(appointment: Appointment): Boolean = withContext(Dispatchers.IO) {
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
                put("appointmentDate", JSONObject().put("stringValue", appointment.date))
                put("time", JSONObject().put("stringValue", appointment.time))
                put("timeSlot", JSONObject().put("stringValue", appointment.time))
                put("status", JSONObject().put("stringValue", appointment.status))
                put("doctorName", JSONObject().put("stringValue", appointment.doctorName))
                put("clinicalNotes", JSONObject().put("stringValue", appointment.clinicalNotes))
                put("prescription", JSONObject().put("stringValue", appointment.prescription))
                put("createdAt", JSONObject().put("integerValue", appointment.createdAt.toString()))
                put("updatedAt", JSONObject().put("integerValue", appointment.updatedAt.toString()))
            }

            val bodyJson = JSONObject().put("fields", fields).toString()
            val request = Request.Builder()
                .url(url)
                .patch(bodyJson.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val success = response.isSuccessful
                Log.d(TAG, "saveAppointment HTTP ${response.code}: $success")
                success
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error saving appointment to Firestore", e)
            false
        }
    }

    suspend fun fetchAppointments(): List<Appointment> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/appointments?key=$API_KEY&pageSize=50"
            val request = Request.Builder().url(url).get().build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val root = JSONObject(bodyStr)
                val docsArray = root.optJSONArray("documents") ?: return@withContext emptyList()

                val list = mutableListOf<Appointment>()
                for (i in 0 until docsArray.length()) {
                    val docObj = docsArray.getJSONObject(i)
                    val fields = docObj.optJSONObject("fields") ?: continue
                    val docName = docObj.optString("name", "")
                    val docId = docName.substringAfterLast("/")

                    val id = fields.optJSONObject("id")?.optString("stringValue") ?: docId
                    val patientName = fields.optJSONObject("patientName")?.optString("stringValue") ?: "Patient"
                    val patientMobile = fields.optJSONObject("patientMobile")?.optString("stringValue")
                        ?: fields.optJSONObject("patientPhone")?.optString("stringValue") ?: ""
                    val patientEmail = fields.optJSONObject("patientEmail")?.optString("stringValue") ?: ""
                    val symptoms = fields.optJSONObject("symptoms")?.optString("stringValue")
                        ?: fields.optJSONObject("reasonForVisit")?.optString("stringValue") ?: ""
                    val consultType = fields.optJSONObject("consultationType")?.optString("stringValue") ?: "IN_CLINIC"
                    val date = fields.optJSONObject("date")?.optString("stringValue")
                        ?: fields.optJSONObject("appointmentDate")?.optString("stringValue") ?: ""
                    val time = fields.optJSONObject("time")?.optString("stringValue")
                        ?: fields.optJSONObject("timeSlot")?.optString("stringValue") ?: ""
                    val status = fields.optJSONObject("status")?.optString("stringValue") ?: "PENDING"
                    val doctor = fields.optJSONObject("doctorName")?.optString("stringValue") ?: "Dr. Balaji"
                    val notes = fields.optJSONObject("clinicalNotes")?.optString("stringValue") ?: ""
                    val prescription = fields.optJSONObject("prescription")?.optString("stringValue") ?: ""
                    val createdAt = fields.optJSONObject("createdAt")?.optString("integerValue")?.toLongOrNull()
                        ?: System.currentTimeMillis()

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
                            createdAt = createdAt
                        )
                    )
                }
                list.sortedByDescending { it.createdAt }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch appointments from Firestore", e)
            emptyList()
        }
    }
}
