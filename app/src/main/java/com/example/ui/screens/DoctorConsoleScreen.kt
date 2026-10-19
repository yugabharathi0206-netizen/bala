package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Appointment
import com.example.ui.HomeoCareViewModel
import com.example.ui.theme.StatusConfirmedBg
import com.example.ui.theme.StatusConfirmedText
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingText

@Composable
fun DoctorConsoleScreen(
    viewModel: HomeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val appointments by viewModel.appointments.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var activePrescriptionAppt by remember { mutableStateOf<Appointment?>(null) }
    var prescriptionInput by remember { mutableStateOf("") }
    var clinicalNotesInput by remember { mutableStateOf("") }

    val pendingCount = appointments.count { it.status == "PENDING" }
    val confirmedCount = appointments.count { it.status == "CONFIRMED" }
    val completedCount = appointments.count { it.status == "COMPLETED" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("doctor_console_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dr. Balaji Clinic Console",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time Patient Review & Clinical Prescriptions",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.syncWithCloud() },
                    modifier = Modifier.testTag("doctor_sync_btn")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sync Cloud")
                    }
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Pending",
                    count = pendingCount.toString(),
                    color = StatusPendingBg,
                    textColor = StatusPendingText,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Confirmed",
                    count = confirmedCount.toString(),
                    color = StatusConfirmedBg,
                    textColor = StatusConfirmedText,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Completed",
                    count = completedCount.toString(),
                    color = Color(0xFFDBEAFE),
                    textColor = Color(0xFF1E40AF),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section Title
        item {
            Text(
                text = "Patient Consultation Queue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(appointments) { appt ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${appt.patientName} (${appt.id})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Phone: ${appt.patientMobile} • ${if (appt.consultationType == "IN_CLINIC") "In-Clinic" else "Video"}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (appt.status) {
                                "CONFIRMED" -> StatusConfirmedBg
                                "COMPLETED" -> Color(0xFFDBEAFE)
                                "CANCELLED" -> Color(0xFFFEE2E2)
                                else -> StatusPendingBg
                            }
                        ) {
                            Text(
                                text = appt.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (appt.status) {
                                    "CONFIRMED" -> StatusConfirmedText
                                    "COMPLETED" -> Color(0xFF1E40AF)
                                    "CANCELLED" -> Color(0xFF991B1B)
                                    else -> StatusPendingText
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Slot: ${appt.date} at ${appt.time}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Symptoms: ${appt.symptoms}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (appt.prescription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Prescribed: ${appt.prescription}",
                            fontSize = 11.sp,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (appt.status == "PENDING") {
                            Button(
                                onClick = { viewModel.updateAppointmentStatus(appt.id, "CONFIRMED") },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Confirm Slot", fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                activePrescriptionAppt = appt
                                prescriptionInput = appt.prescription
                                clinicalNotesInput = appt.clinicalNotes
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (appt.prescription.isNotEmpty()) "Edit Prescription" else "Prescribe", fontSize = 11.sp)
                        }

                        if (appt.status != "CANCELLED") {
                            OutlinedButton(
                                onClick = { viewModel.updateAppointmentStatus(appt.id, "CANCELLED") },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Prescription & Clinical Notes Entry Dialog
    if (activePrescriptionAppt != null) {
        val appt = activePrescriptionAppt!!
        AlertDialog(
            onDismissRequest = { activePrescriptionAppt = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(text = "Clinical Prescription", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Patient: ${appt.patientName} (${appt.id})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Complaints: ${appt.symptoms}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = prescriptionInput,
                        onValueChange = { prescriptionInput = it },
                        label = { Text("Prescribed Remedies & Dosage *") },
                        placeholder = { Text("e.g. Arsenicum Alb 200C - 4 pills once daily in morning for 7 days.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = clinicalNotesInput,
                        onValueChange = { clinicalNotesInput = it },
                        label = { Text("Doctor's Advice & Diet Restrictions") },
                        placeholder = { Text("e.g. Avoid coffee/raw onion near dosage. Drink warm water.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addPrescription(appt.id, prescriptionInput.trim(), clinicalNotesInput.trim())
                        activePrescriptionAppt = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Prescription")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { activePrescriptionAppt = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    count: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}
