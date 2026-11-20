package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.firebase.ErpRepository
import com.example.firebase.authStateFlow
import com.example.firebase.signOut
import com.example.localization.AppStrings
import com.example.model.AppLanguage
import com.example.security.PinSecurityManager
import com.example.ui.components.CompanySwitcherSheet
import com.example.ui.components.NotificationsSheet
import com.example.ui.screens.AddMemberDialog
import com.example.ui.screens.AddProductionDialog
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.CompanyWizardScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LandingAuthScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.PinSetupScreen
import com.example.ui.screens.ProductionScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TeamScreen
import com.example.ui.state.ErpViewModel
import com.example.ui.state.SecurityGateMode
import com.example.ui.state.UiState
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DressyElegantTheme
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SilkWhite
import com.example.ui.theme.SoftGrayBg
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DressyElegantTheme {
                AppNavigation()
            }
        }
    }
}

private enum class ErpDestination(
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD(Icons.Default.Dashboard, "nav_dashboard"),
    LEDGER(Icons.Default.AccountBalanceWallet, "nav_ledger"),
    PRODUCTION(Icons.Default.Factory, "nav_production"),
    TEAM(Icons.Default.Groups, "nav_team"),
    SETTINGS(Icons.Default.Settings, "nav_settings")
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pinManager = remember(context) { PinSecurityManager(context.applicationContext) }
    val language by pinManager.languageFlow.collectAsStateWithLifecycle(initialValue = AppLanguage.BENGALI)
    val currentUser by Firebase.auth.authStateFlow().collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    val user = currentUser
    if (user == null) {
        LandingAuthScreen(
            language = language,
            onLanguageChange = { lang ->
                scope.launch {
                    pinManager.setLanguage(lang)
                }
            },
            onAuthSuccess = {}
        )
    } else {
        key(user.uid) {
            AuthenticatedErpRoot(
                user = user,
                pinManager = pinManager
            )
        }
    }
}

@Composable
private fun AuthenticatedErpRoot(
    user: FirebaseUser,
    pinManager: PinSecurityManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: ErpViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ErpViewModel(
                    repository = ErpRepository(db),
                    pinManager = pinManager,
                    currentUserId = user.uid,
                    firebaseUser = user
                )
            }
        }
    )

    val language by viewModel.language.collectAsStateWithLifecycle()
    val copy = AppStrings.get(language)
    val securityGate by viewModel.securityGateMode.collectAsStateWithLifecycle()
    val pinState by viewModel.pinAccountState.collectAsStateWithLifecycle()
    val companiesState by viewModel.companiesState.collectAsStateWithLifecycle()
    val activeCompany by viewModel.activeCompany.collectAsStateWithLifecycle()
    val role by viewModel.currentMemberRole.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val kpis by viewModel.dashboardKpis.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val membersState by viewModel.membersState.collectAsStateWithLifecycle()
    val productionState by viewModel.productionOrdersState.collectAsStateWithLifecycle()
    val notificationsState by viewModel.notificationsState.collectAsStateWithLifecycle()
    val auditLogsState by viewModel.auditLogsState.collectAsStateWithLifecycle()
    val paymentRequestsState by viewModel.paymentRequestsState.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    var showCreateCompanyWizard by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(ErpDestination.DASHBOARD) }
    var showSwitcherSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddProductionDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    // 1. Security PIN Gate (Setup Prompt on first login / Lock Screen on launch)
    when (securityGate) {
        SecurityGateMode.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBlack),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ChampagneGold)
            }
            return
        }
        SecurityGateMode.PIN_SETUP_PROMPT -> {
            PinSetupScreen(
                language = language,
                allowSkip = true,
                onSavePin = { pin -> viewModel.setupSecurityPin(pin) },
                onSkip = { viewModel.skipPinSetup() }
            )
            return
        }
        SecurityGateMode.PIN_LOCKED -> {
            PinLockScreen(
                language = language,
                userEmail = user.email ?: "",
                onVerifyPin = { pin -> viewModel.unlockWithPin(pin) },
                onResetViaSignOut = {
                    viewModel.resetSecurityPin(null)
                    signOut(credentialManager, scope)
                },
                onSignOut = { signOut(credentialManager, scope) }
            )
            return
        }
        SecurityGateMode.UNLOCKED -> Unit
    }

    // 2. Company Presence Check -> Company Registration Wizard
    when (val compUi = companiesState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SoftGrayBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ObsidianBlack)
            }
            return
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SoftGrayBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(compUi.message)
            }
            return
        }
        is UiState.Success -> {
            val companies = compUi.data
            if (companies.isEmpty() || showCreateCompanyWizard) {
                CompanyWizardScreen(
                    language = language,
                    defaultOwnerName = user.displayName ?: "",
                    defaultEmail = user.email ?: "",
                    canNavigateBack = companies.isNotEmpty(),
                    isBusy = isBusy,
                    onBack = { showCreateCompanyWizard = false },
                    onCreateCompany = { name, owner, phone, email, address, cat, desc, logoTag ->
                        viewModel.createCompany(name, owner, phone, email, address, cat, desc, logoTag) {
                            showCreateCompanyWizard = false
                            currentTab = ErpDestination.DASHBOARD
                        }
                    }
                )
                return
            }
        }
    }

    val company = activeCompany ?: return
    val members = (membersState as? UiState.Success)?.data.orEmpty()
    val productionOrders = (productionState as? UiState.Success)?.data.orEmpty()
    val notifications = (notificationsState as? UiState.Success)?.data.orEmpty()
    val auditLogs = (auditLogsState as? UiState.Success)?.data.orEmpty()
    val paymentRequests = (paymentRequestsState as? UiState.Success)?.data.orEmpty()
    val companies = (companiesState as? UiState.Success)?.data.orEmpty()

    fun tabLabel(dest: ErpDestination): String = when (dest) {
        ErpDestination.DASHBOARD -> copy.navDashboard
        ErpDestination.LEDGER -> copy.navLedger
        ErpDestination.PRODUCTION -> copy.navProduction
        ErpDestination.TEAM -> copy.navTeam
        ErpDestination.SETTINGS -> copy.navSettings
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp

        Scaffold(
            containerColor = SoftGrayBg,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = ObsidianBlack) {
                        ErpDestination.entries.forEach { dest ->
                            NavigationBarItem(
                                selected = currentTab == dest,
                                onClick = { currentTab = dest },
                                icon = { Icon(dest.icon, contentDescription = tabLabel(dest)) },
                                label = { Text(tabLabel(dest)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ObsidianBlack,
                                    selectedTextColor = ChampagneGold,
                                    indicatorColor = ChampagneGold,
                                    unselectedIconColor = SilkWhite.copy(alpha = 0.7f),
                                    unselectedTextColor = SilkWhite.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = ObsidianBlack,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        ErpDestination.entries.forEach { dest ->
                            NavigationRailItem(
                                selected = currentTab == dest,
                                onClick = { currentTab = dest },
                                icon = { Icon(dest.icon, contentDescription = tabLabel(dest)) },
                                label = { Text(tabLabel(dest)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = ObsidianBlack,
                                    selectedTextColor = ChampagneGold,
                                    indicatorColor = ChampagneGold,
                                    unselectedIconColor = SilkWhite.copy(alpha = 0.7f),
                                    unselectedTextColor = SilkWhite.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (currentTab) {
                        ErpDestination.DASHBOARD -> DashboardScreen(
                            company = company,
                            role = role,
                            language = language,
                            dateFilter = dateFilter,
                            kpis = kpis,
                            transactions = transactions,
                            productionOrders = productionOrders,
                            auditLogs = auditLogs,
                            notifications = notifications,
                            onLanguageChange = viewModel::toggleLanguage,
                            onDateFilterChange = viewModel::setDateFilter,
                            onOpenCompanySwitcher = { showSwitcherSheet = true },
                            onOpenNotifications = { showNotificationsSheet = true },
                            onQuickAddTransaction = { showAddTransactionDialog = true },
                            onQuickAddProduction = { showAddProductionDialog = true },
                            onQuickAddMember = { showAddMemberDialog = true },
                            onOpenSubscription = { currentTab = ErpDestination.TEAM }
                        )
                        ErpDestination.LEDGER -> LedgerScreen(
                            role = role,
                            language = language,
                            transactions = transactions,
                            onOpenAddTransaction = { showAddTransactionDialog = true }
                        )
                        ErpDestination.PRODUCTION -> ProductionScreen(
                            role = role,
                            language = language,
                            orders = productionOrders,
                            onOpenAddProduction = { showAddProductionDialog = true },
                            onUpdateProgress = viewModel::updateProductionProgress
                        )
                        ErpDestination.TEAM -> TeamScreen(
                            company = company,
                            role = role,
                            language = language,
                            members = members,
                            paymentRequests = paymentRequests,
                            onOpenAddMember = { showAddMemberDialog = true },
                            onSubmitPayment = { plan, sender, ref ->
                                viewModel.submitSubscriptionPayment(plan, sender, ref) {}
                            }
                        )
                        ErpDestination.SETTINGS -> SettingsScreen(
                            user = user,
                            company = company,
                            role = role,
                            language = language,
                            pinState = pinState,
                            auditLogs = auditLogs,
                            onLanguageChange = viewModel::toggleLanguage,
                            onOpenPinSetup = viewModel::requestOpenPinSetup,
                            onTogglePinLock = viewModel::togglePinOnLaunch,
                            onVerifyAndChangePin = viewModel::changeSecurityPin,
                            onResetPin = viewModel::resetSecurityPin,
                            onCreateNewCompany = { showCreateCompanyWizard = true },
                            onSignOut = { signOut(credentialManager, scope) }
                        )
                    }
                }
            }
        }
    }

    if (showSwitcherSheet) {
        CompanySwitcherSheet(
            companies = companies,
            activeCompanyId = company.id,
            language = language,
            copy = copy,
            onSelectCompany = viewModel::switchCompany,
            onCreateNewCompany = { showCreateCompanyWizard = true },
            onDismiss = { showSwitcherSheet = false }
        )
    }

    if (showNotificationsSheet) {
        NotificationsSheet(
            notifications = notifications,
            language = language,
            copy = copy,
            onMarkRead = viewModel::markNotificationRead,
            onDismiss = { showNotificationsSheet = false }
        )
    }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            language = language,
            onSave = { entryType, category, title, partyName, amountBdt, paymentMethod, notes ->
                viewModel.addTransaction(entryType, category, title, partyName, amountBdt, paymentMethod, notes) {
                    showAddTransactionDialog = false
                }
            },
            onDismiss = { showAddTransactionDialog = false }
        )
    }

    if (showAddProductionDialog) {
        AddProductionDialog(
            language = language,
            onSave = { productName, productType, targetUnits, assignedKarigors, dueLabel ->
                viewModel.addProductionBatch(productName, productType, targetUnits, assignedKarigors, dueLabel) {
                    showAddProductionDialog = false
                }
            },
            onDismiss = { showAddProductionDialog = false }
        )
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            language = language,
            onSave = { displayName, email, phone, memberRole, staffType, memberUidInput ->
                viewModel.addMemberOrKarigor(displayName, email, phone, memberRole, staffType, memberUidInput) {
                    showAddMemberDialog = false
                }
            },
            onDismiss = { showAddMemberDialog = false }
        )
    }
}
