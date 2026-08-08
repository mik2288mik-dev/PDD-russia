package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuestionDiagramView(diagramType: String?, modifier: Modifier = Modifier) {
    if (diagramType == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (diagramType) {
                "intersection_priority" -> IntersectionDiagram()
                "traffic_sign_warning" -> TrafficSignDiagram()
                "pedestrian_crossing" -> PedestrianCrossingDiagram()
                "overtaking_road" -> OvertakingDiagram()
                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Ситуация на дороге",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Схема дорожной ситуации",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntersectionDiagram() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val roadWidth = 60.dp.toPx()

        val asphalt = Color(0xFF334155)
        val roadLine = Color(0xFFF8FAFC)
        val carBlue = Color(0xFF3B82F6)
        val carRed = Color(0xFFEF4444)

        // Vertical Road
        drawRect(
            color = asphalt,
            topLeft = Offset((width - roadWidth) / 2, 0f),
            size = Size(roadWidth, height)
        )

        // Horizontal Road
        drawRect(
            color = asphalt,
            topLeft = Offset(0f, (height - roadWidth) / 2),
            size = Size(width, roadWidth)
        )

        // Center dashed lines
        drawLine(
            color = roadLine,
            start = Offset(width / 2, 0f),
            end = Offset(width / 2, (height - roadWidth) / 2),
            strokeWidth = 3f
        )
        drawLine(
            color = roadLine,
            start = Offset(width / 2, (height + roadWidth) / 2),
            end = Offset(width / 2, height),
            strokeWidth = 3f
        )

        // Blue Car (bottom coming up)
        drawRoundRect(
            color = carBlue,
            topLeft = Offset((width / 2) + 6.dp.toPx(), height - 40.dp.toPx()),
            size = Size(20.dp.toPx(), 30.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )

        // Red Car (right coming left)
        drawRoundRect(
            color = carRed,
            topLeft = Offset(width - 40.dp.toPx(), (height / 2) - 26.dp.toPx()),
            size = Size(30.dp.toPx(), 20.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
}

@Composable
fun TrafficSignDiagram() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFDC2626)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "STOP",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Знак 2.5 «Движение без остановки запрещено»",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PedestrianCrossingDiagram() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Road
        drawRect(
            color = Color(0xFF334155),
            topLeft = Offset(0f, 20.dp.toPx()),
            size = Size(width, height - 40.dp.toPx())
        )

        // Zebra lines
        val stripeWidth = 16.dp.toPx()
        val stripeGap = 12.dp.toPx()
        var currentX = width / 2 - 40.dp.toPx()
        while (currentX < width / 2 + 40.dp.toPx()) {
            drawRect(
                color = Color.White,
                topLeft = Offset(currentX, 20.dp.toPx()),
                size = Size(stripeWidth, height - 40.dp.toPx())
            )
            currentX += stripeWidth + stripeGap
        }

        // Car approaching
        drawRoundRect(
            color = Color(0xFF10B981),
            topLeft = Offset(30.dp.toPx(), height / 2 - 12.dp.toPx()),
            size = Size(36.dp.toPx(), 24.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
}

@Composable
fun OvertakingDiagram() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Two lane road
        drawRect(
            color = Color(0xFF334155),
            topLeft = Offset(0f, 20.dp.toPx()),
            size = Size(width, height - 40.dp.toPx())
        )

        // Center broken line
        var x = 0f
        while (x < width) {
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(x, height / 2),
                end = Offset(x + 20.dp.toPx(), height / 2),
                strokeWidth = 3f
            )
            x += 35.dp.toPx()
        }

        // Blue Car (top lane - overtaking trajectory)
        drawRoundRect(
            color = Color(0xFF3B82F6),
            topLeft = Offset(width / 2 - 20.dp.toPx(), 30.dp.toPx()),
            size = Size(32.dp.toPx(), 20.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )

        // Slow Truck (bottom lane)
        drawRoundRect(
            color = Color(0xFF64748B),
            topLeft = Offset(width / 3, height - 50.dp.toPx()),
            size = Size(48.dp.toPx(), 22.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
}
