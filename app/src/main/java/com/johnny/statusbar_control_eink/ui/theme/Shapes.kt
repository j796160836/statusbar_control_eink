package com.johnny.statusbar_control_eink.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Deliberately boxy — no pill/circle shapes, which read poorly on e-ink and
// are harder to distinguish from filled shapes at low contrast.
val EinkShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(2.dp),
    extraLarge = RoundedCornerShape(0.dp)
)
