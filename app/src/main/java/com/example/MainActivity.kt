package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.MilkRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonthlyBillScreen
import com.example.ui.screens.RateManagementScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DairyBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MilkViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class MainTab(val hindiTitle: String, val icon: ImageVector, val tag: String) {
    DAILY("हिसाब", Icons.Default.LocalDrink, "nav_tab_daily"),
    BILL("मासिक बिल", Icons.Default.Calculate, "nav_tab_bill"),
    RATES("रेट बदलें", Icons.Default.CurrencyRupee, "nav_tab_rates"),
    REMINDERS("रिमाइंडर", Icons.Default.Notifications, "nav_tab_reminders"),
    SETTINGS("सेटिंग्स", Icons.Default.Settings, "nav_tab_settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = FirebaseAuth.getInstance().currentUser
            }
        )
    } else {
        MainFeatureApp(
            currentUserId = currentUser!!.uid,
            onSignOut = {
                currentUser = null
            }
        )
    }
}

@Composable
fun MainFeatureApp(
    currentUserId: String,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(MainTab.DAILY) }

    // Resolve custom firestore_database_id strictly at the ViewModel Factory boundary
    val viewModel: MilkViewModel = viewModel(
        key = currentUserId,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val databaseId = context.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                return MilkViewModel(MilkRepository(db), currentUserId) as T
            }
        }
    )

    // BackHandler to navigate back to Home if user is on another tab
    if (currentTab != MainTab.DAILY) {
        BackHandler {
            currentTab = MainTab.DAILY
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.hindiTitle) },
                        label = { Text(tab.hindiTitle) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DairyBlue,
                            indicatorColor = DairyBlue.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.DAILY -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToBill = { currentTab = MainTab.BILL },
                        onNavigateToRates = { currentTab = MainTab.RATES }
                    )
                }
                MainTab.BILL -> {
                    MonthlyBillScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentTab = MainTab.DAILY },
                        onNavigateToReminders = { currentTab = MainTab.REMINDERS }
                    )
                }
                MainTab.RATES -> {
                    RateManagementScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentTab = MainTab.DAILY }
                    )
                }
                MainTab.REMINDERS -> {
                    RemindersScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentTab = MainTab.DAILY }
                    )
                }
                MainTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentTab = MainTab.DAILY },
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}
