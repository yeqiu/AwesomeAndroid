package com.yeqiu.screenrecorder.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File


fun openVideoByIntent(context: Context, file: File) {

    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uriForFile =
            FileProvider.getUriForFile(context, context.packageName + ".provider", file)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uriForFile, "video/*")
        context.startActivity(intent)
    } catch (e: Exception) {
        val message: String? = e.message
        message?.let {
            log("e == $message")
        }
    }
}