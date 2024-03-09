package com.example.newcollage.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newcollage.R
import com.example.newcollage.compose.ui.theme.NewCollageTheme

class TestLayoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LayoutScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LayoutScreen(modifier: Modifier = Modifier) {
    TestLayout2(modifier)
}

@Composable
private fun TestLayout2(modifier: Modifier = Modifier) {
    var isFade by remember { mutableStateOf(true) }
    Button(
        onClick = { isFade = !isFade },
        modifier = modifier
            .padding(16.dp)
            .wrapContentSize()
            .animateFade(isFade)
    ) {
        Text(text = "Click Me")
    }
}

//自定义modifier
//带动画的enable，
//用this then实现Modifier的链式调用
@Composable
fun Modifier.animateFade(enable: Boolean): Modifier {
    val alpha by animateFloatAsState(if (enable) 0.5f else 1.0f, label = "")
    return this then Modifier.graphicsLayer { this.alpha = alpha }
}


//布局测试1 modifier的测量顺序
@Composable
private fun TestLayout1(modifier: Modifier) {
    Column(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.test_0),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            // size的enforceIncoming为true，会遵守上一个传入的约束
            // requiredSize为false为false，会严格按照自己的大小
            // 这里size传入的min和max都是100，requiredSize按照自己的200设置大小，
            // 因此Image尺寸200的大小，但是size返回的是100，因此column按照100的位置布局
            // 传递方式：
            // parent ---(maxW, maxH)--> size(100)---(100, 100)--> requiredSize(200) 确定了大小是200
            // 回传
            // requiredSize --> (200, 200) --> size(返回100) --> parent(收到这个child的布局尺寸为100)
            modifier = Modifier
                .size(100.dp)
                .requiredSize(200.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.test_0),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            // 3个size连续使用，由于size会遵守上游传来的大小，因此大小是100
            modifier = Modifier
                .size(100.dp)
                .size(200.dp)
                .size(50.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.test_1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            // 这里size 返回100 padding返回110， clip返回110 因此绘制的时候clip就按照110裁切的
            modifier = Modifier
                .clip(CircleShape)
                .padding(10.dp)
                .size(100.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun GreetingPreview2() {
    NewCollageTheme {
        LayoutScreen()
    }
}