package com.johnny.statusbar_control_eink.audio

import android.content.Context
import android.media.AudioManager

enum class VolumeStream(val streamType: Int) {
    MEDIA(AudioManager.STREAM_MUSIC),
    RING(AudioManager.STREAM_RING)
}

/**
 * Single point of contact for reading/writing media & ring stream volume.
 * Never adjust AudioManager directly from UI/tile/notification code — go
 * through here so all three surfaces (tiles, notification, dashboard) stay
 * in sync by construction.
 */
class VolumeController(context: Context) {

    private val audioManager = context.applicationContext
        .getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun getVolume(stream: VolumeStream): Int =
        audioManager.getStreamVolume(stream.streamType)

    fun getMaxVolume(stream: VolumeStream): Int =
        audioManager.getStreamMaxVolume(stream.streamType)

    fun getMinVolume(stream: VolumeStream): Int =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(stream.streamType)
        } else {
            0
        }

    fun setVolume(stream: VolumeStream, value: Int) {
        val clamped = value.coerceIn(getMinVolume(stream), getMaxVolume(stream))
        // FLAG_SHOW_UI intentionally omitted — our own UI is the volume UI.
        audioManager.setStreamVolume(stream.streamType, clamped, 0)
    }

    fun increment(stream: VolumeStream) {
        setVolume(stream, getVolume(stream) + 1)
    }

    fun decrement(stream: VolumeStream) {
        setVolume(stream, getVolume(stream) - 1)
    }

    fun percent(stream: VolumeStream): Int {
        val min = getMinVolume(stream)
        val max = getMaxVolume(stream)
        val range = (max - min).coerceAtLeast(1)
        return ((getVolume(stream) - min) * 100) / range
    }
}
