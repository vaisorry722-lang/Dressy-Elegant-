package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.firebase.attemptAutoSignIn
import com.example.firebase.onGoogleSignInClicked
import com.example.localization.AppStrings
import com.example.localization.BdtFormatter
import com.example.model.AppLanguage
import com.example.model.BusinessCategory
import com.example.model.SubscriptionPlan
import com.example.ui.components.DeMonogramBadge
import com.example.ui.components.LanguageTogglePill
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
import com.example.ui.theme.SoftGrayBg

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LandingAuthScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val credentialManager = remember { CredentialManager.create(context) }
    val copy = AppStrings.get(language)
    val scrollState = rememberScrollState()

    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var expandedFaq by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    fun triggerGoogleSignIn() {
        authError = null
        isSigningIn = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isSigningIn = false
                onAuthSuccess()
            },
            onAuthError = { msg ->
                isSigningIn = false
                authError = msg
            },
            scope = scope,
            onAuthCancelled = {
                isSigningIn = false
            }
        )
    }

    Scaffold(
        containerColor = SoftGrayBg,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(scrollState)
                    .navigationBarsPadding()
            ) {
                // Luxury Top Header
                Surface(color = ObsidianBlack, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DeMonogramBadge(size = 38.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = copy.brandName,
                                    fontFamily = PlayfairDisplayFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ChampagneGold,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = copy.tagline,
                                    fontSize = 10.sp,
                                    color = SilkWhite.copy(alpha = 0.78f)
                                )
                            }
                        }
                        LanguageTogglePill(language = language, onToggle = onLanguageChange)
                    }
                }

                // Hero Section with Editorial Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianBlack)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = copy.brandName,
                        contentScale = ContentScale.Crop,
                        alpha = 0.42f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        ObsidianBlack.copy(alpha = 0.45f),
                                        ObsidianBlack.copy(alpha = 0.88f),
                                        ObsidianBlack
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 28.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = ChampagneGold.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "BDT (৳) • ASIA/DHAKA • MULTI-TENANT ERP",
                                color = ChampagneGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = copy.heroHeading,
                            style = MaterialTheme.typography.headlineLarge,
                            color = SilkWhite
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = copy.heroSubheading,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SilkWhite.copy(alpha = 0.84f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Interactive Google Sign-In & CTA Buttons
                        Button(
                            onClick = ::triggerGoogleSignIn,
                            enabled = !isSigningIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("google_sign_in_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampagneGold,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = ObsidianBlack,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${copy.getStartedFree} • ${copy.signInGoogle}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = ::triggerGoogleSignIn,
                            enabled = !isSigningIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("explore_premium_button"),
                            border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ChampagneGold),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(copy.explorePremium, fontWeight = FontWeight.SemiBold)
                        }

                        if (!authError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CrimsonExpense.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, CrimsonExpense)
                            ) {
                                Text(
                                    text = authError ?: "",
                                    color = SilkWhite,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Brand Intro Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, SoftBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = copy.brandIntroTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = InkBlack
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = copy.brandIntroBody,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = copy.authSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = ObsidianBlack
                            )
                        }
                    }

                    // Features Grid
                    Text(copy.featuresTitle, style = MaterialTheme.typography.titleLarge, color = InkBlack)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LandingFeatureCard(
                                icon = Icons.Default.VerifiedUser,
                                title = if (language == AppLanguage.BENGALI) "মাল্টি-টেন্যান্ট আইসোলেশন" else "Multi-Tenant Isolation",
                                desc = if (language == AppLanguage.BENGALI) "প্রতিটি কোম্পানির আলাদা ডেটা ও রোল সিকিউরিটি" else "Zero cross-company leakage via Firestore rules",
                                modifier = Modifier.weight(1f)
                            )
                            LandingFeatureCard(
                                icon = Icons.Default.AccountBalanceWallet,
                                title = if (language == AppLanguage.BENGALI) "BDT (৳) অ্যাকাউন্টিং" else "BDT (৳) Accounting",
                                desc = if (language == AppLanguage.BENGALI) "আয়, ব্যয়, ক্যাশ, ব্যাংক ও বাকি হিসাব" else "Income, expense, cash, bank & dues tracking",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LandingFeatureCard(
                                icon = Icons.Default.Factory,
                                title = if (language == AppLanguage.BENGALI) "উৎপাদন ও কারিগর" else "Production & Karigors",
                                desc = if (language == AppLanguage.BENGALI) "বোরকা, আবায়া ও গার্মেন্টস ব্যাচ মনিটরিং" else "Track burqa, abaya & garments batches",
                                modifier = Modifier.weight(1f)
                            )
                            LandingFeatureCard(
                                icon = Icons.Default.Lock,
                                title = if (language == AppLanguage.BENGALI) "সিকিউরিটি পিন লক" else "Security PIN Lock",
                                desc = if (language == AppLanguage.BENGALI) "লগইন পিন সেটআপ, পরিবর্তন ও রিসেট সুবিধা" else "Setup, change & reset PIN right in Settings",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Supported Business Solutions
                    Text(copy.solutionsTitle, style = MaterialTheme.typography.titleLarge, color = InkBlack)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BusinessCategory.entries.forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = CardWhite,
                                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = cat.label(language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = InkBlack,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    // Free vs Premium Comparison & Pricing
                    Text(copy.pricingTitle, style = MaterialTheme.typography.titleLarge, color = InkBlack)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = copy.paymentNumberLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SilkWhite.copy(alpha = 0.78f)
                                    )
                                    Text(
                                        text = SubscriptionPlan.PAYMENT_NUMBER,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ChampagneGold
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(SubscriptionPlan.PAYMENT_NUMBER))
                                        Toast.makeText(context, copy.copiedToast, Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("copy_payment_number_landing")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = copy.copyNumber,
                                        tint = ChampagneGold
                                    )
                                }
                            }
                            HorizontalDivider(color = ChampagneGold.copy(alpha = 0.25f))
                            SubscriptionPlan.entries.forEach { plan ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ChampagneGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(plan.title(language), color = SilkWhite, fontWeight = FontWeight.Medium)
                                    }
                                    Text(
                                        text = "${BdtFormatter.formatBdt(plan.priceBdt, language)} ${plan.period(language)}",
                                        fontFamily = JetBrainsMonoFamily,
                                        color = ChampagneGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Sample Testimonials (Clearly Labeled)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(copy.testimonialsTitle, style = MaterialTheme.typography.titleLarge, color = InkBlack)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftGoldTint,
                            border = BorderStroke(1.dp, ChampagneGold)
                        ) {
                            Text(
                                text = copy.samplePreviewBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBlack,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, SoftBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (language == AppLanguage.BENGALI)
                                    "“আমাদের মিরপুর আবায়া কারখানার কারিগর হিসাব ও দৈনিক আয়-ব্যয় এখন এক অ্যাপেই পরিষ্কার।”"
                                else
                                    "“Tracking our Dhaka abaya karigors and daily BDT ledger in one multi-company dashboard transformed operations.”",
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkBlack
                            )
                            Text(
                                text = "— ${copy.samplePreviewBadge} • Al-Noor Abaya House, Dhaka",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate
                            )
                        }
                    }

                    // FAQ Accordion
                    Text(copy.faqTitle, style = MaterialTheme.typography.titleLarge, color = InkBlack)
                    val faqs = if (language == AppLanguage.BENGALI) listOf(
                        "একজন ব্যবহারকারী কি একাধিক কোম্পানি তৈরি করতে পারবেন?" to "হ্যাঁ, একই অ্যাকাউন্ট থেকে একাধিক কোম্পানি তৈরি ও সুইচ করা যাবে, এবং প্রতিটি কোম্পানির ডেটা সম্পূর্ণ আলাদা থাকবে।",
                        "সিকিউরিটি পিন কীভাবে কাজ করে?" to "লগইনের পর ৪–৬ সংখ্যার সিকিউরিটি পিন সেটআপ করতে পারবেন। সেটিংস থেকে যেকোনো সময় পিন পরিবর্তন বা রিসেট করা যাবে।",
                        "প্রিমিয়াম প্ল্যান কীভাবে সক্রিয় হয়?" to "পেমেন্ট নম্বর 301620556052-এ পেমেন্ট রেফারেন্স জমা দিলে সার্ভার যাচাইয়ের মাধ্যমে প্রিমিয়াম চালু হবে।"
                    ) else listOf(
                        "Can one owner manage multiple companies?" to "Yes. Create and switch between multiple isolated company workspaces under one login.",
                        "How does Security PIN work?" to "Set up a 4–6 digit PIN after login for device unlock protection, with Change and Reset options inside Settings.",
                        "How are Premium subscriptions activated?" to "Submit payment references to 301620556052; entitlements are verified server-side and never bypassed client-side."
                    )
                    faqs.forEachIndexed { idx, (q, a) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedFaq = if (expandedFaq == idx) -1 else idx },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, SoftBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = q,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = InkBlack,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (expandedFaq == idx) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MutedSlate
                                    )
                                }
                                AnimatedVisibility(visible = expandedFaq == idx) {
                                    Text(
                                        text = a,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedSlate,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Footer
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianBlack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = copy.contactTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = ChampagneGold
                            )
                            Text(
                                text = copy.contactBody,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SilkWhite.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "© DRESSY ELEGANT • ${copy.tagline}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SilkWhite.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandingFeatureCard(
    icon: ImageVector,
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, SoftBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(18.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = InkBlack)
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = MutedSlate)
        }
    }
}
