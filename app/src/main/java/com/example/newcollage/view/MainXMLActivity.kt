package com.example.newcollage.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.newcollage.bean.CollageItem
import com.example.newcollage.compose.CodeLabActivity
import com.example.newcollage.compose.ComposeActivity
import com.example.newcollage.compose.MySootheActivity
import com.example.newcollage.compose.StateActivity
import com.example.newcollage.databinding.ActivityMainBinding
import com.example.newcollage.repository.ImageRepository


/// no used, used compose activity instead.
class MainXMLActivity : AppCompatActivity() {

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

        binding.codeLabButton.setOnClickListener {
            val intent = Intent(this, CodeLabActivity::class.java)
            startActivity(intent)
        }

        binding.mySootheButton.setOnClickListener {
            val intent = Intent(this, MySootheActivity::class.java)
            startActivity(intent)
        }

        binding.stateButton.setOnClickListener {
            val intent = Intent(this, StateActivity::class.java)
            startActivity(intent)
        }

        val items = ImageRepository.imageResIds.shuffled().map { res ->
            val bitmap = BitmapFactory.decodeResource(resources, res)
            CollageItem(bitmap)
        }

        binding.collageView.setData(items)
    }

}