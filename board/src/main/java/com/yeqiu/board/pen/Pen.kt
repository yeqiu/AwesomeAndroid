package com.yeqiu.board.pen

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.MotionEvent

/**
 * @project：AwesomeAndroid
 * @author：小卷子
 * @date 2023/5/11
 * @describe：笔
 * @fix：
 */
abstract class Pen {


    var penWidth = 6
        set(value) {
            field = value
            onPenWidthChange(value)
        }
    var penColor = Color.BLACK
        set(value) {
            field = value
            onPenColorChange(value)
        }
     val paint = Paint()
    protected lateinit var canvas: Canvas

    protected var path = Path()


    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND

        initPaint()
    }

    protected open fun initPaint() {
        paint.color = penColor
        paint.strokeWidth = penWidth.toFloat()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    }


    abstract fun handleTouchEvent(motionEvent: MotionEvent):Path?


    fun drawPath(path: Path,paint:Paint){

        canvas.drawPath(path, paint)
    }


    @JvmName("setCanvasFroJava")
    fun setCanvas(canvas: Canvas) {
        this.canvas = canvas
    }


    open fun onPenWidthChange(size: Int) {
        paint.strokeWidth = penWidth.toFloat()
    }


    open fun onPenColorChange(color: Int) {
        paint.color = penColor
    }



}