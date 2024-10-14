package com.example.newcollage.compose

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.repository.GalleryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

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


@OptIn(ExperimentalMaterial3Api::class)
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

            val showFab by remember {
                derivedStateOf { images.any { it.selected } }
            }
            AnimatedVisibility(
                showFab,
                enter = scaleIn(initialScale = 0.25f),
                exit = scaleOut(targetScale = 0.25f) + fadeOut()
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

        val state = rememberLazyGridState()
        //是否显示遮照
        val showMask by remember {
            derivedStateOf {
                state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 48
            }
        }


        Box(modifier = modifier.padding(innerPadding)) {

            //相册
            Gallery(
                modifier = Modifier.fillMaxSize(),
                state = state,
                images = images,
                contentPadding = PaddingValues(start = 2.dp, end = 2.dp, top = 128.dp, bottom = 2.dp),
                onClick = { uri ->
                    //点击图片 修改images
                    val list = images.toMutableList()
                    val index = list.indexOfFirst { it.uri == uri }
                    val selectedImage = list.getOrNull(index) ?: return@Gallery
                    val newImage = selectedImage.copy(selected = !selectedImage.selected)
                    list.removeAt(index)
                    list.add(index, newImage)
                    images = list
                })

            //遮照
            AnimatedVisibility(
                visible = showMask,
                enter = fadeIn(animationSpec = tween(700)),
                exit = fadeOut(animationSpec = tween(700))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(192.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Black.copy(alpha = 0.0f)
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                )
            }

            var queryString by remember {
                mutableStateOf("")
            }

            //search bar
            SearchBar(
                query = queryString,
                onQueryChange = { queryString = it },
                onSearch = {},
                active = false,
                onActiveChange = {},
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Bar",
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
            ) {
            }
        }

    }
}


@Composable
private fun Gallery(
    modifier: Modifier = Modifier,
    images: List<ImageModel>,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(2.dp),
    onClick: (Uri) -> Unit,
) {
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Adaptive(112.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(images) { image ->
            GalleryItem(image = image, click = onClick)
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
                .animateSelect(image.selected)
        )
//        AsyncImage(
//            model = image.uri,
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxSize()
//                .animateSelect(image.selected)
//        )
        AnimatedVisibility(image.selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = "",
                modifier = Modifier
                    .padding(start = 4.dp, top = 4.dp)
                    .border(4.dp, color = MaterialTheme.colorScheme.background, shape = CircleShape)
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
