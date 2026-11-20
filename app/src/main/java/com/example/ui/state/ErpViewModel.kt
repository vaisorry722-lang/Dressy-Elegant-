package com.example.ui.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebase.ErpRepository
import com.example.model.AppLanguage
import com.example.model.AuditLog
import com.example.model.BusinessCategory
import com.example.model.Company
import com.example.model.CompanyMember
import com.example.model.DashboardKpis
import com.example.model.DateFilterRange
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
import com.example.security.PinAccountState
import com.example.security.PinSecurityManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class SecurityGateMode {
    LOADING,
    PIN_SETUP_PROMPT,
    PIN_LOCKED,
    UNLOCKED
}

@OptIn(ExperimentalCoroutinesApi::class)
class ErpViewModel(
    private val repository: ErpRepository,
    private val pinManager: PinSecurityManager,
    private val currentUserId: String,
    private val firebaseUser: FirebaseUser
) : ViewModel() {

    private val _sessionPinUnlocked = MutableStateFlow(false)
    private val _forcePinSetupScreen = MutableStateFlow(false)
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _dateFilter = MutableStateFlow(DateFilterRange.THIRTY_DAYS)
    val dateFilter: StateFlow<DateFilterRange> = _dateFilter.asStateFlow()

    val language: StateFlow<AppLanguage> = pinManager.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AppLanguage.BENGALI)

    val pinAccountState: StateFlow<PinAccountState?> = pinManager.observePinState(currentUserId)
        .map<PinAccountState, PinAccountState?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val securityGateMode: StateFlow<SecurityGateMode> = combine(
        pinAccountState,
        _sessionPinUnlocked,
        _forcePinSetupScreen
    ) { state, unlocked, forceSetup ->
        when {
            state == null -> SecurityGateMode.LOADING
            forceSetup -> SecurityGateMode.PIN_SETUP_PROMPT
            state.requirePinOnLaunch && !unlocked -> SecurityGateMode.PIN_LOCKED
            !state.hasPinConfigured && !state.setupPromptDismissed && !unlocked -> SecurityGateMode.PIN_SETUP_PROMPT
            else -> SecurityGateMode.UNLOCKED
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SecurityGateMode.LOADING)

    val userProfileState: StateFlow<UiState<UserProfile?>> =
        safeStateFlow(repository.observeUserProfile(currentUserId), "userProfile")

    val companiesState: StateFlow<UiState<List<Company>>> =
        safeStateFlow(repository.observeMyCompanies(currentUserId), "companies")

    private val localActiveCompanyId = pinManager.observeActiveCompanyId(currentUserId)

    val activeCompany: StateFlow<Company?> = combine(
        companiesState,
        localActiveCompanyId,
        userProfileState
    ) { companiesUi, localId, profileUi ->
        val list = (companiesUi as? UiState.Success)?.data ?: return@combine null
        if (list.isEmpty()) return@combine null
        val preferredId = localId ?: (profileUi as? UiState.Success)?.data?.activeCompanyId
        list.firstOrNull { it.id == preferredId } ?: list.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val activeCompanyIdFlow = activeCompany.map { it?.id }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000L), null
    )

    val membersState: StateFlow<UiState<List<CompanyMember>>> = activeCompanyIdFlow
        .flatMapLatest { companyId ->
            if (companyId.isNullOrBlank()) flowOf(UiState.Success(emptyList()))
            else toUiStateFlow(repository.observeMembers(companyId, currentUserId), "members")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val currentMemberRole: StateFlow<UserRole> = membersState.map { state ->
        val members = (state as? UiState.Success)?.data.orEmpty()
        val me = members.firstOrNull { it.userId == currentUserId }
        UserRole.fromKey(me?.role)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserRole.COMPANY_OWNER)

    val transactionsState: StateFlow<UiState<List<FinancialTransaction>>> = combine(
        activeCompanyIdFlow,
        currentMemberRole
    ) { companyId, role -> companyId to role }
        .flatMapLatest { (companyId, role) ->
            if (companyId.isNullOrBlank() || !role.has(Permission.VIEW_FINANCES)) {
                flowOf(UiState.Success(emptyList()))
            } else {
                toUiStateFlow(repository.observeTransactions(companyId, currentUserId), "transactions")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val productionOrdersState: StateFlow<UiState<List<ProductionOrder>>> = activeCompanyIdFlow
        .flatMapLatest { companyId ->
            if (companyId.isNullOrBlank()) flowOf(UiState.Success(emptyList()))
            else toUiStateFlow(repository.observeProductionOrders(companyId, currentUserId), "production")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val notificationsState: StateFlow<UiState<List<TenantNotification>>> = activeCompanyIdFlow
        .flatMapLatest { companyId ->
            if (companyId.isNullOrBlank()) flowOf(UiState.Success(emptyList()))
            else toUiStateFlow(repository.observeNotifications(companyId, currentUserId), "notifications")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val auditLogsState: StateFlow<UiState<List<AuditLog>>> = activeCompanyIdFlow
        .flatMapLatest { companyId ->
            if (companyId.isNullOrBlank()) flowOf(UiState.Success(emptyList()))
            else toUiStateFlow(repository.observeAuditLogs(companyId, currentUserId), "audit_logs")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val paymentRequestsState: StateFlow<UiState<List<PaymentRequest>>> = activeCompanyIdFlow
        .flatMapLatest { companyId ->
            if (companyId.isNullOrBlank()) flowOf(UiState.Success(emptyList()))
            else toUiStateFlow(repository.observePaymentRequests(companyId, currentUserId), "payment_requests")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val filteredTransactions: StateFlow<List<FinancialTransaction>> = combine(
        transactionsState,
        dateFilter
    ) { txUi, range ->
        val all = (txUi as? UiState.Success)?.data.orEmpty()
        val days = range.days ?: return@combine all
        val cutoffMillis = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L
        all.filter { tx ->
            val ts = tx.createdAt?.toDate()?.time ?: System.currentTimeMillis()
            ts >= cutoffMillis
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val dashboardKpis: StateFlow<DashboardKpis> = combine(
        filteredTransactions,
        membersState,
        productionOrdersState
    ) { txs, membersUi, prodUi ->
        var income = 0.0
        var expense = 0.0
        var cash = 0.0
        var bank = 0.0
        var customerDue = 0.0
        var supplierDue = 0.0

        for (tx in txs) {
            when (TransactionEntryType.fromKey(tx.entryType)) {
                TransactionEntryType.INCOME -> {
                    income += tx.amountBdt
                    when (PaymentMethod.fromKey(tx.paymentMethod)) {
                        PaymentMethod.CASH -> cash += tx.amountBdt
                        PaymentMethod.BANK, PaymentMethod.BKASH, PaymentMethod.NAGAD -> bank += tx.amountBdt
                        PaymentMethod.DUE -> customerDue += tx.amountBdt
                    }
                }
                TransactionEntryType.EXPENSE -> {
                    expense += tx.amountBdt
                    when (PaymentMethod.fromKey(tx.paymentMethod)) {
                        PaymentMethod.CASH -> cash -= tx.amountBdt
                        PaymentMethod.BANK, PaymentMethod.BKASH, PaymentMethod.NAGAD -> bank -= tx.amountBdt
                        PaymentMethod.DUE -> supplierDue += tx.amountBdt
                    }
                }
                TransactionEntryType.CASH_IN -> cash += tx.amountBdt
                TransactionEntryType.CASH_OUT -> cash -= tx.amountBdt
                TransactionEntryType.BANK_DEPOSIT -> bank += tx.amountBdt
                TransactionEntryType.BANK_WITHDRAW -> bank -= tx.amountBdt
                TransactionEntryType.CUSTOMER_DUE -> customerDue += tx.amountBdt
                TransactionEntryType.SUPPLIER_DUE -> supplierDue += tx.amountBdt
            }
        }

        val members = (membersUi as? UiState.Success)?.data.orEmpty()
        val employeesCount = members.count { it.staffType != StaffType.KARIGOR.key }
        val karigorsCount = members.count { it.staffType == StaffType.KARIGOR.key }

        val orders = (prodUi as? UiState.Success)?.data.orEmpty()
        val targetTotal = orders.sumOf { it.targetUnits.toInt() }
        val completedTotal = orders.sumOf { it.completedUnits.toInt() }
        val activeCount = orders.count { it.status != ProductionStatus.COMPLETED.key }

        DashboardKpis(
            totalIncome = income,
            totalExpenses = expense,
            totalProfit = income - expense,
            cashBalance = cash,
            bankBalance = bank,
            customerDue = customerDue,
            supplierDue = supplierDue,
            totalEmployees = employeesCount,
            totalKarigors = karigorsCount,
            totalProductionTarget = targetTotal,
            totalProductionCompleted = completedTotal,
            activeOrdersCount = activeCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DashboardKpis())

    init {
        viewModelScope.launch {
            val pinState = pinManager.getPinState(currentUserId)
            val lang = language.value
            repository.ensureUserProfile(firebaseUser, lang, pinState.requirePinOnLaunch)
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun setDateFilter(range: DateFilterRange) {
        _dateFilter.value = range
    }

    fun toggleLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            pinManager.setLanguage(lang)
            repository.updateUserPreferences(preferredLanguage = lang)
        }
    }

    fun switchCompany(companyId: String) {
        viewModelScope.launch {
            pinManager.setActiveCompanyId(currentUserId, companyId)
            repository.updateUserPreferences(activeCompanyId = companyId)
        }
    }

    // --- Security PIN Actions ---

    fun requestOpenPinSetup() {
        _forcePinSetupScreen.value = true
    }

    fun setupSecurityPin(pin: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val ok = pinManager.setPin(currentUserId, pin)
            if (ok) {
                _forcePinSetupScreen.value = false
                _sessionPinUnlocked.value = true
                repository.updateUserPreferences(pinEnabled = true)
                activeCompany.value?.let { comp ->
                    repository.logAuditEvent(
                        company = comp,
                        actorName = firebaseUser.displayName ?: "Owner",
                        action = "SECURITY_PIN_SET",
                        details = "Security PIN enabled for account login protection"
                    )
                }
                _actionMessage.value = if (language.value == AppLanguage.BENGALI) {
                    "সিকিউরিটি পিন সফলভাবে সেট করা হয়েছে"
                } else {
                    "Security PIN saved successfully"
                }
                onComplete()
            }
        }
    }

    fun skipPinSetup() {
        viewModelScope.launch {
            pinManager.dismissSetupPrompt(currentUserId)
            _forcePinSetupScreen.value = false
            _sessionPinUnlocked.value = true
        }
    }

    suspend fun unlockWithPin(pin: String): Boolean {
        val valid = pinManager.verifyPin(currentUserId, pin)
        if (valid) {
            _sessionPinUnlocked.value = true
        }
        return valid
    }

    suspend fun changeSecurityPin(currentPin: String, newPin: String): Boolean {
        val verified = pinManager.verifyPin(currentUserId, currentPin)
        if (!verified) return false
        val saved = pinManager.setPin(currentUserId, newPin)
        if (saved) {
            activeCompany.value?.let { comp ->
                repository.logAuditEvent(
                    company = comp,
                    actorName = firebaseUser.displayName ?: "Owner",
                    action = "SECURITY_PIN_CHANGED",
                    details = "User changed their Security PIN from Settings"
                )
            }
            _actionMessage.value = if (language.value == AppLanguage.BENGALI) {
                "সিকিউরিটি পিন পরিবর্তন হয়েছে"
            } else {
                "Security PIN updated"
            }
        }
        return saved
    }

    fun resetSecurityPin(newPin: String? = null) {
        viewModelScope.launch {
            if (newPin != null && PinSecurityManager.isValidPinFormat(newPin)) {
                pinManager.setPin(currentUserId, newPin)
                repository.updateUserPreferences(pinEnabled = true)
                _sessionPinUnlocked.value = true
                activeCompany.value?.let { comp ->
                    repository.logAuditEvent(
                        company = comp,
                        actorName = firebaseUser.displayName ?: "Owner",
                        action = "SECURITY_PIN_RESET",
                        details = "User reset Security PIN"
                    )
                }
                _actionMessage.value = if (language.value == AppLanguage.BENGALI) {
                    "সিকিউরিটি পিন রিসেট হয়েছে"
                } else {
                    "Security PIN has been reset"
                }
            } else {
                pinManager.resetPin(currentUserId)
                repository.updateUserPreferences(pinEnabled = false)
                _sessionPinUnlocked.value = true
                _actionMessage.value = if (language.value == AppLanguage.BENGALI) {
                    "সিকিউরিটি পিন মুছে ফেলা হয়েছে"
                } else {
                    "Security PIN cleared"
                }
            }
        }
    }

    fun togglePinOnLaunch(enabled: Boolean) {
        viewModelScope.launch {
            pinManager.setPinLockEnabled(currentUserId, enabled)
            repository.updateUserPreferences(pinEnabled = enabled)
        }
    }

    // --- Company & ERP Mutations ---

    fun createCompany(
        name: String,
        ownerName: String,
        phone: String,
        email: String,
        address: String,
        category: BusinessCategory,
        description: String?,
        logoTag: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.createCompany(
                name = name,
                ownerName = ownerName,
                phone = phone,
                email = email,
                address = address,
                category = category,
                description = description,
                logoTag = logoTag,
                actorName = firebaseUser.displayName ?: ownerName
            )
            _isBusy.value = false
            result.onSuccess { newId ->
                pinManager.setActiveCompanyId(currentUserId, newId)
                _actionMessage.value = if (language.value == AppLanguage.BENGALI) {
                    "কোম্পানি সফলভাবে তৈরি হয়েছে"
                } else {
                    "Company workspace created"
                }
                onSuccess()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to create company"
            }
        }
    }

    fun addTransaction(
        entryType: TransactionEntryType,
        category: String,
        title: String,
        partyName: String?,
        amountBdt: Double,
        paymentMethod: PaymentMethod,
        notes: String?,
        onSuccess: () -> Unit
    ) {
        val company = activeCompany.value ?: return
        val members = (membersState.value as? UiState.Success)?.data.orEmpty()
        val financeUids = members
            .filter { UserRole.fromKey(it.role).has(Permission.VIEW_FINANCES) }
            .map { it.userId }
            .ifEmpty { listOf(currentUserId) }

        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.addTransaction(
                company = company,
                financeMemberUids = financeUids,
                entryType = entryType,
                category = category,
                title = title,
                partyName = partyName,
                amountBdt = amountBdt,
                paymentMethod = paymentMethod,
                notes = notes,
                actorName = firebaseUser.displayName ?: company.ownerName
            )
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = if (language.value == AppLanguage.BENGALI) "লেনদেন সংরক্ষিত হয়েছে" else "Transaction recorded"
                onSuccess()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to save transaction"
            }
        }
    }

    fun addProductionBatch(
        productName: String,
        productType: ProductionType,
        targetUnits: Long,
        assignedKarigors: Long,
        dueLabel: String?,
        onSuccess: () -> Unit
    ) {
        val company = activeCompany.value ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.addProductionOrder(
                company = company,
                productName = productName,
                productType = productType,
                targetUnits = targetUnits,
                assignedKarigors = assignedKarigors,
                dueLabel = dueLabel,
                actorName = firebaseUser.displayName ?: company.ownerName
            )
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = if (language.value == AppLanguage.BENGALI) "উৎপাদন ব্যাচ যোগ হয়েছে" else "Production batch added"
                onSuccess()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to add batch"
            }
        }
    }

    fun updateProductionProgress(order: ProductionOrder, completedUnits: Long, status: ProductionStatus) {
        val company = activeCompany.value ?: return
        viewModelScope.launch {
            val res = repository.updateProductionProgress(company.id, order, completedUnits, status)
            res.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to update progress"
            }
        }
    }

    fun addMemberOrKarigor(
        displayName: String,
        email: String?,
        phone: String?,
        role: UserRole,
        staffType: StaffType,
        memberUidInput: String?,
        onSuccess: () -> Unit
    ) {
        val company = activeCompany.value ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.addTeamMemberOrKarigor(
                company = company,
                memberUidInput = memberUidInput,
                displayName = displayName,
                email = email,
                phone = phone,
                role = role,
                staffType = staffType,
                actorName = firebaseUser.displayName ?: company.ownerName
            )
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = if (language.value == AppLanguage.BENGALI) "সদস্য যোগ হয়েছে" else "Member added"
                onSuccess()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to add member"
            }
        }
    }

    fun submitSubscriptionPayment(
        plan: SubscriptionPlan,
        senderAccount: String,
        transactionRef: String,
        onSuccess: () -> Unit
    ) {
        val company = activeCompany.value ?: return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.submitSubscriptionPaymentRequest(
                company = company,
                plan = plan,
                senderAccount = senderAccount,
                transactionRef = transactionRef,
                actorName = firebaseUser.displayName ?: company.ownerName
            )
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = if (language.value == AppLanguage.BENGALI) {
                    "পেমেন্ট রেফারেন্স যাচাইয়ের জন্য পাঠানো হয়েছে"
                } else {
                    "Payment reference queued for verification"
                }
                onSuccess()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to submit payment reference"
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        val company = activeCompany.value ?: return
        viewModelScope.launch {
            repository.markNotificationRead(company.id, notificationId)
        }
    }

    private fun <T> safeStateFlow(flow: Flow<T>, label: String): StateFlow<UiState<T>> =
        toUiStateFlow(flow, label).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    private fun <T> toUiStateFlow(flow: Flow<T>, label: String): Flow<UiState<T>> =
        flow.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w("ErpViewModel", "Error observing $label", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load $label"))
            }
}
