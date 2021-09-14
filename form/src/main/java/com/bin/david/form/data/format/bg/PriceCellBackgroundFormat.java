package com.bin.david.form.data.format.bg;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.bin.david.form.core.TableConfig;

/**
 * 通用绘制价格Rect格子背景绘制
 *
 * @author gaoxiaoduo
 * @version 1.0
 * @date 2021/9/13 11:17
 */
public abstract class PriceCellBackgroundFormat<T> implements ICellBackgroundFormat<T>
{

    @Override
    public void drawBackground (Canvas canvas, Rect rect, T t, Paint paint)
    {

        int color = getBackGroundColor(t);
        if (color != TableConfig.INVALID_COLOR)
        {
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rect, paint);
        }
    }

    /**
     * 获取背景颜色
     */
    public abstract int getBackGroundColor (T t);

    /**
     * 默认字体颜色不跟随背景变化，
     * 当有需要多种字体颜色，请重写该方法
     *
     * @param t
     *
     * @return
     */
    @Override
    public int getTextColor (T t)
    {

        return TableConfig.INVALID_COLOR;
    }

    /**
     * 是否为有效价格
     *
     * @return
     */
    public boolean isEffectivePrice (T cellInfo, int col, int row)
    {

        return false;
    }
}
