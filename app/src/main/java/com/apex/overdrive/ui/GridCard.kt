package com.apex.overdrive.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GridCard(
    icon: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "sc"
    )
    val bc by animateColorAsState(
        targetValue = if (pressed) accent.copy(alpha = 0.9f) else C.Border,
        animationSpec = tween(150),
        label = "bc"
    )

    Box(
        modifier
            .fillMaxWidth()
            .height(130.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        C.BgSurface,
                        C.BgElevated,
                        accent.copy(alpha = if (pressed) 0.15f else 0.03f)
                    )
                )
            )
            .border(
                width = if (pressed) 1.5.dp else 1.dp,
                color = bc,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 28.sp)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(6.dp)
                        .background(accent, RoundedCornerShape(3.dp))
                )
            }
            Column {
                Text(
                    title,
                    color = C.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    subtitle,
                    color = C.TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .width(2.dp)
                .height(36.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            accent.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    RoundedCornerShape(1.dp)
                )
        )
    }
}
