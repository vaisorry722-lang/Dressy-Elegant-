package com.example.localization

import com.example.model.AppLanguage
import com.google.firebase.Timestamp
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

object BdtFormatter {
    private val dhakaTz: TimeZone = TimeZone.getTimeZone("Asia/Dhaka")

    fun formatBdt(amount: Double, language: AppLanguage = AppLanguage.BENGALI): String {
        val isNegative = amount < 0
        val absValue = abs(amount)
        val rounded = absValue.toLong()
        val formattedNumber = formatBangladeshiGrouping(rounded)
        val localized = if (language == AppLanguage.BENGALI) {
            toBengaliDigits(formattedNumber)
        } else {
            formattedNumber
        }
        return if (isNegative) "-৳$localized" else "৳$localized"
    }

    fun formatCount(value: Int, language: AppLanguage = AppLanguage.BENGALI): String {
        val raw = value.toString()
        return if (language == AppLanguage.BENGALI) toBengaliDigits(raw) else raw
    }

    fun formatDhakaDateTime(timestamp: Timestamp?, language: AppLanguage = AppLanguage.BENGALI): String {
        val date = timestamp?.toDate() ?: return if (language == AppLanguage.BENGALI) "এখনই" else "Just now"
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).apply {
            timeZone = dhakaTz
        }
        return sdf.format(date)
    }

    fun formatDhakaShortDate(timestamp: Timestamp?): String {
        val date = timestamp?.toDate() ?: return "--"
        val sdf = SimpleDateFormat("dd MMM", Locale.ENGLISH).apply {
            timeZone = dhakaTz
        }
        return sdf.format(date)
    }

    private fun formatBangladeshiGrouping(value: Long): String {
        val s = value.toString()
        if (s.length <= 3) return s
        val lastThree = s.substring(s.length - 3)
        val rest = s.substring(0, s.length - 3)
        val groupedRest = rest.reversed().chunked(2).joinToString(",").reversed()
        return "$groupedRest,$lastThree"
    }

    fun toBengaliDigits(input: String): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
