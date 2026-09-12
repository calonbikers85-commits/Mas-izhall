package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.BengkelViewModel

/**
 * Floating / Top-Header Tester Pill for effortless multi-role testing by anyone.
 */
@Composable
fun TesterRoleSwitchFloatingBadge(
    currentRole: UserRole?,
    onOpenTesterHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentRole == null) return

    val roleLabel = when (currentRole) {
        UserRole.CUSTOMER -> "Customer"
        UserRole.MECHANIC -> "Montir"
        UserRole.ADMIN -> "Admin"
    }

    val roleColor = when (currentRole) {
        UserRole.CUSTOMER -> BengkelBluePrimary
        UserRole.MECHANIC -> BengkelAmberDark
        UserRole.ADMIN -> BengkelRed
    }

    Surface(
        onClick = onOpenTesterHub,
        modifier = modifier
            .testTag("tester_hub_floating_badge")
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Slate900.copy(alpha = 0.92f),
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, roleColor.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BengkelGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Uji Coba: ",
                color = Slate400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = roleLabel,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = roleColor.copy(alpha = 0.25f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Ganti Peran",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Ganti",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Modal Dialog for switching roles, resetting demo data, and reading test scenarios.
 */
@Composable
fun TesterHubDialog(
    viewModel: BengkelViewModel,
    currentRole: UserRole?,
    onDismiss: () -> Unit
) {
    var isResetting by remember { mutableStateOf(false) }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showScenarioHelp by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("tester_hub_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BengkelBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Science,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pusat Uji Coba Pengguna",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "Beralih peran & kelola data demo seketika",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Slate600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Success notification if reset performed
                AnimatedVisibility(
                    visible = resetSuccessMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BengkelGreenLight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BengkelGreenDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = resetSuccessMessage ?: "",
                                fontSize = 11.sp,
                                color = BengkelGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Subtitle
                Text(
                    text = "PILIH PERAN UNTUK DIUJI COBA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Role Option 1: Customer
                TesterRoleOptionCard(
                    title = "Customer (Pemilik Kendaraan)",
                    subtitle = "Bambang Pamungkas • 081234567890",
                    description = "Pesan montir darurat, cek tarif 11 layanan, lacak GPS real-time, chat & bayar.",
                    isSelected = currentRole == UserRole.CUSTOMER,
                    accentColor = BengkelBluePrimary,
                    icon = Icons.Default.Person,
                    onClick = {
                        viewModel.switchDemoAccount(UserRole.CUSTOMER)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Option 2: Mechanic
                TesterRoleOptionCard(
                    title = "Montir Panggilan (Mitra Bengkel)",
                    subtitle = "Budi Santoso • 081298765431",
                    description = "Terima order masuk, rute peta, update status 'Menuju Lokasi' & selesaikan servis.",
                    isSelected = currentRole == UserRole.MECHANIC,
                    accentColor = BengkelAmberDark,
                    icon = Icons.Default.Build,
                    onClick = {
                        viewModel.switchDemoAccount(UserRole.MECHANIC)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Option 3: Admin
                TesterRoleOptionCard(
                    title = "Administrator (Owner Bengkelku)",
                    subtitle = "Admin Utama • Calonbikers85@gmail.com",
                    description = "Edit tarif resmi 11 layanan, verifikasi montir baru, lihat grafik omzet & audit log.",
                    isSelected = currentRole == UserRole.ADMIN,
                    accentColor = BengkelRed,
                    icon = Icons.Default.Security,
                    onClick = {
                        viewModel.switchDemoAccount(UserRole.ADMIN)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons (Reset Data & Skenario)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isResetting = true
                            viewModel.resetDemoData {
                                isResetting = false
                                resetSuccessMessage = "Data pesanan berhasil di-reset! Siap untuk simulasi baru."
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_demo_data_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isResetting,
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        if (isResetting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Data", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = { showScenarioHelp = !showScenarioHelp },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (showScenarioHelp) "Tutup Panduan" else "Panduan Uji",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Guided Test Scenarios
                AnimatedVisibility(visible = showScenarioHelp) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate50,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "💡 Alur Uji Coba Cepat (End-to-End):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1. Customer: Pilih layanan (cth. Ganti Oli) -> Buat Pesanan Panggil Montir.",
                                fontSize = 11.sp,
                                color = Slate700
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "2. Ganti Peran: Klik 'Montir' di panel ini -> Buka tab Pesanan Masuk -> Terima pesanan & klik 'Menuju Lokasi'.",
                                fontSize = 11.sp,
                                color = Slate700
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "3. Kembali ke Customer: Lihat montir di peta interaktif -> Bayar invoice setelah pengerjaan selesai.",
                                fontSize = 11.sp,
                                color = Slate700
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "4. Ganti ke Admin: Buka dashboard Admin -> Cek log audit, atur tarif baru, atau verifikasi montir.",
                                fontSize = 11.sp,
                                color = Slate700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Logout button
                TextButton(
                    onClick = {
                        viewModel.logout()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Keluar ke Halaman Awal (Login/Daftar)",
                        color = Slate600,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TesterRoleOptionCard(
    title: String,
    subtitle: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.08f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) accentColor else Slate200
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) accentColor else Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Slate600,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) accentColor else Slate900
                    )
                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor
                        ) {
                            Text(
                                text = "Aktif",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = Slate600,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
