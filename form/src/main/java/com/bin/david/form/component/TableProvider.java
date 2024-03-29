package com.bin.david.form.component;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.TableInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.column.ColumnInfo;
import com.bin.david.form.data.format.bg.BaseBackgroundFormat;
import com.bin.david.form.data.format.bg.IBackgroundFormat;
import com.bin.david.form.data.format.bg.ICellBackgroundFormat;
import com.bin.david.form.data.format.draw.TextDrawFormat;
import com.bin.david.form.data.format.selected.IDrawOver;
import com.bin.david.form.data.format.selected.ISelectFormat;
import com.bin.david.form.data.format.tip.ITip;
import com.bin.david.form.data.table.TableData;
import com.bin.david.form.listener.OnColumnClickListener;
import com.bin.david.form.listener.TableClickObserver;
import com.bin.david.form.utils.DrawUtils;

import java.util.List;

/**
 * Created by huang on 2017/11/1.
 * 表格内容绘制
 */

public class TableProvider<T> implements TableClickObserver
{


    private Rect scaleRect;

    private Rect showRect;

    private TableConfig config;

    private PointF clickPoint;

    private ColumnInfo clickColumnInfo;

    private boolean isClickPoint;

    private OnColumnClickListener onColumnClickListener;

    /**
     * 选中格子格式化
     */
    private SelectionOperation operation;

    private TableData<T> tableData;

    private ITip<Column, ?> tip;

    private Rect clipRect;

    private Rect tempRect; //用于存储数据

    private Column tipColumn;

    private int tipPosition;

    private GridDrawer<T> gridDrawer;

    private PointF tipPoint = new PointF();

    private IDrawOver drawOver;

    private CellInfo cellInfo = new CellInfo();

    /**
     * 左上角日历时间
     */
    private String mCalendarText = "";

    public TableProvider ()
    {

        clickPoint = new PointF(-1, -1);
        clipRect = new Rect();
        tempRect = new Rect();
        operation = new SelectionOperation();
        gridDrawer = new GridDrawer<>();
    }

    /**
     * 绘制
     *
     * @param canvas    画布
     * @param scaleRect 缩放Rect
     * @param showRect  显示Rect
     * @param tableData 表格数据
     * @param config    配置
     */
    public void onDraw (
            Canvas canvas, Rect scaleRect, Rect showRect,
            TableData<T> tableData, TableConfig config)
    {

        setData(scaleRect, showRect, tableData, config);
        canvas.save();
        canvas.clipRect(this.showRect);
        drawColumnTitle(canvas, config);
        drawCount(canvas);
        drawContent(canvas);
        operation.draw(canvas, showRect, config);
        if (drawOver != null)
            drawOver.draw(canvas, scaleRect, showRect, config);
        canvas.restore();
        if (isClickPoint && clickColumnInfo != null)
        {
            onColumnClickListener.onClick(clickColumnInfo);
        }
        if (tipColumn != null)
        {
            drawTip(canvas, tipPoint.x, tipPoint.y, tipColumn, tipPosition);
        }
    }


    /**
     * 设置基本信息和清除数据
     *
     * @param scaleRect 缩放Rect
     * @param showRect  显示Rect
     * @param tableData 表格数据
     * @param config    配置
     */
    private void setData (Rect scaleRect, Rect showRect, TableData<T> tableData, TableConfig config)
    {

        isClickPoint = false;
        clickColumnInfo = null;
        tipColumn = null;
        operation.reset();
        this.scaleRect = scaleRect;
        this.showRect = showRect;
        this.config = config;
        this.tableData = tableData;
        gridDrawer.setTableData(tableData);
    }

    /**
     * 绘制列标题
     *
     * @param canvas
     * @param config
     */
    private void drawColumnTitle (Canvas canvas, TableConfig config)
    {

        if (config.isShowColumnTitle())
        {
            if (config.isFixedTitle())
            {
                drawTitle(canvas);
                canvas.restore();
                canvas.save();
                canvas.clipRect(this.showRect);
            }
            else
            {
                drawTitle(canvas);
            }
        }
    }

    /**
     * 绘制统计行
     *
     * @param canvas 画布
     */
    private void drawCount (Canvas canvas)
    {

        if (tableData.isShowCount())
        {
            float left = scaleRect.left;
            float bottom = config.isFixedCountRow() ?
                    Math.min(scaleRect.bottom, showRect.bottom) : scaleRect.bottom;
            int countHeight = tableData.getTableInfo().getCountHeight();
            float top = bottom - countHeight;
            if (config.getCountBackground() != null)
            {
                tempRect.set((int) left, (int) top, showRect.right, (int) bottom);
                config.getCountBackground().drawBackground(canvas, tempRect, config.getPaint());
            }
            List<ColumnInfo> childColumnInfos = tableData.getChildColumnInfos();
            if (DrawUtils.isVerticalMixRect(showRect, (int) top, (int) bottom))
            {
                List<Column> columns = tableData.getChildColumns();
                int columnSize = columns.size();
                boolean isPerColumnFixed = false;
                clipRect.set(showRect);
                int clipCount = 0;
                for (int i = 0; i < columnSize; i++)
                {
                    Column column = columns.get(i);
                    float tempLeft = left;
                    float width = column.getComputeWidth() * config.getZoom();
                    if (childColumnInfos.get(i).getTopParent().column.isFixed())
                    {
                        if (left < clipRect.left)
                        {
                            left = clipRect.left;
                            clipRect.left += width;
                            isPerColumnFixed = true;
                        }
                    }
                    else if (isPerColumnFixed)
                    {
                        canvas.save();
                        clipCount++;
                        canvas.clipRect(clipRect.left, showRect.bottom - countHeight,
                                showRect.right, showRect.bottom);
                    }
                    tempRect.set((int) left, (int) top, (int) (left + width), (int) bottom);
                    drawCountText(canvas, column, i, tempRect, column.getTotalNumString(), config);
                    left = tempLeft;
                    left += width;
                }
                for (int i = 0; i < clipCount; i++)
                {
                    canvas.restore();
                }
            }
        }
    }

    /**
     * 绘制列标题
     *
     * @param canvas 画布
     */
    private void drawTitle (Canvas canvas)
    {

        int dis = showRect.top - scaleRect.top;
        TableInfo tableInfo = tableData.getTableInfo();
        int titleHeight = tableInfo.getTitleHeight() * tableInfo.getMaxLevel();

        int clipHeight = config.isFixedTitle() ? titleHeight : Math.max(0, titleHeight - dis);
        if (config.getColumnTitleBackground() != null)
        {
            tempRect.set(showRect.left, showRect.top, showRect.right,
                    showRect.top + clipHeight);
            config.getColumnTitleBackground().drawBackground(canvas, tempRect, config.getPaint());
        }
        clipRect.set(showRect);
        List<ColumnInfo> columnInfoList = tableData.getColumnInfos();
        float zoom = config.getZoom();
        boolean isPerColumnFixed = false;
        int clipCount = 0;
        ColumnInfo parentColumnInfo = null;
        for (ColumnInfo info : columnInfoList)
        {
            int left = (int) (info.left * zoom + scaleRect.left);
            //根据top ==0是根部，根据最根部的Title判断是否需要固定
            if (info.top == 0 && info.column.isFixed())
            {
                if (left < clipRect.left)
                {
                    parentColumnInfo = info;
                    left = clipRect.left;
                    fillColumnTitle(canvas, info, left);
                    clipRect.left += info.width * zoom;
                    isPerColumnFixed = true;
                    continue;
                }
                //根部需要固定，同时固定所有子类
            }
            else if (isPerColumnFixed && info.top != 0)
            {
                left = (int) (clipRect.left - info.width * zoom);
                // 固定子列
                if (info.getParent() == null)
                {
                    left += (info.left - parentColumnInfo.left);
                }
            }
            else if (isPerColumnFixed)
            {
                canvas.save();
                canvas.clipRect(clipRect.left, showRect.top, showRect.right,
                        showRect.top + clipHeight);
                isPerColumnFixed = false;
                clipCount++;
            }
            fillColumnTitle(canvas, info, left);
            canvas.restore();
            drawCalendar(canvas, info, left);
        }
        for (int i = 0; i < clipCount; i++)
        {
            canvas.restore();
        }
        if (config.isFixedTitle())
        {
            scaleRect.top += titleHeight;
            showRect.top += titleHeight;
        }
        else
        {
            showRect.top += clipHeight;
            scaleRect.top += titleHeight;
        }

    }

    /**
     * 填充列标题
     *
     * @param canvas 画布
     * @param info   列信息
     * @param left   左边
     */
    private void fillColumnTitle (Canvas canvas, ColumnInfo info, int left)
    {

        int top = (int) (info.top * config.getZoom())
                + (config.isFixedTitle() ? showRect.top : scaleRect.top);
        int right = (int) (left + info.width * config.getZoom());
        int bottom = (int) (top + info.height * config.getZoom());
        if (DrawUtils.isMixRect(showRect, left, top, right, bottom))
        {
            if (!isClickPoint && onColumnClickListener != null)
            {
                if (DrawUtils.isClick(left, top, right, bottom, clickPoint))
                {
                    isClickPoint = true;
                    clickColumnInfo = info;
                    //坐标原点为(0,0),(-1,-1)为屏幕外侧，意味着禁用列名点击事件
                    clickPoint.set(-1, -1);
                }
            }
            Paint paint = config.getPaint();
            tempRect.set(left, top, right, bottom);
            // 画假日背景
            if (config.getColumnTitleCellBackgroundFormat() != null && info.column != null && info.column.isHoliday())
            {
                config.getColumnTitleCellBackgroundFormat().drawBackground(canvas, tempRect,
                        cellInfo, config.getPaint());
                int textColor =
                        config.getColumnTitleCellBackgroundFormat().getTextColor(cellInfo);
                if (textColor != TableConfig.INVALID_COLOR)
                {
                    config.getContentGridStyle().fillPaint(config.getPaint());
                    config.getPaint().setColor(textColor);
                }
            }
            // 画网格边线
            if (config.getTableGridFormat() != null)
            {
                config.getColumnTitleGridStyle().fillPaint(paint);
                int position = tableData.getChildColumns().indexOf(info.column);
                config.getTableGridFormat().drawColumnTitleGrid(canvas, tempRect, info
                        .column, position, paint);
            }
            // 画列标题文字
            if (config.getColumnDateTitleFormat() != null)
            {
                config.getColumnDateTitleFormat().draw(canvas, info.column, tempRect, config);
            }
            else
            {
                tableData.getTitleDrawFormat().draw(canvas, info.column, tempRect, config);
            }
        }
    }

    /**
     * 绘制内容
     *
     * @param canvas 画布
     */
    private void drawContent (Canvas canvas)
    {

        float top;
        float left = scaleRect.left;
        List<Column> columns = tableData.getChildColumns();
        clipRect.set(showRect);
        TableInfo info = tableData.getTableInfo();
        int columnSize = columns.size();
        int dis = config.isFixedCountRow() ? info.getCountHeight()
                : showRect.bottom + info.getCountHeight() - scaleRect.bottom;
        int fillBgBottom = showRect.bottom - Math.max(dis, 0);
        if (config.getContentBackground() != null)
        {
            tempRect.set(showRect.left, showRect.top, showRect.right, fillBgBottom);
            config.getContentBackground().drawBackground(canvas, tempRect, config.getPaint());
        }
        if (config.isFixedCountRow())
        {
            canvas.save();
            canvas.clipRect(showRect.left, showRect.top, showRect.right,
                    showRect.bottom - info.getCountHeight());
        }
        List<ColumnInfo> childColumnInfo = tableData.getChildColumnInfos();
        boolean isPerFixed = false;
        int clipCount = 0;
        Rect correctCellRect;
        TableInfo tableInfo = tableData.getTableInfo();
        // 按列遍历
        for (int i = 0; i < columnSize; i++)
        {
            top = scaleRect.top;
            Column column = columns.get(i);
            float width = column.getComputeWidth() * config.getZoom();

            float tempLeft = left;
            //根据根部标题是否固定
            Column topColumn = childColumnInfo.get(i).getTopParent().column;
            if (topColumn.isFixed())
            {
                isPerFixed = false;
                if (tempLeft < clipRect.left)
                {
                    left = clipRect.left;
                    clipRect.left += width;
                    isPerFixed = true;
                }
            }
            else if (isPerFixed)
            {
                canvas.save();
                canvas.clipRect(clipRect);
                isPerFixed = false;
                clipCount++;
            }
            float right = left + width;
            if (left < showRect.right)
            {
                int size = column.getDatas().size();
                int realPosition = 0;
                // 按行遍历
                for (int j = 0; j < size; j++)
                {
                    String value = column.format(j);
                    int skip = tableInfo.getSeizeCellSize(column, j);
                    int totalLineHeight = 0;
                    for (int k = realPosition; k < realPosition + skip; k++)
                    {
                        totalLineHeight += info.getLineHeightArray()[k];
                    }
                    realPosition += skip;
                    float bottom = top + totalLineHeight * config.getZoom();
                    tempRect.set((int) left, (int) top, (int) right, (int) bottom);
                    correctCellRect = gridDrawer.correctCellRect(j, i, tempRect,
                            config.getZoom()); //矫正格子的大小

                    if (correctCellRect != null)
                    {
                        if (correctCellRect.top < showRect.bottom)
                        {
                            if (correctCellRect.right > showRect.left && correctCellRect.bottom > showRect.top)
                            {
                                Object data = column.getDatas().get(j);
                                if (DrawUtils.isClick(correctCellRect, clickPoint))
                                {
                                    operation.setSelectionRect(i, j, correctCellRect);
                                    tipPoint.x = (left + right) / 2;
                                    tipPoint.y = (top + bottom) / 2;
                                    tipColumn = column;
                                    tipPosition = j;
                                    clickColumn(column, j, value, data);
                                    isClickPoint = true;
                                    clickPoint.set(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
                                }
                                operation.checkSelectedPoint(i, j, correctCellRect);
                                cellInfo.set(column, data, value, i, j);
                                drawContentCellBackground(column, canvas, cellInfo,
                                        correctCellRect, i, j);
                                drawContentCell(canvas, cellInfo, correctCellRect,
                                        column.isColorBar(), config);
                            }
                        }
                        else
                        {
                            break;
                        }
                    }
                    top = bottom;
                }
                left = tempLeft + width;
            }
            else
            {
                break;
            }
        }
        for (int i = 0; i < clipCount; i++)
        {
            canvas.restore();
        }
        if (config.isFixedCountRow())
        {
            canvas.restore();
        }
    }

    protected void drawContentCellBackground (
            Column column, Canvas c, CellInfo<T> cellInfo, Rect rect, int col, int row)
    {

        try
        {

            //画彩条
            if (column.isColorBar())
            {
                new BaseBackgroundFormat(Integer.valueOf(cellInfo.value)).drawBackground(c,
                        rect, config.getPaint());
            }
            //是否可以点击列表
            List<Boolean> clickEnableList = column.getClickEnableList(tableData.getT());
            //是否被选中列表
            List<Boolean> selectedList = column.getSelectedList(tableData.getT());
            if (clickEnableList == null)
            {
                //画非彩条的固定列文字
                if (!column.isColorBar())
                {
                    rect.left += config.getTextLeftOffset();
                    cellInfo.column.getDrawFormat().draw(c, rect, cellInfo, config);
                }
                return;
            }
            Boolean clickEnable = clickEnableList.get(row);
            Boolean selected = selectedList.get(row);
            if (config.getColumnPriceCellBackgroundFormat() != null && config.getSelectedCellBackgroundFormat() != null)
            {
                // 今日之前的方格、价格小于0的方格不可以点击，并且颜色为灰色
                if (!clickEnable)
                {
                    config.getColumnPriceCellBackgroundFormat().drawBackground(c, rect, cellInfo,
                            config.getPaint());
                }
                // 选中后的方格背景为蓝色、文字为白色
                if (selected)
                {
                    if (!column.isColorBar())
                    {
                        config.getSelectedCellBackgroundFormat().drawBackground(c, rect, cellInfo,
                                config.getPaint());
                        int textColor =
                                config.getSelectedCellBackgroundFormat().getTextColor(cellInfo);
                        if (textColor != TableConfig.INVALID_COLOR)
                        {
                            config.getContentGridStyle().fillPaint(config.getPaint());
                            config.getPaint().setColor(textColor);
                        }
                        rect.left += config.getTextLeftOffset();
                        cellInfo.column.getDrawSelectedFormat().draw(c, rect, cellInfo, config);
                    }
                }
                else
                {
                    //画非彩条、非固定列文字(价格列文字)
                    if (!column.isColorBar())
                    {
                        rect.left += config.getTextLeftOffset();
                        cellInfo.column.getDrawPriceFormat().draw(c, rect, cellInfo, config);
                    }
                }
            }
        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 绘制内容格子
     *
     * @param c        画布
     * @param cellInfo 格子信息
     * @param rect     方位
     * @param config   表格配置
     */
    protected void drawContentCell (
            Canvas c, CellInfo<T> cellInfo, Rect rect, Boolean isColorBar,
            TableConfig config)
    {

        if (config.getTableGridFormat() != null)
        {
            config.getContentGridStyle().fillPaint(config.getPaint());
            config.getTableGridFormat().drawContentGrid(c, cellInfo.col, cellInfo.row,
                    rect, cellInfo, config.getPaint());
        }
    }

    /**
     * 绘制左上角日期文字
     */
    private void drawCalendar (Canvas canvas, ColumnInfo info, int left)
    {

        List<ColumnInfo> columnInfoList = tableData.getColumnInfos();
        List<Column> columns = tableData.getChildColumns();
        Column column = columns.get(0);
        Object data = column.getDatas().get(0);
        //日期列的宽度
        int width = columnInfoList.get(0).width;
        int top = columnInfoList.get(0).top;
        // 日历区域占用2个列标题的高度
        int height = config.getColumnDateTitleHeight() * 2;
        tempRect.set(showRect.left, top, width, height);
        // 获取日期数字 -------------- start ---------
        int end = width + info.column.getComputeWidth() / 2;
        try
        {
            if (left >= width && left < end)
            {
                //日期列表
                List<Integer> dateList = info.column.getDateList(tableData.getT());
                if (dateList != null && dateList.size() > 0)
                {
                    mCalendarText = dateList.get(0).toString();
                }
            }
        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        // 获取日期数字 -------------- end  ----------
        if (DrawUtils.isClick(tempRect, clickPoint))
        {

            clickCalendar(mCalendarText);
            isClickPoint = true;
            clickPoint.set(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
        }
        canvas.translate(0, 0);
        canvas.save();
        canvas.clipRect(tempRect);
        cellInfo.set(column, data, mCalendarText, 0, 0);
        config.getContentGridStyle().fillPaint(config.getPaint());

        //绘制背景
        IBackgroundFormat calendarBackground = config.getCalendarBackground();
        if (calendarBackground != null)
        {
            calendarBackground.drawBackground(canvas, tempRect, new Paint());
        }
        //绘制网格
        if (config.getCalendarGridFormat() != null)
        {
            config.getCalendarGridFormat().drawTableBorderGrid(canvas, tempRect.left,
                    tempRect.top, tempRect.right, tempRect.bottom, config.getPaint());
        }
        //字体颜色跟随背景变化
        //绘制文字+图标
        if (config.getCalendarTextFormat() != null)
        {
            config.getCalendarTextFormat().draw(canvas, tempRect, cellInfo, config);
        }
        else
        {
            new TextDrawFormat<T>().draw(canvas, tempRect, cellInfo, config);
        }
        canvas.restore();
        canvas.save();
    }

    /**
     * 点击日历区域
     *
     * @param value
     */
    private void clickCalendar (String value)
    {

        if (!isClickPoint && tableData.getOnCalendarClickListener() != null)
        {
            tableData.getOnCalendarClickListener().onClick(value);
        }
    }

    /**
     * 点击格子
     *
     * @param column   列
     * @param position 位置
     * @param value    值
     * @param data     数据
     */
    private void clickColumn (Column column, int position, String value, Object data)
    {

        if (!isClickPoint && column.getOnColumnItemClickListener() != null)
        {
            column.getOnColumnItemClickListener().onClick(column, value, data, position);
        }
    }


    /**
     * 绘制提示
     */
    private void drawTip (Canvas canvas, float x, float y, Column c, int position)
    {

        if (tip != null)
        {
            tip.drawTip(canvas, x, y, showRect, c, position);
        }
    }

    private void drawCountText (
            Canvas canvas, Column column, int position, Rect rect,
            String text, TableConfig config)
    {

        Paint paint = config.getPaint();
        //绘制背景
        ICellBackgroundFormat<Column> backgroundFormat = config.getCountBgCellFormat();
        if (backgroundFormat != null)
        {
            backgroundFormat.drawBackground(canvas, rect, column, config.getPaint());
        }
        //绘制网格
        if (config.getTableGridFormat() != null)
        {
            config.getContentGridStyle().fillPaint(paint);
            config.getTableGridFormat().drawCountGrid(canvas, position, rect, column, paint);
        }
        config.getCountStyle().fillPaint(paint);
        //字体颜色跟随背景变化
        if (backgroundFormat != null && backgroundFormat.getTextColor(column) != TableConfig.INVALID_COLOR)
        {
            paint.setColor(backgroundFormat.getTextColor(column));
        }
        //绘制字体
        paint.setTextSize(paint.getTextSize() * config.getZoom());
        if (column.getTextAlign() != null)
        {
            paint.setTextAlign(column.getTextAlign());
        }
        canvas.drawText(text, DrawUtils.getTextCenterX(rect.left, rect.right, paint),
                DrawUtils.getTextCenterY(rect.centerY(), paint), paint);
    }


    @Override
    public void onClick (float x, float y)
    {

        clickPoint.x = x;
        clickPoint.y = y;
    }

    public OnColumnClickListener getOnColumnClickListener ()
    {

        return onColumnClickListener;
    }

    public void setOnColumnClickListener (OnColumnClickListener onColumnClickListener)
    {

        this.onColumnClickListener = onColumnClickListener;
    }

    public ITip<Column, ?> getTip ()
    {

        return tip;
    }

    public void setTip (ITip<Column, ?> tip)
    {

        this.tip = tip;
    }


    public void setSelectFormat (ISelectFormat selectFormat)
    {

        this.operation.setSelectFormat(selectFormat);
    }

    public GridDrawer<T> getGridDrawer ()
    {

        return gridDrawer;
    }

    public void setGridDrawer (GridDrawer<T> gridDrawer)
    {

        this.gridDrawer = gridDrawer;
    }


    /**
     * 计算任何point在View的位置
     *
     * @param row 列
     * @param col 行
     *
     * @return
     */
    public int[] getPointLocation (double row, double col)
    {

        List<Column> childColumns = tableData.getChildColumns();
        int[] lineHeights = tableData.getTableInfo().getLineHeightArray();
        int x = 0, y = 0;
        int columnSize = childColumns.size();
        for (int i = 0; i <= (columnSize > col + 1 ? col + 1 : columnSize - 1); i++)
        {
            int w = childColumns.get(i).getComputeWidth();
            if (i == (int) col + 1)
            {
                x += w * (col - (int) col);
            }
            else
            {
                x += w;
            }
        }
        for (int i = 0; i <= (lineHeights.length > row + 1 ? row + 1 : lineHeights.length - 1); i++)
        {
            int h = lineHeights[i];
            if (i == (int) row + 1)
            {
                y += h * (row - (int) row);
            }
            else
            {
                y += h;
            }
        }
        x *= config.getZoom();
        y *= config.getZoom();
        x += scaleRect.left;
        y += scaleRect.top;
        return new int[]{x, y};

    }

    /**
     * 计算任何point在View的大小
     *
     * @param row 列
     * @param col 行
     *
     * @return
     */
    public int[] getPointSize (int row, int col)
    {

        List<Column> childColumns = tableData.getChildColumns();
        int[] lineHeights = tableData.getTableInfo().getLineHeightArray();
        col = col < childColumns.size() ? col : childColumns.size() - 1;//列
        row = row < lineHeights.length ? row : lineHeights.length;//行
        col = col < 0 ? 0 : col;
        row = row < 0 ? 0 : row;
        return new int[]{(int) (childColumns.get(col).getComputeWidth() * config.getZoom()),
                (int) (lineHeights[row] * config.getZoom())};

    }

    /**
     * 设置表面绘制
     */
    public void setDrawOver (IDrawOver drawOver)
    {

        this.drawOver = drawOver;
    }

    public SelectionOperation getOperation ()
    {

        return operation;
    }

}
