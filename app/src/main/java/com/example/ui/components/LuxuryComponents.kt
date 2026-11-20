package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.AppCopy
import com.example.localization.BdtFormatter
import com.example.model.AppLanguage
import com.example.model.BusinessCategory
import com.example.model.Company
import com.example.model.FinancialTransaction
import com.example.model.TenantNotification
import com.example.model.TransactionEntryType
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonExpense
import com.example.ui.theme.EmeraldIncome
import com.example.ui.theme.InkBlack
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.SilkWhite
import com.example.ui.theme.SoftBorder
import com.example.ui.theme.SoftGoldTint
import kotlin.math.max

@Composable
fun DeMonogramBadge(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(ObsidianBlack, Color(0xFF24221D))
                )
            )
            .border(1.5.dp, ChampagneGold, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "DE",
            fontFamily = PlayfairDisplayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).sp,
            color = ChampagneGold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun LanguageTogglePill(
    language: AppLanguage,
    onToggle: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("language_toggle_pill"),
        shape = RoundedCornerShape(50),
        color = ObsidianBlack,
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppLanguage.entries.forEach { item ->
                val selected = item == language
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) ChampagneGold else Color.Transparent)
                        .clickable { onToggle(item) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("lang_option_${item.code}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.label,
                        color = if (selected) ObsidianBlack else SilkWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LuxuryKpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    badgeBg: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    testTag: String = "kpi_card"
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, SoftBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = value,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = InkBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FinancialAnalyticsChart(
    transactions: List<FinancialTransaction>,
    income: Double,
    expenses: Double,
    profit: Double,
    language: AppLanguage,
    copy: AppCopy,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("financial_analytics_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, SoftBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = copy.financialAnalytics,
                style = MaterialTheme.typography.titleMedium,
                color = InkBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (transactions.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_empty_atelier),
                        contentDescription = copy.emptyDashboardTitle,
                        contentScale = ContentScale.Crop,
                        alpha = 0.55f,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = copy.emptyDashboardTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = ChampagneGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = copy.emptyDashboardSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SilkWhite.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                // Legend Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendDot(color = EmeraldIncome, label = "${copy.totalIncome}: ${BdtFormatter.formatBdt(income, language)}")
                    LegendDot(color = CrimsonExpense, label = "${copy.totalExpenses}: ${BdtFormatter.formatBdt(expenses, language)}")
                }
                LegendDot(
                    color = ChampagneGold,
                    label = "${copy.totalProfit}: ${BdtFormatter.formatBdt(profit, language)}"
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Group recent entries into up to 6 buckets for responsive bar visualization
                val buckets = transactions.take(18).chunked(3).take(6).reversed()
                val maxVal = max(
                    1.0,
                    max(income, max(expenses, kotlin.math.abs(profit)))
                )
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val baseline = h - 18.dp.toPx()
                    val usableH = baseline - 12.dp.toPx()

                    // Subtle grid lines
                    for (step in 0..3) {
                        val y = 12.dp.toPx() + usableH * (step / 3f)
                        drawLine(
                            color = SoftBorder,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Summary 3 primary bars (Income, Expense, Profit) + recent mini trend
                    val barCount = if (buckets.size > 1) buckets.size else 3
                    if (buckets.size <= 1) {
                        val values = listOf(
                            income.toFloat() to EmeraldIncome,
                            expenses.toFloat() to CrimsonExpense,
                            kotlin.math.max(0.0, profit).toFloat() to ChampagneGold
                        )
                        val slotW = w / 3f
                        val barW = (slotW * 0.42f).coerceAtMost(48.dp.toPx())
                        values.forEachIndexed { idx, (v, col) ->
                            val ratio = (v / maxVal.toFloat()).coerceIn(0.04f, 1f)
                            val barH = usableH * ratio
                            val left = idx * slotW + (slotW - barW) / 2f
                            drawRoundRect(
                                color = col,
                                topLeft = Offset(left, baseline - barH),
                                size = Size(barW, barH),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                    } else {
                        val slotW = w / barCount.toFloat()
                        val barW = (slotW * 0.24f).coerceAtMost(18.dp.toPx())
                        val bucketMax = max(
                            1.0,
                            buckets.maxOf { grp ->
                                max(
                                    grp.filter { it.entryType == TransactionEntryType.INCOME.key }.sumOf { it.amountBdt },
                                    grp.filter { it.entryType == TransactionEntryType.EXPENSE.key }.sumOf { it.amountBdt }
                                )
                            }
                        )
                        buckets.forEachIndexed { idx, grp ->
                            val inc = grp.filter { it.entryType == TransactionEntryType.INCOME.key }.sumOf { it.amountBdt }
                            val exp = grp.filter { it.entryType == TransactionEntryType.EXPENSE.key }.sumOf { it.amountBdt }
                            val prof = kotlin.math.max(0.0, inc - exp)
                            val centerX = idx * slotW + slotW / 2f

                            val incH = usableH * (inc / bucketMax).toFloat().coerceIn(0.03f, 1f)
                            val expH = usableH * (exp / bucketMax).toFloat().coerceIn(0.03f, 1f)
                            val profH = usableH * (prof / bucketMax).toFloat().coerceIn(0.03f, 1f)

                            drawRoundRect(
                                color = EmeraldIncome,
                                topLeft = Offset(centerX - barW * 1.6f, baseline - incH),
                                size = Size(barW, incH),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                            drawRoundRect(
                                color = CrimsonExpense,
                                topLeft = Offset(centerX - barW * 0.5f, baseline - expH),
                                size = Size(barW, expH),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                            drawRoundRect(
                                color = ChampagneGold,
                                topLeft = Offset(centerX + barW * 0.6f, baseline - profH),
                                size = Size(barW, profH),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = InkBlack,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PinKeypadPad(
    pinValue: String,
    maxLength: Int = 6,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN Dot indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(maxLength) { index ->
                val filled = index < pinValue.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (filled) ChampagneGold else Color.Transparent)
                        .border(
                            width = 1.5.dp,
                            color = if (filled) ChampagneGold else SilkWhite.copy(alpha = 0.45f),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val rows = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9')
        )
        rows.forEach { rowDigits ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                rowDigits.forEach { digit ->
                    PinKeyButton(
                        label = digit.toString(),
                        onClick = { if (pinValue.length < maxLength) onDigit(digit) },
                        testTag = "pin_key_$digit"
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(68.dp))
            PinKeyButton(
                label = "0",
                onClick = { if (pinValue.length < maxLength) onDigit('0') },
                testTag = "pin_key_0"
            )
            Surface(
                modifier = Modifier
                    .size(68.dp)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(onClick = onBackspace)
                    .testTag("pin_key_backspace"),
                shape = CircleShape,
                color = Color(0xFF1F1F26),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.28f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = ChampagneGold
                    )
                }
            }
        }
    }
}

@Composable
private fun PinKeyButton(
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .size(68.dp)
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = CircleShape,
        color = Color(0xFF1A1A20),
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.35f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SilkWhite
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySwitcherSheet(
    companies: List<Company>,
    activeCompanyId: String?,
    language: AppLanguage,
    copy: AppCopy,
    onSelectCompany: (String) -> Unit,
    onCreateNewCompany: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = copy.switchCompany,
                    style = MaterialTheme.typography.titleLarge,
                    color = InkBlack
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = copy.cancel)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(companies, key = { it.id }) { comp ->
                    val selected = comp.id == activeCompanyId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectCompany(comp.id)
                                onDismiss()
                            }
                            .testTag("switch_company_${comp.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) SoftGoldTint else SilkWhite
                        ),
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) ChampagneGold else SoftBorder
                        )
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
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Business,
                                    contentDescription = null,
                                    tint = ChampagneGold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comp.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkBlack
                                )
                                Text(
                                    text = "${comp.companyCode} • ${BusinessCategory.fromKey(comp.category).label(language)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSlate
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = ObsidianBlack
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = SoftBorder)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onDismiss()
                    onCreateNewCompany()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sheet_create_company_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ObsidianBlack,
                    contentColor = ChampagneGold
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(copy.createCompany, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<TenantNotification>,
    language: AppLanguage,
    copy: AppCopy,
    onMarkRead: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(copy.notificationsTitle, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = copy.cancel)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MutedSlate,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.BENGALI) "কোনো নতুন বিজ্ঞপ্তি নেই" else "No notifications yet",
                            color = MutedSlate
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMarkRead(item.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.readFlag) SilkWhite else SoftGoldTint
                            ),
                            border = BorderStroke(1.dp, SoftBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = InkBlack
                                    )
                                    Text(
                                        text = BdtFormatter.formatDhakaShortDate(item.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSlate
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSlate
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
