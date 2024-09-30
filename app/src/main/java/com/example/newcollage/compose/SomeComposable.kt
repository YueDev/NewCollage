package com.example.newcollage.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview


//addPath关于path的方向：
//顺时针+逆时针，相当于顺时针里 把逆时针的形状扣出来了
//逆时针+顺时针，相当于逆时针里 把顺时针的形状扣出来了
//在嵌套几层就有点乱了。

//最好使用Path.combine来合并两个path，这个只根据path的形状来合并的，会忽略path的方向
//val path = Path.combine(PathOperation.Union, path1, path2)

//又看了看，path又个filltype，默认是非零填充，是上边这个效果
//还有一种常用的是奇偶填充，这个应该是忽略各种方向的
//还是挺麻烦的，Path.combine算了

// 结论：android compose合并两个path，请使用Path.combine
// android view的path没试，算了吧 不管了。还有人写android view？
@Composable
fun PathAddComposable(modifier: Modifier = Modifier) {

    //PathFillType 好像只对系统的那几个path管用，我自己写的path1和path2不太行
    Canvas(modifier = modifier.fillMaxSize()) {
        val path = Path().apply {
            addRect(Rect(100f, 100f, 300f, 300f))
            addRect(Rect(200f, 200f, 400f, 400f))

            // 使用 PathFillType.EvenOdd  相交的部分会被扣掉
            //.NonZero 两个叠加了
            fillType = PathFillType.NonZero
        }
        drawPath(path = path, color = Color.Red.copy(alpha = 0.35f), style = Fill)
    }

}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PathAddPreview() {
    PathAddComposable()
}