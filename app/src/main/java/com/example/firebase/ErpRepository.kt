package com.example.firebase

import android.content.Context
import com.example.R
import com.example.model.AppLanguage
import com.example.model.AuditLog
import com.example.model.BusinessCategory
import com.example.model.Company
import com.example.model.CompanyMember
import com.example.model.FinancialTransaction
import com.example.model.PaymentMethod
import com.example.model.PaymentRequest
import com.example.model.Permission
import com.example.model.ProductionOrder
import com.example.model.ProductionStatus
import com.example.model.ProductionType
import com.example.model.StaffType
import com.example.model.SubscriptionPlan
import com.example.model.TenantNotification
import com.example.model.TransactionEntryType
import com.example.model.UserProfile
import com.example.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ErpRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun generateShortId(prefix: String): String {
        val token = UUID.randomUUID().toString().replace("-", "").take(12)
        return "${prefix}_$token"
    }

    private fun generateCompanyCode(): String {
        val code = UUID.randomUUID().toString().replace("-", "").take(6).uppercase()
        return "DE-$code"
    }

    private fun <T> wrapSnapshotError(
        flow: Flow<T>,
        path: String,
        uid: String
    ): Flow<T> = flow.catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
        }
        throw error
    }

    // --- User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        require(uid == userId) { "Refusing cross-user profile read." }
        val path = "users/$uid"
        emitAll(
            wrapSnapshotError(
                db.collection("users").document(uid)
                    .snapshots()
                    .map { snapshot ->
                        if (!snapshot.exists()) null
                        else snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    },
                path,
                uid
            )
        )
    }

    suspend fun ensureUserProfile(
        user: FirebaseUser,
        preferredLanguage: AppLanguage,
        pinEnabled: Boolean
    ): Result<Unit> {
        val path = "users/${user.uid}"
        return try {
            val uid = requireUserId()
            check(uid == user.uid) { "Authenticated user mismatch." }
            val docRef = db.collection("users").document(uid)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val displayName = (user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")
                    ?: "Dressy User").take(80)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName,
                    "email" to (user.email ?: "").take(120),
                    "photoUrl" to user.photoUrl?.toString()?.take(500),
                    "preferredLanguage" to preferredLanguage.code,
                    "pinEnabled" to pinEnabled,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateUserPreferences(
        preferredLanguage: AppLanguage? = null,
        pinEnabled: Boolean? = null,
        activeCompanyId: String? = null
    ): Result<Unit> {
        var path = "users/unknown"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (preferredLanguage != null) updates["preferredLanguage"] = preferredLanguage.code
            if (pinEnabled != null) updates["pinEnabled"] = pinEnabled
            if (!activeCompanyId.isNullOrBlank()) updates["activeCompanyId"] = activeCompanyId.take(128)
            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Companies (Multi-Tenant) ---

    fun observeMyCompanies(userId: String): Flow<List<Company>> = flow {
        val uid = requireUserId()
        require(uid == userId) { "Refusing cross-user company query." }
        val path = "companies"
        emitAll(
            wrapSnapshotError(
                db.collection("companies")
                    .whereArrayContains("memberUids", uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Company::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt }
                    },
                path,
                uid
            )
        )
    }

    suspend fun createCompany(
        name: String,
        ownerName: String,
        phone: String,
        email: String,
        address: String,
        category: BusinessCategory,
        description: String?,
        logoTag: String?,
        actorName: String
    ): Result<String> {
        val companyId = generateShortId("comp")
        val path = "companies/$companyId"
        return try {
            val uid = requireUserId()
            require(name.trim().length in 2..100) { "Company name must be 2–100 characters." }
            require(ownerName.trim().length in 2..80) { "Owner name must be 2–80 characters." }
            require(phone.trim().length in 5..30) { "Phone number must be 5–30 characters." }
            require(email.trim().length in 3..120) { "Email must be 3–120 characters." }
            require(address.trim().length in 2..250) { "Address must be 2–250 characters." }

            val compRef = db.collection("companies").document(companyId)
            val memberRef = compRef.collection("members").document(uid)
            val notifRef = compRef.collection("notifications").document(generateShortId("ntf"))
            val auditRef = compRef.collection("audit_logs").document(generateShortId("log"))
            val userRef = db.collection("users").document(uid)

            val companyCode = generateCompanyCode()
            val cleanDescription = description?.trim()?.takeIf { it.isNotEmpty() }?.take(400)
            val cleanLogoTag = logoTag?.trim()?.takeIf { it.isNotEmpty() }?.take(100)

            val companyPayload = mapOf(
                "companyCode" to companyCode,
                "name" to name.trim(),
                "ownerId" to uid,
                "ownerName" to ownerName.trim(),
                "phone" to phone.trim(),
                "email" to email.trim(),
                "address" to address.trim(),
                "category" to category.key,
                "country" to "Bangladesh",
                "currency" to "BDT",
                "timezone" to "Asia/Dhaka",
                "description" to cleanDescription,
                "logoTag" to cleanLogoTag,
                "memberUids" to listOf(uid),
                "subscriptionTier" to "FREE",
                "subscriptionPlan" to SubscriptionPlan.FREE.key,
                "subscriptionStatus" to "ACTIVE",
                "paymentNumber" to SubscriptionPlan.PAYMENT_NUMBER,
                "employeeCount" to 1L,
                "karigorCount" to 0L,
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            val memberPayload = mapOf(
                "companyId" to companyId,
                "userId" to uid,
                "displayName" to ownerName.trim(),
                "email" to email.trim(),
                "phone" to phone.trim(),
                "role" to UserRole.COMPANY_OWNER.key,
                "staffType" to StaffType.OWNER.key,
                "status" to "ACTIVE",
                "memberUids" to listOf(uid),
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val notifPayload = mapOf(
                "companyId" to companyId,
                "memberUids" to listOf(uid),
                "title" to "Workspace Initialized",
                "message" to "${name.trim()} ($companyCode) is active with multi-tenant isolation.",
                "category" to "COMPANY",
                "readFlag" to false,
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val auditPayload = mapOf(
                "companyId" to companyId,
                "memberUids" to listOf(uid),
                "actorUid" to uid,
                "actorName" to actorName.trim().ifBlank { ownerName.trim() }.take(80),
                "action" to "COMPANY_CREATED",
                "details" to "Created ${name.trim()} ($companyCode) in Bangladesh (BDT)",
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val batch = db.batch()
            batch.set(compRef, companyPayload)
            batch.set(memberRef, memberPayload)
            batch.set(notifRef, notifPayload)
            batch.set(auditRef, auditPayload)
            batch.update(userRef, mapOf("activeCompanyId" to companyId, "updatedAt" to FieldValue.serverTimestamp()))
            batch.commit().await()

            Result.success(companyId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateCompanyDetails(
        companyId: String,
        name: String,
        ownerName: String,
        phone: String,
        email: String,
        address: String,
        category: BusinessCategory,
        description: String?
    ): Result<Unit> {
        val path = "companies/$companyId"
        return try {
            requireUserId()
            require(name.trim().length in 2..100) { "Company name must be 2–100 characters." }
            val updates = mutableMapOf<String, Any>(
                "name" to name.trim(),
                "ownerName" to ownerName.trim().take(80),
                "phone" to phone.trim().take(30),
                "email" to email.trim().take(120),
                "address" to address.trim().take(250),
                "category" to category.key,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val desc = description?.trim()
            if (!desc.isNullOrEmpty()) {
                updates["description"] = desc.take(400)
            }
            db.collection("companies").document(companyId).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Members & Karigors ---

    fun observeMembers(companyId: String, userId: String): Flow<List<CompanyMember>> = flow {
        val uid = requireUserId()
        require(uid == userId)
        val path = "companies/$companyId/members"
        emitAll(
            wrapSnapshotError(
                db.collection("companies").document(companyId).collection("members")
                    .whereArrayContains("memberUids", uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(CompanyMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedBy { it.displayName.lowercase() }
                    },
                path,
                uid
            )
        )
    }

    suspend fun addTeamMemberOrKarigor(
        company: Company,
        memberUidInput: String?,
        displayName: String,
        email: String?,
        phone: String?,
        role: UserRole,
        staffType: StaffType,
        actorName: String
    ): Result<Unit> {
        val memberDocId = memberUidInput?.trim()?.takeIf { it.matches(Regex("^[a-zA-Z0-9_\\-]+$")) }
            ?: generateShortId("staff")
        val path = "companies/${company.id}/members/$memberDocId"
        return try {
            val uid = requireUserId()
            require(role != UserRole.SUPER_ADMIN && role != UserRole.COMPANY_OWNER) {
                "Cannot assign restricted owner or super admin roles here."
            }
            require(displayName.trim().length in 1..80) { "Member name must be 1–80 characters." }

            val updatedMemberUids = if (memberUidInput != null && memberDocId !in company.memberUids && company.memberUids.size < 50) {
                company.memberUids + memberDocId
            } else {
                company.memberUids
            }

            val compRef = db.collection("companies").document(company.id)
            val memberRef = compRef.collection("members").document(memberDocId)
            val auditRef = compRef.collection("audit_logs").document(generateShortId("log"))

            val memberPayload = mapOf(
                "companyId" to company.id,
                "userId" to memberDocId,
                "displayName" to displayName.trim(),
                "email" to email?.trim()?.takeIf { it.isNotEmpty() }?.take(120),
                "phone" to phone?.trim()?.takeIf { it.isNotEmpty() }?.take(30),
                "role" to role.key,
                "staffType" to staffType.key,
                "status" to "ACTIVE",
                "memberUids" to updatedMemberUids,
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            val newEmployeeDelta = if (staffType == StaffType.STAFF) 1L else 0L
            val newKarigorDelta = if (staffType == StaffType.KARIGOR) 1L else 0L

            val batch = db.batch()
            batch.set(memberRef, memberPayload)
            batch.update(
                compRef,
                mapOf(
                    "memberUids" to updatedMemberUids,
                    "employeeCount" to (company.employeeCount + newEmployeeDelta),
                    "karigorCount" to (company.karigorCount + newKarigorDelta),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                auditRef,
                mapOf(
                    "companyId" to company.id,
                    "memberUids" to updatedMemberUids,
                    "actorUid" to uid,
                    "actorName" to actorName.take(80),
                    "action" to "MEMBER_ADDED",
                    "details" to "Added ${displayName.trim()} (${role.labelEn})",
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- Transactions (Financial Ledger) ---

    fun observeTransactions(companyId: String, userId: String): Flow<List<FinancialTransaction>> = flow {
        val uid = requireUserId()
        require(uid == userId)
        val path = "companies/$companyId/transactions"
        emitAll(
            wrapSnapshotError(
                db.collection("companies").document(companyId).collection("transactions")
                    .whereArrayContains("memberUids", uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(FinancialTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt }
                    },
                path,
                uid
            )
        )
    }

    suspend fun addTransaction(
        company: Company,
        financeMemberUids: List<String>,
        entryType: TransactionEntryType,
        category: String,
        title: String,
        partyName: String?,
        amountBdt: Double,
        paymentMethod: PaymentMethod,
        notes: String?,
        actorName: String
    ): Result<Unit> {
        val txId = generateShortId("tx")
        val path = "companies/${company.id}/transactions/$txId"
        return try {
            val uid = requireUserId()
            require(title.trim().length in 1..120) { "Title must be 1–120 characters." }
            require(category.trim().length in 1..60) { "Category must be 1–60 characters." }
            require(amountBdt > 0.0 && amountBdt <= 1_000_000_000.0) { "Amount must be > 0 BDT." }

            val scopedFinanceUids = (financeMemberUids + uid).distinct().take(50)
            val compRef = db.collection("companies").document(company.id)
            val txRef = compRef.collection("transactions").document(txId)
            val auditRef = compRef.collection("audit_logs").document(generateShortId("log"))

            val txPayload = mapOf(
                "companyId" to company.id,
                "memberUids" to scopedFinanceUids,
                "entryType" to entryType.key,
                "category" to category.trim().take(60),
                "title" to title.trim().take(120),
                "partyName" to partyName?.trim()?.takeIf { it.isNotEmpty() }?.take(100),
                "amountBdt" to amountBdt,
                "paymentMethod" to paymentMethod.key,
                "notes" to notes?.trim()?.takeIf { it.isNotEmpty() }?.take(300),
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            val batch = db.batch()
            batch.set(txRef, txPayload)
            batch.set(
                auditRef,
                mapOf(
                    "companyId" to company.id,
                    "memberUids" to company.memberUids,
                    "actorUid" to uid,
                    "actorName" to actorName.take(80),
                    "action" to "TRANSACTION_RECORDED",
                    "details" to "${entryType.labelEn}: ${title.trim()} (BDT ${amountBdt.toLong()})",
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- Production Batches ---

    fun observeProductionOrders(companyId: String, userId: String): Flow<List<ProductionOrder>> = flow {
        val uid = requireUserId()
        require(uid == userId)
        val path = "companies/$companyId/production_orders"
        emitAll(
            wrapSnapshotError(
                db.collection("companies").document(companyId).collection("production_orders")
                    .whereArrayContains("memberUids", uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(ProductionOrder::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt }
                    },
                path,
                uid
            )
        )
    }

    suspend fun addProductionOrder(
        company: Company,
        productName: String,
        productType: ProductionType,
        targetUnits: Long,
        assignedKarigors: Long,
        dueLabel: String?,
        actorName: String
    ): Result<Unit> {
        val orderId = generateShortId("prd")
        val path = "companies/${company.id}/production_orders/$orderId"
        return try {
            val uid = requireUserId()
            require(productName.trim().length in 1..120) { "Product name must be 1–120 characters." }
            require(targetUnits in 1L..1_000_000L) { "Target units must be at least 1." }
            require(assignedKarigors in 0L..10_000L) { "Invalid karigor count." }

            val batchCode = "DE-PRD-${UUID.randomUUID().toString().take(4).uppercase()}"
            val compRef = db.collection("companies").document(company.id)
            val orderRef = compRef.collection("production_orders").document(orderId)
            val auditRef = compRef.collection("audit_logs").document(generateShortId("log"))

            val payload = mapOf(
                "companyId" to company.id,
                "memberUids" to company.memberUids,
                "batchCode" to batchCode,
                "productName" to productName.trim().take(120),
                "productType" to productType.key,
                "targetUnits" to targetUnits,
                "completedUnits" to 0L,
                "assignedKarigors" to assignedKarigors,
                "status" to ProductionStatus.IN_PROGRESS.key,
                "dueLabel" to dueLabel?.trim()?.takeIf { it.isNotEmpty() }?.take(40),
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            val batch = db.batch()
            batch.set(orderRef, payload)
            batch.set(
                auditRef,
                mapOf(
                    "companyId" to company.id,
                    "memberUids" to company.memberUids,
                    "actorUid" to uid,
                    "actorName" to actorName.take(80),
                    "action" to "PRODUCTION_BATCH_ADDED",
                    "details" to "$batchCode • ${productName.trim()} ($targetUnits pcs)",
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProductionProgress(
        companyId: String,
        order: ProductionOrder,
        completedUnits: Long,
        status: ProductionStatus
    ): Result<Unit> {
        val path = "companies/$companyId/production_orders/${order.id}"
        return try {
            requireUserId()
            val clamped = completedUnits.coerceIn(0L, order.targetUnits)
            db.collection("companies").document(companyId)
                .collection("production_orders").document(order.id)
                .update(
                    mapOf(
                        "completedUnits" to clamped,
                        "status" to status.key,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Notifications & Audit Logs ---

    fun observeNotifications(companyId: String, userId: String): Flow<List<TenantNotification>> = flow {
        val uid = requireUserId()
        require(uid == userId)
        val path = "companies/$companyId/notifications"
        emitAll(
            wrapSnapshotError(
                db.collection("companies").document(companyId).collection("notifications")
                    .whereArrayContains("memberUids", uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(TenantNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt }
                    },
                path,
                uid
            )
        )
    }

    suspend fun markNotificationRead(companyId: String, notificationId: String): Result<Unit> {
        val path = "companies/$companyId/notifications/$notificationId"
        return try {
            requireUserId()
            db.collection("companies").document(companyId)
                .collection("notifications").document(notificationId)
                .update(mapOf("readFlag" to true, "updatedAt" to FieldValue.serverTimestamp()))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeAuditLogs(companyId: String, userId: String): Flow<List<AuditLog>> = flow {
        val uid = requireUserId()
        require(uid == userId)
        val path = "companies/$companyId/audit_logs"
        emitAll(
            wrapSnapshotError(
                db.collection("companies").document(companyId).collection("audit_logs")
                    .whereArrayContains("memberUids", uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(AuditLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt }
                    },
                path,
                uid
            )
        )
    }

    suspend fun logAuditEvent(
        company: Company,
        actorName: String,
        action: String,
        details: String
    ): Result<Unit> {
        val logId = generateShortId("log")
        val path = "companies/${company.id}/audit_logs/$logId"
        return try {
            val uid = requireUserId()
            db.collection("companies").document(company.id)
                .collection("audit_logs").document(logId)
                .set(
                    mapOf(
                        "companyId" to company.id,
                        "memberUids" to company.memberUids,
                        "actorUid" to uid,
                        "actorName" to actorName.take(80),
                        "action" to action.take(60),
                        "details" to details.take(300),
                        "createdBy" to uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- Subscription Payment Request (Server-Verification Architecture) ---

    fun observePaymentRequests(companyId: String, userId: String): Flow<List<PaymentRequest>> = flow {
        val uid = requireUserId()
        require(uid == userId)
        val path = "companies/$companyId/payment_requests"
        emitAll(
            wrapSnapshotError(
                db.collection("companies").document(companyId).collection("payment_requests")
                    .whereArrayContains("memberUids", uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(PaymentRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt }
                    },
                path,
                uid
            )
        )
    }

    suspend fun submitSubscriptionPaymentRequest(
        company: Company,
        plan: SubscriptionPlan,
        senderAccount: String,
        transactionRef: String,
        actorName: String
    ): Result<Unit> {
        val reqId = generateShortId("pay")
        val path = "companies/${company.id}/payment_requests/$reqId"
        return try {
            val uid = requireUserId()
            require(plan != SubscriptionPlan.FREE) { "Choose a paid subscription plan." }
            require(senderAccount.trim().length in 4..40) { "Sender number must be 4–40 characters." }
            require(transactionRef.trim().length in 3..60) { "Transaction reference must be 3–60 characters." }

            val compRef = db.collection("companies").document(company.id)
            val payRef = compRef.collection("payment_requests").document(reqId)
            val notifRef = compRef.collection("notifications").document(generateShortId("ntf"))
            val auditRef = compRef.collection("audit_logs").document(generateShortId("log"))

            val batch = db.batch()
            batch.set(
                payRef,
                mapOf(
                    "companyId" to company.id,
                    "memberUids" to company.memberUids,
                    "plan" to plan.key,
                    "amountBdt" to plan.priceBdt,
                    "paymentNumber" to SubscriptionPlan.PAYMENT_NUMBER,
                    "senderAccount" to senderAccount.trim(),
                    "transactionRef" to transactionRef.trim(),
                    "status" to "PENDING_VERIFICATION",
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                notifRef,
                mapOf(
                    "companyId" to company.id,
                    "memberUids" to company.memberUids,
                    "title" to "Subscription Request Submitted",
                    "message" to "Plan ${plan.titleEn} (Ref: ${transactionRef.trim()}) queued for server verification.",
                    "category" to "SUBSCRIPTION",
                    "readFlag" to false,
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                auditRef,
                mapOf(
                    "companyId" to company.id,
                    "memberUids" to company.memberUids,
                    "actorUid" to uid,
                    "actorName" to actorName.take(80),
                    "action" to "PAYMENT_REQUEST_SUBMITTED",
                    "details" to "Submitted ${plan.key} (BDT ${plan.priceBdt.toLong()}) to ${SubscriptionPlan.PAYMENT_NUMBER}",
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }
}
