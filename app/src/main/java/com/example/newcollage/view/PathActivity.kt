package com.example.newcollage.view

import android.graphics.Path
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.graphics.path.iterator
import com.example.newcollage.R

class PathActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_path)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val path = Path()
        path.moveTo(100f, 100f)
        path.lineTo(100f, 200f)
        path.lineTo(200f, 200f)
        path.close()


        val pathIterator = path.iterator()
        pathIterator.forEach {
            Log.d("YUEDEVTAG", "${it.type}")
            Log.d("YUEDEVTAG", it.points.contentToString())
        }
    }
}