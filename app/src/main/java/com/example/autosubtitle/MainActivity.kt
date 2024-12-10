package com.example.autosubtitle

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // จัดการ padding สำหรับระบบ
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // สำหรับงานที่ไม่ซิงโครนัส
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // เพิ่มการจัดการคลิกที่ปุ่ม imageButtonMain
        val imageButtonMain = findViewById<ImageButton>(R.id.imageButtonMain)
        imageButtonMain.setOnClickListener {
            // เปลี่ยนไปที่ LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // ปิด MainActivity ถ้าต้องการ
        }

        // เพิ่มการจัดการคลิกที่ปุ่ม buttonUp
        val buttonUp = findViewById<Button>(R.id.buttonUp)
        buttonUp.setOnClickListener {
            // เปลี่ยนไปที่ UploadActivity
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }

        // เพิ่มการจัดการคลิกที่ปุ่ม buttonDown
        val buttonDown = findViewById<Button>(R.id.buttonDown)
        buttonDown.setOnClickListener {
            // เปลี่ยนไปที่ UploadActivity
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }
    }
}
