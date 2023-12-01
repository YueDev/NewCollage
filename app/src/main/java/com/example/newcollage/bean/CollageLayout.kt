package com.example.newcollage.bean

import android.graphics.Path


// collage layout布局
// 默认画布是1000 * 1000 int类型 不要小数
data class CollageLayout(
    // item的名字
    val name: String,
    // 原始四个坐标点
    val leftOrigin: Float = 0f,
    val topOrigin: Float = 0f,
    val rightOrigin: Float = 0f,
    val bottomOrigin: Float = 0f,
    // item的类型
    val type:Int = 0,
    // 当前的四个坐标点 已应用到坐标的大小
    var left: Float = leftOrigin,
    var top: Float = topOrigin,
    var right: Float = rightOrigin,
    var bottom: Float = bottomOrigin,
    // 四边是否可以调节
    val canAdjustLeft: Boolean = false,
    val canAdjustTop: Boolean = false,
    val canAdjustRight: Boolean = false,
    val canAdjustBottom: Boolean = false,
    // 四边调节时联动item
    val leftAdjustPositiveName: List<String> = listOf(),
    val leftAdjustNegativeName: List<String> = listOf(),
    val topAdjustPositiveName: List<String> = listOf(),
    val topAdjustNegativeName: List<String> = listOf(),
    val rightAdjustPositiveName: List<String> = listOf(),
    val rightAdjustNegativeName: List<String> = listOf(),
    val bottomAdjustPositiveName: List<String> = listOf(),
    val bottomAdjustNegativeName: List<String> = listOf(),
    //内边距的系数，0就是没有内边距
    val leftPaddingFactor:Float = 0.0f,
    val topPaddingFactor:Float = 0.0f,
    val rightPaddingFactor:Float = 0.0f,
    val bottomPaddingFactor:Float = 0.0f,
    //item的路径 根据上边的信息计算出来
    val path: Path = Path()
)