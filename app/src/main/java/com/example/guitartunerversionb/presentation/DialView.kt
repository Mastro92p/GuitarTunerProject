package com.example.guitartunerversionb.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text

// Function to determine the color based on deviation
fun getDeviationColor(deviation: Float): Color {
    return when {
        deviation > 25 || deviation < -25 -> Color.Red
        deviation in 15f..25f || deviation in -25f..-15f -> Color(0xFFFFA500)
        deviation in 5f..15f || deviation in -15f..-5f -> Color.Yellow
        deviation in 1f..5f || deviation in -5f..-1f -> Color(0xFF9BFF00)
        else -> Color.Green
    }
}

@Composable
fun DialView(cents: Float, targetFrequency: Float = 440.0f) {
    val animatedCents = remember { Animatable(cents) }
    LaunchedEffect(cents) {
        animatedCents.animateTo(cents)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension / 2 - 1.4.dp.toPx()
        val centerX = center.x
        val centerY = center.y

        drawTuningArc(centerX, centerY, radius, getDeviationColor(animatedCents.value))
        drawMarkers(centerX, centerY, radius)
        drawDeviationLine(centerX, centerY, radius, animatedCents.value)
        drawFixedLine(centerX, centerY, radius)
    }
}


@Composable
fun CustomText(text: String, fontSize: TextUnit, fontWeight: FontWeight? = null) {
    Text(
        text = text,
        fontSize = fontSize,
        color = Color.White,
        fontWeight = fontWeight
    )
}

@Composable
fun FrequencyDisplay(note: String, octave: Int, frequency: Float, deviation: Float, cents: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Display the big note
        CustomText(text = note, fontSize = 64.sp, fontWeight = FontWeight.Bold)
        // Display note with octave in smaller font under the big note
        CustomText(text = "$note$octave", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        // Display frequency
        CustomText(text = "$frequency Hz", fontSize = 12.sp)
        // Display cents deviation
        CustomText(text = "${cents.toInt()} cents", fontSize = 12.sp)
    }
}

// Function to draw the main tuning arc
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTuningArc(
    centerX: Float,
    centerY: Float,
    radius: Float,
    color: Color
) {
    drawCircle(
        color = color,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 4.dp.toPx())
    )
}

// Function to draw markers around the arc
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMarkers(
    centerX: Float,
    centerY: Float,
    radius: Float
) {
    val lineCount = 71
    val lineLength = 5.dp.toPx()
    val linesOffset = 7.5.dp.toPx()
    val linesRadius = radius - linesOffset

    for (i in 0 until lineCount) {
        val angle = Math.toRadians((180 + i * (180f / (lineCount - 1))).toDouble()).toFloat()
        val lineStartX = centerX + linesRadius * cos(angle)
        val lineStartY = centerY + linesRadius * sin(angle)
        val lineEndX = lineStartX + lineLength * cos(angle)
        val lineEndY = lineStartY + lineLength * sin(angle)

        drawLine(
            color = Color.White,
            start = Offset(lineStartX, lineStartY),
            end = Offset(lineEndX, lineEndY),
            strokeWidth = 4.dp.toPx()
        )
    }
}

// Function to draw the moving deviation line
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDeviationLine(
    centerX: Float,
    centerY: Float,
    radius: Float,
    cents: Float
) {
    val maxCents = 50f
    val maxAngle = PI / 2
    val angle = (cents / maxCents) * maxAngle

    val lineOffset = 12.dp.toPx()
    val lineRadius = radius - lineOffset
    val lineLength = 20.dp.toPx()

    val lineX = centerX + (lineRadius - lineLength) * cos(PI / 2 - angle).toFloat()
    val lineY = centerY - (lineRadius - lineLength) * sin(PI / 2 - angle).toFloat()

    drawLine(
        color = getDeviationColor(cents),
        start = Offset(lineX, lineY),
        end = Offset(
            (lineX + lineLength * cos(PI / 2 - angle)).toFloat(),
            (lineY - lineLength * sin(PI / 2 - angle)).toFloat()
        ),
        strokeWidth = 4.dp.toPx()
    )
}

// Function to draw the fixed 12 o'clock line
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFixedLine(
    centerX: Float,
    centerY: Float,
    radius: Float
) {
    val fixedLineLength = 9.dp.toPx()
    val fixedLineOffset = 11.5.dp.toPx()
    val fixedLineX = centerX
    val fixedLineYStart = centerY - radius + fixedLineOffset
    val fixedLineYEnd = fixedLineYStart - fixedLineLength

    drawLine(
        color = Color.White,
        start = Offset(fixedLineX, fixedLineYStart),
        end = Offset(fixedLineX, fixedLineYEnd),
        strokeWidth = 4.dp.toPx()
    )
}
