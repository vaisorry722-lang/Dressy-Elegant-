package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.localization.BdtFormatter
import com.example.model.AppLanguage
import com.example.model.AuditLog
import com.example.model.BusinessCategory
import com.example.model.Company
import com.example.model.DashboardKpis
import com.example.model.DateFilterRange
import com.example.model.FinancialTransaction
import com.example.model.Permission
import com.example.model.ProductionOrder
import com.example.model.TenantNotification
import com.example.model.TransactionEntryType
import com.example.model.UserRole
import com.example.ui.components.DeMonogramBadge
import com.example.ui.components.FinancialAnalyticsChart
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.LuxuryKpiCard
import com.example.ui.theme.AmberDue
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonExpense
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.IndigoBank
import com.example.ui.theme.IndigoSoft
import com.example.ui.theme.InkBlack
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SilkWhite
import com.example.ui.theme.SoftBorder
import com.example.ui.theme.SoftGoldTint

@Composable
fun DashboardScreen(
    company: Company,
    role: UserRole,
    language: AppLanguage,
    dateFilter: DateFilterRange,
    kpis: DashboardKpis,
    transactions: List<FinancialTransaction>,
    productionOrders: List<ProductionOrder>,
    auditLogs: List<AuditLog>,
    notifications: List<TenantNotification>,
    onLanguageChange: (AppLanguage) -> Unit,
    onDateFilterChange: (DateFilterRange) -> Unit,
    onOpenCompanySwitcher: () -> Unit,
    onOpenNotifications: () -> Unit,
    onQuickAddTransaction: () -> Unit,
    onQuickAddProduction: () -> Unit,
    onQuickAddMember: () -> Unit,
    onOpenSubscription: () -> Unit
) {
    val copy = AppStrings.get(language)
    val canViewFinances = role.has(Permission.VIEW_FINANCES)
    val canWriteFinances = role.has(Permission.WRITE_FINANCES)
    val canWriteProduction = role.has(Permission.WRITE_PRODUCTION)
    val canManageTeam = role.has(Permission.MANAGE_TEAM)
    val unreadCount = notifications.count { !it.readFlag }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Luxury Executive Header
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianBlack,
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.55f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                DeMonogramBadge(size = 42.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = company.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = ChampagneGold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${company.companyCode} • ${BusinessCategory.fromKey(company.category).label(language)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SilkWhite.copy(alpha = 0.78f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LanguageTogglePill(language = language, onToggle = onLanguageChange)
                                IconButton(
                                    onClick = onOpenNotifications,
                                    modifier = Modifier.testTag("dashboard_notifications_btn")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadCount > 0) {
                                                Badge(containerColor = ChampagneGold, contentColor = ObsidianBlack) {
                                                    Text(unreadCount.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = copy.notificationsTitle,
                                            tint = ChampagneGold
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = ChampagneGold.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = role.label(language),
                                        color = ChampagneGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SilkWhite.copy(alpha = 0.1f),
                                    modifier = Modifier.clickable(onClick = onOpenSubscription)
                                ) {
                                    Text(
                                        text = "${company.subscriptionTier} • ${company.currency}",
                                        color = SilkWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            AssistChip(
                                onClick = onOpenCompanySwitcher,
                                label = { Text(copy.switchCompany, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = ChampagneGold,
                                    labelColor = ObsidianBlack,
                                    leadingIconContentColor = ObsidianBlack
                                ),
                                modifier = Modifier.testTag("dashboard_switch_company_chip")
                            )
                        }
                    }
                }
            }

            // Date Filters
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateFilterRange.entries.forEach { range ->
                        val selected = range == dateFilter
                        FilterChip(
                            selected = selected,
                            onClick = { onDateFilterChange(range) },
                            label = { Text(range.label(language)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ObsidianBlack,
                                selectedLabelColor = ChampagneGold
                            ),
                            modifier = Modifier.testTag("date_filter_${range.name.lowercase()}")
                        )
                    }
                }
            }

            // Quick Actions Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(copy.quickActions, style = MaterialTheme.typography.titleMedium, color = InkBlack)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (canWriteFinances) {
                            QuickActionTile(
                                icon = Icons.Default.Add,
                                label = copy.addTransaction,
                                onClick = onQuickAddTransaction,
                                testTag = "quick_add_transaction"
                            )
                        }
                        if (canWriteProduction) {
                            QuickActionTile(
                                icon = Icons.Default.Factory,
                                label = copy.addProduction,
                                onClick = onQuickAddProduction,
                                testTag = "quick_add_production"
                            )
                        }
                        if (canManageTeam) {
                            QuickActionTile(
                                icon = Icons.Default.GroupAdd,
                                label = copy.addMember,
                                onClick = onQuickAddMember,
                                testTag = "quick_add_member"
                            )
                        }
                        QuickActionTile(
                            icon = Icons.Default.AddBusiness,
                            label = copy.switchCompany,
                            onClick = onOpenCompanySwitcher,
                            testTag = "quick_switch_company"
                        )
                    }
                }
            }

            // Financial KPIs & Chart (RBAC Gated)
            if (canViewFinances) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LuxuryKpiCard(
                                title = copy.totalIncome,
                                value = BdtFormatter.formatBdt(kpis.totalIncome, language),
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                accentColor = EmeraldIncome,
                                badgeBg = EmeraldSoft,
                                modifier = Modifier.weight(1f),
                                testTag = "kpi_total_income"
                            )
                            LuxuryKpiCard(
                                title = copy.totalExpenses,
                                value = BdtFormatter.formatBdt(kpis.totalExpenses, language),
                                icon = Icons.AutoMirrored.Filled.TrendingDown,
                                accentColor = CrimsonExpense,
                                badgeBg = CrimsonSoft,
                                modifier = Modifier.weight(1f),
                                testTag = "kpi_total_expenses"
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LuxuryKpiCard(
                                title = copy.totalProfit,
                                value = BdtFormatter.formatBdt(kpis.totalProfit, language),
                                icon = Icons.Default.Payments,
                                accentColor = ObsidianBlack,
                                badgeBg = SoftGoldTint,
                                modifier = Modifier.weight(1f),
                                testTag = "kpi_total_profit"
                            )
                            LuxuryKpiCard(
                                title = copy.cashBalance,
                                value = BdtFormatter.formatBdt(kpis.cashBalance, language),
                                icon = Icons.Default.ReceiptLong,
                                accentColor = EmeraldIncome,
                                badgeBg = EmeraldSoft,
                                modifier = Modifier.weight(1f),
                                testTag = "kpi_cash_balance"
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LuxuryKpiCard(
                                title = copy.bankBalance,
                                value = BdtFormatter.formatBdt(kpis.bankBalance, language),
                                icon = Icons.Default.AccountBalance,
                                accentColor = IndigoBank,
                                badgeBg = IndigoSoft,
                                modifier = Modifier.weight(1f),
                                testTag = "kpi_bank_balance"
                            )
                            LuxuryKpiCard(
                                title = copy.customerDue,
                                value = BdtFormatter.formatBdt(kpis.customerDue, language),
                                icon = Icons.Default.CreditScore,
                                accentColor = AmberDue,
                                badgeBg = AmberSoft,
                                modifier = Modifier.weight(1f),
                                testTag = "kpi_customer_due"
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LuxuryKpiCard(
                                title = copy.supplierDue,
                                value = BdtFormatter.formatBdt(kpis.supplierDue, language),
                                icon = Icons.Default.CreditScore,
                                accentColor = CrimsonExpense,
                                badgeBg = CrimsonSoft,
                                modifier = Modifier.weight(1f),
                                testTag = "kpi_supplier_due"
                            )
                            LuxuryKpiCard(
                                title = copy.totalEmployees,
                                value = BdtFormatter.formatCount(kpis.totalEmployees, language),
                                icon = Icons.Default.Badge,
                                accentColor = ObsidianBlack,
                                badgeBg = SoftGoldTint,
                                modifier = Modifier.weight(1f),
                                testTag = "kpi_employees"
                            )
                        }
                    }
                }

                item {
                    FinancialAnalyticsChart(
                        transactions = transactions,
                        income = kpis.totalIncome,
                        expenses = kpis.totalExpenses,
                        profit = kpis.totalProfit,
                        language = language,
                        copy = copy
                    )
                }
            } else {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ChampagneGold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(copy.restrictedFinanceNotice, color = SilkWhite)
                        }
                    }
                }
            }

            // Workforce & Production Summary Row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LuxuryKpiCard(
                        title = copy.totalKarigors,
                        value = BdtFormatter.formatCount(kpis.totalKarigors, language),
                        icon = Icons.Default.Handyman,
                        accentColor = ObsidianBlack,
                        badgeBg = SoftGoldTint,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_karigors"
                    )
                    LuxuryKpiCard(
                        title = copy.productionSummary,
                        value = "${BdtFormatter.formatCount(kpis.totalProductionCompleted, language)} / ${BdtFormatter.formatCount(kpis.totalProductionTarget, language)}",
                        subtitle = "${BdtFormatter.formatCount(kpis.activeOrdersCount, language)} ${if (language == AppLanguage.BENGALI) "চলমান ব্যাচ" else "active batches"}",
                        icon = Icons.Default.Factory,
                        accentColor = IndigoBank,
                        badgeBg = IndigoSoft,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_production_summary"
                    )
                }
            }

            // Production Progress Snippet
            if (productionOrders.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, SoftBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(copy.productionSummary, style = MaterialTheme.typography.titleMedium)
                            productionOrders.take(3).forEach { order ->
                                val progress = if (order.targetUnits > 0) {
                                    (order.completedUnits.toFloat() / order.targetUnits.toFloat()).coerceIn(0f, 1f)
                                } else 0f
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${order.batchCode} • ${order.productName}", fontWeight = FontWeight.Medium)
                                        Text(
                                            text = "${order.completedUnits}/${order.targetUnits}",
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 12.sp,
                                            color = MutedSlate
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        color = ChampagneGold,
                                        trackColor = SoftBorder,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(50))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions (Finance Authorized Only)
            if (canViewFinances) {
                item {
                    Text(copy.recentTransactions, style = MaterialTheme.typography.titleMedium, color = InkBlack)
                }
                if (transactions.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, SoftBorder)
                        ) {
                            Text(
                                text = copy.emptyDashboardSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(transactions.take(5), key = { it.id }) { tx ->
                        val isIncomeLike = tx.entryType in listOf(
                            TransactionEntryType.INCOME.key,
                            TransactionEntryType.CASH_IN.key,
                            TransactionEntryType.BANK_DEPOSIT.key
                        )
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, SoftBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.title, style = MaterialTheme.typography.titleMedium, color = InkBlack)
                                    Text(
                                        text = "${TransactionEntryType.fromKey(tx.entryType).label(language)} • ${tx.category}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedSlate
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = BdtFormatter.formatBdt(tx.amountBdt, language),
                                        fontFamily = JetBrainsMonoFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIncomeLike) EmeraldIncome else CrimsonExpense
                                    )
                                    Text(
                                        text = BdtFormatter.formatDhakaShortDate(tx.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSlate
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Activities (Audit Feed)
            item {
                Text(copy.recentActivities, style = MaterialTheme.typography.titleMedium, color = InkBlack)
            }
            items(auditLogs.take(4), key = { it.id }) { log ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, SoftBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SoftGoldTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.action, style = MaterialTheme.typography.labelLarge, color = InkBlack)
                            Text(log.details, style = MaterialTheme.typography.bodyMedium, color = MutedSlate)
                        }
                        Text(
                            text = BdtFormatter.formatDhakaShortDate(log.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = ObsidianBlack,
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = SilkWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}
