package com.example.newcollage.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.newcollage.repository.GalleryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val galleryRepository = GalleryRepository()

    // 直接把flow冷流暴露给调用方的话，调用方每次发生改动，会重新collect，重新请求Repository的数据
    // 用一个stateflow，是热流，可以在viewmdel初始的时候就可以请求，调用方如果重新收集的时候，可以迅速把最新的值传递过去。
    // 能节省从repository请求数据
    // 更多关于stateflow的：
    // https://amitshekhar.me/blog/stateflow-and-sharedflow
//    val uris = galleryRepository.loadMediaData(application).flowOn(Dispatchers.IO)

    private val _urisStateFlow: MutableStateFlow<List<Uri>> = MutableStateFlow(listOf())
    val urisStateFlow: StateFlow<List<Uri>> = _urisStateFlow

    init {
        viewModelScope.launch {
            galleryRepository.loadMediaData(application)
                .flowOn(Dispatchers.IO)
                .collect {
                    _urisStateFlow.value = it
                }
        }
    }
}