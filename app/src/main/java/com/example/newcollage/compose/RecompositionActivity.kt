package com.example.newcollage.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import kotlinx.coroutines.delay

class RecompositionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RecompositionScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun RecompositionScreen(modifier: Modifier = Modifier) {
    Recomposition2(modifier = modifier)
}

@Composable
private fun Recomposition2(modifier: Modifier = Modifier) {

}


@Composable
private fun Recomposition1(modifier: Modifier = Modifier) {

    var color by remember { mutableStateOf(Color.White) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            var r = (color.red - 0.1f).coerceIn(0.0f, 1.0f)
            if (r == 0.0f) r = 1.0f
            color = Color(red = r, color.green, color.blue, color.alpha)
        }
    }

    //background 会重组，drawBehind不会 在绘制阶段一般不会重组
    Column(modifier = Modifier) {
        Text(text = "123", modifier = modifier
            .size(64.dp)
//            .background(color)
            .drawBehind {
                drawRect(color = color)
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun RecompositionPreview() {
    NewCollageTheme {
        RecompositionScreen()
    }
}