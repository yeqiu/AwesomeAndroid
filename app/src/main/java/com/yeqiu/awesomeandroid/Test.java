package com.yeqiu.awesomeandroid;

import androidx.activity.ComponentActivity;

import com.yeqiu.screenrecording.ScreenRecordingHelper;

public class Test {


    public void init(ComponentActivity activity){
        ScreenRecordingHelper.getInstance().init(activity);
    }

    public void prepare(){
        ScreenRecordingHelper.getInstance().prepare(true);
    }

    public void start(){
        ScreenRecordingHelper.getInstance().startRecording();
        ScreenRecordingHelper.getInstance().pauseRecording();
        ScreenRecordingHelper.getInstance().stopRecording();
        ScreenRecordingHelper.getInstance().screenshot();
    }
}
