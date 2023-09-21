package com.topmortar.topmortarsales.commons.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormat {
    fun format(amount: Double): String {
        val localeID = Locale("id", "ID") // Create a Locale for Indonesia (ID)
        val currencyFormat = NumberFormat.getCurrencyInstance(localeID)
        return currencyFormat.format(amount).replace("Rp", "Rp ").replace(",00", "")
    }
}