package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.localization.BdtFormatter
import com.example.model.AppLanguage
import com.example.model.Company
import com.example.model.CompanyMember
import com.example.model.PaymentRequest
import com.example.model.Permission
import com.example.model.StaffType
import com.example.model.SubscriptionPlan
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
fun TeamScreen(
    company: Company,
    role: UserRole,
    language: AppLanguage,
    members: List<CompanyMember>,
    paymentRequests: List<PaymentRequest>,
    onOpenAddMember: () -> Unit,
    onSubmitPayment: (SubscriptionPlan, String, String) -> Unit
) {
    val copy = AppStrings.get(language)
    val canManageTeam = role.has(Permission.MANAGE_TEAM)
    var showSubscriptionModal by remember { mutableStateOf(false) }

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
                Text(copy.navTeam, style = MaterialTheme.typography.headlineMedium, color = InkBlack)
            }

            // Subscription Entitlement Card
            item {
                SubscriptionOverviewCard(
                    company = company,
                    language = language,
                    paymentRequests = paymentRequests,
                    onUpgradeClick = { showSubscriptionModal = true }
                )
            }

            // Members & Karigors List
            item {
                Text(
                    text = if (language == AppLanguage.BENGALI)
                        "কোম্পানি সদস্য ও কারিগর (${members.size})"
                    else
                        "Team Members & Karigors (${members.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkBlack
                )
            }
            items(members, key = { it.id }) { member ->
                val memberRole = UserRole.fromKey(member.role)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, SoftBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ObsidianBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ChampagneGold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.displayName, style = MaterialTheme.typography.titleMedium, color = InkBlack)
                            Text(
                                text = "${memberRole.label(language)} • ${StaffType.fromKey(member.staffType).label(language)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate
                            )
                            if (!member.phone.isNullOrBlank()) {
                                Text(member.phone, style = MaterialTheme.typography.labelSmall, color = InkBlack)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldIncome.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = member.status,
                                color = EmeraldIncome,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // RBAC Role Architecture Matrix
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.BENGALI)
                        "রোল ও পারমিশন আর্কিটেকচার (RBAC)"
                    else
                        "Role & Permission Matrix (RBAC)",
                    style = MaterialTheme.typography.titleMedium,
                    color = InkBlack
                )
            }
            items(UserRole.entries, key = { it.key }) { r ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, SoftBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(r.label(language), style = MaterialTheme.typography.titleMedium, color = InkBlack)
                                if (r == UserRole.SUPER_ADMIN) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Lock, contentDescription = "Platform Restricted", tint = CrimsonExpense, modifier = Modifier.size(15.dp))
                                }
                            }
                            Text(
                                text = "${r.permissions().size} permissions • ${if (r.has(Permission.VIEW_FINANCES)) "Finance Access" else "Operations Only"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate
                            )
                        }
                        if (r.isFuturePhasePlaceholder) {
                            Surface(shape = RoundedCornerShape(50), color = SoftGoldTint) {
                                Text(
                                    text = if (language == AppLanguage.BENGALI) "প্রস্তুত রোল" else "Ready Role",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBlack,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (canManageTeam) {
            FloatingActionButton(
                onClick = onOpenAddMember,
                containerColor = ObsidianBlack,
                contentColor = ChampagneGold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("team_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = copy.addMember)
            }
        }
    }

    if (showSubscriptionModal) {
        SubscriptionPaymentDialog(
            language = language,
            onSubmit = { plan, sender, ref ->
                onSubmitPayment(plan, sender, ref)
                showSubscriptionModal = false
            },
            onDismiss = { showSubscriptionModal = false }
        )
    }
}

@Composable
fun SubscriptionOverviewCard(
    company: Company,
    language: AppLanguage,
    paymentRequests: List<PaymentRequest>,
    onUpgradeClick: () -> Unit
) {
    val copy = AppStrings.get(language)
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = ChampagneGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(copy.subscriptionTitle, style = MaterialTheme.typography.titleMedium, color = ChampagneGold)
                }
                Surface(shape = RoundedCornerShape(50), color = ChampagneGold) {
                    Text(
                        text = "${company.subscriptionTier} (${company.subscriptionStatus})",
                        color = ObsidianBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(copy.paymentNumberLabel, color = SilkWhite.copy(alpha = 0.78f), fontSize = 12.sp)
                    Text(
                        text = SubscriptionPlan.PAYMENT_NUMBER,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChampagneGold
                    )
                }
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(SubscriptionPlan.PAYMENT_NUMBER))
                        Toast.makeText(context, copy.copiedToast, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_payment_number_team")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = copy.copyNumber, tint = ChampagneGold)
                }
            }
            Text(
                text = copy.pendingVerificationNote,
                style = MaterialTheme.typography.bodyMedium,
                color = SilkWhite.copy(alpha = 0.78f)
            )
            if (paymentRequests.isNotEmpty()) {
                HorizontalDivider(color = ChampagneGold.copy(alpha = 0.25f))
                val latest = paymentRequests.first()
                Text(
                    text = "Latest Request: ${latest.plan} • Ref ${latest.transactionRef} (${latest.status})",
                    style = MaterialTheme.typography.labelSmall,
                    color = ChampagneGold
                )
            }
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth().testTag("open_subscription_modal_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(copy.submitPaymentRef, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubscriptionPaymentDialog(
    language: AppLanguage,
    onSubmit: (SubscriptionPlan, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val copy = AppStrings.get(language)
    var selectedPlan by remember { mutableStateOf(SubscriptionPlan.MONTHLY) }
    var senderAccount by remember { mutableStateOf("") }
    var txRef by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        title = { Text(copy.subscriptionTitle) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${copy.paymentNumberLabel}: ${SubscriptionPlan.PAYMENT_NUMBER}",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianBlack
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(SubscriptionPlan.MONTHLY, SubscriptionPlan.QUARTERLY, SubscriptionPlan.YEARLY).forEach { plan ->
                        FilterChip(
                            selected = selectedPlan == plan,
                            onClick = { selectedPlan = plan },
                            label = {
                                Text("${plan.title(language)} (${BdtFormatter.formatBdt(plan.priceBdt, language)})")
                            }
                        )
                    }
                }
                OutlinedTextField(
                    value = senderAccount,
                    onValueChange = { senderAccount = it; errorMsg = null },
                    label = { Text(if (language == AppLanguage.BENGALI) "প্রেরকের নম্বর *" else "Sender Account / Mobile *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payment_sender_input")
                )
                OutlinedTextField(
                    value = txRef,
                    onValueChange = { txRef = it; errorMsg = null },
                    label = { Text(if (language == AppLanguage.BENGALI) "ট্রানজেকশন আইডি (TrxID) *" else "Transaction Reference *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payment_ref_input")
                )
                Text(copy.pendingVerificationNote, style = MaterialTheme.typography.bodyMedium, color = MutedSlate)
                if (!errorMsg.isNullOrBlank()) {
                    Text(errorMsg ?: "", color = CrimsonExpense)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        senderAccount.trim().length < 4 -> errorMsg = "Enter sender number (min 4 chars)"
                        txRef.trim().length < 3 -> errorMsg = "Enter transaction reference"
                        else -> onSubmit(selectedPlan, senderAccount, txRef)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = ChampagneGold),
                modifier = Modifier.testTag("submit_payment_btn")
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
fun AddMemberDialog(
    language: AppLanguage,
    onSave: (
        displayName: String,
        email: String?,
        phone: String?,
        role: UserRole,
        staffType: StaffType,
        memberUidInput: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val copy = AppStrings.get(language)
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var memberUidInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.KARIGOR) }
    var staffType by remember { mutableStateOf(StaffType.KARIGOR) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        title = { Text(copy.addMember) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it; errorMsg = null },
                    label = { Text(if (language == AppLanguage.BENGALI) "নাম *" else "Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("member_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (language == AppLanguage.BENGALI) "ফোন নম্বর" else "Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (language == AppLanguage.BENGALI) "ইমেইল (ঐচ্ছিক)" else "Email (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(StaffType.STAFF, StaffType.KARIGOR).forEach { st ->
                        FilterChip(
                            selected = staffType == st,
                            onClick = { staffType = st },
                            label = { Text(st.label(language)) }
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UserRole.assignableRoles.forEach { r ->
                        FilterChip(
                            selected = selectedRole == r,
                            onClick = {
                                selectedRole = r
                                if (r == UserRole.KARIGOR) staffType = StaffType.KARIGOR
                            },
                            label = { Text(r.label(language), fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = memberUidInput,
                    onValueChange = { memberUidInput = it },
                    label = { Text(if (language == AppLanguage.BENGALI) "লগইন UID (ঐচ্ছিক)" else "Firebase UID (Optional for multi-user login)") },
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
                    if (displayName.trim().isEmpty()) {
                        errorMsg = "Enter full name"
                    } else {
                        onSave(
                            displayName,
                            email.takeIf { it.isNotBlank() },
                            phone.takeIf { it.isNotBlank() },
                            selectedRole,
                            staffType,
                            memberUidInput.takeIf { it.isNotBlank() }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = ChampagneGold),
                modifier = Modifier.testTag("member_save_btn")
            ) {
                Text(copy.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(copy.cancel) }
        }
    )
}
