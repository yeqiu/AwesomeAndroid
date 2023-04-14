package com.yeqiu.awesomeandroid

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.yeqiu.common.log
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object FileUtils {


    fun addFileToDownloads(
        context: Context,
        fileName: String,
        fileContent: ByteArray,
        mimeType: String?
    ): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType ?: getMimeType(fileName))
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        var uri: Uri? = null
        resolver.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uri = insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            }
            uri?.let {
                openOutputStream(it)?.use { outputStream ->
                    outputStream.write(fileContent)
                    outputStream.close()
                }
            }
        }

        return uri
    }

    private fun getMimeType(fileName: String): String? {
        var type: String? = null
        val extension = fileName.substringAfterLast(".", "")
        if (extension.isNotEmpty()) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        if (type == null) {
            type = "application/octet-stream"
        }
        return type
    }


    fun getDownloadedFile(
        context: Context,
        fileName: String
    ): File? {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val queryUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.RELATIVE_PATH
        )
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)
        val sortOrder = "${MediaStore.Downloads.DATE_MODIFIED} DESC"

        val cursor = context.contentResolver.query(
            queryUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                val path =
                    it.getString(it.getColumnIndexOrThrow(MediaStore.Downloads.RELATIVE_PATH))
                val file = File(downloadDir, fileName)
                val name = it.getString(it.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME))
                if (file.exists()) {
                    return file
                }
            }
        }

        return null
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun deleteDownloadedFile(context: Context, fileName: String): Boolean {
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)
        val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        var deleted = false

        context.contentResolver.query(uri, null, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                val contentUri = ContentUris.withAppendedId(uri, id)
                context.contentResolver.delete(contentUri, null, null)
                deleted = true
            }
        }

        return deleted
    }


    fun updateTxtFileContent(context: Context, fileName: String, content: String) {

        val uri = getFileUriByFileName(context, fileName) ?: return
        context.contentResolver.openOutputStream(uri, "wt")?.use {
            it.write(content.toByteArray())
            it.flush()
        }


    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun getFileUriByFileName(context: Context, fileName: String): Uri? {

        var result: Uri? = null

        val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)
        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.let { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                log("columnIndex = $columnIndex")
                val id = cursor.getLong(columnIndex)
                result = Uri.withAppendedPath(uri, id.toString())
            }

            cursor.close()
        }

        return result
    }


    fun readFileToString(context: Context, fileName: String): String? {

        val fileUri = getFileUriByFileName(context, fileName)
        if (fileUri == null) {
            return ""
        }
        var inputStream: InputStream? = null
        var reader: BufferedReader? = null
        try {
            inputStream = context.contentResolver.openInputStream(fileUri!!)
            reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
                stringBuilder.append("\n") // 添加换行符
            }
            return stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            reader?.close()
            inputStream?.close()
        }
        return null
    }


}