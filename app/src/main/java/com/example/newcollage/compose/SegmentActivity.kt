package com.example.newcollage.compose

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.viewmodel.SegmentViewModel
import java.net.URI

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
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun SegmentScreen(uri: Uri, modifier: Modifier = Modifier) {
    GlideImage(model = uri, contentDescription = null, modifier = modifier)
}


