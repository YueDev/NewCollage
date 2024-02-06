package com.example.newcollage.compose

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.newcollage.compose.ui.theme.ComposeTheme
import com.example.newcollage.viewmodel.GalleryViewModel

class GalleryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme(showStatusBarInEdgeToEdgeMode = false) {
                GalleryScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

val imageUrls = listOf(
    "https://imgg.cos16.com/images/fzly/123/1%20%284%29.jpg",
    "https://imgg.cos16.com/meitu/201-300/235/235%20%284%29.jpg",
    "https://imgg.cos16.com/images/fzly/124/1%20%282%29.jpg",
    "https://imgg.cos16.com/meitu/101-200/0182/182%20%281%29.jpg",
    "https://imgg.cos16.com/meitu/101-200/0182/182%20%2810%29.jpg",
    "https://imgg.cos16.com/images/mtyh/vol/054/1%20%281%29.jpg",
    "https://imgg.cos16.com/images/other/qisikuai/005/1%20%281%29.jpg",
    "https://imgg.cos16.com/images/snzx/vol008/1%20%282%29.jpg",
    "https://imgg.cos16.com/images/slct/X/013/1%20%282%29.jpg",
    "https://imgg.cos16.com/images/slct/SSR/005/1%20%281%29.jpg",
    "https://imgg.cos16.com/images/other/lmmd/003/1%20%285%29.jpg",
    "https://imgg.cos16.com/images/other/slbzd/010/1%20%283%29.jpg",
    "https://imgg.cos16.com/meitu/201-300/210/210%20%286%29.jpg",
    "https://www.aiweifulishe.net/wp-content/uploads/2022/01/%E9%93%83%E6%9C%A8%E7%BE%8E%E5%92%B2Misaki-Suzuki-%E2%80%93-%E9%A3%8E%E9%87%8E%E7%81%AF%E7%BB%87-4.jpg",
    "https://www.aiweifulishe.net/wp-content/uploads/2023/06/%E9%93%83%E6%9C%A8%E7%BE%8E%E5%92%B2-%E9%AD%94%E5%A5%B3%E7%9A%84%E8%AF%95%E7%82%BC-%E4%B8%89%E9%83%A8%E6%9B%B2-12.jpg",
).let {
    it + it + it + it + it
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GalleryScreen(
    modifier: Modifier = Modifier,
    viewModel: GalleryViewModel = viewModel()
) {

    val uris by viewModel.urisStateFlow.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(2.dp),
        modifier = modifier
    ) {
        items(uris) {
            GlideImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.aspectRatio(1.0f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
    ComposeTheme {
        GalleryScreen()
    }
}