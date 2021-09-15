package com.bin.david.form.data.format.title;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.LruCache;

import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.format.bg.ICellBackgroundFormat;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.utils.DrawUtils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期列标题格式化
 *
 * @author gaoxiaoduo
 * @version 1.0
 * @date 2021/9/7 17:29
 */
public class DateTitleDrawFormat implements ITitleDrawFormat
{
    private Map<String, SoftReference<String[]>> valueMap; //避免产生大量对象

    private Context mContext;

    //使用缓存
    private LruCache<Integer, Bitmap> cache;

    /**
     * 临时保存
     */
    private Rect mTempRect;

    /**
     * 图片范围
     */
    private Rect mImgRect;

    private int mTodayResourceID;

    /**
     * 周、假字体
     */
    private FontStyle mWeekFontStyle;

    /**
     * 日期字体
     */
    private FontStyle mDateFontStyle;

    // 日历总高度92*2
    private int weekRectHeight = 80;

    private int dateRectHeight = 104;

    private static int defaultFontColor = Color.parseColor("#636363");

    /**
     * 今日默认字体颜色
     */
    private int mTodayTextColor;

    /**
     * 周默认字体颜色
     */
    private int mWeekTextNormalColor;

    /**
     * 周五、六、假日字体颜色
     */
    private int mWeekTextHolidayColor;

    /**
     * 日期字体颜色
     */
    private int mDateTextColor;

    /**
     * 日期列标题
     *
     * @param context              上下文
     * @param todayBgResourceID    今日背景图片资源ID
     * @param todayTextColor       今日字体颜色
     * @param weekTextNormalColor  周默认颜色
     * @param weekTextHolidayColor 周五、周六、假日 颜色
     * @param dateTextColor        日期默认颜色
     */
    public DateTitleDrawFormat (
            Context context, int todayBgResourceID, int todayTextColor, int weekTextNormalColor,
            int weekTextHolidayColor, int dateTextColor)
    {

        mContext = context;
        mTodayResourceID = todayBgResourceID;
        valueMap = new HashMap<>();
        mTempRect = new Rect();
        mImgRect = new Rect();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);// kB
        int cacheSize = maxMemory / 16;
        cache = new LruCache<Integer, Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf (Integer key, Bitmap bitmap)
            {

                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;// KB
            }
        };
        mTodayTextColor = mContext.getResources().getColor(todayTextColor);
        mWeekTextNormalColor = mContext.getResources().getColor(weekTextNormalColor);
        mWeekTextHolidayColor = mContext.getResources().getColor(weekTextHolidayColor);
        mDateTextColor = mContext.getResources().getColor(dateTextColor);

        mWeekFontStyle =
                new FontStyle(mContext, 12, mWeekTextNormalColor).setAlign(Paint.Align.CENTER);
        mDateFontStyle = new FontStyle(mContext, 16, mDateTextColor).setAlign(Paint.Align.CENTER);
    }

    private boolean isDrawBg;

    @Override
    public int measureWidth (Column column, TableConfig config)
    {

        Paint paint = config.getPaint();
        config.getColumnTitleStyle().fillPaint(paint);
        return (int) (paint.measureText(column.getColumnName()));
    }


    @Override
    public int measureHeight (TableConfig config)
    {

        Paint paint = config.getPaint();
        config.getColumnTitleStyle().fillPaint(paint);
        return config.getColumnDateTitleHeight();
        //DrawUtils.getTextHeight(config.getColumnTitleStyle(), config.getPaint());
    }

    @Override
    public void draw (Canvas c, Column column, Rect rect, TableConfig config)
    {

        Paint paint = config.getPaint();
        boolean isDrawBg = drawBackground(c, column, rect, config);
        config.getColumnTitleStyle().fillPaint(paint);
        ICellBackgroundFormat<Column> backgroundFormat = config.getColumnCellBackgroundFormat();

        paint.setTextSize(paint.getTextSize() * config.getZoom());
        if (isDrawBg && backgroundFormat.getTextColor(column) != TableConfig.INVALID_COLOR)
        {
            paint.setColor(backgroundFormat.getTextColor(column));
        }
        drawText(c, column, rect, paint, config);
    }

    private void drawText (Canvas c, Column column, Rect rect, Paint paint, TableConfig config)
    {

        if (column.getTitleAlign() != null)
        { //如果列设置Align ，则使用列的Align
            paint.setTextAlign(column.getTitleAlign());
        }
        String[] values = getSplitString(column.getColumnName());
        if (values != null && values.length >= 2)
        {
            // 周数据
            String week = values[0];
            // 日期数据
            String date = values[1];
            // drawGridBackground(c, rect, config);
            drawWeek(c, column, rect, paint, config, week);
            drawTodayBitmap(c, column, rect, paint, config, column.isToday());
            drawDate(c, column, rect, paint, config, date);
        }
    }

    private void drawWeek (
            Canvas c, Column column, Rect rect, Paint paint, TableConfig config,
            String week)
    {

        mTempRect.set(rect.left, rect.top, rect.right, weekRectHeight);
        if (column.isHoliday())
        {
            mWeekFontStyle.setTextColor(mWeekTextHolidayColor);
        }
        else
        {
            mWeekFontStyle.setTextColor(mWeekTextNormalColor);
        }
        mWeekFontStyle.fillPaint(paint);
        c.drawText(week, DrawUtils.getTextCenterX(mTempRect.left, mTempRect.right, paint)
                , DrawUtils.getTextCenterY((weekRectHeight + mTempRect.top) / 2, paint), paint);
    }

    private void drawDate (
            Canvas c, Column column, Rect rect, Paint paint, TableConfig config,
            String date)
    {

        int top = rect.top + weekRectHeight;
        mTempRect.set(rect.left, top, rect.right, rect.bottom);
        if (column.isToday())
        {
            mDateFontStyle.setTextColor(mTodayTextColor);
        }
        else
        {
            mDateFontStyle.setTextColor(mDateTextColor);
        }
        mDateFontStyle.fillPaint(paint);
        c.drawText(date, DrawUtils.getTextCenterX(mTempRect.left, mTempRect.right, paint)
                , DrawUtils.getTextCenterY((mTempRect.bottom + top) / 2, paint), paint);
    }

    public boolean drawBackground (Canvas c, Column column, Rect rect, TableConfig config)
    {

        ICellBackgroundFormat<Column> backgroundFormat = config.getColumnCellBackgroundFormat();
        if (isDrawBg && backgroundFormat != null)
        {
            backgroundFormat.drawBackground(c, rect, column, config.getPaint());
            return true;
        }
        return false;
    }

    public void drawGridBackground (Canvas c, Rect rect, TableConfig config, int color)
    {

        Paint paint = config.getPaint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        c.drawRect(rect, paint);
    }

    public void drawTodayBitmap (
            Canvas c, Column column, Rect rect, Paint paint, TableConfig config,
            boolean isToday)
    {

        if (!isToday)
        {
            return;
        }
        Bitmap bitmap = getBitmap();
        if (bitmap == null)
        {
            return;
        }

        int top = rect.top + weekRectHeight;
        mTempRect.set(rect.left, top, rect.right, rect.bottom);
        Rect drawRect = new Rect();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        int imageWidth = mTempRect.height();
        int imageHeight = mTempRect.height();

        int width = bitmap.getHeight();
        int height = bitmap.getHeight();

        mImgRect.set(0, 0, width, height);
        float scaleX = (float) width / imageWidth;
        float scaleY = (float) height / imageHeight;
        if (scaleX > 1 || scaleY > 1)
        {
            if (scaleX > scaleY)
            {
                width = (int) (width / scaleX);
                height = imageHeight;
            }
            else
            {
                height = (int) (height / scaleY);
                width = imageWidth;
            }
        }
        width = (int) (width * config.getZoom());
        height = (int) (height * config.getZoom());
        int disX = (mTempRect.right - mTempRect.left - width) / 2;
        int disY = (mTempRect.bottom - mTempRect.top - height) / 2;

        drawRect.left = mTempRect.left + disX;
        drawRect.top = mTempRect.top + disY;
        drawRect.right = mTempRect.right - disX;
        drawRect.bottom = mTempRect.bottom - disY;

        c.drawBitmap(bitmap, mImgRect, drawRect, paint);
    }

    public boolean isDrawBg ()
    {

        return isDrawBg;
    }

    public void setDrawBg (boolean drawBg)
    {

        isDrawBg = drawBg;
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

    protected Bitmap getBitmap ()
    {

        Bitmap bitmap = cache.get(mTodayResourceID);
        if (bitmap == null)
        {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), mTodayResourceID);
            if (bitmap != null)
            {
                cache.put(mTodayResourceID, bitmap);
            }
        }
        return bitmap;
    }
}
