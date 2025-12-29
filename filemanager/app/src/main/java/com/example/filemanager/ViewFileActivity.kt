package com.example.filemanager

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ViewFileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val filePath = intent.getStringExtra("file_path") ?: return finish()
        val file = File(filePath)
        
        if (!file.exists()) {
            finish()
            return
        }
        
        val extension = file.extension.lowercase()
        
        when {
            extension == "txt" -> {
                setContentView(R.layout.activity_view_text)
                val tvContent: TextView = findViewById(R.id.tvContent)
                try {
                    tvContent.text = file.readText()
                } catch (e: Exception) {
                    tvContent.text = "Lỗi đọc file: ${e.message}"
                }
            }
            extension in listOf("jpg", "jpeg", "png", "bmp") -> {
                setContentView(R.layout.activity_view_image)
                val imgView: ImageView = findViewById(R.id.imgView)
                try {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        imgView.setImageBitmap(bitmap)
                    } else {
                        finish()
                    }
                } catch (e: Exception) {
                    finish()
                }
            }
            else -> {
                finish()
            }
        }
        
        supportActionBar?.title = file.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

