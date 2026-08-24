package com.johnny.statusbar_control_eink.tiles

import com.johnny.statusbar_control_eink.R
import com.johnny.statusbar_control_eink.audio.TileControlType
import com.johnny.statusbar_control_eink.audio.VolumeStream

class RingVolumeTileService : BaseVolumeTileService() {
    override val stream = VolumeStream.RING
    override val controlType = TileControlType.RING_VOLUME
    override val requestCode = 101
    override val tileIconRes = R.drawable.ic_tile_ring_volume
}
