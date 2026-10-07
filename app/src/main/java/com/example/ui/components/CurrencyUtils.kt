package com.example.ui.components

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyUtils {
    private val localeId = Locale("in", "ID")
    private val rupiahFormat = NumberFormat.getCurrencyInstance(localeId).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatRupiah(amount: Double): String {
        return try {
            rupiahFormat.format(amount).replace("Rp", "Rp ")
        } catch (_: Exception) {
            "Rp " + String.format(Locale.US, "%,.0f", amount).replace(',', '.')
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", localeId)
        return sdf.format(Date(timestamp))
    }
}
