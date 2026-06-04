package com.punchlist.app.data.model

import androidx.compose.ui.graphics.Color

enum class Priority(val label: String, val color: Color) {
    LOW("Low", Color(0xFF4CAF50)),
    MEDIUM("Medium", Color(0xFFFF9800)),
    HIGH("High", Color(0xFFF44336)),
    URGENT("Urgent", Color(0xFF9C27B0));

    companion object {
        fun fromString(value: String): Priority =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}
