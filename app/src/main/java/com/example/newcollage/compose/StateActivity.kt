package com.example.newcollage.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newcollage.compose.ui.theme.ComposeTheme


// https://developer.android.com/codelabs/jetpack-compose-state#0
class StateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WellnessScreen()
                }
            }
        }
    }
}

@Composable
fun WellnessScreen(modifier: Modifier = Modifier) {
    StatefulCounter(modifier)
}

@Composable
fun WaterCounter(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        var count by rememberSaveable { mutableIntStateOf(0) }
        if (count > 0) {
            Text("You've had $count glasses.")
        }
        Button(onClick = { count++ }, Modifier.padding(top = 8.dp), enabled = count < 8) {
            Text("Add one")
        }
    }
}


// 无状态的WaterCounter
@Composable
fun StatelessCounter(count: Int, onCountChanged: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        if (count > 0) {
            Text("You've had $count glasses.")
        }
        Button(
            onClick = { onCountChanged(count + 1) },
            Modifier.padding(top = 8.dp),
            enabled = count < 8
        ) {
            Text("Add one")
        }
    }
}

// 有状态的WaterCounter
@Composable
fun StatefulCounter(modifier: Modifier = Modifier) {
    var counter by remember { mutableIntStateOf(0) }

    StatelessCounter(count = counter, onCountChanged = { counter = it }, modifier = modifier)

}

@Composable
fun WellnessTaskItem(taskName: String, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            text = taskName
        )

        IconButton(onClick = onClose) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                Modifier.clickable { onClose() })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WellnessScreenCounter() {
    ComposeTheme {
        WellnessScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun WellnessTaskItemPreview() {
    WellnessTaskItem(
        taskName = "This is a task.",
        onClose = {},
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun StatelessCounterPreview() {
    StatelessCounter(count = 0, onCountChanged = {})
}

@Preview(showBackground = true)
@Composable
fun StatefulCounterPreview() {
    StatefulCounter()
}