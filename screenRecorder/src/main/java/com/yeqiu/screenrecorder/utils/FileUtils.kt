package com.yeqiu.screenrecorder.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

internal fun getScreenRecordingFilePath(context: Context): String {

    val dirFile = File(context.externalCacheDir, Environment.DIRECTORY_MOVIES)
    dirFile.mkdirs()
    val fileName = getDataStr(System.currentTimeMillis())
    return File(dirFile, "screenRecording_$fileName.mp4").absolutePath
}


internal fun getScreenshotFilePath(context: Context): String {


    val dirFile = File(context.externalCacheDir, Environment.DIRECTORY_PICTURES)
    dirFile.mkdirs()
    val fileName = getDataStr(System.currentTimeMillis())
    return File(dirFile, "screenshot_$fileName.png").absolutePath
}

private fun getDataStr(time: Long): String {

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = Date(time)
    return simpleDateFormat.format(date)

}