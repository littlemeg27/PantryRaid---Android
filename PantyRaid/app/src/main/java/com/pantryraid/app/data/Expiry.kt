package com.onhand.app.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object Expiry {
    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun parseDay(raw: String): Long? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        return runCatching { format.parse(trimmed)?.time }.getOrNull()
    }

    fun formatDay(epochMs: Long?): String {
        if (epochMs == null) return ""
        return format.format(Date(epochMs))
    }

    fun state(epochMs: Long?): ExpiryState {
        if (epochMs == null) return ExpiryState.None
        val days = TimeUnit.MILLISECONDS.toDays(epochMs - System.currentTimeMillis())
        return when {
            days < 0 -> ExpiryState.Expired
            days <= 3 -> ExpiryState.Soon
            else -> ExpiryState.Fresh
        }
    }

    fun label(epochMs: Long?): String? {
        if (epochMs == null) return null
        val day = formatDay(epochMs)
        return when (state(epochMs)) {
            ExpiryState.Expired -> "Expired $day"
            ExpiryState.Soon -> "Use by $day"
            ExpiryState.Fresh -> "Expires $day"
            ExpiryState.None -> null
        }
    }
}
