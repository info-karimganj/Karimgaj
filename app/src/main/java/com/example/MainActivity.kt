package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.*
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // Retrieve our single-instance repository through explicit compile-time dependency injection
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory((application as KarimganjApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(LocalThemeState provides isDarkMode) {
                    App(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun App(viewModel: MainViewModel) {
    // Top-level screen state (either "splash" or "main")
    var currentScreen by remember { mutableStateOf("splash") }
    // If set, shows item detail page on top
    var selectedDetailId by remember { mutableStateOf<String?>(null) }

    // Unified global back button navigation flow
    BackHandler(enabled = currentScreen != "splash") {
        if (selectedDetailId != null) {
            selectedDetailId = null
        } else {
            val handledByViewModel = viewModel.handleBack()
            if (!handledByViewModel) {
                currentScreen = "splash"
            }
        }
    }

    when (currentScreen) {
        "splash" -> {
            SplashScreen(
                onNavigateToHome = {
                    currentScreen = "main"
                }
            )
        }
        "main" -> {
            if (selectedDetailId != null) {
                DetailScreen(
                    itemId = selectedDetailId!!,
                    viewModel = viewModel,
                    onNavigateBack = {
                        selectedDetailId = null
                    }
                )
            } else {
                MainHostScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { itemId ->
                        selectedDetailId = itemId
                    }
                )
            }
        }
    }
}

@Composable
fun MainHostScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White,
                tonalElevation = 8.dp
            ) {
                // Home (হোম)
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home, contentDescription = if (isEnglish) "Home" else "হোম") },
                    label = { Text(if (isEnglish) "Home" else "হোম", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KarimganjCrimson,
                        selectedTextColor = KarimganjCrimson,
                        indicatorColor = if (isDarkMode) Color(0xFF451A1A) else Color(0xFFFFEBEE),
                        unselectedIconColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        unselectedTextColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )
                
                // Online (অনলাইন)
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(if (selectedTab == 1) Icons.Default.Language else Icons.Outlined.Language, contentDescription = if (isEnglish) "Online" else "অনলাইন") },
                    label = { Text(if (isEnglish) "Online" else "অনলাইন", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KarimganjCrimson,
                        selectedTextColor = KarimganjCrimson,
                        indicatorColor = if (isDarkMode) Color(0xFF451A1A) else Color(0xFFFFEBEE),
                        unselectedIconColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        unselectedTextColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    ),
                    modifier = Modifier.testTag("nav_item_online")
                )
                
                // Complaint (অভিযোগ)
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(if (selectedTab == 2) Icons.Default.EditNote else Icons.Outlined.EditNote, contentDescription = if (isEnglish) "Complaint" else "অভিযোগ") },
                    label = { Text(if (isEnglish) "Complaint" else "অভিযোগ", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KarimganjCrimson,
                        selectedTextColor = KarimganjCrimson,
                        indicatorColor = if (isDarkMode) Color(0xFF451A1A) else Color(0xFFFFEBEE),
                        unselectedIconColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        unselectedTextColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    ),
                    modifier = Modifier.testTag("nav_item_complaint")
                )
                
                // Info (তথ্য)
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(if (selectedTab == 3) Icons.Default.Info else Icons.Outlined.Info, contentDescription = if (isEnglish) "Info" else "তথ্য") },
                    label = { Text(if (isEnglish) "Info" else "তথ্য", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KarimganjCrimson,
                        selectedTextColor = KarimganjCrimson,
                        indicatorColor = if (isDarkMode) Color(0xFF451A1A) else Color(0xFFFFEBEE),
                        unselectedIconColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        unselectedTextColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    ),
                    modifier = Modifier.testTag("nav_item_info")
                )
                
                // Profile (প্রোফাইল)
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = { Icon(if (selectedTab == 4) Icons.Default.Person else Icons.Outlined.Person, contentDescription = if (isEnglish) "Profile" else "প্রোফাইল") },
                    label = { Text(if (isEnglish) "Profile" else "প্রোফাইল", fontSize = 11.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KarimganjCrimson,
                        selectedTextColor = KarimganjCrimson,
                        indicatorColor = if (isDarkMode) Color(0xFF451A1A) else Color(0xFFFFEBEE),
                        unselectedIconColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        unselectedTextColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    ),
                    modifier = Modifier.testTag("nav_item_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToTab = { tabIndex -> viewModel.selectTab(tabIndex) }
                )
                1 -> OnlineServicesScreen(isEnglish = isEnglish)
                2 -> ComplaintScreen(isEnglish = isEnglish)
                3 -> AboutScreen(viewModel = viewModel)
                4 -> ProfileScreen(isEnglish = isEnglish, onCallHelpline = { viewModel.selectTab(0) }) // Back to Home or trigger action
            }
        }
    }
}
