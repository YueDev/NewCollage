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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newcollage.repository.SubjectSegmentationHelper
import com.example.newcollage.ui.theme.NewCollageTheme
import com.example.newcollage.viewmodel.SegmentResult
import com.example.newcollage.viewmodel.SegmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class SubjectSegmentationActivity : ComponentActivity() {

    companion object {
        fun startNewInstance(context: Context, uri: Uri) {
            val intent = Intent(context, SubjectSegmentationActivity::class.java)
            intent.putExtra(SegmentActivity.KEY_URI, uri)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val uri = intent.getParcelableExtra<Uri>(SegmentActivity.KEY_URI) ?: return
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

    var segmentResult: SegmentResult<Bitmap> by remember {
        mutableStateOf(SegmentResult.Loading())
    }


    LaunchedEffect(key1 = uri) {
        SubjectSegmentationHelper.segment(context, uri)
            .flowOn(Dispatchers.Default)
            .collect {
                segmentResult = it
            }
    }

    SegmentView(segmentResult = segmentResult, modifier = modifier)
}

