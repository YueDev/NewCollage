package com.example.newcollage.compose

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.repository.GalleryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import java.util.ArrayList

//相册 支持多选
class GalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                GalleryScreen(modifier = Modifier.fillMaxSize())
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

    LaunchedEffect(Unit) {
        GalleryRepository().loadMediaData(context).flowOn(Dispatchers.IO).collect { uriList ->
            val newImageList = uriList.map { uri ->
                images.find { it.uri == uri } ?: ImageModel(uri)
            }
            images = newImageList
        }
    }

    Scaffold(
        floatingActionButton = {
            val showFab = images.any { it.selected }
            AnimatedVisibility(
                showFab, enter = scaleIn(initialScale = 0.25f), exit = scaleOut(targetScale = 0.25f) + fadeOut()
            ) {
                FloatingActionButton(onClick = {
                    val uris = images.filter { it.selected }.map { it.uri }
                    if (uris.isEmpty()) return@FloatingActionButton
                    XCollageActivity.startNewInstance(context, ArrayList(uris))
                }) { Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null) }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Gallery(
            images = images, onClick = { uri ->
                //点击图片 修改images
                val list = images.toMutableList()
                val index = list.indexOfFirst { it.uri == uri }
                val selectedImage = list.getOrNull(index) ?: return@Gallery
                val newImage = selectedImage.copy(selected = !selectedImage.selected)
                list.removeAt(index)
                list.add(index, newImage)
                images = list
            },
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}


@Composable
private fun Gallery(
    images: List<ImageModel>, onClick: (Uri) -> Unit, modifier: Modifier = Modifier
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
    Box(modifier = modifier
        .aspectRatio(1.0f)
        .clickable { click(image.uri) }) {
        GlideImage(
            model = image.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .animateSelect(image.selected)
        )
        AnimatedVisibility(image.selected) {
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
    val progress by animateFloatAsState(
        animationSpec = spring(stiffness = Spring.StiffnessMedium), targetValue = if (select) 1.0f else 0.0f, label = ""
    )
    //scale 1.0: 0.8f  0.0: 1.0f
    val scale = 0.8f + (1.0f - progress) * 0.2f
    //角度 ：  0.0  0dp  1.0 24dp
    val cornerSize = (progress * 24.0f).dp
    val shape = roundedCornerShape(cornerSize)
    return this then Modifier
        .graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
        }
        .clip(shape = shape)
}

@Composable
private fun roundedCornerShape(cornerSize: Dp) = RoundedCornerShape(cornerSize)


// 不知道为什么data class是不稳定的，需要加Stable
@Stable
private data class ImageModel(
    val uri: Uri, val selected: Boolean = false
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
