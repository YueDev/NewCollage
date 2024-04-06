package com.example.newcollage.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.util.getImageBitmap
import com.example.newcollage.viewmodel.XCollageViewModel
import com.google.mlkit.vision.common.internal.ImageUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.zip
import kotlin.jvm.Throws

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

    val sizeFlow = remember {
        MutableStateFlow(IntSize.Zero)
    }

    LaunchedEffect(uris) {

    }


    XCollageView(modifier = modifier.onSizeChanged {
        sizeFlow.value = it
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