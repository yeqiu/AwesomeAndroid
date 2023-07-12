package com.yeqiu.board.pen

import android.graphics.Path
import android.view.MotionEvent
import com.yeqiu.board.log

/**
 * @project：AwesomeAndroid
 * @author：小卷子
 * @date 2023/5/15
 * @describe：
 * @fix：
 */
class BezierCurvePen : Pen() {


    var mCurrentX = 0f
    var mCurrentY = 0f


    override fun handleTouchEvent(motionEvent: MotionEvent): Path? {

        log("BezierCurvePen")

        val x = motionEvent.x
        val y = motionEvent.y
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
//                path.reset()
                path.moveTo(x, y)
                mCurrentX = x
                mCurrentY = y
            }

            MotionEvent.ACTION_MOVE -> {

                drawBeziercurve(x, y)
                mCurrentX = x
                mCurrentY = y
            }

            MotionEvent.ACTION_UP -> {
                path.lineTo(x, y)
                return path
            }
        }

        canvas.drawPath(path, paint)

        return null
    }


    private fun drawBeziercurve(x: Float, y: Float) {
        path.quadTo(
            mCurrentX, mCurrentY, (x + mCurrentX) / 2,
            (y + mCurrentY) / 2
        )
    }

}