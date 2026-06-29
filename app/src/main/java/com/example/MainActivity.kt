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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
            MyApplicationTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(
                            onNavigateToHome = {
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("main") {
                        MainHostScreen(
                            viewModel = viewModel,
                            onNavigateToDetail = { itemId ->
                                navController.navigate("detail/$itemId")
                            }
                        )
                    }

                    composable(
                        route = "detail/{itemId}",
                        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                        DetailScreen(
                            itemId = itemId,
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainHostScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    // We maintain a history trace of tabs so that the system back button takes the user to their previously viewed tab.
    val tabHistory = remember { mutableStateListOf(0) }
    val selectedTab = tabHistory.lastOrNull() ?: 0

    val navigateToTab = { tabIndex: Int ->
        if (tabHistory.lastOrNull() != tabIndex) {
            tabHistory.remove(tabIndex)
            tabHistory.add(tabIndex)
        }
    }

    BackHandler(enabled = tabHistory.size > 1) {
        tabHistory.removeLast()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Home (হোম)
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { navigateToTab(0) },
                    icon = { Icon(if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home, contentDescription = "হোম") },
                    label = { Text("হোম", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KarimganjCrimson,
                        selectedTextColor = KarimganjCrimson,
                        indicatorColor = Color(0xFFFFEBEE),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280)
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )
                
                // Online (অনলাইন)
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { navigateToTab(1) },
                    icon = { Icon(if (selectedTab == 1) Icons.Default.Language else Icons.Outlined.Language, contentDescription = "অনলাইন") },
                    label = { Text("অনলাইন", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KarimganjCrimson,
                        selectedTextColor = KarimganjCrimson,
                        indicatorColor = Color(0xFFFFEBEE),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280)
                    ),
                    modifier = Modifier.testTag("nav_item_online")
                )
                
                // Complaint (অভিযোগ)
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { navigateToTab(2) },
                    icon = { Icon(if (selectedTab == 2) Icons.Default.EditNote else Icons.Outlined.EditNote, contentDescription = "অভিযোগ") },
                    label = { Text("অভিযোগ", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KarimganjCrimson,
                        selectedTextColor = KarimganjCrimson,
                        indicatorColor = Color(0xFFFFEBEE),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280)
                    ),
                    modifier = Modifier.testTag("nav_item_complaint")
                )
                
                // Info (তথ্য)
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { navigateToTab(3) },
                    icon = { Icon(if (selectedTab == 3) Icons.Default.Info else Icons.Outlined.Info, contentDescription = "তথ্য") },
                    label = { Text("তথ্য", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KarimganjCrimson,
                        selectedTextColor = KarimganjCrimson,
                        indicatorColor = Color(0xFFFFEBEE),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280)
                    ),
                    modifier = Modifier.testTag("nav_item_info")
                )
                
                // Profile (প্রোফাইল)
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { navigateToTab(4) },
                    icon = { Icon(if (selectedTab == 4) Icons.Default.Person else Icons.Outlined.Person, contentDescription = "প্রোফাইল") },
                    label = { Text("প্রোফাইল", fontSize = 11.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KarimganjCrimson,
                        selectedTextColor = KarimganjCrimson,
                        indicatorColor = Color(0xFFFFEBEE),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280)
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
                    onNavigateToTab = { tabIndex -> navigateToTab(tabIndex) }
                )
                1 -> OnlineServicesScreen()
                2 -> ComplaintScreen()
                3 -> AboutScreen(viewModel = viewModel)
                4 -> ProfileScreen(onCallHelpline = { navigateToTab(0) }) // Back to Home or trigger action
            }
        }
    }
}
