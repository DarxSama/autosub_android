package com.example.autosubtitle

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class UploadActivity : AppCompatActivity() {

    private var selectedVideoUri: Uri? = null // เก็บ URI ของไฟล์ที่เลือก

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        // จัดการ Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // กำหนดนโยบายเพื่อให้สามารถทำงานใน Thread หลักได้
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // อ้างอิง View ใน Layout
        val selectFileButton = findViewById<Button>(R.id.selectFileButton)
        val uploadFileButton = findViewById<Button>(R.id.uploadFileButton)
        val selectedFileTextView = findViewById<TextView>(R.id.selectedFileTextView)
        val imageButtonUpload = findViewById<ImageButton>(R.id.imageButtonUpload)

        // ตั้งค่าปุ่มเลือกไฟล์
        selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "video/mp4" // จำกัดให้เลือกเฉพาะไฟล์ MP4
            startActivityForResult(intent, 100) // ใช้ request code 100
        }

        // ตั้งค่าปุ่มย้อนกลับ
        imageButtonUpload.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // ปิดหน้าปัจจุบันเพื่อไม่ให้กลับมาหน้านี้
        }

        // ตั้งค่าปุ่มอัปโหลดไฟล์
        uploadFileButton.setOnClickListener {
            if (selectedVideoUri == null) {
                // แจ้งเตือนว่าผู้ใช้ยังไม่ได้เลือกไฟล์
                Toast.makeText(this, "กรุณาเลือกวิดีโอ", Toast.LENGTH_SHORT).show()
            } else {
                // อัปโหลดวิดีโอไปยัง API
                uploadVideoToAPI(selectedVideoUri!!)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // ตรวจสอบว่าเป็นผลลัพธ์จากการเลือกไฟล์
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedVideoUri = data.data // บันทึก URI ของไฟล์ที่เลือก
            val filePath = selectedVideoUri?.path // ดึง path ของไฟล์
            val selectedFileTextView = findViewById<TextView>(R.id.selectedFileTextView)

            // แสดง path ของไฟล์ใน TextView
            selectedFileTextView.text = filePath
        }
    }

    // ฟังก์ชันสำหรับอัปโหลดวิดีโอไปยัง API
    private fun uploadVideoToAPI(videoUri: Uri) {
        try {
            val videoFile = getFileFromUri(videoUri) // แปลง URI เป็น File

            // สร้าง OkHttpClient พร้อมกำหนด timeout
            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // กำหนดเวลาเชื่อมต่อ
                .writeTimeout(60, TimeUnit.SECONDS)  // กำหนดเวลาเขียนข้อมูล
                .readTimeout(60, TimeUnit.SECONDS)   // กำหนดเวลาอ่านข้อมูล
                .build()

            val fileBody = RequestBody.create("video/mp4".toMediaTypeOrNull(), videoFile)

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("video", videoFile.name, fileBody)
                .build()

            val request = Request.Builder()
                .url("http://10.10.9.48:5000/video") // เปลี่ยนเป็น IP/URL ของเซิร์ฟเวอร์ Python
                .post(multipartBody)
                .build()

            // ใช้ Thread สำหรับการเรียก API
            Thread {
                try {
                    val response = client.newCall(request).execute()

                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "อัปโหลดสำเร็จ!", Toast.LENGTH_SHORT).show()

                            // ส่ง URI ของวิดีโอไปยัง EditActivity
                            val intent = Intent(this, EditActivity::class.java)
                            intent.putExtra("videoUri", videoUri.toString()) // ส่ง URI ของไฟล์วิดีโอ
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "อัปโหลดไม่สำเร็จ: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ฟังก์ชันแปลง URI เป็น File
    private fun getFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "temp_video.mp4") // สร้างไฟล์ชั่วคราวใน Cache Directory
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream) // คัดลอกเนื้อหาจาก URI ลงไฟล์ชั่วคราว
        outputStream.close()
        inputStream?.close()
        return file
    }
}
