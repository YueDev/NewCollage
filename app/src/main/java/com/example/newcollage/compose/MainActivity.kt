package com.example.newcollage.compose

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {

    private val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PermissionX.init(this)
            .permissions(permissions)
            .request { allGranted, _, _ ->
                if (allGranted) setContent()
            }
    }

    private fun setContent() {
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainHome(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}


@Composable
fun MainHome(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        items(buttonData) {

            Button(onClick = {
                when (it) {
                    "Test" -> context.startActivity(Intent(context, TestActivity::class.java))
                    "SubjectSegmentation" -> SelectPhotoActivity.startNewInstance(context, 1)
                    "SegmentationSelfie" -> SelectPhotoActivity.startNewInstance(context, 0)
                    "Gallery" -> context.startActivity(Intent(context, GalleryActivity::class.java))
                    "RuntimeShader" -> {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            Toast.makeText(context, "required API 33", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        context.startActivity(Intent(context, ShaderRuntimeActivity::class.java))
                    }
                    "Compose" -> context.startActivity(Intent(context, ComposeActivity::class.java))
                    "CodeLab" -> context.startActivity(Intent(context, CodeLabActivity::class.java))
                    "MySoothe" -> context.startActivity(Intent(context, MySootheActivity::class.java))
                    "State" -> context.startActivity(Intent(context, StateActivity::class.java))
                    "Animation" -> context.startActivity(Intent(context, AnimationActivity::class.java))
                    "TestLayout" -> context.startActivity(Intent(context, TestLayoutActivity::class.java))
                    "Recomposition" -> context.startActivity(Intent(context, RecompositionActivity::class.java))
                    "SaveBitmap" -> context.startActivity(Intent(context, SaveBitmapActivity::class.java))
                    "Touch" -> context.startActivity(Intent(context, TouchActivity::class.java))
                }
            }) {
                Text(text = it)
            }
        }
    }
}

val buttonData = listOf(
    "Test",
    "SubjectSegmentation",
    "SegmentationSelfie",
    "Gallery",
    "RuntimeShader",
    "Compose",
    "CodeLab",
    "MySoothe",
    "State",
    "Animation",
    "TestLayout",
    "Recomposition",
    "SaveBitmap",
    "Touch",
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    NewCollageTheme {
        MainHome()
    }
}