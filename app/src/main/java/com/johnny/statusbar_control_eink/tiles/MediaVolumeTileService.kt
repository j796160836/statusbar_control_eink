package com.johnny.statusbar_control_eink.tiles

import com.johnny.statusbar_control_eink.R
import com.johnny.statusbar_control_eink.audio.TileControlType
import com.johnny.statusbar_control_eink.audio.VolumeStream

class MediaVolumeTileService : BaseVolumeTileService() {
    override val stream = VolumeStream.MEDIA
    override val controlType = TileControlType.MEDIA_VOLUME
    override val requestCode = 100
    override val tileIconRes = R.drawable.ic_tile_media_volume
}
