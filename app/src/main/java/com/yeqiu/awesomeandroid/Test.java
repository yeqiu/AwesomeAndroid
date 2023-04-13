package com.yeqiu.awesomeandroid;

import androidx.activity.ComponentActivity;

import com.yeqiu.screenrecorder.ScreenRecorderHelper;

public class Test {


    public void init(ComponentActivity activity){
        ScreenRecorderHelper.getInstance().init(activity);
    }

    public void prepare(){
        ScreenRecorderHelper.getInstance().prepare(true);
    }

    public void start(){
        ScreenRecorderHelper.getInstance().startRecording();
        ScreenRecorderHelper.getInstance().pauseRecording();
        ScreenRecorderHelper.getInstance().stopRecording();
        ScreenRecorderHelper.getInstance().screenshot();
    }
}
