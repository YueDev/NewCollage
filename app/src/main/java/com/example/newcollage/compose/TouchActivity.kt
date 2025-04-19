package com.example.newcollage.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.repository.ImageRepository
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class TouchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TouchScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TouchScreen(modifier: Modifier = Modifier) {
    Touch2(modifier = modifier)
}

@Composable
fun Touch2(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        GestureImage(
            drawableResId = ImageRepository.imageResIds.random(),
            modifier = Modifier.width(192.dp)
        )

        GestureImage(
            drawableResId = ImageRepository.imageResIds.random(),
            modifier = Modifier.width(192.dp)
        )
    }
}

//最好的办法是graphicsLayer在pointerInput之前
//这样多张图片的触摸就好处理了
@Composable
fun GestureImage(@DrawableRes drawableResId: Int, modifier: Modifier = Modifier) {
    var translation by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var origin by remember { mutableStateOf(TransformOrigin(0.5f, 0.5f)) }
    Image(
        painter = painterResource(id = drawableResId),
        contentDescription = "",
        modifier = modifier
            .graphicsLayer {
                translationX = translation.x
                translationY = translation.y
                scaleX = scale
                scaleY = scale
                transformOrigin = origin
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, gesPan, gesZoom, gesRotation ->
                    scale *= gesZoom
                    translation += gesPan * scale
                }
            }
    )
}


//这是google官方给的例子 图片能跟着手势运动 很完美
// 缺点就是需要fillmaxsize，并且transformOrigin设置为0，0
// 不好判断落点是不是在图片内部了
// 并且多张图片再一起很难判断
@Composable
fun Touch1(modifier: Modifier = Modifier) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var angle by remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = ImageRepository.imageResIds.random()),
            contentDescription = "",
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .pointerInput(Unit) {
                    detectTransformGestures(
                        onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                            val oldScale = zoom
                            val newScale = zoom * gestureZoom

                            offset = (offset + centroid / oldScale).rotateBy(gestureRotate) -
                                    (centroid / newScale + pan / oldScale)
                            zoom = newScale
                            angle += gestureRotate
                        }
                    )
                }
                .graphicsLayer {
                    translationX = -offset.x * zoom
                    translationY = -offset.y * zoom
                    scaleX = zoom
                    scaleY = zoom
                    rotationZ = angle
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        )
    }
}

fun Offset.rotateBy(angle: Float): Offset {
    val angleInRadians = angle * PI / 180
    return Offset(
        (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
        (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview5() {
    NewCollageTheme {
        TouchScreen()
    }
}