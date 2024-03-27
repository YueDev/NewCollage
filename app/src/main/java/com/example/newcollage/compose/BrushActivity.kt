package com.example.newcollage.compose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.example.newcollage.compose.ui.theme.NewCollageTheme


//根据手指绘制彩色图案

class BrushActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BrushScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BrushScreen(modifier: Modifier = Modifier) {
    MyCanvas(modifier = modifier.fillMaxSize())
}

@Composable
fun MyCanvas(modifier: Modifier = Modifier) {


    var path by remember {
        mutableStateOf(Path())
    }

    var drawPath by remember {
        mutableStateOf(Path())
    }

    Canvas(modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = {
                path = Path()
                path.moveTo(it.x, it.y)
            },
            onDragEnd = {
                drawPath = path
            },
            onDragCancel = {
                drawPath = path
            }
        ) { change, offset ->
            path.lineTo(offset.x, offset.y)
            change.consume()
        }
    }) {
        DrawScope
        drawIntoCanvas {
            drawPath(drawPath, Color.Red)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BrushScreenPreview() {
    NewCollageTheme {
        BrushScreen()
    }
}