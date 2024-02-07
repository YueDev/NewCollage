package com.example.newcollage.compose

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.view.FlowActivity
import com.example.newcollage.view.hilt.ui.HiltActivity
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {

    private val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PermissionX.init(this)
            .permissions(permissions)
            .request { allGranted, _, _ ->
                if (allGranted) setContent()
            }
    }

    private fun setContent() {
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainHome(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}


@Composable
fun MainHome(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        items(buttonDataList) {
            Button(onClick = {
                val intent = Intent(context, it.second)
                context.startActivity(intent)
            }) {
                Text(text = it.first)
            }
        }
    }
}


val buttonDataList = listOf(
    "Gallery" to SelectPhotoActivity::class.java,
    "Compose" to ComposeActivity::class.java,
    "CodeLab" to CodeLabActivity::class.java,
    "MySoothe" to MySootheActivity::class.java,
    "State" to StateActivity::class.java,
    "Animation" to AnimationActivity::class.java,
    "TestLayout" to TestLayoutActivity::class.java,
    "Flow(XML)" to FlowActivity::class.java,
    "Hilt(XML)" to HiltActivity::class.java,
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    NewCollageTheme {
        MainHome()
    }
}