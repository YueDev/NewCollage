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
import com.example.newcollage.bean.CollageItem
import com.example.newcollage.repository.ImageRepository
import com.example.newcollage.util.loadBitmapSync
import com.example.newcollage.util.pointInImage

class CollageView: View {

    private val imagePaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    private val items = mutableListOf<CollageItem>()

    private var selectItem: CollageItem? = null

    // 手势识别器
    private val gestureDetector = object : MyGestureDetector(context) {

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return selectItem?.let {
                it.matrix.postTranslate(-distanceX, -distanceY)
                invalidate()
                true
            } ?: false
        }


        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return selectItem?.let {
                it.matrix.postScale(detector.scaleFactor, detector.scaleFactor, detector.focusX, detector.focusY)
                invalidate()
                true
            } ?: false
        }

        override fun onRotation(
            beginDegree: Float,
            prevDegree: Float,
            currentDegree: Float,
            focusX: Float,
            focusY: Float
        ) {
            selectItem?.let {
                it.matrix.postRotate(currentDegree - prevDegree, focusX, focusY)
                invalidate()
            }
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

    }


    fun setData(items:List<CollageItem>) {
        this.items.clear()
        this.items.addAll(items)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        items.forEach {
            canvas.drawBitmap(it.bitmap, it.matrix, imagePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            selectItem = null
        } else if (event.action == MotionEvent.ACTION_DOWN) {
            selectItem = items.lastOrNull { pointInImage(event.x, event.y, it.bitmap, it.matrix) }
            if (selectItem == null) return false
        }
        return gestureDetector.onTouchEvent(event)
    }

}