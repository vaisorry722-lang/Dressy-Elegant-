package com.example.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom

private val Context.dressyDataStore: DataStore<Preferences> by preferencesDataStore(name = "dressy_elegant_prefs")

data class PinAccountState(
    val hasPinConfigured: Boolean = false,
    val requirePinOnLaunch: Boolean = false,
    val setupPromptDismissed: Boolean = false
)

class PinSecurityManager(private val context: Context) {
    private val dataStore = context.applicationContext.dressyDataStore

    private val languageKey = stringPreferencesKey("app_language")
    private fun hashKey(uid: String) = stringPreferencesKey("pin_hash_$uid")
    private fun saltKey(uid: String) = stringPreferencesKey("pin_salt_$uid")
    private fun enabledKey(uid: String) = booleanPreferencesKey("pin_enabled_$uid")
    private fun dismissedKey(uid: String) = booleanPreferencesKey("pin_prompt_dismissed_$uid")
    private fun activeCompanyKey(uid: String) = stringPreferencesKey("active_company_$uid")

    val languageFlow: Flow<AppLanguage> = dataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[languageKey])
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { prefs ->
            prefs[languageKey] = language.code
        }
    }

    fun observeActiveCompanyId(uid: String): Flow<String?> = dataStore.data.map { prefs ->
        prefs[activeCompanyKey(uid)]
    }

    suspend fun setActiveCompanyId(uid: String, companyId: String?) {
        dataStore.edit { prefs ->
            if (companyId.isNullOrBlank()) {
                prefs.remove(activeCompanyKey(uid))
            } else {
                prefs[activeCompanyKey(uid)] = companyId
            }
        }
    }

    fun observePinState(uid: String): Flow<PinAccountState> = dataStore.data.map { prefs ->
        val hasHash = !prefs[hashKey(uid)].isNullOrBlank() && !prefs[saltKey(uid)].isNullOrBlank()
        PinAccountState(
            hasPinConfigured = hasHash,
            requirePinOnLaunch = hasHash && (prefs[enabledKey(uid)] ?: true),
            setupPromptDismissed = prefs[dismissedKey(uid)] ?: false
        )
    }

    suspend fun getPinState(uid: String): PinAccountState = observePinState(uid).first()

    suspend fun setPin(uid: String, rawPin: String): Boolean {
        if (!isValidPinFormat(rawPin)) return false
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val saltHex = toHex(saltBytes)
        val digestHex = computeSaltedHash(rawPin, saltBytes)
        dataStore.edit { prefs ->
            prefs[saltKey(uid)] = saltHex
            prefs[hashKey(uid)] = digestHex
            prefs[enabledKey(uid)] = true
            prefs[dismissedKey(uid)] = true
        }
        return true
    }

    suspend fun verifyPin(uid: String, rawPin: String): Boolean {
        if (!isValidPinFormat(rawPin)) return false
        val prefs = dataStore.data.first()
        val storedSalt = prefs[saltKey(uid)] ?: return false
        val storedHash = prefs[hashKey(uid)] ?: return false
        val saltBytes = fromHex(storedSalt) ?: return false
        val candidateHash = computeSaltedHash(rawPin, saltBytes)
        return MessageDigest.isEqual(
            storedHash.toByteArray(Charsets.UTF_8),
            candidateHash.toByteArray(Charsets.UTF_8)
        )
    }

    suspend fun setPinLockEnabled(uid: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[enabledKey(uid)] = enabled
        }
    }

    suspend fun dismissSetupPrompt(uid: String) {
        dataStore.edit { prefs ->
            prefs[dismissedKey(uid)] = true
        }
    }

    suspend fun resetPin(uid: String) {
        dataStore.edit { prefs ->
            prefs.remove(hashKey(uid))
            prefs.remove(saltKey(uid))
            prefs[enabledKey(uid)] = false
            prefs[dismissedKey(uid)] = false
        }
    }

    companion object {
        fun isValidPinFormat(pin: String): Boolean =
            pin.length in 4..6 && pin.all { it in '0'..'9' }

        internal fun computeSaltedHash(pin: String, salt: ByteArray): String {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(salt)
            val digest = md.digest(pin.toByteArray(Charsets.UTF_8))
            return toHex(digest)
        }

        private fun toHex(bytes: ByteArray): String =
            bytes.joinToString("") { "%02x".format(it) }

        private fun fromHex(hex: String): ByteArray? {
            if (hex.length % 2 != 0) return null
            return try {
                ByteArray(hex.length / 2) { i ->
                    hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }
            } catch (_: NumberFormatException) {
                null
            }
        }
    }
}
