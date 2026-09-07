package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val dfCurrency2 = DecimalFormat("#,##0.00")
    private val dfCurrency4 = DecimalFormat("#,##0.0000")
    private val dfPercent = DecimalFormat("+0.00%;-0.00%")
    private val dfNumber = DecimalFormat("#,##0.##")

    fun formatPrice(price: Double): String {
        return when {
            price >= 1000.0 -> dfCurrency2.format(price)
            price >= 1.0 -> dfCurrency2.format(price)
            price > 0.0 -> dfCurrency4.format(price)
            else -> "0.00"
        }
    }

    fun formatCurrency(amount: Double): String {
        return "$${dfCurrency2.format(amount)}"
    }

    fun formatPercent(percent: Double): String {
        return dfPercent.format(percent / 100.0)
    }

    fun formatNumber(number: Double): String {
        return dfNumber.format(number)
    }

    fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0) return "N/A"
        val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        if (timestamp <= 0) return "N/A"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
