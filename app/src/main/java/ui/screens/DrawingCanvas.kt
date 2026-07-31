package com.example.dedsec.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

private data class DrawStroke(val points: List<Offset>, val color: Color, val width: Float)

@Composable
fun DrawingOverlay(
    backgroundBitmap: Bitmap? = null,
    onSave: (fileName: String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    var strokes by remember { mutableStateOf(listOf<DrawStroke>()) }
    var currentPoints by remember { mutableStateOf(listOf<Offset>()) }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var selectedWidth by remember { mutableStateOf(10f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val colors = listOf(
        Color.Black, Color.White, Color.Red, Color(0xFFFFA500),
        Color.Yellow, Color.Green, Color.Blue, Color.Magenta
    )
    val brushSizes = listOf(3f, 8f, 16f, 28f, 44f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(">> DRAW_MODE", color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.White)
                        .clickable { strokes = emptyList() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("CLEAR", color = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .border(1.dp, Color(0xFFFF003C))
                        .clickable { onClose() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("CLOSE X", color = Color(0xFFFF003C))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .onSizeChanged { canvasSize = it }
        ) {
            backgroundBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedColor, selectedWidth) {
                        detectDragGestures(
                            onDragStart = { offset -> currentPoints = listOf(offset) },
                            onDrag = { change, _ -> currentPoints = currentPoints + change.position },
                            onDragEnd = {
                                if (currentPoints.size > 1) {
                                    strokes = strokes + DrawStroke(currentPoints, selectedColor, selectedWidth)
                                }
                                currentPoints = emptyList()
                            }
                        )
                    }
            ) {
                val toDraw = strokes + if (currentPoints.size > 1)
                    listOf(DrawStroke(currentPoints, selectedColor, selectedWidth)) else emptyList()
                toDraw.forEach { stroke ->
                    val path = Path()
                    stroke.points.firstOrNull()?.let { path.moveTo(it.x, it.y) }
                    stroke.points.drop(1).forEach { path.lineTo(it.x, it.y) }
                    drawPath(
                        path = path,
                        color = stroke.color,
                        style = Stroke(width = stroke.width, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            brushSizes.forEach { size ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { selectedWidth = size }
                        .border(if (selectedWidth == size) 2.dp else 1.dp, Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size((size / 2).dp).background(Color.White))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color)
                        .border(if (selectedColor == color) 2.dp else 1.dp, Color.White)
                        .clickable { selectedColor = color }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(1.dp, Color.White)
                .clickable {
                    val width = canvasSize.width.coerceAtLeast(1)
                    val height = canvasSize.height.coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val androidCanvas = AndroidCanvas(bitmap)
                    if (backgroundBitmap != null) {
                        androidCanvas.drawBitmap(
                            Bitmap.createScaledBitmap(backgroundBitmap, width, height, true),
                            0f, 0f, null
                        )
                    } else {
                        androidCanvas.drawColor(android.graphics.Color.WHITE)
                    }
                    val paint = Paint().apply {
                        isAntiAlias = true
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                    }
                    strokes.forEach { stroke ->
                        paint.color = stroke.color.toArgb()
                        paint.strokeWidth = stroke.width
                        val path = android.graphics.Path()
                        stroke.points.firstOrNull()?.let { path.moveTo(it.x, it.y) }
                        stroke.points.drop(1).forEach { path.lineTo(it.x, it.y) }
                        androidCanvas.drawPath(path, paint)
                    }
                    val fileName = "drawing_${System.currentTimeMillis()}.png"
                    FileOutputStream(File(context.filesDir, fileName)).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    onSave(fileName)
                }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("SAVE DRAWING", color = Color.White)
        }
    }
}