package com.punchlist.app.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Timestamp.toRelativeString(): String {
    val now = System.currentTimeMillis()
    val diffMs = now - toDate().time
    return when {
        diffMs < TimeUnit.MINUTES.toMillis(1) -> "just now"
        diffMs < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diffMs)}m ago"
        diffMs < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diffMs)}h ago"
        diffMs < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diffMs)}d ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(toDate())
    }
}

fun Timestamp.toDisplayDate(): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(toDate())

fun Date.toDisplayDate(): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(this)
