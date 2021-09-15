package com.bin.david.form.data.format.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.utils.DrawUtils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 房价金额/库存格式
 *
 * @author gaoxiaoduo
 * @version 1.0
 * @date 2021/9/15 11:17
 */
public class PriceTextDrawFormat<T> implements IDrawFormat<T>
{


    private Map<String, SoftReference<String[]>> valueMap; //避免产生大量对象

    /**
     * 价格字体
     */
    private FontStyle mPriceFontStyle;

    /**
     * 库存字体
     */
    private FontStyle mStockFontStyle;

    private final Rect mTempRect;

    public PriceTextDrawFormat ()
    {

        valueMap = new HashMap<>();
        mTempRect = new Rect();
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

        setTextPaint(config);
        if (cellInfo.column.getTextAlign() != null)
        {
            config.getPaint().setTextAlign(cellInfo.column.getTextAlign());
        }
        drawText(c, cellInfo.value, rect, config);
    }

    protected void drawText (Canvas c, String value, Rect rect, TableConfig config)
    {

        String[] textArray = getSplitString(value);
        drawPriceText(c, textArray[0], rect, config);
        drawStockText(c, textArray[1], rect, config);
    }

    public void drawPriceText (Canvas c, String value, Rect rect, TableConfig config)
    {

        mTempRect.set(rect.left, rect.top + config.getVerticalPadding(), rect.right,
                rect.top + rect.height() / 2);
        mPriceFontStyle.fillPaint(config.getPaint());
        config.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
        config.getPaint().setStrokeWidth(1);
        DrawUtils.drawSingleText(c, config.getPaint(), mTempRect, value);
    }

    public void drawStockText (Canvas c, String value, Rect rect, TableConfig config)
    {

        mTempRect.set(rect.left, rect.top + (rect.height() / 2), rect.right,
                rect.bottom - config.getVerticalPadding());
        mStockFontStyle.fillPaint(config.getPaint());
        DrawUtils.drawSingleText(c, config.getPaint(), mTempRect, value);
    }

    public void setTextPaint (TableConfig config)
    {

        mPriceFontStyle = config.getPriceTitleStyle();
        mStockFontStyle = config.getStockTitleStyle();
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
