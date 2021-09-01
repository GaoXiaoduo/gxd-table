package com.gxd.gxd_table

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.bin.david.form.data.CellRange
import com.bin.david.form.data.column.Column
import com.bin.david.form.data.format.draw.MultiLineDrawFormat
import com.bin.david.form.data.table.TableData
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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initTable()
    }


    private fun initTable()
    {
        initData()
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
        val colorColumn = Column<PriceConsole>("颜", "color", MultiLineDrawFormat<PriceConsole>(5))
        colorColumn.isFixed = true
        colorColumn.isAutoMerge = true

        val houseNameColumn = Column<PriceConsole>("房价名称", "houseName", MultiLineDrawFormat<PriceConsole>(100))
        houseNameColumn.isFixed = true
        houseNameColumn.isAutoMerge = true

        val channelColumn = Column<PriceConsole>("渠道", "channel", MultiLineDrawFormat<PriceConsole>(30))
        channelColumn.isFixed = true

        val calendarColumn = Column<PriceConsole>("日历", colorColumn, houseNameColumn, channelColumn) // MultiLineDrawFormat<PriceConsole>(140)) // houseNameColumn) //, channelColumn)
        calendarColumn.isFixed = true
        calendarColumn.columnName

        mColumList.add(calendarColumn)

        // 列数
        for (i: Int in 0..10)
        {
            val info = PriceConsole()
            info.date = i + 1

            val columnDate = Column<PriceConsole>("9/18", "date", MultiLineDrawFormat<PriceConsole>(30))
            val column = Column<PriceConsole>("日期", columnDate)

            mColumList.add(column)
        }
        // 行数
        for (i: Int in 0..12)
        {
            val info = PriceConsole()
            info.houseName = "房价名称：${(i / 4).toInt()}"
            info.channel = "渠道：$i"
            info.color = "${(i / 4).toInt()}"
            info.date = i
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


    }

    private fun addColumns()
    {
        val columList = mutableListOf<Column<PriceConsole>>()
        var dataList = mutableListOf<PriceConsole>()

        for (i: Int in 7..10)
        {
            val info = PriceConsole()
            info.date = i
            info.price = i * 100
            val column = Column<PriceConsole>(info.date.toString(), "date")
            columList.add(column)
        }
        // 行数
        for (i: Int in 3..4)
        {
            val info = PriceConsole()
            info.date = i
            dataList.add(info)
        }
        mBinding.table.addColumns(columList, dataList)
    }

    private fun setColumnsStyle()
    {
        val houseNameColumn = mBinding.table.tableData.getColumnByFieldName("houseName")
        houseNameColumn.isFixed = true
        // mBinding.table.config.setfi
    }
}