package com.insperity.escmobile.util

import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.text.DecimalFormat
import java.util.*

/**
 * Provides static methods that format strings to various representations
 */
object StringUtil {

    fun numberToPhoneNumber(number: String) = if (number.length != 10) number else "(" + number.substring(0, 3) + ") " + number.substring(3, 6) + "-" + number.substring(6)

    fun hoursAndMinutes(minutes: Int): String {
        if (minutes == 0) return "0h"
        val hour = minutes / 60
        val minute = minutes % 60
        return String.format("%s %s", if (hour > 0) String.format("%sh", hour) else "", if (minute > 0) String.format("%sm", minute) else "").trim { it <= ' ' }
    }

    fun formattedTime(dateTime: DateTime): String = DateTimeFormat.forPattern("hh:mm a").print(dateTime)

    fun parseDateToCalendar(date: String): Date = DateTimeFormat.forPattern("MM/dd/yyyy").parseDateTime(date).toDate()

    fun decimalHours(hours: Double): String = DecimalFormat("0.##").format(hours)

    private fun decimalHours(minutes: Int, decimalPlaces: Int): String {
        val hrs = minutes.toDouble() / 60.toDouble()
        return String.format(Locale.US, "%.${decimalPlaces}f", hrs)
    }

    fun timeWithFormat(minutes: Int, decimalFormatted: Boolean = true, decimalPlaces: Int = 5): String {
        if (!decimalFormatted) {
            return hoursAndMinutes(minutes)
        }
        return decimalHours(minutes, decimalPlaces)
    }

    fun setSpanBetweenTokens(charSequence: CharSequence, token: String, vararg cs: CharacterStyle): CharSequence {
        var text = charSequence
        // Start and end refer to the points where the span will apply
        val tokenLen = token.length
        val start = text.toString().indexOf(token) + tokenLen
        val end = text.toString().indexOf(token, start)

        if (start > -1 && end > -1) {
            // Copy the spannable string to a mutable spannable string
            val ssb = SpannableStringBuilder(text)
            for (c in cs)
                ssb.setSpan(c, start, end, 0)

            // Delete the tokens before and after the span
            ssb.delete(end, end + tokenLen)
            ssb.delete(start - tokenLen, start)

            text = ssb
        }
        return text
    }
}
