package com.johnny.statusbar_control_eink.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.johnny.statusbar_control_eink.ui.theme.EinkBlack
import com.johnny.statusbar_control_eink.ui.theme.EinkGray
import com.johnny.statusbar_control_eink.ui.theme.EinkWhite

/**
 * White fill, black outline, no ripple/fill-color feedback — outline only,
 * since color/animation cues don't read reliably on e-ink.
 */
@Composable
fun EinkOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = if (enabled) EinkBlack else EinkGray
    val textColor = if (enabled) EinkBlack else EinkGray
    Box(
        modifier = modifier
            .background(EinkWhite)
            .border(BorderStroke(2.dp, borderColor))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold)
    }
}

/** `[ - ]   62%   [ + ]` — mirrors the notification's RemoteViews layout 1:1. */
@Composable
fun EinkStepper(
    label: String,
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    decrementEnabled: Boolean = true,
    incrementEnabled: Boolean = true
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        EinkOutlinedButton(text = "－", onClick = onDecrement, enabled = decrementEnabled, modifier = Modifier.width(64.dp))
        Text(text = valueText, fontWeight = FontWeight.Bold)
        EinkOutlinedButton(text = "＋", onClick = onIncrement, enabled = incrementEnabled, modifier = Modifier.width(64.dp))
    }
}

/** Bracket-style toggle: the active state is shown in `[brackets]`, never by color alone. */
@Composable
fun EinkToggleSwitch(
    isOn: Boolean,
    onLabel: String,
    offLabel: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .border(BorderStroke(2.dp, EinkBlack))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isOn) "[$onLabel]" else onLabel,
            fontWeight = if (isOn) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = if (!isOn) "[$offLabel]" else offLabel,
            fontWeight = if (!isOn) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * One row in a single-select group: inverted fill (black bg/white text) when
 * selected, plain outline otherwise — an unambiguous filled/unfilled state
 * instead of relying on bracket text.
 */
@Composable
fun EinkRadioOption(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (selected) EinkBlack else EinkWhite)
            .border(BorderStroke(2.dp, EinkBlack))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            color = if (selected) EinkWhite else EinkBlack,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Discrete-step slider (snaps to integer volume levels) with a bordered
 * rectangular thumb and outlined track instead of a filled circle/bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EinkOutlinedSlider(
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = (valueRange.last - valueRange.first - 1).coerceAtLeast(0)
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.toInt()) },
        valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
        steps = steps,
        modifier = modifier.fillMaxWidth(),
        colors = SliderDefaults.colors(
            thumbColor = EinkBlack,
            activeTrackColor = EinkBlack,
            inactiveTrackColor = EinkWhite,
            activeTickColor = EinkWhite,
            inactiveTickColor = EinkBlack
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(width = 8.dp, height = 28.dp)
                    .background(EinkWhite)
                    .border(BorderStroke(2.dp, EinkBlack))
            )
        },
        track = { _ ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(EinkWhite)
                    .border(BorderStroke(1.dp, EinkBlack))
            )
        }
    )
}
