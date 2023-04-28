package com.yeqiu.awesomeandroid

import android.content.Context
import android.view.View
import com.yeqiu.awesomeandroid.app.databinding.ActivityAndroidQFileBinding
import com.yeqiu.awesomeandroid.utils.DownloadFolderFileUtil
import com.yeqiu.awesomeandroid.utils.DownloadFolderFileUtil.toByteArrayWithUtf8
import com.yeqiu.common.BaseBindActivity
import com.yeqiu.common.log
import java.text.SimpleDateFormat


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
        val fileContent = "Android白板注册方案\n" +
                "\n" +
                "\n" +
                "1.首次启动时候用户输入注册码，校验通过后保存为文件 authorization.bs,同时复制备份文件backupAthorization.bs(备份文件保存到公共储存空间)\n" +
                "authorization内容为：校验通过时的时间戳(需白板程序提供)\n" +
                "backupAthorization内容为：校验通过时的时间戳，app内部文件的创建时间戳\n" +
                "\n" +
                "2.白板app每次启动后都会更新 backupAthorization 文件，目的为了更新文件的创建日期。\n" +
                "\n" +
                "3.用户卸载重装后，此时 authorization.bs被删除，使用备份的 backupAthorization 文件中的时间戳，\n" +
                "同时判断backupAthorization创建时间和authorization的创建时间是否小于七天。\n" +
                "\n" +
                "4.重装系统后，因无法 authorization.bs 和 backupAthorization.bs 均被删除，无法校验。启用解绑流程。"
        val mimeType = "text/plain"
        val charset = Charsets.UTF_8
//        FileUtils.addFileToDownloads(context, fileName, fileContent.toByteArray(charset), mimeType)


        val addFile =
           DownloadFolderFileUtil.addFile(context, fileName, fileContent.toByteArrayWithUtf8())

        log("添加结果 = $addFile")

    }


    private fun edit() {

        val fileName = "example.txt"
        val fileContent = "张君1的账号为3006799；初始密码为：Hcbp3z\n" +
                "张君2的账号为3006233；初始密码为：twYmvU\n" +
                "张君3的账号为3006236；初始密码为：rwB8gq\n" +
                "张君4的账号为3006802；初始密码为：QV2X2W"
        val context: Context = this
//        FileUtils.updateTxtFileContent(context, fileName, fileContent)

        val updateFile =
          DownloadFolderFileUtil.updateFile(context, fileName, fileContent.toByteArrayWithUtf8())
        log("修改结果 = $updateFile")

    }

    private fun del() {
        val context: Context = this
        val fileName = "example.txt"
//        FileUtils.deleteDownloadedFile(context, fileName)
        val delFile = DownloadFolderFileUtil.delFile(context, fileName)
        log("删除结果 = $delFile")
    }

    private fun get() {


        val context: Context = this
        val fileName = "example.txt"
//        val file = FileUtils.getDownloadedFile(context, fileName)
        val file = com.yeqiu.awesomeandroid.utils.DownloadFolderFileUtil.getFile(context, fileName)
        if (file != null) {
            // 处理文件对象
            log("成功获取文件：地址为 = ${file.absolutePath}")
//            val readFileToString = FileUtils.readFileToString(context, fileName)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            val lastModified = file.lastModified()
            log("修改时间时间 = ${simpleDateFormat.format(lastModified)}")


            val readFileToString = com.yeqiu.awesomeandroid.utils.DownloadFolderFileUtil.getFileStr(context, fileName)

            log("文件内容为 = $readFileToString")

        } else {
            // 文件不存在，需要处理异常情况
            log("file == null")
        }


        val files = DownloadFolderFileUtil.getAllFiles(this)
        log("files = ${files.size}")

        for (file in files){

            log("name = ${ file.name}")
        }


    }


}