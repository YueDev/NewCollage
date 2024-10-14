package com.example.newcollage.compose

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.example.newcollage.repository.SubjectSegmentationHelper
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.repository.MyResult
import kotlinx.coroutines.flow.onCompletion


class SubjectSegmentationActivity : ComponentActivity() {

    companion object {
        const val KEY_URI = "key_uri_SubjectSegmentationActivity"
        fun startNewInstance(context: Context, uri: Uri) {
            val intent = Intent(context, SubjectSegmentationActivity::class.java)
            intent.putExtra(KEY_URI, uri)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val uri = intent.getParcelableExtra<Uri>(KEY_URI) ?: return
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SubjectSegmentationScreen(
                        uri = uri,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectSegmentationScreen(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var downloadModuleResult: MyResult<Int> by remember {
        mutableStateOf(MyResult.Loading())
    }

    //下载模型
    LaunchedEffect(key1 = true) {
        SubjectSegmentationHelper.downloadModule(context)
            .onCompletion {
                Log.d("YUEDEVTAG", "Completion: $it")
            }
            .collect {
                downloadModuleResult = it
            }
    }

    Column(modifier = modifier) {
        when (downloadModuleResult) {
            is MyResult.Failed -> Text(text = "Download Module error: ${downloadModuleResult.errorMessage}")
            is MyResult.Loading -> Text(text = "Downloading Module...")
            is MyResult.Success -> SubjectSegmentationView(uri = uri)
        }
    }

}

@Composable
private fun SubjectSegmentationView(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var segmentResult: MyResult<Bitmap> by remember {
        mutableStateOf(MyResult.Loading())
    }

    LaunchedEffect(key1 = uri) {
        SubjectSegmentationHelper.segment(context, uri)
            .collect {
                segmentResult = it
            }

    }

    SegmentView(segmentResult = segmentResult, modifier = modifier)
}

@Composable
fun SegmentView(segmentResult: MyResult<Bitmap>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        when (segmentResult) {
            is MyResult.Failed -> {
                Text(
                    text = segmentResult.errorMessage ?: "unknown error",
                    modifier = Modifier.fillMaxSize()
                )
            }

            is MyResult.Loading -> {
                Text(text = "Segment...", modifier = Modifier.fillMaxSize())
            }

            is MyResult.Success -> {
                val bitmap = segmentResult.data ?: return@Column
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.errorContainer)
                )
            }
        }
    }
}

