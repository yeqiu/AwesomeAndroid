package com.yeqiu.awesomeandroid

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

object DownloadFolderFileUtil {

    private val downloadsPath = Environment.DIRECTORY_DOWNLOADS
    private const val displayName = MediaStore.Downloads.DISPLAY_NAME
    private const val id = MediaStore.Downloads._ID
    private const val mimeType = MediaStore.Downloads.MIME_TYPE

    @RequiresApi(Build.VERSION_CODES.Q)
    private const val relativePath = MediaStore.Downloads.RELATIVE_PATH

    @RequiresApi(Build.VERSION_CODES.Q)
    private val downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI


    fun addFile(context: Context, fileName: String, fileContent: ByteArray): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (addFileAfterQ(context, fileName, fileContent) != null)
        } else {
            addFileBeforeQ(context, fileName, fileContent)
        }
    }

    private fun addFileAfterQ(context: Context, fileName: String, fileContent: ByteArray): Uri? {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return null
        }

        val values = ContentValues().apply {
            put(displayName, fileName)
            put(mimeType, getMimeType(fileName))
            put(relativePath, downloadsPath)
        }
        val resolver = context.contentResolver

        resolver.run {
            var resultUri = insert(downloadsUri, values)
            resultUri?.let {
                try {
                    openOutputStream(it)?.use { outputStream: OutputStream ->
                        outputStream.write(fileContent)
                        outputStream.close()
                    }
                } catch (e: Exception) {
                    //写入文件失败
                    // TODO: 需要删除文件
                }

            }
            return resultUri
        }
    }

    private fun addFileBeforeQ(context: Context, fileName: String, content: ByteArray): Boolean {
        log("低版本添加文件")
        // TODO: 低版本添加文件
        //权限

        val absolutePath = Environment.getDownloadCacheDirectory().absolutePath


        return false

    }

    /**
     * 获取文件类型
     * @param fileName String
     * @return String?
     */
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


    fun String.toByteArrayWithUtf8(): ByteArray {
        return this.toByteArray(Charsets.UTF_8)
    }


    fun getFile(
        context: Context,
        fileName: String
    ): File? {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getFileAfterQ(context, fileName)
        } else {
            getFileBeforeQ(context, fileName)
        }

    }

    private fun getFileAfterQ(context: Context, fileName: String): File? {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return null
        }

        getQFileByFileName(context, fileName)?.apply {
            return file
        }

        return null
    }

    private fun getFileBeforeQ(context: Context, fileName: String): File? {

        return null
    }


    fun getQFileByFileName(context: Context, fileName: String): QFile? {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return null
        }
        var qFile: QFile? = null
        val queryUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(id, displayName, relativePath)
        val selection = "$displayName = ?"
        val selectionArgs = arrayOf(fileName)
        val sortOrder = "${MediaStore.Downloads.DATE_MODIFIED} DESC"
        val cursor =
            context.contentResolver.query(queryUri, projection, selection, selectionArgs, sortOrder)

        cursor?.apply {
            if (moveToFirst()) {
                val index = getColumnIndex(id)
                val id = getLong(index)
                val uri = Uri.withAppendedPath(queryUri, id.toString())
                val name = getString(getColumnIndexOrThrow(displayName))
                val downloadDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadDir, name)
                if (file.exists()) {
                    qFile = QFile(uri, id, file.absolutePath, name, file)
                }

            }
            cursor.close()
        }
        return qFile
    }


    fun getFileStr(context: Context, fileName: String): String {

        var fileContent = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val qFile = getQFileByFileName(context, fileName)
            var inputStream: InputStream? = null
            var reader: BufferedReader? = null
            qFile?.apply {
                try {
                    inputStream = context.contentResolver.openInputStream(uri)
                    reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                    val stringBuffer = StringBuffer()

                    reader?.apply {
                        while (true) {
                            val line = readLine() ?: break
                            stringBuffer.append(line)
                            stringBuffer.append("\n")
                        }
                    }
                    fileContent = stringBuffer.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    reader?.close()
                    inputStream?.close()
                }
            }

        } else {

            // TODO: 低版本获取文件

        }

        return fileContent
    }


    /**
     * 更新文件内容,完全覆盖
     * @param context Context
     * @param fileName String
     * @param newContent ByteArray
     */
    fun updateFile(context: Context, fileName: String, newContent: ByteArray): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val qFile = getQFileByFileName(context, fileName)
            qFile?.apply {
                val outputStream = context.contentResolver.openOutputStream(uri, "wt")
                try {
                    outputStream?.apply {
                        write(newContent)
                        flush()
                    }
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    outputStream?.close()
                }
            }

        } else {
            // TODO: 低版本
        }


        return false

    }

    fun delFile(context: Context, fileName: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val qFile = getQFileByFileName(context, fileName)
            qFile?.apply {
                context.contentResolver.delete(qFile.uri, null, null)
                return true
            }

        } else {
            // TODO: 低版本删除
        }

        return false
    }


    data class QFile(val uri: Uri, val id: Long, val path: String, val name: String, val file: File)

}