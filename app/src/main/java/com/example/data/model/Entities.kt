package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    MECHANIC,
    ADMIN
}

enum class MechanicStatus {
    WAITING_APPROVAL,
    ACTIVE,
    BUSY,
    OFFLINE,
    SUSPENDED,
    REJECTED
}

enum class OrderStatus(val title: String) {
    WAITING_MECHANIC("Menunggu montir"),
    MECHANIC_FOUND("Montir ditemukan"),
    MECHANIC_ACCEPTED("Montir menerima pesanan"),
    HEADING_TO_LOCATION("Montir menuju lokasi"),
    ARRIVED_AT_LOCATION("Montir tiba di lokasi"),
    WORKING_IN_PROGRESS("Pengerjaan berlangsung"),
    WORK_COMPLETED("Pengerjaan selesai"),
    WAITING_PAYMENT("Menunggu pembayaran"),
    COMPLETED("Pesanan selesai"),
    CANCELLED("Pesanan dibatalkan")
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: String,
    val fullName: String,
    val address: String,
    val isPhoneVerified: Boolean = false,
    val isEmailValidated: Boolean = false,
    val isProfileComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val customerId: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val address: String,
    val latitude: Double = -6.8887,
    val longitude: Double = 109.6753, // Default Pekalongan / Central Java region coordinates
    val defaultVehicle: String = "Honda Vario 160",
    val profilePhoto: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mechanics")
data class MechanicEntity(
    @PrimaryKey val mechanicId: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val address: String,
    val skills: String,
    val experienceYears: Int,
    val profilePhotoUrl: String = "",
    val workLocation: String,
    val latitude: Double = -6.8900,
    val longitude: Double = 109.6780,
    val status: String = MechanicStatus.WAITING_APPROVAL.name,
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val completedJobsCount: Int = 0,
    val earnings: Long = 0L,
    val rejectionCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "services")
data class WorkshopServiceEntity(
    @PrimaryKey val serviceId: String,
    val name: String,
    val description: String,
    val basePrice: Long,
    val estimatedDurationMinutes: Int,
    val isActive: Boolean = true,
    val category: String = "Reguler",
    val iconName: String = "build"
)

@Entity(tableName = "service_prices")
data class ServicePriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceId: String,
    val serviceName: String,
    val oldPrice: Long,
    val newPrice: Long,
    val changedByAdmin: String,
    val changedAt: Long = System.currentTimeMillis(),
    val reason: String = "Penyesuaian tarif berkala"
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val customerLat: Double,
    val customerLng: Double,
    val mechanicId: String? = null,
    val mechanicName: String? = null,
    val mechanicPhone: String? = null,
    val mechanicPhoto: String? = null,
    val mechanicRating: Double = 5.0,
    val mechanicJobsCount: Int = 0,
    val mechanicLat: Double = 0.0,
    val mechanicLng: Double = 0.0,
    val vehicleType: String,
    val serviceId: String,
    val serviceName: String,
    val problemDescription: String,
    val officialPrice: Long,
    val additionalPrice: Long = 0L,
    val additionalNotes: String = "",
    val additionalApprovedByCustomer: Boolean = false,
    val additionalRequested: Boolean = false,
    val totalAmount: Long,
    val status: String = OrderStatus.WAITING_MECHANIC.name,
    val workDetails: String = "",
    val sparePartsUsed: String = "",
    val workNotes: String = "",
    val photoBefore: String = "",
    val photoAfter: String = "",
    val paymentMethod: String = "TUNAI",
    val isPaid: Boolean = false,
    val paidAt: Long? = null,
    val ratingGiven: Int? = null,
    val reviewComment: String? = null,
    val travelEtaMinutes: Int = 15,
    val distanceKm: Double = 2.5,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val itemName: String,
    val itemPrice: Long,
    val isApproved: Boolean = true
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityId: String,
    val entityType: String,
    val latitude: Double,
    val longitude: Double,
    val heading: Float = 0f,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val chatId: String,
    val orderId: String,
    val customerId: String,
    val mechanicId: String,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val chatId: String,
    val orderId: String,
    val senderId: String,
    val senderRole: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetUserId: String,
    val targetRole: String,
    val title: String,
    val message: String,
    val orderId: String? = null,
    val type: String = "GENERAL",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val reviewId: String,
    val orderId: String,
    val mechanicId: String,
    val customerId: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val paymentId: String,
    val orderId: String,
    val customerId: String,
    val amount: Long,
    val method: String = "TUNAI",
    val status: String = "SUCCESS",
    val transactionTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "mechanic_documents")
data class MechanicDocumentEntity(
    @PrimaryKey val docId: String,
    val mechanicId: String,
    val docType: String,
    val docNumber: String,
    val docStatus: String = "VALID"
)

@Entity(tableName = "admin_users")
data class AdminUserEntity(
    @PrimaryKey val adminId: String,
    val email: String,
    val name: String,
    val role: String = "SUPER_ADMIN"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val actorId: String,
    val actorRole: String,
    val action: String,
    val targetEntity: String,
    val targetId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "rejection_logs")
data class RejectionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val mechanicId: String,
    val mechanicName: String,
    val reason: String,
    val rejectedAt: Long = System.currentTimeMillis()
)
