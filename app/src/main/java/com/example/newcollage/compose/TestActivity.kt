package com.example.newcollage.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newcollage.compose.ui.theme.NewCollageTheme

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TestScreen(modifier: Modifier = Modifier) {

}


@Preview(showBackground = true)
@Composable
fun TestScreenPreview() {
    NewCollageTheme {
        TestScreen()
    }
}


//测试interactionSource，compose控件的的各种按下 焦点 拖动的状态
@Composable
private fun InteractionTest(modifier: Modifier) {
    Column(modifier = modifier) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val isDragged by interactionSource.collectIsDraggedAsState()
        val isFocused by interactionSource.collectIsFocusedAsState()
        val isHovered by interactionSource.collectIsHoveredAsState()
        Button(
            onClick = {}, interactionSource = interactionSource, modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        ) {
            Text(
                text = "isPressed: $isPressed \n" + "isDragged: $isDragged \n" + "isFocused: $isFocused \n" + "isHovered: $isHovered",
                modifier = Modifier.padding(16.dp)
            )
        }

    }
}

