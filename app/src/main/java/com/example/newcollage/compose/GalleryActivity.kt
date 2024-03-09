package com.example.newcollage.compose

import android.graphics.drawable.shapes.RoundRectShape
import android.health.connect.ReadRecordsRequestUsingIds
import android.media.Image
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.newcollage.R
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.repository.GalleryRepository
import com.example.newcollage.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

//相册 支持多选
class GalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GalleryScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GalleryScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    var images: List<ImageModel> by remember {
        mutableStateOf(listOf())
    }

    LaunchedEffect(key1 = Unit) {
        GalleryRepository().loadMediaData(context).flowOn(Dispatchers.IO)
            .collect { uriList ->
                val newImageList = uriList.map { uri ->
                    images.find { it.uri == uri } ?: ImageModel(uri)
                }
                images = newImageList
            }
    }

    Gallery(
        images = images,
        onClick = { uri ->
            //点击图片 修改images
            val list = images.toMutableList()
            val index = list.indexOfFirst { it.uri == uri }
            val selectedImage = list.getOrNull(index) ?: return@Gallery
            val newImage = selectedImage.copy(selected = !selectedImage.selected)
            list.removeAt(index)
            list.add(index, newImage)
            images = list
        },
        modifier = modifier.fillMaxSize()
    )
}


@Composable
private fun Gallery(
    images: List<ImageModel>,
    onClick: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(112.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(2.dp),
        state = rememberLazyGridState(),
        modifier = modifier
    ) {
        items(images) { image ->
            GalleryItem(image, click = onClick)
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun GalleryItem(image: ImageModel, click: (Uri) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.aspectRatio(1.0f)) {
        GlideImage(
            model = image.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { click(image.uri) }
                .animateSelect(image.selected)
        )
        if (image.selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = "",
                modifier = Modifier
                    .padding(start = 4.dp, top = 4.dp)
                    .border(4.dp, color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

//相册的选中modifier
@Composable
fun Modifier.animateSelect(select: Boolean): Modifier {
    val progress by animateFloatAsState(targetValue = if (select) 1.0f else 0.0f)
    //scale 1.0: 0.8f  0.0: 1.0f
    val scale = 0.8f + (1.0f - progress) * 0.2f
    //角度 ：  0.0  0dp  1.0 24dp
    val cornerSize = (progress * 24.0f).dp
    val shape = RoundedCornerShape(cornerSize)

    return this then Modifier
        .graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
        }
        .clip(shape = shape)
}


// 不知道为什么data class是不稳定的，需要加Stable
@Stable
private data class ImageModel(
    val uri: Uri,
    val selected: Boolean = false
)

@Composable
@Preview(showBackground = true)
fun IconPreview() {
    Icon(
        imageVector = Icons.Filled.Check,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 4.dp, top = 4.dp)
            .border(4.dp, color = Color.Red, shape = CircleShape)
            .padding(4.dp)
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.Cyan),
        contentDescription = ""
    )
}
