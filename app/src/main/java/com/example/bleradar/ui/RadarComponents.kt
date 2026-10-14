package com.example.bleradar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bleradar.model.SignalZone

@Composable
internal fun SignalBadge(zone: SignalZone) {
    val color = zone.color()
    Text(
        text = zone.label,
        modifier = Modifier.background(color.copy(alpha = 0.11f), RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = color,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
internal fun SignalBars(rssi: Int, modifier: Modifier = Modifier) {
    val zone = com.example.bleradar.model.Proximity.zone(rssi)
    val active = when (zone) {
        SignalZone.VERY_CLOSE -> 5
        SignalZone.CLOSE -> 4
        SignalZone.GOOD -> 3
        SignalZone.WEAK -> 2
        SignalZone.VERY_WEAK -> 1
        SignalZone.LOST -> 0
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        repeat(5) { index ->
            Box(
                Modifier.width(4.dp).height((8 + index * 4).dp)
                    .background(
                        if (index < active) zone.color() else RadarColors.border,
                        RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}

@Composable
internal fun SmallDot(color: Color) {
    Box(Modifier.width(7.dp).height(7.dp).background(color, RoundedCornerShape(50)))
}
