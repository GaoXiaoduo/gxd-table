package com.bin.david.form.data.format.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.format.bg.ICellBackgroundFormat;
import com.bin.david.form.utils.DrawUtils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 已选中的房价金额/库存格式
 *
 * @author gaoxiaoduo
 * @version 1.0
 * @date 2021/9/15 11:17
 */
public class SelectedTextDrawFormat<T> implements IDrawFormat<T>
{


    private Map<String, SoftReference<String[]>> valueMap; //避免产生大量对象

    public SelectedTextDrawFormat ()
    {

        valueMap = new HashMap<>();
    }

    @Override
    public int measureWidth (Column<T> column, int position, TableConfig config)
    {

        Paint paint = config.getPaint();
        config.getContentStyle().fillPaint(paint);
        return DrawUtils.getMultiTextWidth(paint, getSplitString(column.format(position)));
    }


    @Override
    public int measureHeight (Column<T> column, int position, TableConfig config)
    {

        Paint paint = config.getPaint();
        config.getContentStyle().fillPaint(paint);
        return DrawUtils.getMultiTextHeight(paint, getSplitString(column.format(position)));
    }

    @Override
    public void draw (Canvas c, Rect rect, CellInfo<T> cellInfo, TableConfig config)
    {

        Paint paint = config.getPaint();
        setTextPaint(config, cellInfo, paint);
        if (cellInfo.column.getTextAlign() != null)
        {
            paint.setTextAlign(cellInfo.column.getTextAlign());
        }
        drawText(c, cellInfo.value, rect, paint);
    }

    protected void drawText (Canvas c, String value, Rect rect, Paint paint)
    {

        DrawUtils.drawMultiText(c, paint, rect, getSplitString(value));
    }


    public void setTextPaint (TableConfig config, CellInfo<T> cellInfo, Paint paint)
    {

        config.getContentStyle().fillPaint(paint);
        ICellBackgroundFormat<CellInfo> backgroundFormat = config.getSelectedCellBackgroundFormat();
        if (backgroundFormat != null && backgroundFormat.getTextColor(cellInfo) != TableConfig.INVALID_COLOR)
        {
            paint.setColor(backgroundFormat.getTextColor(cellInfo));
        }
        paint.setTextSize(paint.getTextSize() * config.getZoom());

    }

    protected String[] getSplitString (String val)
    {

        String[] values = null;
        if (valueMap.get(val) != null)
        {
            values = valueMap.get(val).get();
        }
        if (values == null)
        {
            values = val.split("\n");

            valueMap.put(val, new SoftReference<>(values));
        }
        return values;
    }
}
