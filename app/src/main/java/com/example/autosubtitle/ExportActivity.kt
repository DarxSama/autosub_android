package com.example.autosubtitle

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_export)

        // จัดการ WindowInsets เพื่อการจัดเลย์เอาต์
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // สำหรับงานที่ไม่ซิงโครนัส
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // ดึงปุ่ม ImageButton และกำหนดการทำงานให้ย้อนกลับไปที่หน้า UploadActivity
        val imageButtonMain = findViewById<ImageButton>(R.id.imageButtonMain)
        imageButtonMain.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
            finish() // ปิดหน้าปัจจุบันเพื่อไม่ให้กลับมา
        }

        // ดึงปุ่ม Button และกำหนดการทำงานให้ย้อนกลับไปที่หน้า MainActivity
        val backButton = findViewById<Button>(R.id.button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // ปิดหน้าปัจจุบันเพื่อไม่ให้กลับมา
        }
    }
}
