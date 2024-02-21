package com.example.newcollage.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newcollage.compose.ui.theme.NewCollageTheme


// https://developer.android.com/codelabs/jetpack-compose-state#0
class StateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    WellnessScreen()
                }
            }
        }
    }
}

@Composable
fun WellnessScreen(
    modifier: Modifier = Modifier,
    wellnessViewModel: WellnessViewModel = viewModel()
) {
    Column(modifier = modifier) {
        StatefulCounter()

        WellnessTasksList(
            list = wellnessViewModel.tasks,
            onClose = wellnessViewModel::remove,
            onCheckedChange = wellnessViewModel::changeTaksedChecked
        )
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


//无状态的WellnessTaskItem
@Composable
fun WellnessTaskItem(
    taskName: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp), text = taskName
        )

        Checkbox(checked = checked, onCheckedChange = onCheckedChange)

        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close,
                contentDescription = "Close",
                Modifier.clickable { onClose() })
        }
    }
}


@Composable
fun Handler(modifier: Modifier = Modifier) {

}

@Composable
fun WellnessTasksList(
    modifier: Modifier = Modifier,
    onClose: (WellnessTask) -> Unit,
    onCheckedChange: (WellnessTask, Boolean) -> Unit,
    list: List<WellnessTask>
) {
    LazyColumn(
        state = rememberLazyListState(),
        modifier = modifier
    ) {
        items(list) { task ->
            WellnessTaskItem(
                taskName = task.label,
                checked = task.checked,
                onCheckedChange = { newChecked ->
                    onCheckedChange(task, newChecked)
                },
                onClose = { onClose(task) }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WellnessScreenPreview() {
    NewCollageTheme {
        WellnessScreen()
    }
}


data class WellnessTask(
    val id: Int,
    val label: String,
    val initChecked: Boolean = false
) {
    //利用委托属性来管理checked
    //相当与定义了一个val checked: MutableState<Boolean> = mutableStateOf(false)
    //在data里利用一个MutableState来管理状态，比新建一个data，从list里替换要高效很多.
    var checked by mutableStateOf(initChecked)
}


class WellnessViewModel : ViewModel() {
    private val _tasks =
        getWellnessTasks().toMutableStateList()
    val tasks: List<WellnessTask>
        get() = _tasks


    fun remove(item: WellnessTask) {
        _tasks.remove(item)
    }

    fun changeTaksedChecked(item: WellnessTask, checked: Boolean) {
        _tasks.find { it.id == item.id }?.let {
            it.checked = checked
        }
    }

    private fun getWellnessTasks() =
        List(30) { i -> WellnessTask(i, "Task # $i") }

}

