package com.topmortar.topmortarsales.commons.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormat {

    fun format(amount: Double, withCurrency: Boolean = true): String {
        val localeID = Locale("id", "ID") // Create a Locale for Indonesia (ID)
        val currencyFormat = NumberFormat.getCurrencyInstance(localeID)

        val formattedAmount = if (withCurrency) {
            if (amount < 0) {
                currencyFormat.format(amount).replace("-Rp", "Rp -").replace(",00", "")
            } else currencyFormat.format(amount).replace("Rp", "Rp ").replace(",00", "")
        } else {
            if (amount < 0) {
                currencyFormat.format(amount).replace("-Rp", "-").replace(",00", "")
            } else currencyFormat.format(amount).replace("Rp", "").replace(",00", "")
        }

        return formattedAmount
    }

}