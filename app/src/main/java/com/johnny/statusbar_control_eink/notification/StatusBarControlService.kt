package com.johnny.statusbar_control_eink.notification

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.johnny.statusbar_control_eink.audio.SystemAudioBroadcasts

/**
 * Keeps the persistent status-bar notification alive and refreshes it when
 * volume/ringer mode changes externally (hardware rocker, another app, the
 * system Settings screen). Self-triggered changes are refreshed immediately
 * by [NotificationActionReceiver]; this receiver is the catch-all for
 * everything else.
 */
class StatusBarControlService : Service() {

    private var receiverRegistered = false

    private val audioChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            EinkNotificationBuilder.refresh(context)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(SystemAudioBroadcasts.VOLUME_CHANGED_ACTION)
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(this, audioChangeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        receiverRegistered = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(EinkNotificationBuilder.NOTIFICATION_ID, EinkNotificationBuilder.build(this))
        return START_STICKY
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(audioChangeReceiver)
            receiverRegistered = false
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, StatusBarControlService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, StatusBarControlService::class.java))
        }
    }
}
