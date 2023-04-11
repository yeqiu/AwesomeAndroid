package com.yeqiu.awesomeandroid

import android.graphics.Color
import android.view.View
import com.yeqiu.awesomeandroid.databinding.ActivityMainBinding
import com.yeqiu.common.BaseBindActivity
import com.yeqiu.common.log
import com.yeqiu.common.toast
import com.yeqiu.screenrecording.OnScreenRecordingCallBack
import com.yeqiu.screenrecording.ScreenRecordingHelper
import java.io.File

class MainActivity : BaseBindActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun init() {


        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.statusBarColor = Color.TRANSPARENT


        binding.btPrepare.setOnClickListener(this)
        binding.btStartRecording.setOnClickListener(this)
        binding.btStopRecording.setOnClickListener(this)
        binding.btScreenshot.setOnClickListener(this)
        binding.btStopServer.setOnClickListener(this)


        ScreenRecordingHelper.getInstance().init(this)


        ScreenRecordingHelper.getInstance()
            .setOnScreenRecordingCallBack(object : OnScreenRecordingCallBack() {

                override fun onRecordingStatusChange(status: ScreenRecordingHelper.ScreenRecordingStatus) {
                    super.onRecordingStatusChange(status)

                    log("当前状态：${status.name}")
                    when (status) {
                        ScreenRecordingHelper.ScreenRecordingStatus.Idle -> {
                            binding.tvStatus.text =
                                "当前状态：空闲"
                            binding.btStartRecording.isClickable = false
                            binding.btStopRecording.isClickable = false
                            binding.btScreenshot.isClickable = false
                        }

                        ScreenRecordingHelper.ScreenRecordingStatus.Prepare -> {
                            binding.tvStatus.text =
                                "当前状态：准备就绪"
                            binding.btPrepare.text = "已准备"
                            binding.btStartRecording.text = "开始录屏"
                            binding.btStartRecording.isClickable = true
                            binding.btStopRecording.isClickable = true
                            binding.btScreenshot.isClickable = true
                        }
                        ScreenRecordingHelper.ScreenRecordingStatus.Recording -> {
                            binding.tvStatus.text =
                                "当前状态：录屏中"
                            binding.btStartRecording.text = "暂停"
                        }
                        ScreenRecordingHelper.ScreenRecordingStatus.Pause -> {
                            binding.tvStatus.text =
                                "当前状态：录屏暂停"
                            binding.btStartRecording.text = "继续"
                        }
                        ScreenRecordingHelper.ScreenRecordingStatus.Screenshotting -> {
                            binding.tvStatus.text =
                                "当前状态：截屏中"
                        }
                    }
                }

                override fun onError(actionType: Int, msg: String) {
                    super.onError(actionType, msg)
                    toast("${msg}")
                    binding.tvInfo.text = msg
                }

                override fun onRecordingStatusResult(file: File) {
                    super.onRecordingStatusResult(file)

                    binding.tvInfo.text = "录屏成功，文件路径为：${file.absolutePath}"

                }

                override fun onScreenshotResult(file: File) {
                    super.onScreenshotResult(file)
                    binding.tvInfo.text = "截图成功，文件路径为：${file.absolutePath}"
                }

            })


    }

    override fun onClick(view: View?) {
        view?.let {
            if (view == binding.btPrepare) {
                prepare()
            } else if (view === binding.btStartRecording) {
                start()
            } else if (view === binding.btStopRecording) {
                stop()
            } else if (view === binding.btScreenshot) {
                screenshot()
            } else if (view === binding.btStopServer) {
                stopServer()
            }
        }
    }


    private fun prepare() {

        if (ScreenRecordingHelper.getInstance()
                .getStatus() == ScreenRecordingHelper.ScreenRecordingStatus.Idle
        ) {
            ScreenRecordingHelper.getInstance().prepare(true)
        }
    }

    private fun start() {


        if (ScreenRecordingHelper.getInstance()
                .getStatus() == ScreenRecordingHelper.ScreenRecordingStatus.Idle
        ) {

            toast("请先点击准备，初始化服务")

            binding.tvInfo.text = "请先点击准备，初始化服务"
        }


        when (ScreenRecordingHelper.getInstance().getStatus()) {

            ScreenRecordingHelper.ScreenRecordingStatus.Prepare -> {
                //开始
                ScreenRecordingHelper.getInstance().startRecording()
            }
            ScreenRecordingHelper.ScreenRecordingStatus.Pause -> {
                //开始
                ScreenRecordingHelper.getInstance().startRecording()
            }
            ScreenRecordingHelper.ScreenRecordingStatus.Recording -> {
                //录屏中，暂停
                ScreenRecordingHelper.getInstance().pauseRecording()
            }
            else -> {}
        }
    }


    private fun stop() {

        ScreenRecordingHelper.getInstance().stopRecording()
    }

    private fun screenshot() {
        ScreenRecordingHelper.getInstance().screenshot()
    }

    private fun stopServer() {
        ScreenRecordingHelper.getInstance().release()
    }

}


