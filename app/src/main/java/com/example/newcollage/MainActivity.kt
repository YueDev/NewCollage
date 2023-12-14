package com.example.newcollage

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newcollage.bean.CollageItem
import com.example.newcollage.compose.ComposeActivity
import com.example.newcollage.databinding.ActivityMainBinding
import com.example.newcollage.repository.ImageRepository

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.composeButton.setOnClickListener {
            val intent = Intent(this, ComposeActivity::class.java)
            startActivity(intent)
        }

        val items = ImageRepository.imageResIds.shuffled().map {res ->
            val bitmap = BitmapFactory.decodeResource(resources, res)
            CollageItem(bitmap)
        }
        binding.collageView.setData(items)
    }

}