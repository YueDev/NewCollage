package com.example.newcollage.compose

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import coil3.Image
import coil3.ImageLoader
import coil3.asDrawable
import coil3.compose.asPainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.bumptech.glide.Glide
import com.example.newcollage.compose.ui.theme.NewCollageTheme

class TimelineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimelineScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TimelineScreen(modifier: Modifier = Modifier) {

}

@Composable
fun TimelineItem(@DrawableRes drawableResId: Int, modifier: Modifier = Modifier) {

    val context = LocalContext.current


    var result by remember { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(drawableResId) {
        val imageLoader = ImageLoader.Builder(context)
            .allowHardware(false) // 禁用硬件位图，确保可以操作
            .build()
        val request = ImageRequest.Builder(context)
            .data(drawableResId)
            .size(36)
            .build()
        val image = (imageLoader.execute(request)).image?.asDrawable(context.resources)
        result = image
    }

    Canvas(modifier = modifier) {
        val androidCanvas = drawContext.canvas.nativeCanvas
        
    }
}



@Preview(showBackground = true, widthDp = 420, heightDp = 800)
@Composable
fun TimelineScreenPreview() {
    NewCollageTheme {
        TimelineScreen()
    }
}