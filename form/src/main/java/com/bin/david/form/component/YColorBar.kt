package com.bin.david.form.component

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.bin.david.form.core.TableConfig
import com.bin.david.form.data.TableInfo
import com.bin.david.form.data.format.bg.BaseBackgroundFormat
import com.bin.david.form.data.format.bg.IBackgroundFormat
import com.bin.david.form.data.format.sequence.ISequenceFormat
import com.bin.david.form.data.table.TableData

/**
 * 左边竖向排版颜色条
 *
 * @author gaoxiaoduo
 * @date 2021/9/3 14:11
 * @version 1.0
 */
public class YColorBar<T> : IComponent<TableData<T>>
{
    private var rect: Rect? = null
    private var width: Int = 0
    private var clipWidth = 0
    private var scaleRect: Rect? = null
    private var format: ISequenceFormat? = null

    /** 临时使用 */
    private var tempRect: Rect? = null

    init
    {
        rect = Rect()
        tempRect = Rect()
    }

    override fun onMeasure(scaleRect: Rect?, showRect: Rect?, config: TableConfig?)
    {
        if (scaleRect == null || showRect == null || config == null)
        {
            return
        }
        this.scaleRect = scaleRect
        val scaleWidth = width.times(if (config.zoom > 1) 1f else config.zoom)
                .toInt()
        val fixed = config.isFixedYColorBar
        rect!!.top = scaleRect.top
        rect!!.bottom = scaleRect.bottom
        rect!!.left = if (fixed) showRect.left else scaleRect.left
        rect!!.right = rect!!.left + scaleWidth
        if (fixed)
        {
            scaleRect.left += scaleWidth
            showRect.left += scaleWidth
            clipWidth = scaleWidth
        } else
        {
            val disX = showRect.left - scaleRect.left
            clipWidth = Math.max(0, scaleWidth - disX)
            showRect.left += clipWidth
            scaleRect.left += scaleWidth
        }
    }

    override fun onDraw(canvas: Canvas, showRect: Rect, tableData: TableData<T>, config: TableConfig)
    {
        //format = tableData.ySequenceFormat
        val hZoom: Float = if (config.zoom > 1) 1f else config.zoom

        val totalSize: Int = tableData.lineSize
        val info: TableInfo = tableData.tableInfo
        val topHeight = info.getTopHeight(hZoom)
        var top = (rect!!.top + topHeight).toFloat()
        val showLeft = showRect.left - clipWidth
        val isFixTop = config.isFixedXSequence
        val showTop = if (isFixTop) showRect.top + topHeight else showRect.top
        var num = 0
        var tempTop = top

        tempRect!![showLeft, tempTop.toInt() - topHeight, showRect.left] = tempTop.toInt()
        //drawLeftAndTop(canvas, showRect, tempRect, config)
        canvas.save()
        // canvas.clipRect(showLeft, showTop, showRect.left, showRect.bottom)
        //drawBackground(canvas, showRect, config, showLeft, showTop)
        // 顶部
        val columnInfoList = tableData.columnInfos
        val barLeft = showRect.left
        val barTop = columnInfoList[4].top + columnInfoList[4].height
        val barRight = barLeft + config.yColorBarWidth
        var barBottom = 300
        if (config.yColorBarArray == null)
        {
            return
        }
        for (i in 1..4)
        {
            val top = barTop + barBottom * (i - 1)
            val bottom = top + barBottom
            tempRect!!.set(barLeft, top, barRight, bottom)
            val color = config.yColorBarArray[i - 1]
            val paint = Paint()
            paint.color = color
            drawBackground(canvas, tempRect!!, BaseBackgroundFormat(color), paint)
        }
    }

    /**
     * 绘制背景
     * @param canvas
     * @param showRect
     * @param config
     * @param showLeft
     * @param showTop
     */
    protected fun drawBackground(canvas: Canvas?, colorBarRect: Rect, format: IBackgroundFormat, paint: Paint)
    {
        format.drawBackground(canvas, colorBarRect, paint)
    }
}