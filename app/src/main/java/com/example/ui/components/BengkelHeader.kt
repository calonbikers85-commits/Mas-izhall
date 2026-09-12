package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BengkelTopAppBar(
    currentRole: UserRole?,
    userName: String,
    notifications: List<NotificationEntity> = emptyList(),
    onSwitchRole: ((UserRole) -> Unit)? = null,
    onResetDemoData: (() -> Unit)? = null,
    onLogout: () -> Unit = {}
) {
    var showNotifDialog by remember { mutableStateOf(false) }
    var showRoleSwitchDialog by remember { mutableStateOf(false) }
    var resetNoticeMessage by remember { mutableStateOf<String?>(null) }
    val unreadCount = notifications.count { !it.isRead }

    Surface(
        color = Slate900,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Identity
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(BengkelBluePrimary, BengkelAmber)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Logo Bengkelku",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BENGKELKU",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            currentRole?.let { role ->
                                val badgeBg = when (role) {
                                    UserRole.CUSTOMER -> BengkelBluePrimary
                                    UserRole.MECHANIC -> BengkelAmberDark
                                    UserRole.ADMIN -> BengkelRed
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = role.name,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Montir Dekat, Bantuan Cepat.",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Actions: Switch Role (Demo helper), Notifications, Logout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onSwitchRole != null) {
                        Surface(
                            onClick = { showRoleSwitchDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Slate800,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BengkelBlueCyan.copy(alpha = 0.6f)),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Ganti Peran",
                                    tint = BengkelBlueCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ganti Peran",
                                    color = BengkelBlueCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showNotifDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = Color.White
                            )
                        }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(BengkelRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Keluar Akun",
                            tint = Slate400
                        )
                    }
                }
            }
        }
    }

    // Role Switch Dialog for testing all roles seamlessly
    if (showRoleSwitchDialog && onSwitchRole != null) {
        AlertDialog(
            onDismissRequest = {
                showRoleSwitchDialog = false
                resetNoticeMessage = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Science,
                        contentDescription = null,
                        tint = BengkelBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pusat Uji Coba Pengguna",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Slate900
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Beralih peran seketika untuk menguji coba alur aplikasi:",
                        fontSize = 12.sp,
                        color = Slate600
                    )

                    if (resetNoticeMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BengkelGreenLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = resetNoticeMessage ?: "",
                                fontSize = 11.sp,
                                color = BengkelGreenDark,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Customer Option
                    Button(
                        onClick = {
                            onSwitchRole(UserRole.CUSTOMER)
                            showRoleSwitchDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Customer (Bambang Pamungkas)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Pesan montir darurat, live map GPS & bayar", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }

                    // Mechanic Option
                    Button(
                        onClick = {
                            onSwitchRole(UserRole.MECHANIC)
                            showRoleSwitchDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelAmberDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Montir (Budi Santoso - Aktif)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Terima pesanan, update lokasi & selesai servis", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }

                    // Admin Option
                    Button(
                        onClick = {
                            onSwitchRole(UserRole.ADMIN)
                            showRoleSwitchDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BengkelRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Admin (Calonbikers85@gmail.com)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Kelola tarif 11 layanan, verifikasi montir & omzet", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }

                    if (onResetDemoData != null) {
                        OutlinedButton(
                            onClick = {
                                onResetDemoData()
                                resetNoticeMessage = "✓ Data pesanan di-reset ke kondisi awal."
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Data Pengujian (Mulai dari Nol)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showRoleSwitchDialog = false
                    resetNoticeMessage = null
                }) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Notifications Dialog
    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BengkelAmber)
                    Text("Notifikasi Realtime", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada notifikasi baru.",
                                color = Slate400,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
                        notifications.take(15).forEach { notif ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!notif.isRead) BengkelBlueLight else Slate100
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Slate900
                                        )
                                        Text(
                                            text = dateFormat.format(Date(notif.createdAt)),
                                            fontSize = 10.sp,
                                            color = Slate500
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 12.sp,
                                        color = Slate700
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
