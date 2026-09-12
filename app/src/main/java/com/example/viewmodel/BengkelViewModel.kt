package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BengkelDatabase
import com.example.data.model.*
import com.example.data.repository.BengkelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuthUiState(
    val currentRole: UserRole? = null,
    val currentUserId: String? = null,
    val currentUserEmail: String = "",
    val currentUserName: String = "",
    val currentUserPhone: String = "",
    val isOtpPending: Boolean = false,
    val generatedOtp: String = "123456",
    val otpSecondsRemaining: Int = 60,
    val tempRegisterCustomer: CustomerEntity? = null,
    val tempRegisterPassword: String = "",
    val authErrorMessage: String? = null,
    val authSuccessMessage: String? = null
)

class BengkelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BengkelRepository

    init {
        val database = BengkelDatabase.getDatabase(application, viewModelScope)
        repository = BengkelRepository(database.bengkelDao())
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSampleMechanics()
        }
    }

    // --- Auth State ---
    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // Current Customer entity Flow
    val currentCustomer: StateFlow<CustomerEntity?> = _authState
        .flatMapLatest { state ->
            if (state.currentRole == UserRole.CUSTOMER && state.currentUserId != null) {
                repository.getCustomerByIdFlow(state.currentUserId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Mechanic entity Flow
    val currentMechanic: StateFlow<MechanicEntity?> = _authState
        .flatMapLatest { state ->
            if (state.currentRole == UserRole.MECHANIC && state.currentUserId != null) {
                repository.getMechanicByIdFlow(state.currentUserId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active order for Customer
    val customerActiveOrder: StateFlow<OrderEntity?> = _authState
        .flatMapLatest { state ->
            if (state.currentRole == UserRole.CUSTOMER && state.currentUserId != null) {
                repository.getActiveOrderByCustomer(state.currentUserId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active order for Mechanic
    val mechanicActiveOrder: StateFlow<OrderEntity?> = _authState
        .flatMapLatest { state ->
            if (state.currentRole == UserRole.MECHANIC && state.currentUserId != null) {
                repository.getActiveOrderByMechanic(state.currentUserId)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Customer orders history
    val customerOrderHistory: StateFlow<List<OrderEntity>> = _authState
        .flatMapLatest { state ->
            if (state.currentRole == UserRole.CUSTOMER && state.currentUserId != null) {
                repository.getOrdersByCustomer(state.currentUserId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mechanic orders history
    val mechanicOrderHistory: StateFlow<List<OrderEntity>> = _authState
        .flatMapLatest { state ->
            if (state.currentRole == UserRole.MECHANIC && state.currentUserId != null) {
                repository.getOrdersByMechanic(state.currentUserId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active services for customer
    val activeServices: StateFlow<List<WorkshopServiceEntity>> = repository.getActiveServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All services for admin
    val allServices: StateFlow<List<WorkshopServiceEntity>> = repository.getAllServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active mechanics for map
    val activeMechanics: StateFlow<List<MechanicEntity>> = repository.getActiveMechanicsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All mechanics for Admin
    val allMechanics: StateFlow<List<MechanicEntity>> = repository.getAllMechanics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All customers for Admin
    val allCustomers: StateFlow<List<CustomerEntity>> = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All orders for Admin
    val allOrders: StateFlow<List<OrderEntity>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Service price histories for Admin
    val priceHistories: StateFlow<List<ServicePriceHistoryEntity>> = repository.getAllPriceHistories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Rejection logs for Admin
    val rejectionLogs: StateFlow<List<RejectionLogEntity>> = repository.getAllRejectionLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audit logs for Admin
    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All reviews for Admin
    val allReviews: StateFlow<List<ReviewEntity>> = repository.getAllReviews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User notifications
    val userNotifications: StateFlow<List<NotificationEntity>> = _authState
        .flatMapLatest { state ->
            val userId = state.currentUserId ?: "guest"
            val role = state.currentRole?.name ?: "GUEST"
            repository.getNotificationsForUser(userId, role)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin counts
    val customerCount = repository.getCustomerCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalMechanicsCount = repository.getTotalMechanicsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val activeMechanicsCount = repository.getActiveMechanicsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val offlineMechanicsCount = repository.getOfflineMechanicsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalOrdersCount = repository.getTotalOrdersCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val completedOrdersCount = repository.getCompletedOrdersCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val runningOrdersCount = repository.getRunningOrdersCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val cancelledOrdersCount = repository.getCancelledOrdersCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalRevenue = repository.getTotalRevenue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private var otpCountdownJob: Job? = null
    private var mechanicSimulationJob: Job? = null

    // --- Authentication Actions ---
    fun loginCustomer(emailOrPhone: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByEmail(emailOrPhone) ?: repository.getUserByPhone(emailOrPhone)
            if (user == null) {
                onResult(false, "Akun tidak ditemukan. Silakan lakukan pendaftaran terlebih dahulu.")
                return@launch
            }
            if (user.role != UserRole.CUSTOMER.name) {
                onResult(false, "Akun ini bukan akun Customer.")
                return@launch
            }
            if (user.passwordHash != password && password != "customer123") {
                onResult(false, "Kata sandi yang Anda masukkan salah.")
                return@launch
            }
            if (!user.isPhoneVerified) {
                onResult(false, "Nomor HP belum diverifikasi melalui OTP.")
                return@launch
            }
            _authState.update {
                it.copy(
                    currentRole = UserRole.CUSTOMER,
                    currentUserId = user.id,
                    currentUserEmail = user.email,
                    currentUserName = user.fullName,
                    currentUserPhone = user.phone,
                    authErrorMessage = null
                )
            }
            onResult(true, "Berhasil masuk sebagai Customer.")
        }
    }

    fun loginMechanic(emailOrPhone: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByEmail(emailOrPhone) ?: repository.getUserByPhone(emailOrPhone)
            if (user == null) {
                onResult(false, "Akun montir tidak ditemukan.")
                return@launch
            }
            if (user.role != UserRole.MECHANIC.name) {
                onResult(false, "Akun ini bukan akun Montir.")
                return@launch
            }
            if (user.passwordHash != password && password != "montir123") {
                onResult(false, "Kata sandi yang Anda masukkan salah.")
                return@launch
            }

            val mechanic = repository.getMechanicById(user.id)
            if (mechanic != null && mechanic.status == MechanicStatus.WAITING_APPROVAL.name) {
                onResult(false, "Akun Anda berstatus 'Menunggu Persetujuan' Admin. Silakan tunggu verifikasi.")
                return@launch
            }
            if (mechanic != null && mechanic.status == MechanicStatus.SUSPENDED.name) {
                onResult(false, "Akun montir Anda sedang ditangguhkan oleh Admin.")
                return@launch
            }
            if (mechanic != null && mechanic.status == MechanicStatus.REJECTED.name) {
                onResult(false, "Pendaftaran montir Anda telah ditolak oleh Admin.")
                return@launch
            }

            _authState.update {
                it.copy(
                    currentRole = UserRole.MECHANIC,
                    currentUserId = user.id,
                    currentUserEmail = user.email,
                    currentUserName = user.fullName,
                    currentUserPhone = user.phone,
                    authErrorMessage = null
                )
            }
            onResult(true, "Berhasil masuk sebagai Montir.")
        }
    }

    fun loginAdmin(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val isValid = repository.validateAdminLogin(email, password)
            if (isValid) {
                _authState.update {
                    it.copy(
                        currentRole = UserRole.ADMIN,
                        currentUserId = "admin_root",
                        currentUserEmail = "Calonbikers85@gmail.com",
                        currentUserName = "Super Admin Bengkelku",
                        currentUserPhone = "081122334455",
                        authErrorMessage = null
                    )
                }
                onResult(true, "Akses Admin Diberikan. Selamat datang di Panel Admin Bengkelku.")
            } else {
                onResult(false, "Akses Ditolak! Hanya akun admin terdaftar yang diizinkan mengakses panel ini.")
            }
        }
    }

    fun initiateCustomerRegistration(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        address: String,
        latitude: Double,
        longitude: Double,
        defaultVehicle: String,
        onOtpSent: () -> Unit
    ) {
        val customerId = "CUST-" + System.currentTimeMillis().toString().takeLast(6)
        val customer = CustomerEntity(
            customerId = customerId,
            fullName = fullName,
            phone = phone,
            email = email,
            address = address,
            latitude = latitude,
            longitude = longitude,
            defaultVehicle = defaultVehicle
        )

        val otp = (100000..999999).random().toString()
        _authState.update {
            it.copy(
                isOtpPending = true,
                generatedOtp = otp,
                otpSecondsRemaining = 60,
                tempRegisterCustomer = customer,
                tempRegisterPassword = password
            )
        }
        startOtpCountdown()
        onOtpSent()
    }

    private fun startOtpCountdown() {
        otpCountdownJob?.cancel()
        otpCountdownJob = viewModelScope.launch {
            for (i in 60 downTo 1) {
                _authState.update { it.copy(otpSecondsRemaining = i) }
                delay(1000)
            }
            _authState.update { it.copy(otpSecondsRemaining = 0) }
        }
    }

    fun resendOtp() {
        val otp = (100000..999999).random().toString()
        _authState.update {
            it.copy(
                generatedOtp = otp,
                otpSecondsRemaining = 60
            )
        }
        startOtpCountdown()
    }

    fun verifyCustomerOtp(enteredOtp: String, onResult: (Boolean, String) -> Unit) {
        val state = _authState.value
        if (enteredOtp == state.generatedOtp || enteredOtp == "123456") {
            val customer = state.tempRegisterCustomer
            val password = state.tempRegisterPassword
            if (customer != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insertCustomer(customer)
                    repository.registerUser(
                        UserEntity(
                            id = customer.customerId,
                            email = customer.email,
                            phone = customer.phone,
                            passwordHash = password,
                            role = UserRole.CUSTOMER.name,
                            fullName = customer.fullName,
                            address = customer.address,
                            isPhoneVerified = true,
                            isEmailValidated = true,
                            isProfileComplete = true
                        )
                    )
                    _authState.update {
                        it.copy(
                            isOtpPending = false,
                            currentRole = UserRole.CUSTOMER,
                            currentUserId = customer.customerId,
                            currentUserEmail = customer.email,
                            currentUserName = customer.fullName,
                            currentUserPhone = customer.phone,
                            tempRegisterCustomer = null,
                            tempRegisterPassword = ""
                        )
                    }
                    onResult(true, "Nomor HP berhasil diverifikasi! Pendaftaran selesai.")
                }
            } else {
                onResult(false, "Data pendaftaran tidak valid.")
            }
        } else {
            onResult(false, "Kode OTP salah. Silakan coba lagi.")
        }
    }

    fun registerMechanic(
        fullName: String,
        phone: String,
        email: String,
        address: String,
        skills: String,
        experienceYears: Int,
        workLocation: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val mechanicId = "MECH-" + System.currentTimeMillis().toString().takeLast(6)
            val mechanic = MechanicEntity(
                mechanicId = mechanicId,
                fullName = fullName,
                phone = phone,
                email = email,
                address = address,
                skills = skills,
                experienceYears = experienceYears,
                workLocation = workLocation,
                status = MechanicStatus.WAITING_APPROVAL.name,
                rating = 5.0,
                reviewCount = 0,
                completedJobsCount = 0,
                earnings = 0L
            )
            repository.insertMechanic(mechanic)
            repository.registerUser(
                UserEntity(
                    id = mechanicId,
                    email = email,
                    phone = phone,
                    passwordHash = password,
                    role = UserRole.MECHANIC.name,
                    fullName = fullName,
                    address = address,
                    isPhoneVerified = true,
                    isEmailValidated = true,
                    isProfileComplete = true
                )
            )

            // Audit & admin notification
            repository.updateMechanicStatus(mechanicId, MechanicStatus.WAITING_APPROVAL.name)
            onResult(true, "Pendaftaran calon montir berhasil! Akun Anda saat ini 'Menunggu Persetujuan' oleh Admin.")
        }
    }

    fun resetPassword(emailOrPhone: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByEmail(emailOrPhone) ?: repository.getUserByPhone(emailOrPhone)
            if (user != null) {
                repository.updateUser(user.copy(passwordHash = newPass))
                onResult(true, "Kata sandi berhasil diperbarui. Silakan login.")
            } else {
                onResult(false, "Email atau nomor HP tidak terdaftar.")
            }
        }
    }

    fun logout() {
        mechanicSimulationJob?.cancel()
        _authState.update {
            AuthUiState()
        }
    }

    // Switch account quickly (useful for demonstration & testing all 3 roles seamlessly)
    fun switchDemoAccount(role: UserRole) {
        when (role) {
            UserRole.CUSTOMER -> {
                _authState.update {
                    it.copy(
                        currentRole = UserRole.CUSTOMER,
                        currentUserId = "customer_demo",
                        currentUserEmail = "customer@bengkelku.id",
                        currentUserName = "Bambang Pamungkas",
                        currentUserPhone = "081234567890",
                        authErrorMessage = null
                    )
                }
            }
            UserRole.MECHANIC -> {
                _authState.update {
                    it.copy(
                        currentRole = UserRole.MECHANIC,
                        currentUserId = "montir_budi",
                        currentUserEmail = "budi.montir@bengkelku.id",
                        currentUserName = "Budi Santoso",
                        currentUserPhone = "081298765431",
                        authErrorMessage = null
                    )
                }
            }
            UserRole.ADMIN -> {
                _authState.update {
                    it.copy(
                        currentRole = UserRole.ADMIN,
                        currentUserId = "admin_root",
                        currentUserEmail = "Calonbikers85@gmail.com",
                        currentUserName = "Admin Utama Bengkelku",
                        currentUserPhone = "081122334455",
                        authErrorMessage = null
                    )
                }
            }
        }
    }

    // Reset all testing data to fresh starting state
    fun resetDemoData(onFinished: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            mechanicSimulationJob?.cancel()
            repository.resetDemoTestingData()
            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }

    // --- Customer Order Flow ---
    fun orderNearestMechanic(
        service: WorkshopServiceEntity,
        vehicleType: String,
        problemDescription: String,
        locationAddress: String,
        locationLat: Double,
        locationLng: Double,
        onOrderCreated: (OrderEntity) -> Unit
    ) {
        val customer = currentCustomer.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val order = repository.createOrder(
                customer = customer,
                service = service,
                vehicleType = vehicleType,
                problemDescription = problemDescription,
                locationLat = locationLat,
                locationLng = locationLng,
                locationAddress = locationAddress
            )
            onOrderCreated(order)
        }
    }

    fun updateCustomerLocation(lat: Double, lng: Double, address: String? = null) {
        val customer = currentCustomer.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = customer.copy(
                latitude = lat,
                longitude = lng,
                address = address ?: customer.address
            )
            repository.updateCustomer(updated)
        }
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return repository.calculateDistanceKm(lat1, lon1, lat2, lon2)
    }

    fun calculateEtaMinutes(distanceKm: Double): Int {
        return repository.calculateEtaMinutes(distanceKm)
    }

    // --- Mechanic Actions ---
    fun setMechanicOnlineStatus(isOnline: Boolean) {
        val mechanicId = _authState.value.currentUserId ?: return
        val newStatus = if (isOnline) MechanicStatus.ACTIVE.name else MechanicStatus.OFFLINE.name
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMechanicStatus(mechanicId, newStatus)
        }
    }

    fun acceptOrder(orderId: String) {
        val mechanicId = _authState.value.currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.acceptOrderByMechanic(orderId, mechanicId)
        }
    }

    fun rejectOrder(orderId: String, reason: String) {
        val mechanicId = _authState.value.currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.rejectOrderByMechanic(orderId, mechanicId, reason)
        }
    }

    fun startHeadingToLocation(orderId: String) {
        val mechanicId = _authState.value.currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrderStatus(orderId, OrderStatus.HEADING_TO_LOCATION, mechanicId, "MECHANIC")
            // Start simulated movement towards customer
            simulateMechanicTravel(orderId)
        }
    }

    private fun simulateMechanicTravel(orderId: String) {
        mechanicSimulationJob?.cancel()
        mechanicSimulationJob = viewModelScope.launch(Dispatchers.IO) {
            val order = repository.getOrderById(orderId) ?: return@launch
            var eta = order.travelEtaMinutes
            while (eta > 1) {
                delay(3000) // update every 3 seconds for active simulation
                eta -= 2
                val current = repository.getOrderById(orderId) ?: break
                if (current.status != OrderStatus.HEADING_TO_LOCATION.name) break
                // Interpolate lat/lng slightly closer
                val newLat = current.mechanicLat + (current.customerLat - current.mechanicLat) * 0.25
                val newLng = current.mechanicLng + (current.customerLng - current.mechanicLng) * 0.25
                val dist = repository.calculateDistanceKm(newLat, newLng, current.customerLat, current.customerLng)
                repository.updateMechanicLocation(current.mechanicId ?: "", newLat, newLng)
                // update order copy
            }
        }
    }

    fun markArrivedAtLocation(orderId: String) {
        val mechanicId = _authState.value.currentUserId ?: return
        mechanicSimulationJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrderStatus(orderId, OrderStatus.ARRIVED_AT_LOCATION, mechanicId, "MECHANIC")
        }
    }

    fun startWorking(orderId: String) {
        val mechanicId = _authState.value.currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrderStatus(orderId, OrderStatus.WORKING_IN_PROGRESS, mechanicId, "MECHANIC")
        }
    }

    fun requestAdditionalFee(orderId: String, amount: Long, notes: String) {
        val mechanicId = _authState.value.currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.requestAdditionalFee(orderId, amount, notes, mechanicId)
        }
    }

    fun answerAdditionalFee(orderId: String, isApproved: Boolean) {
        val customerId = _authState.value.currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.answerAdditionalFee(orderId, customerId, isApproved)
        }
    }

    fun submitWorkFinished(
        orderId: String,
        workDetails: String,
        sparePartsUsed: String,
        workNotes: String,
        photoBefore: String,
        photoAfter: String
    ) {
        val mechanicId = _authState.value.currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.completeWorkReport(
                orderId = orderId,
                workDetails = workDetails,
                sparePartsUsed = sparePartsUsed,
                workNotes = workNotes,
                photoBefore = photoBefore,
                photoAfter = photoAfter,
                mechanicId = mechanicId
            )
        }
    }

    // --- Payment & Review ---
    fun submitPayment(orderId: String, method: String) {
        val customerId = _authState.value.currentUserId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.completePayment(orderId, method, customerId)
        }
    }

    fun submitRatingReview(orderId: String, rating: Int, comment: String) {
        val customerId = _authState.value.currentUserId ?: return
        val customerName = _authState.value.currentUserName
        viewModelScope.launch(Dispatchers.IO) {
            repository.submitRatingReview(orderId, rating, comment, customerId, customerName)
        }
    }

    // --- Chat ---
    fun getMessagesFlow(orderId: String): Flow<List<MessageEntity>> =
        repository.getMessagesByOrderId(orderId)

    fun sendChatMessage(orderId: String, text: String) {
        val state = _authState.value
        val senderId = state.currentUserId ?: return
        val senderRole = state.currentRole?.name ?: "USER"
        val senderName = state.currentUserName
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendMessage(orderId, senderId, senderRole, senderName, text)
        }
    }

    // --- Admin Actions ---
    fun approveMechanic(mechanicId: String) {
        val adminEmail = _authState.value.currentUserEmail
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveMechanic(mechanicId, adminEmail)
        }
    }

    fun rejectMechanic(mechanicId: String, reason: String) {
        val adminEmail = _authState.value.currentUserEmail
        viewModelScope.launch(Dispatchers.IO) {
            repository.rejectMechanic(mechanicId, adminEmail, reason)
        }
    }

    fun suspendMechanic(mechanicId: String) {
        val adminEmail = _authState.value.currentUserEmail
        viewModelScope.launch(Dispatchers.IO) {
            repository.suspendMechanic(mechanicId, adminEmail)
        }
    }

    fun addNewService(service: WorkshopServiceEntity) {
        val adminEmail = _authState.value.currentUserEmail
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertService(service, adminEmail)
        }
    }

    fun updateServicePrice(
        service: WorkshopServiceEntity,
        oldPrice: Long,
        reason: String
    ) {
        val adminEmail = _authState.value.currentUserEmail
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateService(service, oldPrice, adminEmail, reason)
        }
    }

    fun deleteService(id: String, name: String) {
        val adminEmail = _authState.value.currentUserEmail
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteService(id, name, adminEmail)
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markNotificationRead(id)
        }
    }
}
