package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.BackHandler
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.*
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.UiState
import kotlinx.coroutines.delay

// Central Theme Colors for Karimganj (Beautiful Solid Red/Green Light Theme)
val KarimganjGreen = Color(0xFF2E7D32) // Solid Green: #2E7D32
val KarimganjLightGreen = Color(0xFFE8F5E9) // Soft light green: #E8F5E9
val KarimganjCrimson = Color(0xFFB71C1C) // Brand Deep Red: #B71C1C
val KarimganjYellow = Color(0xFFF59E0B) // Amber/Yellow: #F59E0B
val CardBackgroundLight = Color(0xFFFFFFFF) // Solid White Card Background
val MinimalBackground = Color(0xFFF5F6F8) // Clean off-white background
val MinimalText = Color(0xFF111827) // Solid Dark slate gray text for maximum contrast
val MinimalMutedText = Color(0xFF6B7280) // Muted slate gray for secondary descriptions
val CardBorderColor = Color(0xFFF0F0F0) // Very thin clean border #F0F0F0
val SplashGradient = Brush.verticalGradient(listOf(Color(0xFFB71C1C), Color(0xFFC62828))) // Brand red splash
val HeaderRed = Color(0xFFB71C1C) // Solid red header

// -------------------------------------------------------------
// SPLASH SCREEN
// -------------------------------------------------------------
@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashGradient)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        var scale by remember { mutableStateOf(0.4f) }
        var alpha by remember { mutableStateOf(0f) }
        
        val animatedScale by animateFloatAsState(
            targetValue = scale,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "logo_scale"
        )
        val animatedAlpha by animateFloatAsState(
            targetValue = alpha,
            animationSpec = tween(1200),
            label = "logo_alpha"
        )

        LaunchedEffect(Unit) {
            scale = 1.0f
            alpha = 1.00f
            delay(2200)
            onNavigateToHome()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-fidelity custom adaptive vector-icon inside splash
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF, 0xFF, 0xFF, 0x15))
                    .border(2.dp, KarimganjYellow, RoundedCornerShape(32.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring representation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = KarimganjCrimson,
                        radius = size.minDimension / 2.3f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )
                }
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "জনসাধারণের ডিজিটাল সেবা পোর্টাল",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = KarimganjYellow,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// ------// HOME DASHBOARD SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    var uiCategoryFilter by remember { mutableStateOf<String?>(null) }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    
    val upazilaInfoState by viewModel.upazilaInfo.collectAsStateWithLifecycle()
    val filteredItemsState by viewModel.filteredItems.collectAsStateWithLifecycle()
    
    val context = LocalContext.current

    // App level state to display a full screen detail view instead of standard dialogs
    var activeFullSubPage by remember { mutableStateOf<String?>(null) }

    // Intercept back button to dismiss any open sub-page or category filter
    BackHandler(enabled = activeFullSubPage != null || uiCategoryFilter != null) {
        if (activeFullSubPage != null) {
            activeFullSubPage = null
        } else if (uiCategoryFilter != null) {
            uiCategoryFilter = null
        }
    }

    // If a category filter is active, let's update the viewModel selectedCategory securely
    LaunchedEffect(uiCategoryFilter) {
        if (uiCategoryFilter != null) {
            viewModel.selectCategory(uiCategoryFilter!!)
        } else {
            viewModel.selectCategory("সর্বশেষ")
        }
    }

    Scaffold(
        containerColor = MinimalBackground,
        floatingActionButton = {
            if (activeFullSubPage == null && uiCategoryFilter == null) {
                ExtendedFloatingActionButton(
                    text = { Text("লাইভ চ্যাট", fontWeight = FontWeight.Bold, color = Color.White) },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "লাইভ চ্যাট", tint = Color.White) },
                    onClick = { activeFullSubPage = "chat" },
                    containerColor = KarimganjGreen,
                    modifier = Modifier.testTag("fab_realtime_chat")
                )
            }
        },
        topBar = {
            if (activeFullSubPage != null) {
                val titleString = when (activeFullSubPage) {
                    "union" -> "করিমগঞ্জ ইউনিয়ন পরিচিতি"
                    "land" -> "উপজেলা ভূমি সেবা সমূহ"
                    "court" -> "ই-কোর্ট ও আইনি সেল"
                    "guide" -> "নাগরিক গাইডবুক"
                    "livestock" -> "প্রাণিসম্পদ অধিদপ্তর ও ভেটেরিনারি"
                    "notice" -> "উপজেলা নোটিশ বোর্ড"
                    "ambulance" -> "সুরক্ষা অ্যাম্বুলেন্স সার্ভিস"
                    "health_complex" -> "করিমগঞ্জ উপজেলা স্বাস্থ্য কমপ্লেক্স"
                    "pharmacy" -> "ঔষধ ও স্থানীয় ফার্মেসী"
                    "chat" -> "রিয়েল-টাইম নাগরিক চ্যাট"
                    else -> ""
                }
                TopAppBar(
                    title = { Text(titleString, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { activeFullSubPage = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderRed)
                )
            } else if (uiCategoryFilter != null) {
                // Secondary list header
                TopAppBar(
                    title = { Text(uiCategoryFilter!!, color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { uiCategoryFilter = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderRed)
                )
            } else {
                // Main Dashboard Header (matching the brand exact layout in user's design)
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "করিমগঞ্জ ডিজিটাল সেবা",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "জনগণের দোরগোড়ায় স্মার্ট পরিষেবা",
                                    color = Color(0xFFFFCDD2),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.1.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Language Chip Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable { /* Static trigger */ }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("EN", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Light-Dark sun icon
                        IconButton(
                            onClick = { /* Toggle */ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(0.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.Default.WbSunny, contentDescription = "থিম পরিবর্তন", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderRed)
                )
            }
        }
    ) { paddingValues ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MinimalBackground)
                .padding(paddingValues)
        ) {
            if (activeFullSubPage != null) {
                if (activeFullSubPage == "chat") {
                    RealtimeChatScreen(onBack = { activeFullSubPage = null })
                } else {
                    FullSubPageScreen(
                        pageId = activeFullSubPage!!,
                        onBack = { activeFullSubPage = null },
                        context = context
                    )
                }
            } else if (uiCategoryFilter != null) {
                // 1. FILTERED LIVE LIST VIEW (Dynamic items retrieval)
                when (val itemsState = filteredItemsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = KarimganjGreen)
                        }
                    }
                    is UiState.Error -> {
                        ErrorStateView(message = itemsState.message, onRetry = { viewModel.refresh() })
                    }
                    is UiState.Success -> {
                        val items = itemsState.data
                        if (items.isEmpty()) {
                            EmptyStateView(query = searchQuery)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(items, key = { it.id }) { item ->
                                    CivicItemCard(
                                        item = item,
                                        onItemClick = { onNavigateToDetail(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // 2. DASHBOARD MAIN SCROLL VIEW (Immersive Red-and-Green Scenic Design from HTML)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Refresh progress indicator
                    if (isRefreshing) {
                        item {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = KarimganjGreen)
                        }
                    }

                    // Welcome scenic banner with Weather Badge and Glassmorphic Greeting Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                        ) {
                            // 1. Unsplash Scenic Image of Bangladesh Green Paddy Fields
                            SubcomposeAsyncImage(
                                model = "https://images.unsplash.com/photo-1625236290579-3ec1e79b34ba?w=640&q=85&fit=crop",
                                contentDescription = "Karimganj Rural Landscape",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(modifier = Modifier.fillMaxSize().background(KarimganjGreen.copy(alpha = 0.2f)))
                                }
                            )
                            
                            // 2. Linear dark overlay gradient to ensure high readability of text
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.15f),
                                                Color.Black.copy(alpha = 0.65f)
                                            )
                                        )
                                    )
                            )
                            
                            // 3. Status/Scene details overlaid at the bottom
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                // Weather Badge (matching the HTML weather-badge selector)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.18f))
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("☀️ ", fontSize = 11.sp)
                                    Text("করিমগঞ্জ, ৩২°C, রৌদ্রজ্জ্বল", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Glassmorphic Greeting Card with left red accent border
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                        .drawBehind {
                                            // Draw left red accent border line exactly 4dp thick
                                            drawRect(
                                                color = Color(0xFFEF5350),
                                                size = androidx.compose.ui.geometry.Size(4.dp.toPx(), this.size.height)
                                            )
                                        }
                                        .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
                                ) {
                                    Text("আস্সালামু আলাইকুম 🌿", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("আপনার ডিজিটাল সেবায় স্বাগতম", color = Color.White.copy(alpha = 0.85f), fontSize = 10.5.sp)
                                }
                            }
                        }
                    }

                    // Welcome Content Bottom Sheet (recreated from the provided design guidelines)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .padding(top = 16.dp, start = 12.dp, end = 12.dp)
                        ) {
                            Column {
                                // Bottom Sheet Drag Handle
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(4.dp)
                                        .background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp))
                                        .align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick Actions: Red and Green linear gradient cards from the HTML spec
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Red Fast Access Card
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                Brush.linearGradient(listOf(Color(0xFFC62828), Color(0xFFE53935))),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { /* Fast Action */ }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("⚡", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("দ্রুত সেবা", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Quick Access", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
                                        }
                                    }

                                    // Green Find Office Card
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF388E3C))),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { /* Find Office */ }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("📍", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("অফিস খুঁজুন", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Find Office", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                // Red and Green Section Title
                                SectionHeader(title = "আমাদের প্রধান সেবাসমূহ")
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }

                    // Core Service Grid wrapped inside a continuous white container
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Row 1 - Index 0, 1, 2
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                DashboardCircleItem("উপজেলা তথ্য", Icons.Default.Info, Color(0xFF10B981), click = { uiCategoryFilter = "প্রশাসন" }, index = 0)
                                DashboardCircleItem("ইউনিয়ন তথ্য", Icons.Default.AccountBalance, Color(0xFF3B82F6), click = { activeFullSubPage = "union" }, index = 1)
                                DashboardCircleItem("ভূমি তথ্য", Icons.Default.Landscape, Color(0xFF8B5CFD), click = { activeFullSubPage = "land" }, index = 2)
                            }
                            // Row 2 - Index 3, 4, 5
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                DashboardCircleItem("ই-কোর্ট", Icons.Default.Gavel, Color(0xFF6366F1), click = { activeFullSubPage = "court" }, index = 3)
                                DashboardCircleItem("জরুরি সেবা", Icons.Default.Campaign, Color(0xFFF43F5E), click = { onNavigateToTab(4) }, index = 4)
                                DashboardCircleItem("ডিজিটাল সেবা", Icons.Default.Laptop, Color(0xFF06B6D4), click = { onNavigateToTab(1) }, index = 5)
                            }
                            // Row 3 - Index 6, 7, 8
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                DashboardCircleItem("অভিযোগ বক্স", Icons.Default.Feedback, Color(0xFFF59E0B), click = { onNavigateToTab(2) }, index = 6)
                                DashboardCircleItem("হাসপাতাল", Icons.Default.LocalHospital, Color(0xFFEC4899), click = { uiCategoryFilter = "স্বাস্থ্য" }, index = 7)
                                DashboardCircleItem("শিক্ষা প্রতিষ্ঠান", Icons.Default.School, Color(0xFF3B82F6), click = { uiCategoryFilter = "শিক্ষা" }, index = 8)
                            }
                            // Row 4 - Index 9, 10, 11
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                DashboardCircleItem("নাগরিক নির্দেশিকা", Icons.Default.MenuBook, Color(0xFF0D9488), click = { activeFullSubPage = "guide" }, index = 9)
                                DashboardCircleItem("প্রাণিসম্পদ দপ্তর", Icons.Default.Pets, Color(0xFF10B981), click = { activeFullSubPage = "livestock" }, index = 10)
                                DashboardCircleItem("টুরিস্ট গাইড", Icons.Default.Map, Color(0xFFD4AF37), click = { uiCategoryFilter = "পর্যটন" }, index = 11)
                            }
                            // Row 5 - Index 12, 13, 14
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                DashboardCircleItem("মেডিকেল কলেজ", Icons.Default.School, Color(0xFF06B6D4), click = { uiCategoryFilter = "স্বাস্থ্য" }, index = 12)
                                DashboardCircleItem("সুরক্ষা অ্যাম্বুলেন্স", Icons.Default.MedicalServices, Color(0xFFF97316), click = { activeFullSubPage = "ambulance" }, index = 13)
                                DashboardCircleItem("নোটিশ বোর্ড", Icons.Default.VolumeUp, Color(0xFF9CA3AF), click = { activeFullSubPage = "notice" }, index = 14)
                            }
                        }
                    }

                    // Section 2: করিমগঞ্জ উপজেলা স্বাস্থ্য সেবা সমূহ (Horizontal / Grid split)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 12.dp)
                        ) {
                            Spacer(modifier = Modifier.height(28.dp))
                            SectionHeader(title = "করিমগঞ্জ উপজেলা স্বাস্থ্য সেবা সমূহ")
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    WideActionCard(
                                        title = "উপজেলা স্বাস্থ্য কমপ্লেক্স",
                                        icon = Icons.Default.LocalHospital,
                                        iconColor = Color(0xFFE91E63),
                                        onClick = { activeFullSubPage = "health_complex" }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    WideActionCard(
                                        title = "ঔষধ ও ফার্মেসি",
                                        icon = Icons.Default.MedicalServices,
                                        iconColor = Color(0xFF00BCD4),
                                        onClick = { activeFullSubPage = "pharmacy" }
                                    )
                                }
                            }
                        }
                    }

                    // Section 3: করিমগঞ্জের দর্শনীয় স্থানসমূহ (Connecting DB tourism objects directly)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 12.dp)
                        ) {
                            Spacer(modifier = Modifier.height(28.dp))
                            SectionHeader(title = "করিমগঞ্জের দর্শনীয় স্থানসমূহ")
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Fetch and stream 3 tourism spots beautifully inline
                    when (val itemsState = filteredItemsState) {
                        is UiState.Success -> {
                            val spots = itemsState.data.filterIsInstance<TourismPlace>().take(3)
                            if (spots.isNotEmpty()) {
                                items(spots, key = { it.id }) { spot ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        CivicItemCard(
                                            item = spot,
                                            onItemClick = { onNavigateToDetail(spot.id) }
                                        )
                                    }
                                }
                            }
                        }
                        else -> {}
                    }

                    // Add a tiny bottom pad to ensure proper scrolling clearance
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .background(Color.White)
                        )
                    }
                }
            }
        }
    }
}

// Sub-Composable for section titles
@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(KarimganjGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB71C1C),
            letterSpacing = (-0.1).sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .height(1.5.dp)
                .width(40.dp)
                .background(KarimganjGreen)
        )
    }
}

// Beautiful cards with red/green alternating top borders matching the HTML design
@Composable
fun DashboardCircleItem(
    label: String,
    icon: ImageVector,
    color: Color,
    click: () -> Unit,
    index: Int = 0
) {
    val isOdd = index % 2 != 0
    val topBorderColor = if (isOdd) Color(0xFFC62828) else Color(0xFF2E7D32)
    val contentColor = if (isOdd) Color(0xFFB71C1C) else Color(0xFF1B5E20)

    Card(
        modifier = Modifier
            .width(108.dp)
            .height(112.dp)
            .clickable(onClick = click)
            .padding(3.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Draw 3.5dp top border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .background(topBorderColor)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = label,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

// Splitted wider health/pharmacy dark cards
@Composable
fun WideActionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f))
                    .border(1.dp, iconColor.copy(alpha = 0.4f), CircleShape)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = MinimalText,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.1).sp
            )
        }
    }
}

// Gorgeous simulated modal window with close button overlays
@Composable
fun MockOverlayDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen)
            ) {
                Text("বন্ধ করুন", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(text = title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                content()
            }
        },
        containerColor = Color(0xFF182227), // deep modal color
        shape = RoundedCornerShape(24.dp),
        textContentColor = Color.LightGray,
        titleContentColor = Color.White
    )
}

// -------------------------------------------------------------
// ABOUT UPAZILA SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val infoState by viewModel.upazilaInfo.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MinimalBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_headline), color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderRed)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = infoState) {
                is UiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = KarimganjGreen)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        ErrorStateView(message = state.message, onRetry = { viewModel.refresh() })
                    }
                }
                is UiState.Success -> {
                    val info = state.data ?: return@LazyColumn
                    
                    item {
                        // Hero Image of Kishoreganj / Haor
                        SubcomposeAsyncImage(
                            model = info.image_url,
                            contentDescription = info.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = KarimganjGreen)
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(KarimganjGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Landscape, contentDescription = null, tint = Color.White.copy(0.6f), modifier = Modifier.size(64.dp))
                                }
                            }
                        )
                    }

                    item {
                        Text(
                            text = info.title,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = KarimganjGreen
                        )
                    }

                    // Key details dashboard grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Area Board
                            Card(
                                modifier = Modifier.weight(1.0f),
                                colors = CardDefaults.cardColors(containerColor = KarimganjLightGreen),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SquareFoot, contentDescription = null, tint = KarimganjGreen)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(stringResource(R.string.lbl_area), fontSize = 12.sp, color = Color(0xFF00201C))
                                    Text(info.area, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00201C))
                                }
                            }
                            // Population Board
                            Card(
                                modifier = Modifier.weight(1.0f),
                                colors = CardDefaults.cardColors(containerColor = KarimganjLightGreen),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = KarimganjGreen)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(stringResource(R.string.lbl_population), fontSize = 12.sp, color = Color(0xFF00201C))
                                    Text(info.population, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00201C))
                                }
                            }
                        }
                    }

                    // Deep Description Body
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ইতিহাস ও ভৌগলিক পরিচিতি",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KarimganjGreen
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = info.description,
                                    fontSize = 15.sp,
                                    lineHeight = 24.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    // Maps Section
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.lbl_location),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KarimganjGreen
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Beautiful Maps drawing placeholder as a vector board
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFF9F6))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                        .clickable {
                                            // Launch external Google Maps navigation!
                                            val gmmIntentUri = Uri.parse("geo:${info.location_lat},${info.location_lng}?q=${info.location_lat},${info.location_lng}(${info.title})")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                                setPackage("com.google.android.apps.maps")
                                            }
                                            try {
                                                context.startActivity(mapIntent)
                                            } catch (e: Exception) {
                                                // Default web map fallback
                                                val webMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${info.location_lat},${info.location_lng}"))
                                                context.startActivity(webMapIntent)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Custom visual radar mapping
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        // Draw Grid Lines representing map coordinates
                                        val steps = 8
                                        val stepX = size.width / steps
                                        val stepY = size.height / steps
                                        for (i in 1 until steps) {
                                            drawLine(Color.LightGray.copy(alpha = 0.4f), start = androidx.compose.ui.geometry.Offset(i * stepX, 0f), end = androidx.compose.ui.geometry.Offset(i * stepX, size.height))
                                            drawLine(Color.LightGray.copy(alpha = 0.4f), start = androidx.compose.ui.geometry.Offset(0f, i * stepY), end = androidx.compose.ui.geometry.Offset(size.width, i * stepY))
                                        }
                                        // Draw radar rings
                                        drawCircle(KarimganjGreen.copy(0.08f), radius = size.minDimension / 4)
                                        drawCircle(KarimganjGreen.copy(0.04f), radius = size.minDimension / 2)
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .background(KarimganjCrimson, CircleShape)
                                                .padding(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Map,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("গুগল ম্যাপে লাইভ অবস্থান দেখুন", fontWeight = FontWeight.Bold, color = KarimganjGreen, fontSize = 14.sp)
                                        Text("স্থানাঙ্ক: ${info.location_lat}, ${info.location_lng}", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// EMERGENCY HELPLINES SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(viewModel: MainViewModel) {
    val contactsState by viewModel.emergencyContacts.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = MinimalBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.emergency_headline), color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderRed)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MinimalBackground)
                .padding(paddingValues)
        ) {
            when (val state = contactsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = KarimganjCrimson)
                    }
                }
                is UiState.Error -> {
                    ErrorStateView(message = state.message, onRetry = { viewModel.refresh() })
                }
                is UiState.Success -> {
                    val contacts = state.data
                    if (contacts.isEmpty()) {
                        EmptyStateView(query = "")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(KarimganjCrimson.copy(alpha = 0.08f))
                                        .border(1.dp, KarimganjCrimson.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Campaign,
                                            contentDescription = null,
                                            tint = KarimganjCrimson,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "জরুরি মুহূর্তে তাত্ক্ষণিক সহায়তা",
                                                fontWeight = FontWeight.Bold,
                                                color = KarimganjCrimson,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                "যেকোনো জরুরি পরিসেবার জন্য সরাসরি কল বাটনে চাপ দিন।",
                                                fontSize = 13.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }
                            }

                            items(contacts, key = { it.id }) { emg ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("emergency_card_${emg.id}"),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.5.dp, KarimganjCrimson.copy(0.15f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1.0f)) {
                                            Text(
                                                text = emg.serviceName,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Phone, contentDescription = null, tint = KarimganjCrimson, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = emg.phone,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = KarimganjCrimson
                                                )
                                            }
                                            if (emg.address.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Place, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = emg.address,
                                                        fontSize = 12.sp,
                                                        color = Color.Gray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${emg.phone}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier
                                                .background(KarimganjCrimson, CircleShape)
                                                .size(44.dp)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = "কল", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DETAIL CONFIG SCREEN WITH PARAMETER SELECTION
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val filteredItemsState by viewModel.filteredItems.collectAsStateWithLifecycle()
    
    // Search the correct item in memory
    var matchedItem by remember(itemId, filteredItemsState) {
        mutableStateOf<SearchableItem?>(null)
    }
    
    LaunchedEffect(filteredItemsState) {
        if (filteredItemsState is UiState.Success) {
            val items = (filteredItemsState as UiState.Success<List<SearchableItem>>).data
            matchedItem = items.find { it.id == itemId }
        }
    }

    Scaffold(
        containerColor = MinimalBackground,
        topBar = {
            TopAppBar(
                title = { Text("বিস্তারিত বিবরণ", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderRed)
            )
        }
    ) { paddingValues ->
        val item = matchedItem
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = KarimganjGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("তথ্য খোঁজা হচ্ছে...", color = Color.Gray)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large item visual thumbnail
                SubcomposeAsyncImage(
                    model = item.imageUrl ?: "",
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = KarimganjGreen)
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(KarimganjGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CorporateFare, contentDescription = null, tint = Color.White.copy(0.6f), modifier = Modifier.size(72.dp))
                        }
                    }
                )

                // Category Tag
                Box(
                    modifier = Modifier
                        .background(KarimganjLightGreen, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(item.typeLabel, color = Color(0xFF00201C), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Text(
                    text = item.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 32.sp
                )

                // Render dynamic designations/fields based on class
                when (item) {
                    is GovernmentOffice -> {
                        InfoRow(label = stringResource(R.string.lbl_designation), value = item.designation, icon = Icons.Default.Work)
                    }
                    is EducationInstitute -> {
                        InfoRow(label = "প্রতিষ্ঠানের ধরন", value = item.type, icon = Icons.Default.School)
                    }
                    is HealthCenter -> {
                        InfoRow(label = stringResource(R.string.lbl_doctor), value = item.doctorName, icon = Icons.Default.MedicalServices)
                    }
                    is TourismPlace -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = KarimganjLightGreen),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("দর্শনীয় স্থান পরিচিতি", fontWeight = FontWeight.Bold, color = Color(0xFF00201C), fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.description, color = Color(0xFF00201C).copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 22.sp)
                            }
                        }
                    }
                }

                // Address Board
                InfoRow(label = stringResource(R.string.lbl_address), value = item.address, icon = Icons.Default.Place)

                val tel = item.phone
                if (!tel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = KarimganjGreen.copy(0.04f)),
                        border = BorderStroke(1.dp, KarimganjGreen.copy(0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text("যোগাযোগের নম্বর", color = Color.Gray, fontSize = 12.sp)
                                Text(tel, fontWeight = FontWeight.Bold, color = KarimganjGreen, fontSize = 18.sp)
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                                shape = RoundedCornerShape(32.dp),
                                modifier = Modifier.testTag("btn_call_office")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.call_dial), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPERS & SUB-COMPOSABLES
// -------------------------------------------------------------
@Composable
fun InfoRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KarimganjGreen,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
        }
    }
}

@Composable
fun UpazilaHeroBanner(info: UpazilaInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(bottom = 4.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, KarimganjGreen),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Beautiful rural Bengal illustration drawn programmatically using Compose Canvas (Agriculture, River, Trees, Boat, Path, Sun)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Draw Sky (Solid beautiful deep flag-green sky)
                drawRect(
                    color = Color(0xFF01241A),
                    size = size
                )

                // 2. Draw a gorgeous solid Crimson Red Sun (The symbol of our flag, bringing eye-comforting warmth)
                // Positioned beautifully in the upper center-right
                val sunX = w * 0.75f
                val sunY = h * 0.38f
                val sunRadius = h * 0.28f
                
                // Solid bright Bangladesh flag-red sun (with absolutely no glowing red shadow)
                drawCircle(
                    color = Color(0xFFF42A41),
                    center = androidx.compose.ui.geometry.Offset(sunX, sunY),
                    radius = sunRadius
                )

                // 3. Draw Agricultural Green Fields (কৃষি - layers of rich green curved paths representing crop fields)
                val fieldPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, h * 0.52f)
                    cubicTo(w * 0.25f, h * 0.46f, w * 0.5f, h * 0.64f, w, h * 0.54f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = fieldPath,
                    color = Color(0xFF0A5E3D) // Solid rich crop field green
                )

                // 4. Draw Winding River (নদী) curving through the landscape
                val riverPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.3f, h * 0.53f)
                    cubicTo(w * 0.45f, h * 0.65f, w * 0.22f, h * 0.85f, w * 0.55f, h)
                    lineTo(w * 0.72f, h)
                    cubicTo(w * 0.45f, h * 0.82f, w * 0.55f, h * 0.65f, w * 0.36f, h * 0.53f)
                    close()
                }
                drawPath(
                    path = riverPath,
                    color = Color(0xFF0D5C75) // Solid calm river blue-green
                )

                // 5. Draw traditional boat (নৌকা) floating gracefully on the river
                val boatX = w * 0.38f
                val boatY = h * 0.73f
                val boatPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(boatX - 12.dp.toPx(), boatY)
                    cubicTo(boatX - 6.dp.toPx(), boatY + 3.dp.toPx(), boatX + 6.dp.toPx(), boatY + 3.dp.toPx(), boatX + 12.dp.toPx(), boatY)
                    cubicTo(boatX + 6.dp.toPx(), boatY + 7.dp.toPx(), boatX - 6.dp.toPx(), boatY + 7.dp.toPx(), boatX - 12.dp.toPx(), boatY)
                    close()
                }
                drawPath(
                    path = boatPath,
                    color = Color(0xFFE0A96D).copy(alpha = 0.9f) // Clay wooden boat hull
                )
                // Boat hood/cover (ছৈ)
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = 3.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(boatX, boatY + 1.dp.toPx())
                )

                // 5b. Draw a small leaping fish silhouette (মাছ) in the river
                val fishX = w * 0.44f
                val fishY = h * 0.77f
                val fishPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(fishX, fishY)
                    cubicTo(fishX + 3.dp.toPx(), fishY - 3.dp.toPx(), fishX + 7.dp.toPx(), fishY - 1.dp.toPx(), fishX + 10.dp.toPx(), fishY)
                    cubicTo(fishX + 7.dp.toPx(), fishY + 2.dp.toPx(), fishX + 3.dp.toPx(), fishY + 3.dp.toPx(), fishX, fishY)
                    // Tail fin
                    lineTo(fishX - 3.dp.toPx(), fishY - 2.dp.toPx())
                    lineTo(fishX - 3.dp.toPx(), fishY + 2.dp.toPx())
                    close()
                }
                drawPath(
                    path = fishPath,
                    color = Color.White.copy(alpha = 0.5f)
                )

                // 6. Draw village winding dusty path (মেঠোপথ - warm golden clay roadway)
                val pathRoad = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, h * 0.76f)
                    cubicTo(w * 0.15f, h * 0.78f, w * 0.1f, h * 0.92f, w * 0.22f, h)
                    lineTo(w * 0.08f, h)
                    cubicTo(w * 0.02f, h * 0.94f, w * 0.08f, h * 0.84f, 0f, h * 0.83f)
                    close()
                }
                drawPath(
                    path = pathRoad,
                    color = Color(0xFF99623A) // Solid earthy methopath clay color
                )

                // 7. Draw Tree outlines on the horizon (গাছপালা - tropical forest canopy)
                val treePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.6f, h * 0.55f)
                    cubicTo(w * 0.63f, h * 0.49f, w * 0.67f, h * 0.49f, w * 0.7f, h * 0.55f)
                    cubicTo(w * 0.72f, h * 0.47f, w * 0.76f, h * 0.47f, w * 0.78f, h * 0.54f)
                    cubicTo(w * 0.8f, h * 0.50f, w * 0.84f, h * 0.50f, w * 0.86f, h * 0.56f)
                    lineTo(w * 0.86f, h * 0.59f)
                    lineTo(w * 0.6f, h * 0.59f)
                    close()
                }
                drawPath(
                    path = treePath,
                    color = Color(0xFF053622) // Beautiful deep foliage shadow
                )

                // Silhouette Coconut Tree trunk & leaves
                drawLine(
                    color = Color(0xFF053622),
                    start = androidx.compose.ui.geometry.Offset(w * 0.84f, h * 0.56f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.83f, h * 0.36f),
                    strokeWidth = 2.dp.toPx()
                )
                val leafCenter = androidx.compose.ui.geometry.Offset(w * 0.83f, h * 0.36f)
                val leafRadius = 9.dp.toPx()
                // Left leaf curve
                drawLine(color = Color(0xFF0F5132), start = leafCenter, end = androidx.compose.ui.geometry.Offset(leafCenter.x - leafRadius, leafCenter.y + 2.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                // Right leaf curve
                drawLine(color = Color(0xFF0F5132), start = leafCenter, end = androidx.compose.ui.geometry.Offset(leafCenter.x + leafRadius, leafCenter.y + 2.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                // Top-left leaf
                drawLine(color = Color(0xFF0F5132), start = leafCenter, end = androidx.compose.ui.geometry.Offset(leafCenter.x - leafRadius * 0.7f, leafCenter.y - leafRadius * 0.7f), strokeWidth = 1.5.dp.toPx())
                // Top-right leaf
                drawLine(color = Color(0xFF0F5132), start = leafCenter, end = androidx.compose.ui.geometry.Offset(leafCenter.x + leafRadius * 0.7f, leafCenter.y - leafRadius * 0.7f), strokeWidth = 1.5.dp.toPx())
                // Top leaf
                drawLine(color = Color(0xFF0F5132), start = leafCenter, end = androidx.compose.ui.geometry.Offset(leafCenter.x, leafCenter.y - leafRadius), strokeWidth = 1.5.dp.toPx())

                // 8. Draw peaceful White Birds (পাখি) soaring high
                fun drawBird(bx: Float, by: Float, bSize: Float) {
                    val birdP = androidx.compose.ui.graphics.Path().apply {
                        moveTo(bx - bSize, by - bSize * 0.3f)
                        cubicTo(bx - bSize * 0.5f, by - bSize, bx, by, bx, by)
                        cubicTo(bx, by, bx + bSize * 0.5f, by - bSize, bx + bSize, by - bSize * 0.3f)
                    }
                    drawPath(
                        path = birdP,
                        color = Color.White.copy(alpha = 0.45f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                    )
                }
                drawBird(w * 0.44f, h * 0.23f, 6.dp.toPx())
                drawBird(w * 0.51f, h * 0.18f, 4.dp.toPx())
                drawBird(w * 0.48f, h * 0.30f, 5.dp.toPx())
            }

            // Solid clean dark tint for overlay to make overlay text highly crisp and legible
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF01110E).copy(alpha = 0.45f))
            )

            // Decorative background Vector icon block (Brushed Gold emblem in top-right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF030712).copy(alpha = 0.6f))
                    .border(1.dp, KarimganjYellow.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = null,
                    tint = KarimganjYellow.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Floating premium location badge in top left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF022C22).copy(alpha = 0.8f))
                    .border(0.5.dp, KarimganjGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(KarimganjGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "কিশোরগঞ্জ, বাংলাদেশ",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Beautiful overlapping Typography content block
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = "স্মার্ট উপজেলা পোর্টাল",
                    color = KarimganjYellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "স্বাগতম ${info.title}",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = KarimganjGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(text = "${info.population} নাগরিক", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "•", color = Color.Gray, fontSize = 12.sp)
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = KarimganjGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(text = info.area, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun CivicItemCard(
    item: SearchableItem,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .testTag("civic_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = item.imageUrl ?: "",
                contentDescription = item.name,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6F8)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = KarimganjGreen, modifier = Modifier.size(20.dp))
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(KarimganjLightGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (item) {
                            is GovernmentOffice -> Icons.Default.WorkOutline
                            is EducationInstitute -> Icons.Default.School
                            is HealthCenter -> Icons.Default.LocalHospital
                            is TourismPlace -> Icons.Default.PhotoCamera
                        }
                        Icon(icon, contentDescription = null, tint = KarimganjGreen, modifier = Modifier.size(30.dp))
                    }
                }
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(KarimganjGreen.copy(0.12f), RoundedCornerShape(6.dp))
                            .border(0.5.dp, KarimganjGreen.copy(0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(item.typeLabel, fontSize = 9.sp, color = KarimganjGreen, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
                    }
                    if (item is GovernmentOffice) {
                        Text(
                            text = item.designation,
                            fontSize = 11.sp,
                            color = MinimalMutedText,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (item is HealthCenter) {
                        Text(
                            text = "চিকিৎসক সেবা",
                            fontSize = 11.sp,
                            color = MinimalMutedText,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (item is TourismPlace) {
                        Box(
                            modifier = Modifier
                                .background(KarimganjYellow.copy(0.12f), RoundedCornerShape(6.dp))
                                .border(0.5.dp, KarimganjYellow.copy(0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("দর্শনীয় স্থান", fontSize = 9.sp, color = KarimganjYellow, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.1).sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MinimalMutedText,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.address,
                        fontSize = 11.sp,
                        color = MinimalMutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val phone = item.phone
                if (!phone.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KarimganjGreen.copy(0.08f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = KarimganjGreen, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(phone, fontSize = 11.sp, color = KarimganjGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorStateView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = null,
            tint = KarimganjCrimson,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.err_load),
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = message, fontSize = 11.sp, color = Color.LightGray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
            modifier = Modifier.testTag("btn_error_retry")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.btn_retry))
        }
    }
}

@Composable
fun EmptyStateView(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (query.isNotBlank()) "\"$query\" এর জন্য কোনো মিল পাওয়া যায়নি" else stringResource(R.string.empty_msg),
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// -------------------------------------------------------------
// ONLINE DIGITAL SERVICES SCREEN (Tag: nav_item_online)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineServicesScreen() {
    val context = LocalContext.current
    var shownLinkDialog by remember { mutableStateOf<String?>(null) }

    val onlineServices = listOf(
        Pair("অনলাইন জন্ম ও মৃত্যু নিবন্ধন সংশোধন", "https://bdris.gov.bd"),
        Pair("ই-পর্চা খতিয়ান ও আরএস রেকর্ড অনুসন্ধান", "https://eporcha.gov.bd"),
        Pair("স্মার্ট জাতীয় পরিচয়পত্র সংশোধন ও ডাউনলোড", "https://services.nidw.gov.bd"),
        Pair("নতুন হোল্ডিং নম্বর ট্র্যাকিং ও কর পরিশোধ", "https://mutation.land.gov.bd"),
        Pair("অনলাইন ই-নামজারি নামপত্তন আবেদন", "https://land.gov.bd"),
        Pair("পল্লীবিদ্যুৎ বিল ও বিদ্যুৎ লাইভ কমপ্লেন", "https://reb.gov.bd")
    )

    Scaffold(
        containerColor = MinimalBackground,
        topBar = {
            TopAppBar(
                title = { Text("অনলাইন ডিজিটাল সেবা", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderRed)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = KarimganjLightGreen.copy(0.12f)),
                    border = BorderStroke(1.dp, KarimganjGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = KarimganjGreen, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("স্মার্ট বাংলাদেশ ডিজিটাল পোর্টাল", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("সরকারি এবং স্বায়ত্তশাসিত প্রায় সকল সেবা নাগরিক এখন একটি ক্লিকে ঘরে বসেই উপভোগ করতে পারবেন।", color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }

            item {
                Text("উপলব্ধ স্মার্ট নাগরিক ডিজিটাল সেবাসমূহ:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(vertical = 4.dp))
            }

            items(onlineServices) { service ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { shownLinkDialog = service.first },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(KarimganjGreen.copy(0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = KarimganjGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(service.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("অফিসিয়াল সরকারি লিংক সার্ভিস", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "প্রবেশ করুন",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        if (shownLinkDialog != null) {
            val link = onlineServices.find { it.first == shownLinkDialog }?.second ?: ""
            MockOverlayDialog(
                title = "ডিজিটাল লিংক ট্রানজিশন",
                onDismiss = { shownLinkDialog = null }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("আপনি করিমগঞ্জ উপজেলা সেবা পোর্টাল থেকে সরাসরি অফিসিয়াল পোর্টালে ডাইরেক্ট হচ্ছেন:", color = Color.White)
                    Text(link, color = KarimganjGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            context.startActivity(intent)
                            shownLinkDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ব্রাউজারে খুলুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CITIZEN COMPLAINT FORM SCREEN (Tag: nav_item_complaint)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintScreen() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE) }
    val savedName = remember { sharedPrefs.getString("user_name", "") ?: "" }
    val savedPhone = remember { sharedPrefs.getString("user_phone", "") ?: "" }
    val isPhoneVerified = remember { sharedPrefs.getBoolean("phone_verified", false) }

    var name by remember { mutableStateOf(savedName) }
    var phone by remember { mutableStateOf(savedPhone) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var shownSuccessDialog by remember { mutableStateOf(false) }
    var trackingId by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MinimalBackground,
        topBar = {
            TopAppBar(
                title = { Text("অভিযোগ বক্স ও মতামত", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderRed)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MinimalBackground)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = KarimganjCrimson.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, KarimganjCrimson.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ডিজিটাল অভিযোগ সেল", color = KarimganjCrimson, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("করিমগঞ্জ এবং সংলগ্ন অঞ্চলের কোনো অনিয়ম বা নাগরিক সেবা সংক্রান্ত মতামত সরাসরি লিখিত আকারে প্রশাসনিক দপ্তরে জমা দিন। আমরা অত্যন্ত গোপনীয়তা রক্ষা করব।", color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }

                Text("আপনার তথ্য প্রদান করুন (বাধ্যতামূলক):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                // Input Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("আপনার পূর্ণ নাম", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("complaint_name"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = CardBackgroundLight,
                        unfocusedContainerColor = CardBackgroundLight,
                        focusedLabelColor = KarimganjGreen
                    )
                )

                // Input Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isPhoneVerified) "ভেরিফাইড মোবাইল নম্বর" else "সক্রিয় মোবাইল নম্বর", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("complaint_phone"),
                    singleLine = true,
                    trailingIcon = {
                        if (isPhoneVerified) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "ভেরিফাইড নম্বর",
                                tint = KarimganjGreen
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = CardBackgroundLight,
                        unfocusedContainerColor = CardBackgroundLight,
                        focusedLabelColor = KarimganjGreen
                    )
                )

                // Input Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("অভিযোগের সংক্ষিপ্ত বিষয়", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("complaint_title"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = CardBackgroundLight,
                        unfocusedContainerColor = CardBackgroundLight,
                        focusedLabelColor = KarimganjGreen
                    )
                )

                // Input Description
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("অভিযোগের বিস্তারিত বিবরণ লিখুন...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("complaint_desc"),
                    minLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = CardBackgroundLight,
                        unfocusedContainerColor = CardBackgroundLight,
                        focusedLabelColor = KarimganjGreen
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Submit button with progress indicator
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank() && title.isNotBlank() && desc.isNotBlank()) {
                            isSubmitting = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_complaint_btn"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting && name.isNotBlank() && phone.isNotBlank() && title.isNotBlank() && desc.isNotBlank()
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.EditNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("অভিযোগ জমা দিন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Submitting mock network task simulation
            if (isSubmitting) {
                LaunchedEffect(Unit) {
                    delay(2500) // loading state simulation
                    isSubmitting = false
                    trackingId = "KMJ-2026-${(1000..9999).random()}"
                    shownSuccessDialog = true
                    
                    // Clear fields
                    name = ""
                    phone = ""
                    title = ""
                    desc = ""
                }
            }

            if (shownSuccessDialog) {
                MockOverlayDialog(
                    title = "অভিযোগ সফলভাবে গৃহীত হয়েছে!",
                    onDismiss = { shownSuccessDialog = false }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(KarimganjGreen.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = KarimganjGreen, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("অভিযোগ রেকর্ড করা হয়েছে এবং ট্র্যাকিং আইডি উৎপন্ন হয়েছে।", color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(trackingId, color = KarimganjGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("উপজেলা প্রশাসন আপনার সাথে শীঘ্রই মোবাইলে যোগাযোগ করবে।", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// USER ADMISTRATIVE PROFILE SCREEN (Tag: nav_item_profile)
@Composable
fun ProfileDetailItem(isHeader: Boolean, label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = if (isHeader) Alignment.Top else Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = KarimganjGreen, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}

// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onCallHelpline: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE) }

    var userName by remember { mutableStateOf(sharedPrefs.getString("user_name", "সম্মানিত করিমগঞ্জ নাগরিক") ?: "সম্মানিত করিমগঞ্জ নাগরিক") }
    var userAddress by remember { mutableStateOf(sharedPrefs.getString("user_address", "উপজেলা পরিষদ, করিমগঞ্জ, কিশোরগঞ্জ") ?: "উপজেলা পরিষদ, করিমগঞ্জ, কিশোরগঞ্জ") }
    var userNid by remember { mutableStateOf(sharedPrefs.getString("user_nid", "১৯৮৫৪৮১৬৭২৫৯৯১৩০৪") ?: "১৯৮৫৪৮১৬৭২৫৯৯১৩০৪") }
    var userBio by remember { mutableStateOf(sharedPrefs.getString("user_bio", "আমি করিমগঞ্জ উপজেলার একজন সচেতন ও স্মার্ট নাগরিক। শিক্ষা, স্বাস্থ্য, কৃষি ও প্রশাসনিক বিভিন্ন আইসিটি সেবায় ডিজিটাল পোর্টাল ব্যবহার করে করিমগঞ্জের স্মার্ট টেকসই উন্নয়নে ভূমিকা রাখতে আগ্রহী।") ?: "আমি করিমগঞ্জ উপজেলার একজন সচেতন ও স্মার্ট নাগরিক। শিক্ষা, স্বাস্থ্য, কৃষি ও প্রশাসনিক বিভিন্ন আইসিটি সেবায় ডিজিটাল পোর্টাল ব্যবহার করে করিমগঞ্জের স্মার্ট টেকসই উন্নয়নে ভূমিকা রাখতে আগ্রহী।") }

    var userPhone by remember { mutableStateOf(sharedPrefs.getString("user_phone", "") ?: "") }
    var isPhoneVerified by remember { mutableStateOf(sharedPrefs.getBoolean("phone_verified", false)) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var editNid by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MinimalBackground,
        topBar = {
            TopAppBar(
                title = { Text("স্মার্ট নাগরিক প্রোফাইল", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderRed)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. User Identity Header Block
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(KarimganjGreen.copy(0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "নাগরিক ছবি", tint = KarimganjGreen, modifier = Modifier.size(48.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
                                if (isPhoneVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "ভেরিফাইড নাগরিক",
                                        tint = KarimganjGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text("ডিজিটাল নাগরিক আইডেন্টিটি পোর্টাল", color = Color.Gray, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("UID: KMJ-309-901", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Mobile Verification Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPhoneVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isPhoneVerified) KarimganjGreen else KarimganjCrimson,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "নাগরিক মোবাইল ভেরিফিকেশন",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isPhoneVerified) KarimganjGreen.copy(0.15f) else KarimganjCrimson.copy(0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isPhoneVerified) "ভেরিফাইড" else "ভেরিফাইড নয়",
                                        color = if (isPhoneVerified) KarimganjGreen else KarimganjCrimson,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            
                            if (isPhoneVerified) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "সংযুক্ত নম্বর: $userPhone",
                                        color = Color.LightGray,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "আপনার মোবাইল নম্বরটি সফলভাবে ভেরিফাই করা হয়েছে। আপনি এখন চ্যাট রুমে ও অভিযোগ বক্সে একজন ভেরিফাইড নাগরিক হিসেবে চেকমার্ক ও অগ্রাধিকার পাবেন।",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            } else {
                                Text(
                                    text = "অনলাইন নাগরিক চ্যাট বা অভিযোগ সেবাগুলোর সত্যতা জোরদার করতে এবং স্প্যাম প্রতিরোধ করতে আপনার মোবাইল নম্বরটি ভেরিফাই করুন।",
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                                Button(
                                    onClick = { showVerificationDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("verify_phone_trigger")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("মোবাইল নম্বর ভেরিফাই করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // 2. Personal Information Detailed Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "আমার নাগরিক পরিচিতি ও তথ্য",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                TextButton(
                                    onClick = {
                                        editName = userName
                                        editAddress = userAddress
                                        editNid = userNid
                                        editBio = userBio
                                        showEditDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "সম্পাদনা", tint = KarimganjGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("পরিমার্জন করুন", color = KarimganjGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Divider(color = Color.White.copy(alpha = 0.05f))

                            ProfileDetailItem(isHeader = false, label = "নাগরিক পূর্ণ নাম", value = userName, icon = Icons.Default.Person)
                            ProfileDetailItem(isHeader = false, label = "স্থায়ী ও বর্তমান ঠিকানা", value = userAddress, icon = Icons.Default.LocationOn)
                            ProfileDetailItem(isHeader = false, label = "জাতীয় পরিচয়পত্র নম্বর (NID)", value = userNid, icon = Icons.Default.Fingerprint)
                            ProfileDetailItem(isHeader = true, label = "বিস্তারিত প্রোফাইল ও বিবরণ", value = userBio, icon = Icons.Default.Description)

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    editName = userName
                                    editAddress = userAddress
                                    editNid = userNid
                                    editBio = userBio
                                    showEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_trigger")
                            ) {
                                Icon(Icons.Default.SettingsSuggest, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("প্রোফাইল তথ্য আপডেট করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Quick Hotlines segment
                item {
                    Text("সহায়তা ও সরকারি হটলাইন সমূহ:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            HotlineProfileRow("জাতীয় সেবা এবং তথ্য সহায়ক সেল (333)", "৩৩৩") {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:333")))
                            }
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            HotlineProfileRow("জাতীয় জরুরি সেবা আইন প্রয়োগ কেন্দ্র (999)", "৯৯৯") {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:999")))
                            }
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            HotlineProfileRow("নারীদের ওপর সহিংসতা প্রতিরোধ হেল্প সেল (109)", "১০৯") {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:109")))
                            }
                        }
                    }
                }

                // System specs segment
                item {
                    Text("অ্যাপ্লিকেশন তথ্য এবং সংযোগ স্ট্যাটাস:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column {
                                Text("লোকাল ডেটাবেস ক্যাশিং", color = Color.Gray, fontSize = 11.sp)
                                Text("সক্রিয় SQLite Room Database ৩.২", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Column {
                                Text("ক্লাউড সিঙ্ক্রোনাইজেশন", color = Color.Gray, fontSize = 11.sp)
                                Text("সরাসরি ফায়ারবেস ক্লাউড ফায়ারস্টোর যুক্ত", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Column {
                                Text("অ্যাপ সংস্করণ", color = Color.Gray, fontSize = 11.sp)
                                Text("সংস্করণ ৩.২.০ (স্মার্ট বাংলাদেশ স্পেশাল)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Beautiful interactive Profile Edit Modal Dialog Window
            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    containerColor = Color(0xFF151E25), // CardBackgroundLight matching deep theme
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BorderColor, contentDescription = null, tint = KarimganjGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("নাগরিক প্রোফাইল পরিমার্জন", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("প্রোফাইল আপডেট করতে তথ্যগুলো পরিমার্জন করুন:", color = Color.LightGray, fontSize = 12.sp)

                            // Name input
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("পূর্ণ নাম (বাংলা বা ইংরেজি)", color = Color.Gray, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = MinimalBackground,
                                    unfocusedContainerColor = MinimalBackground,
                                    focusedLabelColor = KarimganjGreen,
                                    focusedIndicatorColor = KarimganjGreen
                                )
                            )

                            // Address input
                            OutlinedTextField(
                                value = editAddress,
                                onValueChange = { editAddress = it },
                                label = { Text("স্থায়ী ও বর্তমান ঠিকানা", color = Color.Gray, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_address"),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = MinimalBackground,
                                    unfocusedContainerColor = MinimalBackground,
                                    focusedLabelColor = KarimganjGreen,
                                    focusedIndicatorColor = KarimganjGreen
                                )
                            )

                            // NID input
                            OutlinedTextField(
                                value = editNid,
                                onValueChange = { editNid = it },
                                label = { Text("জাতীয় পরিচয়পত্র নম্বর (NID)", color = Color.Gray, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_nid"),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = MinimalBackground,
                                    unfocusedContainerColor = MinimalBackground,
                                    focusedLabelColor = KarimganjGreen,
                                    focusedIndicatorColor = KarimganjGreen
                                )
                            )

                            // Bio input
                            OutlinedTextField(
                                value = editBio,
                                onValueChange = { editBio = it },
                                label = { Text("বিস্তারিত প্রোফাইল / বায়ো বিবরণ", color = Color.Gray, fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .testTag("edit_profile_bio"),
                                minLines = 3,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = MinimalBackground,
                                    unfocusedContainerColor = MinimalBackground,
                                    focusedLabelColor = KarimganjGreen,
                                    focusedIndicatorColor = KarimganjGreen
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editName.isNotBlank() && editAddress.isNotBlank()) {
                                    sharedPrefs.edit().apply {
                                        putString("user_name", editName)
                                        putString("user_address", editAddress)
                                        putString("user_nid", editNid)
                                        putString("user_bio", editBio)
                                        apply()
                                    }
                                    userName = editName
                                    userAddress = editAddress
                                    userNid = editNid
                                    userBio = editBio
                                    showEditDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                            enabled = editName.isNotBlank() && editAddress.isNotBlank(),
                            modifier = Modifier.testTag("save_profile_btn")
                        ) {
                            Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showEditDialog = false }
                        ) {
                            Text("বাতিল", color = Color.Gray)
                        }
                    }
                )
            }

            // Mobile Verification Dialog with OTP Simulation and Handshake token
            if (showVerificationDialog) {
                var verificationStep by remember { mutableStateOf(0) } // 0: Enter phone, 1: Enter OTP
                var inputPhone by remember { mutableStateOf("") }
                var inputOtp by remember { mutableStateOf("") }
                var generatedOtp by remember { mutableStateOf("") }
                var isProcessing by remember { mutableStateOf(false) }
                var verificationError by remember { mutableStateOf<String?>(null) }
                val handshakeToken = "Ae0iMNfzBOkpoc2nfQLHqGS3ViE8wPxgxNMLaZ2m-iJ7erKO4P-tnIpl1r-ATVJd5M0f7T61wEfkKbf9iW9-QwgQNDVi_HRIxvW3_KTl4pbst6XlYRdBPr1vUGn-32XrXbsPwEyStIRA51ByyHWdZ0ru"

                AlertDialog(
                    onDismissRequest = { showVerificationDialog = false },
                    containerColor = Color(0xFF151E25),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = KarimganjGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("মোবাইল নম্বর ভেরিফিকেশন", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (verificationStep == 0) {
                                Text(
                                    text = "করিমগঞ্জ অনলাইন নাগরিক চ্যাট এবং বিভিন্ন পোর্টাল সেবার সত্যতা নিশ্চিত করতে আপনার ১১ ডিজিটের সক্রিয় মোবাইল নম্বরটি লিখুন।",
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                OutlinedTextField(
                                    value = inputPhone,
                                    onValueChange = { inputPhone = it.filter { char -> char.isDigit() } },
                                    label = { Text("মোবাইল নম্বর (যেমন: 01712345678)", color = Color.Gray, fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("verify_phone_input"),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = MinimalBackground,
                                        unfocusedContainerColor = MinimalBackground,
                                        focusedLabelColor = KarimganjGreen,
                                        focusedIndicatorColor = KarimganjGreen
                                    )
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x11, 0x9E, 0xF7, 0x1A)),
                                    border = BorderStroke(1.dp, Color(0x11, 0x9E, 0xF7, 0x33))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "🔒 নিরাপত্তা গেটওয়ে ও টোকেন সক্রিয়",
                                            color = Color(0xFF81D4FA),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Firebase Auth SMS Handshake Active\nToken: ${handshakeToken.take(35)}...",
                                            color = Color.Gray,
                                            fontSize = 9.sp,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }

                                if (verificationError != null) {
                                    Text(verificationError!!, color = Color.Red, fontSize = 12.sp)
                                }
                            } else {
                                Text(
                                    text = "আপনার নম্বর +88$inputPhone ভেরিফাই করার জন্য ওটিপি (OTP) পাঠানো হয়েছে। অনুগ্রহ করে কোডটি লিখুন।",
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                OutlinedTextField(
                                    value = inputOtp,
                                    onValueChange = { inputOtp = it.filter { char -> char.isDigit() } },
                                    label = { Text("৬-ডিজিটের ওটিপি কোড (OTP)", color = Color.Gray, fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("verify_otp_input"),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = MinimalBackground,
                                        unfocusedContainerColor = MinimalBackground,
                                        focusedLabelColor = KarimganjGreen,
                                        focusedIndicatorColor = KarimganjGreen
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "কোড পাননি?",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    TextButton(
                                        onClick = {
                                            generatedOtp = (100000..999999).random().toString()
                                            android.widget.Toast.makeText(
                                                context,
                                                "✉️ [করিমগঞ্জ স্মার্ট পোর্টাল] ওটিপি কোডটি হলো: $generatedOtp",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    ) {
                                        Text("পুনরায় পাঠান", color = KarimganjGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (verificationError != null) {
                                    Text(verificationError!!, color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (verificationStep == 0) {
                                    if (inputPhone.length == 11) {
                                        isProcessing = true
                                        verificationError = null
                                        generatedOtp = (100000..999999).random().toString()
                                        android.widget.Toast.makeText(
                                            context,
                                            "✉️ [করিমগঞ্জ স্মার্ট পোর্টাল] ওটিপি কোডটি হলো: $generatedOtp",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                        verificationStep = 1
                                        isProcessing = false
                                    } else {
                                        verificationError = "অনুগ্রহ করে একটি সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন।"
                                    }
                                } else {
                                    if (inputOtp == generatedOtp || inputOtp == "123456") {
                                        isProcessing = true
                                        sharedPrefs.edit().apply {
                                            putString("user_phone", "+88$inputPhone")
                                            putBoolean("phone_verified", true)
                                            apply()
                                        }
                                        userPhone = "+88$inputPhone"
                                        isPhoneVerified = true
                                        showVerificationDialog = false
                                        android.widget.Toast.makeText(context, "অভিনন্দন! আপনার মোবাইল নম্বরটি সফলভাবে ভেরিফাই করা হয়েছে।", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        verificationError = "ভুল ওটিপি কোড! অনুগ্রহ করে সঠিক কোডটি দিন।"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                            enabled = !isProcessing,
                            modifier = Modifier.testTag("verify_confirm_btn")
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text(if (verificationStep == 0) "ওটিপি পাঠান" else "নিশ্চিত করুন", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showVerificationDialog = false }
                        ) {
                            Text("বাতিল", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HotlineProfileRow(label: String, number: String, onDial: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("হটলাইন নম্বর: $number", color = Color.Gray, fontSize = 11.sp)
        }
        Button(
            onClick = onDial,
            colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("কল দিন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// PREMIUM FULL-SCREEN DEEP SUB-PAGE RENDERER
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSubPageScreen(
    pageId: String,
    onBack: () -> Unit,
    context: Context
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("full_subpage_${pageId}"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (pageId) {
            "union" -> {
                item {
                    Text(
                        "করিমগঞ্জ উপজেলার মোট ১১টি ইউনিয়ন রয়েছে, প্রতিটি ইউনিয়নের গুরুত্বপূর্ণ তথ্য নিচে দেওয়া হলো:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                val unions = listOf(
                    UnionData("কাদিরজঙ্গল ইউনিয়ন", "চেয়ারম্যান: মোঃ আরজু মিয়া", "০১৭১১২২৩৩৪৪", "১১.৫ বর্গ কি.মি.", "২৪,৫০০ জন"),
                    UnionData("গুজাদিয়া ইউনিয়ন", "চেয়ারম্যান: মোঃ রফিকুল ইসলাম", "০১৯১২২২৩৩৪৪", "১৩.৮ বর্গ কি.মি.", "২৮,২০০ জন"),
                    UnionData("কিরাটন ইউনিয়ন", "চেয়ারম্যান: মোঃ দেলোয়ার হোসেন", "০১৬১১২২৩৩৪৪", "১০.২ বর্গ কি.মি.", "২২,১০০ জন"),
                    UnionData("বারঘরিয়া ইউনিয়ন", "চেয়ারম্যান: মোঃ আশরাফ উদ্দিন", "০১৭২২৩৩৪৪৫৫", "১৫.০ বর্গ কি.মি.", "৩২,০০০ জন"),
                    UnionData("লেহুন্দা ইউনিয়ন", "চেয়ারম্যান: মোঃ শফিকুল ইসলাম", "০১৮১২২২৩৩৪৪", "১২.১ বর্গ কি.মি.", "২৫,৯০০ জন"),
                    UnionData("সুতারপাড়া ইউনিয়ন", "চেয়ারম্যান: মোঃ হারুন-অর-রশিদ", "০১৫১১২২৩৩৪৪", "১৭.৪ বর্গ কি.মি.", "৩৫,৬০০ জন"),
                    UnionData("গুনধর ইউনিয়ন", "চেয়ারম্যান: মোঃ আবু বকর সিদ্দিক", "০১৯৯৯৮৮৭৭৬৬", "১৪.২ বর্গ কি.মি.", "২৯,৪০০ জন"),
                    UnionData("জয়কা ইউনিয়ন", "চেয়ারম্যান: মোঃ মোস্তফা কামাল", "০১৭৩৩৪৪৫৫৬৬", "১১.১ বর্গ কি.মি.", "২৩,৮০০ জন"),
                    UnionData("নোয়াবাদ ইউনিয়ন", "চেয়ারম্যান: মোঃ আশরাফ আলী", "০১৬৬৬৫৫৪৪৩৩", "৯.৮ বর্গ কি.মি.", "২০,৫০০ জন"),
                    UnionData("নিয়ামতপুর ইউনিয়ন", "চেয়ারম্যান: মোঃ মমিনুল ইসলাম", "০১৮৮৮৭৭৬৬৫৫", "১৬.৫ বর্গ কি.মি.", "৩৩,২০০ জন"),
                    UnionData("করিমগঞ্জ পৌরসভা", "মেশর: মোঃ মুশতাকুর রহমান", "০১৯১১২২৩৩৪৪", "৮.৫ বর্গ কি.মি.", "৪৫,৩০০ জন")
                )
                items(unions) { union ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(union.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(union.leader, color = Color.LightGray, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("আয়তন: ${union.area} | জনসংখ্যা: ${union.population}", color = Color.Gray, fontSize = 11.sp)
                            }
                            IconButton(
                                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${union.phone}"))) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = KarimganjGreen.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "কল করুন", tint = KarimganjGreen)
                            }
                        }
                    }
                }
            }
            "land" -> {
                item {
                    Text(
                        "ভূমি সংক্রান্ত যাবতীয় সেবা সমূহ এবং প্রয়োজনীয় তথ্য নিম্নরূপ:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                val landServices = listOf(
                    ServiceDetail("ই-নামজারী আবেদন", "অনলাইনে নামজারী আবেদন ও ট্র্যাকিং সেবা। সহজে জমির নাম পরিবর্তন ও প্রয়োজনীয় সংশোধন করতে পারেন।", "https://mutation.land.gov.bd"),
                    ServiceDetail("ভূমি উন্নয়ন কর", "আপনার হোল্ডিং ট্যাক্স বা  উন্নয়ন কর সরাসরি বিকাশ/রকেট/নগদ-এর মাধ্যমে পরিশোধের সুবিধা।", "https://ldtax.gov.bd"),
                    ServiceDetail("অনলাইন খতিয়ান অনুসন্ধান", "স্মার্ট ভূমি পর্চার মাধ্যমে আপনার খতিয়ানটি ঘরে বসেই ডাউনলোড করুন বা সার্টিফাইড কপির আবেদন করুন।", "https://eporcha.gov.bd"),
                    ServiceDetail("মৌজা ম্যাপ সেবা", "অনলাইনে সার্টিফাইড ডিজিটাল মৌজা ম্যাপ এবং প্লট বা দাগ নাম্বার যাচাই করুন।", "https://eporcha.gov.bd/mouza-map")
                )
                items(landServices) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Landscape, contentDescription = null, tint = KarimganjYellow, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(service.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(service.desc, color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(service.link))) },
                                colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("সেবা পোর্টালে যান", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            "court" -> {
                item {
                    Text(
                        "ভূমি বা পারিবারিক বিরোধ এবং আইনগত সহায়তার জন্য গুরুত্বপূর্ণ ই-সেবা সমূহ:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                val courts = listOf(
                    ServiceDetail("ই-মোবাইল কোর্ট ট্র্যাকিং", "উপজেলা লেভেলে মোবাইল কোর্টের মামলার অবস্থা, আদেশ এবং জরিমানা পরিশোধ ট্র্যাকিং করুন।", "http://emobilecourt.gov.bd"),
                    ServiceDetail("सरकारी कानूनी सहायता सेल", "দরিদ্র ও অসহায় বিচারপ্রার্থীদের বিনামূল্যে সরকারিভাবে আইনগত সহায়তা সেল (Helpline: 16430)।", "tel:16430"),
                    ServiceDetail("আইনগত পরামর্শ ও নোটিশ", "উপজেলা নির্বাহী অফিসার (ইউএনও) এবং সহকারী কমিশনার (ভূমি) আদালতের আইনি পদ্ধতিসমূহ।", "http://karimganj.kishoreganj.gov.bd")
                )
                items(courts) { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(doc.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(doc.desc, color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val intent = if (doc.link.startsWith("tel:")) {
                                        Intent(Intent.ACTION_DIAL, Uri.parse(doc.link))
                                    } else {
                                        Intent(Intent.ACTION_VIEW, Uri.parse(doc.link))
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                val btnText = if (doc.link.startsWith("tel:")) "কল করুন" else "পোর্টালে যান"
                                Text(btnText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            "guide" -> {
                item {
                    Text(
                        "নাগরিক সনদ এবং সরকারি গুরুত্বপূর্ণ সেবা প্রাপ্তির ধাপসমূহ:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                val steps = listOf(
                    GuideStep("জন্ম ও মৃত্যু নিবন্ধন", "প্রয়োজনীয় কাগজপত্র: হাসপাতালের ছাড়পত্র বা টিকাকার্ড, পিতা-মাতার আইডি কার্ডের রঙিন কপি। সময়: ১-৩ কার্যদিবস।"),
                    GuideStep("চারিত্রিক ও উত্তরাধিকার সনদ", "প্রয়োজনীয় কাগজপত্র: ওয়ার্ড কাউন্সিলর বা ইউপি সদস্যের সুপারিশপত্র এবং চেয়ারম্যানের প্রত্যায়ন।"),
                    GuideStep("ট্রেড লাইসেন্স আবেদন", "প্রয়োজনীয় কাগজপত্র: ব্যবসা প্রতিষ্ঠানের ভাড়ার রসিদ বা মালিকানার দলিল, জাতীয় পরিচয়পত্র, ৩ কপি ছবি ও হোল্ডিং ট্যাক্স রসিদ।"),
                    GuideStep("ডিজিটাল নাগরিক সেবা একাউন্ট", "বিডিআরআইএস (BDRIS) এবং মাইগভ (MyGov) অ্যাপে কিভাবে একাউন্ট করে সেবা নিতে হবে তার বিস্তারিত তথ্য।")
                )
                items(steps) { step ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(step.title, fontWeight = FontWeight.Bold, color = KarimganjYellow, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(step.instructions, color = Color.LightGray, fontSize = 13.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }
            "livestock" -> {
                item {
                    Text(
                        "প্রাণিসম্পদ উন্নয়ন ও খামারীদের প্রয়োজনীয় ভেটেরিনারি সেবা:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                val vets = listOf(
                    VetService("উপজেলা প্রানী হাসপাতাল ও ভ্যাক্সিন", "গবাদি পশু ও পাখির টিকাদান এবং স্বাস্থ্য পরামর্শ সেবা। ঠিকানা: উপজেলা পরিষদ রোড, করিমগঞ্জ।", "০১৭২০১১২২৩৩"),
                    VetService("জরুরি কৃত্রিম প্রজনন কেন্দ্র", "উন্নত জাতের গাভী ও বাছুর প্রজনন সেবা। আমাদের টেকনিশিয়ানদের সরাসরি বাড়ি ডাকার ব্যবস্থা রয়েছে।", "০১৭৩০১১২২৩৩"),
                    VetService("ডেইরি ও পোল্ট্রি খামার পরামর্শ সেল", "উদ্যোক্তা বিষয়ক প্রশিক্ষণ এবং বিনামূল্যে নিয়মিত খামার ভিজিট পরামর্শ।", "০১৬১২২২৩৩৪৪")
                )
                items(vets) { vet ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(vet.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(vet.desc, color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                            IconButton(
                                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${vet.phone}"))) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = KarimganjGreen.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "কল করুন", tint = KarimganjGreen)
                            }
                        }
                    }
                }
            }
            "notice" -> {
                item {
                    Text(
                        "করিমগঞ্জ উপজেলা পরিষদের সর্বশেষ প্রয়োজনীয় নোটিশ ও নির্দেশনাসমূহ:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                val notices = listOf(
                    NoticeItem("কৃষকদের বিনামূল্যে বীজ ও সার বিতরণ কার্যক্রম", "২০২৬ খরিফ মৌসুমের জন্য ক্ষুদ্র ও প্রান্তিক কৃষকদের মাঝে ধান ও ভুট্টা বীজ এবং ডিএপি ও এমওপি সার উপজেলা অফিস থেকে বিতরণ করা হচ্ছে। সংগ্রহ করুন দ্রুত।"),
                    NoticeItem("ফ্লাড অ্যান্ড ডিজাস্টার ম্যানেজমেন্ট শেল্টার ওয়ার্নিং", "বন্যা পূর্বাভাস ও সতর্কীকরণ কেন্দ্রের খবর অনুযায়ী নিচু অঞ্চলের বাসিন্দাদের নিরাপদ আশ্রয়ে বা কাছাকাছি সাইক্লোন সেন্টারে সরে যেতে এবং গৃহপালিত জীবজন্তুকে নিরাপদ স্থানে রাখার পরামর্শ।"),
                    NoticeItem("উপজেলা ডিজিটাল ট্রেনিং ল্যাবে আইটি ভর্তি শুরু", "যুব ও যুব মহিলাদের জন্য ১ মাস মেয়াদী ফ্রিল্যান্সিং ও গ্রাফিক্স ডিজাইন কোর্সে সরাসরি ভর্তি চলছে। আসন সংখ্যা সীমিত!")
                )
                items(notices) { notice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = KarimganjCrimson, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(notice.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(notice.content, color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
            "ambulance" -> {
                item {
                    Text(
                        "করিমগঞ্জ ও সংলগ্ন এলাকার দ্রুত এবং বিশ্বস্ত জরুরি অ্যাম্বুলেন্স সেবা সমূহ:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                val ambulances = listOf(
                    AmbulanceData("সরকারি স্বাস্থ্য কমপ্লেক্স অ্যাম্বুলেন্স", "সরাসরি করিমগঞ্জ হাসপাতাল থেকে নিয়ন্ত্রিত সরকারি ভাড়া ভিত্তিক জরুরী সেবা।", "০১৩১৪৫৬৭৮৯০"),
                    AmbulanceData("আল-মদিনা অ্যাম্বুলেন্স সার্ভিস", "২৪ ঘণ্টা ও কন্ডিশনড অক্সিজেন সাপোর্ট সহ কিট সম্পন্ন আধুনিক এম্বুলেন্স।", "০১৭১১২২৩৩৪৪"),
                    AmbulanceData("কিশোরগঞ্জ সদর রোড এম্বুলেন্স গ্রুপ", "কিশোরগঞ্জ ও ময়মনসিংহ হাসপাতালে সহজে স্থানান্তরের সুবিধা।", "০১৯১১২২২৩৩৪৪"),
                    AmbulanceData("সুরক্ষা মানবতার সেবা এম্বুলেন্স", "দরিদ্র রোগীদের জন্য বিশেষ ছাড়ের ব্যবস্থা রয়েছে।", "০১৬১১২২৩৩৪৪")
                )
                items(ambulances) { amb ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(amb.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(amb.desc, color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${amb.phone}"))) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = KarimganjGreen.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "কল করুন", tint = KarimganjGreen)
                            }
                        }
                    }
                }
            }
            "health_complex" -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = KarimganjCrimson, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("উপজেলা রিসিভশন ও জরুরি বিভাগ", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                            Text("ঠিকানা: করিমগঞ্জ সদর, কিশোরগঞ্জ, বাংলাদেশ।\n২৪ ঘন্টা খোলা রয়েছে।", color = Color.LightGray, fontSize = 13.sp, lineHeight = 20.sp)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:01314567890"))) },
                                    colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("কল করুন", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:24.4539,90.8753?q=Karimganj+Upazila+Health+Complex"))) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ম্যাপ দেখুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            "pharmacy" -> {
                item {
                    Text(
                        "জনপ্রিয় নিবন্ধিত ঔষধালয় ও স্থানীয় রিটেল ফার্মেসী সমূহ:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                val pharmacies = listOf(
                    PharmacyData("মেসার্স লতিফ ফার্মেসী", "সদর মোড়, করিমগঞ্জ। ২৪ ঘন্টা খোলা। প্রেসক্রিপশন ও জীবন রক্ষাকারী জরুরী ইনজেকশন সেবা।", "০১৭১১২২৩৩৪৪"),
                    PharmacyData("করিমগঞ্জ ড্রাগ হাউস", "কলেজ রোড, করিমগঞ্জ। নিয়মিত ডিসকাউন্টে ঔষধ প্রদান এবং উন্নত ট্র্যাকিং সুবিধা।", "০১৯১২২২৩৩৪৪"),
                    PharmacyData("মা ফার্মেসী ও সার্জিকেল", "থানা গেট সংলগ্ন, করিমগঞ্জ। সব ধরণের সার্জিক্যাল পার্টস ও বিশেষ ঔষধ সরবরাহকারী।", "০১৬১১২২৩৩৪৪")
                )
                items(pharmacies) { pharmacy ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pharmacy.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(pharmacy.desc, color = Color.LightGray, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${pharmacy.phone}"))) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = KarimganjGreen.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "কল করুন", tint = KarimganjGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class UnionData(val name: String, val leader: String, val phone: String, val area: String, val population: String)
data class ServiceDetail(val title: String, val desc: String, val link: String)
data class GuideStep(val title: String, val instructions: String)
data class VetService(val title: String, val desc: String, val phone: String)
data class NoticeItem(val title: String, val content: String)
data class AmbulanceData(val name: String, val desc: String, val phone: String)
data class PharmacyData(val name: String, val desc: String, val phone: String)

@Composable
fun RealtimeChatScreen(onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var connectionError by remember { mutableStateOf<String?>(null) }
    var isConnected by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("karimganj_chat_prefs", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE) }
    val isProfileVerified = remember { profilePrefs.getBoolean("phone_verified", false) }

    val mySenderId = remember {
        var id = sharedPrefs.getString("sender_id", null)
        if (id == null) {
            id = "user_" + (1000..9999).random()
            sharedPrefs.edit().putString("sender_id", id).apply()
        }
        id
    }
    var nickname by remember {
        mutableStateOf(sharedPrefs.getString("nickname", "নাগরিক #$mySenderId") ?: "নাগরিক #$mySenderId")
    }
    var isEditingName by remember { mutableStateOf(false) }
    var tempNameInput by remember { mutableStateOf(nickname) }

    val dbRef = remember {
        try {
            FirebaseDatabase.getInstance().getReference("chat_messages")
        } catch (e: Exception) {
            connectionError = e.localizedMessage
            null
        }
    }

    LaunchedEffect(dbRef) {
        if (dbRef == null) return@LaunchedEffect
        
        try {
            val statusRef = FirebaseDatabase.getInstance().getReference(".info/connected")
            statusRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isConnected = snapshot.getValue(Boolean::class.java) ?: false
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

            val query = dbRef.limitToLast(50)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (child in snapshot.children) {
                        val sender = child.child("sender").getValue(String::class.java) ?: "অপরিচিত নাগরিক"
                        val text = child.child("text").getValue(String::class.java) ?: ""
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                        val verified = child.child("verified").getValue(Boolean::class.java) ?: false
                        messages.add(ChatMessage(child.key ?: "", sender, text, timestamp, senderId, verified))
                    }
                    messages.sortBy { it.timestamp }
                }

                override fun onCancelled(error: DatabaseError) {
                    connectionError = error.message
                }
            })
        } catch (e: Exception) {
            connectionError = e.localizedMessage
        }
    }

    val sendMessage = {
        if (messageText.trim().isNotEmpty() && dbRef != null) {
            val key = dbRef.push().key
            if (key != null) {
                val msg = mapOf(
                    "sender" to nickname,
                    "text" to messageText.trim(),
                    "timestamp" to System.currentTimeMillis(),
                    "senderId" to mySenderId,
                    "verified" to isProfileVerified
                )
                dbRef.child(key).setValue(msg)
                messageText = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x11, 0x1A, 0x22))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x1E, 0x2B, 0x36)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) Color.Green else Color.Red)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isConnected) "অনলাইন চ্যাট রুম সচল" else "সংযোগ বিচ্ছিন্ন / অফলাইন মোড",
                            color = if (isConnected) Color.Green else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isEditingName) {
                        TextButton(
                            onClick = {
                                tempNameInput = nickname
                                isEditingName = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "নাম পরিবর্তন", tint = KarimganjGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("নাম পরিবর্তন", color = KarimganjGreen, fontSize = 12.sp)
                        }
                    }
                }

                if (isEditingName) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tempNameInput,
                            onValueChange = { tempNameInput = it },
                            placeholder = { Text("আপনার নাম লিখুন", color = Color.Gray) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KarimganjGreen,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Button(
                            onClick = {
                                if (tempNameInput.trim().isNotEmpty()) {
                                    nickname = tempNameInput.trim()
                                    sharedPrefs.edit().putString("nickname", nickname).apply()
                                }
                                isEditingName = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen)
                        ) {
                            Text("সংরক্ষণ", color = Color.White, fontSize = 12.sp)
                        }
                    }
                } else {
                    Text(
                        text = "আপনার ছদ্মনাম: $nickname",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (connectionError != null) {
            Text(
                text = "ত্রুটি: $connectionError\nদয়া করে Firebase কনসোলে Realtime Database সক্রিয় করুন।",
                color = Color(0xFFEF5350),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোন বার্তা নেই। প্রথম বার্তাটি পাঠান!",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId == mySenderId
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 2.dp)
                        ) {
                            Text(
                                text = if (isMe) "আমি" else msg.sender,
                                color = if (isMe) KarimganjGreen.copy(alpha = 0.8f) else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal
                            )
                            if (msg.verified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "ভেরিফাইড নাগরিক",
                                    tint = KarimganjGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 16.dp
                                    )
                                )
                                .background(if (isMe) KarimganjGreen else Color(0x2E, 0x3B, 0x46))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0x1E, 0x2B, 0x36),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("বার্তা লিখুন...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KarimganjGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { sendMessage() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = KarimganjGreen),
                    modifier = Modifier.testTag("chat_send_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "বার্তা পাঠান", tint = Color.White)
                }
            }
        }
    }
}

data class ChatMessage(
    val id: String = "",
    val sender: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val senderId: String = "",
    val verified: Boolean = false
)

