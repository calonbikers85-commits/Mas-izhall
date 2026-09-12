package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BengkelDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'")
    fun getCustomerCount(): Flow<Int>

    // --- Customers ---
    @Query("SELECT * FROM customers WHERE customerId = :id LIMIT 1")
    fun getCustomerByIdFlow(id: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE customerId = :id LIMIT 1")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    // --- Mechanics ---
    @Query("SELECT * FROM mechanics WHERE mechanicId = :id LIMIT 1")
    fun getMechanicByIdFlow(id: String): Flow<MechanicEntity?>

    @Query("SELECT * FROM mechanics WHERE mechanicId = :id LIMIT 1")
    suspend fun getMechanicById(id: String): MechanicEntity?

    @Query("SELECT * FROM mechanics")
    fun getAllMechanics(): Flow<List<MechanicEntity>>

    @Query("SELECT * FROM mechanics WHERE status = :status")
    fun getMechanicsByStatus(status: String): Flow<List<MechanicEntity>>

    @Query("SELECT * FROM mechanics WHERE status = 'ACTIVE'")
    suspend fun getActiveMechanicsList(): List<MechanicEntity>

    @Query("SELECT * FROM mechanics WHERE status = 'ACTIVE'")
    fun getActiveMechanicsFlow(): Flow<List<MechanicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMechanic(mechanic: MechanicEntity)

    @Update
    suspend fun updateMechanic(mechanic: MechanicEntity)

    @Query("UPDATE mechanics SET status = :status WHERE mechanicId = :id")
    suspend fun updateMechanicStatus(id: String, status: String)

    @Query("UPDATE mechanics SET latitude = :lat, longitude = :lng WHERE mechanicId = :id")
    suspend fun updateMechanicLocation(id: String, lat: Double, lng: Double)

    @Query("SELECT COUNT(*) FROM mechanics")
    fun getTotalMechanicsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM mechanics WHERE status = 'ACTIVE'")
    fun getActiveMechanicsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM mechanics WHERE status = 'OFFLINE'")
    fun getOfflineMechanicsCount(): Flow<Int>

    // --- Services ---
    @Query("SELECT * FROM services WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveServices(): Flow<List<WorkshopServiceEntity>>

    @Query("SELECT * FROM services ORDER BY name ASC")
    fun getAllServices(): Flow<List<WorkshopServiceEntity>>

    @Query("SELECT * FROM services WHERE serviceId = :id LIMIT 1")
    suspend fun getServiceById(id: String): WorkshopServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: WorkshopServiceEntity)

    @Update
    suspend fun updateService(service: WorkshopServiceEntity)

    @Query("DELETE FROM services WHERE serviceId = :id")
    suspend fun deleteService(id: String)

    // --- Service Price Logs ---
    @Query("SELECT * FROM service_prices ORDER BY changedAt DESC")
    fun getAllPriceHistories(): Flow<List<ServicePriceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: ServicePriceHistoryEntity)

    // --- Orders ---
    @Query("SELECT * FROM orders WHERE orderId = :id LIMIT 1")
    fun getOrderByIdFlow(id: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE orderId = :id LIMIT 1")
    suspend fun getOrderById(id: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersByCustomer(customerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId AND status NOT IN ('COMPLETED', 'CANCELLED') LIMIT 1")
    fun getActiveOrderByCustomer(customerId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE mechanicId = :mechanicId ORDER BY createdAt DESC")
    fun getOrdersByMechanic(mechanicId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE mechanicId = :mechanicId AND status NOT IN ('COMPLETED', 'CANCELLED') LIMIT 1")
    fun getActiveOrderByMechanic(mechanicId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("SELECT COUNT(*) FROM orders")
    fun getTotalOrdersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED'")
    fun getCompletedOrdersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status NOT IN ('COMPLETED', 'CANCELLED', 'WAITING_MECHANIC')")
    fun getRunningOrdersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'CANCELLED'")
    fun getCancelledOrdersCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM orders WHERE isPaid = 1")
    fun getTotalRevenue(): Flow<Long>

    // --- Rejection Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRejectionLog(log: RejectionLogEntity)

    @Query("SELECT * FROM rejection_logs ORDER BY rejectedAt DESC")
    fun getAllRejectionLogs(): Flow<List<RejectionLogEntity>>

    @Query("SELECT * FROM rejection_logs ORDER BY rejectedAt DESC")
    suspend fun getRejectionLogsList(): List<RejectionLogEntity>

    // --- Chats & Messages ---
    @Query("SELECT * FROM chats WHERE orderId = :orderId LIMIT 1")
    suspend fun getChatByOrderId(orderId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE customerId = :userId OR mechanicId = :userId ORDER BY lastMessageTimestamp DESC")
    fun getChatsForUser(userId: String): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("SELECT * FROM messages WHERE orderId = :orderId ORDER BY timestamp ASC")
    fun getMessagesByOrderId(orderId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE targetUserId = :userId OR targetRole = :role OR targetUserId = 'ALL' ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String, role: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    // --- Reviews ---
    @Query("SELECT * FROM reviews WHERE mechanicId = :mechanicId ORDER BY createdAt DESC")
    fun getReviewsForMechanic(mechanicId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    // --- Payments ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Query("SELECT * FROM payments WHERE orderId = :orderId LIMIT 1")
    suspend fun getPaymentByOrderId(orderId: String): PaymentEntity?

    // --- Audit Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    // --- Admin Users ---
    @Query("SELECT * FROM admin_users WHERE email = :email LIMIT 1")
    suspend fun getAdminByEmail(email: String): AdminUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminUser(admin: AdminUserEntity)
}
