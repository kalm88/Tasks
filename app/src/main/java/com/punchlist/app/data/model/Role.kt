package com.punchlist.app.data.model

enum class Role {
    ADMIN,
    MANAGER,
    WORKER;

    companion object {
        fun fromString(value: String): Role =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: WORKER
    }
}
