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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.compositionLocalOf

data class ChatMessage(
    val id: String = "",
    val sender: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val senderId: String = "",
    val verified: Boolean = false
)

data class UnionData(val name: String, val leader: String, val phone: String, val area: String, val population: String, val website: String = "")
data class ServiceDetail(val title: String, val desc: String, val link: String)
data class GuideStep(val title: String, val instructions: String)
data class VetService(val title: String, val desc: String, val phone: String)
data class NoticeItem(val title: String, val content: String)
data class AmbulanceData(val name: String, val desc: String, val phone: String)
data class PharmacyData(val name: String, val desc: String, val phone: String)

val LocalThemeState = compositionLocalOf { false }

// Central Theme Colors for Karimganj (Beautiful Solid Red/Green Light Theme)
val KarimganjGreen = Color(0xFF2E7D32) // Solid Green: #2E7D32
val KarimganjCrimson = Color(0xFFB71C1C) // Brand Deep Red: #B71C1C
val KarimganjYellow = Color(0xFFF59E0B) // Amber/Yellow: #F59E0B

val KarimganjLightGreen: Color
    @Composable
    get() = if (LocalThemeState.current) Color(0xFF14532D).copy(alpha = 0.45f) else Color(0xFFE8F5E9)

val CardBackgroundLight: Color
    @Composable
    get() = if (LocalThemeState.current) Color(0xFF1E293B) else Color(0xFFFFFFFF)

val MinimalBackground: Color
    @Composable
    get() = if (LocalThemeState.current) Color(0xFF0F172A) else Color(0xFFF5F6F8)

val MinimalText: Color
    @Composable
    get() = if (LocalThemeState.current) Color(0xFFF9FAFB) else Color(0xFF111827)

val MinimalMutedText: Color
    @Composable
    get() = if (LocalThemeState.current) Color(0xFF9CA3AF) else Color(0xFF6B7280)

val CardBorderColor: Color
    @Composable
    get() = if (LocalThemeState.current) Color(0xFF334155) else Color(0xFFF0F0F0)

val SplashGradient = Brush.verticalGradient(listOf(Color(0xFFB71C1C), Color(0xFFC62828))) // Brand red splash

val HeaderRed: Color
    @Composable
    get() = if (LocalThemeState.current) Color(0xFF991B1B) else Color(0xFFB71C1C)

@Composable
fun WavingBangladeshFlag(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "flag_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Deep emerald background
        drawRect(
            color = Color(0xFF0B2D24)
        )

        // Soft, muted flag green
        drawRect(
            color = Color(0xFF1E6F58).copy(alpha = pulseAlpha * 0.45f)
        )

        // Draw standard red circle of Bangladesh's flag (slightly offset to the left at 42% width)
        val circleCenterX = width * 0.42f
        val circleCenterY = height / 2f
        val circleRadius = height * 0.33f

        drawCircle(
            color = Color(0xFFE05364).copy(alpha = pulseAlpha * 0.5f),
            radius = circleRadius,
            center = androidx.compose.ui.geometry.Offset(circleCenterX, circleCenterY)
        )
    }
}

@Composable
fun WavingFlagIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "flag_icon_pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Text(
        text = "🇧🇩",
        fontSize = 18.sp,
        modifier = Modifier
            .graphicsLayer(
                rotationZ = rotation,
                scaleX = scale,
                scaleY = scale
            )
    )
}

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
    val uiCategoryFilter by viewModel.uiCategoryFilter.collectAsStateWithLifecycle()
    val activeFullSubPage by viewModel.activeFullSubPage.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    
    val upazilaInfoState by viewModel.upazilaInfo.collectAsStateWithLifecycle()
    val filteredItemsState by viewModel.filteredItems.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    var isAdminAreaExpanded by remember { mutableStateOf(false) }

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
                    onClick = { viewModel.setActiveFullSubPage("chat") },
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
                        IconButton(onClick = { viewModel.setActiveFullSubPage(null) }) {
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
                        IconButton(onClick = { viewModel.setUiCategoryFilter(null) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান", tint = Color.White)
                        }
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
                // if (activeFullSubPage == "chat") {
                //     RealtimeChatScreen(viewModel = viewModel, onBack = { viewModel.setActiveFullSubPage(null) })
                // } else {
                //     FullSubPageScreen(
                //         pageId = activeFullSubPage!!,
                //         onBack = { viewModel.setActiveFullSubPage(null) },
                //         context = context
                //     )
                // }
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
                                if (uiCategoryFilter == "প্রশাসন") {
                                    val officers = items.filter { it is GovernmentOffice && !it.id.startsWith("office_staff_") && !it.id.startsWith("office_union_") }
                                    val staff = items.filter { it is GovernmentOffice && it.id.startsWith("office_staff_") && !it.id.startsWith("office_union_") }
                                    val otherItems = items.filter { it !is GovernmentOffice && !it.id.startsWith("office_union_") }

                                    if (officers.isNotEmpty()) {
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 16.dp, bottom = 8.dp)
                                                    .background(
                                                        color = Color(0xFFB71C1C).copy(alpha = 0.05f),
                                                        shape = RoundedCornerShape(12.dp)
                                                     )
                                                    .border(
                                                        border = BorderStroke(1.dp, Color(0xFFB71C1C).copy(alpha = 0.12f)),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(Color(0xFFB71C1C), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AdminPanelSettings,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = if (isEnglish) "Chief Administrative Officers" else "প্রধান কর্মকর্তাবৃন্দ",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB71C1C),
                                                        letterSpacing = (-0.1).sp
                                                    )
                                                    Text(
                                                        text = if (isEnglish) "Core leadership of Karimganj Upazila" else "করিমগঞ্জ উপজেলার শীর্ষ প্রশাসনিক নেতৃত্ব",
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                        items(officers, key = { it.id }) { item ->
                                            AdministrationItemCard(
                                                item = item as GovernmentOffice,
                                                isEnglish = isEnglish,
                                                onItemClick = { onNavigateToDetail(item.id) }
                                            )
                                        }
                                    }

                                    if (staff.isNotEmpty()) {
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 16.dp, bottom = 8.dp)
                                                    .background(
                                                        color = Color(0xFF00695C).copy(alpha = 0.05f),
                                                        shape = RoundedCornerShape(12.dp)
                                                     )
                                                    .border(
                                                        border = BorderStroke(1.dp, Color(0xFF00695C).copy(alpha = 0.12f)),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(Color(0xFF00695C), CircleShape),
                                                     contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.People,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = if (isEnglish) "Upazila Parishad Staff" else "উপজেলা পরিষদের কর্মচারীবৃন্দ (পিডিএফ ডাটা)",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF00695C),
                                                        letterSpacing = (-0.1).sp
                                                    )
                                                    Text(
                                                        text = if (isEnglish) "Support staff database and details" else "সহায়ক কর্মকর্তা ও কর্মচারী তালিকা",
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                        items(staff, key = { it.id }) { item ->
                                            AdministrationItemCard(
                                                item = item as GovernmentOffice,
                                                isEnglish = isEnglish,
                                                onItemClick = { onNavigateToDetail(item.id) }
                                            )
                                        }
                                    }

                                    if (otherItems.isNotEmpty()) {
                                        items(otherItems, key = { it.id }) { item ->
                                            CivicItemCard(
                                                item = item,
                                                isEnglish = isEnglish,
                                                onItemClick = { onNavigateToDetail(item.id) }
                                            )
                                        }
                                    }
                                } else {
                                    items(items, key = { it.id }) { item ->
                                        CivicItemCard(
                                            item = item,
                                            isEnglish = isEnglish,
                                            onItemClick = { onNavigateToDetail(item.id) }
                                        )
                                    }
                                }

                                // Removed union section block from here as per user request
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

                    // Welcome scenic banner with integrated TopAppBar, Weather Badge, and Glassmorphic Greeting Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(310.dp)
                                .background(HeaderRed) // fallback background
                        ) {
                            // 1. Full waving Bangladesh flag behind everything
                            WavingBangladeshFlag(
                                modifier = Modifier.fillMaxSize()
                            )

                            // 2. High-contrast premium scrim gradient for optimal legibility
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.3f),
                                                Color.Black.copy(alpha = 0.05f),
                                                Color.Black.copy(alpha = 0.5f)
                                            )
                                        )
                                    )
                            )

                            // 3. Content layout combining status bar padding, header, and greeting
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                            ) {
                                // Transparent TopAppBar matching the layout
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
                                                    text = if (isEnglish) "Karimganj Digital Sheba" else "করিমগঞ্জ ডিজিটাল সেবা",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 18.sp,
                                                    letterSpacing = (-0.5).sp
                                                )
                                                Text(
                                                    text = if (isEnglish) "Smart Service at Your Doorstep" else "জনগণের দোরগোড়ায় স্মার্ট পরিষেবা",
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
                                                .clickable { viewModel.toggleLanguage() }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(if (isEnglish) "BN" else "EN", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // Light-Dark sun icon
                                        val isDark = LocalThemeState.current
                                        IconButton(
                                            onClick = { viewModel.toggleDarkMode() },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .border(0.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = if (isDark) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                                contentDescription = "থিম পরিবর্তন",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // Status/Scene details overlaid at the bottom
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                                        val currentEmoji = weatherState?.emoji ?: "☀️"
                                        val currentTemp = weatherState?.tempText ?: "৩২"
                                        val currentCondition = weatherState?.conditionText ?: "রৌদ্রজ্জ্বল"
                                        val displayCondition = if (isEnglish) {
                                            when (currentCondition) {
                                                "রৌদ্রজ্জ্বল" -> "Sunny"
                                                "বৃষ্টি" -> "Rainy"
                                                "মেঘলা" -> "Cloudy"
                                                else -> currentCondition
                                            }
                                        } else currentCondition
                                        val displayLoc = if (isEnglish) "Karimganj" else "করিমগঞ্জ"
                                        Text("$currentEmoji ", fontSize = 11.sp)
                                        Text("$displayLoc, $currentTemp°C, $displayCondition", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                        Text(if (isEnglish) "Assalamu Alaikum 🌿" else "আস্সালামু আলাইকুম 🌿", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(if (isEnglish) "Welcome to your digital service" else "আপনার ডিজিটাল সেবায় স্বাগতম", color = Color.White.copy(alpha = 0.85f), fontSize = 10.5.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Welcome Content Bottom Sheet (recreated from the provided design guidelines)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBackgroundLight, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
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
                                SectionHeader(title = if (isEnglish) "Our Main Services" else "আমাদের প্রধান সেবাসমূহ")
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }

                    // Core Service Grid wrapped inside a continuous white container with 3 horizontally scrollable rows for all 15 features
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBackgroundLight)
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Row 1 - Index 0 to 4
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DashboardCircleItem(
                                    label = if (isEnglish) "Administration" else "প্রশাসন",
                                    icon = Icons.Default.AdminPanelSettings,
                                    color = Color(0xFFB71C1C),
                                    click = { viewModel.setUiCategoryFilter("প্রশাসন") },
                                    index = 0
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Union Info" else "ইউনিয়ন তথ্য",
                                    icon = Icons.Default.AccountBalance,
                                    color = Color(0xFF3B82F6),
                                    click = { viewModel.setActiveFullSubPage("union") },
                                    index = 1
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Land Info" else "ভূমি তথ্য",
                                    icon = Icons.Default.Landscape,
                                    color = Color(0xFF8B5CFD),
                                    click = { viewModel.setActiveFullSubPage("land") },
                                    index = 2
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "E-Court" else "ই-কোর্ট",
                                    icon = Icons.Default.Gavel,
                                    color = Color(0xFF6366F1),
                                    click = { viewModel.setActiveFullSubPage("court") },
                                    index = 3
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Emergency" else "জরুরি সেবা",
                                    icon = Icons.Default.Campaign,
                                    color = Color(0xFFF43F5E),
                                    click = { viewModel.selectTab(4) },
                                    index = 4
                                )
                            }

                            // Row 2 - Index 5 to 9
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DashboardCircleItem(
                                    label = if (isEnglish) "Digital Desk" else "ডিজিটাল সেবা",
                                    icon = Icons.Default.Laptop,
                                    color = Color(0xFF06B6D4),
                                    click = { viewModel.selectTab(1) },
                                    index = 5
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Complaint Box" else "অভিযোগ বক্স",
                                    icon = Icons.Default.Feedback,
                                    color = Color(0xFFF59E0B),
                                    click = { viewModel.selectTab(2) },
                                    index = 6
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Hospitals" else "হাসপাতাল",
                                    icon = Icons.Default.LocalHospital,
                                    color = Color(0xFFEC4899),
                                    click = { viewModel.setUiCategoryFilter("স্বাস্থ্য") },
                                    index = 7
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Schools" else "শিক্ষা প্রতিষ্ঠান",
                                    icon = Icons.Default.School,
                                    color = Color(0xFF3B82F6),
                                    click = { viewModel.setUiCategoryFilter("শিক্ষা") },
                                    index = 8
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Citizen Guide" else "নাগরিক নির্দেশিকা",
                                    icon = Icons.Default.MenuBook,
                                    color = Color(0xFF0D9488),
                                    click = { viewModel.setActiveFullSubPage("guide") },
                                    index = 9
                                )
                            }

                            // Row 3 - Index 10 to 14
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DashboardCircleItem(
                                    label = if (isEnglish) "Livestock" else "প্রাণিসম্পদ দপ্তর",
                                    icon = Icons.Default.Pets,
                                    color = Color(0xFF10B981),
                                    click = { viewModel.setActiveFullSubPage("livestock") },
                                    index = 10
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Tourist Guide" else "টুরিস্ট গাইড",
                                    icon = Icons.Default.Map,
                                    color = Color(0xFFD4AF37),
                                    click = { viewModel.setUiCategoryFilter("পর্যটন") },
                                    index = 11
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Medical College" else "মেডিকেল কলেজ",
                                    icon = Icons.Default.School,
                                    color = Color(0xFF06B6D4),
                                    click = { viewModel.setUiCategoryFilter("স্বাস্থ্য") },
                                    index = 12
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Ambulance" else "সুরক্ষা অ্যাম্বুলেন্স",
                                    icon = Icons.Default.MedicalServices,
                                    color = Color(0xFFF97316),
                                    click = { viewModel.setActiveFullSubPage("ambulance") },
                                    index = 13
                                )
                                DashboardCircleItem(
                                    label = if (isEnglish) "Notice Board" else "নোটিশ বোর্ড",
                                    icon = Icons.Default.VolumeUp,
                                    color = Color(0xFF9CA3AF),
                                    click = { viewModel.setActiveFullSubPage("notice") },
                                    index = 14
                                )
                            }
                        }
                    }

                    // Section 2: করিমগঞ্জ উপজেলা স্বাস্থ্য সেবা সমূহ (Horizontal / Grid split)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBackgroundLight)
                                .padding(horizontal = 12.dp)
                        ) {
                            Spacer(modifier = Modifier.height(28.dp))
                            SectionHeader(title = if (isEnglish) "Karimganj Upazila Health Services" else "করিমগঞ্জ উপজেলা স্বাস্থ্য সেবা সমূহ")
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    WideActionCard(
                                        title = if (isEnglish) "Upazila Health Complex" else "উপজেলা স্বাস্থ্য কমপ্লেক্স",
                                        icon = Icons.Default.LocalHospital,
                                        iconColor = Color(0xFFE91E63),
                                        onClick = { viewModel.setActiveFullSubPage("health_complex") }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    WideActionCard(
                                        title = if (isEnglish) "Medicine & Pharmacy" else "ঔষধ ও ফার্মেসি",
                                        icon = Icons.Default.MedicalServices,
                                        iconColor = Color(0xFF00BCD4),
                                        onClick = { viewModel.setActiveFullSubPage("pharmacy") }
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
                                .background(CardBackgroundLight)
                                .padding(horizontal = 12.dp)
                        ) {
                            Spacer(modifier = Modifier.height(28.dp))
                            SectionHeader(title = if (isEnglish) "Tourist Spots of Karimganj" else "করিমগঞ্জের দর্শনীয় স্থানসমূহ")
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
                                            .background(CardBackgroundLight)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        CivicItemCard(
                                            item = spot,
                                            isEnglish = isEnglish,
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
                                .background(CardBackgroundLight)
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
        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
        border = BorderStroke(1.dp, CardBorderColor),
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
                        .background(CardBackgroundLight),
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
// -------------------------------------------------------------
// ABOUT UPAZILA SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    // Expandable states for each section - initialized to true (expanded) by default
    var expandedLocation by remember { mutableStateOf(true) }
    var expandedHistory by remember { mutableStateOf(true) }
    var expandedEducation by remember { mutableStateOf(true) }
    var expandedEconomy by remember { mutableStateOf(true) }
    var expandedPersonalities by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = MinimalBackground, // Dynamic background
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEnglish) "About Karimganj" else "করিমগঞ্জ পরিচিতি",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20)) // Dark Green Top Bar
                )
                // Red Accent line under top bar representing Red & Green combination
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(KarimganjCrimson)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Karimganj Upazila Intro Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF4ED)), // Soft light green-gray tint
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Info Icon circle
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFF2E7D32), shape = CircleShape), // Dark green circle background
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFEDF4ED), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF1B5E20), // Dark green icon
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isEnglish) "Karimganj Upazila Overview" else "করিমগঞ্জ উপজেলা পরিচিতি",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isEnglish) "A traditional administrative region of Kishoreganj district" else "কিশোরগঞ্জ জেলার ঐতিহ্যবাহী একটি প্রশাসনিক অঞ্চল",
                            fontSize = 14.sp,
                            color = Color(0xFF616161),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Card 2: Statistics & Graph
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (isEnglish) "Statistics & Graphs" else "পরিসংখ্যান ও গ্রাফ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isEnglish) "Literacy Rate Comparison (Top 5 Selected Unions)" else "সাক্ষরতার হার তুলনা (ইউনিয়ন ভিত্তিক শীর্ষ ৫ চয়ন)",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Literacy Rate bars (Green)
                        val literacyData = listOf(
                            Triple(if (isEnglish) "Niamatpur" else "নিয়ামতপুর", 0.80f, "48.0%"),
                            Triple(if (isEnglish) "Jafarabad" else "জাফরাবাদ", 0.77f, "46.5%"),
                            Triple(if (isEnglish) "Gujadia" else "গুজাদিয়া", 0.75f, "45.2%"),
                            Triple(if (isEnglish) "Dehunda" else "দেহুন্দা", 0.73f, "44.2%"),
                            Triple(if (isEnglish) "Noabad" else "নোয়াবাদ", 0.72f, "43.8%")
                        )

                        literacyData.forEach { (label, progress, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.width(90.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp)
                                        .background(Color(0xFFF4F6F9), shape = RoundedCornerShape(5.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progress)
                                            .background(Color(0xFF1B5E20), shape = RoundedCornerShape(5.dp)) // Dark Green Bar
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = value,
                                    modifier = Modifier.width(45.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isEnglish) "Population Comparison (Top 5 Unions)" else "জনসংখ্যা তুলনা (শীর্ষ ৫ ইউনিয়ন)",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Population bars (Blue)
                        val populationData = listOf(
                            Triple(if (isEnglish) "Gunadhar" else "গুনধর", 0.85f, "42k"),
                            Triple(if (isEnglish) "Niamatpur" else "নিয়ামতপুর", 0.70f, "35k"),
                            Triple(if (isEnglish) "Gujadia" else "গুজাদিয়া", 0.64f, "32k"),
                            Triple(if (isEnglish) "Kadirjungle" else "কাদিরজঙ্গল", 0.56f, "28k"),
                            Triple(if (isEnglish) "Jafarabad" else "জাফরাবাদ", 0.54f, "27k")
                        )

                        populationData.forEach { (label, progress, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.width(90.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp)
                                        .background(Color(0xFFF4F6F9), shape = RoundedCornerShape(5.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progress)
                                            .background(Color(0xFF2196F3), shape = RoundedCornerShape(5.dp)) // Blue Bar
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = value,
                                    modifier = Modifier.width(45.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: অবস্থান ও আয়তন (Location & Area)
            item {
                ExpandableInfoCard(
                    title = if (isEnglish) "Location & Area" else "অবস্থান ও আয়তন",
                    subtitle = if (isEnglish) "Second largest among 13 Upazilas of Kishoreganj." else "কিশোরগঞ্জ জেলার ১৩টি উপজেলার মধ্যে দ্বিতীয় বৃহত্তম উপজেলা।",
                    icon = Icons.Default.Map,
                    expanded = expandedLocation,
                    onExpandToggle = { expandedLocation = !expandedLocation }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CheckmarkPoint(if (isEnglish) "North: Tarail Upazila" else "উত্তরে: তাড়াইল উপজেলা")
                        CheckmarkPoint(if (isEnglish) "South: Nikli & Katiadi Upazila" else "দক্ষিণে: নিকলী ও কটিয়াদী উপজেলা")
                        CheckmarkPoint(if (isEnglish) "East: Itna & Mithamain Upazila" else "পূর্বে: ইটনা ও মিঠামইন উপজেলা")
                        CheckmarkPoint(if (isEnglish) "West: Kishoreganj Sadar Upazila" else "পশ্চিমে: কিশোরগঞ্জ সদর উপজেলা")
                        CheckmarkPoint(if (isEnglish) "Total Area: 200.52 sq km (77.42 sq mi)" else "মোট আয়তন: ২০০.৫২ বর্গকিমি (৭৭.৪২ বর্গমাইল)")
                    }
                }
            }

            // Card 5: ইতিহাস ও ঐতিহ্য (History & Heritage)
            item {
                ExpandableInfoCard(
                    title = if (isEnglish) "History & Heritage" else "ইতিহাস ও ঐতিহ্য",
                    subtitle = if (isEnglish) "Historically, the name Jangalbari is very well known." else "ঐতিহাসিকভাবে করিমগঞ্জের জঙ্গলবাড়ি নামটি অত্যন্ত সুপরিচিত।",
                    icon = Icons.Default.History,
                    expanded = expandedHistory,
                    onExpandToggle = { expandedHistory = !expandedHistory }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CheckmarkPoint(if (isEnglish) "It was the second capital of Isha Khan, the leader of Bengal's Baro-Bhuyans." else "এটি বাংলার বারো ভূঁইয়ার প্রধান ঈশা খাঁর দ্বিতীয় রাজধানী ছিল।")
                        CheckmarkPoint(if (isEnglish) "Palace, Shahi Mosque, and Isha Khan's Fort are located in Jangalbari as archaeological sites." else "জঙ্গলবাড়িতে প্রত্নতাত্ত্বিক নিদর্শন হিসেবে প্রাসাদ, শাহী মসজিদ ও ঈশা খাঁর দুর্গ অবস্থিত।")
                        CheckmarkPoint(if (isEnglish) "Karimganj Thana was formed in 1909 and upgraded to an Upazila in 1983." else "১৯০৯ সালে করিমগঞ্জ থানা গঠিত হয় এবং ১৯৮৩ সালে উপজেলায় রূপান্তর করা হয়।")
                    }
                }
            }

            // Card 6: শিক্ষা প্রতিষ্ঠান (Educational Institutions)
            item {
                ExpandableInfoCard(
                    title = if (isEnglish) "Educational Institutions" else "শিক্ষা প্রতিষ্ঠান",
                    subtitle = if (isEnglish) "There are several ancient and well-known institutions." else "উপজেলায় বেশ কিছু প্রাচীন ও সুপরিচিত শিক্ষা প্রতিষ্ঠান রয়েছে।",
                    icon = Icons.Default.School,
                    expanded = expandedEducation,
                    onExpandToggle = { expandedEducation = !expandedEducation }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SubSectionHeader(if (isEnglish) "Institute" else "ইনস্টিটিউট")
                        BulletPointItem(if (isEnglish) "Kishoreganj Polytechnic Institute" else "কিশোরগঞ্জ পলিটেকনিক ইনস্টিটিউট")
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        SubSectionHeader(if (isEnglish) "Colleges" else "কলেজ")
                        val colleges = listOf(
                            if (isEnglish) "Karimganj Government College" else "করিমগঞ্জ সরকারি মহাবিদ্যালয়",
                            if (isEnglish) "Niamatpur School & College" else "নিয়ামতপুর স্কুল এন্ড কলেজ",
                            if (isEnglish) "Haji Abdul Bari Master College" else "হাজী আব্দুলবারী মাস্টার মহাবিদ্যালয়",
                            if (isEnglish) "Pouro Model College" else "পৌর মডেল কলেজ"
                        )
                        colleges.forEach { BulletPointItem(it) }

                        Spacer(modifier = Modifier.height(8.dp))
                        SubSectionHeader(if (isEnglish) "Secondary Schools" else "মাধ্যমিক বিদ্যালয়")
                        val schools = listOf(
                            if (isEnglish) "Kandail High School (1970)" else "কান্দাইল উচ্চ বিদ্যালয় (১৯৭০)",
                            if (isEnglish) "Nanshree High School (1943)" else "নানশ্রী উচ্চ বিদ্যালয় (১৯৪৩)",
                            if (isEnglish) "Dehunda High School (1970)" else "দেহুন্দা উচ্চ বিদ্যালয় (১৯৭০)",
                            if (isEnglish) "Gujadia Abdul Hakim Secondary School (1950)" else "গুজাদিয়া আব্দুল হাকিম মাধ্যমিক বিদ্যালয় (১৯৫০)",
                            if (isEnglish) "Karimganj Boys & Girls School (1952)" else "করিমগঞ্জ বয়েজ এন্ড গার্লস স্কুল (১৯৫২)",
                            if (isEnglish) "Bhatia High School (1993)" else "ভাটিয়া উচ্চ বিদ্যালয় (১৯৯৩)"
                        )
                        schools.forEach { BulletPointItem(it) }

                        Spacer(modifier = Modifier.height(8.dp))
                        SubSectionHeader(if (isEnglish) "Madrasas" else "মাদ্রাসা")
                        val madrasas = listOf(
                            if (isEnglish) "Urdighi Dakhil Madrasah" else "উরদিঘী দাখিল মাদ্রাসা",
                            if (isEnglish) "Kiraton Islamic Fazil Madrasah" else "কিরাটন ইসলামি ফাজিল মাদ্রাসা",
                            if (isEnglish) "Karimganj Sobhania Fazil Madrasah" else "করিমগঞ্জ সোবহানিয়া ফাজিল মাদ্রাসা"
                        )
                        madrasas.forEach { BulletPointItem(it) }
                    }
                }
            }

            // Card 7: অর্থনীতি (Economy)
            item {
                ExpandableInfoCard(
                    title = if (isEnglish) "Economy" else "অর্থনীতি",
                    subtitle = if (isEnglish) "Karimganj's primary economy relies on agriculture and fisheries." else "করিমগঞ্জের প্রধান অর্থনীতি কৃষি ও মৎস্য নির্ভর।",
                    icon = Icons.Default.TrendingUp,
                    expanded = expandedEconomy,
                    onExpandToggle = { expandedEconomy = !expandedEconomy }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CheckmarkPoint(if (isEnglish) "Major crops: Rice, jute, tomato, green chili, potato, brinjal, and banana." else "প্রধান ফসল: ধান, পাট, টমেটো, কাঁচা মরিচ, আলু, বেগুন ও কলা।")
                        CheckmarkPoint(if (isEnglish) "Fisheries: This region is highly developed in fish resources due to fertile haor and wetlands." else "মৎস্য চাষ: হাওর ও জলাভূমি সমৃদ্ধ হওয়ায় মৎস্য সম্পদে এই অঞ্চল অত্যন্ত উন্নত।")
                    }
                }
            }

            // Card 8: উল্লেখযোগ্য ব্যক্তিত্ব (Notable Personalities)
            item {
                ExpandableInfoCard(
                    title = if (isEnglish) "Notable Personalities" else "উল্লেখযোগ্য ব্যক্তিত্ব",
                    subtitle = if (isEnglish) "Illustrious sons and daughters of this region who contributed in various fields." else "এই জনপদের কৃতী সন্তান যারা বিভিন্ন ক্ষেত্রে অবদান রেখেছেন।",
                    icon = Icons.Default.Star,
                    expanded = expandedPersonalities,
                    onExpandToggle = { expandedPersonalities = !expandedPersonalities }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CheckmarkPoint(if (isEnglish) "Colonel ATM Haidar - Bir Uttam decorated freedom fighter." else "কর্নেল এটিএম হায়দার - বীর উত্তম খেতাবপ্রাপ্ত মুক্তিযোদ্ধা।")
                        CheckmarkPoint(if (isEnglish) "Captain Sitara Begum - Bir Protik decorated female freedom fighter." else "ক্যাপ্টেন সিতারা বেগম - বীর প্রতীক খেতাবপ্রাপ্ত নারী মুক্তিযোদ্ধা।")
                        CheckmarkPoint(if (isEnglish) "Ilias Kanchan - Actor and founder of the 'We Demand Safe Roads' movement." else "ইলিয়াস কাঞ্চন - অভিনেতা ও 'নিরাপদ সড়ক চাই' আন্দোলনের প্রতিষ্ঠাতা।")
                        CheckmarkPoint(if (isEnglish) "Shirin Sharmin Chaudhury - First female Speaker of the National Parliament of Bangladesh." else "শিরীন শারমিন চৌধুরী - বাংলাদেশের জাতীয় সংসদের প্রথম মহিলা স্পীকার।")
                        CheckmarkPoint(if (isEnglish) "Osman Gani - Renowned academic and scientist." else "ওসমান গণি - প্রখ্যাত শিক্ষাবিদ ও বিজ্ঞানী।")
                        CheckmarkPoint(if (isEnglish) "Osman Farruk - Politician and former minister." else "ওসমান ফারুক - রাজনীতিবিদ ও সাবেক মন্ত্রী।")
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableInfoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular icon background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE8F5E9), shape = CircleShape), // Soft light green circle
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32), // Solid Green
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "সংকুচিত করুন" else "প্রসারিত করুন",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (expanded) {
                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun CheckmarkPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF333333),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun SubSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2E7D32),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun BulletPointItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF444444),
            lineHeight = 18.sp
        )
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
                                    colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
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
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    
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
                title = { Text(if (isEnglish) "Detail Information" else "বিস্তারিত বিবরণ", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = if (isEnglish) "Go back" else "ফিরে যান", tint = Color.White)
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
                    Text(if (isEnglish) "Searching information..." else "তথ্য খোঁজা হচ্ছে...", color = Color.Gray)
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
                if (item is TourismPlace) {
                    // Beautiful custom banner for TourismPlace, replacing dummy photos
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(KarimganjGreen, Color(0xFF0F766E))
                                )
                            )
                            .border(1.2.dp, KarimganjYellow.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = KarimganjYellow,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isEnglish) "OFFICIAL TRAVEL GUIDE" else "সরকারি তথ্যকোষ ও ভ্রমণ নির্দেশিকা",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                } else {
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
                }

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
                        InfoRow(label = if (isEnglish) "Institution Type" else "প্রতিষ্ঠানের ধরন", value = item.type, icon = Icons.Default.School)
                    }
                    is HealthCenter -> {
                        InfoRow(label = stringResource(R.string.lbl_doctor), value = item.doctorName, icon = Icons.Default.MedicalServices)
                    }
                    is TourismPlace -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = KarimganjLightGreen),
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(if (isEnglish) "Tourism Place Introduction" else "দর্শনীয় স্থান পরিচিতি", fontWeight = FontWeight.Bold, color = Color(0xFF00201C), fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.description, color = Color(0xFF00201C).copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 22.sp)
                                }
                            }

                            // Google Maps Navigation Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)), // Warm yellow/amber container
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.2.dp, KarimganjYellow.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(KarimganjYellow.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Map,
                                                contentDescription = null,
                                                tint = KarimganjYellow,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (isEnglish) "Google Maps Location" else "গুগল ম্যাপে লোকেশন",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E), // dark amber
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isEnglish) "Get precise directions on map" else "সহজেই ম্যাপের সাহায্যে পৌঁছান",
                                                color = Color(0xFF78350F),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    
                                    Button(
                                        onClick = {
                                            val query = if (item.id == "tour_1") {
                                                "Balikhola Ghat, Karimganj, Kishoreganj"
                                            } else if (item.id == "tour_2") {
                                                "Jangalbari Fort, Karimganj, Kishoreganj"
                                            } else {
                                                "${item.name}, ${item.address}"
                                            }
                                            val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query))
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                                setPackage("com.google.android.apps.maps")
                                            }
                                            try {
                                                context.startActivity(mapIntent)
                                            } catch (e: Exception) {
                                                // Fallback to web browser Google Maps
                                                val webIntent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))
                                                )
                                                context.startActivity(webIntent)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = KarimganjYellow),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("btn_view_map_${item.id}")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isEnglish) "Go" else "ম্যাপ দেখুন",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Directions,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
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
                                Text(if (isEnglish) "Contact Number" else "যোগাযোগের নম্বর", color = Color.Gray, fontSize = 12.sp)
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
fun AdministrationItemCard(
    item: GovernmentOffice,
    isEnglish: Boolean,
    onItemClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Check if the item is a core officer or general staff
    val isStaff = item.id.startsWith("office_staff_")
    
    // Extract email from address if present
    val email = if (item.address.contains("@")) {
        item.address.substringBefore("|").trim()
    } else null
    
    val cleanAddress = if (item.address.contains("@")) {
        item.address.substringAfter("|").trim()
    } else {
        item.address
    }
    
    // Choose beautiful gradient & icon based on designation
    val (gradientColors, icon) = when {
        item.designation.contains("নির্বাহী") -> listOf(Color(0xFFB71C1C), Color(0xFFE53935)) to Icons.Default.AccountBalance
        item.designation.contains("কমিশনার") -> listOf(Color(0xFF1565C0), Color(0xFF1E88E5)) to Icons.Default.LocationOn
        item.designation.contains("চার্জ") || item.designation.contains("OC") -> listOf(Color(0xFF37474F), Color(0xFF546E7A)) to Icons.Default.Lock
        item.designation.contains("কৃষি") -> listOf(Color(0xFF2E7D32), Color(0xFF43A047)) to Icons.Default.Build
        item.designation.contains("সাঁট মুদ্রাক্ষরিক") -> listOf(Color(0xFF00695C), Color(0xFF00897B)) to Icons.Default.Keyboard
        item.designation.contains("চালক") -> listOf(Color(0xFFD84315), Color(0xFFF4511E)) to Icons.Default.PlayArrow
        item.designation.contains("সহায়ক") -> listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA)) to Icons.Default.Person
        item.designation.contains("মালী") -> listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)) to Icons.Default.Favorite
        item.designation.contains("পরিচ্ছন্নতা") -> listOf(Color(0xFF795548), Color(0xFF8D6E63)) to Icons.Default.Refresh
        else -> listOf(Color(0xFF37474F), Color(0xFF455A64)) to Icons.Default.Person
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Left-side modern vertical authority indicator capsule bar
                    val barWidth = 4.dp.toPx()
                    val barHeight = size.height - 32.dp.toPx()
                    drawRoundRect(
                        brush = Brush.verticalGradient(gradientColors),
                        topLeft = Offset(0f, 16.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
                .clickable { onItemClick() }
                .padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Beautiful multi-layered outer avatar box
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    gradientColors[0].copy(alpha = 0.12f),
                                    gradientColors[1].copy(alpha = 0.04f)
                                )
                            )
                        )
                        .border(1.dp, gradientColors[0].copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(11.dp))
                            .background(Brush.verticalGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Badge: Officer vs Staff
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isStaff) Color(0xFFE0F2F1) else Color(0xFFFFEBEE),
                                    RoundedCornerShape(100.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isStaff) Color(0xFFB2DFDB) else Color(0xFFFFCDD2),
                                    shape = RoundedCornerShape(100.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isStaff) Icons.Default.Work else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isStaff) Color(0xFF00796B) else Color(0xFFD32F2F),
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = if (isStaff) {
                                        if (isEnglish) "Parishad Staff" else "উপজেলা কর্মচারী"
                                    } else {
                                        if (isEnglish) "Govt. Officer" else "সরকারি কর্মকর্তা"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStaff) Color(0xFF004D40) else Color(0xFF880E4F),
                                    letterSpacing = 0.1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Office/Staff Name
                    Text(
                        text = item.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Designation Subtitle
                    Text(
                        text = item.designation,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Contact details in beautiful background pills
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Address Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cleanAddress,
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Email Row (if present)
                if (email != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F9FF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$email")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = email,
                            fontSize = 11.5.sp,
                            color = Color(0xFF0369A1),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Direct Call Action Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${item.phone}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // fallback
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Call Directly" else "সরাসরি কল",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 2. Details or Copy Info Action Button
                OutlinedButton(
                    onClick = {
                        onItemClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF334155)),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "More Info" else "বিস্তারিত তথ্য",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CivicItemCard(
    item: SearchableItem,
    isEnglish: Boolean = false,
    onItemClick: () -> Unit
) {
    if (item is TourismPlace) {
        // Special, elegant Tourism Card without dummy photos and beautifully structured travel details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onItemClick)
                .testTag("tourism_card_${item.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)), // Warm organic linen color
            border = BorderStroke(1.2.dp, KarimganjYellow.copy(alpha = 0.35f)), // Custom warm yellow border
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Stylized travel icon/badge (no dummy images!)
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
                                )
                            )
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = KarimganjYellow,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(KarimganjYellow.copy(0.12f), RoundedCornerShape(6.dp))
                                    .border(0.5.dp, KarimganjYellow.copy(0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isEnglish) "Tourism" else "পর্যটন আকর্ষণ",
                                    fontSize = 9.sp,
                                    color = Color(0xFFB45309), // Dark amber
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.3.sp
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(KarimganjGreen.copy(0.08f), RoundedCornerShape(6.dp))
                                    .border(0.5.dp, KarimganjGreen.copy(0.25f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isEnglish) "Travel Guide" else "ভ্রমণ গাইড",
                                    fontSize = 9.sp,
                                    color = KarimganjGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MinimalText,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = KarimganjGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.address,
                                fontSize = 11.sp,
                                color = MinimalMutedText,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Beautifully designed data segment / description of the tourist spot
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F6F0)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFE5E7EB))
                ) {
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = Color(0xFF374151),
                        lineHeight = 18.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Elegant action button at the bottom of the card for high visual appeal and accessibility
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onItemClick,
                        colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isEnglish) "Explore Spot Details" else "দর্শনীয় স্থান বিস্তারিত দেখুন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Generic CivicItemCard for other categories
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
fun OnlineServicesScreen(isEnglish: Boolean) {
    val context = LocalContext.current
    var shownLinkDialog by remember { mutableStateOf<String?>(null) }

    val onlineServices = if (isEnglish) {
        listOf(
            Pair("Online Birth & Death Registration Correction", "https://bdris.gov.bd"),
            Pair("E-Porcha Khatian & RS Record Search", "https://eporcha.gov.bd"),
            Pair("Smart NID Card Correction & Download", "https://services.nidw.gov.bd"),
            Pair("New Holding Number Tracking & Tax Payment", "https://mutation.land.gov.bd"),
            Pair("Online E-Namjari Mutation Application", "https://land.gov.bd"),
            Pair("Palli Bidyut Bill & Live Electricity Complaint", "https://reb.gov.bd")
        )
    } else {
        listOf(
            Pair("অনলাইন জন্ম ও মৃত্যু নিবন্ধন সংশোধন", "https://bdris.gov.bd"),
            Pair("ই-পর্চা খতিয়ান ও আরএস রেকর্ড অনুসন্ধান", "https://eporcha.gov.bd"),
            Pair("স্মার্ট জাতীয় পরিচয়পত্র সংশোধন ও ডাউনলোড", "https://services.nidw.gov.bd"),
            Pair("নতুন হোল্ডিং নম্বর ট্র্যাকিং ও কর পরিশোধ", "https://mutation.land.gov.bd"),
            Pair("অনলাইন ই-নামজারি নামপত্তন আবেদন", "https://land.gov.bd"),
            Pair("পল্লীবিদ্যুৎ বিল ও বিদ্যুৎ লাইভ কমপ্লেন", "https://reb.gov.bd")
        )
    }

    Scaffold(
        containerColor = MinimalBackground,
        topBar = {
            TopAppBar(
                title = { Text(if (isEnglish) "Online Digital Services" else "অনলাইন ডিজিটাল সেবা", color = Color.White, fontWeight = FontWeight.Bold) },
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
                            Text(if (isEnglish) "Smart Bangladesh Digital Portal" else "স্মার্ট বাংলাদেশ ডিজিটাল পোর্টাল", color = KarimganjGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(if (isEnglish) "Citizens can now enjoy almost all government and autonomous services at their doorstep with a single click." else "সরকারি এবং স্বায়ত্তশাসিত প্রায় সকল সেবা নাগরিক এখন একটি ক্লিকে ঘরে বসেই উপভোগ করতে পারবেন।", color = MinimalText, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }

            item {
                Text(if (isEnglish) "Available Smart Citizen Digital Services:" else "উপলব্ধ স্মার্ট নাগরিক ডিজিটাল সেবাসমূহ:", color = Color(0xFF0F5132), fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(vertical = 4.dp))
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
                                Text(service.first, color = MinimalText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(if (isEnglish) "Official Government Service Link" else "অফিসিয়াল সরকারি লিংক সার্ভিস", color = MinimalMutedText, fontSize = 11.sp)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = if (isEnglish) "Enter" else "প্রবেশ করুন",
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
                title = if (isEnglish) "Digital Link Transition" else "ডিজিটাল লিংক ট্রানজিশন",
                onDismiss = { shownLinkDialog = null }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (isEnglish) "You are transitioning directly from Karimganj Upazila Service Portal to the official portal:" else "আপনি করিমগঞ্জ উপজেলা সেবা পোর্টাল থেকে সরাসরি অফিসিয়াল পোর্টালে ডাইরেক্ট হচ্ছেন:", color = Color.White)
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
                        Text(if (isEnglish) "Open in Browser" else "ব্রাউজারে খুলুন", fontWeight = FontWeight.Bold)
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
fun ComplaintScreen(isEnglish: Boolean) {
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
                title = { Text(if (isEnglish) "Complaint Box & Feedback" else "অভিযোগ বক্স ও মতামত", color = Color.White, fontWeight = FontWeight.Bold) },
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
                        Text(if (isEnglish) "Digital Complaint Cell" else "ডিজিটাল অভিযোগ সেল", color = KarimganjCrimson, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(if (isEnglish) "Submit any irregularities or feedback regarding civic services in Karimganj and adjacent areas directly to the administrative office in writing. We will maintain absolute confidentiality." else "করিমগঞ্জ এবং সংলগ্ন অঞ্চলের কোনো অনিয়ম বা নাগরিক সেবা সংক্রান্ত মতামত সরাসরি লিখিত আকারে প্রশাসনিক দপ্তরে জমা দিন। আমরা অত্যন্ত গোপনীয়তা রক্ষা করব।", color = MinimalText, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }

                Text(if (isEnglish) "Provide Your Information (Mandatory):" else "আপনার তথ্য প্রদান করুন (বাধ্যতামূলক):", color = Color(0xFF0F5132), fontWeight = FontWeight.Bold, fontSize = 14.sp)

                // Input Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isEnglish) "Your Full Name" else "আপনার পূর্ণ নাম", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("complaint_name"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MinimalText,
                        unfocusedTextColor = MinimalText,
                        focusedContainerColor = CardBackgroundLight,
                        unfocusedContainerColor = CardBackgroundLight,
                        focusedLabelColor = KarimganjGreen
                    )
                )

                // Input Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isEnglish) (if (isPhoneVerified) "Verified Mobile Number" else "Active Mobile Number") else (if (isPhoneVerified) "ভেরিফাইড মোবাইল নম্বর" else "সক্রিয় মোবাইল নম্বর"), color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("complaint_phone"),
                    singleLine = true,
                    trailingIcon = {
                        if (isPhoneVerified) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = if (isEnglish) "Verified Number" else "ভেরিফাইড নম্বর",
                                tint = KarimganjGreen
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MinimalText,
                        unfocusedTextColor = MinimalText,
                        focusedContainerColor = CardBackgroundLight,
                        unfocusedContainerColor = CardBackgroundLight,
                        focusedLabelColor = KarimganjGreen
                    )
                )

                // Input Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isEnglish) "Brief Subject of Complaint" else "অভিযোগের সংক্ষিপ্ত বিষয়", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("complaint_title"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MinimalText,
                        unfocusedTextColor = MinimalText,
                        focusedContainerColor = CardBackgroundLight,
                        unfocusedContainerColor = CardBackgroundLight,
                        focusedLabelColor = KarimganjGreen
                    )
                )

                // Input Description
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(if (isEnglish) "Write detailed description of complaint..." else "অভিযোগের বিস্তারিত বিবরণ লিখুন...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("complaint_desc"),
                    minLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MinimalText,
                        unfocusedTextColor = MinimalText,
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
                        Text(if (isEnglish) "Submit Complaint" else "অভিযোগ জমা দিন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    title = if (isEnglish) "Complaint Submitted Successfully!" else "অভিযোগ সফলভাবে গৃহীত হয়েছে!",
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
                        Text(if (isEnglish) "The complaint has been recorded and a tracking ID has been generated." else "অভিযোগ রেকর্ড করা হয়েছে এবং ট্র্যাকিং আইডি উৎপন্ন হয়েছে।", color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 14.sp)
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
                        Text(if (isEnglish) "The Upazila administration will contact you on your mobile shortly." else "উপজেলা প্রশাসন আপনার সাথে শীঘ্রই মোবাইলে যোগাযোগ করবে।", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
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
                .background(KarimganjGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = KarimganjGreen, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = MinimalMutedText, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                color = MinimalText,
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
fun ProfileScreen(isEnglish: Boolean, onCallHelpline: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE) }

    val defaultName = if (isEnglish) "Honored Karimganj Citizen" else "সম্মানিত করিমগঞ্জ নাগরিক"
    val defaultAddress = if (isEnglish) "Upazila Parishad, Karimganj, Kishoreganj" else "উপজেলা পরিষদ, করিমগঞ্জ, কিশোরগঞ্জ"
    val defaultNid = if (isEnglish) "19854816725991304" else "১৯৮৫৪৮১৬৭২৫৯৯১৩০৪"
    val defaultBio = if (isEnglish) "I am a conscious and smart citizen of Karimganj Upazila. Interested in playing a role in smart sustainable development of Karimganj using digital portal services." else "আমি করিমগঞ্জ উপজেলার একজন সচেতন ও স্মার্ট নাগরিক। শিক্ষা, স্বাস্থ্য, কৃষি ও প্রশাসনিক বিভিন্ন আইসিটি সেবায় ডিজিটাল পোর্টাল ব্যবহার করে করিমগঞ্জের স্মার্ট টেকসই উন্নয়নে ভূমিকা রাখতে আগ্রহী।"

    var userName by remember { mutableStateOf(sharedPrefs.getString("user_name", defaultName) ?: defaultName) }
    var userAddress by remember { mutableStateOf(sharedPrefs.getString("user_address", defaultAddress) ?: defaultAddress) }
    var userNid by remember { mutableStateOf(sharedPrefs.getString("user_nid", defaultNid) ?: defaultNid) }
    var userBio by remember { mutableStateOf(sharedPrefs.getString("user_bio", defaultBio) ?: defaultBio) }

    LaunchedEffect(isEnglish) {
        val otherName = if (isEnglish) "সম্মানিত করিমগঞ্জ নাগরিক" else "Honored Karimganj Citizen"
        val otherAddress = if (isEnglish) "উপজেলা পরিষদ, করিমগঞ্জ, কিশোরগঞ্জ" else "Upazila Parishad, Karimganj, Kishoreganj"
        val otherNid = if (isEnglish) "১৯৮৫৪৮১৬৭২৫৯৯১৩০৪" else "19854816725991304"
        val otherBio = if (isEnglish) "আমি করিমগঞ্জ উপজেলার একজন সচেতন ও স্মার্ট নাগরিক। শিক্ষা, স্বাস্থ্য, কৃষি ও প্রশাসনিক বিভিন্ন আইসিটি সেবায় ডিজিটাল পোর্টাল ব্যবহার করে করিমগঞ্জের স্মার্ট টেকসই উন্নয়নে ভূমিকা রাখতে আগ্রহী।" else "I am a conscious and smart citizen of Karimganj Upazila. Interested in playing a role in smart sustainable development of Karimganj using digital portal services."

        if (userName == otherName || userName.isBlank() || userName == "সম্মানিত করিমগঞ্জ নাগরিক" || userName == "Honored Karimganj Citizen") {
            userName = defaultName
        }
        if (userAddress == otherAddress || userAddress.isBlank() || userAddress == "উপজেলা পরিষদ, করিমগঞ্জ, কিশোরগঞ্জ" || userAddress == "Upazila Parishad, Karimganj, Kishoreganj") {
            userAddress = defaultAddress
        }
        if (userNid == otherNid || userNid.isBlank() || userNid == "১৯৮৫৪৮১৬৭২৫৯৯১৩০৪" || userNid == "19854816725991304") {
            userNid = defaultNid
        }
        if (userBio == otherBio || userBio.isBlank() || userBio == "আমি করিমগঞ্জ উপজেলার একজন সচেতন ও স্মার্ট নাগরিক। শিক্ষা, স্বাস্থ্য, কৃষি ও প্রশাসনিক বিভিন্ন আইসিটি সেবায় ডিজিটাল পোর্টাল ব্যবহার করে করিমগঞ্জের স্মার্ট টেকসই উন্নয়নে ভূমিকা রাখতে আগ্রহী।" || userBio == "I am a conscious and smart citizen of Karimganj Upazila. Interested in playing a role in smart sustainable development of Karimganj using digital portal services.") {
            userBio = defaultBio
        }
    }

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
                title = { Text(if (isEnglish) "Smart Citizen Profile" else "স্মার্ট নাগরিক প্রোফাইল", color = Color.White, fontWeight = FontWeight.Bold) },
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
                                Icon(Icons.Default.Person, contentDescription = if (isEnglish) "Citizen Photo" else "নাগরিক ছবি", tint = KarimganjGreen, modifier = Modifier.size(48.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(userName, color = MinimalText, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
                            }
                            ProfileDetailItem(isHeader = false, label = if (isEnglish) "National Identity Card Number (NID)" else "জাতীয় পরিচয়পত্র নম্বর (NID)", value = userNid, icon = Icons.Default.CreditCard)
                            ProfileDetailItem(isHeader = false, label = if (isEnglish) "My Personal Bio/Tagline" else "ব্যক্তিগত সংক্ষিপ্ত বিবরণী (বায়ো)", value = userBio, icon = Icons.Default.Info)
                            
                            Divider(color = CardBorderColor)
                            
                            if (isPhoneVerified) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEnglish) "Connected Number: $userPhone" else "সংযুক্ত নম্বর: $userPhone",
                                        color = MinimalText,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = if (isEnglish) "Your mobile number has been successfully verified. You will now receive verified checkmark and priority in the chat room and complaint box." else "আপনার মোবাইল নম্বরটি সফলভাবে ভেরিফাই করা হয়েছে। আপনি এখন চ্যাট রুমে ও অভিযোগ বক্সে একজন ভেরিফাইড নাগরিক হিসেবে চেকমার্ক ও অগ্রাধিকার পাবেন।",
                                    color = MinimalMutedText,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            } else {
                                Text(
                                    text = if (isEnglish) "Verify your mobile number to strengthen the authenticity of online citizen chats or complaint services and prevent spam." else "অনলাইন নাগরিক চ্যাট বা অভিযোগ সেবাগুলোর সত্যতা জোরদার করতে এবং স্প্যাম প্রতিরোধ করতে আপনার মোবাইল নম্বরটি ভেরিফাই করুন।",
                                    color = MinimalMutedText,
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
                                    Text(if (isEnglish) "Verify Mobile Number" else "মোবাইল নম্বর ভেরিফাই করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "Personal Information" else "ব্যক্তিগত তথ্য",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$userName\n$userAddress\nNID: $userNid",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                /*
                // Mobile Verification Dialog with OTP Simulation and Handshake token
                val (verificationStep, setVerificationStep) = remember { mutableStateOf(0) }
                val (inputPhone, setInputPhone) = remember { mutableStateOf("") }
                val (inputOtp, setInputOtp) = remember { mutableStateOf("") }
                val (generatedOtp, setGeneratedOtp) = remember { mutableStateOf("") }
                val (isProcessing, setIsProcessing) = remember { mutableStateOf(false) }
                val (verificationError, setVerificationError) = remember { mutableStateOf<String?>(null) }
                val handshakeToken = "Ae0iMNfzBOkpoc2nfQLHqGS3ViE8wPxgxNMLaZ2m-iJ7erKO4P-tnIpl1r-ATVJd5M0f7T61wEfkKbf9iW9-QwgQNDVi_HRIxvW3_KTl4pbst6XlYRdBPr1vUGn-32XrXbsPwEyStIRA51ByyHWdZ0ru"
                */


                /*
                if (showVerificationDialog) {
                */
                    AlertDialog(
                        onDismissRequest = { showVerificationDialog = false },
                    containerColor = Color(0xFF151E25),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = KarimganjGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(if (isEnglish) "Mobile Number Verification" else "মোবাইল নম্বর ভেরিফিকেশন", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (verificationStep == 0) {
                                Text(
                                    text = if (isEnglish) "Enter your 11-digit active mobile number to verify authenticity for online citizen chat and other portal services." else "করিমগঞ্জ অনলাইন নাগরিক চ্যাট এবং বিভিন্ন পোর্টাল সেবার সত্যতা নিশ্চিত করতে আপনার ১১ ডিজিটের সক্রিয় মোবাইল নম্বরটি প্রবেশ করান।",
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                OutlinedTextField(
                                    value = inputPhone,
                                    onValueChange = { inputPhone = it.filter { char -> char.isDigit() } },
                                    label = { Text(if (isEnglish) "Active Mobile Number (e.g., 01712345678)" else "সক্রিয় মোবাইল নম্বর (যেমন, ০১৭১২৩৪৫৬৭৮)", color = Color.Gray, fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("verify_phone_input"),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = MinimalText,
                                        unfocusedTextColor = MinimalText,
                                        focusedContainerColor = MinimalBackground,
                                        unfocusedContainerColor = MinimalBackground,
                                        focusedLabelColor = KarimganjGreen,
                                        focusedIndicatorColor = KarimganjGreen
                                    )
                                )�্থায়ী ও বর্তমান ঠিকানা", color = Color.Gray, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_address"),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MinimalText,
                                    unfocusedTextColor = MinimalText,
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
                                label = { Text(if (isEnglish) "National Identity Card Number (NID)" else "জাতীয় পরিচয়পত্র নম্বর (NID)", color = Color.Gray, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_nid"),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MinimalText,
                                    unfocusedTextColor = MinimalText,
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
                                label = { Text(if (isEnglish) "Detailed Profile / Bio Description" else "বিস্তারিত প্রোফাইল / বায়ো বিবরণ", color = Color.Gray, fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .testTag("edit_profile_bio"),
                                minLines = 3,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MinimalText,
                                    unfocusedTextColor = MinimalText,
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
                            Text(if (isEnglish) "Save" else "সংরক্ষণ করুন", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showEditDialog = false }
                        ) {
                            Text(if (isEnglish) "Cancel" else "বাতিল", color = Color.Gray)
                        }
                    }
                )
            }


            // End of dialogs area
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
            Text(label, color = MinimalText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("হটলাইন নম্বর: $number", color = MinimalMutedText, fontSize = 11.sp)
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
                    UnionData("১ নং কাদিরজঙ্গল ইউনিয়ন পরিষদ", "চেয়ারম্যান: মো: ফজলুর রহমান", "01731-496642", "৫,৫৮০ একর", "৩৩,৭২৪ জন", "http://kadirjangalup.kishoreganj.gov.bd/"),
                    UnionData("২ নং গুজাদিয়া ইউনিয়ন পরিষদ", "চেয়ারম্যান: মোঃ রফিকুল ইসলাম", "01716-241357", "৬,৫৪২ একর", "৩৬,৭৬৮ জন", "http://gujadiaup.kishoreganj.gov.bd/"),
                    UnionData("৩ নং কিরাটন ইউনিয়ন পরিষদ", "চেয়ারম্যান: মো : ইবাদুর রহমান শামীম", "01989-255499", "১,৭৩১ একর", "৯,৫৭১ জন", "http://kiratonup.kishoreganj.gov.bd/"),
                    UnionData("৪ নং বারঘরিয়া ইউনিয়ন পরিষদ", "চেয়ারম্যান: আয়ুব উদ্দীন", "01736-437903", "২,৫০৬ একর", "১৩,৭০৬ জন", "http://karimgonj.kishoreganj.gov.bd/"),
                    UnionData("৫ নং নিয়ামতপুর ইউনিয়ন পরিষদ", "চেয়ারম্যান: মখদুম কবীর তন্ময়", "01711-143948", "৩,২০২ একর", "২৪,৭৫৮ জন", "http://karimgonj.kishoreganj.gov.bd/"),
                    UnionData("৬ নং দেহুন্দা ইউনিয়ন পরিষদ", "চেয়ারম্যান: মশিউর রহমান", "01716-325430", "২,৩৯৭ একর", "২০,৮৯৬ জন", "http://dehundaup.kishoreganj.gov.bd/"),
                    UnionData("৭ নং সুতারপাড়া ইউনিয়ন পরিষদ", "চেয়ারম্যান: মো : হারুণ অর রশীদ", "01789-568289", "৭,৪৫১ একর", "১৫,৮৪২ জন", "http://sutarparaup.kishoreganj.gov.bd/"),
                    UnionData("৮ নং গুনধর ইউনিয়ন পরিষদ", "চেয়ারম্যান: মোঃ আবুছায়েম রাসেল", "01712-856616", "৬,৫৫৮ একর", "৩০,০৫১ জন", "http://gunodharup.kishoreganj.gov.bd/"),
                    UnionData("৯ নং জয়কা ইউনিয়ন পরিষদ", "চেয়ারম্যান: মো : আশরাফ উদ্দীন", "01741-393843", "৬,৪০৭ একর", "৩৭,০৯৪ জন", "http://joykaup.kishoreganj.gov.bd/"),
                    UnionData("১০ নং জাফরাবাদ ইউনিয়ন পরিষদ", "চেয়ারম্যান: সাইফ উদ্দীন ফকির", "01911-924935", "২,৩৯০ একর", "১৪,৫৩৭ জন", "http://zafrabadup.kishoreganj.gov.bd/"),
                    UnionData("১১ নং নোয়াবাদ ইউনিয়ন পরিষদ", "চেয়ারম্যান: মো : রুহুল আমীন কাজী", "01728-306372", "২,৮৪১ একর", "২৪,০১৬ জন", "http://noabadup.kishoreganj.gov.bd/"),
                    UnionData("করিমগঞ্জ পৌরসভা", "মেয়র: মোঃ মুশতাকুর রহমান", "01911-223344", "৯টি ওয়ার্ড ও ১৭টি মহল্লা", "২৪,২০৯ ভোটার", "http://karimgonj.kishoreganj.gov.bd/")
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
                        if (union.website.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { 
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(union.website))
                                    context.startActivity(webIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ইউনিয়ন পোর্টাল দেখুন", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

data class UnionData(val name: String, val leader: String, val phone: String, val area: String, val population: String, val website: String = "")
data class ServiceDetail(val title: String, val desc: String, val link: String)
data class GuideStep(val title: String, val instructions: String)
data class VetService(val title: String, val desc: String, val phone: String)
data class NoticeItem(val title: String, val content: String)
data class AmbulanceData(val name: String, val desc: String, val phone: String)
data class PharmacyData(val name: String, val desc: String, val phone: String)

@Composable
fun RealtimeChatScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val firebaseMessages = remember { mutableStateListOf<ChatMessage>() }
    var connectionError by remember { mutableStateOf<String?>(null) }
    var isConnected by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPrefs = remember { context.getSharedPreferences("karimganj_chat_prefs", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE) }
    val isProfileVerified = remember { profilePrefs.getBoolean("phone_verified", false) }

    // Chat mode tabs state: 0 = AI Helpdesk, 1 = Public Chatroom
    var selectedTabMode by remember { mutableStateOf(0) }

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

    // Firebase Database Reference
    val dbRef = remember {
        try {
            FirebaseDatabase.getInstance().getReference("chat_messages")
        } catch (e: Exception) {
            connectionError = e.localizedMessage
            null
        }
    }

    // AI Chat History
    val aiMessages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "welcome_msg",
                sender = "করিমগঞ্জ ডিজিটাল সহকারী",
                text = "আসসালামু আলাইকুম! করিমগঞ্জ নাগরিক সেবা সহায়তা কেন্দ্রে আপনাকে স্বাগতম। আমি আপনার ডিজিটাল সহকারী। করিমগঞ্জ উপজেলার যেকোনো নাগরিক সেবা, হটলাইন নম্বর, স্বাস্থ্যসেবা বা অভিযোগ প্রক্রিয়া সম্পর্কে তথ্য পেতে নিচের যেকোনো বাটনে ট্যাপ করুন অথবা সরাসরি আপনার প্রশ্নটি বাংলায় লিখুন।",
                timestamp = System.currentTimeMillis(),
                senderId = "bot",
                verified = true
            )
        )
    }
    var isAiTyping by remember { mutableStateOf(false) }
    var aiTypingText by remember { mutableStateOf("") }

    // Demo Public Chatroom fallback messages
    val publicMessages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "mock_msg_1",
                sender = "আব্দুর রহমান",
                text = "করিমগঞ্জ বাজারে নতুন ড্রেনেজ ব্যবস্থার কারণে এবার বর্ষায় রাস্তায় পানি জমেনি। ধন্যবাদ উপজেলা প্রশাসনকে!",
                timestamp = System.currentTimeMillis() - 7200000,
                senderId = "citizen_1",
                verified = true
            ),
            ChatMessage(
                id = "mock_msg_2",
                sender = "সুমি আক্তার",
                text = "অনলাইন সেবাসমূহ ব্যবহার করে ঘরে বসেই জন্ম নিবন্ধনের আবেদন করেছি, প্রক্রিয়াটা খুবই সহজ ও দ্রুত ছিল।",
                timestamp = System.currentTimeMillis() - 3600000,
                senderId = "citizen_2",
                verified = false
            ),
            ChatMessage(
                id = "mock_msg_3",
                sender = "কামরুল হাসান",
                text = "উপজেলা স্বাস্থ্য কমপ্লেক্সের জরুরি বিভাগের সেবা অনেক উন্নত হয়েছে। গতকাল জরুরি প্রয়োজনে রাত ১২টায় তাৎক্ষণিক ডাক্তার ও অক্সিজেন সহায়তা পেয়েছি।",
                timestamp = System.currentTimeMillis() - 1800000,
                senderId = "citizen_3",
                verified = true
            )
        )
    }
    var isPublicTyping by remember { mutableStateOf(false) }
    var publicTypingName by remember { mutableStateOf("") }

    // Firebase live synchronization
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
                    firebaseMessages.clear()
                    for (child in snapshot.children) {
                        val sender = child.child("sender").getValue(String::class.java) ?: "অপরিচিত নাগরিক"
                        val text = child.child("text").getValue(String::class.java) ?: ""
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                        val verified = child.child("verified").getValue(Boolean::class.java) ?: false
                        firebaseMessages.add(ChatMessage(child.key ?: "", sender, text, timestamp, senderId, verified))
                    }
                    firebaseMessages.sortBy { it.timestamp }
                }

                override fun onCancelled(error: DatabaseError) {
                    connectionError = error.message
                }
            })
        } catch (e: Exception) {
            connectionError = e.localizedMessage
        }
    }

    // AI Helper response generation
    fun getBotResponse(userMessage: String): String {
        val msg = userMessage.lowercase()
        return when {
            msg.contains("অ্যাম্বুলেন্স") || msg.contains("ambulance") || msg.contains("হাসপাতাল") || msg.contains("hospital") || 
            msg.contains("ডাক্তার") || msg.contains("doctor") || msg.contains("স্বাস্থ্য") || msg.contains("চিকিৎসা") || 
            msg.contains("ফার্মেসি") || msg.contains("ঔষধ") -> {
                "🏥 **করিমগঞ্জ উপজেলা স্বাস্থ্যসেবা তথ্য:**\n\n" +
                "*১. করিমগঞ্জ উপজেলা স্বাস্থ্য কমপ্লেক্স:*\n" +
                "- **জরুরি বিভাগ হটলাইন:** ০১৭৩০-১১২২৩৩ (২৪ ঘণ্টা খোলা)\n" +
                "- **জরুরি অ্যাম্বুলেন্স সেবা:** ০১৭৩০-১১২২৩৪, ০১৯১১-২২৩৩৪৪\n\n" +
                "*২. রক্তদান ও মেডিকেল টিম:*\n" +
                "- **করিমগঞ্জ ব্লাড ডোনার্স ক্লাব:** ০১৮১২-৩৪৫৬৭৮\n\n" +
                "*৩. ঔষধ ও ফার্মেসি:*\n" +
                "- করিমগঞ্জ বাজারের প্রধান ফার্মেসিগুলো ২৪ ঘণ্টা সেবা দিয়ে থাকে।\n\n" +
                "আপনার কি নির্দিষ্ট কোনো ডাক্তারের অ্যাপয়েন্টমেন্ট বা স্পেশালিটি সম্পর্কে জানা প্রয়োজন?"
            }
            msg.contains("অভিযোগ") || msg.contains("complain") || msg.contains("নালিশ") || msg.contains("সমস্যা") || 
            msg.contains("বিদ্যুৎ") || msg.contains("ড্রেন") || msg.contains("রাস্তা") || msg.contains("পানি") || 
            msg.contains("লোডশেডিং") || msg.contains("অফিস") -> {
                "📝 **করিমগঞ্জ অভিযোগ ও প্রতিকার সেল:**\n\n" +
                "আমাদের অ্যাপের প্রধান মেনু থেকে সরাসরি **'অভিযোগ' (Complaint)** ট্যাবে গিয়ে আপনি যেকোনো সমস্যা (রাস্তাঘাট, ড্রেনেজ, বিদ্যুৎ বিভ্রাট, অবৈধ দখল ইত্যাদি) সম্পর্কে ছবি এবং বিবরণসহ অভিযোগ দায়ের করতে পারেন।\n\n" +
                "*প্রক্রিয়া:*\n" +
                "১. অ্যাপের নিচের বারে **'অভিযোগ'** আইকনে চাপ দিন।\n" +
                "২. অভিযোগের ধরন নির্বাচন করুন (যেমন: রাস্তা, বিদ্যুৎ, স্বাস্থ্য ইত্যাদি)।\n" +
                "৩. আপনার অভিযোগের বিবরণ লিখুন এবং প্রয়োজনে ছবি আপলোড করুন।\n" +
                "৪. 'অভিযোগ জমা দিন' বাটনে ট্যাপ করুন।\n\n" +
                "আমাদের উপজেলা প্রশাসন ও পৌরসভা টিম প্রতিটি অভিযোগ অত্যন্ত গুরুত্বের সাথে পর্যালোচনা করে দ্রুততম সময়ে সমাধান করে থাকে।"
            }
            msg.contains("ভূমি") || msg.contains("land") || msg.contains("খতিয়ান") || msg.contains("পর্চা") || 
            msg.contains("দলিল") || msg.contains("নামজারী") || msg.contains("মিউটেশন") || msg.contains("খারিজ") -> {
                "🏛️ **করিমগঞ্জ উপজেলা ভূমি সেবা গাইড:**\n\n" +
                "উপজেলা ভূমি অফিস ও ইউনিয়ন তথ্য কেন্দ্রের মাধ্যমে আপনি ডিজিটাল উপায়ে আপনার জমির সকল সেবা গ্রহণ করতে পারেন।\n\n" +
                "*প্রধান ডিজিটাল সেবা সমূহ:*\n" +
                "- **ই-নামজারী (e-Mutation):** অনলাইনে আবেদনের জন্য land.gov.bd পোর্টালে প্রবেশ করুন। সরকারি ফি ১১৫০/- টাকা।\n" +
                "- **ডিজিটাল পর্চা/খতিয়ান অনুসন্ধান:** আপনার আরএস/এসএ খতিয়ান অনুসন্ধান ও কপির জন্য ভিজিট করুন eporcha.gov.bd।\n\n" +
                "*প্রয়োজনীয় কাগজপত্র:*\n" +
                "- মূল দলিলের কপি ও ভায়া দলিল।\n" +
                "- সর্বশেষ দাখিলা (ভূমি উন্নয়ন করের রসিদ)।\n" +
                "- ওয়ারিশন সনদ (প্রযোজ্য ক্ষেত্রে)।"
            }
            msg.contains("ইউনিয়ন") || msg.contains("union") || msg.contains("ইউপি") || msg.contains("চেয়ারম্যান") || 
            msg.contains("মেম্বার") || msg.contains("প্রত্যয়ন") || msg.contains("জন্ম নিবন্ধন") || msg.contains("প্রত্যয়ন") || msg.contains("নাগরিকত্ব") -> {
                "🌾 **করিমগঞ্জ ইউনিয়ন পরিষদ ও নাগরিক সেবা:**\n\n" +
                "করিমগঞ্জ উপজেলায় মোট ১১টি ইউনিয়ন রয়েছে (যেমন: করিমগঞ্জ সদর, গুনধর, নিয়ামতপুর, দেহুন্দা, নোয়াবাদ, কিরাটন ইত্যাদি)।\n\n" +
                "*ইউনিয়ন পরিষদ থেকে প্রাপ্ত প্রধান সেবাসমূহ:*\n" +
                "- **নাগরিকত্ব ও চারিত্রিক সনদপত্র**\n" +
                "- **জন্ম ও মৃত্যু নিবন্ধন সংশোধন ও নতুন আবেদন**\n" +
                "- **ওয়ারিশ বা উত্তরাধিকার সনদপত্র**\n\n" +
                "*আবেদন প্রক্রিয়া:*\n" +
                "১. আপনার সংশ্লিষ্ট ইউনিয়ন পরিষদের ডিজিটাল সেন্টারে যোগাযোগ করুন।\n" +
                "২. জন্ম নিবন্ধনের জন্য অনলাইনে আবেদন করুন: bdris.gov.bd।\n\n" +
                "জরুরি প্রয়োজনে আপনার সংশ্লিষ্ট ইউনিয়নের চেয়ারম্যান বা সচিবের সাথে সরাসরি যোগাযোগ করতে আমাদের 'তথ্য' ট্যাবটি ভিজিট করুন।"
            }
            msg.contains("অনলাইন") || msg.contains("online") || msg.contains("ডিজিটাল") || msg.contains("সার্টিফিকেট") || 
            msg.contains("রেজিস্ট্রেশন") || msg.contains("পাসপোর্ট") || msg.contains("এনআইডি") || msg.contains("nid") -> {
                "🌐 **করিমগঞ্জ ডিজিটাল অনলাইন সেবাসমূহ:**\n\n" +
                "অ্যাপের **'অনলাইন' (Online)** মেনু ব্যবহার করে আপনি ঘরে বসেই নিম্নলিখিত সেবাগুলির জন্য আবেদন ও ফি প্রদান করতে পারেন:\n\n" +
                "- **জাতীয় পরিচয়পত্র (NID) সেবা:** সংশোধন, ডাউনলোড ও নতুন ভোটার আবেদন।\n" +
                "- **অনলাইন পাসপোর্ট আবেদন:** ই-পাসপোর্ট পোর্টাল।\n" +
                "- **জন্ম ও মৃত্যু নিবন্ধন:** নতুন আবেদন ও যাচাই।\n" +
                "- **উপজেলা ই-সেবা কেন্দ্র:** সরকারি বিভিন্ন দপ্তরের সেবার আবেদন।\n\n" +
                "যেকোনো কারিগরি সহায়তার জন্য আমাদের ডিজিটাল সেন্টারে যোগাযোগ করুন।"
            }
            msg.contains("হটলাইন") || msg.contains("হেল্পলাইন") || msg.contains("৯৯৯") || msg.contains("৩৩৩") || msg.contains("১০৯") || msg.contains("জরুরি নম্বর") -> {
                "📞 **জাতীয় জরুরি ও জনকল্যাণমূলক হটলাইন নম্বরসমূহ:**\n\n" +
                "যেকোনো সময়ে বিনামূল্যে কল করুন নিম্নোক্ত নম্বরগুলোতে:\n\n" +
                "- **৯৯৯ (জাতীয় জরুরি সেবা):** পুলিশ, ফায়ার সার্ভিস ও অ্যাম্বুলেন্স সহায়তার জন্য।\n" +
                "- **৩৩৩ (জাতীয় তথ্য ও সেবা):** সরকারি সেবা, সামাজিক সমস্যা ও ডিজিটাল তথ্য সহায়তার জন্য।\n" +
                "- **১০৯ (নারী ও শিশু নির্যাতন প্রতিরোধ):** যেকোনো নির্যাতনের বিরুদ্ধে তাৎক্ষণিক আইনি সহায়তা।\n" +
                "- **১০৬ (দুর্নীতি দমন কমিশন):** দুর্নীতি প্রতিরোধে সরাসরি অভিযোগ।\n" +
                "- **১৬১২৩ (কৃষি কল সেন্টার):** ফসলের রোগবালাই ও কৃষি পরামর্শের জন্য।"
            }
            msg.contains("পর্যটন") || msg.contains("ঘোরার") || msg.contains("ভ্রমণ") || msg.contains("জঙ্গলবাড়ি") || 
            msg.contains("কেল্লা") || msg.contains("দুর্গ") || msg.contains("ঈশা খাঁ") || msg.contains("ইতিহাস") || msg.contains("দর্শনীয়") -> {
                "🏰 **করিমগঞ্জের দর্শনীয় স্থান ও ঐতিহ্য:**\n\n" +
                "করিমগঞ্জ উপজেলার রয়েছে সুদীর্ঘ ঐতিহাসিক এবং সাংস্কৃতিক ঐতিহ্য।\n\n" +
                "*প্রধান দর্শনীয় স্থান:*\n" +
                "- **জঙ্গলবাড়ি দুর্গ (Isa Khan's Jangalbari Fort):** মসনদ-ই-আলা ঈশা খাঁর স্মৃতিবিজড়িত ঐতিহাসিক দুর্গ। এটি করিমগঞ্জ উপজেলার জঙ্গলবাড়ি গ্রামে অবস্থিত। এখানে রয়েছে ঈশা খাঁর দরবার হল, মসজিদ ও প্রাচীন তোরণ।\n" +
                "- **নরসুんだা নদী তীরবর্তী পার্ক:** বিকেলে ভ্রমণের জন্য চমৎকার স্থান।\n\n" +
                "*যাতায়াত:* কিশোরগঞ্জ সদর উপজেলা থেকে মাত্র ২০-৩০ মিনিটে ইজিবাইক বা সিএনজি যোগে জঙ্গলবাড়ি দুর্গে যাওয়া যায়।"
            }
            msg.contains("কৃষি") || msg.contains("কৃষক") || msg.contains("ধান") || msg.contains("ফসল") || 
            msg.contains("প্রাণিসম্পদ") || msg.contains("গরু") || msg.contains("ছাগল") || msg.contains("ভেটেরিনারি") || 
            msg.contains("চিকিৎসা") || msg.contains("গাভী") || msg.contains("বাছুর") -> {
                "🐄 **করিমগঞ্জ কৃষি ও প্রাণিসম্পদ উন্নয়ন সেবা:**\n\n" +
                "আমাদের উপজেলার অর্থনৈতিক মূল ভিত্তি কৃষি ও দুগ্ধ খামার। উপজেলা প্রশাসন কৃষকদের সহায়তায় সচেষ্ট।\n\n" +
                "*কৃষি সেবা:*\n" +
                "- আধুনিক চাষাবাদ পদ্ধতি ও সার/বীজ সহায়তার জন্য উপজেলা কৃষি কর্মকর্তার কার্যালয়ে যোগাযোগ করুন।\n" +
                "- **কৃষি পরামর্শ হটলাইন:** ১৬১২৩\n\n" +
                "*প্রাণিসম্পদ ও ভেটেরিনারি সেবা:*\n" +
                "- **উপজেলা প্রাণিসম্পদ দপ্তর ও ভেটেরিনারি হাসপাতাল:** করিমগঞ্জ সদর।\n" +
                "- **কৃত্রিম প্রজনন ও ভ্যাক্সিনেশন সেবা:** আমাদের টেকনিশিয়ানদের সরাসরি বাড়ি ডাকার ব্যবস্থা রয়েছে। জরুরি প্রয়োজনে প্রাণিসম্পদ কর্মকর্তার নম্বর সংগ্রহ করতে 'তথ্য' ট্যাবে যোগাযোগ করুন।"
            }
            msg.contains("হাই") || msg.contains("হ্যালো") || msg.contains("hello") || msg.contains("hi") || 
            msg.contains("আসসালামু") || msg.contains("আলাইকুম") || msg.contains("কেমন") || msg.contains("আছেন") || 
            msg.contains("আদাব") || msg.contains("নমস্কার") -> {
                "👋 **আসসালামু আলাইকুম!**\n\n" +
                "আমি করিমগঞ্জ ডিজিটাল সহকারী। আপনার নাগরিক সেবা সহজ করতে আমি সবসময় প্রস্তুত।\n\n" +
                "আমি আপনাকে নিম্নোক্ত বিষয়গুলো সম্পর্কে তথ্য দিয়ে সাহায্য করতে পারি:\n" +
                "- 🚑 জরুরি অ্যাম্বুলেন্স ও স্বাস্থ্যসেবা\n" +
                "- 📝 অভিযোগ দায়েরের প্রক্রিয়া\n" +
                "- 🏛️ ভূমি সেবা ও ইউনিয়ন পরিষদ তথ্য\n" +
                "- 🌐 অনলাইন সরকারি সেবা ও জাতীয় হটলাইন\n\n" +
                "আপনার যেকোনো প্রশ্ন আমাকে বাংলায় অথবা ইংরেজিতে টাইপ করে জানান।"
            }
            else -> {
                "🤔 **আমি আপনার প্রশ্নটি পুরোপুরি বুঝতে পারিনি।**\n\n" +
                "আমি আপনাকে করিমগঞ্জ উপজেলার নাগরিক সেবা সম্পর্কে সঠিক তথ্য দিতে পারি। অনুগ্রহ করে নিচের যেকোনো একটি বিষয়ে সংক্ষেপে প্রশ্ন করুন:\n" +
                "- জরুরি অ্যাম্বুলেন্স বা হাসপাতালের তথ্য\n" +
                "- জমিজমা বা ভূমি সেবা ও নামজারী\n" +
                "- অভিযোগ দায়ের করার পদ্ধতি\n" +
                "- ইউনিয়ন পরিষদের জন্ম নিবন্ধন ও সার্টিফিকেট\n\n" +
                "অথবা সরাসরি নিচের সাজেস্টেড বাটনগুলোতে ট্যাপ করে তাৎক্ষণিক তথ্য জেনে নিতে পারেন।"
            }
        }
    }

    // General send action
    val onSendText: (String) -> Unit = { text ->
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            if (selectedTabMode == 0) {
                // AI Helpdesk Chat Flow
                aiMessages.add(
                    ChatMessage(
                        id = "user_msg_${System.currentTimeMillis()}",
                        sender = nickname,
                        text = trimmed,
                        timestamp = System.currentTimeMillis(),
                        senderId = mySenderId,
                        verified = isProfileVerified
                    )
                )
                messageText = ""
                
                // Trigger Simulated typing and reply
                scope.launch {
                    isAiTyping = true
                    aiTypingText = "ডিজিটাল সহকারী টাইপ করছেন..."
                    delay(1200)
                    isAiTyping = false
                    val botReply = getBotResponse(trimmed)
                    aiMessages.add(
                        ChatMessage(
                            id = "bot_msg_${System.currentTimeMillis()}",
                            sender = "করিমগঞ্জ ডিজিটাল সহকারী",
                            text = botReply,
                            timestamp = System.currentTimeMillis(),
                            senderId = "bot",
                            verified = true
                        )
                    )
                }
            } else {
                // Public Citizen Chatroom Flow
                if (dbRef != null) {
                    // Try to send to Firebase Realtime Database
                    val key = dbRef.push().key
                    if (key != null) {
                        val msg = mapOf(
                            "sender" to nickname,
                            "text" to trimmed,
                            "timestamp" to System.currentTimeMillis(),
                            "senderId" to mySenderId,
                            "verified" to isProfileVerified
                        )
                        dbRef.child(key).setValue(msg)
                    }
                    messageText = ""
                } else {
                    // If database is null or offline, add to Local simulated Public messages
                    publicMessages.add(
                        ChatMessage(
                            id = "local_public_msg_${System.currentTimeMillis()}",
                            sender = nickname,
                            text = trimmed,
                            timestamp = System.currentTimeMillis(),
                            senderId = mySenderId,
                            verified = isProfileVerified
                        )
                    )
                    messageText = ""
                    
                    // Trigger simulated citizen reply to make the forum active
                    scope.launch {
                        isPublicTyping = true
                        val simulatedCitizens = listOf(
                            Pair("জহিরুল ইসলাম", "খুবই দরকারী কথা বলেছেন ভাই। আমাদের সকলের সচেতনতা প্রয়োজন।"),
                            Pair("ফাতেমা খাতুন", "সহমত প্রকাশ করছি। ডিজিটাল করিমগঞ্জ বিনির্মাণে এই অ্যাপটি একটি বড় পদক্ষেপ।"),
                            Pair("সজীব মিয়া", "তথ্যগুলো শেয়ার করার জন্য ধন্যবাদ! অ্যাপের অভিযোগ সেলটি অনেক ভালো কাজ করে।")
                        ).random()
                        publicTypingName = simulatedCitizens.first
                        delay(1500)
                        isPublicTyping = false
                        publicMessages.add(
                            ChatMessage(
                                id = "mock_reply_${System.currentTimeMillis()}",
                                sender = simulatedCitizens.first,
                                text = simulatedCitizens.second,
                                timestamp = System.currentTimeMillis(),
                                senderId = "simulated_citizen_${(10..99).random()}",
                                verified = listOf(true, false).random()
                            )
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBackground)
    ) {
        // Mode Selector Tab Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                // Custom Tab Row for modern design and touch precision
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTabMode == 0) KarimganjGreen else Color.Transparent)
                            .clickable { selectedTabMode = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "ডিজিটাল সহকারী",
                                tint = if (selectedTabMode == 0) Color.White else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ডিজিটাল সহকারী",
                                color = if (selectedTabMode == 0) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTabMode == 1) KarimganjGreen else Color.Transparent)
                            .clickable { selectedTabMode = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "নাগরিক চ্যাট",
                                tint = if (selectedTabMode == 1) Color.White else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "নাগরিক চ্যাট রুম",
                                color = if (selectedTabMode == 1) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Profile details / status banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (selectedTabMode == 0 || isConnected || dbRef == null) Color(0xFF4CAF50) else Color(0xFFE53935))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedTabMode == 0) "সহকারী সেবা সচল ও অনলাইন" 
                                   else if (isConnected) "পাবলিক চ্যাট রুম সচল" 
                                   else "ডেমো/অফলাইন মোড সক্রিয়",
                            color = if (selectedTabMode == 0 || isConnected || dbRef == null) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isEditingName) {
                        TextButton(
                            onClick = {
                                tempNameInput = nickname
                                isEditingName = true
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "নাম পরিবর্তন", tint = KarimganjGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("নাম পরিবর্তন", color = KarimganjGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isEditingName) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tempNameInput,
                            onValueChange = { tempNameInput = it },
                            placeholder = { Text("আপনার নাম লিখুন", color = Color.Gray) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
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
                            colors = ButtonDefaults.buttonColors(containerColor = KarimganjGreen),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("সংরক্ষণ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "আপনার নাম: $nickname" + if (isProfileVerified) " (ভেরিফাইড নাগরিক ✅)" else "",
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Selected Chat List Area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val currentMessagesList = if (selectedTabMode == 0) aiMessages 
                                      else if (dbRef != null) firebaseMessages 
                                      else publicMessages

            items(currentMessagesList, key = { it.id }) { msg ->
                val isMe = msg.senderId == mySenderId
                val isBot = msg.senderId == "bot"
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    // Sender name header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isMe) "আমি" else msg.sender,
                            color = if (isMe) KarimganjGreen else if (isBot) KarimganjCrimson else Color.DarkGray,
                            fontSize = 11.sp,
                            fontWeight = if (isMe || isBot) FontWeight.Bold else FontWeight.Medium
                        )
                        if (msg.verified || isBot) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "ভেরিফাইড",
                                tint = if (isBot) KarimganjCrimson else KarimganjGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    
                    // Bubble Container
                    Box(
                        modifier = Modifier
                            .widthIn(max = 290.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 2.dp,
                                    bottomEnd = if (isMe) 2.dp else 16.dp
                                )
                            )
                            .background(
                                if (isMe) KarimganjGreen 
                                else if (isBot) Color(0xFFFFF3E0) 
                                else Color.White
                            )
                            .border(
                                width = 1.dp,
                                color = if (isMe) KarimganjGreen 
                                        else if (isBot) Color(0xFFFFB74D) 
                                        else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 2.dp,
                                    bottomEnd = if (isMe) 2.dp else 16.dp
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isMe) Color.White else Color.Black,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Typing Indicator bubble
            if ((selectedTabMode == 0 && isAiTyping) || (selectedTabMode == 1 && isPublicTyping)) {
                item {
                    val typingName = if (selectedTabMode == 0) "করিমগঞ্জ ডিজিটাল সহকারী" else publicTypingName
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = typingName,
                                color = if (selectedTabMode == 0) KarimganjCrimson else Color.DarkGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp))
                                .background(Color(0xFFEEEEEE))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedTabMode == 0) "টাইপ করছেন..." else "লিখছেন...",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Helpdesk suggestion buttons row
        if (selectedTabMode == 0 && messageText.trim().isEmpty()) {
            val suggestions = listOf(
                Pair("🚑 স্বাস্থ্যসেবা", "অ্যাম্বুলেন্স ও উপজেলা স্বাস্থ্য কমপ্লেক্স এর তথ্য কি?"),
                Pair("📝 অভিযোগ প্রক্রিয়া", "পৌরসভায় অভিযোগ করার নিয়ম কি?"),
                Pair("🏛️ ভূমি সেবা", "অনলাইন ভূমি সেবা ও খতিয়ানের তথ্য কিভাবে পাবো?"),
                Pair("🌾 ইউনিয়ন পরিষদ", "জন্ম নিবন্ধন ও ইউপি সনদপত্র কিভাবে নিবো?"),
                Pair("🌐 অনলাইন সেবা", "পাসপোর্ট, NID এবং ভোটার আবেদনের লিংক কি?"),
                Pair("📞 জাতীয় হটলাইন", "জরুরি সরকারি সেবা হটলাইন নম্বরসমূহ কি কি?")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { item ->
                    AssistChip(
                        onClick = { onSendText(item.second) },
                        label = { Text(item.first, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KarimganjGreen) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = CardBackgroundLight,
                            labelColor = KarimganjGreen
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = KarimganjGreen.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp)
                    )
                }
            }
        }

        // Message Input Bottom Panel
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
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
                    placeholder = { 
                        Text(
                            text = if (selectedTabMode == 0) "সহকারীকে জিজ্ঞাসা করুন..." else "বার্তা লিখুন...", 
                            color = Color.Gray,
                            fontSize = 13.sp
                        ) 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KarimganjGreen,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onSendText(messageText) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = KarimganjGreen),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_send_button")
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

