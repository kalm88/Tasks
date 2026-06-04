package com.punchlist.app.data.model

import androidx.compose.ui.graphics.Color

enum class Status(val label: String, val color: Color) {
    OPEN("Open", Color(0xFF2196F3)),
    IN_PROGRESS("In Progress", Color(0xFFFF9800)),
    NEEDS_REVIEW("Needs Review", Color(0xFF9C27B0)),
    COMPLETE("Complete", Color(0xFF4CAF50));

    companion object {
        fun fromString(value: String): Status =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OPEN
    }
}
