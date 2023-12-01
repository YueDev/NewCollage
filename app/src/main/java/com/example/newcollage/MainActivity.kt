package com.example.newcollage

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newcollage.bean.CollageItem
import com.example.newcollage.bean.CollageLayout
import com.example.newcollage.databinding.ActivityMainBinding
import com.example.newcollage.repository.ImageRepository

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val items = ImageRepository.imageResIds.shuffled().take(4).mapIndexed { index, res ->
            val bitmap = BitmapFactory.decodeResource(resources, res)
            CollageItem(bitmap, getLayout(index))
        }
        binding.collageView.setData(items)
    }

    private fun getLayout(index: Int): CollageLayout {
        var l = 0f
        var t = 0f
        var r = 0f
        var b = 0f
        when (index) {
            0 -> {
                r = 500f
                b = 500f
            }
            1 -> {
                l = 500f
                r = 1000f
                b = 500f
            }
            2 -> {
                t = 500f
                r = 500f
                b = 1000f
            }
            3 -> {
                l = 500f
                t = 500f
                r = 1000f
                b = 1000f
            }
        }
        return CollageLayout("index", l, t, r, b)
    }

}