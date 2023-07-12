package com.yeqiu.board.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.yeqiu.board.log
import com.yeqiu.board.model.PathData
import com.yeqiu.board.pen.EraserPen
import com.yeqiu.board.pen.NormalPen
import com.yeqiu.board.pen.Pen


/**
 * @project：AwesomeAndroid
 * @author：小卷子
 * @date 2023/5/11
 * @describe：画板
 * @fix：
 */
class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint by lazy { Paint() }
    var pen: Pen = NormalPen()
    private lateinit var bitmap: Bitmap
    private val canvas by lazy { Canvas() }
    private var openEraser = false
    var lastPen: Pen = pen
    private val pathList = mutableListOf<PathData>()


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (!::bitmap.isInitialized) {
            bitmap = createBitmap(measuredWidth, measuredHeight)
            canvas.setBitmap(bitmap)
            pen.setCanvas(canvas)
        }
    }

    private fun createBitmap(width: Int, height: Int): Bitmap {

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        return bitmap
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(bitmap, 0f, 0f, paint)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {


        event?.let {
            val path = pen.handleTouchEvent(event)
            path?.let {
                pathList.add(PathData(it, pen.paint))
            }
            invalidate()
            return true
        } ?: return super.onTouchEvent(event)

    }


    fun setAlpha(percentage: Int) {
        val alpha = calculateAlpha(percentage)
        val color = Color.argb(alpha, 0, 0, 0)
        setBackgroundColor(color)
    }

    private fun calculateAlpha(percentage: Int): Int {

        var newPercentage = percentage.toFloat()
        if (percentage < 0) {
            newPercentage = 10F
        } else if (percentage > 100) {
            newPercentage = 90F
        }

        return (newPercentage / 100f * 255f).toInt()
    }


    fun setPenStyle(color: Int, width: Int) {

        pen.penWidth = width
        pen.penColor = color

    }

    fun setNewPen(pen: Pen) {

        openEraser = pen is EraserPen

        if (!openEraser) {
            //非橡皮保存上次笔信息
            this.lastPen = pen
        }
        this.pen = pen
        this.pen.setCanvas(canvas)
    }


    /**
     * 使用橡皮
     */
    fun openEraser() {

        setNewPen(EraserPen())
    }

    fun currentIsEraser() = openEraser


    fun revocation() {
        log("path = ${pathList.size}")

        if (pathList.isEmpty()) {
            log("没有笔迹，无法撤销")
            return
        }

        pathList.removeLast()
        bitmap.eraseColor(Color.TRANSPARENT)
        pathList.forEach {
            pen.drawPath(it.path,it.paint)
        }

        invalidate()
    }


    /**
     * 清屏
     */
    fun clear() {

        bitmap = createBitmap(measuredWidth, measuredHeight)
        canvas.setBitmap(bitmap)
        //清除笔迹信息
        pathList.clear()
        invalidate()
        //恢复之前的笔
        setNewPen(this.lastPen)
    }

    fun getBitmap(): Bitmap {

        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        newBitmap.eraseColor(Color.TRANSPARENT)
        val newCanvas = Canvas(newBitmap)
        newCanvas.drawBitmap(bitmap,0F,0F, Paint())
        return newBitmap
    }


}