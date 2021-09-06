package com.gxd.gxd_table

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bin.david.form.data.CellInfo
import com.bin.david.form.data.CellRange
import com.bin.david.form.data.column.Column
import com.bin.david.form.data.format.draw.MultiLineDrawFormat
import com.bin.david.form.data.format.grid.BaseAbstractGridFormat
import com.bin.david.form.data.table.TableData
import com.bin.david.form.utils.DensityUtils
import com.gxd.gxd_table.databinding.ActivityMainBinding
import com.gxd.gxd_table.model.PriceConsole

class MainActivity : AppCompatActivity()
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
        val colorColumn = Column<PriceConsole>("", "color", MultiLineDrawFormat<PriceConsole>(1))
        colorColumn.isFixed = true
        colorColumn.isAutoMerge = true
        colorColumn.isColorBar = true
        // colorColumn.width = 1

        val houseNameColumn = Column<PriceConsole>("房价名称", "houseName", MultiLineDrawFormat<PriceConsole>(100))
        houseNameColumn.isFixed = true
        houseNameColumn.isAutoMerge = true

        val channelColumn = Column<PriceConsole>("渠道", "channel", MultiLineDrawFormat<PriceConsole>(30))
        channelColumn.isFixed = true

        val calendarColumn = Column<PriceConsole>("日历", colorColumn, houseNameColumn, channelColumn) // MultiLineDrawFormat<PriceConsole>(140)) // houseNameColumn) //, channelColumn)
        calendarColumn.isFixed = true

        mColumList.add(calendarColumn)

        val dateWidth = DensityUtils.dp2px(this@MainActivity, 50f)
        // 列数
        for (i: Int in 0..10)
        {
            val info = PriceConsole()
            info.date = (i + 1).toString()

            //val columnDate = Column<PriceConsole>("9/18", "date", MultiLineDrawFormat<PriceConsole>(30))
            val column = Column<PriceConsole>("日期", "date", MultiLineDrawFormat<PriceConsole>(dateWidth)) // columnDate)

            mColumList.add(column)
        }
        // 行数
        for (i: Int in 0..17)
        {
            val info = PriceConsole()
            info.houseName = "房价名称：${(i / 4).toInt()}"
            info.channel = "渠道：$i"
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
            info.date = "¥200\n余2"
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
        mBinding.table.config.isFixedTitle = true
        // 显示左侧颜色条
        mBinding.table.config.isShowYColorBar = false
        // 固定左侧颜色条
        mBinding.table.config.isFixedYColorBar = false
        // 固定左侧颜色条颜色值数组
        mBinding.table.config.yColorBarArray = getYColorBar()
        // 设置网格线
        mBinding.table.config.tableGridFormat =
                object : BaseAbstractGridFormat()
                {

                    override fun isShowVerticalLine(col: Int, row: Int, cellInfo: CellInfo<*>): Boolean
                    {
                        return true //col % 2 == 0
                    }

                    override fun isShowHorizontalLine(col: Int, row: Int, cellInfo: CellInfo<*>): Boolean
                    {
                        return true //row % 2 == 0
                    }

                    override fun isShowColumnTitleVerticalLine(col: Int, column: Column<*>): Boolean
                    {
                        Log.d(TAG, "网格线 垂直 列:$col,column:${column?.columnName},width:${column?.width},computeWidth:${column?.computeWidth}")

                        if (col == 0 || col == 1)
                        {
                            return false
                        }
                        return true
                    }

                    override fun isShowColumnTitleHorizontalLine(col: Int, column: Column<*>): Boolean
                    {
                        // Log.d(TAG, "网格线 水平 列:$col,column:${column?.columnName}")

                        if (col == 0 || col == 1 || col == 2)
                        {
                            return false
                        }
                        return true
                    }


                    //            override fun drawTableBorderGrid(canvas: Canvas?, left: Int, top: Int, right: Int, bottom: Int, paint: Paint?)
                    //            {
                    //                //                paint!!.strokeWidth = 10f
                    //                //                paint.color = Color.GREEN
                    //                //                canvas!!.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
                    //            }
                    //
                    override fun drawColumnTitleGrid(canvas: Canvas?, rect: Rect?, column: Column<*>, col: Int, paint: Paint?)
                    {
                        super.drawColumnTitleGrid(canvas, rect, column, col, paint)
                        //                        paint!!.strokeWidth = 10f
                        //                        paint.color = Color.RED
                        //                        canvas!!.drawRect(0f, 0f, 200f, 400f, paint)
                    }
                }

    }

    private fun tableClick()
    {
        mTableData?.onItemClickListener = TableData.OnItemClickListener<String> { column, value, info, col, row ->
            Log.d(TAG, "点击事件 列:$col,行:$row,数据：$value,T:$info")
        }
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

    companion object
    {
        const val TAG = "表格"
    }
}