package com.gxd.gxd_table

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bin.david.form.core.TableConfig
import com.bin.david.form.data.CellInfo
import com.bin.david.form.data.column.Column
import com.bin.david.form.data.format.bg.BaseBackgroundFormat
import com.bin.david.form.data.format.bg.BaseCellBackgroundFormat
import com.bin.david.form.data.format.bg.PriceCellBackgroundFormat
import com.bin.david.form.data.format.draw.CalendarTextImageDrawFormat
import com.bin.david.form.data.format.draw.ImageResDrawFormat
import com.bin.david.form.data.format.draw.MultiLineDrawFormat
import com.bin.david.form.data.format.grid.BaseGridFormat
import com.bin.david.form.data.format.title.DateTitleDrawFormat
import com.bin.david.form.data.style.FontStyle
import com.bin.david.form.data.table.TableData
import com.bin.david.form.listener.OnTableScrollRangeListener
import com.bin.david.form.utils.DensityUtils
import com.google.gson.reflect.TypeToken
import com.gxd.gxd_table.contract.ShopkeeperConstants
import com.gxd.gxd_table.databinding.ActivityMainBinding
import com.gxd.gxd_table.ext.toDate
import com.gxd.gxd_table.ext.toWeek
import com.gxd.gxd_table.helper.ChannelViewHelper
import com.gxd.gxd_table.model.ColumnDateInfo
import com.gxd.gxd_table.model.PriceConsole
import com.gxd.gxd_table.model.ProductPrice
import com.gxd.gxd_table.util.GsonUtils
import com.gxd.gxd_table.util.GxdFileUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), OnTableScrollRangeListener
{
    private var _binding: ActivityMainBinding? = null

    private val mBinding get() = _binding!!

    private val mHandler = Handler(Looper.myLooper()!!)

    var mTableData: TableData<ColumnDateInfo>? = null


    var mColumnList = mutableListOf<Column<PriceConsole>>()

    var mColorBarArray: IntArray? = null

    var mDateWidth: Int = 0

    /**
     * 房价看板信息
     */
    private var mPriceList: MutableList<PriceConsole> = mutableListOf()

    var mDataList: MutableList<ColumnDateInfo> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mDateWidth = DensityUtils.dp2px(this@MainActivity, 50f)
        initFile()
        initTable()
    }

    /**
     * 初始化配置信息
     */
    private fun initFile()
    {

        if (mPriceList.isNullOrEmpty())
        {
            val provinceJson = GxdFileUtils.readFileContent(this@MainActivity, "price_json.json")
            mPriceList = GsonUtils.fromJson(provinceJson, object : TypeToken<MutableList<PriceConsole>>()
            {}.type)
        }
    }

    private fun initTable()
    {
        mColorBarArray = getYColorBar()
        initData()
        initCalendar()
        tableClick()
        //        mHandler.postDelayed(object : Runnable
        //        {
        //            override fun run()
        //            {
        //                mHandler.postDelayed(this, 3000)
        //                // addColumns()
        //                // setColumnsStyle()
        //            }
        //        }, 3000)
    }


    private fun initData()
    {
        var colorColumnName = "color"
        var houseColumnName = "houseName"
        var channelColumnName = "channel"
        val colorColumn = Column<PriceConsole>("", colorColumnName, MultiLineDrawFormat<PriceConsole>(1))
        colorColumn.isFixed = false
        colorColumn.isAutoMerge = true
        colorColumn.isColorBar = true
        // colorColumn.width = 1

        val houseNameColumn = Column<PriceConsole>("房型", houseColumnName, MultiLineDrawFormat<PriceConsole>(200))
        houseNameColumn.isFixed = false
        houseNameColumn.isAutoMerge = true
        val channelIconWidth = DensityUtils.dp2px(this@MainActivity, 23f)
        val channelColumn = Column<String>("渠道", channelColumnName, object : ImageResDrawFormat<String>(channelIconWidth, channelIconWidth)
        {
            override fun getContext(): Context
            {
                return this@MainActivity
            }

            override fun getResourceID(isCheck: String, value: String, position: Int): Int
            {
                return ChannelViewHelper.getChannelIcon(isCheck.toInt()) //R.mipmap.ic_room_price_date_bg

            }
        })
        channelColumn.isFixed = false

        val calendarColumn = Column<PriceConsole>("日历", colorColumn, houseNameColumn, channelColumn) // MultiLineDrawFormat<PriceConsole>(140)) // houseNameColumn) //, channelColumn)
        calendarColumn.isFixed = true

        mColumnList.add(calendarColumn)

        var priceColumns = mutableListOf<Column<PriceConsole>>()
        // 列数
        val colorList = ArrayList<String>()
        val houseNameList = ArrayList<String>()
        val channelDataList = ArrayList<String>()


        for (i: Int in mPriceList.indices)
        {
            val info = mPriceList[i]
            val week = getWeekOrHoliday(info)
            val date = LocalDate.parse(info.date.toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))
                    .toDate()
            info.dateCompose = "${week}\n${date}"

            val productList: List<ProductPrice>? = info.productPrices
            if (productList.isNullOrEmpty())
            {
                return
            }
            if (i == 0)
            {
                for (j: Int in productList.indices)
                {
                    val product = productList[j]

                    val channelList: List<ProductPrice.Channel>? = product.channelList
                    if (channelList.isNullOrEmpty())
                    {
                        return
                    }
                    for (channel in channelList)
                    {
                        // 彩条颜色
                        colorList.add(mColorBarArray!![j % 4].toString())
                        // 房型名称
                        houseNameList.add(product.houseName)
                        // 渠道
                        channelDataList.add(channel.channel.toString())
                    }
                }
            }

            val priceDataList = ArrayList<String>()
            var idList = ArrayList<String>()
            var channelList = ArrayList<String>()
            var dateList = ArrayList<String>()
            var priceList = ArrayList<Int>()
            var clickEnableList = ArrayList<Boolean>()
            var selectedList = ArrayList<Boolean>()
            for (j: Int in productList.indices)
            {
                val product = productList[j]

                val channelDataList: List<ProductPrice.Channel>? = product.channelList
                if (channelDataList.isNullOrEmpty())
                {
                    return
                }
                for (channelData in channelDataList)
                {
                    // 价格信息
                    var price: String = if (channelData.price == null || channelData.price == 0)
                    {
                        " --"
                    } else
                    {
                        "${channelData.price!! / 100}"
                    }
                    // dataInfo.dateCompose = "¥${price}\n余 ${channel.allowStock}"
                    priceDataList.add("¥${price}\n余 ${channelData.allowStock}")
                    idList.add(product.id)
                    channelList.add(channelData.channel.toString())
                    dateList.add("${info.date}")
                    channelData.price?.let { priceList.add(it) }
                    val clickEnable: Boolean = isClickEnable(channelData.price, info.date)
                    clickEnableList.add(clickEnable)
                    selectedList.add(false)
                }
            }
            val fieldName = "dateCompose_${info.date}"
            val column = Column<PriceConsole>(info.dateCompose, fieldName, MultiLineDrawFormat<PriceConsole>(mDateWidth))
            column.isToday = info.date == 20210912
            mDataList.add(ColumnDateInfo(fieldName, priceDataList, idList, channelList, dateList, priceList, clickEnableList, selectedList))
            mColumnList.add(column)
        }

        mDataList.add(ColumnDateInfo(colorColumnName, colorList, null, null, null, null))
        mDataList.add(ColumnDateInfo(houseColumnName, houseNameList, null, null, null, null))
        mDataList.add(ColumnDateInfo(channelColumnName, channelDataList, null, null, null, null))

        mTableData = TableData<ColumnDateInfo>("房价表", mDataList, mColumnList as List<Column<PriceConsole>>)

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
        mBinding.table.config.setCalendarTextFormat(object : CalendarTextImageDrawFormat<String>(40, 20, BOTTOM, 0)
        {
            override fun getContext(): Context
            {
                return this@MainActivity
            }

            override fun getResourceID(t: String?, value: String?, position: Int): Int
            {
                return R.mipmap.ic_calendar_down
            }

        })
        // 设置日期列标题格式
        mBinding.table.config.columnDateTitleFormat = DateTitleDrawFormat(this@MainActivity,
                R.mipmap.ic_room_price_date_bg, R.color.date_text_color)
        // 设置左上角日期标题字体样式
        mBinding.table.config.calendarTitleStyle = FontStyle(this, 14, ContextCompat.getColor(this, R.color.table_calendar_text_color)).setAlign(Paint.Align.CENTER)

        // 设置内容方格价格无效背景颜色(灰色)
        mBinding.table.config.columnPriceCellBackgroundFormat = object : PriceCellBackgroundFormat<CellInfo<*>>()
        {
            override fun getBackGroundColor(column: CellInfo<*>): Int
            {
                return ContextCompat.getColor(this@MainActivity, R.color.table_cell_no_price_bg_color)
            }

            override fun getTextColor(column: CellInfo<*>): Int
            {
                return TableConfig.INVALID_COLOR
            }

            override fun isEffectivePrice(cellInfo: CellInfo<*>, col: Int, row: Int): Boolean
            {
                return false
            }

        }
        // 设置内容方格选中时的背景颜色(蓝色色)
        mBinding.table.config.selectedCellBackgroundFormat = object : BaseCellBackgroundFormat<CellInfo<*>>()
        {
            override fun getBackGroundColor(cellInfo: CellInfo<*>): Int
            {
                return ContextCompat.getColor(this@MainActivity, R.color.table_cell_selected_bg_color)
            }
        }
    }

    // private val mSelectedCellList: MutableList<>

    private fun tableClick()
    {
        // 单元格点击事件
        mTableData?.onItemClickListener = TableData.OnItemClickListener<String> { column, value, info, col, row ->
            val list = mTableData?.t
            for (i: Int in list!!.indices)
            {
                val colInfo = list[i] as ColumnDateInfo
                if (colInfo.columnName == column.fieldName)
                {
                    val rowString = colInfo.dataList?.get(row)
                    val idString = colInfo.idList?.get(row)
                    val channelString = colInfo.channelList?.get(row)
                    val dateString = colInfo.dateList?.get(row)
                    val clickEnable = colInfo.clickEnableList?.get(row)
                    var selected = colInfo.selectedList?.get(row)
                    var selectedList = colInfo.selectedList
                    if (clickEnable!!)
                    {
                        Log.e(TAG, "点击事件 列:$col,行:$row,clickEnable:$clickEnable")
                        colInfo.selectedList?.set(row, !selected!!)
                    }
                    //Log.e(TAG, "点击事件 列:$col,行:$row,rowString:$rowString,idString：$idString,channelString：$channelString,dateString：$dateString")
                    break
                }
            }
            mBinding.table.invalidate()
        }
        // 左右滑动边界回调事件
        mBinding.table.onTableScrollRangeListener = this
        // 日历区域点击事件
        mTableData?.onCalendarClickListener = TableData.OnCalendarClickListener {
            Log.e(TAG, "点击事件 点击日历：${it},")
        }
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

    private fun getWeekOrHoliday(info: PriceConsole): String
    {
        val week = when (info.dateType)
        {
            //非假日
            ShopkeeperConstants.DateType.DEFAULT_DATE  ->
            {
                LocalDate.parse(info.date.toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))
                        .toWeek()
            }
            //补班
            ShopkeeperConstants.DateType.WORK_DATE     ->
            {
                info.dateName
            }
            //假日当天
            ShopkeeperConstants.DateType.HOLIDAY_TODAY ->
            {
                info.dateName
            }
            //假日
            ShopkeeperConstants.DateType.HOLIDAY_DATE  ->
            {
                info.dateName
            }
            //不放假的假日
            ShopkeeperConstants.DateType.NOT_HOLIDAY   ->
            {
                //TODO 如何处理？
                info.dateName
            }
            else                                       ->
            {
                "--"
            }
        }
        return week!!
    }

    override fun onTableScrollToRight()
    {
        Toast.makeText(this, "我到最右边了", Toast.LENGTH_SHORT)
                .show()
        Log.d(TAG, "点击事件 滚动 边界 isRight")
        var tmpColumnList = mutableListOf<Column<PriceConsole>>()

        var tmpDataList: MutableList<ColumnDateInfo> = mutableListOf()

        for (i: Int in mPriceList.indices)
        {
            val info = mPriceList[i]
            val week = getWeekOrHoliday(info)
            val date = LocalDate.parse(info.date.toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))
                    .toDate()
            info.dateCompose = "${week}\n${date}"

            val productList: List<ProductPrice>? = info.productPrices
            if (productList.isNullOrEmpty())
            {
                return
            }
            val priceDataList = ArrayList<String>()
            var idList = ArrayList<String>()
            var channelList = ArrayList<String>()
            var dateList = ArrayList<String>()
            var priceList = ArrayList<Int>()
            var clickEnableList = ArrayList<Boolean>()
            var selectedList = ArrayList<Boolean>()
            for (j: Int in productList.indices)
            {
                val product = productList[j]

                val channelDataList: List<ProductPrice.Channel>? = product.channelList
                if (channelDataList.isNullOrEmpty())
                {
                    return
                }
                for (channelData in channelDataList)
                {
                    // 价格信息
                    var price: String = if (channelData.price == null || channelData.price == 0)
                    {
                        " --"
                    } else
                    {
                        "${channelData.price!! / 100}"
                    }
                    // dataInfo.dateCompose = "¥${price}\n余 ${channel.allowStock}"
                    priceDataList.add("¥${price}\n余 ${channelData.allowStock}")
                    idList.add(product.id)
                    channelList.add(channelData.channel.toString())
                    dateList.add("${info.date}")
                    channelData.price?.let { priceList.add(it) }
                    val clickEnable: Boolean = isClickEnable(channelData.price, info.date)
                    clickEnableList.add(clickEnable)
                    selectedList.add(false)
                }
            }
            val fieldName = "dateCompose_${info.date}_1"
            val column = Column<PriceConsole>(info.dateCompose, fieldName, MultiLineDrawFormat<PriceConsole>(mDateWidth))
            tmpDataList.add(ColumnDateInfo(fieldName, priceDataList, idList, channelList, dateList, priceList, clickEnableList, selectedList))
            tmpColumnList.add(column)
        }

        mBinding.table.addColumns(tmpColumnList, tmpDataList, true, 1, 3)
    }

    override fun onTableScrollToLeft()
    {

        Toast.makeText(this, "我到最左边了", Toast.LENGTH_SHORT)
                .show()
        Log.d(TAG, "点击事件 滚动 边界 isLeft")
        var tmpColumnList = mutableListOf<Column<PriceConsole>>()

        var tmpDataList: MutableList<ColumnDateInfo> = mutableListOf()

        for (i: Int in mPriceList.indices)
        {
            val info = mPriceList[i]
            val week = getWeekOrHoliday(info)
            val date = LocalDate.parse(info.date.toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))
                    .toDate()
            info.dateCompose = "${week}\n${date}"

            val productList: List<ProductPrice>? = info.productPrices
            if (productList.isNullOrEmpty())
            {
                return
            }
            val priceDataList = ArrayList<String>()
            var idList = ArrayList<String>()
            var channelList = ArrayList<String>()
            var dateList = ArrayList<String>()
            var priceList = ArrayList<Int>()
            var clickEnableList = ArrayList<Boolean>()
            var selectedList = ArrayList<Boolean>()
            for (j: Int in productList.indices)
            {
                val product = productList[j]

                val channelDataList: List<ProductPrice.Channel>? = product.channelList
                if (channelDataList.isNullOrEmpty())
                {
                    return
                }
                for (channelData in channelDataList)
                {
                    // 价格信息
                    var price: String = if (channelData.price == null || channelData.price == 0)
                    {
                        " --"
                    } else
                    {
                        "${channelData.price!! / 100}"
                    }
                    // dataInfo.dateCompose = "¥${price}\n余 ${channel.allowStock}"
                    priceDataList.add("¥${price}\n余 ${channelData.allowStock}")
                    idList.add(product.id)
                    channelList.add(channelData.channel.toString())
                    dateList.add("${info.date}")
                    channelData.price?.let { priceList.add(it) }
                    val clickEnable: Boolean = isClickEnable(channelData.price, info.date)
                    clickEnableList.add(clickEnable)
                    selectedList.add(false)
                }
            }
            val fieldName = "dateCompose_${info.date}_2"
            val column = Column<PriceConsole>(info.dateCompose, fieldName, MultiLineDrawFormat<PriceConsole>(mDateWidth))
            tmpDataList.add(ColumnDateInfo(fieldName, priceDataList, idList, channelList, dateList, priceList, clickEnableList, selectedList))
            tmpColumnList.add(column)
        }

        mBinding.table.addColumns(tmpColumnList, tmpDataList, false, 1, 3)
    }

    private fun initCalendar()
    {
        mTableData?.calendarText = "2021-09-13"
    }

    private fun isClickEnable(price: Int?, date: Int): Boolean
    {
        val today = LocalDate.now()
        val dateFormate = DateTimeFormatter.ofPattern("yyyyMMdd")
        val todayInt = dateFormate.format(today)
                .toInt()
        if (date < todayInt)
        {
            return false
        }
        if (price == null || price == 0)
        {
            return false
        }
        return true
    }

    //    .setColumnCellBackgroundFormat(new BaseCellBackgroundFormat<Column>() {
    //            @Override
    //            public int getBackGroundColor(Column column) {
    //                if("area".equals(column.getFieldName())) {
    //                    return ContextCompat.getColor(NetHttpActivity.this,R.color.column_bg);
    //                }
    //                return TableConfig.INVALID_COLOR;
    //            }
    //            @Override
    //            public int getTextColor(Column column) {
    //                if("area".equals(column.getFieldName())) {
    //                    return ContextCompat.getColor(NetHttpActivity.this, R.color.white);
    //                }else{
    //                    return TableConfig.INVALID_COLOR;
    //                }
    //            }
    //        });

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


    companion object
    {
        const val TAG = "表格"
    }
}