package com.example.newcollage.view.shape

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.shapes.Shape
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.scaleMatrix
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import com.example.newcollage.util.radialToCartesian
import com.example.newcollage.util.toRadians
import kotlin.math.cos
import kotlin.math.sin

class ShapeView : View {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.RED
    }

    private val path = Path()

    private val matrix = Matrix()

    private var shape = RoundedPolygon(3)
        set(value) {
            field = value.normalized()
            calculateMatrixAndInvalidate()
        }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        //五角星的外半径
        val radius = 1.0f
        //内半径
        val innerRadius = radius * sin(18.0f.toRadians()) / cos(36.0f.toRadians())

        //每个顶点之间的角度间隔
        val degreeUnit = 360.0f / 5f
        val startDegree = 0.0f
        val innerStartDegree = 36.0f


        //顺时针拿到10个点
        val point0 = radialToCartesian(radius, startDegree.toRadians())
        val innerPoint0 = radialToCartesian(innerRadius, innerStartDegree.toRadians())
        val point1 = radialToCartesian(radius, (startDegree + degreeUnit).toRadians())
        val innerPoint1 = radialToCartesian(innerRadius, (innerStartDegree + degreeUnit).toRadians())
        val point2 = radialToCartesian(radius, (startDegree + degreeUnit * 2).toRadians())
        val innerPoint2 = radialToCartesian(innerRadius, (innerStartDegree + degreeUnit * 2).toRadians())
        val point3 = radialToCartesian(radius, (startDegree + degreeUnit * 3).toRadians())
        val innerPoint3 = radialToCartesian(innerRadius, (innerStartDegree + degreeUnit * 3).toRadians())
        val point4 = radialToCartesian(radius, (startDegree + degreeUnit * 4).toRadians())
        val innerPoint4 = radialToCartesian(innerRadius, (innerStartDegree + degreeUnit * 4).toRadians())

        val vertex = floatArrayOf(
            point0.x,
            point0.y,
            innerPoint0.x,
            innerPoint0.y,
            point1.x,
            point1.y,
            innerPoint1.x,
            innerPoint1.y,
            point2.x,
            point2.y,
            innerPoint2.x,
            innerPoint2.y,
            point3.x,
            point3.y,
            innerPoint3.x,
            innerPoint3.y,
            point4.x,
            point4.y,
            innerPoint4.x,
            innerPoint4.y,
        )

        shape = RoundedPolygon(vertex, rounding = CornerRounding(0.1f, 0.5f))

    }

    private fun calculateMatrixAndInvalidate() {
        post {
            matrix.reset()

            val maxSize = measuredWidth.coerceAtMost(measuredHeight).toFloat()
            val centerX = measuredWidth / 2.0f
            val centerY = measuredHeight / 2.0f

            val bounds = shape.calculateMaxBounds()
            val shapeW = bounds[3] - bounds[1]
            val shapeH = bounds[2] - bounds[0]

            val scale = (maxSize / shapeW).coerceAtMost(maxSize / shapeH)

            matrix.reset()
            matrix.postTranslate(centerX - shape.centerX, centerY - shape.centerY)
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate(-360.0f / 20.0f, centerX, centerY)

            invalidate()
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        shape.toPath(path)
        path.transform(matrix)

        canvas.drawPath(path, paint)
    }


}