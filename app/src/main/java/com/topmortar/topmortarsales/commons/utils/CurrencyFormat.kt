package com.topmortar.topmortarsales.commons.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormat {

    fun format(amount: Double): String {
        val localeID = Locale("id", "ID") // Create a Locale for Indonesia (ID)
        val currencyFormat = NumberFormat.getCurrencyInstance(localeID)
        var formattedAmmount = currencyFormat.format(amount).replace("Rp", "Rp ").replace(",00", "")
        if (amount < 0) {
            formattedAmmount = currencyFormat.format(amount).replace("-Rp", "Rp -").replace(",00", "")
        }
        return formattedAmmount
    }

}