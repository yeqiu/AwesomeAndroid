package com.yeqiu.screenrecorder

import java.io.File

interface OnScreenRecordCallBack {

    companion object {
        const val actionVideo: Int = 1
        const val actionImage: Int = 2
    }


    /**
     * 录屏服务状态回调
     * @param status ScreenRecordingStatus
     */
    fun onRecordingStatusChange(status: ScreenRecorderHelper.ScreenRecordingStatus)

    /**
     * 错误回调
     * @param actionType Int
     * @param msg String
     */
    fun onError(actionType: Int, msg: String)

    /**
     * 截屏回调
     * @param file File
     */
    fun onScreenshotResult(file: File)

    /**
     * 录屏文件回调
     * @param file File
     */
    fun onRecordingResult(file: File)


}


