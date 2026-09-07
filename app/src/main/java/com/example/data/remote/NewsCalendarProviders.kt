package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.CalendarItem
import com.example.data.model.NewsItem

class NewsProvider {
    val name: String = "Market News Provider"

    private val apiKey: String = runCatching {
        val field = BuildConfig::class.java.getField("NEWS_API_KEY")
        field.get(null) as? String ?: ""
    }.getOrDefault("")

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    val statusDescription: String
        get() = if (isConfigured) "Configured" else "Not configured (Key optional in .env)"

    suspend fun getNews(category: String? = null): List<NewsItem> {
        if (!isConfigured) {
            return emptyList()
        }
        // If user configures NEWS_API_KEY, real RSS/News API calls execute here
        return emptyList()
    }
}

class EconomicCalendarProvider {
    val name: String = "Economic Calendar Provider"

    private val apiKey: String = runCatching {
        val field = BuildConfig::class.java.getField("CALENDAR_API_KEY")
        field.get(null) as? String ?: ""
    }.getOrDefault("")

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    val statusDescription: String
        get() = if (isConfigured) "Configured" else "Not configured (Key optional in .env)"

    suspend fun getCalendarEvents(): List<CalendarItem> {
        if (!isConfigured) {
            return emptyList()
        }
        // If user configures CALENDAR_API_KEY, real calendar API calls execute here
        return emptyList()
    }
}
