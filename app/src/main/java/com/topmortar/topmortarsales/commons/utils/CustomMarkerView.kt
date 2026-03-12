package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.topmortar.topmortarsales.R

class CustomMarkerView(context: Context, layoutResource: Int, private val dates: List<Pair<Int, String>>)
    : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val index = e?.x?.toInt() ?: 0
        tvContent.text = """
            Total:
            ${e?.y?.toInt()} toko
            Terakhir diupdate:
            ${dates[index].second}
        """.trimIndent()
        super.refreshContent(e, highlight)
    }
}