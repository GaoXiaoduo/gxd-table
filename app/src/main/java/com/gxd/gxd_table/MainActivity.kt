package com.gxd.gxd_table

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bin.david.form.data.CellRange
import com.bin.david.form.data.column.Column
import com.bin.david.form.data.format.bg.BaseBackgroundFormat
import com.bin.david.form.data.format.draw.ImageResDrawFormat
import com.bin.david.form.data.format.draw.MultiLineDrawFormat
import com.bin.david.form.data.format.draw.TextImageDrawFormat
import com.bin.david.form.data.format.grid.BaseGridFormat
import com.bin.david.form.data.format.title.DateTitleDrawFormat
import com.bin.david.form.data.table.TableData
import com.bin.david.form.listener.OnTableScrollRangeListener
import com.bin.david.form.utils.DensityUtils
import com.gxd.gxd_table.databinding.ActivityMainBinding
import com.gxd.gxd_table.model.PriceConsole

class MainActivity : AppCompatActivity(), OnTableScrollRangeListener
{
    private var _binding: ActivityMainBinding? = null

    private val mBinding get() = _binding!!

    private val mHandler = Handler(Looper.myLooper()!!)

    var mTableData: TableData<PriceConsole>? = null

    var mData = mutableListOf<PriceConsole>()

    var mColumList = mutableListOf<Column<PriceConsole>>()

    var mColorBarArray: IntArray? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initTable()
    }

    private fun initTable()
    {
        mColorBarArray = getYColorBar()
        initData()
        tableClick()
        mHandler.postDelayed(object : Runnable
        {
            override fun run()
            {
                mHandler.postDelayed(this, 3000)
                // addColumns()
                // setColumnsStyle()
            }
        }, 3000)
    }


    private fun initData()
    {
        val dateWidth = DensityUtils.dp2px(this@MainActivity, 50f)

        val colorColumn = Column<PriceConsole>("", "color", MultiLineDrawFormat<PriceConsole>(1))
        colorColumn.isFixed = false
        colorColumn.isAutoMerge = true
        colorColumn.isColorBar = true
        // colorColumn.width = 1

        val houseNameColumn = Column<PriceConsole>("房型", "houseName", MultiLineDrawFormat<PriceConsole>(200))
        houseNameColumn.isFixed = false
        houseNameColumn.isAutoMerge = true
        val channelIconWidth = DensityUtils.dp2px(this@MainActivity, 23f)
        val channelColumn = Column<String>("渠道", "channel", object : ImageResDrawFormat<String>(channelIconWidth, channelIconWidth)
        {
            override fun getContext(): Context
            {
                return this@MainActivity
            }

            override fun getResourceID(isCheck: String, value: String, position: Int): Int
            {
                return R.mipmap.ic_room_price_date_bg

            }
        })
        channelColumn.isFixed = false

        val calendarColumn = Column<PriceConsole>("日历", colorColumn, houseNameColumn, channelColumn) // MultiLineDrawFormat<PriceConsole>(140)) // houseNameColumn) //, channelColumn)
        calendarColumn.isFixed = true

        mColumList.add(calendarColumn)


        // 列数
        for (i: Int in 0..10)
        {
            val info = PriceConsole()
            info.date = (i + 1).toString()
            info.dateName = "六"
            info.dateCompose = "${info.dateName}\n${info.date}"
            //val columnDate = Column<PriceConsole>("9/18", "date", MultiLineDrawFormat<PriceConsole>(30))
            val column = Column<PriceConsole>(info.dateCompose, "dateCompose", MultiLineDrawFormat<PriceConsole>(dateWidth)) // columnDate)
            //column.minHeight = 400
            mColumList.add(column)
        }
        // 行数
        for (i: Int in 0..17)
        {
            val info = PriceConsole()
            info.date = (i + 1).toString()
            info.dateName = "六"
            info.houseName = "房价名称：${(i / 4).toInt()}"
            info.channel = "渠道$i"
            //info.color = "${(i / 4).toInt()}"
            info.color = when
            {
                i < 4  ->
                {
                    mColorBarArray!![0]
                }
                i < 8  ->
                {
                    mColorBarArray!![1]
                }
                i < 12 ->
                {
                    mColorBarArray!![2]
                }
                i < 16 ->
                {
                    mColorBarArray!![3]
                }
                else   ->
                {
                    mColorBarArray!![0]
                }
            }
            info.dateCompose = "¥200\n余2"
            mData.add(info)
        }




        mTableData = TableData<PriceConsole>("房价表", mData, mColumList as List<Column<PriceConsole>>)

        var cellList = mutableListOf<CellRange>()
        var cellRange = CellRange(0, 1, 0, 2)
        cellList.add(cellRange)
        // mTableData!!.userCellRange = cellList


        mBinding.table.setTableData(mTableData)
        // 隐藏行序号
        mBinding.table.config.isShowXSequence = false
        // 隐藏列序号
        mBinding.table.config.isShowYSequence = false
        // 隐藏表名
        mBinding.table.config.isShowTableTitle = false
        // 隐藏列标题
        mBinding.table.config.isShowColumnTitle = true
        // 固定标题
        //mBinding.table.config.isFixedTitle = true
        // 固定左侧颜色条颜色值数组
        mBinding.table.config.yColorBarArray = getYColorBar()
        // 设置日历背景
        mBinding.table.config.calendarBackground = BaseBackgroundFormat(ContextCompat.getColor(this@MainActivity, R.color.white))
        // 设置日历网格
        mBinding.table.config.calendarGridFormat = object : BaseGridFormat()
        {
            override fun drawTableBorderGrid(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int, paint: Paint)
            {
                paint.strokeWidth = mBinding.table.config.contentGridStyle.width
                paint.color = mBinding.table.config.contentGridStyle.color
                canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
            }
        }
        // 设置日历文字格式化
        mBinding.table.config.setCalendarTextFormat(object : TextImageDrawFormat<Int>(40, 20, BOTTOM, 0)
        {
            override fun getContext(): Context
            {
                return this@MainActivity
            }

            override fun getResourceID(t: Int?, value: String?, position: Int): Int
            {
                return R.mipmap.ic_calendar_down
            }

        })
        // 设置日期列标题格式
        mBinding.table.config.columnDateTitleFormat = DateTitleDrawFormat(this@MainActivity,
                R.mipmap.ic_room_price_date_bg, R.color.date_text_color)


        // 设置网格线
        //        mBinding.table.config.tableGridFormat =
        //                object : BaseAbstractGridFormat()
        //                {
        //
        //                    override fun isShowVerticalLine(col: Int, row: Int, cellInfo: CellInfo<*>): Boolean
        //                    {
        //                        return true //col % 2 == 0
        //                    }
        //
        //                    override fun isShowHorizontalLine(col: Int, row: Int, cellInfo: CellInfo<*>): Boolean
        //                    {
        //                        return true //row % 2 == 0
        //                    }
        //
        //                    override fun isShowColumnTitleVerticalLine(col: Int, column: Column<*>): Boolean
        //                    {
        //                        Log.d(TAG, "网格线 垂直 列:$col,column:${column?.columnName},width:${column?.width},computeWidth:${column?.computeWidth}")
        //
        //                        if (col == 0 || col == 1)
        //                        {
        //                            return false
        //                        }
        //                        return true
        //                    }
        //
        //                    override fun isShowColumnTitleHorizontalLine(col: Int, column: Column<*>): Boolean
        //                    {
        //                        // Log.d(TAG, "网格线 水平 列:$col,column:${column?.columnName}")
        //
        //                        if (col == 0 || col == 1 || col == 2)
        //                        {
        //                            return false
        //                        }
        //                        return true
        //                    }
        //
        //
        //                    //            override fun drawTableBorderGrid(canvas: Canvas?, left: Int, top: Int, right: Int, bottom: Int, paint: Paint?)
        //                    //            {
        //                    //                //                paint!!.strokeWidth = 10f
        //                    //                //                paint.color = Color.GREEN
        //                    //                //                canvas!!.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        //                    //            }
        //                    //
        //                    override fun drawColumnTitleGrid(canvas: Canvas?, rect: Rect?, column: Column<*>, col: Int, paint: Paint?)
        //                    {
        //                        super.drawColumnTitleGrid(canvas, rect, column, col, paint)
        //                        //                        paint!!.strokeWidth = 10f
        //                        //                        paint.color = Color.RED
        //                        //                        canvas!!.drawRect(0f, 0f, 200f, 400f, paint)
        //                    }
        //                }

    }

    private fun tableClick()
    {
        mTableData?.onItemClickListener = TableData.OnItemClickListener<String> { column, value, info, col, row ->
            Log.d(TAG, "点击事件 列:$col,行:$row,数据：$value,T:${info.toString()}")
        }
        mBinding.table.onTableScrollRangeListener = this
    }

    private fun addColumns()
    {
        val columList = mutableListOf<Column<PriceConsole>>()
        var dataList = mutableListOf<PriceConsole>()

        for (i: Int in 7..10)
        {
            val info = PriceConsole()
            info.date = i.toString()
            info.price = i * 100
            val column = Column<PriceConsole>(info.date.toString(), "date")
            columList.add(column)
        }
        // 行数
        for (i: Int in 3..4)
        {
            val info = PriceConsole()
            info.date = i.toString()
            dataList.add(info)
        }
        mBinding.table.addColumns(columList, dataList)
    }

    private fun setColumnsStyle()
    {
        val houseNameColumn = mBinding.table.tableData.getColumnByFieldName("houseName")
        houseNameColumn.isFixed = true
    }


    private fun getYColorBar(): IntArray
    {
        val yColorBar1 = ContextCompat.getColor(this, R.color.y_color_bar_1)
        val yColorBar2 = ContextCompat.getColor(this, R.color.y_color_bar_2)
        val yColorBar3 = ContextCompat.getColor(this, R.color.y_color_bar_3)
        val yColorBar4 = ContextCompat.getColor(this, R.color.y_color_bar_4)
        val yColorBar5 = ContextCompat.getColor(this, R.color.white)

        return intArrayOf(yColorBar1, yColorBar2, yColorBar3, yColorBar4, yColorBar5)
    }


    override fun onTableScrollToRight()
    {
        Toast.makeText(this, "我到最右边了", Toast.LENGTH_SHORT)
                .show()
        Log.d(TAG, "滚动 边界 isRight")
    }

    override fun onTableScrollToLeft()
    {

        Toast.makeText(this, "我到最左边了", Toast.LENGTH_SHORT)
                .show()
        Log.d(TAG, "滚动 边界 isLeft")
    }

    companion object
    {
        const val TAG = "表格"
    }

}