package com.example.newcollage.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SegmentViewModel(application: Application) : AndroidViewModel(application) {

    // 简单弄一个标志当初始化了，写构造参数太麻烦了
    private var isInit = false

    private val _segmentResult: MutableStateFlow<SegmentResult<Bitmap>> = MutableStateFlow(SegmentResult.Loading())
    val segmentResult = _segmentResult as StateFlow<SegmentResult<Bitmap>>

    fun requsetSegment(uri: Uri) {
        isInit = true
        viewModelScope.launch {

        }
    }
}

sealed class SegmentResult<T>(val data: T? = null, val errorMessage: String? = null) {
    class Success<T>(data: T): SegmentResult<T>(data = data)
    class Loading<T>: SegmentResult<T>()
    class Failed<T>(errorMessage: String): SegmentResult<T>(errorMessage = errorMessage)
}