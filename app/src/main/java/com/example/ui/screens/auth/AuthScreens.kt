package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.BengkelViewModel

@Composable
fun AuthMainScreen(viewModel: BengkelViewModel) {
    var selectedRoleTab by remember { mutableStateOf(UserRole.CUSTOMER) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    // If OTP verification is pending for customer
    if (authState.isOtpPending) {
        OtpVerificationDialog(
            viewModel = viewModel,
            phone = authState.tempRegisterCustomer?.phone ?: "",
            secondsRemaining = authState.otpSecondsRemaining,
            testOtpCode = authState.generatedOtp
        )
    }

    Scaffold(
        containerColor = Slate50
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Brand Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Slate900, Slate800, BengkelBlueDeep)
                        )
                    )
                    .padding(top = 40.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
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
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "BENGKELKU",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Montir Dekat, Bantuan Cepat.",
                        color = BengkelAmber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Layanan bengkel online & panggilan montir realtime",
                        color = Slate400,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -------------------------------------------------------------
            // MODE UJI COBA CEPAT UNTUK SEMUA ORANG (PUBLIC TESTER HUB)
            // -------------------------------------------------------------
            TesterQuickStartHub(
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Slate300)
                Text(
                    text = "  ATAU MASUK DENGAN FORM  ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.5.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Slate300)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Role Selector Tabs (Customer, Montir, Admin)
            Surface(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Slate200,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    listOf(
                        Triple(UserRole.CUSTOMER, "Customer", Icons.Default.Person),
                        Triple(UserRole.MECHANIC, "Montir", Icons.Default.Build),
                        Triple(UserRole.ADMIN, "Admin", Icons.Default.Security)
                    ).forEach { (role, label, icon) ->
                        val isSelected = selectedRoleTab == role
                        val tabBg = when {
                            isSelected && role == UserRole.CUSTOMER -> BengkelBluePrimary
                            isSelected && role == UserRole.MECHANIC -> BengkelAmberDark
                            isSelected && role == UserRole.ADMIN -> BengkelRed
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tabBg)
                                .clickable {
                                    selectedRoleTab = role
                                    isRegisterMode = false
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Slate600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Slate700,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content according to selected tab
            when (selectedRoleTab) {
                UserRole.CUSTOMER -> {
                    if (isRegisterMode) {
                        CustomerRegisterCard(
                            viewModel = viewModel,
                            onSwitchToLogin = { isRegisterMode = false }
                        )
                    } else {
                        CustomerLoginCard(
                            viewModel = viewModel,
                            onSwitchToRegister = { isRegisterMode = true },
                            onForgotPassword = { showForgotPasswordDialog = true }
                        )
                    }
                }
                UserRole.MECHANIC -> {
                    if (isRegisterMode) {
                        MechanicRegisterCard(
                            viewModel = viewModel,
                            onSwitchToLogin = { isRegisterMode = false }
                        )
                    } else {
                        MechanicLoginCard(
                            viewModel = viewModel,
                            onSwitchToRegister = { isRegisterMode = true },
                            onForgotPassword = { showForgotPasswordDialog = true }
                        )
                    }
                }
                UserRole.ADMIN -> {
                    AdminLoginCard(viewModel = viewModel)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Demo Shortcut Section for quick testing all 3 roles
            Surface(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Akses Cepat Pengujian (1-Klik Masuk):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.switchDemoAccount(UserRole.CUSTOMER) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Customer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.switchDemoAccount(UserRole.MECHANIC) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Montir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.switchDemoAccount(UserRole.ADMIN) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            viewModel = viewModel,
            onDismiss = { showForgotPasswordDialog = false }
        )
    }
}

// -------------------------------------------------------------
// CUSTOMER LOGIN
// -------------------------------------------------------------
@Composable
fun CustomerLoginCard(
    viewModel: BengkelViewModel,
    onSwitchToRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("081234567890") }
    var password by remember { mutableStateOf("customer123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Masuk Sebagai Customer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                text = "Pesan montir darurat langsung ke lokasi Anda",
                fontSize = 12.sp,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BengkelRedLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = BengkelRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = { Text("Email atau Nomor HP") },
                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata Sandi") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onForgotPassword) {
                    Text("Lupa Kata Sandi?", fontSize = 12.sp, color = BengkelBluePrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (emailOrPhone.isBlank() || password.isBlank()) {
                        errorMessage = "Email/Nomor HP dan kata sandi wajib diisi."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    viewModel.loginCustomer(emailOrPhone.trim(), password) { success, msg ->
                        isLoading = false
                        if (!success) {
                            errorMessage = msg
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text("Masuk", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Belum punya akun? ", fontSize = 13.sp, color = Slate600)
                Text(
                    text = "Daftar Customer",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BengkelBluePrimary,
                    modifier = Modifier.clickable { onSwitchToRegister() }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// CUSTOMER REGISTER (With OTP Requirement)
// -------------------------------------------------------------
@Composable
fun CustomerRegisterCard(
    viewModel: BengkelViewModel,
    onSwitchToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var defaultVehicle by remember { mutableStateOf("Honda Vario 160") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Pendaftaran Akun Customer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                text = "Isi data wajib lengkap sebelum menggunakan layanan",
                fontSize = 12.sp,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BengkelRedLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = BengkelRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nama Lengkap *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Aktif *") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor HP Aktif (Wajib OTP) *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata Sandi *") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat Tempat Tinggal *") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = defaultVehicle,
                onValueChange = { defaultVehicle = it },
                label = { Text("Kendaraan Utama (Merk & Tipe)") },
                leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // GPS location auto-detection badge
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
                        text = "Lokasi GPS Tempat Tinggal: Lat -6.8887, Long 109.6753 (Terdeteksi)",
                        fontSize = 11.sp,
                        color = Slate700
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (fullName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || address.isBlank()) {
                        errorMessage = "Seluruh kolom bertanda bintang (*) wajib diisi lengkap."
                        return@Button
                    }
                    if (!email.contains("@") || !email.contains(".")) {
                        errorMessage = "Format email tidak valid."
                        return@Button
                    }
                    if (phone.length < 9) {
                        errorMessage = "Nomor HP minimal 9 digit."
                        return@Button
                    }
                    errorMessage = null
                    viewModel.initiateCustomerRegistration(
                        fullName = fullName.trim(),
                        email = email.trim(),
                        phone = phone.trim(),
                        password = password,
                        address = address.trim(),
                        latitude = -6.8887,
                        longitude = 109.6753,
                        defaultVehicle = defaultVehicle.trim()
                    ) {
                        // OTP dialog opens via state
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary)
            ) {
                Text("Lanjut Verifikasi OTP HP", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sudah punya akun? ", fontSize = 13.sp, color = Slate600)
                Text(
                    text = "Masuk di sini",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BengkelBluePrimary,
                    modifier = Modifier.clickable { onSwitchToLogin() }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// OTP VERIFICATION DIALOG
// -------------------------------------------------------------
@Composable
fun OtpVerificationDialog(
    viewModel: BengkelViewModel,
    phone: String,
    secondsRemaining: Int,
    testOtpCode: String
) {
    var otpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BengkelBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verifikasi OTP Nomor HP", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Kode OTP 6-digit telah dikirimkan ke nomor $phone.",
                    fontSize = 13.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Test OTP Hint
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BengkelAmberLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Simulasi SMS OTP: $testOtpCode (atau gunakan 123456)",
                        color = BengkelAmberDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = BengkelRed,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { if (it.length <= 6) otpInput = it },
                    label = { Text("Masukkan 6 Digit OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (secondsRemaining > 0) {
                        Text(
                            text = "Kirim ulang dlm $secondsRemaining dtk",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    } else {
                        TextButton(onClick = { viewModel.resendOtp() }) {
                            Text("Kirim Ulang OTP", fontSize = 12.sp, color = BengkelBluePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (otpInput.isBlank()) {
                        errorMessage = "Silakan masukkan kode OTP."
                        return@Button
                    }
                    isVerifying = true
                    viewModel.verifyCustomerOtp(otpInput.trim()) { success, msg ->
                        isVerifying = false
                        if (!success) {
                            errorMessage = msg
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BengkelBluePrimary)
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("Verifikasi & Selesai")
                }
            }
        }
    )
}

// -------------------------------------------------------------
// MONTIR LOGIN
// -------------------------------------------------------------
@Composable
fun MechanicLoginCard(
    viewModel: BengkelViewModel,
    onSwitchToRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("081298765431") }
    var password by remember { mutableStateOf("montir123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Masuk Sebagai Montir Mitra",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                text = "Terima order servis dan bantu kendaraan customer terdekat",
                fontSize = 12.sp,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BengkelRedLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = BengkelRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = { Text("Email atau Nomor HP Montir") },
                leadingIcon = { Icon(Icons.Default.Engineering, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata Sandi") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onForgotPassword) {
                    Text("Lupa Kata Sandi?", fontSize = 12.sp, color = BengkelAmberDark)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (emailOrPhone.isBlank() || password.isBlank()) {
                        errorMessage = "Email/Nomor HP dan kata sandi wajib diisi."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    viewModel.loginMechanic(emailOrPhone.trim(), password) { success, msg ->
                        isLoading = false
                        if (!success) {
                            errorMessage = msg
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BengkelAmberDark)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text("Masuk Montir", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ingin menjadi mitra? ", fontSize = 13.sp, color = Slate600)
                Text(
                    text = "Daftar Calon Montir",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BengkelAmberDark,
                    modifier = Modifier.clickable { onSwitchToRegister() }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// MONTIR REGISTER (Calon Montir -> Menunggu Persetujuan Admin)
// -------------------------------------------------------------
@Composable
fun MechanicRegisterCard(
    viewModel: BengkelViewModel,
    onSwitchToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("Servis Mesin, Ganti Ban, Servis Rem, Aki") }
    var experienceYears by remember { mutableStateOf("5") }
    var workLocation by remember { mutableStateOf("Pekalongan Kota & Sekitarnya") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Pendaftaran Calon Montir",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                text = "Pendaftaran wajib diverifikasi & disetujui oleh ADMIN sebelum aktif menerima pekerjaan",
                fontSize = 12.sp,
                color = BengkelAmberDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BengkelRedLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = BengkelRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (successNotice != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BengkelGreenLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = successNotice ?: "",
                            color = BengkelGreenDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Status: Menunggu Persetujuan Admin. Admin dapat menyetujui akun melalui menu Manajemen Montir.",
                            fontSize = 11.sp,
                            color = Slate700
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nama Lengkap Montir *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor HP Aktif *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Montir *") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat Tempat Tinggal *") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                label = { Text("Keahlian / Spesialisasi *") },
                leadingIcon = { Icon(Icons.Default.Handyman, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = experienceYears,
                onValueChange = { experienceYears = it },
                label = { Text("Pengalaman Kerja (Tahun) *") },
                leadingIcon = { Icon(Icons.Default.WorkHistory, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = workLocation,
                onValueChange = { workLocation = it },
                label = { Text("Area Jangkauan Lokasi Kerja *") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata Sandi Akun Montir *") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (fullName.isBlank() || phone.isBlank() || email.isBlank() || address.isBlank() || password.isBlank()) {
                        errorMessage = "Seluruh kolom bertanda bintang (*) wajib diisi."
                        return@Button
                    }
                    errorMessage = null
                    val exp = experienceYears.toIntOrNull() ?: 1
                    viewModel.registerMechanic(
                        fullName = fullName.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        address = address.trim(),
                        skills = skills.trim(),
                        experienceYears = exp,
                        workLocation = workLocation.trim(),
                        password = password
                    ) { success, msg ->
                        if (success) {
                            successNotice = msg
                        } else {
                            errorMessage = msg
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BengkelAmberDark)
            ) {
                Text("Daftar Calon Montir", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sudah terdaftar? ", fontSize = 13.sp, color = Slate600)
                Text(
                    text = "Masuk di sini",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BengkelAmberDark,
                    modifier = Modifier.clickable { onSwitchToLogin() }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// ADMIN DEDICATED LOGIN (Strict Credentials)
// -------------------------------------------------------------
@Composable
fun AdminLoginCard(viewModel: BengkelViewModel) {
    var adminEmail by remember { mutableStateOf("Calonbikers85@gmail.com") }
    var adminPassword by remember { mutableStateOf("Pekalongan27") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BengkelRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Portal Khusus Administrator",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Akses hak penuh manajemen sistem Bengkelku",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Slate100,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Catatan Keamanan: Portal ini diproteksi ketat. Semua akses selain administrator terdaftar akan ditolak dan dicatat pada audit log.",
                    fontSize = 11.sp,
                    color = Slate700,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BengkelRedLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = BengkelRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = BengkelRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = adminEmail,
                onValueChange = { adminEmail = it },
                label = { Text("Email Administrator") },
                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = adminPassword,
                onValueChange = { adminPassword = it },
                label = { Text("Kata Sandi Administrator") },
                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    // STRICT REQUIREMENT: Only Calonbikers85@gmail.com and Pekalongan27
                    viewModel.loginAdmin(adminEmail, adminPassword) { success, msg ->
                        isLoading = false
                        if (!success) {
                            errorMessage = msg
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BengkelRed)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Masuk Panel Admin", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// FORGOT PASSWORD DIALOG
// -------------------------------------------------------------
@Composable
fun ForgotPasswordDialog(
    viewModel: BengkelViewModel,
    onDismiss: () -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Lupa / Ubah Kata Sandi", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Masukkan email atau nomor HP terdaftar dan kata sandi baru Anda:",
                    fontSize = 12.sp,
                    color = Slate600
                )
                if (message != null) {
                    Text(
                        text = message ?: "",
                        color = if (isSuccess) BengkelGreenDark else BengkelRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = { Text("Email atau Nomor HP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Kata Sandi Baru") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (emailOrPhone.isBlank() || newPassword.isBlank()) {
                        message = "Semua kolom wajib diisi."
                        isSuccess = false
                        return@Button
                    }
                    viewModel.resetPassword(emailOrPhone.trim(), newPassword) { ok, msg ->
                        message = msg
                        isSuccess = ok
                    }
                }
            ) {
                Text("Simpan Kata Sandi")
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
// TESTER QUICK-START HUB (FOR EVERYONE TO TEST SEAMLESSLY)
// -------------------------------------------------------------
@Composable
fun TesterQuickStartHub(
    viewModel: BengkelViewModel,
    modifier: Modifier = Modifier
) {
    var isResetting by remember { mutableStateOf(false) }
    var resetFeedback by remember { mutableStateOf<String?>(null) }
    var showTestingGuide by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tester_quick_start_hub"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, BengkelBluePrimary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(BengkelBluePrimary, BengkelBlueDeep)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mode Uji Coba Cepat (Semua Orang)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Siap dicoba instan tanpa perlu daftar OTP",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BengkelGreenLight
                ) {
                    Text(
                        text = "1-Klik",
                        color = BengkelGreenDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Feedback Banner
            if (resetFeedback != null) {
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
                            text = resetFeedback ?: "",
                            color = BengkelGreenDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 1. Customer Option Card
            QuickStartRoleCard(
                title = "1. Uji Coba: Customer (Pemilik Kendaraan)",
                roleName = "Bambang Pamungkas • 081234567890",
                description = "Pesan montir darurat, GPS live tracking, tarif 11 layanan & bayar invoice.",
                buttonText = "Masuk sebagai Customer",
                accentColor = BengkelBluePrimary,
                icon = Icons.Default.Person,
                onSelect = { viewModel.switchDemoAccount(UserRole.CUSTOMER) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Mechanic Option Card
            QuickStartRoleCard(
                title = "2. Uji Coba: Montir Mitra (Siaga Online)",
                roleName = "Budi Santoso • 081298765431",
                description = "Terima order masuk, update status 'Menuju Lokasi', navigasi rute & selesai servis.",
                buttonText = "Masuk sebagai Montir",
                accentColor = BengkelAmberDark,
                icon = Icons.Default.Build,
                onSelect = { viewModel.switchDemoAccount(UserRole.MECHANIC) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Admin Option Card
            QuickStartRoleCard(
                title = "3. Uji Coba: Administrator (Owner Bengkel)",
                roleName = "Admin Utama • Calonbikers85@gmail.com",
                description = "Ubah harga resmi 11 layanan, verifikasi montir baru & pantau omzet keuangan.",
                buttonText = "Masuk sebagai Admin",
                accentColor = BengkelRed,
                icon = Icons.Default.Security,
                onSelect = { viewModel.switchDemoAccount(UserRole.ADMIN) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Reset demo data & scenarios action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isResetting = true
                        viewModel.resetDemoData {
                            isResetting = false
                            resetFeedback = "Data pesanan berhasil di-reset! Siap untuk simulasi baru."
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isResetting,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    if (isResetting) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = Slate700,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset Data",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                    }
                }

                Button(
                    onClick = { showTestingGuide = !showTestingGuide },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showTestingGuide) "Tutup Panduan" else "Panduan Uji",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Collapsible Testing Walkthrough Guide
            AnimatedVisibility(visible = showTestingGuide) {
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
                            text = "🚀 Rekomendasi Skenario Uji Coba End-to-End:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1. Klik 'Masuk sebagai Customer' -> Pilih layanan (cth. Ganti Ban Bocor) -> Buat Pesanan.",
                            fontSize = 11.sp,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. Klik tombol 'Ganti Peran' di bagian atas layar -> Pilih Montir -> Tab 'Pesanan Masuk' -> Klik 'Terima Pesanan' & 'Menuju Lokasi'.",
                            fontSize = 11.sp,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "3. Kembali ke Customer via 'Ganti Peran' -> Lacak montir bergerak di peta -> Bayar pesanan setelah montir menyelesaikan servis.",
                            fontSize = 11.sp,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "4. Beralih ke Admin -> Buka tab Layanan untuk ubah tarif resmi atau tab Montir untuk setujui montir baru!",
                            fontSize = 11.sp,
                            color = Slate700
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStartRoleCard(
    title: String,
    roleName: String,
    description: String,
    buttonText: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Slate50,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(text = roleName, fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = Slate600,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
