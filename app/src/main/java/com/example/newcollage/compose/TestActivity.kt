package com.example.newcollage.compose

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newcollage.compose.ui.theme.NewCollageTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
fun TestScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val isUsed by viewModel.isUsedState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Switch(checked = isUsed, onCheckedChange = viewModel::toggleIsUsed)
        Text("isUsed: $isUsed")
    }
}

@Preview(showBackground = true)
@Composable
fun TestScreenPreview() {
    NewCollageTheme {
        TestScreen()
    }
}


class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sp = application.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    private val _isUsedState = MutableStateFlow(sp.getBoolean("isUsed", false))

    val isUsedState: StateFlow<Boolean> = _isUsedState.asStateFlow()

    fun toggleIsUsed(isToggled: Boolean) {
        viewModelScope.launch {
            val newValue = !_isUsedState.value
            sp.edit().putBoolean("isToggled", newValue).apply()
            _isUsedState.value = isToggled
        }
    }
}




// region interaction
// 测试interactionSource，compose控件的的各种按下 焦点 拖动的状态
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
                text = "isPressed: $isPressed \nisDragged: $isDragged \nisFocused: $isFocused \nisHovered: $isHovered",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
//end region
