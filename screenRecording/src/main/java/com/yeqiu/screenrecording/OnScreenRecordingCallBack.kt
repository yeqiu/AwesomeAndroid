package com.yeqiu.screenrecording

import java.io.File

open class OnScreenRecordingCallBack {

    companion object {
        const val actionVideo: Int = 1
        const val actionImage: Int = 2
    }


    open fun onRecordingStatusChange(status: ScreenRecordingHelper.ScreenRecordingStatus) {


    }


    open fun onError(actionType: Int, msg: String) {

    }


    open fun onScreenshotResult(file: File) {

    }


    open fun onRecordingStatusResult(file: File) {

    }


}


