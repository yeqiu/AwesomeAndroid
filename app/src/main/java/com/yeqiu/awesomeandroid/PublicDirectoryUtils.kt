package com.yeqiu.awesomeandroid

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.yeqiu.common.log
import java.io.OutputStream


class PublicDirectoryUtils(private val context: Context) {


    @RequiresApi(Build.VERSION_CODES.Q)
    private val externalUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
    private val path: String = Environment.DIRECTORY_DOWNLOADS


    fun  add(fileName:String,content:String){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            deleteFile(fileName)

            val contentValues = ContentValues()
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            contentValues.put(MediaStore.Downloads.RELATIVE_PATH, path)
            val insertResult: Uri? =
                context.contentResolver.insert(externalUri, contentValues)
            if (insertResult != null) {
                try {
                    val outputStream: OutputStream? =
                        context.contentResolver.openOutputStream(insertResult)
//                    val printWriter = PrintWriter(outputStream)
//                    printWriter.print(content)
//                    printWriter.flush()
//                    printWriter.close()

                    outputStream?.let {
                        outputStream.write(content.toByteArray())
                    }

                    if (outputStream == null){
                        log("outputStream == null")
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }else{
            log("低版本")
        }
    }



    private fun deleteFile(fileName: String) {

    }


}