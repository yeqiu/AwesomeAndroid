package com.yeqiu.awesomeandroid

import android.content.Context
import android.view.View
import com.yeqiu.awesomeandroid.app.databinding.ActivityAndroidQFileBinding
import com.yeqiu.awesomeandroid.utils.DownloadFolderFileUtil
import com.yeqiu.awesomeandroid.utils.DownloadFolderFileUtil.toByteArrayWithUtf8
import com.yeqiu.common.BaseBindActivity
import com.yeqiu.common.log
import com.yeqiu.common.toast


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
        val fileContent = "https://github.com/yeqiu/AwesomeAndroid"


        val addFile =
           DownloadFolderFileUtil.addFile(context, fileName, fileContent.toByteArrayWithUtf8())

        log("添加结果 = $addFile")

        if (addFile){
            toast("添加成功，已经在Download目录下生成example.txt")
        }else{
            toast("添加失败")
        }


    }


    private fun edit() {

        val fileName = "example.txt"
        val fileContent = "https://github.com/yeqiu"
        val context: Context = this

        val updateFile =
            DownloadFolderFileUtil.updateFile(context, fileName, fileContent.toByteArrayWithUtf8())
        log("修改结果 = $updateFile")
        toast("修改结果 = $updateFile")

    }

    private fun del() {
        val context: Context = this
        val fileName = "example.txt"
        val delFile = DownloadFolderFileUtil.delFile(context, fileName)
        log("删除结果 = $delFile")
        toast("删除结果 = $delFile")
    }

    private fun get() {


        val context: Context = this
        val fileName = "example.txt"
        val file = DownloadFolderFileUtil.getFile(context, fileName)
        if (file != null) {
            // 处理文件对象
            log("成功获取文件：地址为 = ${file.absolutePath}")
            val readFileToString = DownloadFolderFileUtil.getFileStr(context, fileName)
            log("文件内容为 = $readFileToString")
            toast("文件内容为 = $readFileToString")
        } else {
            // 文件不存在，需要处理异常情况
            log("file == null")
            toast("file == null")
        }


    }


}