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
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.viewmodel.SegmentResult
import com.example.newcollage.viewmodel.SegmentViewModel

class SegmentActivity : ComponentActivity() {

    companion object {

        private const val KEY_URI = "key_uri"
        fun startNewInstance(context: Context, uri: Uri) {
            val intent = Intent(context, SegmentActivity::class.java)
            intent.putExtra(KEY_URI, uri)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.getParcelableExtra<Uri>(KEY_URI) ?: return
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SegmentScreen(
                        uri,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
//                        viewModel = viewModel
                    )
                }
            }
        }
    }

}

@Composable
private fun SegmentScreen(
    uri: Uri,
    modifier: Modifier = Modifier,
    viewModel: SegmentViewModel = viewModel()
) {
    val context = LocalContext.current
    viewModel.requestSegment(context, uri)

    val result = viewModel.segmentResult.collectAsState()
    SegmentView(segmentResult = result.value, modifier = modifier)
}

@Composable
private fun SegmentView(segmentResult: SegmentResult<Bitmap>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        when (segmentResult) {
            is SegmentResult.Failed -> {
                Text(
                    text = segmentResult.errorMessage ?: "unknown error",
                    modifier = Modifier.fillMaxSize()
                )
            }

            is SegmentResult.Loading -> {
                Text(text = "Loading", modifier = Modifier.fillMaxSize())
            }

            is SegmentResult.Success -> {
                val bitmap = segmentResult.data ?: return@Column
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
