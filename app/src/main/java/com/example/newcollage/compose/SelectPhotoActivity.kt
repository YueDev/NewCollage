package com.example.newcollage.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.viewmodel.PhotoViewModel

class SelectPhotoActivity : ComponentActivity() {
    private val viewModel by viewModels<PhotoViewModel>()


    companion object {

        const val KEY_DST = "key_dst"

        //0 segment activity
        //1 sugject segmetation activity
        fun startNewInstance(context: Context, dst: Int) {
            val intent = Intent(context, SelectPhotoActivity::class.java)
            intent.putExtra(KEY_DST, dst)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dst = intent.getIntExtra(KEY_DST, 0)
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SelectPhotoScreen(
                        dst = dst,
                        modifier = Modifier.padding(innerPadding), viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectPhotoScreen(
    dst: Int,
    modifier: Modifier = Modifier,
    viewModel: PhotoViewModel = viewModel()
) {
    val uris by viewModel.urisStateFlow.collectAsState()
    val context = LocalContext.current

    Gallery(uris = uris, modifier = modifier) {
        SubjectSegmentationActivity.startNewInstance(context, it)
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Gallery(
    modifier: Modifier = Modifier, uris: List<Uri>, onSelectUri: (Uri) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(2.dp),
        modifier = modifier
    ) {
        items(uris) {
            GlideImage(model = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1.0f)
                    .clickable { onSelectUri(it) })
        }
    }
}