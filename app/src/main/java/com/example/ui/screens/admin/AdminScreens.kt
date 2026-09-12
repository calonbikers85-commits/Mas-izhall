package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.BengkelTopAppBar
import com.example.ui.theme.*
import com.example.viewmodel.BengkelViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminMainScreen(viewModel: BengkelViewModel) {
    var currentAdminTab by remember { mutableStateOf("dashboard") }
    // dashboard, mechanics, orders, services, customers, reports, audit_logs
    val notifications by viewModel.userNotifications.collectAsState()
    val authState by viewModel.authState.collectAsState()

    Scaffold(
        topBar = {
            BengkelTopAppBar(
                currentRole = UserRole.ADMIN,
                userName = "Super Admin (Calonbikers85@gmail.com)",
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
                listOf(
                    Triple("dashboard", "Statistik", Icons.Default.Dashboard),
                    Triple("mechanics", "Montir", Icons.Default.Engineering),
                    Triple("orders", "Pesanan", Icons.Default.ReceiptLong),
                    Triple("services", "Tarif Resmi", Icons.Default.PriceCheck),
                    Triple("audit_logs", "Audit Log", Icons.Default.HistoryEdu)
                ).forEach { (tabId, label, icon) ->
                    NavigationBarItem(
                        selected = currentAdminTab == tabId,
                        onClick = { currentAdminTab = tabId },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BengkelRed,
                            selectedTextColor = BengkelRed,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400,
                            indicatorColor = Slate800
                        )
                    )
                }
            }
        },
        containerColor = Slate50
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentAdminTab) {
                "dashboard" -> AdminDashboardTab(
                    viewModel = viewModel,
                    onNavigateTo = { currentAdminTab = it }
                )
                "mechanics" -> AdminMechanicsManagementTab(viewModel = viewModel)
                "orders" -> AdminOrdersManagementTab(viewModel = viewModel)
                "services" -> AdminServicesAndPricesTab(viewModel = viewModel)
                "audit_logs" -> AdminAuditLogsTab(viewModel = viewModel)
                "customers" -> AdminCustomersTab(viewModel = viewModel)
                "reports" -> AdminReportsTab(viewModel = viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. ADMIN DASHBOARD & KEY METRICS TAB
// -------------------------------------------------------------
@Composable
fun AdminDashboardTab(
    viewModel: BengkelViewModel,
    onNavigateTo: (String) -> Unit
) {
    val totalCust by viewModel.customerCount.collectAsState()
    val totalMech by viewModel.totalMechanicsCount.collectAsState()
    val activeMech by viewModel.activeMechanicsCount.collectAsState()
    val offlineMech by viewModel.offlineMechanicsCount.collectAsState()
    val totalOrders by viewModel.totalOrdersCount.collectAsState()
    val completedOrders by viewModel.completedOrdersCount.collectAsState()
    val runningOrders by viewModel.runningOrdersCount.collectAsState()
    val cancelledOrders by viewModel.cancelledOrdersCount.collectAsState()
    val totalRev by viewModel.totalRevenue.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Banner
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
                        Text("PANEL KONTROL ADMINISTRATOR", color = BengkelRed, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text("Ringkasan Statistik Sistem", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BengkelRed)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("ROOT ACCESS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate700)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Total Omzet Transaksi Selesai:", color = Slate400, fontSize = 12.sp)
                Text(
                    text = currencyFormat.format(totalRev),
                    color = BengkelGreen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Quick Navigation Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigateTo("customers") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Customer", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { onNavigateTo("reports") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Rekap Finansial", fontSize = 12.sp)
            }
        }

        Text("Statistik Mitra Montir", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(modifier = Modifier.weight(1f), title = "Total Montir", value = "$totalMech", color = BengkelBlueDeep)
            MetricCard(modifier = Modifier.weight(1f), title = "Montir Aktif", value = "$activeMech Online", color = BengkelGreenDark)
            MetricCard(modifier = Modifier.weight(1f), title = "Montir Offline", value = "$offlineMech", color = Slate500)
        }

        Text("Statistik Pesanan Layanan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(modifier = Modifier.weight(1f), title = "Total Pesanan", value = "$totalOrders", color = Slate900)
            MetricCard(modifier = Modifier.weight(1f), title = "Pekerjaan Selesai", value = "$completedOrders", color = BengkelGreenDark)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(modifier = Modifier.weight(1f), title = "Sedang Berjalan", value = "$runningOrders", color = BengkelAmberDark)
            MetricCard(modifier = Modifier.weight(1f), title = "Dibatalkan", value = "$cancelledOrders", color = BengkelRed)
        }

        Text("Data Pengguna Aplikasi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
        MetricCard(modifier = Modifier.fillMaxWidth(), title = "Total Customer Terverifikasi OTP", value = "$totalCust Customer", color = BengkelBluePrimary)
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = Slate500)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

// -------------------------------------------------------------
// 2. MANAJEMEN MONTIR (APPROVAL, SUSPEND, REJECT)
// -------------------------------------------------------------
@Composable
fun AdminMechanicsManagementTab(viewModel: BengkelViewModel) {
    val mechanics by viewModel.allMechanics.collectAsState()
    var filterStatus by remember { mutableStateOf("ALL") }

    val filtered = when (filterStatus) {
        "WAITING_APPROVAL" -> mechanics.filter { it.status == MechanicStatus.WAITING_APPROVAL.name }
        "ACTIVE" -> mechanics.filter { it.status == MechanicStatus.ACTIVE.name }
        "OFFLINE" -> mechanics.filter { it.status == MechanicStatus.OFFLINE.name }
        "SUSPENDED" -> mechanics.filter { it.status == MechanicStatus.SUSPENDED.name }
        else -> mechanics
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Manajemen Mitra Montir", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Verifikasi pendaftaran calon montir & kelola hak akses operasional.", fontSize = 12.sp, color = Slate500)
        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "ALL" to "Semua (${mechanics.size})",
                "WAITING_APPROVAL" to "Persetujuan",
                "ACTIVE" to "Aktif",
                "SUSPENDED" to "Ditangguhkan"
            ).forEach { (statusKey, label) ->
                val isSelected = filterStatus == statusKey
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BengkelRed else Slate200)
                        .clickable { filterStatus = statusKey }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Slate700,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada montir pada kategori status ini.", color = Slate400, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { mechanic ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mechanic.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (mechanic.status) {
                                                "ACTIVE" -> BengkelGreenLight
                                                "WAITING_APPROVAL" -> BengkelAmberLight
                                                "SUSPENDED" -> BengkelRedLight
                                                else -> Slate200
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = mechanic.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (mechanic.status) {
                                            "ACTIVE" -> BengkelGreenDark
                                            "WAITING_APPROVAL" -> BengkelAmberDark
                                            "SUSPENDED" -> BengkelRed
                                            else -> Slate700
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("No. HP: ${mechanic.phone} • Email: ${mechanic.email}", fontSize = 12.sp, color = Slate600)
                            Text("Keahlian: ${mechanic.skills}", fontSize = 12.sp, color = Slate700)
                            Text("Pengalaman: ${mechanic.experienceYears} Tahun • Area: ${mechanic.workLocation}", fontSize = 12.sp, color = Slate600)
                            Text(
                                "Rating: ${mechanic.rating} ★ (${mechanic.reviewCount} ulasan) • Penolakan: ${mechanic.rejectionCount}x",
                                fontSize = 12.sp,
                                color = if (mechanic.rejectionCount > 3) BengkelRed else Slate600,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (mechanic.status == MechanicStatus.WAITING_APPROVAL.name) {
                                    Button(
                                        onClick = { viewModel.approveMechanic(mechanic.mechanicId) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
                                    ) {
                                        Text("Setujui Akun", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.rejectMechanic(mechanic.mechanicId, "Dokumen/kualifikasi belum memenuhi standar") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Tolak", fontSize = 12.sp, color = BengkelRed)
                                    }
                                } else if (mechanic.status == MechanicStatus.ACTIVE.name || mechanic.status == MechanicStatus.OFFLINE.name) {
                                    OutlinedButton(
                                        onClick = { viewModel.suspendMechanic(mechanic.mechanicId) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Tangguhkan / Suspend", fontSize = 11.sp, color = BengkelRed)
                                    }
                                } else if (mechanic.status == MechanicStatus.SUSPENDED.name) {
                                    Button(
                                        onClick = { viewModel.approveMechanic(mechanic.mechanicId) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BengkelGreenDark)
                                    ) {
                                        Text("Aktifkan Kembali", fontSize = 12.sp)
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
// 3. MANAJEMEN PESANAN ADMIN
// -------------------------------------------------------------
@Composable
fun AdminOrdersManagementTab(viewModel: BengkelViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Pantau Seluruh Pesanan Layanan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Total ${orders.size} transaksi tercatat di database", fontSize = 12.sp, color = Slate500)
        Spacer(modifier = Modifier.height(14.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada pesanan masuk.", color = Slate400)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(orders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ID: ${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (order.status == "COMPLETED") BengkelGreenLight else BengkelAmberLight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = order.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.status == "COMPLETED") BengkelGreenDark else BengkelAmberDark
                                    )
                                }
                            }
                            Text(dateFormat.format(Date(order.createdAt)), fontSize = 11.sp, color = Slate400)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Layanan: ${order.serviceName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 12.sp, color = Slate700)
                            Text("Montir: ${order.mechanicName ?: "Belum Ditugaskan"}", fontSize = 12.sp, color = Slate700)
                            Text("Lokasi: ${order.customerAddress}", fontSize = 11.sp, color = Slate600)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Biaya Dasar: ${currencyFormat.format(order.officialPrice)}", fontSize = 11.sp, color = Slate500)
                                if (order.additionalPrice > 0) {
                                    Text("Tambahan: ${currencyFormat.format(order.additionalPrice)}", fontSize = 11.sp, color = BengkelAmberDark)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Tagihan:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                Text(currencyFormat.format(order.totalAmount), fontWeight = FontWeight.Black, fontSize = 14.sp, color = BengkelGreenDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. MANAJEMEN LAYANAN & HARGA RESMI BENGKEL
// -------------------------------------------------------------
@Composable
fun AdminServicesAndPricesTab(viewModel: BengkelViewModel) {
    val services by viewModel.allServices.collectAsState()
    val priceHistories by viewModel.priceHistories.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))

    var showAddDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<WorkshopServiceEntity?>(null) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tarif Layanan Resmi", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Text("Ditetapkan resmi oleh Admin & tercatat pada audit", fontSize = 12.sp, color = Slate500)
            }
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BengkelRed)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { showHistoryDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.History, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lihat Riwayat Perubahan Harga (${priceHistories.size} Catatan)", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(services) { service ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(service.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                            Text(currencyFormat.format(service.basePrice), fontWeight = FontWeight.Black, fontSize = 14.sp, color = BengkelGreenDark)
                        }
                        Text(service.description, fontSize = 11.sp, color = Slate600)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Durasi: ~${service.estimatedDurationMinutes} menit", fontSize = 11.sp, color = Slate400)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { serviceToEdit = service }) {
                                    Text("Ubah Tarif", fontSize = 12.sp, color = BengkelBluePrimary, fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { viewModel.deleteService(service.serviceId, service.name) }) {
                                    Text("Hapus", fontSize = 12.sp, color = BengkelRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Tambah Layanan Baru
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Servis") }
        var desc by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var duration by remember { mutableStateOf("30") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Layanan Bengkel Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Layanan") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi Singkat") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga Dasar Resmi (Rp)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Estimasi Durasi (Menit)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val basePrice = price.toLongOrNull() ?: 50000L
                        val dur = duration.toIntOrNull() ?: 30
                        val newService = WorkshopServiceEntity(
                            serviceId = "SRV-" + System.currentTimeMillis().toString().takeLast(6),
                            name = name.trim(),
                            category = category.trim(),
                            description = desc.trim(),
                            basePrice = basePrice,
                            estimatedDurationMinutes = dur
                        )
                        viewModel.addNewService(newService)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BengkelRed)
                ) {
                    Text("Simpan Layanan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }

    // Modal: Ubah Tarif Resmi & Alasan Audit
    if (serviceToEdit != null) {
        val s = serviceToEdit!!
        var newPriceInput by remember { mutableStateOf(s.basePrice.toString()) }
        var reasonInput by remember { mutableStateOf("Penyesuaian biaya operasional montir & standar bengkel") }

        AlertDialog(
            onDismissRequest = { serviceToEdit = null },
            title = { Text("Ubah Tarif Resmi: ${s.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tarif Lama: ${currencyFormat.format(s.basePrice)}", color = Slate600, fontSize = 12.sp)
                    OutlinedTextField(
                        value = newPriceInput,
                        onValueChange = { newPriceInput = it },
                        label = { Text("Tarif Baru Resmi (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text("Alasan Perubahan (Wajib Dicatat pada Audit Log)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPrice = newPriceInput.toLongOrNull() ?: s.basePrice
                        val updatedService = s.copy(basePrice = newPrice)
                        viewModel.updateServicePrice(updatedService, s.basePrice, reasonInput.trim())
                        serviceToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BengkelRed)
                ) {
                    Text("Perbarui & Catat Audit")
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceToEdit = null }) { Text("Batal") }
            }
        )
    }

    // Modal: Log Riwayat Perubahan Harga
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("Riwayat Perubahan Harga Resmi", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    if (priceHistories.isEmpty()) {
                        Text("Belum ada catatan perubahan harga.", color = Slate400, fontSize = 12.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(priceHistories) { hist ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Slate100)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(hist.serviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "${currencyFormat.format(hist.oldPrice)} -> ${currencyFormat.format(hist.newPrice)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BengkelGreenDark
                                        )
                                        Text("Oleh: ${hist.changedByAdmin} • ${dateFormat.format(Date(hist.changedAt))}", fontSize = 10.sp, color = Slate500)
                                        Text("Alasan: \"${hist.reason}\"", fontSize = 11.sp, color = Slate700)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) { Text("Tutup") }
            }
        )
    }
}

// -------------------------------------------------------------
// 5. AUDIT LOG TAB
// -------------------------------------------------------------
@Composable
fun AdminAuditLogsTab(viewModel: BengkelViewModel) {
    val auditLogs by viewModel.auditLogs.collectAsState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id", "ID"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Log Audit Sistem Bengkelku", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Rekam jejak setiap aksi penting seluruh aktor demi transparansi & keamanan sistem.", fontSize = 12.sp, color = Slate500)
        Spacer(modifier = Modifier.height(14.dp))

        if (auditLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada log audit tercatat.", color = Slate400)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(auditLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (log.actorRole) {
                                                "ADMIN" -> BengkelRed
                                                "MECHANIC" -> BengkelAmberDark
                                                else -> BengkelBluePrimary
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(log.actorRole, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(dateFormat.format(Date(log.timestamp)), fontSize = 10.sp, color = Slate400)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Aksi: ${log.action}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900)
                            Text(log.details, fontSize = 12.sp, color = Slate700)
                            Text("Aktor ID: ${log.actorId}", fontSize = 10.sp, color = Slate400)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. CUSTOMER DATA TAB
// -------------------------------------------------------------
@Composable
fun AdminCustomersTab(viewModel: BengkelViewModel) {
    val customers by viewModel.allCustomers.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Daftar Customer Terdaftar", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(modifier = Modifier.height(4.dp))
        Text("${customers.size} Customer terverifikasi menggunakan aplikasi", fontSize = 12.sp, color = Slate500)
        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(customers) { c ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(c.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("OTP Terverifikasi", color = BengkelGreenDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("HP: ${c.phone} • Email: ${c.email}", fontSize = 12.sp, color = Slate600)
                        Text("Alamat: ${c.address}", fontSize = 12.sp, color = Slate700)
                        Text("Kendaraan: ${c.defaultVehicle}", fontSize = 11.sp, color = Slate500)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. FINANCIAL & OPERATIONAL REPORTS TAB
// -------------------------------------------------------------
@Composable
fun AdminReportsTab(viewModel: BengkelViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    val completed = orders.filter { it.status == "COMPLETED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Laporan & Rekap Finansial", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Text("Rekapitulasi operasional transaksi bengkel online & montir panggilan", fontSize = 12.sp, color = Slate500)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Total Pendapatan Transaksi Sukses", color = Slate400, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(currencyFormat.format(totalRevenue), color = BengkelGreen, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Berdasarkan ${completed.size} transaksi yang telah lunas", color = Slate300, fontSize = 12.sp)
            }
        }

        Text("Rincian Transaksi Selesai", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
        completed.forEach { o ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(o.serviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${o.customerName} • Montir: ${o.mechanicName ?: "-"}", fontSize = 11.sp, color = Slate500)
                    }
                    Text(currencyFormat.format(o.totalAmount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BengkelGreenDark)
                }
            }
        }
    }
}
