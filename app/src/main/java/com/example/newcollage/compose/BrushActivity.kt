package com.example.newcollage.compose

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.tooling.preview.Preview
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BrushActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BrushViewModel.reset()
                    BrushComposable(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

object BrushViewModel {

    private val _paths = MutableStateFlow<List<Path>>(emptyList())
    val paths: StateFlow<List<Path>> = _paths.asStateFlow()

    fun reset() {
        _paths.value = emptyList()
    }

    fun addNewPath(x: Float, y: Float) {
        val path = Path().apply {
            moveTo(x, y)
        }
        _paths.value += path
    }

    fun addPointToPath(x: Float, y: Float) {
        val oldPath = _paths.value.lastOrNull() ?: return
        val newPath = Path().apply {
            asAndroidPath().set(oldPath.asAndroidPath())
        }
        newPath.lineTo(x, y)
        val pathList = paths.value.toMutableList()
        pathList.removeLast()
        pathList.add(newPath)
        _paths.value = pathList
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BrushComposable(modifier: Modifier = Modifier) {

    val viewModel = BrushViewModel

    val paths by viewModel.paths.collectAsState()

    val stroke = Stroke(width = 4.0f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    Canvas(modifier = modifier
        .fillMaxSize()
        .background(Color.LightGray)
        .pointerInteropFilter {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    viewModel.addNewPath(it.x, it.y)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    viewModel.addPointToPath(it.x, it.y)
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    viewModel.addPointToPath(it.x, it.y)
                    true
                }

                else -> false
            }
        }
    ) {
        paths.forEach {
            drawPath(it, Color.Red, style = stroke)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun BrushComposablePreview() {
    NewCollageTheme {
        BrushComposable()
    }
}