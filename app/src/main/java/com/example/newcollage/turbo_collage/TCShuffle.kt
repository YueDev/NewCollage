package com.example.newcollage.turbo_collage

import kotlin.math.abs
import kotlin.random.Random

class TCShuffle {

    private var s1: TCShuffle? = null
    private var s2: TCShuffle? = null
    private var s3: TCShuffle? = null
    private var uuid: String? = null
    private var ratio = 0.0
    private var tcJoin: TCJoin? = null


    companion object {

        fun getTotalShuffle(
            ratioMap: Map<String, Double>,
            width: Double,
            height: Double
        ): TCShuffle? {
            var shuffle: TCShuffle?
            if (ratioMap.isEmpty()) {
                shuffle = null
            } else {
                val canvasRatio = width / height
                var a = getTotalShuffle(ratioMap)
                a!!.b(canvasRatio)
                var i = 0
                var dVar: TCShuffle? = null
                var r17 = 0.0
                while (i < 500) {
                    var z = r17
                    var dVar2 = dVar
                    if (abs(a!!.a() - canvasRatio) < 0.01) {
                        var z2 = r17
                        if (dVar == null) {
                            dVar2 = a
                            z2 = a.a(width, height)
                        }
                        val a2 = a.a(width, height)
                        z = z2
                        if (a2 > z2) {
                            z = a2
                            dVar2 = a
                        }
                    }
                    val a3 = getTotalShuffle(ratioMap)
                    a3!!.b(canvasRatio)
                    var dVar3 = a
                    if (abs(a3.a() - canvasRatio) < abs(a.a() - canvasRatio)) {
                        dVar3 = a3
                    }
                    i++
                    r17 = z
                    dVar = dVar2
                    a = dVar3
                }
                shuffle = dVar
                if (dVar == null) {
                    shuffle = a
                }
            }
            return shuffle
        }


        private fun getTotalShuffle(ratioMap: Map<String, Double>): TCShuffle? {
            val shuffle: TCShuffle?
            if (ratioMap.isEmpty()) {
                shuffle = null
            } else {
                val keySet = ratioMap.keys
                val keyList: List<String> = ArrayList(keySet)
                val randomKeyList: List<String> = keyList.shuffled()
                val shuffleList: MutableList<TCShuffle> =
                    ArrayList(randomKeyList.size)
                for (uuid in randomKeyList) {
                    val ratio = ratioMap[uuid]
                    val s = TCShuffle()
                    s.uuid = uuid
                    s.ratio = ratio ?: 1.0
                    shuffleList.add(s)
                }
                shuffle = linkAllShuffles(shuffleList, null)
            }
            return shuffle
        }


        // 把所有shuffleList链接起来
        private fun linkAllShuffles(shuffleList: List<TCShuffle>, noUse: TCShuffle?): TCShuffle {
            if (shuffleList.size == 1) {
                val s = shuffleList[0]
                s.s3 = noUse
                return s
            }

            val s = TCShuffle()
            s.tcJoin = TCJoin.TCLeftRightJoin
            val shuffle1 = linkAllShuffles(shuffleList.subList(0, shuffleList.size / 2), s)
            val shuffle2 =
                linkAllShuffles(shuffleList.subList(shuffleList.size / 2, shuffleList.size), s)
            s.s1 = shuffle1
            s.s2 = shuffle2
            s.s3 = noUse

            return s
        }
    }

    private fun a(d: Double, d2: Double): Double {
        val list = ArrayList<TCCollageItem>(b())
        a(list, TCRect(0.0, 0.0, 1.0, 1.0))
        var result = 4.7264832958171709E18

        for (item: TCCollageItem in list) {
            val c2: Double = item.ratioRect.right * d.coerceAtMost(result)
            result = item.ratioRect.bottom * d2.coerceAtMost(c2)
        }
        return result
    }

    private fun b(): Int {
        return if (uuid != null) 1 else s1!!.b() + s2!!.b()
    }


    fun a(list: MutableList<TCCollageItem>?, iVar: TCRect) {
        val iVar2: TCRect
        val iVar3: TCRect
        if (list != null) {
            if (uuid != null) {
                list.add(TCCollageItem(uuid, iVar))
                return
            }
            val a: Double = s1!!.a()
            val a2: Double = s2!!.a()
            if (tcJoin === TCJoin.TCLeftRightJoin) {
                if (Random.nextBoolean()) {
                    val iVar4 =
                        TCRect(iVar.left, iVar.top, iVar.right * (a / (a2 + a)), iVar.bottom)
                    iVar2 = TCRect(
                        iVar.left + iVar4.right,
                        iVar.top,
                        iVar.right - iVar4.right,
                        iVar.bottom
                    )
                    iVar3 = iVar4
                } else {
                    iVar2 = TCRect(iVar.left, iVar.top, iVar.right * (a2 / (a + a2)), iVar.bottom)
                    iVar3 = TCRect(
                        iVar.left + iVar2.right,
                        iVar.top,
                        iVar.right - iVar2.right,
                        iVar.bottom
                    )
                }
            } else if (Random.nextBoolean()) {
                val iVar5 = TCRect(
                    iVar.left,
                    iVar.top,
                    iVar.right,
                    1.0 / a / (1.0 / a + 1.0 / a2) * iVar.bottom
                )
                iVar2 = TCRect(
                    iVar.left,
                    iVar.top + iVar5.bottom,
                    iVar.right,
                    iVar.bottom - iVar5.bottom
                )
                iVar3 = iVar5
            } else {
                iVar2 = TCRect(
                    iVar.left,
                    iVar.top,
                    iVar.right,
                    1.0 / a2 / (1.0 / a + 1.0 / a2) * iVar.bottom
                )
                iVar3 = TCRect(
                    iVar.left,
                    iVar.top + iVar2.bottom,
                    iVar.right,
                    iVar.bottom - iVar2.bottom
                )
            }
            s1!!.a(list, iVar3)
            s2!!.a(list, iVar2)
        }
    }

    private fun a(): Double {
        val r10: Double = if (uuid != null) {
            ratio
        } else {
            val a = s1!!.a()
            val a2 = s2!!.a()
            if (tcJoin === TCJoin.TCLeftRightJoin) a + a2 else 1.0 / (1.0 / a + 1.0 / a2)
        }
        return r10
    }

    private fun changeJoinType() {
        tcJoin = if (tcJoin === TCJoin.TCLeftRightJoin) {
            TCJoin.TCUpDownJoin
        } else {
            TCJoin.TCLeftRightJoin
        }
    }

    private fun b(paramDouble: Double) {
        if (uuid == null) {
            val list: List<TCShuffle> = c().shuffled()
            for (d1 in list) {
                val d2 = a()
                d1.changeJoinType()
                val d3 = a()
                if (abs(d3 - paramDouble) >= abs(d2 - paramDouble)) {
                    d1.changeJoinType()
                }
                if (abs(d3 - paramDouble) < 0.01) {
                    return
                }
            }
        }
    }

    private fun c(): List<TCShuffle> {
        val list: MutableList<TCShuffle>
        if (uuid != null) {
            list = ArrayList()
        } else {
            val c = s1!!.c()
            val c2 = s2!!.c()
            list = ArrayList(c.size + c2.size + 1)
            list.addAll(c)
            list.add(this)
            list.addAll(c2)
        }
        return list
    }
}
