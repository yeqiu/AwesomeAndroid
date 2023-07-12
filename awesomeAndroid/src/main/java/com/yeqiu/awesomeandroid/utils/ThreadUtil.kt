package com.yeqiu.awesomeandroid.utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

/**
 * @project：AwesomeAndroid
 * @author：小卷子
 * @date 2023/5/25
 * @describe：
 * @fix：
 */
object ThreadUtil {


    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val childHandler by lazy {
        val childThread = HandlerThread("childThread")
        childThread.start()
        Handler(childThread.looper)
    }


    fun runOnMainThread(delayMillis:Long = 0L,action:()->Unit){

        if (delayMillis <= 0L){
            mainHandler.post(action)
        }else {
            mainHandler.postDelayed(action,delayMillis)
        }

    }

    fun runOnChildThread(delayMillis:Long = 0L,action:()->Unit){
        if (delayMillis <= 0L){
            childHandler.post(action)
        }else {
            childHandler.postDelayed(action,delayMillis)
        }
    }


}