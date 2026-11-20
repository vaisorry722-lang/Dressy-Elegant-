package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AppLanguage(val code: String, val label: String) {
    BENGALI("bn", "বাংলা"),
    ENGLISH("en", "EN");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: BENGALI
    }
}

enum class BusinessCategory(val key: String, val labelEn: String, val labelBn: String) {
    GARMENTS_FACTORY("GARMENTS_FACTORY", "Garments Factory", "গার্মেন্টস ফ্যাক্টরি"),
    BURQA_MANUFACTURING("BURQA_MANUFACTURING", "Burqa Manufacturing", "বোরকা উৎপাদন"),
    ABAYA_BUSINESS("ABAYA_BUSINESS", "Abaya Business", "আবায়া ব্যবসা"),
    CLOTHING_COMPANY("CLOTHING_COMPANY", "Clothing Company", "পোশাক কোম্পানি"),
    TAILORING("TAILORING", "Tailoring Business", "টেইলারিং ও দর্জি"),
    WHOLESALE("WHOLESALE", "Wholesale Business", "পাইকারি ব্যবসা"),
    RETAIL("RETAIL", "Retail Business", "খুচরা ব্যবসা"),
    OTHER_SME("OTHER_SME", "Other SME Business", "অন্যান্য এসএমই ব্যবসা");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) labelBn else labelEn

    companion object {
        fun fromKey(raw: String?): BusinessCategory =
            entries.firstOrNull { it.key == raw } ?: GARMENTS_FACTORY
    }
}

enum class Permission {
    VIEW_DASHBOARD,
    VIEW_FINANCES,
    WRITE_FINANCES,
    VIEW_PRODUCTION,
    WRITE_PRODUCTION,
    MANAGE_TEAM,
    MANAGE_COMPANY_SETTINGS,
    MANAGE_SUBSCRIPTION,
    VIEW_AUDIT_LOGS
}

enum class UserRole(
    val key: String,
    val labelEn: String,
    val labelBn: String,
    val isFuturePhasePlaceholder: Boolean = false
) {
    SUPER_ADMIN("SUPER_ADMIN", "Super Admin (Platform Only)", "সুপার অ্যাডমিন (প্ল্যাটফর্ম)", false),
    COMPANY_OWNER("COMPANY_OWNER", "Company Owner", "কোম্পানি মালিক", false),
    COMPANY_ADMIN("COMPANY_ADMIN", "Company Admin", "কোম্পানি অ্যাডমিন", false),
    MANAGER("MANAGER", "Manager", "ম্যানেজার", false),
    ACCOUNTANT("ACCOUNTANT", "Accountant", "হিসাবরক্ষক", false),
    EMPLOYEE("EMPLOYEE", "Employee", "কর্মচারী", false),
    PRODUCTION_MANAGER("PRODUCTION_MANAGER", "Production Manager", "উৎপাদন ম্যানেজার", true),
    INVENTORY_MANAGER("INVENTORY_MANAGER", "Inventory Manager", "ইনভেন্টরি ম্যানেজার", true),
    SALES_MANAGER("SALES_MANAGER", "Sales Manager", "সেলস ম্যানেজার", true),
    KARIGOR("KARIGOR", "Karigor (Artisan)", "কারিগর", true),
    VIEWER("VIEWER", "Viewer", "দর্শক", true);

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) labelBn else labelEn

    fun permissions(): Set<Permission> = when (this) {
        SUPER_ADMIN -> Permission.entries.toSet()
        COMPANY_OWNER -> Permission.entries.toSet()
        COMPANY_ADMIN -> setOf(
            Permission.VIEW_DASHBOARD,
            Permission.VIEW_FINANCES,
            Permission.WRITE_FINANCES,
            Permission.VIEW_PRODUCTION,
            Permission.WRITE_PRODUCTION,
            Permission.MANAGE_TEAM,
            Permission.MANAGE_COMPANY_SETTINGS,
            Permission.MANAGE_SUBSCRIPTION,
            Permission.VIEW_AUDIT_LOGS
        )
        MANAGER -> setOf(
            Permission.VIEW_DASHBOARD,
            Permission.VIEW_FINANCES,
            Permission.WRITE_FINANCES,
            Permission.VIEW_PRODUCTION,
            Permission.WRITE_PRODUCTION,
            Permission.VIEW_AUDIT_LOGS
        )
        ACCOUNTANT -> setOf(
            Permission.VIEW_DASHBOARD,
            Permission.VIEW_FINANCES,
            Permission.WRITE_FINANCES,
            Permission.VIEW_AUDIT_LOGS
        )
        PRODUCTION_MANAGER -> setOf(
            Permission.VIEW_DASHBOARD,
            Permission.VIEW_PRODUCTION,
            Permission.WRITE_PRODUCTION
        )
        INVENTORY_MANAGER, SALES_MANAGER -> setOf(
            Permission.VIEW_DASHBOARD,
            Permission.VIEW_PRODUCTION
        )
        EMPLOYEE, KARIGOR, VIEWER -> setOf(
            Permission.VIEW_DASHBOARD,
            Permission.VIEW_PRODUCTION
        )
    }

    fun has(permission: Permission): Boolean = permission in permissions()

    companion object {
        val assignableRoles: List<UserRole> = listOf(
            COMPANY_ADMIN,
            MANAGER,
            ACCOUNTANT,
            EMPLOYEE,
            PRODUCTION_MANAGER,
            INVENTORY_MANAGER,
            SALES_MANAGER,
            KARIGOR,
            VIEWER
        )

        fun fromKey(raw: String?): UserRole =
            entries.firstOrNull { it.key == raw } ?: EMPLOYEE
    }
}

enum class StaffType(val key: String, val labelEn: String, val labelBn: String) {
    OWNER("OWNER", "Owner", "মালিক"),
    STAFF("STAFF", "Staff / Employee", "কর্মচারী"),
    KARIGOR("KARIGOR", "Karigor", "কারিগর");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) labelBn else labelEn

    companion object {
        fun fromKey(raw: String?): StaffType = entries.firstOrNull { it.key == raw } ?: STAFF
    }
}

enum class TransactionEntryType(val key: String, val labelEn: String, val labelBn: String) {
    INCOME("INCOME", "Income / Sale", "আয় / বিক্রয়"),
    EXPENSE("EXPENSE", "Expense", "ব্যয় / খরচ"),
    CASH_IN("CASH_IN", "Cash In", "নগদ জমা"),
    CASH_OUT("CASH_OUT", "Cash Out", "নগদ উত্তোলন"),
    BANK_DEPOSIT("BANK_DEPOSIT", "Bank Deposit", "ব্যাংক জমা"),
    BANK_WITHDRAW("BANK_WITHDRAW", "Bank Withdraw", "ব্যাংক উত্তোলন"),
    CUSTOMER_DUE("CUSTOMER_DUE", "Customer Due", "কাস্টমার বাকি"),
    SUPPLIER_DUE("SUPPLIER_DUE", "Supplier Due", "সাপ্লায়ার বাকি");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) labelBn else labelEn

    companion object {
        fun fromKey(raw: String?): TransactionEntryType =
            entries.firstOrNull { it.key == raw } ?: INCOME
    }
}

enum class PaymentMethod(val key: String, val labelEn: String, val labelBn: String) {
    CASH("CASH", "Cash", "নগদ"),
    BANK("BANK", "Bank", "ব্যাংক"),
    BKASH("BKASH", "bKash", "বিকাশ"),
    NAGAD("NAGAD", "Nagad", "নগদ (এমএফএস)"),
    DUE("DUE", "Due / Credit", "বাকি");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) labelBn else labelEn

    companion object {
        fun fromKey(raw: String?): PaymentMethod = entries.firstOrNull { it.key == raw } ?: CASH
    }
}

enum class ProductionType(val key: String, val labelEn: String, val labelBn: String) {
    BURQA("BURQA", "Burqa", "বোরকা"),
    ABAYA("ABAYA", "Abaya", "আবায়া"),
    GARMENTS("GARMENTS", "Garments", "গার্মেন্টস"),
    TAILORING("TAILORING", "Tailoring", "টেইলারিং"),
    OTHER("OTHER", "Other", "অন্যান্য");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) labelBn else labelEn

    companion object {
        fun fromKey(raw: String?): ProductionType = entries.firstOrNull { it.key == raw } ?: GARMENTS
    }
}

enum class ProductionStatus(val key: String, val labelEn: String, val labelBn: String) {
    PLANNED("PLANNED", "Planned", "পরিকল্পিত"),
    IN_PROGRESS("IN_PROGRESS", "In Progress", "চলমান"),
    COMPLETED("COMPLETED", "Completed", "সম্পন্ন");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) labelBn else labelEn

    companion object {
        fun fromKey(raw: String?): ProductionStatus = entries.firstOrNull { it.key == raw } ?: PLANNED
    }
}

enum class SubscriptionPlan(
    val key: String,
    val titleEn: String,
    val titleBn: String,
    val priceBdt: Double,
    val periodEn: String,
    val periodBn: String
) {
    FREE("FREE", "Free Starter", "ফ্রি স্টার্টার", 0.0, "Forever", "সর্বদা ফ্রি"),
    MONTHLY("MONTHLY", "Premium Monthly", "প্রিমিয়াম মাসিক", 1490.0, "/ month", "/ মাস"),
    QUARTERLY("QUARTERLY", "Premium Quarterly", "প্রিমিয়াম ত্রৈমাসিক", 3990.0, "/ 3 months", "/ ৩ মাস"),
    YEARLY("YEARLY", "Premium Yearly", "প্রিমিয়াম বাৎসরিক", 13990.0, "/ year", "/ বছর");

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) titleBn else titleEn
    fun period(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) periodBn else periodEn

    companion object {
        const val PAYMENT_NUMBER = "301620556052"
        fun fromKey(raw: String?): SubscriptionPlan = entries.firstOrNull { it.key == raw } ?: FREE
    }
}

enum class DateFilterRange(val labelEn: String, val labelBn: String, val days: Int?) {
    TODAY("Today", "আজ", 1),
    SEVEN_DAYS("7 Days", "৭ দিন", 7),
    THIRTY_DAYS("30 Days", "৩০ দিন", 30),
    ALL("All Time", "সর্বমোট", null);

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.BENGALI) labelBn else labelEn
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val phone: String? = null,
    val preferredLanguage: String = "bn",
    val pinEnabled: Boolean = false,
    val activeCompanyId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Company(
    @DocumentId val id: String = "",
    val companyCode: String = "",
    val name: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val category: String = BusinessCategory.GARMENTS_FACTORY.key,
    val country: String = "Bangladesh",
    val currency: String = "BDT",
    val timezone: String = "Asia/Dhaka",
    val description: String? = null,
    val logoTag: String? = null,
    val memberUids: List<String> = emptyList(),
    val subscriptionTier: String = "FREE",
    val subscriptionPlan: String = "FREE",
    val subscriptionStatus: String = "ACTIVE",
    val paymentNumber: String = SubscriptionPlan.PAYMENT_NUMBER,
    val employeeCount: Long = 1L,
    val karigorCount: Long = 0L,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CompanyMember(
    @DocumentId val id: String = "",
    val companyId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val phone: String? = null,
    val role: String = UserRole.EMPLOYEE.key,
    val staffType: String = StaffType.STAFF.key,
    val status: String = "ACTIVE",
    val memberUids: List<String> = emptyList(),
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FinancialTransaction(
    @DocumentId val id: String = "",
    val companyId: String = "",
    val memberUids: List<String> = emptyList(),
    val entryType: String = TransactionEntryType.INCOME.key,
    val category: String = "",
    val title: String = "",
    val partyName: String? = null,
    val amountBdt: Double = 0.0,
    val paymentMethod: String = PaymentMethod.CASH.key,
    val notes: String? = null,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ProductionOrder(
    @DocumentId val id: String = "",
    val companyId: String = "",
    val memberUids: List<String> = emptyList(),
    val batchCode: String = "",
    val productName: String = "",
    val productType: String = ProductionType.BURQA.key,
    val targetUnits: Long = 1L,
    val completedUnits: Long = 0L,
    val assignedKarigors: Long = 0L,
    val status: String = ProductionStatus.PLANNED.key,
    val dueLabel: String? = null,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TenantNotification(
    @DocumentId val id: String = "",
    val companyId: String = "",
    val memberUids: List<String> = emptyList(),
    val title: String = "",
    val message: String = "",
    val category: String = "SYSTEM",
    val readFlag: Boolean = false,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val companyId: String = "",
    val memberUids: List<String> = emptyList(),
    val actorUid: String = "",
    val actorName: String = "",
    val action: String = "",
    val details: String = "",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PaymentRequest(
    @DocumentId val id: String = "",
    val companyId: String = "",
    val memberUids: List<String> = emptyList(),
    val plan: String = SubscriptionPlan.MONTHLY.key,
    val amountBdt: Double = 0.0,
    val paymentNumber: String = SubscriptionPlan.PAYMENT_NUMBER,
    val senderAccount: String = "",
    val transactionRef: String = "",
    val status: String = "PENDING_VERIFICATION",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class DashboardKpis(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalProfit: Double = 0.0,
    val cashBalance: Double = 0.0,
    val bankBalance: Double = 0.0,
    val customerDue: Double = 0.0,
    val supplierDue: Double = 0.0,
    val totalEmployees: Int = 0,
    val totalKarigors: Int = 0,
    val totalProductionTarget: Int = 0,
    val totalProductionCompleted: Int = 0,
    val activeOrdersCount: Int = 0
)
