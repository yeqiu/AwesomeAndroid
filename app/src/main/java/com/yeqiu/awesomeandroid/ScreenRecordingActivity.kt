package com.yeqiu.awesomeandroid

import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.yeqiu.awesomeandroid.app.databinding.ActivityScreenRecordingBinding
import com.yeqiu.common.BaseBindActivity
import com.yeqiu.common.log
import com.yeqiu.common.toast
import com.yeqiu.screenrecorder.OnScreenRecordCallBack
import com.yeqiu.screenrecorder.ScreenRecorderHelper
import com.yeqiu.screenrecorder.utils.openVideoByIntent
import java.io.File

class ScreenRecordingActivity : BaseBindActivity<ActivityScreenRecordingBinding>(),
    View.OnClickListener {

    override fun init() {

        binding.btPrepare.setOnClickListener(this)
        binding.btStartRecording.setOnClickListener(this)
        binding.btStopRecording.setOnClickListener(this)
        binding.btScreenshot.setOnClickListener(this)
        binding.btStopServer.setOnClickListener(this)

        ScreenRecorderHelper.getInstance().init(this)



        ScreenRecorderHelper.getInstance()
            .setOnScreenRecordingCallBack(ScreenRecordCallBack())


    }


    inner class ScreenRecordCallBack : OnScreenRecordCallBack {
        override fun onRecordingStatusChange(status: ScreenRecorderHelper.ScreenRecordingStatus) {
            log("当前状态：${status.name}")
            when (status) {
                ScreenRecorderHelper.ScreenRecordingStatus.Idle -> {
                    binding.tvStatus.text =
                        "当前状态：空闲"
                    binding.btStartRecording.isClickable = false
                    binding.btStopRecording.isClickable = false
                    binding.btScreenshot.isClickable = false
                }

                ScreenRecorderHelper.ScreenRecordingStatus.Prepare -> {
                    binding.tvStatus.text =
                        "当前状态：准备就绪"
                    binding.btPrepare.text = "已准备"
                    binding.btStartRecording.text = "开始录屏"
                    binding.btStartRecording.isClickable = true
                    binding.btStopRecording.isClickable = true
                    binding.btScreenshot.isClickable = true
                }
                ScreenRecorderHelper.ScreenRecordingStatus.Recording -> {
                    binding.tvStatus.text =
                        "当前状态：录屏中"
                    binding.btStartRecording.text = "暂停"
                }
                ScreenRecorderHelper.ScreenRecordingStatus.Pause -> {
                    binding.tvStatus.text =
                        "当前状态：录屏暂停"
                    binding.btStartRecording.text = "继续"
                }
                ScreenRecorderHelper.ScreenRecordingStatus.Screenshotting -> {
                    binding.tvStatus.text =
                        "当前状态：截屏中"
                }
            }
        }

        override fun onError(actionType: Int, msg: String) {
            toast("${msg}")
            binding.tvInfo.text = msg
        }

        override fun onScreenshotResult(file: File) {
            binding.tvInfo.text = "截图成功，文件路径为：${file.absolutePath}"
        }

        override fun onRecordingResult(file: File) {
            binding.tvInfo.text = "录屏成功，文件路径为：${file.absolutePath}"
            openVideoByIntent(this@ScreenRecordingActivity, file)
        }

    }


    override fun onClick(view: View?) {
        view ?: return
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

    private fun prepare() {
        

        if (ScreenRecorderHelper.getInstance()
                .getStatus() == ScreenRecorderHelper.ScreenRecordingStatus.Idle
        ) {
            ScreenRecorderHelper.getInstance().prepare(true)
        }
    }

    private fun start() {




        if (ScreenRecorderHelper.getInstance()
                .getStatus() == ScreenRecorderHelper.ScreenRecordingStatus.Idle
        ) {

            toast("请先点击准备，初始化服务")

            binding.tvInfo.text = "请先点击准备，初始化服务"
        }


        when (ScreenRecorderHelper.getInstance().getStatus()) {

            ScreenRecorderHelper.ScreenRecordingStatus.Prepare -> {

                startCountDown(5)


            }
            ScreenRecorderHelper.ScreenRecordingStatus.Pause -> {
                //开始
                ScreenRecorderHelper.getInstance().startRecording()
            }
            ScreenRecorderHelper.ScreenRecordingStatus.Recording -> {
                //录屏中，暂停
                ScreenRecorderHelper.getInstance().pauseRecording()
            }
            else -> {}
        }
    }


    private fun stop() {

        ScreenRecorderHelper.getInstance().stopRecording()
    }

    private fun screenshot() {
        ScreenRecorderHelper.getInstance().screenshot()
    }

    private fun stopServer() {

        ScreenRecorderHelper.getInstance().release()
    }


    private fun startCountDown(time:Int) {

        var time = 5
        val animation = ScaleAnimation(
            1f,
            1.5f,
            1f,
            1.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )

        animation.duration = 1000
        animation.repeatCount = time - 1

        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                binding.tvCountdown.visibility = View.VISIBLE
                binding.tvCountdown.text = time.toString()
            }

            override fun onAnimationEnd(animation: Animation?) {

                binding.tvCountdown.visibility = View.GONE


                //开始
                ScreenRecorderHelper.getInstance().startRecording()

            }

            override fun onAnimationRepeat(animation: Animation?) {
                time -= 1
                binding.tvCountdown.text = time.toString()
            }

        })

        binding.tvCountdown.startAnimation(animation)

    }


}