package com.example.newcollage.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.example.newcollage.repository.ImageRepository
import com.example.newcollage.util.loadBitmapSync
import com.example.newcollage.util.pointInImage

class CollageView: View {

    private val imagePaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    private var imageBitmap: Bitmap? = null
    private val imageMatrix = Matrix()

    // 手势识别器
    private val gestureDetector = object : MyGestureDetector(context) {

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            imageMatrix.postTranslate(-distanceX, -distanceY)
            invalidate()
            return true
        }


        override fun onScale(detector: ScaleGestureDetector): Boolean {
            imageMatrix.postScale(
                detector.scaleFactor,
                detector.scaleFactor,
                detector.focusX,
                detector.focusY
            )
            invalidate()
            return true
        }

        override fun onRotation(
            beginDegree: Float,
            prevDegree: Float,
            currentDegree: Float,
            focusX: Float,
            focusY: Float
        ) {
            imageMatrix.postRotate(currentDegree - prevDegree, focusX, focusY)
            invalidate()
        }
    }



    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val imageResId = ImageRepository.imageResIds.random()
        imageBitmap = BitmapFactory.decodeResource(resources, imageResId)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        imageBitmap?.let {
            canvas.drawBitmap(it, imageMatrix, imagePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        imageBitmap?.also {
            if (event.action == MotionEvent.ACTION_DOWN && !pointInImage(event.x, event.y, it, imageMatrix)) {
                return false
            }
        } ?: return false
        return gestureDetector.onTouchEvent(event)
    }

}