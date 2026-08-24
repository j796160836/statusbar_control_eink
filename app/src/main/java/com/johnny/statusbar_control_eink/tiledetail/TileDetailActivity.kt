package com.johnny.statusbar_control_eink.tiledetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.johnny.statusbar_control_eink.R
import com.johnny.statusbar_control_eink.audio.TileControlType
import com.johnny.statusbar_control_eink.audio.VolumeController
import com.johnny.statusbar_control_eink.audio.VolumeStream
import com.johnny.statusbar_control_eink.ui.components.EinkOutlinedSlider
import com.johnny.statusbar_control_eink.ui.theme.EinkBlack
import com.johnny.statusbar_control_eink.ui.theme.EinkWhite
import com.johnny.statusbar_control_eink.ui.theme.Statusbar_control_einkTheme

/**
 * Translucent dialog-themed activity launched from the volume tiles via
 * startActivityAndCollapse — shows a real drag slider, which a QS tile
 * itself cannot provide.
 */
class TileDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controlType = TileControlType.valueOf(
            intent.getStringExtra(EXTRA_CONTROL_TYPE) ?: TileControlType.MEDIA_VOLUME.name
        )
        val stream = when (controlType) {
            TileControlType.MEDIA_VOLUME -> VolumeStream.MEDIA
            TileControlType.RING_VOLUME -> VolumeStream.RING
        }
        val label = when (controlType) {
            TileControlType.MEDIA_VOLUME -> getString(R.string.dashboard_media_volume)
            TileControlType.RING_VOLUME -> getString(R.string.dashboard_ring_volume)
        }

        val volumeController = VolumeController(this)

        setContent {
            Statusbar_control_einkTheme {
                VolumeDialogContent(
                    label = label,
                    initialValue = volumeController.getVolume(stream),
                    range = volumeController.getMinVolume(stream)..volumeController.getMaxVolume(stream),
                    onValueChange = { volumeController.setVolume(stream, it) },
                    onDismiss = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_CONTROL_TYPE = "control_type"
    }
}

@Composable
private fun VolumeDialogContent(
    label: String,
    initialValue: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableIntStateOf(initialValue) }

    val dismissInteractionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EinkBlack.copy(alpha = 0.15f))
            .clickable(interactionSource = dismissInteractionSource, indication = null, onClick = onDismiss)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = EinkWhite,
            border = androidx.compose.foundation.BorderStroke(2.dp, EinkBlack),
            modifier = Modifier
                .width(280.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                EinkOutlinedSlider(
                    value = value,
                    valueRange = range,
                    onValueChange = {
                        value = it
                        onValueChange(it)
                    }
                )
                Text(
                    text = "$value / ${range.last}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
