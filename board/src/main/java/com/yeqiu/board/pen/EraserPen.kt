package com.yeqiu.board.pen

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.yeqiu.board.log

/**
 * @project：AwesomeAndroid
 * @author：小卷子
 * @date 2023/5/11
 * @describe：橡皮擦
 * @fix：
 */
internal class EraserPen :NormalPen(){

    override fun initPaint() {
//        super.initPaint()
        paint.color = Color.TRANSPARENT
        paint.strokeWidth = 20f

        paint.xfermode= PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }


    override fun onPenColorChange(color: Int) {
//        super.onPenColorChange(color)
        log("橡皮擦不能修改颜色")
    }


}