package com.example.newcollage.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newcollage.repository.SegmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SegmentViewModel : ViewModel() {

    // 简单弄一个标志当初始化了，写构造参数太麻烦了
    private var isInit = false

    private val segmentRepository = SegmentRepository()

    private val _segmentResult: MutableStateFlow<SegmentResult<Bitmap>> = MutableStateFlow(SegmentResult.Loading())
    val segmentResult = _segmentResult as StateFlow<SegmentResult<Bitmap>>

    fun requestSegment(context: Context, uri: Uri) {
        if (isInit) return
        isInit = true
        viewModelScope.launch {
            segmentRepository.segment(context, uri).flowOn(Dispatchers.IO).collect {
                _segmentResult.value = it
            }
        }
    }
}

sealed class SegmentResult<T>(val data: T? = null, val errorMessage: String? = null) {
    class Success<T>(data: T): SegmentResult<T>(data = data)
    class Loading<T>: SegmentResult<T>()
    class Failed<T>(errorMessage: String): SegmentResult<T>(errorMessage = errorMessage)
}