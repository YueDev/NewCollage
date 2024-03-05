package com.example.newcollage.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.newcollage.compose.ui.theme.NewCollageTheme

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
    var num by remember { mutableIntStateOf(0) }

    // Column是内联函数，直接用Column会导致RecompositionScreen重组
    // 写一个不内联的MyColumn 可以再点击button的时候让RecompositionScreen不重组
    // 但我感觉意义不大，因为这里num用by remember记住了 所以直接用Column就行
    MyColumn(modifier = modifier) {
        Text(text = "RecompositionTest")
        Text(text = "Click num: $num")
        Button(onClick = { num++ }) {
            Text(text = "Click")
        }
    }
}

@Composable
private fun MyColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier, content = content)
}


@Preview(showBackground = true)
@Composable
fun RecompositionPreview() {
    NewCollageTheme {
        RecompositionScreen()
    }
}