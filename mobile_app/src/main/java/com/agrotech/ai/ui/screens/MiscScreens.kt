package com.agrotech.ai.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agrotech.ai.ui.components.*
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.viewmodel.AgroViewModel
import com.agrotech.ai.data.model.*
import com.agrotech.ai.ui.theme.LocalAppStrings
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import coil.compose.AsyncImage

@Composable
fun SplashScreen(navController: NavController) {
    val strings = LocalAppStrings.current
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = com.agrotech.ai.R.drawable.ttttt),
            contentDescription = "Splash Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerRecommendationScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    var nitrogen by remember { mutableStateOf("") }
    var phosphorous by remember { mutableStateOf("") }
    var potassium by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    
    // Dropdown States
    var soilType by remember { mutableStateOf("Loamy") }
    var cropType by remember { mutableStateOf("Wheat") }
    var soilExpanded by remember { mutableStateOf(false) }
    var cropExpanded by remember { mutableStateOf(false) }

    val soilTypes = listOf("Sandy", "Loamy", "Black", "Red", "Clay")
    val cropTypes = listOf("Wheat", "Rice", "Maize", "Cotton", "Sugarcane", "Pulses", "Tobacco", "Oil seeds", "Barley", "Millets")

    val fertResult by viewModel.fertilizerRec.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.fertRec, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = strings.enterDetails,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Provide soil test values for precise results",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // N, P, K Row
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AgroTextField(
                        value = nitrogen, 
                        onValueChange = { if(it.length <= 3) nitrogen = it }, 
                        label = "N (Nitrogen)", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Science,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AgroTextField(
                        value = phosphorous, 
                        onValueChange = { if(it.length <= 3) phosphorous = it }, 
                        label = "P (Phos.)", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Science,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // K, Moisture Row
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AgroTextField(
                        value = potassium, 
                        onValueChange = { if(it.length <= 3) potassium = it }, 
                        label = "K (Potass.)", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Science,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AgroTextField(
                        value = moisture, 
                        onValueChange = { if(it.length <= 3) moisture = it }, 
                        label = "Moisture", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.WaterDrop,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Temp, Humidity Row
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AgroTextField(
                        value = temperature, 
                        onValueChange = { temperature = it }, 
                        label = "Temp (°C)", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Thermostat,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    AgroTextField(
                        value = humidity, 
                        onValueChange = { humidity = it }, 
                        label = "Humidity", 
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Cloud,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Dropdowns
            item {
                Text("Environmental Context", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = soilExpanded,
                    onExpandedChange = { soilExpanded = !soilExpanded }
                ) {
                    OutlinedTextField(
                        value = soilType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Soil Type") },
                        leadingIcon = { Icon(Icons.Default.Landscape, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soilExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = soilExpanded, onDismissRequest = { soilExpanded = false }) {
                        soilTypes.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = { soilType = selectionOption; soilExpanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = cropExpanded,
                    onExpandedChange = { cropExpanded = !cropExpanded }
                ) {
                    OutlinedTextField(
                        value = cropType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Crop Type") },
                        leadingIcon = { Icon(Icons.Default.Agriculture, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = cropExpanded, onDismissRequest = { cropExpanded = false }) {
                        cropTypes.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = { cropType = selectionOption; cropExpanded = false }
                            )
                        }
                    }
                }
            }

            item {
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                val context = androidx.compose.ui.platform.LocalContext.current
                Spacer(modifier = Modifier.height(16.dp))
                AgroButton(
                    text = if (isLoading) "Analyzing Soil Data..." else strings.predictFertilizer,
                    onClick = {
                        // DIAGNOSTIC TOAST
                        android.widget.Toast.makeText(context, "Requesting AI Recommendation...", android.widget.Toast.LENGTH_SHORT).show()
                        
                        viewModel.getFertilizerRecommendation(
                            FertilizerRequest(
                                n = (nitrogen.toIntOrNull() ?: 0),
                                p = (phosphorous.toIntOrNull() ?: 0),
                                k = (potassium.toIntOrNull() ?: 0),
                                moisture = (moisture.toDoubleOrNull() ?: 0.0),
                                temp = (temperature.toDoubleOrNull() ?: 0.0),
                                humidity = (humidity.toDoubleOrNull() ?: 0.0),
                                soilType = soilType,
                                cropType = cropType
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }

            if (fertResult != null) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // --- PREMIUM DIAGNOSTIC DASHBOARD ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAF8)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Diagnostic Header
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("AgroTeck Solution AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
                                    Text("Premium Fertilizer Analysis Report", style = MaterialTheme.typography.labelSmall, color = Color(0xFF666666))
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                            Spacer(Modifier.height(24.dp))

                            // Main Recommendation
                            Text("RECOMMENDED FERTILIZER", style = MaterialTheme.typography.labelLarge, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text(
                                text = fertResult!!.recommendation.uppercase(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF212121)
                            )
                            
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Accuracy: ${fertResult!!.accuracy ?: "98.7%"}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }

                            Spacer(Modifier.height(32.dp))

                            // Nutrient Deficiency Section
                            Text("Nutrient Gap Analysis (kg/ha)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val deficiency = fertResult!!.deficiency ?: mapOf("N" to 0.0, "P" to 0.0, "K" to 0.0)
                                listOf("N" to Color(0xFFFFEBEE), "P" to Color(0xFFFFF3E0), "K" to Color(0xFFE3F2FD)).forEach { (key, bgColor) ->
                                    val textColor = when(key) { "N" -> Color(0xFFC62828); "P" -> Color(0xFFEF6C00); else -> Color(0xFF1565C0) }
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = bgColor),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(key, fontWeight = FontWeight.Bold, color = textColor)
                                            Text("${deficiency[key]?.toInt() ?: 0}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = textColor)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(32.dp))

                            // AI Reasoning Section
                            Text("Why this fertilizer? (AI Reasoning)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F8E9), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                fertResult!!.whyThisFertilizer?.forEach { item ->
                                    val isPositive = item.impact > 0
                                    Text(
                                        text = item.feature,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontWeight = FontWeight.Medium
                                    )
                                } ?: Text("Calculating feature impacts...", color = Color.Gray)
                            }

                            Spacer(Modifier.height(32.dp))

                            // EXPERT ANALYSIS CARD
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Color(0xFFE8F5E9)),
                                shadowElevation = 2.dp
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MenuBook, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Text("Scientific Agricultural Advice", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = fertResult!!.expertExplanation ?: "Consulting ICAR knowledge base...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp,
                                        color = Color(0xFF424242)
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(32.dp))
                            
                            // Application Schedule
                            Text("Recommended Application Schedule", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            fertResult!!.schedule?.forEach { step ->
                                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = Color(0xFF2E7D32), modifier = Modifier.size(8.dp)) {}
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(step.stage, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${step.quantity} kg/ha", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun NutrientBadge(label: String, value: Double, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
            Text(value.toInt().toString(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun ScheduleRow(stage: String, qty: Double, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF4A148C),
                modifier = Modifier.size(10.dp)
            ) {}
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(Color(0xFF4A148C).copy(alpha = 0.2f))
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(stage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("$qty kg/ha", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: AgroViewModel) {
    val userState by viewModel.userState.collectAsState()

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // 1. Header with Gradient Background
            item {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier
                                .size(100.dp)
                                .border(4.dp, Color.White, CircleShape),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().clickable { imagePickerLauncher.launch("image/*") }) {
                                if (profileImageUri != null) {
                                    AsyncImage(
                                        model = profileImageUri,
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person, 
                                        null, 
                                        modifier = Modifier.size(60.dp), 
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            userState?.name ?: "Admin Farmer", 
                            style = MaterialTheme.typography.headlineSmall, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            userState?.email ?: "admin@agrotech.com", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // 2. Stats Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val strings = LocalAppStrings.current
                    StatItem(strings.farmSize, userState?.farmSize ?: "12.5", strings.acres)
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    StatItem(strings.crops, "4", strings.types)
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    StatItem(strings.experience, "8", strings.years)
                }
            }

            // 3. Action Cards (Expert Learning)
            item {
                val strings = LocalAppStrings.current
                SectionHeader(
                    title = strings.learning, 
                    actionText = strings.viewAll, 
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onActionClick = { navController.navigate(Screen.Learning.route) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { ExpertVideoCard("Modern Wheat Farming", "12:30", Color(0xFF1976D2)) { navController.navigate(Screen.Learning.route) } }
                    item { ExpertVideoCard("Organic Fertilizers", "08:45", Color(0xFF43A047)) { navController.navigate(Screen.Learning.route) } }
                    item { ExpertVideoCard("Pest Control Basics", "15:20", Color(0xFFD32F2F)) { navController.navigate(Screen.Learning.route) } }
                }
            }

            // 4. Settings Sections
            item {
                val strings = LocalAppStrings.current
                Spacer(modifier = Modifier.height(24.dp))
                val context = androidx.compose.ui.platform.LocalContext.current

                SettingsGroup(title = strings.accountSettings) {
                    ProfileMenuItem(title = strings.farmProfiles, icon = Icons.Default.Landscape, description = strings.manageFieldData, onClick = { navController.navigate(Screen.FarmProfiles.route) })
                    ProfileMenuItem(title = strings.overallHistory, icon = Icons.Default.History, description = strings.viewPastActivities, onClick = { navController.navigate(Screen.OverallHistory.route) })
                }

                SettingsGroup(title = strings.preferences) {
                    ProfileMenuItem(title = strings.languageSettings, icon = Icons.Default.Language, description = "Hindi, English, Punjabi", onClick = { navController.navigate(Screen.LanguageSelector.route) })
                    ProfileMenuItem(title = strings.notification, icon = Icons.Default.NotificationsActive, description = strings.weatherAlerts, onClick = { navController.navigate(Screen.Notifications.route) })
                }

                SettingsGroup(title = strings.support) {
                    ProfileMenuItem(title = strings.helpCenter, icon = Icons.Default.HelpCenter, onClick = { android.widget.Toast.makeText(context, "Contacting Help Center...", android.widget.Toast.LENGTH_SHORT).show() })
                    ProfileMenuItem(title = strings.privacyPolicy, icon = Icons.Default.PrivacyTip, onClick = { android.widget.Toast.makeText(context, "Opening Privacy Policy...", android.widget.Toast.LENGTH_SHORT).show() })
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    AgroButton(
                        text = strings.logout,
                        onClick = { 
                            android.widget.Toast.makeText(context, "Logging out...", android.widget.Toast.LENGTH_SHORT).show()
                            viewModel.setLanguage("en")
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(2.dp))
            Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, description: String? = null, onClick: () -> Unit = {}) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = if (description != null) { { Text(description, style = MaterialTheme.typography.bodySmall) } } else null,
        leadingContent = { 
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        },
        trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun ExpertVideoCard(title: String, duration: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.7f), color)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayCircleFilled, 
                    contentDescription = null, 
                    tint = Color.White, 
                    modifier = Modifier.size(48.dp)
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                ) {
                    Text(
                        duration, 
                        color = Color.White, 
                        fontSize = 10.sp, 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Expert Tutorial", 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDetailsScreen(navController: NavController, @Suppress("UNUSED_PARAMETER") viewModel: AgroViewModel) {
    val crops = listOf(
        CropInfo("Wheat", "120 kg/ha N, 60 kg/ha P, 40 kg/ha K", "Rabi (Oct-Mar)", "Loamy, Clay Loam"),
        CropInfo("Rice", "120 kg/ha N, 60 kg/ha P, 40 kg/ha K", "Kharif (Jun-Nov)", "Clay, Silty Clay"),
        CropInfo("Maize", "150 kg/ha N, 75 kg/ha P, 50 kg/ha K", "Kharif/Rabi", "Sandy Loam, Loamy"),
        CropInfo("Cotton", "100 kg/ha N, 50 kg/ha P, 50 kg/ha K", "Kharif (Apr-Oct)", "Black Soil, Alluvial"),
        CropInfo("Sugarcane", "250 kg/ha N, 115 kg/ha P, 115 kg/ha K", "Feb-Mar planting", "Loamy, Clay Loam")
    )

    val strings = LocalAppStrings.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.cropInfo) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            crops.forEach { crop ->
                CropInfoCard(crop)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

data class CropInfo(val name: String, val nutrients: String, val season: String, val soilType: String)

@Composable
fun CropInfoCard(crop: CropInfo) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(crop.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row { Icon(Icons.Default.Science, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("${strings.nutrients} ${crop.nutrients}", style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(4.dp))
            Row { Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("${strings.season} ${crop.season}", style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(4.dp))
            Row { Icon(Icons.Default.Grass, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("${strings.soil} ${crop.soilType}", style = MaterialTheme.typography.bodySmall) }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartIrrigationScreen(navController: NavController, viewModel: AgroViewModel) {
    val iotState by viewModel.iotState.collectAsState()
    val strings = LocalAppStrings.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.smartIrrigation, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Alert Banner
            when {
                iotState?.decision == "START IRRIGATION" -> {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("IRRIGATION ALERT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.Red)
                                Text("Soil moisture is low. Start irrigation now!", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            }
                        }
                    }
                }
                iotState?.decision == "NO IRRIGATION" -> {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("SOIL HEALTH GOOD", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                                Text("Current moisture levels are optimal.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            }
                        }
                    }
                }
                else -> {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.HourglassEmpty, null, tint = Color(0xFFE65100), modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("WAITING FOR DATA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                                Text("Sensor is connecting. Please wait...", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }

            // Real-time Gauges
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GaugeCard(
                    label = strings.soilMoisture,
                    value = iotState?.soil ?: 0.0,
                    unit = "%",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                GaugeCard(
                    label = "Temperature",
                    value = iotState?.temp ?: 0.0,
                    unit = "°C",
                    color = Color(0xFFE64A19),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Decision Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("AI Decision Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        // Connectivity Badge
                        Surface(
                            color = if (iotState?.soil != null) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (iotState?.soil != null) "● ONLINE" else "○ OFFLINE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (iotState?.soil != null) Color(0xFF2E7D32) else Color.Gray
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = iotState?.decision ?: "Waiting for data...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (iotState?.decision == "START IRRIGATION") Color.Red else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = run {
                            val rawTs = iotState?.timestamp
                            when {
                                rawTs == null -> "Last Sync: Never"
                                rawTs.contains("T") -> {
                                    val timePart = rawTs.split("T").getOrNull(1)?.split(".")?.getOrNull(0) ?: "00:00:00"
                                    "Last Sync: $timePart"
                                }
                                rawTs.contains(" ") -> {
                                    val timePart = rawTs.split(" ").getOrNull(1)?.split(".")?.getOrNull(0) ?: "00:00:00"
                                    "Last Sync: $timePart"
                                }
                                else -> "Last Sync: $rawTs"
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { viewModel.simulateSensorData(25.0, 30.0) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Simulate Low Soil")
                }
                Button(
                    onClick = { viewModel.simulateSensorData(65.0, 28.0) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Simulate High Soil")
                }
            }
            
            Spacer(Modifier.height(24.dp))

            // Historical Trends Section
            Text(
                text = "Historical Trends (Last 10 Polls)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            
            val history by viewModel.iotHistory.collectAsState()
            val primaryColor = MaterialTheme.colorScheme.primary
            
            Card(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (history.isEmpty()) {
                        Text("No historical data yet...", color = Color.Gray, modifier = Modifier.align(Alignment.CenterVertically).fillMaxWidth(), textAlign = TextAlign.Center)
                    } else {
                        history.forEach { item: IotData ->
                            val heightFactor = (item.soil ?: 0.0) / 100
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(heightFactor.toFloat().coerceIn(0.1f, 1f))
                                    .background(
                                        if ((item.soil ?: 0.0) < 30) Color.Red.copy(alpha = 0.7f) 
                                        else primaryColor.copy(alpha = 0.7f),
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { /* Manual Pump Control */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Remote Control Pump (Manual)")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NDVIScreen(navController: NavController, viewModel: AgroViewModel) {
    val result        by viewModel.cropAnalysisResult.collectAsState()
    val isLoading     by viewModel.isSatelliteLoading.collectAsState()
    val errorMsg      by viewModel.errorState.collectAsState()
    val context       = androidx.compose.ui.platform.LocalContext.current

    // ── Input state ──────────────────────────────────────────────────────────
    var latitude  by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var radius    by remember { mutableStateOf("500") }
    var gpsStatus by remember { mutableStateOf("") }

    // ── GPS auto-fill ─────────────────────────────────────────────────────────
    fun fetchGps() {
        gpsStatus = "Fetching GPS…"
        try {
            val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE)
                    as android.location.LocationManager
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                val loc = lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                if (loc != null) {
                    latitude  = "%.6f".format(loc.latitude)
                    longitude = "%.6f".format(loc.longitude)
                    gpsStatus = "✅ GPS location filled"
                } else {
                    gpsStatus = "⚠️ No GPS fix. Enter manually."
                }
            } else {
                gpsStatus = "⚠️ Location permission not granted."
            }
        } catch (e: Exception) {
            gpsStatus = "⚠️ ${e.message}"
        }
    }

    // ── Severity → color mapping ──────────────────────────────────────────────
    fun severityColor(severity: String): Color = when {
        severity.contains("Critical", true)       -> Color(0xFFE53935)
        severity.contains("Moderate", true)       -> Color(0xFFFF6F00)
        severity.contains("Low Activity", true)   -> Color(0xFFF9A825)
        severity.contains("Optimal", true)        -> Color(0xFF2E7D32)
        else                                      -> Color(0xFF546E7A)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛰️ Satellite Crop Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B3A2D),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF1F8F2))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── INPUT CARD ────────────────────────────────────────────────────
            item {
                Card(
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier  = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        // Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1B3A2D),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🛰️", fontSize = 20.sp)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Farm Location",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Enter coordinates or use GPS",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // GPS Button
                        OutlinedButton(
                            onClick = { fetchGps() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B3A2D))
                        ) {
                            Icon(
                                Icons.Default.MyLocation, null,
                                tint = Color(0xFF1B3A2D),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Auto-fill from GPS", color = Color(0xFF1B3A2D))
                        }
                        if (gpsStatus.isNotEmpty()) {
                            Text(
                                gpsStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (gpsStatus.startsWith("✅")) Color(0xFF2E7D32) else Color(0xFFE53935),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Latitude + Longitude
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = latitude,
                                onValueChange = { latitude = it },
                                label = { Text("Latitude") },
                                placeholder = { Text("e.g. 28.6139") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1B3A2D)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                ),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = longitude,
                                onValueChange = { longitude = it },
                                label = { Text("Longitude") },
                                placeholder = { Text("e.g. 77.209") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1B3A2D)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                ),
                                singleLine = true
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Radius
                        OutlinedTextField(
                            value = radius,
                            onValueChange = { radius = it },
                            label = { Text("Analysis Radius (metres)") },
                            placeholder = { Text("e.g. 500") },
                            leadingIcon = { Icon(Icons.Default.Radar, null, tint = Color(0xFF1B3A2D)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            supportingText = { Text("Min 100m • Max 50,000m • Typical farm: 300–1000m") },
                            singleLine = true
                        )

                        Spacer(Modifier.height(20.dp))

                        // Error banner
                        errorMsg?.let { err ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(err, color = Color(0xFFE53935), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        // Analyze Button
                        Button(
                            onClick = {
                                val lat = latitude.toDoubleOrNull()
                                val lon = longitude.toDoubleOrNull()
                                val rad = radius.toDoubleOrNull()
                                when {
                                    lat == null || lon == null ->
                                        android.widget.Toast.makeText(context, "Enter valid latitude and longitude", android.widget.Toast.LENGTH_SHORT).show()
                                    rad == null || rad < 100 ->
                                        android.widget.Toast.makeText(context, "Radius must be at least 100 metres", android.widget.Toast.LENGTH_SHORT).show()
                                    else -> viewModel.analyzeCrop(lat, lon, rad)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3A2D))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Contacting Satellite…", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Text("🛰️  Analyze Crop Health", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        if (isLoading) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "⏳ Fetching Sentinel-2 satellite imagery… This may take 20–45 seconds.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ── RESULT CARD ───────────────────────────────────────────────────
            result?.let { data ->
                // ── 1. HEALTH SCORE HERO ──────────────────────────────────────
                item {
                    val sColor = severityColor(data.severity)
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = sColor.copy(alpha = 0.08f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, sColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(data.severity, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                data.prediction,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = sColor
                            )
                            Spacer(Modifier.height(16.dp))

                            // Health score bar
                            Text("Health Score", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Color(0xFFE0E0E0))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = (data.healthScore / 100.0).toFloat().coerceIn(0f, 1f))
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                listOf(Color(0xFFE53935), Color(0xFFFFA000), sColor)
                                            )
                                        )
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${data.healthScore}/100",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = sColor
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "AI Confidence: ${(data.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // ── 2. NDVI STATS ─────────────────────────────────────────────
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("📡 NDVI Satellite Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Sentinel-2 L2A · ${data.ndviStats.pixelCount} valid pixels analysed", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                NdviStatBox("Mean NDVI", "%.3f".format(data.ndviStats.meanNdvi), Color(0xFF1B3A2D), Modifier.weight(1f))
                                NdviStatBox("Max NDVI",  "%.3f".format(data.ndviStats.maxNdvi),  Color(0xFF2E7D32), Modifier.weight(1f))
                                NdviStatBox("Min NDVI",  "%.3f".format(data.ndviStats.minNdvi),  Color(0xFFBF360C), Modifier.weight(1f))
                                NdviStatBox("Std Dev",   "%.3f".format(data.ndviStats.stdNdvi),  Color(0xFF6A1B9A), Modifier.weight(1f))
                            }
                        }
                    }
                }

                // ── 3. RECOMMENDATIONS ────────────────────────────────────────
                item {
                    val rec = data.recommendation
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FBF5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("📋 Field Recommendations", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(12.dp))

                            // Irrigation badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (rec.irrigationNeeded) Color(0xFFE3F2FD) else Color(0xFFE8F5E9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (rec.irrigationNeeded) "💧" else "✅", fontSize = 20.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            if (rec.irrigationNeeded) "Irrigation Required" else "No Irrigation Needed",
                                            fontWeight = FontWeight.Bold,
                                            color = if (rec.irrigationNeeded) Color(0xFF0D47A1) else Color(0xFF1B5E20)
                                        )
                                        Text(rec.irrigationAction, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            RecommendationRow("🌿 Nutrient Action", rec.nutrientAction)
                            Spacer(Modifier.height(8.dp))
                            RecommendationRow("🔍 Field Action", rec.fieldAction)
                            Spacer(Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF3E0)
                            ) {
                                Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("🛰️", fontSize = 16.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Next satellite check recommended in ${rec.nextCheckDays} days",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun NdviStatBox(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 14.sp, color = color)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RecommendationRow(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1B3A2D))
        Spacer(Modifier.height(2.dp))
        Text(body, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, lineHeight = 18.sp)
    }
}

@Composable
fun GaugeCard(label: String, value: Double, unit: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = (value / 100).toFloat(),
                    modifier = Modifier.size(80.dp),
                    color = color,
                    strokeWidth = 8.dp,
                    trackColor = color.copy(alpha = 0.1f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${value.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(unit, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}
