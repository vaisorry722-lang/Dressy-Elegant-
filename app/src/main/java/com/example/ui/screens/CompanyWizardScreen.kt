package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.localization.AppStrings
import com.example.model.AppLanguage
import com.example.model.BusinessCategory
import com.example.ui.components.DeMonogramBadge
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonExpense
import com.example.ui.theme.InkBlack
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SilkWhite
import com.example.ui.theme.SoftBorder
import com.example.ui.theme.SoftGoldTint
import com.example.ui.theme.SoftGrayBg

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompanyWizardScreen(
    language: AppLanguage,
    defaultOwnerName: String,
    defaultEmail: String,
    canNavigateBack: Boolean,
    isBusy: Boolean,
    onBack: () -> Unit,
    onCreateCompany: (
        name: String,
        ownerName: String,
        phone: String,
        email: String,
        address: String,
        category: BusinessCategory,
        description: String?,
        logoTag: String?
    ) -> Unit
) {
    val copy = AppStrings.get(language)
    if (canNavigateBack) {
        BackHandler(onBack = onBack)
    }

    var companyName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf(defaultOwnerName) }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(defaultEmail) }
    var address by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(BusinessCategory.GARMENTS_FACTORY) }
    var description by remember { mutableStateOf("") }
    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedLogoUri = uri
    }

    Scaffold(
        containerColor = SoftGrayBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DeMonogramBadge(size = 30.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(copy.companySetupTitle, color = ChampagneGold, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("wizard_back_btn")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = copy.cancel,
                                tint = ChampagneGold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBlack)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ChampagneGold.copy(alpha = 0.14f))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("company_logo_picker_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedLogoUri != null) {
                                AsyncImage(
                                    model = selectedLogoUri,
                                    contentDescription = "Company Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Pick Company Logo",
                                    tint = ChampagneGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = copy.companySetupTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = ChampagneGold
                            )
                            Text(
                                text = copy.companySetupSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SilkWhite.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Bangladesh • BDT (৳) • Asia/Dhaka • Auto ID: DE-XXXXXX",
                                style = MaterialTheme.typography.labelSmall,
                                color = ChampagneGold.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Form Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, SoftBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it; validationError = null },
                            label = {
                                Text(if (language == AppLanguage.BENGALI) "কোম্পানির নাম *" else "Company Name *")
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("company_name_input")
                        )

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it; validationError = null },
                            label = {
                                Text(if (language == AppLanguage.BENGALI) "মালিকের নাম *" else "Owner Name *")
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("company_owner_input")
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; validationError = null },
                            label = {
                                Text(if (language == AppLanguage.BENGALI) "ফোন নম্বর *" else "Phone Number *")
                            },
                            placeholder = { Text("+8801XXXXXXXXX") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("company_phone_input")
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; validationError = null },
                            label = {
                                Text(if (language == AppLanguage.BENGALI) "ব্যবসায়িক ইমেইল *" else "Business Email *")
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("company_email_input")
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it; validationError = null },
                            label = {
                                Text(if (language == AppLanguage.BENGALI) "ঠিকানা (যেমন: মিরপুর ১০, ঢাকা) *" else "Business Address *")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("company_address_input")
                        )

                        Text(
                            text = if (language == AppLanguage.BENGALI) "ব্যবসার ধরন নির্বাচন করুন *" else "Business Category *",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkBlack
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BusinessCategory.entries.forEach { cat ->
                                val selected = cat == selectedCategory
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.label(language)) },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ObsidianBlack,
                                        selectedLabelColor = ChampagneGold,
                                        selectedLeadingIconColor = ChampagneGold
                                    ),
                                    modifier = Modifier.testTag("category_chip_${cat.key}")
                                )
                            }
                        }

                        // Locked Country / Currency / Timezone summary
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftGoldTint,
                            border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.BENGALI) "দেশ ও মুদ্রা" else "Country & Currency",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSlate
                                    )
                                    Text(
                                        text = "Bangladesh • BDT (৳)",
                                        fontWeight = FontWeight.Bold,
                                        color = InkBlack
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (language == AppLanguage.BENGALI) "সময় অঞ্চল" else "Timezone",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSlate
                                    )
                                    Text(
                                        text = "Asia/Dhaka",
                                        fontWeight = FontWeight.Bold,
                                        color = InkBlack
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = {
                                Text(if (language == AppLanguage.BENGALI) "ব্যবসার বিবরণ (ঐচ্ছিক)" else "Business Description (Optional)")
                            },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("company_description_input")
                        )

                        if (!validationError.isNullOrBlank()) {
                            Text(
                                text = validationError ?: "",
                                color = CrimsonExpense,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(
                            onClick = {
                                val err = when {
                                    companyName.trim().length < 2 ->
                                        if (language == AppLanguage.BENGALI) "কমপক্ষে ২ অক্ষরের কোম্পানির নাম দিন" else "Enter a valid company name (min 2 chars)"
                                    ownerName.trim().length < 2 ->
                                        if (language == AppLanguage.BENGALI) "মালিকের নাম দিন" else "Enter owner name"
                                    phone.trim().length < 5 ->
                                        if (language == AppLanguage.BENGALI) "সঠিক ফোন নম্বর দিন" else "Enter a valid phone number"
                                    !email.contains("@") ->
                                        if (language == AppLanguage.BENGALI) "সঠিক ইমেইল দিন" else "Enter a valid email address"
                                    address.trim().length < 2 ->
                                        if (language == AppLanguage.BENGALI) "কোম্পানির ঠিকানা দিন" else "Enter business address"
                                    else -> null
                                }
                                if (err != null) {
                                    validationError = err
                                } else {
                                    onCreateCompany(
                                        companyName,
                                        ownerName,
                                        phone,
                                        email,
                                        address,
                                        selectedCategory,
                                        description,
                                        selectedLogoUri?.toString()?.take(100)
                                    )
                                }
                            },
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_create_company_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianBlack,
                                contentColor = ChampagneGold
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = ChampagneGold,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Business, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(copy.createCompany, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
