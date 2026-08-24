package com.johnny.statusbar_control_eink.screen

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import com.johnny.statusbar_control_eink.prefs.SettingsPrefs

sealed class ScreenTimeoutResult {
    data object Success : ScreenTimeoutResult()
    data object NeedsWriteSettingsPermission : ScreenTimeoutResult()
}

/**
 * Toggles between the device's normal auto-lock timeout and "never lock"
 * (screen stays on indefinitely) via Settings.System.SCREEN_OFF_TIMEOUT.
 * Writing this setting requires the WRITE_SETTINGS special permission, which
 * — like Notification Policy Access — must be granted by the user in a
 * dedicated system settings screen, not via a normal runtime prompt.
 */
class ScreenTimeoutController(context: Context) {

    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver

    fun hasWriteSettingsPermission(): Boolean = Settings.System.canWrite(appContext)

    fun writeSettingsPermissionIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, "package:${appContext.packageName}".toUri())

    fun currentTimeoutMs(): Int =
        Settings.System.getInt(resolver, Settings.System.SCREEN_OFF_TIMEOUT, DEFAULT_TIMEOUT_MS)

    fun isNeverLock(): Boolean = currentTimeoutMs() >= NEVER_LOCK_TIMEOUT_MS

    fun setNeverLock(enable: Boolean, prefs: SettingsPrefs): ScreenTimeoutResult {
        if (!hasWriteSettingsPermission()) return ScreenTimeoutResult.NeedsWriteSettingsPermission
        return try {
            if (enable) {
                val current = currentTimeoutMs()
                if (current < NEVER_LOCK_TIMEOUT_MS) prefs.savedScreenTimeoutMs = current
                Settings.System.putInt(resolver, Settings.System.SCREEN_OFF_TIMEOUT, NEVER_LOCK_TIMEOUT_MS)
            } else {
                val restore = prefs.savedScreenTimeoutMs.takeIf { it in 1 until NEVER_LOCK_TIMEOUT_MS } ?: DEFAULT_TIMEOUT_MS
                Settings.System.putInt(resolver, Settings.System.SCREEN_OFF_TIMEOUT, restore)
            }
            ScreenTimeoutResult.Success
        } catch (_: SecurityException) {
            ScreenTimeoutResult.NeedsWriteSettingsPermission
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 30_000
        const val NEVER_LOCK_TIMEOUT_MS = Int.MAX_VALUE
    }
}
