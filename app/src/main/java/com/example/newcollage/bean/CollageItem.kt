package com.example.newcollage.bean

import android.graphics.Bitmap
import android.graphics.Matrix

data class CollageItem(
    val bitmap: Bitmap,
    val matrix: Matrix = Matrix(),
)
