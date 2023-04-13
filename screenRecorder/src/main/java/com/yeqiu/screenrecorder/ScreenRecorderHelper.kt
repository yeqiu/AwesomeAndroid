package com.yeqiu.screenrecorder

import android.app.Activity
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.yeqiu.screenrecorder.utils.getScreenRecordingFilePath
import com.yeqiu.screenrecorder.utils.getScreenshotFilePath
import com.yeqiu.screenrecorder.utils.log

class ScreenRecorderHelper {

    enum class ScreenRecordingStatus {
        Idle, Prepare, Recording, Pause, Screenshotting;
    }

    private var status = ScreenRecordingStatus.Idle
    private lateinit var activity: ComponentActivity
    private lateinit var recordingPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var mediaProjectionService: MediaProjectionService
    private lateinit var displayMetrics: DisplayMetrics
    private lateinit var onScreenRecordCallBack: OnScreenRecordCallBack
    private var enableScreenCapture: Boolean = false


    private val serviceConnection by lazy {
        object : ServiceConnection {

            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                //绑定成功，设置通知
                log("服务绑定成功")
                binder?.let {
                    if (binder is MediaProjectionService.MediaProjectionBinder) {
                        mediaProjectionService = binder.getService()
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                log("服务解绑")

            }
        }
    }


    companion object {
        private val screenRecorderHelper = ScreenRecorderHelper()

        @JvmStatic
        fun getInstance(): ScreenRecorderHelper {
            return screenRecorderHelper
        }
    }


    internal fun getContext(): ComponentActivity {
        return activity
    }

    internal fun setStatus(status: ScreenRecordingStatus) {
        this@ScreenRecorderHelper.status = status
    }

    internal fun getOnScreenRecordingCallBack(): OnScreenRecordCallBack {

        return if (::onScreenRecordCallBack.isInitialized) {
            onScreenRecordCallBack
        } else {
            object : OnScreenRecordCallBack() {
            }
        }
    }

    internal fun getEnableScreenCapture() = enableScreenCapture


    /**
     * 准备录屏launcher,绑定服务
     * @param activity ComponentActivity
     */
    fun init(activity: ComponentActivity) {

        this@ScreenRecorderHelper.activity = activity
        //创建录屏请求launch
        recordingPermissionLauncher = createRecordingPermissionResult(activity)
        //绑定服务
        MediaProjectionService.bindService(activity, serviceConnection)
        displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
    }

    fun setNotification(notification: Notification){

        NotificationHelper.setNotification(notification)

    }


    fun setOnScreenRecordingCallBack(onScreenRecordCallBack: OnScreenRecordCallBack) {
        this@ScreenRecorderHelper.onScreenRecordCallBack = onScreenRecordCallBack
    }

    fun getStatus(): ScreenRecordingStatus {
        return status
    }


    private fun createRecordingPermissionResult(activity: ComponentActivity) =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                log("获取到录屏权限")
                //发送到服务中，准备资源
                result.data?.let {
                    if (::mediaProjectionService.isInitialized) {
                        mediaProjectionService.prepare(
                            result.resultCode,
                            result.data!!, displayMetrics
                        )
                    }
                }
            }
        }


    /**
     * 准备资源,是否提前准备截图资源
     * @param enableScreenCapture Boolean
     */
    fun prepare(enableScreenCapture: Boolean) {
        if (::recordingPermissionLauncher.isInitialized) {
            val mediaProjectionManager =
                activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            recordingPermissionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
            this@ScreenRecorderHelper.enableScreenCapture = enableScreenCapture
        } else {
            log("未初始化")
        }
    }

    /**
     * 开始录屏
     * 自动判断是否是继续
     */
    @JvmOverloads
    fun startRecording(path: String = getScreenRecordingFilePath(activity)) {

        //根据状态判断是否是继续
        when (status) {
            ScreenRecordingStatus.Idle -> {
                log("未初始化")
            }
            ScreenRecordingStatus.Prepare -> {
                mediaProjectionService.startRecording(path)
            }
            ScreenRecordingStatus.Pause -> {
                mediaProjectionService.startRecording(path)
            }
            ScreenRecordingStatus.Recording -> {
                log("正在录屏中，无法再次开启")
            }
            ScreenRecordingStatus.Screenshotting -> {
                log("正在截屏，请稍后开始录屏")
            }
        }
    }

    /**
     * 暂停录屏
     */
    fun pauseRecording() {

        if (status == ScreenRecordingStatus.Recording) {
            mediaProjectionService.pauseRecording()
        } else {
            log("当前状态不可暂停")
        }
    }

    /**
     * 结束录屏
     */
    fun stopRecording() {

        if (status == ScreenRecordingStatus.Recording || status == ScreenRecordingStatus.Pause) {
            mediaProjectionService.stopRecording()
        } else {
            log("当前状态不可暂停")
        }
    }

    /**
     * 截图
     */
    @JvmOverloads
    fun screenshot(path: String = getScreenshotFilePath(activity)) {
        if (status == ScreenRecordingStatus.Prepare) {
            mediaProjectionService.screenshot(path)
        } else {
            log("当前状态不可暂停")
        }

    }

    fun release() {

        if (status != ScreenRecordingStatus.Idle) {
            mediaProjectionService.release()
            MediaProjectionService.unBindService(activity, serviceConnection)
        }

    }

}