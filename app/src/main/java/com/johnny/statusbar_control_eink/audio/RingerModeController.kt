package com.johnny.statusbar_control_eink.audio

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings

sealed class RingerToggleResult {
    data object Success : RingerToggleResult()
    data object NeedsNotificationPolicyAccess : RingerToggleResult()
}

/**
 * Wraps ringer-mode (Normal <-> Vibrate) reads/writes. Changing ringer mode
 * can throw SecurityException on Android 6+ depending on Notification Policy
 * Access / OEM DND restrictions, even for NORMAL<->VIBRATE — always check
 * isNotificationPolicyAccessGranted() first, and still catch the exception
 * as defense-in-depth.
 */
class RingerModeController(context: Context) {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun isVibrate(): Boolean = audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL

    fun hasPolicyAccess(): Boolean = notificationManager.isNotificationPolicyAccessGranted

    fun policyAccessSettingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

    fun toggle(): RingerToggleResult = setVibrate(!isVibrate())

    fun setVibrate(vibrate: Boolean): RingerToggleResult {
        if (!hasPolicyAccess()) return RingerToggleResult.NeedsNotificationPolicyAccess
        val target = if (vibrate) AudioManager.RINGER_MODE_VIBRATE else AudioManager.RINGER_MODE_NORMAL
        return try {
            audioManager.ringerMode = target
            RingerToggleResult.Success
        } catch (_: SecurityException) {
            RingerToggleResult.NeedsNotificationPolicyAccess
        }
    }
}
