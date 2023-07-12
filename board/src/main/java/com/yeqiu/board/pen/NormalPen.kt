package com.yeqiu.board.pen

import android.graphics.Path
import android.view.MotionEvent
import com.yeqiu.board.log

/**
 * @project：AwesomeAndroid
 * @author：小卷子
 * @date 2023/5/11
 * @describe：普通笔
 * @fix：
 */
open class NormalPen : Pen() {


    override fun handleTouchEvent(motionEvent: MotionEvent): Path? {

        val x = motionEvent.x
        val y = motionEvent.y

        var path: Path? = null
        path = defHandleTouchEvent(motionEvent)

        canvas.drawPath(this.path, paint)
        return path
    }

    private fun bezierHandleTouchEvent(motionEvent: MotionEvent): Path? {

        log("贝塞尔")

        val x = motionEvent.x
        val y = motionEvent.y

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                path = Path()
                path.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {



            }

            MotionEvent.ACTION_UP -> {
                return path
            }

        }


        return null
    }


    private fun defHandleTouchEvent(motionEvent: MotionEvent): Path? {


        log("普通")

        val x = motionEvent.x
        val y = motionEvent.y

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {

                path = Path()
                path.moveTo(x, y)

            }

            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)

            }

            MotionEvent.ACTION_UP -> {
                path.lineTo(x, y)
                return path
            }
        }

        return null
    }


}