package com.churchtrack.app.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DISPLAY_FORMAT = "dd/MM/yyyy"
    const val MONTH_YEAR_FORMAT = "yyyy-MM"
    const val FULL_DISPLAY_FORMAT = "EEEE dd MMMM yyyy"

    private val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    private val displayFormatter = SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault())
    private val monthYearFormatter = SimpleDateFormat(MONTH_YEAR_FORMAT, Locale.getDefault())
    private val fullDisplayFormatter = SimpleDateFormat(FULL_DISPLAY_FORMAT, Locale("fr"))

    fun today(): String = dateFormatter.format(Date())
    fun currentMonthYear(): String = monthYearFormatter.format(Date())
    fun currentYear(): String = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

    fun toDisplayFormat(dateStr: String): String {
        return try {
            val date = dateFormatter.parse(dateStr) ?: return dateStr
            displayFormatter.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun toFullDisplayFormat(dateStr: String): String {
        return try {
            val date = dateFormatter.parse(dateStr) ?: return dateStr
            fullDisplayFormatter.format(date).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun fromDisplayFormat(displayDate: String): String {
        return try {
            val date = displayFormatter.parse(displayDate) ?: return displayDate
            dateFormatter.format(date)
        } catch (e: Exception) {
            displayDate
        }
    }

    fun firstDayOfMonth(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return dateFormatter.format(cal.time)
    }

    fun lastDayOfMonth(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return dateFormatter.format(cal.time)
    }

    fun firstDayOfYear(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        return dateFormatter.format(cal.time)
    }

    fun lastDayOfYear(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, Calendar.DECEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 31)
        return dateFormatter.format(cal.time)
    }

    fun formatCurrency(amount: Double): String {
        return String.format("%,.0f FCFA", amount)
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
