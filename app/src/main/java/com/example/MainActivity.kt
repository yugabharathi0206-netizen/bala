package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.model.UserRole
import com.example.ui.HomeoCareViewModel
import com.example.ui.screens.AppointmentsScreen
import com.example.ui.screens.BookAppointmentScreen
import com.example.ui.screens.DoctorConsoleScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RemediesGuideScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HomeoCareApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeoCareApp(viewModel: HomeoCareViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val context = LocalContext.current

    val pendingCount = appointments.count { it.status == "PENDING" }

    // Toast listener
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Predictive back handling
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        viewModel.navigateTo(AppScreen.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🌿", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dr. Balaji Homeo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentRole == UserRole.PATIENT) "Patient Portal • Chennai" else "Dr. Balaji (Doctor Mode)",
                                fontSize = 11.sp,
                                color = if (currentRole == UserRole.PATIENT) MaterialTheme.colorScheme.primary else Color(0xFFD97706),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Role Switcher Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (currentRole == UserRole.PATIENT) MaterialTheme.colorScheme.primaryContainer else Color(0xFFFEF3C7),
                        modifier = Modifier
                            .clickable { viewModel.toggleRole() }
                            .testTag("role_switch_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Role",
                                tint = if (currentRole == UserRole.PATIENT) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF92400E),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentRole == UserRole.PATIENT) "Patient" else "Doctor",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentRole == UserRole.PATIENT) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF92400E)
                            )
                        }
                    }

                    // Cloud Sync Button
                    IconButton(
                        onClick = { viewModel.syncWithCloud() },
                        modifier = Modifier.testTag("top_cloud_sync_btn")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Sync Cloud",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.HOME,
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )

                if (currentRole == UserRole.PATIENT) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.BOOK,
                        onClick = { viewModel.navigateTo(AppScreen.BOOK) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Book") },
                        label = { Text("Book", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_book")
                    )
                }

                NavigationBarItem(
                    selected = currentScreen == AppScreen.APPOINTMENTS,
                    onClick = { viewModel.navigateTo(AppScreen.APPOINTMENTS) },
                    icon = {
                        if (pendingCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text(pendingCount.toString())
                                    }
                                }
                            ) {
                                Icon(Icons.Default.EventNote, contentDescription = "Appointments")
                            }
                        } else {
                            Icon(Icons.Default.EventNote, contentDescription = "Appointments")
                        }
                    },
                    label = { Text("Appts", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_appointments")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.GUIDE,
                    onClick = { viewModel.navigateTo(AppScreen.GUIDE) },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Homeo Guide") },
                    label = { Text("Guide", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_guide")
                )

                if (currentRole == UserRole.DOCTOR) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DOCTOR_CONSOLE,
                        onClick = { viewModel.navigateTo(AppScreen.DOCTOR_CONSOLE) },
                        icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = "Console") },
                        label = { Text("Console", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFD97706),
                            selectedTextColor = Color(0xFFD97706),
                            indicatorColor = Color(0xFFFEF3C7)
                        ),
                        modifier = Modifier.testTag("nav_item_doctor_console")
                    )
                }

                NavigationBarItem(
                    selected = currentScreen == AppScreen.PROFILE,
                    onClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_profile")
                )
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier.padding(innerPadding)
        ) { screen ->
            when (screen) {
                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                AppScreen.BOOK -> BookAppointmentScreen(viewModel = viewModel)
                AppScreen.APPOINTMENTS -> AppointmentsScreen(viewModel = viewModel)
                AppScreen.GUIDE -> RemediesGuideScreen(viewModel = viewModel)
                AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
                AppScreen.DOCTOR_CONSOLE -> DoctorConsoleScreen(viewModel = viewModel)
            }
        }
    }
}
