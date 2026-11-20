package com.example

import com.example.localization.BdtFormatter
import com.example.model.AppLanguage
import com.example.model.Permission
import com.example.model.SubscriptionPlan
import com.example.model.UserRole
import com.example.security.PinSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DressyUnitTest {

    @Test
    fun bdtFormatterFormatsSouthAsianGrouping() {
        assertEquals("৳1,25,000", BdtFormatter.formatBdt(125000.0, AppLanguage.ENGLISH))
        assertEquals("৳১,২৫,০০০", BdtFormatter.formatBdt(125000.0, AppLanguage.BENGALI))
        assertEquals("-৳5,500", BdtFormatter.formatBdt(-5500.0, AppLanguage.ENGLISH))
    }

    @Test
    fun pinFormatAndSaltedHashVerificationWork() {
        assertTrue(PinSecurityManager.isValidPinFormat("1234"))
        assertTrue(PinSecurityManager.isValidPinFormat("482910"))
        assertFalse(PinSecurityManager.isValidPinFormat("123"))
        assertFalse(PinSecurityManager.isValidPinFormat("12ab"))

        val salt1 = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val salt2 = byteArrayOf(8, 7, 6, 5, 4, 3, 2, 1)
        val hash1 = PinSecurityManager.computeSaltedHash("4829", salt1)
        val hash1Again = PinSecurityManager.computeSaltedHash("4829", salt1)
        val hash2 = PinSecurityManager.computeSaltedHash("4829", salt2)

        assertEquals(hash1, hash1Again)
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun rbacPermissionsRestrictFinanceAccessFromKarigorAndEmployee() {
        assertTrue(UserRole.COMPANY_OWNER.has(Permission.VIEW_FINANCES))
        assertTrue(UserRole.ACCOUNTANT.has(Permission.WRITE_FINANCES))
        assertFalse(UserRole.KARIGOR.has(Permission.VIEW_FINANCES))
        assertFalse(UserRole.EMPLOYEE.has(Permission.WRITE_FINANCES))
        assertFalse(UserRole.SUPER_ADMIN in UserRole.assignableRoles)
    }

    @Test
    fun subscriptionPaymentNumberMatchesConfiguredAccount() {
        assertEquals("301620556052", SubscriptionPlan.PAYMENT_NUMBER)
    }
}
