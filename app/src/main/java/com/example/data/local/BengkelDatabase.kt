package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CustomerEntity::class,
        MechanicEntity::class,
        WorkshopServiceEntity::class,
        ServicePriceHistoryEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        LocationEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        NotificationEntity::class,
        ReviewEntity::class,
        PaymentEntity::class,
        MechanicDocumentEntity::class,
        AdminUserEntity::class,
        AuditLogEntity::class,
        RejectionLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BengkelDatabase : RoomDatabase() {

    abstract fun bengkelDao(): BengkelDao

    companion object {
        @Volatile
        private var INSTANCE: BengkelDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BengkelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BengkelDatabase::class.java,
                    "bengkelku_database"
                )
                    .addCallback(BengkelDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class BengkelDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.bengkelDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: BengkelDao) {
                // 1. Initial Admin User
                dao.insertAdminUser(
                    AdminUserEntity(
                        adminId = "admin_root",
                        email = "Calonbikers85@gmail.com",
                        name = "Admin Utama Bengkelku",
                        role = "SUPER_ADMIN"
                    )
                )

                // 2. Initial Official Workshop Services as specified in prompt
                val initialServices = listOf(
                    WorkshopServiceEntity(
                        serviceId = "srv_ganti_oli",
                        name = "Ganti oli",
                        description = "Penggantian oli mesin & filter berkualitas tinggi sesuai spesifikasi kendaraan.",
                        basePrice = 65000L,
                        estimatedDurationMinutes = 20,
                        category = "Perawatan Rutin",
                        iconName = "oil"
                    ),
                    WorkshopServiceEntity(
                        serviceId = "srv_tambal_ban",
                        name = "Tambal ban",
                        description = "Perbaikan ban bocor tubeless atau ban dalam darurat di lokasi.",
                        basePrice = 25000L,
                        estimatedDurationMinutes = 15,
                        category = "Darurat & Ban",
                        iconName = "tire"
                    ),
                    WorkshopServiceEntity(
                        serviceId = "srv_ganti_ban",
                        name = "Ganti ban",
                        description = "Bongkar pasang ban luar dan ban dalam baru secara presisi.",
                        basePrice = 50000L,
                        estimatedDurationMinutes = 25,
                        category = "Darurat & Ban",
                        iconName = "tire"
                    ),
                    WorkshopServiceEntity(
                        serviceId = "srv_aki",
                        name = "Aki kendaraan",
                        description = "Pengecekan voltase aki, jumper starter aki soak, atau penggantian aki baru.",
                        basePrice = 180000L,
                        estimatedDurationMinutes = 20,
                        category = "Kelistrikan",
                        iconName = "battery"
                    ),
                    WorkshopServiceEntity(
                        serviceId = "srv_mesin",
                        name = "Servis mesin",
                        description = "Diagnosa & penanganan mesin brebet, mati total, overheat, atau bunyi abnormal.",
                        basePrice = 150000L,
                        estimatedDurationMinutes = 60,
                        category = "Mesin & Transmisi",
                        iconName = "engine"
                    ),
                    WorkshopServiceEntity(
                        serviceId = "srv_rem",
                        name = "Servis rem",
                        description = "Pembersihan kaliper, bleeding minyak rem, dan penggantian kampas rem depan/belakang.",
                        basePrice = 45000L,
                        estimatedDurationMinutes = 30,
                        category = "Sistem Pengereman",
                        iconName = "brake"
                    ),
                    WorkshopServiceEntity(
                        serviceId = "srv_listrik",
                        name = "Servis kelistrikan",
                        description = "Perbaikan sekring putus, lampu mati, klakson, sistem starter, dan jalur kabel.",
                        basePrice = 75000L,
                        estimatedDurationMinutes = 40,
                        category = "Kelistrikan",
                        iconName = "electric"
                    ),
                    WorkshopServiceEntity(
                        serviceId = "srv_tuneup",
                        name = "Tune up",
                        description = "Pembersihan throttle body/karburator, setel klep, busi, dan pembersihan filter udara.",
                        basePrice = 95000L,
                        estimatedDurationMinutes = 45,
                        category = "Perawatan Rutin",
                        iconName = "tune"
                    ),
                    WorkshopServiceEntity(
                        serviceId = "srv_cek_kendaraan",
                        name = "Cek kendaraan",
                        description = "Inspeksi menyeluruh 20 titik sebelum perjalanan jauh atau pembelian motor/mobil.",
                        basePrice = 35000L,
                        estimatedDurationMinutes = 20,
                        category = "Inspeksi",
                        iconName = "check"
                    ),
                    WorkshopServiceEntity(
                        serviceId = "srv_mogok",
                        name = "Mogok di jalan",
                        description = "Layanan cepat montir darurat langsung datang ke titik koordinat jalan Anda.",
                        basePrice = 85000L,
                        estimatedDurationMinutes = 30,
                        category = "Darurat & Ban",
                        iconName = "emergency"
                    ),
                    WorkshopServiceEntity(
                        serviceId = "srv_lainnya",
                        name = "Layanan lainnya",
                        description = "Konsultasi kendala teknis khusus atau perbaikan mekanikal lainnya.",
                        basePrice = 50000L,
                        estimatedDurationMinutes = 30,
                        category = "Lainnya",
                        iconName = "build"
                    )
                )

                initialServices.forEach { service ->
                    dao.insertService(service)
                    dao.insertPriceHistory(
                        ServicePriceHistoryEntity(
                            serviceId = service.serviceId,
                            serviceName = service.name,
                            oldPrice = service.basePrice,
                            newPrice = service.basePrice,
                            changedByAdmin = "Calonbikers85@gmail.com",
                            reason = "Penetapan tarif awal resmi Bengkelku"
                        )
                    )
                }

                // 3. Seed sample Mechanics
                val initialMechanics = listOf(
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
                        status = "WAITING_APPROVAL", // Pending approval demo for Admin
                        rating = 5.0,
                        reviewCount = 0,
                        completedJobsCount = 0,
                        earnings = 0L
                    ),
                    MechanicEntity(
                        mechanicId = "montir_eko",
                        fullName = "Eko Nugroho",
                        phone = "081234567890",
                        email = "eko.montir@bengkelku.id",
                        address = "Jl. Progo No. 15, Pekalongan",
                        skills = "Servis Mesin, Transmisi Otomatis, Injeksi",
                        experienceYears = 7,
                        workLocation = "Pekalongan Barat",
                        latitude = -6.8858,
                        longitude = 109.6825,
                        status = "BUSY", // Sibuk melayani pesanan lain
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
                        status = "OFFLINE", // Offline sedang istirahat
                        rating = 4.7,
                        reviewCount = 19,
                        completedJobsCount = 42,
                        earnings = 2100000L
                    )
                )

                initialMechanics.forEach { mechanic ->
                    dao.insertMechanic(mechanic)
                    dao.insertUser(
                        UserEntity(
                            id = mechanic.mechanicId,
                            email = mechanic.email,
                            phone = mechanic.phone,
                            passwordHash = "montir123",
                            role = "MECHANIC",
                            fullName = mechanic.fullName,
                            address = mechanic.address,
                            isPhoneVerified = true,
                            isEmailValidated = true,
                            isProfileComplete = true
                        )
                    )
                }

                // 4. Seed sample Customer
                val sampleCustomer = CustomerEntity(
                    customerId = "customer_demo",
                    fullName = "Bambang Pamungkas",
                    phone = "081234567890",
                    email = "customer@bengkelku.id",
                    address = "Jl. Merdeka No. 15, Pekalongan",
                    latitude = -6.8887,
                    longitude = 109.6753,
                    defaultVehicle = "Honda Vario 160 (G 1234 AB)"
                )
                dao.insertCustomer(sampleCustomer)
                dao.insertUser(
                    UserEntity(
                        id = sampleCustomer.customerId,
                        email = sampleCustomer.email,
                        phone = sampleCustomer.phone,
                        passwordHash = "customer123",
                        role = "CUSTOMER",
                        fullName = sampleCustomer.fullName,
                        address = sampleCustomer.address,
                        isPhoneVerified = true,
                        isEmailValidated = true,
                        isProfileComplete = true
                    )
                )

                // 5. Seed some initial completed reviews & history
                val sampleReview = ReviewEntity(
                    reviewId = "rev_1",
                    orderId = "ord_sample_01",
                    mechanicId = "montir_budi",
                    customerId = "customer_demo",
                    customerName = "Bambang Pamungkas",
                    rating = 5,
                    comment = "Montir Budi cepat datang, penjelasannya ramah dan pengerjaan servis mesin sangat rapi. Sangat terbantu!"
                )
                dao.insertReview(sampleReview)

                dao.insertAuditLog(
                    AuditLogEntity(
                        actorId = "system",
                        actorRole = "SYSTEM",
                        action = "SYSTEM_INITIALIZED",
                        targetEntity = "DATABASE",
                        targetId = "bengkelku_database",
                        details = "Database Bengkelku berhasil diinisialisasi dengan layanan resmi dan akun terdaftar."
                    )
                )
            }
        }
    }
}
