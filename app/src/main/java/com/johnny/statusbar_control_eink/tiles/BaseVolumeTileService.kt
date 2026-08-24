package com.johnny.statusbar_control_eink.tiles

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import com.johnny.statusbar_control_eink.R
import com.johnny.statusbar_control_eink.audio.SystemAudioBroadcasts
import com.johnny.statusbar_control_eink.audio.TileControlType
import com.johnny.statusbar_control_eink.audio.VolumeController
import com.johnny.statusbar_control_eink.audio.VolumeStream
import com.johnny.statusbar_control_eink.tiledetail.TileDetailActivity

/**
 * Shared onStartListening/onStopListening + tile-state logic for the two
 * volume tiles. Tapping opens [TileDetailActivity] for a real slider, since
 * a QS tile itself can only report discrete clicks, not a drag gesture.
 */
abstract class BaseVolumeTileService : TileService() {

    protected abstract val stream: VolumeStream
    protected abstract val controlType: TileControlType
    protected abstract val requestCode: Int

    private lateinit var volumeController: VolumeController
    private var receiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        volumeController = VolumeController(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
        val filter = IntentFilter(SystemAudioBroadcasts.VOLUME_CHANGED_ACTION)
        val r = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateTile()
            }
        }
        ContextCompat.registerReceiver(this, r, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        receiver = r
    }

    override fun onStopListening() {
        receiver?.let { unregisterReceiver(it) }
        receiver = null
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, TileDetailActivity::class.java)
            .putExtra(TileDetailActivity.EXTRA_CONTROL_TYPE, controlType.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }

    protected fun updateTile() {
        val tile = qsTile ?: return
        val percent = volumeController.percent(stream)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = "$percent%"
        } else {
            tile.label = "${getString(labelResFallback())}: $percent%"
        }
        tile.state = if (percent > 0) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.icon = Icon.createWithResource(this, tileIconRes)
        tile.updateTile()
    }

    private fun labelResFallback(): Int = when (controlType) {
        TileControlType.MEDIA_VOLUME -> R.string.tile_media_volume
        TileControlType.RING_VOLUME -> R.string.tile_ring_volume
    }

    protected abstract val tileIconRes: Int
}
