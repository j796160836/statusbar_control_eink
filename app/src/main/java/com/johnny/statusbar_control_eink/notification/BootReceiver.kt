package com.johnny.statusbar_control_eink.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.johnny.statusbar_control_eink.prefs.SettingsPrefs

/** Restarts the persistent notification after reboot, only if the user opted into both settings. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = SettingsPrefs(context)
        if (prefs.notificationEnabled && prefs.resumeOnBoot) {
            StatusBarControlService.start(context)
        }
    }
}
