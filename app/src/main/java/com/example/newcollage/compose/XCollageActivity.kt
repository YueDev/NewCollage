package com.example.newcollage.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.util.getImageBitmap
import kotlinx.coroutines.flow.flow

class XCollageActivity : ComponentActivity() {

    companion object {
        const val KEY_URIS = "key_uris"
        fun startNewInstance(context: Context, uris: ArrayList<Uri>) {
            val intent = Intent(context, XCollageActivity::class.java)
            intent.putExtra(KEY_URIS, uris)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val uris = intent.getParcelableArrayListExtra<Uri>(KEY_URIS)
        if (uris.isNullOrEmpty()) return
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    XCollageScreen(
                        uris = uris, modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun XCollageScreen(
    uris: List<Uri>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    //1 根据uris加载可绘制的内容，Uri -> Bitmap -> ImageBitmap
    //2 1结束后，拿到XCollageView的长宽比，然后初始化collage 得到最初的collage result
    //3 每当xcollage的size改变，都collage一下
    //4 如果uris改变，重复1-3
    //5 加一个button，点击后re collage

    XCollageView(modifier = modifier.onSizeChanged {

    })
}

private fun loadImages(context: Context, uris: List<Uri>) = flow {
    val list = uris.map {
        getImageBitmap(context, it) ?: return@flow
    }.map {
        it.asImageBitmap()
    }
    emit(list)
}


@Composable
fun XCollageView(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Color.Blue)
    }
}


@Preview(showBackground = true)
@Composable
private fun XCollageScreenPreview() {
    NewCollageTheme {

    }
}