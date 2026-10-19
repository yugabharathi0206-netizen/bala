package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PatientProfile
import com.example.ui.HomeoCareViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: HomeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.patientProfile.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val context = LocalContext.current

    // Local draft states for modifying profile
    var fullName by remember(currentProfile) { mutableStateOf(currentProfile.fullName) }
    var email by remember(currentProfile) { mutableStateOf(currentProfile.email) }
    var mobile by remember(currentProfile) { mutableStateOf(currentProfile.mobile) }
    var ageText by remember(currentProfile) { mutableStateOf(currentProfile.age.toString()) }
    var gender by remember(currentProfile) { mutableStateOf(currentProfile.gender) }
    var bloodGroup by remember(currentProfile) { mutableStateOf(currentProfile.bloodGroup) }
    var city by remember(currentProfile) { mutableStateOf(currentProfile.city) }
    var address by remember(currentProfile) { mutableStateOf(currentProfile.address) }
    var emergencyName by remember(currentProfile) { mutableStateOf(currentProfile.emergencyContactName) }
    var emergencyPhone by remember(currentProfile) { mutableStateOf(currentProfile.emergencyContactPhone) }
    var chronicConditions by remember(currentProfile) { mutableStateOf(currentProfile.chronicConditions) }
    var allergies by remember(currentProfile) { mutableStateOf(currentProfile.allergies) }
    var preferredConsultMode by remember(currentProfile) { mutableStateOf(currentProfile.preferredConsultMode) }
    var remindersEnabled by remember(currentProfile) { mutableStateOf(currentProfile.appointmentRemindersEnabled) }
    var wellnessTipsEnabled by remember(currentProfile) { mutableStateOf(currentProfile.wellnessTipsEnabled) }
    var selectedColorIndex by remember(currentProfile) { mutableIntStateOf(currentProfile.avatarColorIndex) }

    var bloodDropdownExpanded by remember { mutableStateOf(false) }
    var genderDropdownExpanded by remember { mutableStateOf(false) }
    var showShareModal by remember { mutableStateOf(false) }

    val avatarGradients = listOf(
        listOf(Color(0xFF0D9488), Color(0xFF047857)), // Teal-Emerald
        listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)), // Blue
        listOf(Color(0xFF7C3AED), Color(0xFF6D28D9)), // Violet
        listOf(Color(0xFFD97706), Color(0xFFB45309)), // Amber
        listOf(Color(0xFFE11D48), Color(0xFFBE123C)), // Rose
        listOf(Color(0xFF475569), Color(0xFF334155))  // Slate
    )

    val bloodGroups = listOf("O+", "A+", "B+", "AB+", "O-", "A-", "B-", "AB-")
    val genders = listOf("Male", "Female", "Other")

    val lastSyncFormatted = remember(currentProfile.lastSyncedAt) {
        val df = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        df.format(Date(currentProfile.lastSyncedAt))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Patient Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Manage your health identity & clinic records",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { showShareModal = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Card",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Health Card",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 2. Digital Patient Health Card Display
        item {
            val grad = avatarGradients.getOrElse(selectedColorIndex) { avatarGradients[0] }
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("digital_health_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(grad))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🌿", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "DR. BALAJI HOMEO CARE",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Classical Homeopathy Patient ID",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = bloodGroup,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = fullName.ifEmpty { "Patient Name" },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$email • $mobile",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "HEALTH ID NUMBER",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentProfile.healthCardId,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = city,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Avatar Color Accent Customizer
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Card Theme Accent",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Select your digital card gradient color",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        avatarGradients.forEachIndexed { index, grad ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(grad))
                                    .clickable { selectedColorIndex = index }
                                    .then(
                                        if (selectedColorIndex == index) Modifier.border(
                                            2.dp,
                                            Color.White,
                                            CircleShape
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColorIndex == index) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Personal Information Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Personal Information",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prof_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Email Address
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address (Primary Login) *") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prof_email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mobile Number
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Registered Mobile Number *") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prof_mobile_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Age, Gender & Blood Group in Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Age
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Age") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Gender Dropdown
                        ExposedDropdownMenuBox(
                            expanded = genderDropdownExpanded,
                            onExpandedChange = { genderDropdownExpanded = !genderDropdownExpanded },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Gender") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderDropdownExpanded) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = genderDropdownExpanded,
                                onDismissRequest = { genderDropdownExpanded = false }
                            ) {
                                genders.forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g) },
                                        onClick = {
                                            gender = g
                                            genderDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Blood Group Dropdown
                        ExposedDropdownMenuBox(
                            expanded = bloodDropdownExpanded,
                            onExpandedChange = { bloodDropdownExpanded = !bloodDropdownExpanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = bloodGroup,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Blood") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodDropdownExpanded) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = bloodDropdownExpanded,
                                onDismissRequest = { bloodDropdownExpanded = false }
                            ) {
                                bloodGroups.forEach { bg ->
                                    DropdownMenuItem(
                                        text = { Text(bg) },
                                        onClick = {
                                            bloodGroup = bg
                                            bloodDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // City
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LocationCity, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Address
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Residential Address (for remedy couriers)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 5. Medical History & Allergies (Crucial for Classical Homeopathy!)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Medical History & Constitutional Markers",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "In homeopathy, complete past illness history helps determine your deep-acting constitutional simillimum.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Chronic Conditions
                    OutlinedTextField(
                        value = chronicConditions,
                        onValueChange = { chronicConditions = it },
                        label = { Text("Chronic Conditions / Past Ailments") },
                        placeholder = { Text("e.g. Sinusitis, Childhood wheezing, Migraine, PCOD...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.MedicalInformation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Known Allergies
                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Known Allergies & Drug Sensitivities") },
                        placeholder = { Text("e.g. Dust mites, Pollen, Sulfa drugs, Penicillin, Seafood...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preferred Consultation Mode
                    Text(
                        text = "Preferred Consultation Mode:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (preferredConsultMode == "IN_CLINIC") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { preferredConsultMode = "IN_CLINIC" }
                        ) {
                            Text(
                                text = "🏥 In-Clinic Visit",
                                fontSize = 12.sp,
                                fontWeight = if (preferredConsultMode == "IN_CLINIC") FontWeight.Bold else FontWeight.Normal,
                                color = if (preferredConsultMode == "IN_CLINIC") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (preferredConsultMode == "ONLINE") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { preferredConsultMode = "ONLINE" }
                        ) {
                            Text(
                                text = "🌐 Online Video",
                                fontSize = 12.sp,
                                fontWeight = if (preferredConsultMode == "ONLINE") FontWeight.Bold else FontWeight.Normal,
                                color = if (preferredConsultMode == "ONLINE") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Emergency Contact
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Emergency Contact Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = emergencyName,
                            onValueChange = { emergencyName = it },
                            label = { Text("Contact Person") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = emergencyPhone,
                            onValueChange = { emergencyPhone = it },
                            label = { Text("Emergency Phone") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // 7. Notification Preferences
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. Notification & Clinic Alerts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Consultation Reminders", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "SMS and notification reminders 2 hours before slot", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = { remindersEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Homeo Healing & Seasonal Advice", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Gentle wellness tips by Dr. Balaji tailored to climate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = wellnessTipsEnabled,
                            onCheckedChange = { wellnessTipsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // 8. Save & Cloud Sync Action
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val parsedAge = ageText.toIntOrNull() ?: currentProfile.age
                        val updated = currentProfile.copy(
                            fullName = fullName.trim().ifEmpty { currentProfile.fullName },
                            email = email.trim().ifEmpty { currentProfile.email },
                            mobile = mobile.trim().ifEmpty { currentProfile.mobile },
                            age = parsedAge,
                            gender = gender,
                            bloodGroup = bloodGroup,
                            city = city.trim(),
                            address = address.trim(),
                            emergencyContactName = emergencyName.trim(),
                            emergencyContactPhone = emergencyPhone.trim(),
                            chronicConditions = chronicConditions.trim(),
                            allergies = allergies.trim(),
                            preferredConsultMode = preferredConsultMode,
                            avatarColorIndex = selectedColorIndex,
                            appointmentRemindersEnabled = remindersEnabled,
                            wellnessTipsEnabled = wellnessTipsEnabled
                        )
                        viewModel.saveProfile(updated)
                    },
                    enabled = !isSyncing,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_profile_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing to Clinic Cloud...")
                    } else {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Profile & Sync to Clinic Cloud",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cloud Synced: $lastSyncFormatted",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            fullName = "Yugabharathi"
                            email = "yugabharathi0206@gmail.com"
                            mobile = "+91 98401 23456"
                            ageText = "26"
                            gender = "Male"
                            bloodGroup = "O+"
                            city = "Chennai"
                            address = "No. 14, 2nd Main Road, Anna Nagar, Chennai - 600040"
                            emergencyName = "S. Raman"
                            emergencyPhone = "+91 98401 99887"
                            chronicConditions = "Allergic Rhinitis, Occasional Migraine"
                            allergies = "Dust mite, Pollen, Sulfa drugs"
                            preferredConsultMode = "IN_CLINIC"
                            selectedColorIndex = 0
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Form", fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Health Card Share Dialog
    if (showShareModal) {
        AlertDialog(
            onDismissRequest = { showShareModal = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(text = "Digital Health Card", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Dr. Balaji Homeo Care Health Passport\nCard ID: ${currentProfile.healthCardId}\nPatient: ${currentProfile.fullName}\nBlood Group: ${currentProfile.bloodGroup} • Age: ${currentProfile.age} (${currentProfile.gender})\nMobile: ${currentProfile.mobile}\nCity: ${currentProfile.city}\nEmergency Contact: ${currentProfile.emergencyContactName} (${currentProfile.emergencyContactPhone})\nAllergies: ${currentProfile.allergies}",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareText = "Dr. Balaji Homeo Care - Patient Card\nCard ID: ${currentProfile.healthCardId}\nName: ${currentProfile.fullName}\nBlood Group: ${currentProfile.bloodGroup}\nMobile: ${currentProfile.mobile}\nEmergency: ${currentProfile.emergencyContactPhone}"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Digital Health Card"))
                        showShareModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Share Passport")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showShareModal = false }) {
                    Text("Close")
                }
            }
        )
    }
}
