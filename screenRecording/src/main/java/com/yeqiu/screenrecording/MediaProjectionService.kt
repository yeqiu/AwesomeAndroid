package com.yeqiu.screenrecording

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat
import com.yeqiu.screenrecording.utils.log
import java.io.File
import java.io.FileOutputStream


class MediaProjectionService : Service() {


    private val mediaProjectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private lateinit var displayMetrics: DisplayMetrics
    private lateinit var mediaProjection: MediaProjection
    private lateinit var imageReader: ImageReader
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var mediaFile: File


    companion object {

        fun bindService(
            context: Context,
            serviceConnection: ServiceConnection,
        ) {
            val intent = Intent(context, MediaProjectionService::class.java)
            context.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }

        fun unBindService(context: Context, serviceConnection: ServiceConnection) {
            context.unbindService(serviceConnection)
        }

    }


    inner class MediaProjectionBinder : Binder() {

        fun getService(): MediaProjectionService {
            return this@MediaProjectionService
        }
    }


    override fun onBind(intent: Intent?): IBinder {
        log("onBind")
        updateStatus(ScreenRecordingHelper.ScreenRecordingStatus.Idle)
        return MediaProjectionBinder()
    }

    /**
     * 准备资源
     * @param resultCode Int
     * @param data Intent
     * @param displayMetrics DisplayMetrics
     */

    fun prepare(resultCode: Int, data: Intent, displayMetrics: DisplayMetrics) {

        //设为前台
        showNotification()
        this@MediaProjectionService.displayMetrics = displayMetrics
        //准备录屏资源
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
        //更新状态
        updateStatus(ScreenRecordingHelper.ScreenRecordingStatus.Prepare)

        if (ScreenRecordingHelper.getInstance().getEnableScreenCapture()) {
            createImageReader()
        }

    }


    private fun showNotification(
        notification: Notification = NotificationHelper().create(
            ScreenRecordingHelper.getInstance().getContext()
        )
    ) {
        //设置为前台
        startForeground(100, notification)
    }

    fun startRecording(path: String) {


        if (ScreenRecordingHelper.getInstance()
                .getStatus() == ScreenRecordingHelper.ScreenRecordingStatus.Prepare
        ) {
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels
            val dpi = displayMetrics.densityDpi
            //录屏文件
            mediaFile = File(path)
            log("mediaFile = ${mediaFile.absolutePath}")
            //以下调用顺序不能乱
            mediaRecorder = MediaRecorder()
            val context = ScreenRecordingHelper.getInstance().getContext()
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            }
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setOutputFile(mediaFile.absolutePath)
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            }
            mediaRecorder.setVideoSize(width, height)
            mediaRecorder.setVideoFrameRate(30)
            mediaRecorder.setVideoEncodingBitRate(5 * width * height)
            mediaRecorder.setOnErrorListener { _, _, _ ->
                log("录屏服务发生错误")
                ScreenRecordingHelper.getInstance().getOnScreenRecordingCallBack()
                    .onError(OnScreenRecordingCallBack.actionVideo, "录屏服务发生错误")
            }
            mediaRecorder.prepare()

            if (::virtualDisplay.isInitialized) {
                virtualDisplay.surface = mediaRecorder.surface
            } else {
                virtualDisplay = mediaProjection.createVirtualDisplay(
                    "MediaRecorder",
                    width, height, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mediaRecorder.surface, null, null
                )
            }
            mediaRecorder.start()
            //更新状态
            updateStatus(ScreenRecordingHelper.ScreenRecordingStatus.Recording)
        } else if (ScreenRecordingHelper.getInstance()
                .getStatus() == ScreenRecordingHelper.ScreenRecordingStatus.Pause
        ) {
            mediaRecorder.resume()
        }
        updateStatus(ScreenRecordingHelper.ScreenRecordingStatus.Recording)
    }


    fun pauseRecording() {

        if (::mediaRecorder.isInitialized) {
            mediaRecorder.pause()
        }
        updateStatus(ScreenRecordingHelper.ScreenRecordingStatus.Pause)

    }


    @SuppressLint("WrongConstant")
    private fun createImageReader() {
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val dpi = displayMetrics.densityDpi
        if (!::imageReader.isInitialized) {
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                width,
                height,
                dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,
                null,
                null
            )
        }
    }


    @SuppressLint("WrongConstant")
    fun screenshot(path: String) {

        if (!::imageReader.isInitialized) {
            createImageReader()
        }

        updateStatus(ScreenRecordingHelper.ScreenRecordingStatus.Screenshotting)

        var image = imageReader.acquireLatestImage()
        if (image == null) {
            //500毫秒后尝试再次获取
            log("image为空，500毫秒后尝试再次获取")
            SystemClock.sleep(500)
            image = imageReader.acquireLatestImage()
        }

        if (image == null) {
            ScreenRecordingHelper.getInstance().getOnScreenRecordingCallBack()
                .onError(OnScreenRecordingCallBack.actionImage, "截屏失败")
            return
        }

        val imageWidth = image.width
        val imageHeight = image.height
        val plane = image.planes[0]
        val buffer = plane.buffer
        // 重新计算Bitmap宽度，防止Bitmap显示错位
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * imageWidth
        val bitmapWidth = imageWidth + rowPadding / pixelStride
        // 创建Bitmap
        val bitmap: Bitmap = Bitmap.createBitmap(bitmapWidth, imageHeight, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()
        // 裁剪Bitmap，因为重新计算宽度原因，会导致Bitmap宽度偏大
        val result = Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight)
        bitmap.recycle()

        val imgFile = File(path)
        log("imgFile = ${imgFile.absolutePath}")
        //保存bitmap
        saveBitmap(result, imgFile.absolutePath)
    }

    private fun saveBitmap(bitmap: Bitmap, path: String) {
        try {
            val file = File(path)
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fileOutputStream)
            fileOutputStream.flush()
            ScreenRecordingHelper.getInstance().getOnScreenRecordingCallBack()
                .onScreenshotResult(file)
            ScreenRecordingHelper.getInstance().getOnScreenRecordingCallBack()
                .onScreenshotResult(file)
        } catch (exception: Exception) {
            exception.printStackTrace()
            ScreenRecordingHelper.getInstance().getOnScreenRecordingCallBack()
                .onError(OnScreenRecordingCallBack.actionImage, "保存截图失败")
        } finally {
            //恢复状态
            updateStatus(ScreenRecordingHelper.ScreenRecordingStatus.Prepare)
        }


    }

    private fun updateStatus(status: ScreenRecordingHelper.ScreenRecordingStatus) {
        ScreenRecordingHelper.getInstance()
            .setStatus(status)
        //回调状态
        ScreenRecordingHelper.getInstance().getOnScreenRecordingCallBack()
            .onRecordingStatusChange(status)
    }


    fun stopRecording() {

        if (::mediaRecorder.isInitialized) {
            mediaRecorder.stop()
            mediaRecorder.reset()
            mediaRecorder.release()
        }


        updateStatus(ScreenRecordingHelper.ScreenRecordingStatus.Prepare)
        ScreenRecordingHelper.getInstance().getOnScreenRecordingCallBack()
            .onRecordingStatusResult(mediaFile)
    }

    fun release() {

        if (::mediaRecorder.isInitialized) {

            mediaRecorder.release()
        }

        if (::virtualDisplay.isInitialized) {
            virtualDisplay.release()
        }
        if (::mediaProjection.isInitialized) {
            mediaProjection.stop()
        }


        updateStatus(ScreenRecordingHelper.ScreenRecordingStatus.Idle)
    }


}