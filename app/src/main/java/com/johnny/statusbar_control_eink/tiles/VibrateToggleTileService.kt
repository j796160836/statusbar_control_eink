package com.johnny.statusbar_control_eink.tiles

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.johnny.statusbar_control_eink.R
import com.johnny.statusbar_control_eink.audio.RingerModeController
import com.johnny.statusbar_control_eink.audio.RingerToggleResult

/**
 * Toggles ring <-> vibrate in place, with no detail activity — a boolean
 * flip doesn't justify a full-screen e-ink transition the way the volume
 * sliders do.
 */
class VibrateToggleTileService : TileService() {

    private lateinit var ringerModeController: RingerModeController
    private var receiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        ringerModeController = RingerModeController(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
        val r = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateTile()
            }
        }
        ContextCompat.registerReceiver(
            this,
            r,
            IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        receiver = r
    }

    override fun onStopListening() {
        receiver?.let { unregisterReceiver(it) }
        receiver = null
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        when (ringerModeController.toggle()) {
            is RingerToggleResult.Success -> updateTile()
            is RingerToggleResult.NeedsNotificationPolicyAccess -> {
                Toast.makeText(this, R.string.needs_notification_policy_access, Toast.LENGTH_LONG).show()
                val intent = ringerModeController.policyAccessSettingsIntent()
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val pendingIntent = PendingIntent.getActivity(
                        this,
                        102,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    startActivityAndCollapse(pendingIntent)
                } else {
                    @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
                    startActivityAndCollapse(intent)
                }
            }
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val isVibrate = ringerModeController.isVibrate()
        val stateLabel = getString(if (isVibrate) R.string.state_vibrate else R.string.state_normal)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = stateLabel
        } else {
            tile.label = "${getString(R.string.tile_vibrate)}: $stateLabel"
        }
        tile.state = if (isVibrate) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
