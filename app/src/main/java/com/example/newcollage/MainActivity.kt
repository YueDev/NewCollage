package com.example.newcollage

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newcollage.bean.CollageItem
import com.example.newcollage.databinding.ActivityMainBinding
import com.example.newcollage.repository.ImageRepository

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val items = ImageRepository.imageResIds.shuffled().map {res ->
            val bitmap = BitmapFactory.decodeResource(resources, res)
            CollageItem(bitmap)
        }
        binding.collageView.setData(items)
    }

}