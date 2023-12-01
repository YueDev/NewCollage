package com.example.newcollage.bean

import android.graphics.Bitmap
import android.graphics.Matrix

data class CollageItem(
    val bitmap: Bitmap,
    val layout: CollageLayout,
    val matrix: Matrix = Matrix(),
)
