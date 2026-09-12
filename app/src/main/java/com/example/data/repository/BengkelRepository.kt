package com.example.data.repository

import com.example.data.local.BengkelDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class BengkelRepository(private val dao: BengkelDao) {

    // --- Haversine Distance Calculation ---
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = r * c
        return Math.round(distance * 10.0) / 10.0
    }

    fun calculateEtaMinutes(distanceKm: Double): Int {
        // Average speed 25 km/h + 5 mins preparation
        val minutes = (distanceKm / 25.0 * 60.0).toInt() + 5
        return minutes.coerceIn(5, 120)
    }

    // --- Users & Authentication ---
    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)
    suspend fun getUserByPhone(phone: String): UserEntity? = dao.getUserByPhone(phone)
    suspend fun getUserById(id: String): UserEntity? = dao.getUserById(id)
    suspend fun registerUser(user: UserEntity) = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    // --- Customer ---
    fun getCustomerByIdFlow(id: String): Flow<CustomerEntity?> = dao.getCustomerByIdFlow(id)
    suspend fun getCustomerById(id: String): CustomerEntity? = dao.getCustomerById(id)
    fun getAllCustomers(): Flow<List<CustomerEntity>> = dao.getAllCustomers()
    suspend fun insertCustomer(customer: CustomerEntity) = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = dao.updateCustomer(customer)
    fun getCustomerCount(): Flow<Int> = dao.getCustomerCount()

    // --- Mechanic ---
    fun getMechanicByIdFlow(id: String): Flow<MechanicEntity?> = dao.getMechanicByIdFlow(id)
    suspend fun getMechanicById(id: String): MechanicEntity? = dao.getMechanicById(id)
    fun getAllMechanics(): Flow<List<MechanicEntity>> = dao.getAllMechanics()
    fun getMechanicsByStatus(status: String): Flow<List<MechanicEntity>> = dao.getMechanicsByStatus(status)
    fun getActiveMechanicsFlow(): Flow<List<MechanicEntity>> = dao.getActiveMechanicsFlow()
    suspend fun insertMechanic(mechanic: MechanicEntity) = dao.insertMechanic(mechanic)
    suspend fun updateMechanic(mechanic: MechanicEntity) = dao.updateMechanic(mechanic)
    suspend fun updateMechanicStatus(id: String, status: String) {
        dao.updateMechanicStatus(id, status)
        dao.insertAuditLog(
            AuditLogEntity(
                actorId = id,
                actorRole = "MECHANIC",
                action = "UPDATE_STATUS",
                targetEntity = "MECHANIC",
                targetId = id,
                details = "Montir mengubah status menjadi $status"
            )
        )
    }
    suspend fun updateMechanicLocation(id: String, lat: Double, lng: Double) =
        dao.updateMechanicLocation(id, lat, lng)

    fun getTotalMechanicsCount(): Flow<Int> = dao.getTotalMechanicsCount()
    fun getActiveMechanicsCount(): Flow<Int> = dao.getActiveMechanicsCount()
    fun getOfflineMechanicsCount(): Flow<Int> = dao.getOfflineMechanicsCount()

    suspend fun ensureSampleMechanics() {
        ensureAllDemoData()
    }

    suspend fun ensureAllDemoData() {
        // 1. Ensure Admin User
        val admin = dao.getAdminByEmail("Calonbikers85@gmail.com")
        if (admin == null) {
            dao.insertAdminUser(
                AdminUserEntity(
                    adminId = "admin_root",
                    email = "Calonbikers85@gmail.com",
                    name = "Admin Utama Bengkelku",
                    role = "SUPER_ADMIN"
                )
            )
            dao.insertUser(
                UserEntity(
                    id = "admin_root",
                    email = "Calonbikers85@gmail.com",
                    phone = "081122334455",
                    passwordHash = "Pekalongan27",
                    role = "ADMIN",
                    fullName = "Admin Utama Bengkelku",
                    address = "Pekalongan",
                    isPhoneVerified = true,
                    isEmailValidated = true,
                    isProfileComplete = true
                )
            )
        }

        // 2. Ensure Customer Demo User
        val customer = dao.getCustomerById("customer_demo")
        if (customer == null) {
            dao.insertCustomer(
                CustomerEntity(
                    customerId = "customer_demo",
                    fullName = "Bambang Pamungkas",
                    phone = "081234567890",
                    email = "customer@bengkelku.id",
                    address = "Jl. Merdeka No. 15, Pekalongan",
                    latitude = -6.8887,
                    longitude = 109.6753,
                    defaultVehicle = "Honda Vario 160 (G 1234 AB)"
                )
            )
            dao.insertUser(
                UserEntity(
                    id = "customer_demo",
                    email = "customer@bengkelku.id",
                    phone = "081234567890",
                    passwordHash = "customer123",
                    role = "CUSTOMER",
                    fullName = "Bambang Pamungkas",
                    address = "Jl. Merdeka No. 15, Pekalongan",
                    isPhoneVerified = true,
                    isEmailValidated = true,
                    isProfileComplete = true
                )
            )
        }

        // 3. Ensure All Mechanics
        val demoMechanics = listOf(
            MechanicEntity(
                mechanicId = "montir_budi",
                fullName = "Budi Santoso",
                phone = "081298765431",
                email = "budi.montir@bengkelku.id",
                address = "Jl. Hayam Wuruk No. 12, Pekalongan",
                skills = "Servis Mesin, Tune Up, Ganti Oli, Mogok Jalan",
                experienceYears = 8,
                workLocation = "Pekalongan Barat & Sekitarnya",
                latitude = -6.8850,
                longitude = 109.6720,
                status = "ACTIVE",
                rating = 4.9,
                reviewCount = 48,
                completedJobsCount = 152,
                earnings = 7850000L
            ),
            MechanicEntity(
                mechanicId = "montir_agus",
                fullName = "Agus Prasetyo",
                phone = "081377889900",
                email = "agus.montir@bengkelku.id",
                address = "Jl. Dr. Cipto No. 45, Pekalongan",
                skills = "Tambal Ban, Ganti Ban, Servis Rem, Aki Kendaraan",
                experienceYears = 5,
                workLocation = "Pekalongan Timur & Pusat",
                latitude = -6.8920,
                longitude = 109.6790,
                status = "ACTIVE",
                rating = 4.8,
                reviewCount = 32,
                completedJobsCount = 94,
                earnings = 4920000L
            ),
            MechanicEntity(
                mechanicId = "montir_dimas",
                fullName = "Dimas Pratama",
                phone = "081912345678",
                email = "dimas.montir@bengkelku.id",
                address = "Jl. Veteran No. 8, Pekalongan",
                skills = "Servis Kelistrikan, ECU Scanner, Tune Up, Mogok Darurat",
                experienceYears = 6,
                workLocation = "Pekalongan Utara",
                latitude = -6.8830,
                longitude = 109.6760,
                status = "ACTIVE",
                rating = 5.0,
                reviewCount = 27,
                completedJobsCount = 68,
                earnings = 3850000L
            ),
            MechanicEntity(
                mechanicId = "montir_hendra",
                fullName = "Hendra Wijaya",
                phone = "085612349988",
                email = "hendra.calon@gmail.com",
                address = "Jl. Bahagia No. 19, Wiradesa",
                skills = "Overhaul Mesin Motor & Mobil, Kaki-kaki",
                experienceYears = 4,
                workLocation = "Wiradesa & Pekalongan Selatan",
                latitude = -6.8960,
                longitude = 109.6650,
                status = "WAITING_APPROVAL",
                rating = 5.0,
                reviewCount = 0,
                completedJobsCount = 0,
                earnings = 0L
            ),
            MechanicEntity(
                mechanicId = "montir_eko",
                fullName = "Eko Nugroho",
                phone = "081234567892",
                email = "eko.montir@bengkelku.id",
                address = "Jl. Progo No. 15, Pekalongan",
                skills = "Servis Mesin, Transmisi Otomatis, Injeksi",
                experienceYears = 7,
                workLocation = "Pekalongan Barat",
                latitude = -6.8858,
                longitude = 109.6825,
                status = "BUSY",
                rating = 4.9,
                reviewCount = 54,
                completedJobsCount = 110,
                earnings = 6200000L
            ),
            MechanicEntity(
                mechanicId = "montir_fajar",
                fullName = "Fajar Setiawan",
                phone = "081398761234",
                email = "fajar.montir@bengkelku.id",
                address = "Jl. WR Supratman No. 22, Pekalongan",
                skills = "Tambal Ban, Kelistrikan, Servis Rem",
                experienceYears = 3,
                workLocation = "Pekalongan Selatan",
                latitude = -6.8950,
                longitude = 109.6675,
                status = "OFFLINE",
                rating = 4.7,
                reviewCount = 19,
                completedJobsCount = 42,
                earnings = 2100000L
            )
        )

        demoMechanics.forEach { mech ->
            val existing = dao.getMechanicById(mech.mechanicId)
            if (existing == null) {
                dao.insertMechanic(mech)
            }
            val existingUser = dao.getUserById(mech.mechanicId)
            if (existingUser == null) {
                dao.insertUser(
                    UserEntity(
                        id = mech.mechanicId,
                        email = mech.email,
                        phone = mech.phone,
                        passwordHash = "montir123",
                        role = "MECHANIC",
                        fullName = mech.fullName,
                        address = mech.address,
                        isPhoneVerified = true,
                        isEmailValidated = true,
                        isProfileComplete = true
                    )
                )
            }
        }
    }

    suspend fun resetDemoTestingData() {
        dao.deleteAllOrders()
        dao.deleteAllMessages()
        dao.deleteAllChats()
        // Reset mechanics status
        dao.updateMechanicStatus("montir_budi", "ACTIVE")
        dao.updateMechanicStatus("montir_agus", "ACTIVE")
        dao.updateMechanicStatus("montir_dimas", "ACTIVE")
        dao.updateMechanicStatus("montir_eko", "BUSY")
        dao.updateMechanicStatus("montir_fajar", "OFFLINE")
        dao.updateMechanicStatus("montir_hendra", "WAITING_APPROVAL")

        // Reset locations
        dao.updateMechanicLocation("montir_budi", -6.8850, 109.6720)
        dao.updateMechanicLocation("montir_agus", -6.8920, 109.6790)

        dao.insertAuditLog(
            AuditLogEntity(
                actorId = "tester",
                actorRole = "TESTER",
                action = "DEMO_DATA_RESET",
                targetEntity = "SYSTEM",
                targetId = "all_orders",
                details = "Data uji coba berhasil di-reset ke kondisi awal siap uji coba baru."
            )
        )
    }

    // --- Services & Price Management ---
    fun getActiveServices(): Flow<List<WorkshopServiceEntity>> = dao.getActiveServices()
    fun getAllServices(): Flow<List<WorkshopServiceEntity>> = dao.getAllServices()
    suspend fun getServiceById(id: String): WorkshopServiceEntity? = dao.getServiceById(id)
    suspend fun insertService(service: WorkshopServiceEntity, adminEmail: String) {
        dao.insertService(service)
        dao.insertPriceHistory(
            ServicePriceHistoryEntity(
                serviceId = service.serviceId,
                serviceName = service.name,
                oldPrice = 0L,
                newPrice = service.basePrice,
                changedByAdmin = adminEmail,
                reason = "Layanan baru ditambahkan oleh Admin"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorId = adminEmail,
                actorRole = "ADMIN",
                action = "CREATE_SERVICE",
                targetEntity = "SERVICE",
                targetId = service.serviceId,
                details = "Admin menambahkan layanan '${service.name}' seharga Rp ${service.basePrice}"
            )
        )
    }

    suspend fun updateService(
        service: WorkshopServiceEntity,
        oldPrice: Long,
        adminEmail: String,
        reason: String
    ) {
        dao.updateService(service)
        if (service.basePrice != oldPrice) {
            dao.insertPriceHistory(
                ServicePriceHistoryEntity(
                    serviceId = service.serviceId,
                    serviceName = service.name,
                    oldPrice = oldPrice,
                    newPrice = service.basePrice,
                    changedByAdmin = adminEmail,
                    reason = reason
                )
            )
        }
        dao.insertAuditLog(
            AuditLogEntity(
                actorId = adminEmail,
                actorRole = "ADMIN",
                action = "UPDATE_SERVICE",
                targetEntity = "SERVICE",
                targetId = service.serviceId,
                details = "Admin memperbarui layanan '${service.name}'. Harga: Rp $oldPrice -> Rp ${service.basePrice}. Alasan: $reason"
            )
        )
    }

    suspend fun deleteService(id: String, serviceName: String, adminEmail: String) {
        dao.deleteService(id)
        dao.insertAuditLog(
            AuditLogEntity(
                actorId = adminEmail,
                actorRole = "ADMIN",
                action = "DELETE_SERVICE",
                targetEntity = "SERVICE",
                targetId = id,
                details = "Admin menghapus layanan '$serviceName' (ID: $id)"
            )
        )
    }

    fun getAllPriceHistories(): Flow<List<ServicePriceHistoryEntity>> = dao.getAllPriceHistories()

    // --- Order Creation & Nearest Mechanic Matching ---
    suspend fun createOrder(
        customer: CustomerEntity,
        service: WorkshopServiceEntity,
        vehicleType: String,
        problemDescription: String,
        locationLat: Double,
        locationLng: Double,
        locationAddress: String
    ): OrderEntity {
        // Find all active mechanics
        val activeMechanics = dao.getActiveMechanicsList()
        val orderId = "BK-" + System.currentTimeMillis().toString().takeLast(6)

        // Find nearest active mechanic
        val sortedMechanics = activeMechanics.map { mechanic ->
            val dist = calculateDistanceKm(locationLat, locationLng, mechanic.latitude, mechanic.longitude)
            val eta = calculateEtaMinutes(dist)
            Triple(mechanic, dist, eta)
        }.sortedBy { it.second }

        val assignedMechanic = sortedMechanics.firstOrNull()

        val initialStatus = if (assignedMechanic != null) {
            OrderStatus.MECHANIC_FOUND.name
        } else {
            OrderStatus.WAITING_MECHANIC.name
        }

        val order = OrderEntity(
            orderId = orderId,
            customerId = customer.customerId,
            customerName = customer.fullName,
            customerPhone = customer.phone,
            customerAddress = locationAddress,
            customerLat = locationLat,
            customerLng = locationLng,
            mechanicId = assignedMechanic?.first?.mechanicId,
            mechanicName = assignedMechanic?.first?.fullName,
            mechanicPhone = assignedMechanic?.first?.phone,
            mechanicPhoto = assignedMechanic?.first?.profilePhotoUrl,
            mechanicRating = assignedMechanic?.first?.rating ?: 5.0,
            mechanicJobsCount = assignedMechanic?.first?.completedJobsCount ?: 0,
            mechanicLat = assignedMechanic?.first?.latitude ?: 0.0,
            mechanicLng = assignedMechanic?.first?.longitude ?: 0.0,
            vehicleType = vehicleType,
            serviceId = service.serviceId,
            serviceName = service.name,
            problemDescription = problemDescription,
            officialPrice = service.basePrice,
            additionalPrice = 0L,
            totalAmount = service.basePrice,
            status = initialStatus,
            distanceKm = assignedMechanic?.second ?: 2.0,
            travelEtaMinutes = assignedMechanic?.third ?: 15
        )

        dao.insertOrder(order)

        // Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                actorId = customer.customerId,
                actorRole = "CUSTOMER",
                action = "CREATE_ORDER",
                targetEntity = "ORDER",
                targetId = orderId,
                details = "Customer membuat pesanan $orderId untuk ${service.name}. Montir ditugaskan: ${assignedMechanic?.first?.fullName ?: "Mencari montir"}"
            )
        )

        // Notification to Mechanic if assigned
        assignedMechanic?.first?.let { mech ->
            dao.insertNotification(
                NotificationEntity(
                    targetUserId = mech.mechanicId,
                    targetRole = "MECHANIC",
                    title = "Permintaan Pekerjaan Baru!",
                    message = "Pesanan masuk dari ${customer.fullName} untuk ${service.name} (${order.distanceKm} km).",
                    orderId = orderId,
                    type = "ORDER_NEW"
                )
            )
        }

        // Notification to Admin
        dao.insertNotification(
            NotificationEntity(
                targetUserId = "ALL_ADMIN",
                targetRole = "ADMIN",
                title = "Pesanan Baru Masuk",
                message = "Pesanan $orderId (${service.name}) dibuat oleh ${customer.fullName}.",
                orderId = orderId,
                type = "ORDER_NEW"
            )
        )

        return order
    }

    // --- Order Status Progression ---
    suspend fun acceptOrderByMechanic(orderId: String, mechanicId: String) {
        val order = dao.getOrderById(orderId) ?: return
        val mechanic = dao.getMechanicById(mechanicId) ?: return

        val updatedOrder = order.copy(
            status = OrderStatus.MECHANIC_ACCEPTED.name,
            mechanicId = mechanic.mechanicId,
            mechanicName = mechanic.fullName,
            mechanicPhone = mechanic.phone,
            mechanicRating = mechanic.rating,
            mechanicJobsCount = mechanic.completedJobsCount,
            mechanicLat = mechanic.latitude,
            mechanicLng = mechanic.longitude,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updatedOrder)

        // Create or get chat session
        val chatId = "chat_${order.customerId}_${mechanic.mechanicId}"
        dao.insertChat(
            ChatEntity(
                chatId = chatId,
                orderId = orderId,
                customerId = order.customerId,
                mechanicId = mechanicId,
                lastMessage = "Montir telah menerima pesanan.",
                lastMessageTimestamp = System.currentTimeMillis()
            )
        )

        // Notification to Customer
        dao.insertNotification(
            NotificationEntity(
                targetUserId = order.customerId,
                targetRole = "CUSTOMER",
                title = "Montir Menerima Pesanan!",
                message = "${mechanic.fullName} menerima pesanan Anda dan siap meluncur ke lokasi.",
                orderId = orderId,
                type = "ORDER_ACCEPTED"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                actorId = mechanicId,
                actorRole = "MECHANIC",
                action = "ACCEPT_ORDER",
                targetEntity = "ORDER",
                targetId = orderId,
                details = "Montir ${mechanic.fullName} menerima pesanan $orderId"
            )
        )
    }

    suspend fun rejectOrderByMechanic(orderId: String, mechanicId: String, reason: String) {
        val order = dao.getOrderById(orderId) ?: return
        val mechanic = dao.getMechanicById(mechanicId) ?: return

        // Record rejection log
        dao.insertRejectionLog(
            RejectionLogEntity(
                orderId = orderId,
                mechanicId = mechanicId,
                mechanicName = mechanic.fullName,
                reason = reason
            )
        )

        // Increment rejection count
        dao.updateMechanic(mechanic.copy(rejectionCount = mechanic.rejectionCount + 1))

        // Find rejection history for this order to exclude
        val rejections = dao.getRejectionLogsList()
        val rejectedMechanicIds = rejections.filter { it.orderId == orderId }.map { it.mechanicId }.toSet() + mechanicId

        // Find NEXT nearest active mechanic
        val activeMechanics = dao.getActiveMechanicsList()
        val nextMechanic = activeMechanics
            .filter { it.mechanicId !in rejectedMechanicIds }
            .map { mech ->
                val dist = calculateDistanceKm(order.customerLat, order.customerLng, mech.latitude, mech.longitude)
                val eta = calculateEtaMinutes(dist)
                Triple(mech, dist, eta)
            }
            .sortedBy { it.second }
            .firstOrNull()

        if (nextMechanic != null) {
            val updatedOrder = order.copy(
                mechanicId = nextMechanic.first.mechanicId,
                mechanicName = nextMechanic.first.fullName,
                mechanicPhone = nextMechanic.first.phone,
                mechanicRating = nextMechanic.first.rating,
                mechanicJobsCount = nextMechanic.first.completedJobsCount,
                mechanicLat = nextMechanic.first.latitude,
                mechanicLng = nextMechanic.first.longitude,
                distanceKm = nextMechanic.second,
                travelEtaMinutes = nextMechanic.third,
                status = OrderStatus.MECHANIC_FOUND.name,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateOrder(updatedOrder)

            dao.insertNotification(
                NotificationEntity(
                    targetUserId = nextMechanic.first.mechanicId,
                    targetRole = "MECHANIC",
                    title = "Permintaan Pekerjaan Baru!",
                    message = "Pesanan masuk dari ${order.customerName} untuk ${order.serviceName}.",
                    orderId = orderId,
                    type = "ORDER_NEW"
                )
            )
        } else {
            // No other active mechanic available right now
            val updatedOrder = order.copy(
                mechanicId = null,
                mechanicName = null,
                status = OrderStatus.WAITING_MECHANIC.name,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateOrder(updatedOrder)

            dao.insertNotification(
                NotificationEntity(
                    targetUserId = order.customerId,
                    targetRole = "CUSTOMER",
                    title = "Mencari Montir...",
                    message = "Sistem sedang mencari montir terdekat berikutnya untuk pesanan Anda.",
                    orderId = orderId,
                    type = "ORDER_SEARCHING"
                )
            )
        }

        dao.insertAuditLog(
            AuditLogEntity(
                actorId = mechanicId,
                actorRole = "MECHANIC",
                action = "REJECT_ORDER",
                targetEntity = "ORDER",
                targetId = orderId,
                details = "Montir ${mechanic.fullName} menolak pesanan $orderId. Alasan: $reason"
            )
        )
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus, actorId: String, actorRole: String) {
        val order = dao.getOrderById(orderId) ?: return
        val updated = order.copy(
            status = newStatus.name,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updated)

        // Notifications according to status
        when (newStatus) {
            OrderStatus.HEADING_TO_LOCATION -> {
                dao.insertNotification(
                    NotificationEntity(
                        targetUserId = order.customerId,
                        targetRole = "CUSTOMER",
                        title = "Montir Menuju Lokasi",
                        message = "${order.mechanicName} sedang bergerak menuju lokasi Anda. Estimasi tiba: ${order.travelEtaMinutes} menit.",
                        orderId = orderId,
                        type = "STATUS_UPDATE"
                    )
                )
            }
            OrderStatus.ARRIVED_AT_LOCATION -> {
                dao.insertNotification(
                    NotificationEntity(
                        targetUserId = order.customerId,
                        targetRole = "CUSTOMER",
                        title = "Montir Telah Tiba!",
                        message = "${order.mechanicName} sudah sampai di titik lokasi Anda.",
                        orderId = orderId,
                        type = "STATUS_UPDATE"
                    )
                )
            }
            OrderStatus.WORKING_IN_PROGRESS -> {
                dao.insertNotification(
                    NotificationEntity(
                        targetUserId = order.customerId,
                        targetRole = "CUSTOMER",
                        title = "Pengerjaan Dimulai",
                        message = "Montir sedang melakukan perbaikan pada kendaraan Anda.",
                        orderId = orderId,
                        type = "STATUS_UPDATE"
                    )
                )
            }
            OrderStatus.WORK_COMPLETED -> {
                dao.insertNotification(
                    NotificationEntity(
                        targetUserId = order.customerId,
                        targetRole = "CUSTOMER",
                        title = "Pengerjaan Selesai",
                        message = "Perbaikan selesai! Silakan periksa ringkasan dan selesaikan pembayaran.",
                        orderId = orderId,
                        type = "STATUS_UPDATE"
                    )
                )
            }
            else -> {}
        }

        dao.insertAuditLog(
            AuditLogEntity(
                actorId = actorId,
                actorRole = actorRole,
                action = "UPDATE_ORDER_STATUS",
                targetEntity = "ORDER",
                targetId = orderId,
                details = "Status pesanan $orderId diubah menjadi ${newStatus.title}"
            )
        )
    }

    // --- Additional Fee Management ---
    suspend fun requestAdditionalFee(
        orderId: String,
        additionalAmount: Long,
        notes: String,
        mechanicId: String
    ) {
        val order = dao.getOrderById(orderId) ?: return
        val updated = order.copy(
            additionalRequested = true,
            additionalPrice = additionalAmount,
            additionalNotes = notes,
            additionalApprovedByCustomer = false,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updated)

        dao.insertNotification(
            NotificationEntity(
                targetUserId = order.customerId,
                targetRole = "CUSTOMER",
                title = "Persetujuan Tambahan Biaya",
                message = "Montir mengajukan tambahan biaya Rp $additionalAmount untuk: $notes. Silakan konfirmasi persetujuan di aplikasi.",
                orderId = orderId,
                type = "ADDITIONAL_FEE_REQUEST"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                actorId = mechanicId,
                actorRole = "MECHANIC",
                action = "REQUEST_ADDITIONAL_FEE",
                targetEntity = "ORDER",
                targetId = orderId,
                details = "Montir mengajukan tambahan biaya Rp $additionalAmount. Catatan: $notes"
            )
        )
    }

    suspend fun answerAdditionalFee(orderId: String, customerId: String, isApproved: Boolean) {
        val order = dao.getOrderById(orderId) ?: return
        val updated = if (isApproved) {
            order.copy(
                additionalApprovedByCustomer = true,
                additionalRequested = false,
                totalAmount = order.officialPrice + order.additionalPrice,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            order.copy(
                additionalApprovedByCustomer = false,
                additionalRequested = false,
                additionalPrice = 0L,
                additionalNotes = order.additionalNotes + " (Ditolak customer)",
                totalAmount = order.officialPrice,
                updatedAt = System.currentTimeMillis()
            )
        }
        dao.updateOrder(updated)

        val statusText = if (isApproved) "DISETUJUI" else "DITOLAK"
        order.mechanicId?.let { mechId ->
            dao.insertNotification(
                NotificationEntity(
                    targetUserId = mechId,
                    targetRole = "MECHANIC",
                    title = "Biaya Tambahan $statusText",
                    message = "Customer telah $statusText pengajuan tambahan biaya untuk pesanan $orderId.",
                    orderId = orderId,
                    type = "ADDITIONAL_FEE_RESPONSE"
                )
            )
        }

        dao.insertAuditLog(
            AuditLogEntity(
                actorId = customerId,
                actorRole = "CUSTOMER",
                action = "ANSWER_ADDITIONAL_FEE",
                targetEntity = "ORDER",
                targetId = orderId,
                details = "Customer $statusText tambahan biaya Rp ${order.additionalPrice}"
            )
        )
    }

    // --- Mechanic Finish Job Report ---
    suspend fun completeWorkReport(
        orderId: String,
        workDetails: String,
        sparePartsUsed: String,
        workNotes: String,
        photoBefore: String,
        photoAfter: String,
        mechanicId: String
    ) {
        val order = dao.getOrderById(orderId) ?: return
        val updated = order.copy(
            status = OrderStatus.WAITING_PAYMENT.name,
            workDetails = workDetails,
            sparePartsUsed = sparePartsUsed,
            workNotes = workNotes,
            photoBefore = photoBefore,
            photoAfter = photoAfter,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updated)

        dao.insertNotification(
            NotificationEntity(
                targetUserId = order.customerId,
                targetRole = "CUSTOMER",
                title = "Pekerjaan Selesai - Menunggu Pembayaran",
                message = "Montir telah menyelesaikan pengerjaan. Total biaya: Rp ${order.totalAmount}.",
                orderId = orderId,
                type = "ORDER_WORK_COMPLETED"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                actorId = mechanicId,
                actorRole = "MECHANIC",
                action = "SUBMIT_WORK_REPORT",
                targetEntity = "ORDER",
                targetId = orderId,
                details = "Montir menyelesaikan pekerjaan $orderId. Suku cadang: $sparePartsUsed"
            )
        )
    }

    // --- Payment Completion ---
    suspend fun completePayment(
        orderId: String,
        paymentMethod: String,
        customerId: String
    ) {
        val order = dao.getOrderById(orderId) ?: return
        val now = System.currentTimeMillis()

        val updatedOrder = order.copy(
            isPaid = true,
            paidAt = now,
            paymentMethod = paymentMethod,
            status = OrderStatus.COMPLETED.name,
            completedAt = now,
            updatedAt = now
        )
        dao.updateOrder(updatedOrder)

        // Insert payment entity
        dao.insertPayment(
            PaymentEntity(
                paymentId = "PAY-" + now.toString().takeLast(6),
                orderId = orderId,
                customerId = customerId,
                amount = order.totalAmount,
                method = paymentMethod,
                status = "SUCCESS",
                transactionTime = now
            )
        )

        // Update mechanic earnings & completed jobs
        order.mechanicId?.let { mechId ->
            val mechanic = dao.getMechanicById(mechId)
            mechanic?.let {
                dao.updateMechanic(
                    it.copy(
                        completedJobsCount = it.completedJobsCount + 1,
                        earnings = it.earnings + order.totalAmount
                    )
                )
            }

            dao.insertNotification(
                NotificationEntity(
                    targetUserId = mechId,
                    targetRole = "MECHANIC",
                    title = "Pembayaran Diterima!",
                    message = "Pembayaran pesanan $orderId sebesar Rp ${order.totalAmount} melalui $paymentMethod berhasil diterima.",
                    orderId = orderId,
                    type = "PAYMENT_SUCCESS"
                )
            )
        }

        // Notify Admin
        dao.insertNotification(
            NotificationEntity(
                targetUserId = "ALL_ADMIN",
                targetRole = "ADMIN",
                title = "Pekerjaan Selesai & Lunas",
                message = "Pesanan $orderId (${order.serviceName}) selesai dan dibayar Rp ${order.totalAmount}.",
                orderId = orderId,
                type = "ORDER_COMPLETED"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                actorId = customerId,
                actorRole = "CUSTOMER",
                action = "COMPLETE_PAYMENT",
                targetEntity = "ORDER",
                targetId = orderId,
                details = "Customer membayar pesanan $orderId sebesar Rp ${order.totalAmount} dengan $paymentMethod"
            )
        )
    }

    // --- Rating & Review ---
    suspend fun submitRatingReview(
        orderId: String,
        rating: Int,
        comment: String,
        customerId: String,
        customerName: String
    ) {
        val order = dao.getOrderById(orderId) ?: return
        val updatedOrder = order.copy(
            ratingGiven = rating,
            reviewComment = comment,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updatedOrder)

        order.mechanicId?.let { mechId ->
            val reviewId = "REV-" + System.currentTimeMillis().toString().takeLast(6)
            dao.insertReview(
                ReviewEntity(
                    reviewId = reviewId,
                    orderId = orderId,
                    mechanicId = mechId,
                    customerId = customerId,
                    customerName = customerName,
                    rating = rating,
                    comment = comment
                )
            )

            // Recalculate mechanic rating
            val mechanic = dao.getMechanicById(mechId)
            mechanic?.let {
                val currentCount = it.reviewCount
                val currentRating = it.rating
                val newCount = currentCount + 1
                val newRating = ((currentRating * currentCount) + rating) / newCount
                val roundedRating = Math.round(newRating * 10.0) / 10.0
                dao.updateMechanic(
                    it.copy(
                        rating = roundedRating,
                        reviewCount = newCount
                    )
                )
            }
        }

        dao.insertAuditLog(
            AuditLogEntity(
                actorId = customerId,
                actorRole = "CUSTOMER",
                action = "SUBMIT_REVIEW",
                targetEntity = "ORDER",
                targetId = orderId,
                details = "Customer memberikan rating $rating bintang dan ulasan untuk pesanan $orderId"
            )
        )
    }

    // --- Order Flows & Direct Queries ---
    fun getOrderByIdFlow(id: String): Flow<OrderEntity?> = dao.getOrderByIdFlow(id)
    suspend fun getOrderById(id: String): OrderEntity? = dao.getOrderById(id)
    fun getOrdersByCustomer(customerId: String): Flow<List<OrderEntity>> = dao.getOrdersByCustomer(customerId)
    fun getActiveOrderByCustomer(customerId: String): Flow<OrderEntity?> = dao.getActiveOrderByCustomer(customerId)
    fun getOrdersByMechanic(mechanicId: String): Flow<List<OrderEntity>> = dao.getOrdersByMechanic(mechanicId)
    fun getActiveOrderByMechanic(mechanicId: String): Flow<OrderEntity?> = dao.getActiveOrderByMechanic(mechanicId)
    fun getAllOrders(): Flow<List<OrderEntity>> = dao.getAllOrders()

    // --- Rejection Logs ---
    fun getAllRejectionLogs(): Flow<List<RejectionLogEntity>> = dao.getAllRejectionLogs()

    // --- Chats & Messages ---
    fun getMessagesByOrderId(orderId: String): Flow<List<MessageEntity>> = dao.getMessagesByOrderId(orderId)
    fun getChatsForUser(userId: String): Flow<List<ChatEntity>> = dao.getChatsForUser(userId)

    suspend fun sendMessage(
        orderId: String,
        senderId: String,
        senderRole: String,
        senderName: String,
        text: String
    ) {
        val order = dao.getOrderById(orderId) ?: return
        val chatId = "chat_${order.customerId}_${order.mechanicId ?: "none"}"
        val messageId = "msg_" + System.currentTimeMillis()
        val now = System.currentTimeMillis()

        dao.insertMessage(
            MessageEntity(
                messageId = messageId,
                chatId = chatId,
                orderId = orderId,
                senderId = senderId,
                senderRole = senderRole,
                senderName = senderName,
                text = text,
                timestamp = now,
                isRead = false
            )
        )

        dao.insertChat(
            ChatEntity(
                chatId = chatId,
                orderId = orderId,
                customerId = order.customerId,
                mechanicId = order.mechanicId ?: "",
                lastMessage = text,
                lastMessageTimestamp = now
            )
        )

        // Notification to recipient
        val targetUserId = if (senderRole == "CUSTOMER") order.mechanicId else order.customerId
        val targetRole = if (senderRole == "CUSTOMER") "MECHANIC" else "CUSTOMER"

        targetUserId?.let {
            dao.insertNotification(
                NotificationEntity(
                    targetUserId = it,
                    targetRole = targetRole,
                    title = "Pesan Baru dari $senderName",
                    message = text.take(60),
                    orderId = orderId,
                    type = "CHAT"
                )
            )
        }
    }

    // --- Notifications ---
    fun getNotificationsForUser(userId: String, role: String): Flow<List<NotificationEntity>> =
        dao.getNotificationsForUser(userId, role)

    suspend fun markNotificationRead(id: Long) = dao.markNotificationRead(id)

    // --- Reviews ---
    fun getReviewsForMechanic(mechanicId: String): Flow<List<ReviewEntity>> = dao.getReviewsForMechanic(mechanicId)
    fun getAllReviews(): Flow<List<ReviewEntity>> = dao.getAllReviews()

    // --- Audit Logs ---
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()

    // --- Admin Operations ---
    suspend fun validateAdminLogin(email: String, password: String): Boolean {
        // STRICT REQUIREMENT from prompt:
        // "admin login khusus hanya dengan email Calonbikers85@gmail.com dan kata sandi Pekalongan27"
        // "tolak semua akses yang mencoba masuk diakun admin selain dengan email dan kata sandi itu"
        val trimmedEmail = email.trim()
        val isValid = trimmedEmail.equals("Calonbikers85@gmail.com", ignoreCase = true) && password == "Pekalongan27"
        if (isValid) {
            dao.insertAuditLog(
                AuditLogEntity(
                    actorId = trimmedEmail,
                    actorRole = "ADMIN",
                    action = "ADMIN_LOGIN_SUCCESS",
                    targetEntity = "AUTH",
                    targetId = "admin_root",
                    details = "Admin utama berhasil login ke Bengkelku"
                )
            )
        } else {
            dao.insertAuditLog(
                AuditLogEntity(
                    actorId = trimmedEmail,
                    actorRole = "ANONYMOUS",
                    action = "ADMIN_LOGIN_REJECTED",
                    targetEntity = "AUTH",
                    targetId = "none",
                    details = "Percobaan masuk admin ditolak untuk email '$trimmedEmail'"
                )
            )
        }
        return isValid
    }

    suspend fun approveMechanic(mechanicId: String, adminEmail: String) {
        dao.updateMechanicStatus(mechanicId, MechanicStatus.ACTIVE.name)
        dao.insertNotification(
            NotificationEntity(
                targetUserId = mechanicId,
                targetRole = "MECHANIC",
                title = "Akun Montir Disetujui!",
                message = "Selamat! Akun montir Anda telah disetujui oleh Admin. Anda sekarang dapat menerima pekerjaan.",
                type = "MECHANIC_APPROVED"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorId = adminEmail,
                actorRole = "ADMIN",
                action = "APPROVE_MECHANIC",
                targetEntity = "MECHANIC",
                targetId = mechanicId,
                details = "Admin menyetujui pendaftaran montir ID: $mechanicId"
            )
        )
    }

    suspend fun rejectMechanic(mechanicId: String, adminEmail: String, reason: String) {
        dao.updateMechanicStatus(mechanicId, MechanicStatus.REJECTED.name)
        dao.insertNotification(
            NotificationEntity(
                targetUserId = mechanicId,
                targetRole = "MECHANIC",
                title = "Pendaftaran Montir Ditolak",
                message = "Mohon maaf, pendaftaran Anda belum disetujui. Alasan: $reason",
                type = "MECHANIC_REJECTED"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                actorId = adminEmail,
                actorRole = "ADMIN",
                action = "REJECT_MECHANIC",
                targetEntity = "MECHANIC",
                targetId = mechanicId,
                details = "Admin menolak pendaftaran montir $mechanicId. Alasan: $reason"
            )
        )
    }

    suspend fun suspendMechanic(mechanicId: String, adminEmail: String) {
        dao.updateMechanicStatus(mechanicId, MechanicStatus.SUSPENDED.name)
        dao.insertAuditLog(
            AuditLogEntity(
                actorId = adminEmail,
                actorRole = "ADMIN",
                action = "SUSPEND_MECHANIC",
                targetEntity = "MECHANIC",
                targetId = mechanicId,
                details = "Admin menangguhkan montir ID: $mechanicId"
            )
        )
    }

    // --- Admin Metrics ---
    fun getTotalOrdersCount(): Flow<Int> = dao.getTotalOrdersCount()
    fun getCompletedOrdersCount(): Flow<Int> = dao.getCompletedOrdersCount()
    fun getRunningOrdersCount(): Flow<Int> = dao.getRunningOrdersCount()
    fun getCancelledOrdersCount(): Flow<Int> = dao.getCancelledOrdersCount()
    fun getTotalRevenue(): Flow<Long> = dao.getTotalRevenue()
}
