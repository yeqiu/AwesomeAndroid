package com.yeqiu.awesomeandroid

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import java.io.*

@RequiresApi(Build.VERSION_CODES.Q)
object DownloadFolderFileUtil {

    private val downloadsFolderName = Environment.DIRECTORY_DOWNLOADS
    private const val displayName = MediaStore.Downloads.DISPLAY_NAME
    private const val id = MediaStore.Downloads._ID
    private const val mimeType = MediaStore.Downloads.MIME_TYPE
    private val relativePath by lazy {
        MediaStore.Downloads.RELATIVE_PATH
    }
    private val downloadsUri by lazy {
        MediaStore.Downloads.EXTERNAL_CONTENT_URI
    }
    private val downloadsDriPath by lazy {
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).absolutePath
    }


    fun addFile(context: Context, fileName: String, fileContent: ByteArray): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (addFileAfterQ(context, fileName, fileContent) != null)
        } else {
            addFileBeforeQ(fileName, fileContent)
        }
    }

    fun getFile(
        context: Context,
        fileName: String
    ): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getFileAfterQ(context, fileName)
        } else {
            getFileBeforeQ(fileName)
        }

    }

    /**
     * 更新文件内容,完全覆盖
     * @param context Context
     * @param fileName String
     * @param newContent ByteArray
     */
    fun updateFile(context: Context, fileName: String, newContent: ByteArray): Boolean {

        var result = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val qFile = getQFileByFileName(context, fileName)
            qFile?.apply {
                val outputStream = context.contentResolver.openOutputStream(uri, "wt")
                try {
                    outputStream?.apply {
                        write(newContent)
                        flush()
                    }
                    result = true
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    outputStream?.close()
                }
            }

        } else {
            val file = getFileBeforeQ(fileName)
            file?.apply {
                var outputStream: OutputStream? = null
                try {
                    outputStream = outputStream()
                    outputStream.write(newContent)
                    outputStream.flush()
                    result = true
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    outputStream?.close()
                }
            }
        }
        return result
    }


    fun delFile(context: Context, fileName: String): Boolean {
        var result = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val qFile = getQFileByFileName(context, fileName)
            qFile?.apply {
                context.contentResolver.delete(qFile.uri, null, null)
                result = true
            }
        } else {
            getFileBeforeQ(fileName)?.delete()
            result = true
        }

        return result
    }


    fun getAllFiles(context: Context): ArrayList<File> {

        val fileList = arrayListOf<File>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val queryUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val projection = arrayOf(id, displayName, relativePath)
            val sortOrder = "${MediaStore.Downloads.DATE_MODIFIED} DESC"
            val cursor =
                context.contentResolver.query(queryUri, projection, null, null, sortOrder)

            cursor?.apply {
                if (moveToNext()) {
                    val name = getString(getColumnIndexOrThrow(displayName))
                    val downloadDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadDir, name)
                    if (file.exists()) {
                        fileList.add(file)
                    }

                }
                cursor.close()
            }
        } else {
            val file = File(downloadsDriPath)
            if (file.isDirectory) {
                val listFiles = file.listFiles()
                listFiles?.apply {
                    for (file in this){
                        file?.let {
                            fileList.add(it)
                        }
                    }
                }
            }
        }
        return fileList
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addFileAfterQ(context: Context, fileName: String, fileContent: ByteArray): Uri? {

        //先删除文件
        delFile(context, fileName)

        val values = ContentValues().apply {
            put(displayName, fileName)
            put(mimeType, getMimeType(fileName))
            put(relativePath, downloadsFolderName)
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

    private fun addFileBeforeQ(
        fileName: String,
        fileContent: ByteArray
    ): Boolean {

        var reslut = false

        var fileOutputStream: FileOutputStream? = null
        try {
            val file = File(downloadsDriPath, fileName)
            if (file.exists()) {
                getFileBeforeQ(fileName)?.delete()
            } else {
                file.createNewFile()
            }
            fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(fileContent)
            reslut = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fileOutputStream?.close()
        }

        return reslut

    }

    /**
     * 获取文件类型
     * @param fileName String
     * @return String?
     */
    private fun getMimeType(fileName: String): String {
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


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getFileAfterQ(context: Context, fileName: String): File? {

        getQFileByFileName(context, fileName)?.apply {
            return file
        }

        return null
    }

    private fun getFileBeforeQ(fileName: String): File? {

        val file = File(downloadsDriPath, fileName)


        return if (file.exists()) {
            file
        } else {
            null
        }

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun getQFileByFileName(context: Context, fileName: String): QFile? {
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

            val file = getFileBeforeQ(fileName)
            file?.apply {

                var reader: BufferedReader? = null
                try {
                    reader = bufferedReader()
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
                }
            }
        }
        return fileContent
    }


    data class QFile(val uri: Uri, val id: Long, val path: String, val name: String, val file: File)

}