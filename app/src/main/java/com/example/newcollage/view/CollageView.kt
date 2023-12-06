package com.example.newcollage.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip
import com.example.newcollage.bean.CollageItem
import com.example.newcollage.util.pointInImage

class CollageView: View {

    private val imagePaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    private val items = mutableListOf<CollageItem>()
    private val paths = mutableListOf<Path>()

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
            this.items.addAll(items.take(2))
            resetItem()
            invalidate()
        }
    }

    private fun resetItem() {
        paths.clear()
        //2个的布局[/]这样子
        val padding = 32f

        val k = measuredWidth.toFloat() / measuredHeight

        val point1 = PointF(0f + padding, 0f + padding)
        val point2 = PointF(measuredWidth  - padding - padding / 2f, 0f + padding)
        val point3 = PointF(0f + padding, measuredHeight.toFloat() - padding - padding / 2f)

        val path1 = Path()
        path1.moveTo(point1.x, point1.y)
        path1.lineTo(point2.x, point2.y)
        path1.lineTo(point3.x, point3.y)
        path1.close()
        paths.add(path1)

        val point4 = PointF(measuredWidth - padding, 0f + padding + padding / 2f)
        val point5 = PointF(measuredWidth - padding, measuredHeight - padding)
        val point6 = PointF(0f + padding + padding / 2f, measuredHeight - padding)

        val path2 = Path()
        path2.moveTo(point4.x, point4.y)
        path2.lineTo(point5.x, point5.y)
        path2.lineTo(point6.x, point6.y)
        path2.close()
        paths.add(path2)

    }



    val pathPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeWidth = 8.0f
        color = Color.parseColor("#888800")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paths.forEach {
            canvas.drawPath(it, pathPaint)
        }
//        items.forEach {
//            canvas.save()
//            canvas.clipPath(it.layout.path)
//            canvas.drawBitmap(it.bitmap, it.matrix, imagePaint)
//            canvas.restore()
//        }
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