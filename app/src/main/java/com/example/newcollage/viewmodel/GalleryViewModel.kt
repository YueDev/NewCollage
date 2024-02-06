package com.example.newcollage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.newcollage.repository.GalleryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class GalleryViewModel(application: Application ) : AndroidViewModel(application) {

    private val galleryRepository = GalleryRepository()

    val uris = galleryRepository.loadMediaData(application).flowOn(Dispatchers.IO)

}