package com.example.newcollage.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import com.example.newcollage.viewmodel.XCollageViewModel
import kotlin.jvm.Throws

class XCollageActivity : ComponentActivity() {

    companion object {
        const val KEY_URIS = "key_uris"
        fun startNewInstance(context: Context, uris: ArrayList<Uri>) {
            val intent = Intent(context, XCollageActivity::class.java)
            intent.putExtra(KEY_URIS, uris)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val uris = intent.getParcelableArrayListExtra<Uri>(KEY_URIS)
        if (uris.isNullOrEmpty()) return
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    XCollageScreen(uris = uris, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun XCollageScreen(
    uris: List<Uri>,
    modifier: Modifier = Modifier,
    viewModel: XCollageViewModel = viewModel()
) {
    val context = LocalContext.current

}


@Preview(showBackground = true)
@Composable
fun XCollageScreenPreview() {
    NewCollageTheme {

    }
}