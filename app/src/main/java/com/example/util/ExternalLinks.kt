package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object ExternalLinks {
    // User-provided official ecosystem URLs
    const val APP_1_PLAY_STORE = "https://play.google.com/store/apps/details?id=co.median.android.mbawodo"
    const val APP_2_PLAY_STORE = "https://play.google.com/store/apps/details?id=co.median.android.eezlxez"
    const val DEVELOPER_PAGE = "https://play.google.com/store/apps/dev?id=8617006322141073240"
    const val EXNESS_PARTNER_LINK = "https://one.exnessonelink.com/a/vtkbbmje"

    const val DEVELOPER_NAME = "FX Signal Lab"
    const val APP_NAME = "AI Trading Toolkit – Signals, Scanner & Risk"

    fun openUrlSafely(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
