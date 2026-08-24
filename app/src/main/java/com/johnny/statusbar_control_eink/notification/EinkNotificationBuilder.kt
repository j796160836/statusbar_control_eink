package com.johnny.statusbar_control_eink.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.johnny.statusbar_control_eink.R
import com.johnny.statusbar_control_eink.audio.NotificationActions
import com.johnny.statusbar_control_eink.audio.RingerModeController
import com.johnny.statusbar_control_eink.audio.VolumeController
import com.johnny.statusbar_control_eink.audio.VolumeStream
import com.johnny.statusbar_control_eink.prefs.SettingsPrefs
import com.johnny.statusbar_control_eink.screen.ScreenTimeoutController

/**
 * Single source of truth for the persistent notification's content. Both
 * [StatusBarControlService] (on external changes) and
 * [NotificationActionReceiver] (on self-triggered changes) call this, so the
 * two update paths can never diverge.
 */
object EinkNotificationBuilder {

    const val CHANNEL_ID = "status_bar_controls"
    const val NOTIFICATION_ID = 1

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun build(context: Context): android.app.Notification {
        ensureChannel(context)

        val volumeController = VolumeController(context)
        val ringerModeController = RingerModeController(context)
        val screenTimeoutController = ScreenTimeoutController(context)

        val mediaValue = volumeController.getVolume(VolumeStream.MEDIA)
        val mediaMax = volumeController.getMaxVolume(VolumeStream.MEDIA)
        val ringValue = volumeController.getVolume(VolumeStream.RING)
        val ringMax = volumeController.getMaxVolume(VolumeStream.RING)
        val isVibrate = ringerModeController.isVibrate()
        val isNeverLock = screenTimeoutController.isNeverLock()

        val collapsed = RemoteViews(context.packageName, R.layout.notification_volume_collapsed).apply {
            setTextViewText(
                R.id.text_summary,
                context.getString(
                    R.string.notification_summary,
                    volumeController.percent(VolumeStream.MEDIA),
                    volumeController.percent(VolumeStream.RING),
                    if (isVibrate) context.getString(R.string.state_vibrate) else context.getString(R.string.state_normal)
                )
            )
        }

        val layoutStyle = SettingsPrefs(context).notificationLayoutStyle

        val expanded = RemoteViews(context.packageName, layoutStyle.layoutRes).apply {
            setTextViewText(R.id.tv_media_value, context.getString(R.string.notification_media_label_value, mediaValue, mediaMax))
            setProgressBar(R.id.pb_media, mediaMax, mediaValue, false)
            setOnClickPendingIntent(R.id.btn_media_minus, actionPendingIntent(context, NotificationActions.MEDIA_VOLUME_DOWN, 1))
            setOnClickPendingIntent(R.id.btn_media_plus, actionPendingIntent(context, NotificationActions.MEDIA_VOLUME_UP, 2))

            setTextViewText(R.id.tv_ring_value, context.getString(R.string.notification_ring_label_value, ringValue, ringMax))
            setProgressBar(R.id.pb_ring, ringMax, ringValue, false)
            setOnClickPendingIntent(R.id.btn_ring_minus, actionPendingIntent(context, NotificationActions.RING_VOLUME_DOWN, 3))
            setOnClickPendingIntent(R.id.btn_ring_plus, actionPendingIntent(context, NotificationActions.RING_VOLUME_UP, 4))

            setOnClickPendingIntent(R.id.btn_ringer_normal, actionPendingIntent(context, NotificationActions.SET_RINGER_NORMAL, 5))
            setOnClickPendingIntent(R.id.btn_ringer_vibrate, actionPendingIntent(context, NotificationActions.SET_RINGER_VIBRATE, 6))
            styleSegmentedButton(this, R.id.btn_ringer_normal, active = !isVibrate)
            styleSegmentedButton(this, R.id.btn_ringer_vibrate, active = isVibrate)

            setOnClickPendingIntent(R.id.btn_screen_auto_lock, actionPendingIntent(context, NotificationActions.SET_SCREEN_AUTO_LOCK, 7))
            setOnClickPendingIntent(R.id.btn_screen_never_lock, actionPendingIntent(context, NotificationActions.SET_SCREEN_NEVER_LOCK, 8))
            styleSegmentedButton(this, R.id.btn_screen_auto_lock, active = !isNeverLock)
            styleSegmentedButton(this, R.id.btn_screen_never_lock, active = isNeverLock)
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tile_media_volume)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun refresh(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, build(context))
    }

    private fun actionPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).setAction(action)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    /** Active = inverted (black fill, white text); inactive = plain outline — no ambiguous bracket text. */
    private fun styleSegmentedButton(views: RemoteViews, viewId: Int, active: Boolean) {
        views.setInt(
            viewId,
            "setBackgroundResource",
            if (active) R.drawable.bg_eink_button_active else R.drawable.selector_eink_button
        )
        views.setTextColor(viewId, if (active) Color.WHITE else Color.BLACK)
    }
}
