package com.johnny.statusbar_control_eink.notification

import com.johnny.statusbar_control_eink.R

/**
 * The 3 alternative expanded-notification designs, user-selectable from the
 * dashboard. RemoteViews can't report a live drag gesture back to the app, so
 * the "slider" variants show a non-interactive SeekBar as a visual level
 * indicator, with flanking [-]/[+] buttons doing the actual adjustment.
 */
enum class NotificationLayoutStyle(val displayNameRes: Int, val layoutRes: Int) {
    BUTTONS_SPLIT(
        R.string.notification_style_buttons_split,
        R.layout.notification_expanded_buttons_split
    ),
    SLIDER_SPLIT(
        R.string.notification_style_slider_split,
        R.layout.notification_expanded_slider_split
    ),
    SLIDER_STACKED(
        R.string.notification_style_slider_stacked,
        R.layout.notification_expanded_slider_stacked
    )
}
