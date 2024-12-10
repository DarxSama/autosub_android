package com.example.autosubtitle

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class EditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // URL ของวิดีโอที่จะแสดง
        val videoUriString = "http://10.10.9.48:4000/get-video"
        if (videoUriString != null) {
            val videoUri = Uri.parse(videoUriString)
            playVideo(videoUri)
        } else {
            Toast.makeText(this, "ไม่พบไฟล์วิดีโอ", Toast.LENGTH_SHORT).show()
        }

        // ปุ่มดาวน์โหลดวิดีโอจาก API
        val downloadButton = findViewById<Button>(R.id.button)
        downloadButton.setOnClickListener {
            downloadVideoAndReplace("http://10.10.9.48:4000/get-video")
        }

        // ปุ่มส่งออกวิดีโอไปยังโฟลเดอร์ Downloads
        val exportButton = findViewById<Button>(R.id.button)
        exportButton.setOnClickListener {
            downloadVideoToUserDownloads("http://10.10.9.48:4000/get-video")
            val intent = Intent(this, ExportActivity::class.java)
            startActivity(intent)
        }

        // ปุ่มย้อนกลับไปยัง MainActivity
        val imageButtonMain = findViewById<ImageButton>(R.id.imageButtonMain)
        imageButtonMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // ฟังก์ชันสำหรับเล่นวิดีโอ
    private fun playVideo(videoUri: Uri) {
        val videoView = findViewById<VideoView>(R.id.videoView)
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true // เล่นซ้ำ
            mediaPlayer.start()
        }

        videoView.setOnCompletionListener {
            Toast.makeText(this, "วิดีโอเล่นจบแล้ว", Toast.LENGTH_SHORT).show()
        }

        videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "ไม่สามารถเล่นวิดีโอได้", Toast.LENGTH_SHORT).show()
            true
        }
    }

    // ฟังก์ชันสำหรับดาวน์โหลดวิดีโอและแทนที่วิดีโอปัจจุบัน
    private fun downloadVideoAndReplace(videoUrl: String) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(videoUrl)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()
                    val file = File(
                        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        "downloaded_video.mp4"
                    )

                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    outputStream.close()
                    inputStream?.close()

                    runOnUiThread {
                        Toast.makeText(this, "ดาวน์โหลดสำเร็จ!", Toast.LENGTH_SHORT).show()

                        // เล่นวิดีโอที่ดาวน์โหลดมาใหม่
                        val videoUri = Uri.fromFile(file)
                        playVideo(videoUri)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "ไม่สามารถดาวน์โหลดไฟล์ได้: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // ฟังก์ชันสำหรับดาวน์โหลดวิดีโอไปยังโฟลเดอร์ Downloads ของผู้ใช้
    private fun downloadVideoToUserDownloads(videoUrl: String) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(videoUrl)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()
                    val fileName = "exported_video.mp4"

                    // บันทึกไฟล์ในโฟลเดอร์ Downloads ของผู้ใช้
                    val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDirectory, fileName)

                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    outputStream.close()
                    inputStream?.close()

                    runOnUiThread {
                        Toast.makeText(this, "ดาวน์โหลดสำเร็จ! ไฟล์ถูกบันทึกที่: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "ไม่สามารถดาวน์โหลดไฟล์ได้: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
