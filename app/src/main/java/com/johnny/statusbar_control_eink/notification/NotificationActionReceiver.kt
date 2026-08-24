package com.johnny.statusbar_control_eink.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.johnny.statusbar_control_eink.R
import com.johnny.statusbar_control_eink.audio.NotificationActions
import com.johnny.statusbar_control_eink.audio.RingerModeController
import com.johnny.statusbar_control_eink.audio.RingerToggleResult
import com.johnny.statusbar_control_eink.audio.VolumeController
import com.johnny.statusbar_control_eink.audio.VolumeStream
import com.johnny.statusbar_control_eink.prefs.SettingsPrefs
import com.johnny.statusbar_control_eink.screen.ScreenTimeoutController
import com.johnny.statusbar_control_eink.screen.ScreenTimeoutResult

/**
 * Handles taps on the persistent notification's buttons. Manifest-registered
 * (not dynamic) so it keeps working even if the app process/service has
 * already died — Android starts a fresh process to deliver the broadcast.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val volumeController = VolumeController(context)
        val ringerModeController = RingerModeController(context)
        val screenTimeoutController = ScreenTimeoutController(context)
        val prefs = SettingsPrefs(context)

        when (intent.action) {
            NotificationActions.MEDIA_VOLUME_UP -> volumeController.increment(VolumeStream.MEDIA)
            NotificationActions.MEDIA_VOLUME_DOWN -> volumeController.decrement(VolumeStream.MEDIA)
            NotificationActions.RING_VOLUME_UP -> volumeController.increment(VolumeStream.RING)
            NotificationActions.RING_VOLUME_DOWN -> volumeController.decrement(VolumeStream.RING)
            NotificationActions.SET_RINGER_NORMAL ->
                handleRingerResult(context, ringerModeController.setVibrate(false))
            NotificationActions.SET_RINGER_VIBRATE ->
                handleRingerResult(context, ringerModeController.setVibrate(true))
            NotificationActions.SET_SCREEN_AUTO_LOCK ->
                handleScreenTimeoutResult(context, screenTimeoutController.setNeverLock(false, prefs))
            NotificationActions.SET_SCREEN_NEVER_LOCK ->
                handleScreenTimeoutResult(context, screenTimeoutController.setNeverLock(true, prefs))
        }

        // Update immediately rather than waiting for the AudioManager broadcast
        // round-trip; that broadcast will also fire and re-refresh, which is
        // harmless since both paths converge on the same builder.
        EinkNotificationBuilder.refresh(context)
    }

    private fun handleRingerResult(context: Context, result: RingerToggleResult) {
        when (result) {
            is RingerToggleResult.Success -> Unit
            is RingerToggleResult.NeedsNotificationPolicyAccess -> {
                Toast.makeText(context, R.string.needs_notification_policy_access, Toast.LENGTH_LONG).show()
                context.startActivity(
                    RingerModeController(context).policyAccessSettingsIntent()
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    private fun handleScreenTimeoutResult(context: Context, result: ScreenTimeoutResult) {
        when (result) {
            is ScreenTimeoutResult.Success -> Unit
            is ScreenTimeoutResult.NeedsWriteSettingsPermission -> {
                Toast.makeText(context, R.string.needs_write_settings_permission, Toast.LENGTH_LONG).show()
                context.startActivity(
                    ScreenTimeoutController(context).writeSettingsPermissionIntent()
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }
}
