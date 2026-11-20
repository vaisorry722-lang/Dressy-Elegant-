package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.localization.BdtFormatter
import com.example.model.AppLanguage
import com.example.model.FinancialTransaction
import com.example.model.PaymentMethod
import com.example.model.Permission
import com.example.model.ProductionOrder
import com.example.model.ProductionStatus
import com.example.model.ProductionType
import com.example.model.TransactionEntryType
import com.example.model.UserRole
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonExpense
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.InkBlack
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SilkWhite
import com.example.ui.theme.SoftBorder
import com.example.ui.theme.SoftGoldTint

@Composable
fun LedgerScreen(
    role: UserRole,
    language: AppLanguage,
    transactions: List<FinancialTransaction>,
    onOpenAddTransaction: () -> Unit
) {
    val copy = AppStrings.get(language)
    var selectedType by remember { mutableStateOf<TransactionEntryType?>(null) }

    if (!role.has(Permission.VIEW_FINANCES)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                border = BorderStroke(1.dp, ChampagneGold)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = ChampagneGold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(copy.restrictedFinanceNotice, color = SilkWhite)
                }
            }
        }
        return
    }

    val filtered = if (selectedType == null) transactions else transactions.filter { it.entryType == selectedType?.key }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(copy.navLedger, style = MaterialTheme.typography.headlineMedium, color = InkBlack)
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                        label = { Text(if (language == AppLanguage.BENGALI) "সব" else "All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ObsidianBlack,
                            selectedLabelColor = ChampagneGold
                        )
                    )
                    TransactionEntryType.entries.forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t.label(language)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ObsidianBlack,
                                selectedLabelColor = ChampagneGold
                            )
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, SoftBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(copy.emptyDashboardTitle, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(copy.emptyDashboardSubtitle, color = MutedSlate)
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { tx ->
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
                                    text = "${TransactionEntryType.fromKey(tx.entryType).label(language)} • ${tx.category} • ${PaymentMethod.fromKey(tx.paymentMethod).label(language)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSlate
                                )
                                if (!tx.partyName.isNullOrBlank()) {
                                    Text(
                                        text = tx.partyName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = InkBlack
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = BdtFormatter.formatBdt(tx.amountBdt, language),
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIncomeLike) EmeraldIncome else CrimsonExpense
                                )
                                Text(
                                    text = BdtFormatter.formatDhakaDateTime(tx.createdAt, language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate
                                )
                            }
                        }
                    }
                }
            }
        }

        if (role.has(Permission.WRITE_FINANCES)) {
            FloatingActionButton(
                onClick = onOpenAddTransaction,
                containerColor = ObsidianBlack,
                contentColor = ChampagneGold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("ledger_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = copy.addTransaction)
            }
        }
    }
}

@Composable
fun ProductionScreen(
    role: UserRole,
    language: AppLanguage,
    orders: List<ProductionOrder>,
    onOpenAddProduction: () -> Unit,
    onUpdateProgress: (ProductionOrder, Long, ProductionStatus) -> Unit
) {
    val copy = AppStrings.get(language)
    val canWrite = role.has(Permission.WRITE_PRODUCTION)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(copy.productionSummary, style = MaterialTheme.typography.headlineMedium, color = InkBlack)
            }
            if (orders.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, SoftBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (language == AppLanguage.BENGALI) "কোনো উৎপাদন ব্যাচ যোগ করা হয়নি" else "No production batches yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == AppLanguage.BENGALI)
                                    "বোরকা, আবায়া বা গার্মেন্টস অর্ডার ট্র্যাক করতে + বাটনে চাপুন।"
                                else
                                    "Tap + to schedule a new Burqa, Abaya, or Garments production batch.",
                                color = MutedSlate
                            )
                        }
                    }
                }
            } else {
                items(orders, key = { it.id }) { order ->
                    val status = ProductionStatus.fromKey(order.status)
                    val progress = if (order.targetUnits > 0) {
                        (order.completedUnits.toFloat() / order.targetUnits.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, SoftBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(order.productName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${order.batchCode} • ${ProductionType.fromKey(order.productType).label(language)} • ${order.assignedKarigors} Karigors",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedSlate
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (status == ProductionStatus.COMPLETED) EmeraldIncome.copy(alpha = 0.14f) else SoftGoldTint
                                ) {
                                    Text(
                                        text = status.label(language),
                                        color = if (status == ProductionStatus.COMPLETED) EmeraldIncome else ObsidianBlack,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                color = ChampagneGold,
                                trackColor = SoftBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${order.completedUnits} / ${order.targetUnits} pcs",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                if (canWrite && status != ProductionStatus.COMPLETED) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                val next = (order.completedUnits + 10L).coerceAtMost(order.targetUnits)
                                                val nextStatus = if (next >= order.targetUnits) ProductionStatus.COMPLETED else ProductionStatus.IN_PROGRESS
                                                onUpdateProgress(order, next, nextStatus)
                                            }
                                        ) {
                                            Text("+10 pcs")
                                        }
                                        Button(
                                            onClick = {
                                                onUpdateProgress(order, order.targetUnits, ProductionStatus.COMPLETED)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ObsidianBlack,
                                                contentColor = ChampagneGold
                                            )
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (language == AppLanguage.BENGALI) "সম্পন্ন" else "Done")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (canWrite) {
            FloatingActionButton(
                onClick = onOpenAddProduction,
                containerColor = ObsidianBlack,
                contentColor = ChampagneGold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("production_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = copy.addProduction)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    language: AppLanguage,
    onSave: (
        entryType: TransactionEntryType,
        category: String,
        title: String,
        partyName: String?,
        amountBdt: Double,
        paymentMethod: PaymentMethod,
        notes: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val copy = AppStrings.get(language)
    var entryType by remember { mutableStateOf(TransactionEntryType.INCOME) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Sales") }
    var partyName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        title = { Text(copy.addTransaction) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TransactionEntryType.entries.forEach { t ->
                        FilterChip(
                            selected = entryType == t,
                            onClick = { entryType = t },
                            label = { Text(t.label(language), fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMsg = null },
                    label = { Text(if (language == AppLanguage.BENGALI) "বিবরণ / শিরোনাম *" else "Entry Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("tx_title_input")
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMsg = null },
                    label = { Text(if (language == AppLanguage.BENGALI) "পরিমাণ (BDT ৳) *" else "Amount (BDT ৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("tx_amount_input")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it; errorMsg = null },
                    label = { Text(if (language == AppLanguage.BENGALI) "খাত / ক্যাটাগরি *" else "Category *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("tx_category_input")
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMethod.entries.forEach { pm ->
                        FilterChip(
                            selected = paymentMethod == pm,
                            onClick = { paymentMethod = pm },
                            label = { Text(pm.label(language), fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text(if (language == AppLanguage.BENGALI) "কাস্টমার / সাপ্লায়ার (ঐচ্ছিক)" else "Party Name (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (language == AppLanguage.BENGALI) "নোট" else "Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!errorMsg.isNullOrBlank()) {
                    Text(errorMsg ?: "", color = CrimsonExpense)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    when {
                        title.trim().isEmpty() -> errorMsg = "Enter title"
                        category.trim().isEmpty() -> errorMsg = "Enter category"
                        amt == null || amt <= 0.0 -> errorMsg = "Enter valid BDT amount > 0"
                        else -> onSave(entryType, category, title, partyName, amt, paymentMethod, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = ChampagneGold),
                modifier = Modifier.testTag("tx_save_btn")
            ) {
                Text(copy.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(copy.cancel) }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddProductionDialog(
    language: AppLanguage,
    onSave: (
        productName: String,
        productType: ProductionType,
        targetUnits: Long,
        assignedKarigors: Long,
        dueLabel: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val copy = AppStrings.get(language)
    var productName by remember { mutableStateOf("") }
    var productType by remember { mutableStateOf(ProductionType.ABAYA) }
    var targetText by remember { mutableStateOf("") }
    var karigorsText by remember { mutableStateOf("4") }
    var dueLabel by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        title = { Text(copy.addProduction) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProductionType.entries.forEach { pt ->
                        FilterChip(
                            selected = productType == pt,
                            onClick = { productType = pt },
                            label = { Text(pt.label(language), fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it; errorMsg = null },
                    label = { Text(if (language == AppLanguage.BENGALI) "পণ্যের নাম *" else "Product / Style Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("prod_name_input")
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it; errorMsg = null },
                    label = { Text(if (language == AppLanguage.BENGALI) "লক্ষ্যমাত্রা (পিস) *" else "Target Units (pcs) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("prod_target_input")
                )
                OutlinedTextField(
                    value = karigorsText,
                    onValueChange = { karigorsText = it; errorMsg = null },
                    label = { Text(if (language == AppLanguage.BENGALI) "কারিগর সংখ্যা" else "Assigned Karigors") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueLabel,
                    onValueChange = { dueLabel = it },
                    label = { Text(if (language == AppLanguage.BENGALI) "ডেলিভারি সময় (ঐচ্ছিক)" else "Due Date Label (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!errorMsg.isNullOrBlank()) {
                    Text(errorMsg ?: "", color = CrimsonExpense)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toLongOrNull()
                    val karigors = karigorsText.toLongOrNull() ?: 0L
                    when {
                        productName.trim().isEmpty() -> errorMsg = "Enter product name"
                        target == null || target < 1L -> errorMsg = "Enter target units >= 1"
                        else -> onSave(productName, productType, target, karigors, dueLabel)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = ChampagneGold),
                modifier = Modifier.testTag("prod_save_btn")
            ) {
                Text(copy.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(copy.cancel) }
        }
    )
}
