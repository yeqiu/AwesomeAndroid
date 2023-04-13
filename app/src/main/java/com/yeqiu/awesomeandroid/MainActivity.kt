package com.yeqiu.awesomeandroid

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.annotation.RequiresApi
import com.yeqiu.awesomeandroid.databinding.ActivityMainBinding
import com.yeqiu.common.BaseBindActivity
import com.yeqiu.common.log
import com.yeqiu.common.toast
import com.yeqiu.screenrecorder.OnScreenRecordCallBack
import com.yeqiu.screenrecorder.ScreenRecorderHelper
import com.yeqiu.screenrecorder.utils.openVideoByIntent
import java.io.File
import java.io.OutputStream
import java.io.PrintWriter


class MainActivity : BaseBindActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun init() {


        startActivity(Intent(this, AndroidQFileActivity::class.java))

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.statusBarColor = Color.TRANSPARENT


        binding.btPrepare.setOnClickListener(this)
        binding.btStartRecording.setOnClickListener(this)
        binding.btStopRecording.setOnClickListener(this)
        binding.btScreenshot.setOnClickListener(this)
        binding.btStopServer.setOnClickListener(this)


        ScreenRecorderHelper.getInstance().init(this)


        ScreenRecorderHelper.getInstance()
            .setOnScreenRecordingCallBack(object : OnScreenRecordCallBack() {

                override fun onRecordingStatusChange(status: ScreenRecorderHelper.ScreenRecordingStatus) {
                    super.onRecordingStatusChange(status)

                    log("当前状态：${status.name}")
                    when (status) {
                        ScreenRecorderHelper.ScreenRecordingStatus.Idle -> {
                            binding.tvStatus.text =
                                "当前状态：空闲"
                            binding.btStartRecording.isClickable = false
                            binding.btStopRecording.isClickable = false
                            binding.btScreenshot.isClickable = false
                        }

                        ScreenRecorderHelper.ScreenRecordingStatus.Prepare -> {
                            binding.tvStatus.text =
                                "当前状态：准备就绪"
                            binding.btPrepare.text = "已准备"
                            binding.btStartRecording.text = "开始录屏"
                            binding.btStartRecording.isClickable = true
                            binding.btStopRecording.isClickable = true
                            binding.btScreenshot.isClickable = true
                        }
                        ScreenRecorderHelper.ScreenRecordingStatus.Recording -> {
                            binding.tvStatus.text =
                                "当前状态：录屏中"
                            binding.btStartRecording.text = "暂停"
                        }
                        ScreenRecorderHelper.ScreenRecordingStatus.Pause -> {
                            binding.tvStatus.text =
                                "当前状态：录屏暂停"
                            binding.btStartRecording.text = "继续"
                        }
                        ScreenRecorderHelper.ScreenRecordingStatus.Screenshotting -> {
                            binding.tvStatus.text =
                                "当前状态：截屏中"
                        }
                    }
                }

                override fun onError(actionType: Int, msg: String) {
                    super.onError(actionType, msg)
                    toast("${msg}")
                    binding.tvInfo.text = msg
                }

                override fun onRecordingStatusResult(file: File) {
                    super.onRecordingStatusResult(file)

                    binding.tvInfo.text = "录屏成功，文件路径为：${file.absolutePath}"
                    openVideoByIntent(this@MainActivity, file)
                }

                override fun onScreenshotResult(file: File) {
                    super.onScreenshotResult(file)
                    binding.tvInfo.text = "截图成功，文件路径为：${file.absolutePath}"
                }

            })


    }

    override fun onClick(view: View?) {
        view?.let {
            if (view == binding.btPrepare) {
                prepare()
            } else if (view === binding.btStartRecording) {
                start()
            } else if (view === binding.btStopRecording) {
                stop()
            } else if (view === binding.btScreenshot) {
                screenshot()
            } else if (view === binding.btStopServer) {
                stopServer()
            }
        }
    }


    private fun prepare() {

        if (true) {
            add()
            return
        }

        if (ScreenRecorderHelper.getInstance()
                .getStatus() == ScreenRecorderHelper.ScreenRecordingStatus.Idle
        ) {
            ScreenRecorderHelper.getInstance().prepare(true)
        }
    }

    private fun start() {


        if (ScreenRecorderHelper.getInstance()
                .getStatus() == ScreenRecorderHelper.ScreenRecordingStatus.Idle
        ) {

            toast("请先点击准备，初始化服务")

            binding.tvInfo.text = "请先点击准备，初始化服务"
        }


        when (ScreenRecorderHelper.getInstance().getStatus()) {

            ScreenRecorderHelper.ScreenRecordingStatus.Prepare -> {
                //开始
                ScreenRecorderHelper.getInstance().startRecording()
            }
            ScreenRecorderHelper.ScreenRecordingStatus.Pause -> {
                //开始
                ScreenRecorderHelper.getInstance().startRecording()
            }
            ScreenRecorderHelper.ScreenRecordingStatus.Recording -> {
                //录屏中，暂停
                ScreenRecorderHelper.getInstance().pauseRecording()
            }
            else -> {}
        }
    }


    private fun stop() {

        ScreenRecorderHelper.getInstance().stopRecording()
    }

    private fun screenshot() {
        ScreenRecorderHelper.getInstance().screenshot()
    }

    private fun stopServer() {

        if (true) {
            del()
            return
        }

        ScreenRecorderHelper.getInstance().release()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun add() {
        // 获取 ContentResolver
        val resolver: ContentResolver = contentResolver
        // 创建新文件
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "myFile.txt")
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        // 插入新文件到 MediaStore
        val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        // 打开文件输出流，向文件中写入数据
        uri?.let {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            val data = "Hello, World!"
            val printWriter = PrintWriter(outputStream)
            printWriter.print(data)
            printWriter.flush()
            printWriter.close()
        }

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun del() {
        // 获取 ContentResolver
        val resolver: ContentResolver = contentResolver
        // 创建查询，以检索要删除的文件的信息
        val queryUri: Uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("myFile.txt")
        val cursor = resolver.query(queryUri, projection, selection, selectionArgs, null)
        // 删除符合条件的文件
        cursor?.use {
            if (it.moveToFirst()) {
                val idIndex: Int = it.getColumnIndex(MediaStore.Downloads._ID)
                val fileId: Long = it.getLong(idIndex)
                val deleteUri: Uri = ContentUris.withAppendedId(queryUri, fileId)
                resolver.delete(deleteUri, null, null)
            } else {
                // 找不到要删除的文件
            }
        }

    }


}


