package com.example.moodscribbles.data.preferences

enum class ThemeMode(internal val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromStorageValue(raw: String?): ThemeMode =
            entries.find { it.storageValue == raw } ?: SYSTEM
    }
}
