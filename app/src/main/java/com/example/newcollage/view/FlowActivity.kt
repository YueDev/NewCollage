package com.example.newcollage.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.example.newcollage.databinding.ActivityFlowBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class FlowActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFlowBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<FlowViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collectLatest {
                    binding.textView.text = it
                }
            }
        }

        binding.button.setOnClickListener {
            viewModel.requestNet()
        }

    }

}

class FlowViewModel: ViewModel() {

    private val _stateFlow = MutableStateFlow("Init")
    val stateFlow = _stateFlow as StateFlow<String>

    private var num = 0

    fun requestNet() {
         viewModelScope.launch {
             val message = getString()
             _stateFlow.value = message
         }
    }

    private suspend fun getString() = withContext(Dispatchers.IO) {
        val myNum = num++
        delay(Random.nextLong(1000, 3000))
        "Message from Internet:${myNum}"
    }
}