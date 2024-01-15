package com.example.newcollage.turbo_collage

import kotlin.random.Random

class TCCollage {
    private val collageItems = mutableListOf<TCCollageItem>()

    private val bitmapMap = mutableMapOf<String, TCBitmap>()

    private var width = 1000.0
    private var height = 1500.0

    //初始化，设置TCBitmaps数据，传入的每张图片的尺寸，之后调用collage()
    //图片尺寸变化时，重新调用一下这个方法即可
    fun init(bitmaps: List<TCBitmap>?) {
        bitmapMap.clear()
        collageItems.clear()

        collageItems.add(TCCollageItem(null, TCRect(0.0, 0.0, 1.0, 1.0)))

        bitmaps?.forEach {
            val uuid: String = it.uuid
            val emptyCollageItem = getEmptyItemOrNull()
            if (emptyCollageItem != null) {
                emptyCollageItem.uuid = uuid
            } else {
                val d = d()
                val tcRect = if (d < 0) TCRect(0.0, 0.0, 1.0, 1.0) else a(d)
                collageItems.add(TCCollageItem(uuid, tcRect))
            }
            bitmapMap[uuid] = it
        }
    }


    //拼图 耗时操作
    fun collage(width: Double, height: Double, padding: Double): TCResult? {
        return try {
            this.width = width
            this.height = height
            reCollage()
            drawCollage(padding)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun reCollage() {
        collageItems.clear()
        if (bitmapMap.isEmpty()) {
            collageItems.add(TCCollageItem(null, TCRect(0.0, 0.0, 1.0, 1.0)))
        } else {
            val ratioMap = mutableMapOf<String, Double>()
            for ((uuid, bitmap) in bitmapMap) {
                ratioMap[uuid] = bitmap.width * 1.0 / bitmap.height
            }
            val totalShuffle = TCShuffle.getTotalShuffle(ratioMap, width, height)
            //这个返回这个结果，就是计算的质量
            // Math.abs(totalShuffle.                result.add(it.uuid, tcRectF.getRectF())a() - ((this.width) / this.height)) < 0.01d;
            //一般来说 几张图计算出来 留白比较多，这个值就是false
            totalShuffle?.a(collageItems, TCRect(0.0, 0.0, 1.0, 1.0))
        }
    }

    private fun drawCollage(frame: Double): TCResult {
        val result = TCResult()

        collageItems.forEach {
            val tcRectF: TCRect = getItemInCanvasRect(it, frame, width, height)
            if (!it.emptyUUID()) {
                it.uuid?.let { uuid ->
                    result.add(uuid, tcRectF.getRectF())
                }
            }
        }
        return result
    }

    private fun getItemInCanvasRect(
        collageItem: TCCollageItem,
        frame: Double,
        width: Double,
        height: Double
    ): TCRect {
        val c = if (collageItem.ratioRect.left < 1.0E-4) 2.0 * frame else frame
        val c2 =
            if (collageItem.ratioRect.left + collageItem.ratioRect.right > 0.9999) 2.0 * frame else frame
        val c3 = if (collageItem.ratioRect.top < 1.0E-4) 2.0 * frame else frame
        var c4 = frame
        if (collageItem.ratioRect.top + collageItem.ratioRect.bottom > 0.9999) {
            c4 = frame * 2.0
        }
        return TCRect(
            collageItem.ratioRect.left * width + c,
            collageItem.ratioRect.top * height + c3,
            collageItem.ratioRect.right * width - (c + c2),
            collageItem.ratioRect.bottom * height - (c3 + c4)
        )
    }


    private fun getEmptyItemOrNull(): TCCollageItem? {
        val list = getEmptyUUIDCollageItems()
        return if (list.isNotEmpty()) list[0] else null
    }


    private fun getEmptyUUIDCollageItems(): List<TCCollageItem> {
        val list = mutableListOf<TCCollageItem>()
        for (collageItem in collageItems) {
            if (collageItem.emptyUUID()) {
                list.add(collageItem)
            }
        }
        return list
    }

    private fun d(): Int {
        if (collageItems.isEmpty()) return -1

        var result: Int
        var i2 = 0
        var i3 = 1
        var z = collageItems[0].getRatioMaxBound(width, height)
        while (true) {
            result = i2
            if (i3 >= collageItems.size) {
                break
            }
            val a = collageItems[i3].getRatioMaxBound(width, height)
            var z2 = z
            if (a > z) {
                z2 = a
                i2 = i3
            }
            i3++
            z = z2
        }

        return result
    }

    private fun a(i: Int): TCRect {
        val tcRect: TCRect
        val item = collageItems[i]
        val nextInt: Double = 0.4 + Random.nextInt(20) / 100.0
        val (left, top, right, bottom) = item.ratioRect
        if (item.ratioRect.right * width < item.ratioRect.bottom * height) {
            item.ratioRect = TCRect(left, top, right, bottom * nextInt)
            tcRect = TCRect(left, top + bottom * nextInt, right, (1.0 - nextInt) * bottom)
        } else {
            item.ratioRect = TCRect(left, top, right * nextInt, bottom)
            tcRect = TCRect(left + right * nextInt, top, (1.00 - nextInt) * right, bottom)
        }
        return tcRect
    }


}