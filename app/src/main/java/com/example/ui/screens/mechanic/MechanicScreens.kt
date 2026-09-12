package com.example.ui.screens.mechanic

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.BengkelTopAppBar
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.theme.*
import com.example.viewmodel.BengkelViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MechanicMainScreen(viewModel: BengkelViewModel) {
    var currentTab by remember { mutableStateOf("dashboard") } // dashboard, active_job, history, chat, profile
    val authState by viewModel.authState.collectAsState()
    val currentMechanic by viewModel.currentMechanic.collectAsState()
    val activeOrder by viewModel.mechanicActiveOrder.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()

    val isOnline = currentMechanic?.status == MechanicStatus.ACTIVE.name

    // Check if there is an incoming job popup for this mechanic
    val hasIncomingRequest = activeOrder != null &&
            (activeOrder!!.status == OrderStatus.WAITING_MECHANIC.name || activeOrder!!.status == OrderStatus.MECHANIC_FOUND.name)

    Scaffold(
        topBar = {
            BengkelTopAppBar(
                currentRole = UserRole.MECHANIC,
                userName = currentMechanic?.fullName ?: authState.currentUserName,
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
                    selected = currentTab == "dashboard",
                    onClick = { currentTab = "dashboard" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BengkelAmber,
                        selectedTextColor = BengkelAmber,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Slate800
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "active_job",
                    onClick = { currentTab = "active_job" },
                    icon = {
                        BadgedBox(badge = {
                            if (activeOrder != null) {
                                Badge(containerColor = BengkelAmberDark) { Text("1") }
                            }
                        }) {
                            Icon(Icons.Default.Build, contentDescription = "Pekerjaan")
                        }
                    },
                    label = { Text("Pekerjaan", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BengkelAmber,
                        selectedTextColor = BengkelAmber,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Slate800
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "history",
                    onClick = { currentTab = "history" },
                    icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Pendapatan") },
                    label = { Text("Pendapatan", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BengkelAmber,
                        selectedTextColor = BengkelAmber,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Slate800
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "chat",
                    onClick = { currentTab = "chat" },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BengkelAmber,
                        selectedTextColor = BengkelAmber,
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
                        selectedIconColor = BengkelAmber,
                        selectedTextColor = BengkelAmber,
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
                "dashboard" -> MechanicDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToActiveJob = { currentTab = "active_job" }
                )
                "active_job" -> MechanicActiveJobScreen(
                    viewModel = viewModel,
                    onOpenChat = { currentTab = "chat" }
                )
                "history" -> MechanicEarningsScreen(viewModel = viewModel)
                "chat" -> MechanicChatScreen(viewModel = viewModel)
                "profile" -> MechanicProfileScreen(viewModel = viewModel)
            }

            // POPUP "PERMINTAAN PEKERJAAN BARU" (Triggered when online and order assigned)
            if (isOnline && hasIncomingRequest && activeOrder != null) {
                IncomingJobRequestDialog(
                    order = activeOrder!!,
                    onAccept = {
                        viewModel.acceptOrder(activeOrder!!.orderId)
                        currentTab = "active_job"
                    },
                    onReject = { reason ->
                        viewModel.rejectOrder(activeOrder!!.orderId, reason)
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// POPUP DIALOG: PERMINTAAN PEKERJAAN BARU
// -------------------------------------------------------------
@Composable
fun IncomingJobRequestDialog(
    order: OrderEntity,
    onAccept: () -> Unit,
    onReject: (String) -> Unit
) {
    var showRejectReasonDialog by remember { mutableStateOf(false) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    if (showRejectReasonDialog) {
        RejectReasonDialog(
            onDismiss = { showRejectReasonDialog = false },
            onConfirmReject = { reason ->
                showRejectReasonDialog = false
                onReject(reason)
            }
        )
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BengkelAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Slate900, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("PERMINTAAN PEKERJAAN BARU!", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Slate900)
                    Text("Panggilan servis darurat terdekat", fontSize = 11.sp, color = Slate500)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = BengkelBlueLight,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Layanan: ${order.serviceName}", fontWeight = FontWeight.Bold, color = BengkelBlueDeep, fontSize = 14.sp)
                        Text("Tarif Resmi: ${currencyFormat.format(order.officialPrice)}", fontWeight = FontWeight.SemiBold, color = BengkelGreenDark, fontSize = 13.sp)
                    }
                }

                Text("Nama Customer: ${order.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Kendaraan: ${order.vehicleType}", fontSize = 12.sp)
                Text("Keluhan Kerusakan: \"${order.problemDescription}\"", fontSize = 12.sp, color = Slate700)
                Text("Lokasi Customer: ${order.customerAddress}", fontSize = 12.sp, color = Slate600)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Jarak: ${order.distanceKm} km", fontWeight = FontWeight.Bold, color = BengkelAmberDark, fontSize = 13.sp)
                    Text("Estimasi Tempuh: ~${order.travelEtaMinutes} menit", fontWeight = FontWeight.Bold, color = BengkelBluePrimary, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("TERIMA PEKERJAAN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { showRejectReasonDialog = true }
            ) {
                Text("Tolak", color = BengkelRed, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// -------------------------------------------------------------
// DIALOG: ALASAN PENOLAKAN PESANAN
// -------------------------------------------------------------
@Composable
fun RejectReasonDialog(
    onDismiss: () -> Unit,
    onConfirmReject: (String) -> Unit
) {
    val reasons = listOf(
        "Terlalu jauh dari lokasi saya saat ini",
        "Tidak dapat menghubungi customer",
        "Sedang menangani urusan darurat",
        "Peralatan kerja/suku cadang spesifik tidak tersedia",
        "Alasan lainnya"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var customReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pilih Alasan Penolakan", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Alasan penolakan wajib dipilih untuk pencatatan riwayat montir:",
                    fontSize = 12.sp,
                    color = Slate600
                )
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedReason == reason) Slate200 else Color.Transparent)
                            .clickable { selectedReason = reason }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(reason, fontSize = 12.sp)
                    }
                }
                if (selectedReason == "Alasan lainnya") {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("Tuliskan alasan spesifik") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (selectedReason == "Alasan lainnya" && customReason.isNotBlank()) {
                        customReason
                    } else {
                        selectedReason
                    }
                    onConfirmReject(finalReason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BengkelRed)
            ) {
                Text("Konfirmasi Tolak")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// -------------------------------------------------------------
// 1. MECHANIC DASHBOARD SCREEN
// -------------------------------------------------------------
@Composable
fun MechanicDashboardScreen(
    viewModel: BengkelViewModel,
    onNavigateToActiveJob: () -> Unit
) {
    val mechanic by viewModel.currentMechanic.collectAsState()
    val activeOrder by viewModel.mechanicActiveOrder.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    val isOnline = mechanic?.status == MechanicStatus.ACTIVE.name

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card: Online / Offline Toggle
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
                            text = mechanic?.fullName ?: "Montir",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = BengkelAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${mechanic?.rating ?: 5.0} • ${mechanic?.reviewCount ?: 0} Ulasan",
                                color = Slate300,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Online / Offline Switch
                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { viewModel.setMechanicOnlineStatus(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BengkelGreen,
                                uncheckedThumbColor = Slate400,
                                uncheckedTrackColor = Slate700
                            )
                        )
                        Text(
                            text = if (isOnline) "ONLINE (SIAP)" else "OFFLINE",
                            color = if (isOnline) BengkelGreen else Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate700)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Area: ${mechanic?.workLocation ?: "Pekalongan"}",
                        color = Slate300,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Penolakan: ${mechanic?.rejectionCount ?: 0}x",
                        color = if ((mechanic?.rejectionCount ?: 0) > 3) BengkelRed else Slate400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Summary Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Pekerjaan", fontSize = 11.sp, color = Slate500)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${mechanic?.completedJobsCount ?: 0} Selesai",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BengkelBlueDeep
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Pendapatan", fontSize = 11.sp, color = Slate500)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormat.format(mechanic?.earnings ?: 0L),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = BengkelGreenDark
                    )
                }
            }
        }

        // Active Order Action Banner
        if (activeOrder != null) {
            val order = activeOrder!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToActiveJob() },
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
                            Icon(Icons.Default.Build, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pekerjaan Berjalan: ${order.serviceName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                            Text(
                                text = "Customer: ${order.customerName} (${order.distanceKm} km)",
                                fontSize = 12.sp,
                                color = Slate600
                            )
                        }
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate600)
                }
            }
        }

        // GPS Radar Mini Map
        Column {
            Text("Radar Pemantauan GPS Montir", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(modifier = Modifier.height(8.dp))
            InteractiveMapCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                customerLat = activeOrder?.customerLat ?: -6.8887,
                customerLng = activeOrder?.customerLng ?: 109.6753,
                routeActive = activeOrder?.status == OrderStatus.HEADING_TO_LOCATION.name,
                distanceKm = activeOrder?.distanceKm,
                mechanicEtaMinutes = activeOrder?.travelEtaMinutes
            )
        }
    }
}

// -------------------------------------------------------------
// 2. ACTIVE JOB PROCESS SCREEN (WORKFLOW & BUTTONS)
// -------------------------------------------------------------
@Composable
fun MechanicActiveJobScreen(
    viewModel: BengkelViewModel,
    onOpenChat: () -> Unit
) {
    val activeOrder by viewModel.mechanicActiveOrder.collectAsState()
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    var showAdditionalFeeModal by remember { mutableStateOf(false) }
    var showWorkFinishModal by remember { mutableStateOf(false) }

    if (activeOrder == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.WorkOutline, contentDescription = null, tint = Slate400, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Belum Ada Pekerjaan Aktif",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Pastikan status Anda ONLINE agar sistem dapat menugaskan order terdekat.",
                    fontSize = 13.sp,
                    color = Slate500
                )
            }
        }
        return
    }

    val order = activeOrder!!
    val currentStatus = runCatching { OrderStatus.valueOf(order.status) }.getOrDefault(OrderStatus.WAITING_MECHANIC)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Job Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Order: ${order.orderId}", fontSize = 12.sp, color = Slate500)
                Text(text = order.serviceName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BengkelAmberDark)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(currentStatus.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Customer Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Informasi Customer & Kendaraan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nama Customer: ${order.customerName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Kendaraan: ${order.vehicleType}", fontSize = 13.sp)
                Text("Keluhan: \"${order.problemDescription}\"", fontSize = 13.sp, color = Slate700)
                Text("Alamat: ${order.customerAddress}", fontSize = 12.sp, color = Slate600)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Jarak: ${order.distanceKm} km (Estimasi: ~${order.travelEtaMinutes} menit)", fontWeight = FontWeight.Bold, color = BengkelAmberDark, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenChat,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat Customer", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hubungi HP", fontSize = 12.sp)
                    }
                }
            }
        }

        // Action Workflow Buttons based on current status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Tahapan Pengerjaan Montir", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)

                // 1. Menuju Lokasi
                if (currentStatus == OrderStatus.MECHANIC_ACCEPTED) {
                    Button(
                        onClick = { viewModel.startHeadingToLocation(order.orderId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Berangkat Menuju Lokasi", fontWeight = FontWeight.Bold)
                    }
                }

                // 2. Tiba di Lokasi
                if (currentStatus == OrderStatus.HEADING_TO_LOCATION) {
                    Button(
                        onClick = { viewModel.markArrivedAtLocation(order.orderId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelAmberDark)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saya Sudah Tiba di Lokasi", fontWeight = FontWeight.Bold)
                    }
                }

                // 3. Mulai Pengerjaan
                if (currentStatus == OrderStatus.ARRIVED_AT_LOCATION) {
                    Button(
                        onClick = { viewModel.startWorking(order.orderId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Pengerjaan Kendaraan", fontWeight = FontWeight.Bold)
                    }
                }

                // 4. Working in progress options: Request additional fee OR finish work
                if (currentStatus == OrderStatus.WORKING_IN_PROGRESS) {
                    OutlinedButton(
                        onClick = { showAdditionalFeeModal = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = BengkelAmberDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ajukan Tambahan Biaya / Suku Cadang", color = BengkelAmberDark, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showWorkFinishModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PEKERJAAN SELESAI", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }

                if (currentStatus == OrderStatus.WAITING_PAYMENT) {
                    Surface(
                        color = BengkelAmberLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pekerjaan telah selesai dilaporkan. Menunggu konfirmasi pembayaran customer (${currencyFormat.format(order.totalAmount)}).",
                            color = BengkelAmberDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal: Pengajuan Tambahan Biaya
    if (showAdditionalFeeModal) {
        var addAmount by remember { mutableStateOf("") }
        var addNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAdditionalFeeModal = false },
            title = { Text("Ajukan Tambahan Biaya", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Biaya tambahan wajib disetujui oleh Customer sebelum dibebankan pada tagihan.",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                    OutlinedTextField(
                        value = addAmount,
                        onValueChange = { addAmount = it },
                        label = { Text("Nominal Tambahan (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addNotes,
                        onValueChange = { addNotes = it },
                        label = { Text("Rincian / Alasan Tambahan") },
                        placeholder = { Text("Contoh: Pembelian kampas rem baru merk Daytona...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = addAmount.toLongOrNull() ?: 0L
                        if (amount > 0 && addNotes.isNotBlank()) {
                            viewModel.requestAdditionalFee(order.orderId, amount, addNotes.trim())
                            showAdditionalFeeModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BengkelAmberDark)
                ) {
                    Text("Kirim ke Customer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdditionalFeeModal = false }) { Text("Batal") }
            }
        )
    }

    // Modal: Form Penyelesaian Pekerjaan
    if (showWorkFinishModal) {
        var workDetails by remember { mutableStateOf("Pembersihan karburator & penggantian busi") }
        var sparePartsUsed by remember { mutableStateOf("Busi NGK CR7HSA, Oli Mesin MPX 0.8L") }
        var workNotes by remember { mutableStateOf("Kondisi mesin sudah normal dan tarikan responsif kembali.") }

        AlertDialog(
            onDismissRequest = { showWorkFinishModal = false },
            title = { Text("Laporan Penyelesaian Pekerjaan", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Lengkapi detail hasil servis untuk transparansi kepada customer:",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                    OutlinedTextField(
                        value = workDetails,
                        onValueChange = { workDetails = it },
                        label = { Text("Detail Pengerjaan *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sparePartsUsed,
                        onValueChange = { sparePartsUsed = it },
                        label = { Text("Suku Cadang yang Digunakan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = workNotes,
                        onValueChange = { workNotes = it },
                        label = { Text("Catatan / Saran Teknisi") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitWorkFinished(
                            orderId = order.orderId,
                            workDetails = workDetails.trim(),
                            sparePartsUsed = sparePartsUsed.trim(),
                            workNotes = workNotes.trim(),
                            photoBefore = "photo_before_default",
                            photoAfter = "photo_after_default"
                        )
                        showWorkFinishModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
                ) {
                    Text("Kirim Laporan Selesai")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWorkFinishModal = false }) { Text("Batal") }
            }
        )
    }
}

// -------------------------------------------------------------
// 3. EARNINGS & RIWAYAT PENDAPATAN MONTIR
// -------------------------------------------------------------
@Composable
fun MechanicEarningsScreen(viewModel: BengkelViewModel) {
    val history by viewModel.mechanicOrderHistory.collectAsState()
    val mechanic by viewModel.currentMechanic.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Pendapatan & Riwayat Pekerjaan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Total Saldo Penghasilan", color = Slate400, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currencyFormat.format(mechanic?.earnings ?: 0L),
                    color = BengkelGreen,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${mechanic?.completedJobsCount ?: 0} Pekerjaan telah berhasil diselesaikan",
                    color = Slate300,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Daftar Pekerjaan Selesai", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada riwayat pengerjaan servis.", color = Slate400, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(order.serviceName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(currencyFormat.format(order.totalAmount), fontWeight = FontWeight.Bold, color = BengkelGreenDark)
                            }
                            Text(dateFormat.format(Date(order.createdAt)), fontSize = 11.sp, color = Slate400)
                            Text("Customer: ${order.customerName}", fontSize = 12.sp, color = Slate600)
                            if (order.ratingGiven != null) {
                                Text("Penilaian: ${order.ratingGiven} ★", fontSize = 12.sp, color = BengkelAmberDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. MECHANIC CHAT
// -------------------------------------------------------------
@Composable
fun MechanicChatScreen(viewModel: BengkelViewModel) {
    val activeOrder by viewModel.mechanicActiveOrder.collectAsState()
    var messageInput by remember { mutableStateOf("") }
    val messages by if (activeOrder != null) {
        viewModel.getMessagesFlow(activeOrder!!.orderId).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<MessageEntity>()) }
    }

    if (activeOrder == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Tidak ada percakapan aktif. Chat tersedia saat ada order berlangsung.", color = Slate400, fontSize = 13.sp)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = Slate900) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat dengan ${activeOrder?.customerName ?: "Customer"}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMechanic = msg.senderRole == "MECHANIC"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMechanic) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isMechanic) BengkelAmberDark else Slate200)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isMechanic) Color.White else Slate900,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Surface(color = Color.White, tonalElevation = 6.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Ketik balasan untuk customer...") },
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
                        .background(BengkelAmberDark)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. MECHANIC PROFILE
// -------------------------------------------------------------
@Composable
fun MechanicProfileScreen(viewModel: BengkelViewModel) {
    val mechanic by viewModel.currentMechanic.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profil Montir Mitra", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(BengkelAmberDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(mechanic?.fullName ?: "Montir", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            text = "Status: ${mechanic?.status ?: "ACTIVE"}",
                            color = BengkelGreenDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text("Rating: ${mechanic?.rating ?: 5.0} ★ (${mechanic?.reviewCount ?: 0} ulasan)", fontSize = 12.sp, color = Slate600)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    "Email" to (mechanic?.email ?: "-"),
                    "Nomor HP" to (mechanic?.phone ?: "-"),
                    "Keahlian" to (mechanic?.skills ?: "-"),
                    "Pengalaman" to "${mechanic?.experienceYears ?: 5} Tahun",
                    "Area Kerja" to (mechanic?.workLocation ?: "-"),
                    "Jumlah Penolakan" to "${mechanic?.rejectionCount ?: 0} kali"
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
