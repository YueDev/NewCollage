package com.example.newcollage.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.repository.ImageRepository
import com.example.newcollage.util.getImageBitmap
import com.example.newcollage.util.saveToDisk
import com.example.newcollage.util.shareImage
import kotlinx.coroutines.launch
import java.util.UUID

class SaveBitmapActivity : ComponentActivity() {

    private val imageResId = ImageRepository.imageResIds.random()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SaveBitmapScreen(
                        id = imageResId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SaveBitmapScreen(@DrawableRes id: Int, modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = id),
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .weight(100f)
//                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Button(
            onClick = {
                scope.launch {
                    val bitmap = getImageBitmap(context, id)
                    val fileName = UUID.randomUUID().toString()
                    bitmap?.saveToDisk(context, fileName)?.let {
                        shareImage(context, it)
                    }
                }
            },
            modifier = modifier.padding(8.dp)
        ) {
            Text(text = "Save bitmap")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
    NewCollageTheme {
        SaveBitmapScreen(id = ImageRepository.imageResIds.random())
    }
}