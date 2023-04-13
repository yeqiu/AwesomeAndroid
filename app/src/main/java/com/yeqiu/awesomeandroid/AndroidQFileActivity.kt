package com.yeqiu.awesomeandroid

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import androidx.core.content.ContextCompat
import com.yeqiu.awesomeandroid.databinding.ActivityAndroidQFileBinding
import com.yeqiu.common.BaseBindActivity
import com.yeqiu.common.log


class AndroidQFileActivity : BaseBindActivity<ActivityAndroidQFileBinding>(), View.OnClickListener {
    override fun init() {

        binding.btAdd.setOnClickListener(this)
        binding.btEdit.setOnClickListener(this)
        binding.btDel.setOnClickListener(this)
        binding.btGet.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        view?.let {
            if (view == binding.btAdd) {
                add()
            } else if (view === binding.btEdit) {
                edit()
            } else if (view === binding.btDel) {
                del()
            } else if (view === binding.btGet) {
                get()
            }
        }
    }




    private fun add() {

        val context: Context = this
        val fileName = "example.txt"
        val fileContent = "This is an example file.".toByteArray()
        val mimeType = "text/plain"
        FileUtils.addFileToDownloads(context, fileName, fileContent, mimeType)


    }

    private fun edit() {

        val fileName = "example.txt"
        val fileContent = "100"
        val context: Context =this
        FileUtils.updateTxtFileContent(context,fileName,fileContent)

    }

    private fun del() {
        val context: Context =this
        val fileName = "example.txt"
        FileUtils.deleteDownloadedFile(context, fileName)
    }

    private fun get() {



        val context: Context =this
        val fileName = "example.txt"
        val file = FileUtils.getDownloadedFile(context, fileName)
        if (file != null) {
            // 处理文件对象
            log("file != null")
            log("file = ${file.name}")
        } else {
            // 文件不存在，需要处理异常情况
            log("file == null")
        }



    }




}