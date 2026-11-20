package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.localization.BdtFormatter
import com.example.model.AppLanguage
import com.example.model.AuditLog
import com.example.model.BusinessCategory
import com.example.model.Company
import com.example.model.UserRole
import com.example.security.PinAccountState
import com.example.ui.components.LanguageTogglePill
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonExpense
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.InkBlack
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SilkWhite
import com.example.ui.theme.SoftBorder
import com.example.ui.theme.SoftGoldTint
import com.google.firebase.auth.FirebaseUser

@Composable
fun SettingsScreen(
    user: FirebaseUser,
    company: Company,
    role: UserRole,
    language: AppLanguage,
    pinState: PinAccountState?,
    auditLogs: List<AuditLog>,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenPinSetup: () -> Unit,
    onTogglePinLock: (Boolean) -> Unit,
    onVerifyAndChangePin: suspend (String, String) -> Boolean,
    onResetPin: (String?) -> Unit,
    onCreateNewCompany: () -> Unit,
    onSignOut: () -> Unit
) {
    val copy = AppStrings.get(language)
    var activePinDialog by remember { mutableStateOf<PinDialogMode?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(copy.navSettings, style = MaterialTheme.typography.headlineMedium, color = InkBlack)
            }

            // User Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(ChampagneGold.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ChampagneGold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.displayName ?: "Dressy Owner",
                                style = MaterialTheme.typography.titleMedium,
                                color = ChampagneGold
                            )
                            Text(
                                text = user.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SilkWhite.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = if (user.isEmailVerified) EmeraldIncome else ChampagneGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${role.label(language)} • ${if (user.isEmailVerified) "Verified" else "Google Auth"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SilkWhite
                                )
                            }
                        }
                    }
                }
            }

            // Security PIN Setup, Change & Reset Card (Explicitly Requested)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = ObsidianBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(copy.securityPinSection, style = MaterialTheme.typography.titleLarge, color = InkBlack)
                        }
                        val hasPin = pinState?.hasPinConfigured == true
                        val pinEnabled = pinState?.requirePinOnLaunch == true
                        Text(
                            text = if (pinEnabled) copy.pinEnabledLabel else copy.pinDisabledLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (pinEnabled) EmeraldIncome else MutedSlate,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (hasPin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BENGALI)
                                        "অ্যাপ লঞ্চ / লগইনে পিন চালু রাখুন"
                                    else
                                        "Require Security PIN on login",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Switch(
                                    checked = pinEnabled,
                                    onCheckedChange = onTogglePinLock,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = ChampagneGold,
                                        checkedTrackColor = ObsidianBlack
                                    ),
                                    modifier = Modifier.testTag("settings_pin_toggle")
                                )
                            }
                            HorizontalDivider(color = SoftBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { activePinDialog = PinDialogMode.CHANGE_PIN },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_change_pin_btn"),
                                    border = BorderStroke(1.dp, ObsidianBlack)
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(copy.changePinBtn, color = InkBlack)
                                }
                                Button(
                                    onClick = { activePinDialog = PinDialogMode.RESET_PIN },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_reset_pin_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ObsidianBlack,
                                        contentColor = ChampagneGold
                                    )
                                ) {
                                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(copy.resetPinBtn)
                                }
                            }
                        } else {
                            Button(
                                onClick = onOpenPinSetup,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_setup_pin_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ObsidianBlack,
                                    contentColor = ChampagneGold
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(copy.setupPinBtn, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Language & Regional Settings
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, SoftBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(copy.languageLabel, style = MaterialTheme.typography.titleMedium)
                            LanguageTogglePill(language = language, onToggle = onLanguageChange)
                        }
                        HorizontalDivider(color = SoftBorder)
                        Text(
                            text = "${company.name} (${company.companyCode})",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkBlack
                        )
                        Text(
                            text = "${BusinessCategory.fromKey(company.category).label(language)} • ${company.country} • ${company.currency} (৳) • ${company.timezone}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSlate
                        )
                        OutlinedButton(
                            onClick = onCreateNewCompany,
                            modifier = Modifier.fillMaxWidth().testTag("settings_create_company_btn")
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(copy.createCompany)
                        }
                    }
                }
            }

            // Audit Logs
            item {
                Text(copy.auditLogsTitle, style = MaterialTheme.typography.titleMedium, color = InkBlack)
            }
            items(auditLogs.take(8), key = { it.id }) { log ->
                Card(
                    shape = RoundedCornerShape(12.dp),
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
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(SoftGoldTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.action, style = MaterialTheme.typography.labelLarge)
                            Text(log.details, style = MaterialTheme.typography.bodyMedium, color = MutedSlate)
                            Text(
                                text = "${log.actorName} • ${BdtFormatter.formatDhakaDateTime(log.createdAt, language)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate
                            )
                        }
                    }
                }
            }

            // Sign Out
            item {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("settings_sign_out_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonExpense,
                        contentColor = SilkWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(copy.signOut, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    activePinDialog?.let { mode ->
        PinManagementDialog(
            mode = mode,
            language = language,
            onVerifyAndChangePin = onVerifyAndChangePin,
            onResetPin = onResetPin,
            onDismiss = { activePinDialog = null }
        )
    }
}
