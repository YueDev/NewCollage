package com.example.newcollage.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.withClip
import com.example.newcollage.bean.CollageItem
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



    fun setData(items:List<CollageItem>) {
        post {
            this.items.clear()
            this.items.addAll(items)
            resetItem()
            invalidate()
        }
    }

    private fun resetItem() {
        val w = measuredWidth
        val h = measuredHeight
        items.forEach {
            val layout = it.layout
            layout.left = layout.leftOrigin * w / 1000.0f
            layout.top = layout.topOrigin * h / 1000.0f
            layout.right = layout.rightOrigin * w / 1000.0f
            layout.bottom = layout.bottomOrigin * h / 1000.0f
            val path = layout.path
            layout.path.reset()
            path.moveTo(layout.left, layout.top)
            path.lineTo(layout.right, layout.top)
            path.lineTo(layout.right, layout.bottom)
            path.lineTo(layout.left, layout.bottom)
            path.close()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        items.forEach {
            canvas.save()
            canvas.clipPath(it.layout.path)
            canvas.drawBitmap(it.bitmap, it.matrix, imagePaint)
            canvas.restore()
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