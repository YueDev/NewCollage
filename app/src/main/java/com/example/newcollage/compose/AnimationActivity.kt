package com.example.newcollage.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import com.example.newcollage.compose.ui.home.Home
import com.example.newcollage.compose.ui.theme.NewCollageTheme


//https://developer.android.com/codelabs/jetpack-compose-animation
class AnimationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                AnimationScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun AnimationScreen(modifier: Modifier = Modifier) {
    Home()
}

@Preview(showBackground = true)
@Composable
fun AnimationScreenPreview() {
    NewCollageTheme {
        AnimationScreen()
    }
}

