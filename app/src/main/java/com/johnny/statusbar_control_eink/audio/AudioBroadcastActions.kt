package com.johnny.statusbar_control_eink.audio

/** Actions the notification's buttons send to [com.johnny.statusbar_control_eink.notification.NotificationActionReceiver]. */
object NotificationActions {
    private const val PREFIX = "com.johnny.statusbar_control_eink.action"

    const val MEDIA_VOLUME_UP = "$PREFIX.MEDIA_VOLUME_UP"
    const val MEDIA_VOLUME_DOWN = "$PREFIX.MEDIA_VOLUME_DOWN"
    const val RING_VOLUME_UP = "$PREFIX.RING_VOLUME_UP"
    const val RING_VOLUME_DOWN = "$PREFIX.RING_VOLUME_DOWN"
    const val SET_RINGER_NORMAL = "$PREFIX.SET_RINGER_NORMAL"
    const val SET_RINGER_VIBRATE = "$PREFIX.SET_RINGER_VIBRATE"
    const val SET_SCREEN_AUTO_LOCK = "$PREFIX.SET_SCREEN_AUTO_LOCK"
    const val SET_SCREEN_NEVER_LOCK = "$PREFIX.SET_SCREEN_NEVER_LOCK"
}

/**
 * Not part of the public SDK — `AudioManager` has no exposed constant for this
 * action, but the system has broadcast it on every stream volume change since
 * early Android and continues to for compatibility, so third-party apps have
 * always had to hardcode the string themselves.
 */
object SystemAudioBroadcasts {
    const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
}

/** Control type shown by [com.johnny.statusbar_control_eink.tiledetail.TileDetailActivity]. */
enum class TileControlType {
    MEDIA_VOLUME,
    RING_VOLUME
}
