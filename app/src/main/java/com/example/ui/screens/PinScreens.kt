package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.localization.AppStrings
import com.example.model.AppLanguage
import com.example.security.PinSecurityManager
import com.example.ui.components.DeMonogramBadge
import com.example.ui.components.PinKeypadPad
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonExpense
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SilkWhite
import kotlinx.coroutines.launch

@Composable
fun PinSetupScreen(
    language: AppLanguage,
    allowSkip: Boolean,
    onSavePin: (String) -> Unit,
    onSkip: () -> Unit
) {
    val copy = AppStrings.get(language)
    var step by remember { mutableIntStateOf(1) }
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val activePin = if (step == 1) firstPin else confirmPin

    Scaffold(
        containerColor = ObsidianBlack,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DeMonogramBadge(size = 56.dp)
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = ChampagneGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = copy.pinSetupTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = ChampagneGold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (step == 1) copy.pinSetupSubtitle else copy.pinConfirmPrompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilkWhite.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = ChampagneGold.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (step == 1) copy.pinEnterPrompt else copy.pinConfirmPrompt,
                        color = ChampagneGold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                if (!errorMsg.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMsg ?: "",
                        color = CrimsonExpense,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))
                PinKeypadPad(
                    pinValue = activePin,
                    maxLength = 6,
                    onDigit = { ch ->
                        errorMsg = null
                        if (step == 1) firstPin += ch else confirmPin += ch
                    },
                    onBackspace = {
                        errorMsg = null
                        if (step == 1 && firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
                        else if (step == 2 && confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (step == 1) {
                            if (!PinSecurityManager.isValidPinFormat(firstPin)) {
                                errorMsg = copy.pinLengthRule
                            } else {
                                step = 2
                            }
                        } else {
                            if (confirmPin != firstPin) {
                                errorMsg = copy.pinMismatchError
                                confirmPin = ""
                            } else {
                                onSavePin(firstPin)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pin_setup_continue_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (step == 1) {
                            if (language == AppLanguage.BENGALI) "পরবর্তী ধাপ" else "Continue"
                        } else copy.pinSaveAction,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (step == 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = {
                        step = 1
                        confirmPin = ""
                        errorMsg = null
                    }) {
                        Text(
                            text = if (language == AppLanguage.BENGALI) "পুনরায় পিন লিখুন" else "Re-enter PIN",
                            color = SilkWhite.copy(alpha = 0.8f)
                        )
                    }
                }

                if (allowSkip) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("pin_setup_skip_btn")
                    ) {
                        Text(copy.pinSetupSkip, color = SilkWhite.copy(alpha = 0.72f))
                    }
                }
            }
        }
    }
}

@Composable
fun PinLockScreen(
    language: AppLanguage,
    userEmail: String,
    onVerifyPin: suspend (String) -> Boolean,
    onResetViaSignOut: () -> Unit,
    onSignOut: () -> Unit
) {
    val copy = AppStrings.get(language)
    val scope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableIntStateOf(0) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ObsidianBlack,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DeMonogramBadge(size = 58.dp)
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = ChampagneGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = copy.pinLockTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = ChampagneGold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = copy.pinLockSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilkWhite.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                if (userEmail.isNotBlank()) {
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.labelSmall,
                        color = ChampagneGold.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (!errorMsg.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMsg ?: "",
                        color = CrimsonExpense,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                PinKeypadPad(
                    pinValue = enteredPin,
                    maxLength = 6,
                    onDigit = { ch ->
                        errorMsg = null
                        enteredPin += ch
                    },
                    onBackspace = {
                        errorMsg = null
                        if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = {
                        if (!PinSecurityManager.isValidPinFormat(enteredPin)) {
                            errorMsg = copy.pinLengthRule
                            return@Button
                        }
                        scope.launch {
                            val ok = onVerifyPin(enteredPin)
                            if (!ok) {
                                attempts += 1
                                enteredPin = ""
                                errorMsg = "${copy.pinWrongError} ($attempts)"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pin_unlock_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "আনলক করুন" else "Unlock Workspace",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.testTag("forgot_pin_btn")
                    ) {
                        Text(copy.forgotPinResetBtn, color = ChampagneGold)
                    }
                    TextButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("pin_lock_sign_out_btn")
                    ) {
                        Text(copy.signOut, color = SilkWhite.copy(alpha = 0.75f))
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = CardWhite,
            title = { Text(copy.pinResetTitle) },
            text = {
                Text(
                    if (language == AppLanguage.BENGALI)
                        "সিকিউরিটি পিন রিসেট করতে চাইলে বর্তমান পিন মুছে ফেলা হবে এবং আপনাকে পুনরায় Google দিয়ে সাইন ইন করতে হবে।"
                    else
                        "Resetting a forgotten PIN clears the stored PIN and signs you out so you can verify identity via Google Sign-In."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetViaSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = ChampagneGold)
                ) {
                    Text(copy.resetPinBtn)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirm = false }) {
                    Text(copy.cancel)
                }
            }
        )
    }
}

enum class PinDialogMode {
    CHANGE_PIN,
    RESET_PIN
}

@Composable
fun PinManagementDialog(
    mode: PinDialogMode,
    language: AppLanguage,
    onVerifyAndChangePin: suspend (currentPin: String, newPin: String) -> Boolean,
    onResetPin: (newPin: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val copy = AppStrings.get(language)
    val scope = rememberCoroutineScope()
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        title = {
            Text(if (mode == PinDialogMode.CHANGE_PIN) copy.pinChangeTitle else copy.pinResetTitle)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (mode == PinDialogMode.CHANGE_PIN) {
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) currentPin = it },
                        label = { Text(copy.pinCurrentPrompt) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("change_pin_current_input")
                    )
                } else {
                    Text(
                        text = if (language == AppLanguage.BENGALI)
                            "নতুন ৪–৬ সংখ্যার পিন লিখুন অথবা নিচ থেকে পিন সম্পূর্ণ মুছে ফেলুন।"
                        else
                            "Enter a new 4–6 digit PIN below, or clear the PIN lock completely.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) newPin = it },
                    label = { Text(copy.pinNewPrompt) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manage_pin_new_input")
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) confirmPin = it },
                    label = { Text(copy.pinConfirmPrompt) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manage_pin_confirm_input")
                )
                if (!errorMsg.isNullOrBlank()) {
                    Text(errorMsg ?: "", color = CrimsonExpense, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!PinSecurityManager.isValidPinFormat(newPin)) {
                        errorMsg = copy.pinLengthRule
                        return@Button
                    }
                    if (newPin != confirmPin) {
                        errorMsg = copy.pinMismatchError
                        return@Button
                    }
                    if (mode == PinDialogMode.CHANGE_PIN) {
                        scope.launch {
                            val ok = onVerifyAndChangePin(currentPin, newPin)
                            if (ok) onDismiss() else errorMsg = copy.pinWrongError
                        }
                    } else {
                        onResetPin(newPin)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = ChampagneGold),
                modifier = Modifier.testTag("manage_pin_confirm_btn")
            ) {
                Text(copy.save)
            }
        },
        dismissButton = {
            Row {
                if (mode == PinDialogMode.RESET_PIN) {
                    TextButton(
                        onClick = {
                            onResetPin(null)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("clear_pin_completely_btn")
                    ) {
                        Text(
                            if (language == AppLanguage.BENGALI) "পিন মুছুন" else "Disable PIN",
                            color = CrimsonExpense
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(copy.cancel)
                }
            }
        }
    )
}
