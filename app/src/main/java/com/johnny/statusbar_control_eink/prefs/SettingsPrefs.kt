package com.johnny.statusbar_control_eink.prefs

import android.content.Context
import androidx.core.content.edit
import com.johnny.statusbar_control_eink.notification.NotificationLayoutStyle

class SettingsPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("settings", Context.MODE_PRIVATE)

    var notificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_NOTIFICATION_ENABLED, value) }

    var resumeOnBoot: Boolean
        get() = prefs.getBoolean(KEY_RESUME_ON_BOOT, false)
        set(value) = prefs.edit { putBoolean(KEY_RESUME_ON_BOOT, value) }

    var notificationLayoutStyle: NotificationLayoutStyle
        get() = prefs.getString(KEY_LAYOUT_STYLE, null)
            ?.let { saved -> NotificationLayoutStyle.entries.firstOrNull { it.name == saved } }
            ?: NotificationLayoutStyle.BUTTONS_SPLIT
        set(value) = prefs.edit { putString(KEY_LAYOUT_STYLE, value.name) }

    /** The screen-off timeout (ms) to restore when leaving "never lock" mode; -1 if never saved. */
    var savedScreenTimeoutMs: Int
        get() = prefs.getInt(KEY_SAVED_SCREEN_TIMEOUT, -1)
        set(value) = prefs.edit { putInt(KEY_SAVED_SCREEN_TIMEOUT, value) }

    private companion object {
        const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        const val KEY_RESUME_ON_BOOT = "resume_on_boot"
        const val KEY_LAYOUT_STYLE = "notification_layout_style"
        const val KEY_SAVED_SCREEN_TIMEOUT = "saved_screen_timeout_ms"
    }
}
