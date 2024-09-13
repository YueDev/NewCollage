package com.example.newcollage.compose

import android.graphics.RuntimeShader
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.tooling.preview.Preview
import com.example.newcollage.compose.ui.theme.NewCollageTheme


//根据手指绘制彩色图案

class ShaderRuntimeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewCollageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BrushScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BrushScreen(modifier: Modifier = Modifier) {
    MyCanvas(modifier = modifier.fillMaxSize())
}

@Composable
fun MyCanvas(modifier: Modifier = Modifier) {

    var time by remember {
        mutableLongStateOf(0L)
    }

    Canvas(modifier = modifier) {
//        drawIntoCanvas {
            colorShader.setFloatUniform("iResolution", size.width, size.height)
            colorShader.setFloatUniform("iTime", time.toFloat())
            time++
            drawCircle(brush = shaderBrush)
//        }
    }
}


@Preview(showBackground = true)
@Composable
fun BrushScreenPreview() {
    NewCollageTheme {
        BrushScreen()
    }
}


//fragcoord给的是整个视口的坐标，iResolution是我传进来的视口分辨率
//两者相除就是片段着色器常用的纹理的uv了
//注意 fragcoord 原点在左上角 和metal d3d vulkin 的一样,和opengl不一样。

//https://shaders.skia.org/?id=de2a4d7d893a7251eb33129ddf9d76ea517901cec960db116a1bbd7832757c1f
private const val SHADER = """
    uniform float2 iResolution;
    uniform float iTime;
    
    
    float f(vec3 p) {
        p.z -= iTime * 1.;
        float a = p.z * .1;
        p.xy *= mat2(cos(a), sin(a), -sin(a), cos(a));
        return .1 - length(cos(p.xy) + sin(p.yz));
    }
    
    half4 main(float2 fragcoord) { 
        vec3 d = .5 - fragcoord.xy1 / iResolution.y;
        vec3 p=vec3(0);
        for (int i = 0; i < 32; i++) {
          p += f(p) * d;
        }
        return ((sin(p) + vec3(2, 5, 12)) / length(p)).xyz1;
    }
"""

private val colorShader by lazy { RuntimeShader(SHADER) }

private val shaderBrush by lazy { ShaderBrush(colorShader) }