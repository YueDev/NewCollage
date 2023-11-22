package com.example.newcollage.view

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.absoluteValue
import kotlin.math.atan2

/**
 * Created by Yue on 2020/8/30.
 * 自定义的手势检测
 * 整合了系统的GestureDetector和ScaleGestureDetector
 * 简单实现了多指的缩放和旋转
 */
open class MyGestureDetector(context: Context) {

    private var beginDegree = 0f
    private var prevDegree = 0f


    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {

            return this@MyGestureDetector.onDown(e)
        }

        override fun onShowPress(e: MotionEvent) {
            this@MyGestureDetector.onShowPress(e)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return this@MyGestureDetector.onSingleTapUp(e)
        }


        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return this@MyGestureDetector.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onLongPress(e: MotionEvent) {
            this@MyGestureDetector.onLongPress(e)
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return this@MyGestureDetector.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return this@MyGestureDetector.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return this@MyGestureDetector.onDoubleTap(e)
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            return this@MyGestureDetector.onDoubleTapEvent(e)
        }

        override fun onContextClick(e: MotionEvent): Boolean {
            return this@MyGestureDetector.onContextClick(e)
        }
    }

    private val scaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return  this@MyGestureDetector.onScale(detector)

        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return this@MyGestureDetector.onScaleBegin(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            this@MyGestureDetector.onScaleEnd(detector)
        }
    }




    private val gestureDetector = GestureDetector(context, gestureListener)
    private val scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)

    fun onTouchEvent(e:MotionEvent): Boolean {

        if (e.actionMasked == MotionEvent.ACTION_POINTER_DOWN && e.pointerCount > 1 ) {
            beginDegree = calRotation(e.x, e.y, e.getX(1), e.getY(1))
            prevDegree = beginDegree
        }

        if (e.actionMasked == MotionEvent.ACTION_MOVE && e.pointerCount > 1 ) {
            val currentDegree = calRotation(e.x, e.y, e.getX(1), e.getY(1))
            //有时候旋转抬手的时候会造成point0和point1互换，导致角度出现180的偏差
            //因此如果当前角度与之前角度相差大于30，直接默认无效即可
            if ((currentDegree - prevDegree).absoluteValue > 30.0f) {
                prevDegree = currentDegree
            }
            val focusX = (e.getX(1) + e.x) / 2
            val focusY = (e.getY(1) + e.y) / 2
            onRotation(beginDegree, prevDegree, currentDegree, focusX, focusY)
            prevDegree = currentDegree
        }

        val b = e.pointerCount > 1 && scaleGestureDetector.onTouchEvent(e)

        return gestureDetector.onTouchEvent(e) || b
    }

    //以下是所有的方法，按照需求实现即可

    open fun onDown(e: MotionEvent) = true

    open fun onShowPress(e: MotionEvent) {}
    open fun onSingleTapUp(e: MotionEvent) = false
    open fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ) = false

    open fun onLongPress(e: MotionEvent) {}

    open fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) = false

    open fun onSingleTapConfirmed(e: MotionEvent) = false

    open fun onDoubleTap(e: MotionEvent) = false

    open fun onDoubleTapEvent(e: MotionEvent) = false

    open fun onContextClick(e: MotionEvent) = false

    open fun onScale(detector: ScaleGestureDetector) = false

    open fun onScaleBegin(detector: ScaleGestureDetector) = true

    open fun onScaleEnd(detector: ScaleGestureDetector) {

    }

    open fun onRotation(beginDegree: Float, prevDegree: Float, currentDegree: Float, focusX: Float, focusY: Float) {

    }


    //计算两个点的角度
    private fun calRotation(x: Float, y: Float, newX: Float, newY: Float): Float {
        val dx = newX - x
        val dy = newY - y
        //利用斜切转化为极坐标，相当于把两个点的线迁移到0，0，然后转化为极坐标，得到弧度
        val angle = atan2(dy, dx)
        return Math.toDegrees(angle.toDouble()).toFloat()
    }



}
