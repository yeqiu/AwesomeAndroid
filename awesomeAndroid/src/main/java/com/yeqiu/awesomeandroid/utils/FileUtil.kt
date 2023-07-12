package com.yeqiu.awesomeandroid.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import java.io.*

object FileUtil {

    /**
     * 获取files目录
     * @param context Context
     * @return File?
     */
    fun getFileFolder(context: Context, subdirectory: String = ""): File? {

        return context.getExternalFilesDir(subdirectory)
    }

    /**
     * 获取cache目录
     * @param context Context
     * @return File?
     */
    fun getCacheFolder(context: Context): File? {

        return context.externalCacheDir
    }

    /**
     * 获取app目录
     * @param context Context
     * @return File?
     */
    fun getAppFolder(context: Context): File? {

        return context.externalCacheDir?.parentFile
    }

    /**
     * 删除文件，包含文件夹
     * @param file File
     * @param maxDay Int
     */
    @JvmOverloads
    fun delFile(file: File, maxDay: Int = 0) {

        if (!file.exists()) {
            return
        }

        if (file.isDirectory) {

            val childFiles = file.listFiles()
            childFiles?.let {
                for (childFile in childFiles) {
                    if (file.isDirectory) {
                        delFile(childFile, maxDay)
                    } else {
                        deleteFilesOlderThanDays(childFile, maxDay)
                    }
                }
            } ?: deleteFilesOlderThanDays(file, maxDay)

        } else {
            deleteFilesOlderThanDays(file, maxDay)
        }

    }


    private fun deleteFilesOlderThanDays(file: File, maxDay: Int = 0) {

        if (!file.exists()) {
            return
        }
        val existTime = System.currentTimeMillis() - file.lastModified()
        if (existTime > (maxDay * 24 * 60 * 60 * 1000)) {
            file.delete()
        }
    }


    /**
     * 复制文件
     * @param file File
     * @param path String
     * @return Boolean
     */
    fun copyFile(source: File, dest: File): Boolean {

        try {
            FileInputStream(source).use { inputStream ->
                FileOutputStream(dest).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    /**
     * 获取文件文本
     * @param file File
     * @return String?
     */
    fun readFileText(file: File): String {
        return if (file.exists() && file.isFile) {
            file.readText()
        } else {
            return ""
        }
    }

    /**
     * 获取文件文本
     * @param context Context
     * @param uri Uri
     * @return String
     */
    fun readFileText(context: Context, uri: Uri): String {

        return context.contentResolver.openInputStream(uri)?.let { inputStream ->
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            return bufferedReader.readText()
        } ?: ""
    }


    fun copyFileFromUri(context: Context, uri: Uri, file: File): Boolean {

        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return context.contentResolver.openInputStream(uri)?.let { inputStream ->
                    FileOutputStream(file).let { fileOutputStream ->
                        inputStream.copyTo(fileOutputStream)
                        inputStream.close()
                        fileOutputStream.close()
                        return true
                    }
                } ?: false
            } else {
                //直接复制
                return uri.path?.let {
                    val originalFile = File(it)
                    return copyFile(originalFile, file)
                } ?: false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }


    fun saveFile(path: String, data: ByteArray): Boolean {

        try {
            val file = File(path)
            if (file.isDirectory) {
                //目标目录是文件夹，无法存储
                return false
            }
            if (file.isFile) {
                //文件存在，删除旧文件
                delFile(file)
            }
            file.createNewFile()
            file.writeBytes(data)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }


}