package com.example.ui.screens.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.BengkelTopAppBar
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.components.CustomerExpandedMapDialog
import com.example.ui.theme.*
import com.example.viewmodel.BengkelViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerMainScreen(viewModel: BengkelViewModel) {
    var currentTab by remember { mutableStateOf("home") } // home, order, active_order, catalog, history, chat, profile
    val authState by viewModel.authState.collectAsState()
    val currentCustomer by viewModel.currentCustomer.collectAsState()
    val activeOrder by viewModel.customerActiveOrder.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()

    // If an active order exists and user clicks tracker
    Scaffold(
        topBar = {
            BengkelTopAppBar(
                currentRole = UserRole.CUSTOMER,
                userName = currentCustomer?.fullName ?: authState.currentUserName,
                notifications = notifications,
                onSwitchRole = { viewModel.switchDemoAccount(it) },
                onResetDemoData = { viewModel.resetDemoData() },
                onLogout = { viewModel.logout() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { currentTab = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BengkelBluePrimary,
                        selectedTextColor = BengkelBluePrimary,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Slate800
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "order" || currentTab == "active_order",
                    onClick = {
                        currentTab = if (activeOrder != null) "active_order" else "order"
                    },
                    icon = {
                        BadgedBox(badge = {
                            if (activeOrder != null) {
                                Badge(containerColor = BengkelAmber) { Text("1") }
                            }
                        }) {
                            Icon(Icons.Default.Build, contentDescription = "Pesan")
                        }
                    },
                    label = { Text(if (activeOrder != null) "Lacak" else "Pesan", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BengkelBluePrimary,
                        selectedTextColor = BengkelBluePrimary,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Slate800
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "catalog",
                    onClick = { currentTab = "catalog" },
                    icon = { Icon(Icons.Default.Sell, contentDescription = "Tarif") },
                    label = { Text("Tarif Resmi", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BengkelBluePrimary,
                        selectedTextColor = BengkelBluePrimary,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Slate800
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "history",
                    onClick = { currentTab = "history" },
                    icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
                    label = { Text("Riwayat", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BengkelBluePrimary,
                        selectedTextColor = BengkelBluePrimary,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Slate800
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "profile",
                    onClick = { currentTab = "profile" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BengkelBluePrimary,
                        selectedTextColor = BengkelBluePrimary,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Slate800
                    )
                )
            }
        },
        containerColor = Slate50
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "home" -> CustomerHomeScreen(
                    viewModel = viewModel,
                    onNavigateToOrder = { currentTab = "order" },
                    onNavigateToActiveOrder = { currentTab = "active_order" },
                    onNavigateToCatalog = { currentTab = "catalog" },
                    onNavigateToHistory = { currentTab = "history" }
                )
                "order" -> CustomerCreateOrderScreen(
                    viewModel = viewModel,
                    onOrderSubmitted = { currentTab = "active_order" }
                )
                "active_order" -> CustomerActiveOrderScreen(
                    viewModel = viewModel,
                    onOpenChat = { currentTab = "chat" },
                    onOrderFinished = { currentTab = "home" }
                )
                "catalog" -> CustomerServiceCatalogScreen(viewModel = viewModel)
                "history" -> CustomerHistoryScreen(viewModel = viewModel)
                "chat" -> CustomerChatScreen(
                    viewModel = viewModel,
                    onBack = { currentTab = "active_order" }
                )
                "profile" -> CustomerProfileScreen(viewModel = viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. CUSTOMER HOME SCREEN
// -------------------------------------------------------------
@Composable
fun CustomerHomeScreen(
    viewModel: BengkelViewModel,
    onNavigateToOrder: () -> Unit,
    onNavigateToActiveOrder: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val customer by viewModel.currentCustomer.collectAsState()
    val activeOrder by viewModel.customerActiveOrder.collectAsState()
    val activeMechanics by viewModel.activeMechanics.collectAsState()
    val allMechanics by viewModel.allMechanics.collectAsState()
    val services by viewModel.activeServices.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    var showExpandedMap by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Customer Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Halo, ${customer?.fullName ?: "Customer"}!",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = BengkelAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = customer?.address ?: "Pekalongan, Jawa Tengah",
                                color = Slate300,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BengkelGreenDark)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Terverifikasi OTP", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate700)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = BengkelBlueCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = customer?.defaultVehicle ?: "Honda Vario 160",
                            color = Slate200,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "GPS: -6.8887, 109.6753",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Active Order Banner (If ongoing)
        if (activeOrder != null) {
            val order = activeOrder!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToActiveOrder() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BengkelAmberLight),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(BengkelAmberDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PESANAN AKTIF: ${order.serviceName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                            val statusEnum = runCatching { OrderStatus.valueOf(order.status) }.getOrNull()
                            Text(
                                text = statusEnum?.title ?: order.status,
                                fontSize = 12.sp,
                                color = BengkelAmberDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (order.mechanicName != null) {
                                Text(
                                    text = "Montir: ${order.mechanicName} (~${order.travelEtaMinutes} mnt)",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate600)
                }
            }
        }

        // Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNavigateToOrder,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary)
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Panggil Montir", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = onNavigateToCatalog,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PriceCheck, contentDescription = null, tint = BengkelBlueDeep)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Daftar Tarif", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BengkelBlueDeep)
            }
        }

        // Live Interactive Radar / GPS Map
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Peta Montir Terdekat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    val onlineCount = allMechanics.count { it.status == "ACTIVE" }
                    val busyCount = allMechanics.count { it.status == "BUSY" }
                    Text(
                        text = "$onlineCount Online • $busyCount Sibuk • Pekalongan",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { showExpandedMap = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = BengkelBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Layar Penuh",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BengkelBluePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Interactive Canvas with Zoom/Pan/Markers
            InteractiveMapCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                customerLat = customer?.latitude ?: -6.8887,
                customerLng = customer?.longitude ?: 109.6753,
                customerAddress = customer?.address ?: "Pekalongan, Jawa Tengah",
                allMechanics = if (allMechanics.isNotEmpty()) allMechanics else activeMechanics,
                activeMechanics = activeMechanics,
                isSearching = activeOrder?.status == OrderStatus.WAITING_MECHANIC.name,
                routeActive = activeOrder?.status == OrderStatus.HEADING_TO_LOCATION.name,
                mechanicEtaMinutes = activeOrder?.travelEtaMinutes,
                distanceKm = activeOrder?.distanceKm,
                showFilterTabs = true,
                showControls = true,
                onOrderWithMechanic = { onNavigateToOrder() },
                onExpandClick = { showExpandedMap = true }
            )

            // Tip gesture info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 Geser & cubit untuk zoom/pan • Ketuk pin untuk detail",
                    fontSize = 10.sp,
                    color = Slate500
                )
                Text(
                    text = "${allMechanics.size} Montir Terpantau",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BengkelBlueDark
                )
            }
        }

        // Fullscreen Expanded Map Modal
        if (showExpandedMap) {
            CustomerExpandedMapDialog(
                customerLat = customer?.latitude ?: -6.8887,
                customerLng = customer?.longitude ?: 109.6753,
                customerAddress = customer?.address ?: "Pekalongan, Jawa Tengah",
                allMechanics = if (allMechanics.isNotEmpty()) allMechanics else activeMechanics,
                onDismiss = { showExpandedMap = false },
                onOrderWithMechanic = {
                    showExpandedMap = false
                    onNavigateToOrder()
                }
            )
        }

        // Layanan Populer
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Layanan Bengkel Populer",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                TextButton(onClick = onNavigateToCatalog) {
                    Text("Lihat Semua (${services.size})", fontSize = 12.sp, color = BengkelBluePrimary)
                }
            }

            services.take(4).forEach { service ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToOrder() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BengkelBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = BengkelBluePrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = service.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = service.description,
                                    fontSize = 11.sp,
                                    color = Slate500,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Durasi: ~${service.estimatedDurationMinutes} menit",
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = currencyFormat.format(service.basePrice),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BengkelGreenDark
                            )
                            Text(
                                text = "Tarif Resmi",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. FORM PEMESANAN MONTIR (CREATE ORDER)
// -------------------------------------------------------------
@Composable
fun CustomerCreateOrderScreen(
    viewModel: BengkelViewModel,
    onOrderSubmitted: () -> Unit
) {
    val services by viewModel.activeServices.collectAsState()
    val customer by viewModel.currentCustomer.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    var selectedService by remember { mutableStateOf<WorkshopServiceEntity?>(services.firstOrNull()) }
    var vehicleType by remember { mutableStateOf(customer?.defaultVehicle ?: "Honda Vario 160") }
    var problemDescription by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf(customer?.address ?: "Jl. Veteran No. 12, Pekalongan") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(services) {
        if (selectedService == null && services.isNotEmpty()) {
            selectedService = services.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Panggil Montir ke Lokasi Anda",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Text(
            text = "Sistem akan secara otomatis mencarikan montir terdekat aktif di sekitar titik koordinat Anda.",
            fontSize = 12.sp,
            color = Slate500
        )

        if (errorMessage != null) {
            Surface(
                color = BengkelRedLight,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = BengkelRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // 1. Pilih Layanan
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Pilih Jenis Layanan Bengkel",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(10.dp))

                services.forEach { service ->
                    val isSelected = selectedService?.serviceId == service.serviceId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) BengkelBlueLight else Slate50)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) BengkelBluePrimary else Slate200,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedService = service }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = service.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) BengkelBluePrimary else Slate900
                            )
                            Text(
                                text = service.description,
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                        Text(
                            text = currencyFormat.format(service.basePrice),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BengkelGreenDark
                        )
                    }
                }
            }
        }

        // 2. Data Kendaraan & Keluhan
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Detail Kendaraan & Keluhan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = vehicleType,
                    onValueChange = { vehicleType = it },
                    label = { Text("Tipe Kendaraan (Motor / Mobil)") },
                    placeholder = { Text("Contoh: Honda Vario 160 / Toyota Avanza") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = problemDescription,
                    onValueChange = { problemDescription = it },
                    label = { Text("Deskripsi Kerusakan / Gejala") },
                    placeholder = { Text("Contoh: Mesin mati mendadak saat di lampu merah, ban kempes kena paku...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 3. Konfirmasi Titik Lokasi GPS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Titik Lokasi Panggilan (GPS)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = locationAddress,
                    onValueChange = { locationAddress = it },
                    label = { Text("Alamat / Patokan Lokasi") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = BengkelRed) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, tint = BengkelGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Titik GPS Realtime: -6.8887, 109.6753 (Akurat)",
                            fontSize = 11.sp,
                            color = Slate700
                        )
                    }
                }
            }
        }

        // Rincian Biaya Tetap
        selectedService?.let { s ->
            Surface(
                color = Slate900,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Tarif Resmi:", color = Slate300, fontSize = 12.sp)
                        Text(
                            text = currencyFormat.format(s.basePrice),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "Harga Terjamin",
                        color = BengkelAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tombol Submit
        Button(
            onClick = {
                val service = selectedService
                if (service == null) {
                    errorMessage = "Silakan pilih salah satu layanan."
                    return@Button
                }
                if (vehicleType.isBlank()) {
                    errorMessage = "Tipe kendaraan wajib diisi."
                    return@Button
                }
                if (problemDescription.isBlank()) {
                    errorMessage = "Deskripsi kerusakan wajib diisi agar montir dapat menyiapkan peralatan."
                    return@Button
                }
                isSubmitting = true
                errorMessage = null

                viewModel.orderNearestMechanic(
                    service = service,
                    vehicleType = vehicleType.trim(),
                    problemDescription = problemDescription.trim(),
                    locationAddress = locationAddress.trim(),
                    locationLat = customer?.latitude ?: -6.8887,
                    locationLng = customer?.longitude ?: 109.6753
                ) {
                    isSubmitting = false
                    onOrderSubmitted()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cari Montir Terdekat Sekarang", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// -------------------------------------------------------------
// 3. STATUS PESANAN REALTIME (9-STAGE TRACKER + PAYMENT + REVIEW)
// -------------------------------------------------------------
@Composable
fun CustomerActiveOrderScreen(
    viewModel: BengkelViewModel,
    onOpenChat: () -> Unit,
    onOrderFinished: () -> Unit
) {
    val activeOrder by viewModel.customerActiveOrder.collectAsState()
    val activeMechanics by viewModel.activeMechanics.collectAsState()
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    if (activeOrder == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BengkelGreen, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tidak Ada Pesanan Aktif",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Anda dapat memesan montir kapan pun Anda membutuhkan bantuan servis.",
                    fontSize = 13.sp,
                    color = Slate500,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onOrderFinished,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary)
                ) {
                    Text("Kembali ke Beranda")
                }
            }
        }
        return
    }

    val order = activeOrder!!
    val currentStatus = runCatching { OrderStatus.valueOf(order.status) }.getOrDefault(OrderStatus.WAITING_MECHANIC)

    // Check if additional fee approval is required
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ID: ${order.orderId}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500
                )
                Text(
                    text = order.serviceName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate900
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BengkelAmberDark)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = currentStatus.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live GPS Map Visualizer
        InteractiveMapCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            customerLat = order.customerLat,
            customerLng = order.customerLng,
            activeMechanics = activeMechanics,
            routeActive = currentStatus == OrderStatus.HEADING_TO_LOCATION,
            distanceKm = order.distanceKm,
            mechanicEtaMinutes = order.travelEtaMinutes
        )

        // 9 Stages Progress Tracker
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Proses Penanganan Kendaraan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(12.dp))

                val stages = listOf(
                    OrderStatus.WAITING_MECHANIC to "1. Mencari Montir",
                    OrderStatus.MECHANIC_FOUND to "2. Montir Ditemukan",
                    OrderStatus.MECHANIC_ACCEPTED to "3. Montir Menerima",
                    OrderStatus.HEADING_TO_LOCATION to "4. Menuju Lokasi",
                    OrderStatus.ARRIVED_AT_LOCATION to "5. Montir Tiba",
                    OrderStatus.WORKING_IN_PROGRESS to "6. Pengerjaan",
                    OrderStatus.WORK_COMPLETED to "7. Pengerjaan Selesai",
                    OrderStatus.WAITING_PAYMENT to "8. Pembayaran",
                    OrderStatus.COMPLETED to "9. Selesai"
                )

                val currentIndex = stages.indexOfFirst { it.first == currentStatus }.coerceAtLeast(0)

                stages.forEachIndexed { index, pair ->
                    val isPast = index < currentIndex
                    val isCurrent = index == currentIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPast -> BengkelGreen
                                        isCurrent -> BengkelAmberDark
                                        else -> Slate300
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPast) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = pair.second,
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) Slate900 else if (isPast) BengkelGreenDark else Slate400
                        )
                    }
                }
            }
        }

        // Montir Identity Card (If assigned)
        if (order.mechanicName != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Identitas Montir Ditugaskan", color = Slate400, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(BengkelAmberDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = order.mechanicName ?: "Montir",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = BengkelAmber, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${order.mechanicRating} • ${order.mechanicJobsCount} Pekerjaan",
                                    color = Slate300,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "Jarak: ${order.distanceKm} km (Est: ${order.travelEtaMinutes} mnt)",
                                color = BengkelAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contact buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenChat,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chat Montir", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                order.mechanicPhone?.let { phone ->
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Telepon", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Additional Fee Approval Card (If mechanic requested extra fee)
        if (order.additionalRequested) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BengkelAmberLight),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = BengkelAmberDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Persetujuan Tambahan Biaya",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Montir mengajukan penambahan biaya sebesar ${currencyFormat.format(order.additionalPrice)} untuk keperluan:",
                        fontSize = 12.sp,
                        color = Slate700
                    )
                    Text(
                        text = "\"${order.additionalNotes}\"",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Kebijakan Bengkelku: Montir dilarang memungut biaya tambahan tanpa persetujuan langsung dari customer di aplikasi.",
                        fontSize = 11.sp,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.answerAdditionalFee(order.orderId, isApproved = false) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Tolak", color = BengkelRed, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.answerAdditionalFee(order.orderId, isApproved = true) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
                        ) {
                            Text("Setujui Biaya", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Work Completion Summary & Payment Action
        if (currentStatus == OrderStatus.WAITING_PAYMENT || currentStatus == OrderStatus.WORK_COMPLETED) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ringkasan Hasil Perbaikan & Biaya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (order.workDetails.isNotBlank()) {
                        Text(text = "Pengerjaan: ${order.workDetails}", fontSize = 12.sp, color = Slate700)
                    }
                    if (order.sparePartsUsed.isNotBlank()) {
                        Text(text = "Suku Cadang: ${order.sparePartsUsed}", fontSize = 12.sp, color = Slate700)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tarif Layanan Resmi:", fontSize = 12.sp, color = Slate600)
                        Text(currencyFormat.format(order.officialPrice), fontSize = 12.sp, color = Slate800)
                    }
                    if (order.additionalApprovedByCustomer) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tambahan Suku Cadang:", fontSize = 12.sp, color = Slate600)
                            Text("+ ${currencyFormat.format(order.additionalPrice)}", fontSize = 12.sp, color = Slate800)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Tagihan:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                        Text(
                            text = currencyFormat.format(order.totalAmount),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = BengkelGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showPaymentDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Pembayaran & Lunasi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Rating & Review (if order is completed)
        if (currentStatus == OrderStatus.COMPLETED) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pesanan Selesai! Beri Penilaian Montir",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (order.ratingGiven != null && order.ratingGiven!! > 0) {
                        Text(
                            text = "Anda telah memberikan rating: ${order.ratingGiven} Bintang ★",
                            color = BengkelAmberDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (!order.reviewComment.isNullOrBlank()) {
                            Text(
                                text = "\"${order.reviewComment}\"",
                                fontSize = 12.sp,
                                color = Slate600,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = { showRatingDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BengkelAmberDark)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Beri Rating & Ulasan Montir", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Payment Dialog
    if (showPaymentDialog) {
        var selectedMethod by remember { mutableStateOf("TUNAI") }
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = {
                Text("Konfirmasi Pembayaran", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Total Tagihan: ${currencyFormat.format(order.totalAmount)}", fontWeight = FontWeight.Bold, color = BengkelGreenDark)
                    Text("Pilih metode pembayaran:", fontSize = 13.sp, color = Slate600)

                    listOf(
                        "TUNAI" to "Tunai Langsung ke Montir",
                        "QRIS" to "QRIS / Dompet Digital (GoPay, OVO, Dana)",
                        "TRANSFER" to "Transfer Bank Virtual Account"
                    ).forEach { (method, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedMethod == method) BengkelBlueLight else Slate100)
                                .clickable { selectedMethod = method }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 13.sp, fontWeight = if (selectedMethod == method) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitPayment(order.orderId, selectedMethod)
                        showPaymentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
                ) {
                    Text("Konfirmasi & Bayar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Rating & Review Dialog
    if (showRatingDialog) {
        var ratingStars by remember { mutableStateOf(5) }
        var reviewText by remember { mutableStateOf("Pelayanan montir sangat ramah, cepat sampai, dan kendaraan normal kembali!") }

        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = {
                Text("Penilaian Kinerja Montir", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Berikan bintang untuk ${order.mechanicName ?: "Montir"}:", fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { ratingStars = star }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$star Bintang",
                                    tint = if (star <= ratingStars) BengkelAmber else Slate300,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Tulis Ulasan Anda") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitRatingReview(order.orderId, ratingStars, reviewText.trim())
                        showRatingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BengkelAmberDark)
                ) {
                    Text("Kirim Penilaian")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 4. DAFTAR HARGA RESMI LAYANAN BENGKEL
// -------------------------------------------------------------
@Composable
fun CustomerServiceCatalogScreen(viewModel: BengkelViewModel) {
    val services by viewModel.allServices.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Daftar Tarif Resmi Bengkelku",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Seluruh tarif dasar ditetapkan resmi oleh Admin untuk mencegah manipulasi harga.",
            fontSize = 12.sp,
            color = Slate500
        )
        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(services) { service ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = service.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Slate900
                            )
                            Text(
                                text = currencyFormat.format(service.basePrice),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = BengkelGreenDark
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = service.description,
                            fontSize = 12.sp,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kategori: ${service.category}",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                            Text(
                                text = "Estimasi Pengerjaan: ~${service.estimatedDurationMinutes} menit",
                                fontSize = 11.sp,
                                color = Slate500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. RIWAYAT PESANAN CUSTOMER
// -------------------------------------------------------------
@Composable
fun CustomerHistoryScreen(viewModel: BengkelViewModel) {
    val history by viewModel.customerOrderHistory.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Riwayat Pesanan Anda",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada riwayat pesanan servis kendaraan.",
                    color = Slate400,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(history) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = order.serviceName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = order.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.status == "COMPLETED") BengkelGreenDark else BengkelAmberDark
                                )
                            }
                            Text(
                                text = dateFormat.format(Date(order.createdAt)),
                                fontSize = 11.sp,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Kendaraan: ${order.vehicleType}",
                                fontSize = 12.sp,
                                color = Slate700
                            )
                            Text(
                                text = "Montir: ${order.mechanicName ?: "Tidak Ditugaskan"}",
                                fontSize = 12.sp,
                                color = Slate700
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Biaya: ${currencyFormat.format(order.totalAmount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BengkelGreenDark
                                )
                                if (order.ratingGiven != null) {
                                    Text(
                                        text = "${order.ratingGiven} ★",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = BengkelAmberDark
                                    )
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
// 6. CHAT REALTIME DENGAN MONTIR
// -------------------------------------------------------------
@Composable
fun CustomerChatScreen(
    viewModel: BengkelViewModel,
    onBack: () -> Unit
) {
    val activeOrder by viewModel.customerActiveOrder.collectAsState()
    var messageInput by remember { mutableStateOf("") }
    val messages by if (activeOrder != null) {
        viewModel.getMessagesFlow(activeOrder!!.orderId).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<MessageEntity>()) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header
        Surface(
            color = Slate900,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Chat dengan ${activeOrder?.mechanicName ?: "Montir"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Online • Pesanan ${activeOrder?.orderId ?: ""}",
                        color = BengkelGreen,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isCustomer = msg.senderRole == "CUSTOMER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isCustomer) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isCustomer) 14.dp else 2.dp,
                                    bottomEnd = if (isCustomer) 2.dp else 14.dp
                                )
                            )
                            .background(if (isCustomer) BengkelBluePrimary else Slate200)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.text,
                                color = if (isCustomer) Color.White else Slate900,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Message Input Box
        Surface(
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Tulis pesan untuk montir...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank() && activeOrder != null) {
                            viewModel.sendChatMessage(activeOrder!!.orderId, messageInput.trim())
                            messageInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BengkelBluePrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. PROFIL CUSTOMER
// -------------------------------------------------------------
@Composable
fun CustomerProfileScreen(viewModel: BengkelViewModel) {
    val customer by viewModel.currentCustomer.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profil Customer",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BengkelBluePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = customer?.fullName ?: "Nama Customer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Customer Terverifikasi OTP",
                    fontSize = 12.sp,
                    color = BengkelGreenDark,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    "Email" to (customer?.email ?: "-"),
                    "Nomor HP" to (customer?.phone ?: "-"),
                    "Alamat Tempat Tinggal" to (customer?.address ?: "-"),
                    "Kendaraan Utama" to (customer?.defaultVehicle ?: "-"),
                    "Koordinat GPS" to "${customer?.latitude ?: -6.8887}, ${customer?.longitude ?: 109.6753}"
                ).forEach { (label, value) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(label, fontSize = 11.sp, color = Slate400)
                        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate800)
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BengkelRed)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar dari Akun")
        }
    }
}
