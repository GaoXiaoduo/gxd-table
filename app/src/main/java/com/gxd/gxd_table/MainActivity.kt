package com.gxd.gxd_table

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
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
import com.gxd.gxd_table.model.SelectedDateInfo
import com.gxd.gxd_table.util.GsonUtils
import com.gxd.gxd_table.util.GxdFileUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), OnTableScrollRangeListener
{
    private var _binding: ActivityMainBinding? = null

    private val mBinding get() = _binding!!

    /** 彩条列名称 */
    private val mColorColumnName: String = "color"

    /** 房型列名称 */
    private val mHouseColumnName: String = "houseName"

    /** 渠道列名称 */
    private val mChannelColumnName: String = "channel"

    /** 表格数据 */
    var mTableData: TableData<ColumnDateInfo>? = null

    var mColumnList = mutableListOf<Column<PriceConsole>>()

    /** 彩条颜色数组 */
    var mColorBarArray: IntArray? = null

    var mDateWidth: Int = 0

    /**
     * 房价看板信息
     */
    private var mPriceList: MutableList<PriceConsole> = mutableListOf()

    private var mDataList: MutableList<ColumnDateInfo> = mutableListOf()

    /** 当前选中的行号 */
    private var mCurrentClickRow: Int = -1

    /** 已选中的方格信息 key=列数，value=方格数据信息*/
    private var mSelectedCellMap = HashMap<Int, SelectedDateInfo>()

    /** 今日日期 */
    private val mToday = LocalDate.now()

    /** 已显示的日期页数，每页面15天 */
    private var mPage = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
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

    /**
     * 初始化表格
     */
    private fun initTable()
    {
        mColorBarArray = getYColorBar()
        mDateWidth = DensityUtils.dp2px(this@MainActivity, DATE_COLUMN_WIDTH)
        initFixedColumn()
        initData()
        initTableConfig()
        initTableClick()
    }


    /**
     * 初始化固定列
     */
    private fun initFixedColumn()
    {
        // 设置彩条列属性
        val colorColumn = Column<PriceConsole>("", mColorColumnName, MultiLineDrawFormat<PriceConsole>(DensityUtils.dp2px(this@MainActivity, COLOR_BAR_COLUMN_WIDTH)))
        colorColumn.isFixed = true
        colorColumn.isAutoMerge = true
        colorColumn.isColorBar = true
        // 设置房型名称列属性
        val houseNameColumn = Column<PriceConsole>("房型", mHouseColumnName, MultiLineDrawFormat<PriceConsole>(DensityUtils.dp2px(this@MainActivity, PRODUCT_NAME_COLUMN_WIDTH)))
        houseNameColumn.isFixed = true
        houseNameColumn.isAutoMerge = true
        // 设置渠道列属性
        val channelIconWidth = DensityUtils.dp2px(this@MainActivity, CHANNEL_COLUMN_WIDTH)
        val channelColumn = Column<String>("渠道", mChannelColumnName, object : ImageResDrawFormat<String>(channelIconWidth, channelIconWidth)
        {
            override fun getContext(): Context
            {
                return this@MainActivity
            }

            override fun getResourceID(isCheck: String, value: String, position: Int): Int
            {
                return ChannelViewHelper.getChannelIcon(isCheck.toInt())

            }
        })
        channelColumn.isFixed = true
        // 设置日历列属性（包含彩条、房型、渠道3个子列）
        val calendarColumn = Column<PriceConsole>("日历", colorColumn, houseNameColumn, channelColumn)
        calendarColumn.isFixed = true
        mColumnList.add(calendarColumn)
    }

    /** 初始化表格数据 */
    private fun initData()
    {
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

            val productList: MutableList<ProductPrice>? = info.productPrices
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
            initRowData(info, productList)
        }

        // 添加3个固定列的数据
        mDataList.add(ColumnDateInfo(mColorColumnName, colorList))
        mDataList.add(ColumnDateInfo(mHouseColumnName, houseNameList))
        mDataList.add(ColumnDateInfo(mChannelColumnName, channelDataList))

        // 设置表格数据
        if (mTableData == null)
        {
            mTableData = TableData<ColumnDateInfo>("房价表", mDataList, mColumnList as List<Column<PriceConsole>>)
            mBinding.table.setTableData(mTableData)
        }
    }

    /**
     * 获取价格可用库存信息
     *
     * @param price       价格
     * @param allowStock  可用库存数
     */
    private fun getPriceStockText(price: Int?, allowStock: Int): String
    {
        // 价格信息
        val price: String = if (price == null || price == 0)
        {
            " --"
        } else
        {
            "${price / 100}"
        }
        return "¥${price}\n余 $allowStock"
    }

    /**
     * 初始化表格配置
     */
    private fun initTableConfig()
    {
        // 隐藏行序号
        mBinding.table.config.isShowXSequence = false
        // 隐藏列序号
        mBinding.table.config.isShowYSequence = false
        // 隐藏表名
        mBinding.table.config.isShowTableTitle = false
        // 隐藏列标题
        mBinding.table.config.isShowColumnTitle = true
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
        // 设置日历文字格式化24x12
        mBinding.table.config.setCalendarTextFormat(object : CalendarTextImageDrawFormat<String>(DensityUtils.dp2px(this@MainActivity, CALENDAR_TEXT_IMAGE_WIDTH), DensityUtils.dp2px(this@MainActivity, CALENDAR_TEXT_IMAGE_HEIGHT), BOTTOM, 0)
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
                R.mipmap.ic_room_price_date_bg, R.color.table_cell_column_title_today_text_color, R.color.table_cell_column_title_week_normal_text_color, R.color.table_cell_column_title_holiday_text_color, R.color.table_cell_column_title_date_normal_text_color)
        // 设置日期列标题周五、周六、假日的背景颜色(黄色)
        mBinding.table.config.columnTitleCellBackgroundFormat = object : BaseCellBackgroundFormat<CellInfo<*>>()
        {
            override fun getBackGroundColor(cellInfo: CellInfo<*>): Int
            {
                return ContextCompat.getColor(this@MainActivity, R.color.table_cell_column_title_holiday_bg_color)
            }

            override fun getTextColor(t: CellInfo<*>?): Int
            {
                return ContextCompat.getColor(this@MainActivity, R.color.table_cell_column_title_holiday_text_color)
            }
        }
        // 设置左上角日期标题字体样式
        mBinding.table.config.calendarTitleStyle = FontStyle(this, 12, ContextCompat.getColor(this, R.color.table_calendar_text_color)).setAlign(Paint.Align.CENTER)
        // 设置方格价格字体样式
        mBinding.table.config.priceTitleStyle = FontStyle(this, 12, ContextCompat.getColor(this, R.color.table_cell_column_price_text_color)).setAlign(Paint.Align.CENTER)
        // 设置方格库存字体样式
        mBinding.table.config.stockTitleStyle = FontStyle(this, 12, ContextCompat.getColor(this, R.color.table_cell_column_stock_text_color)).setAlign(Paint.Align.CENTER)

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
        }
        // 设置内容方格选中时的背景颜色(蓝色)
        mBinding.table.config.selectedCellBackgroundFormat = object : BaseCellBackgroundFormat<CellInfo<*>>()
        {
            override fun getBackGroundColor(cellInfo: CellInfo<*>): Int
            {
                return ContextCompat.getColor(this@MainActivity, R.color.table_cell_selected_bg_color)
            }

            override fun getTextColor(t: CellInfo<*>?): Int
            {
                return ContextCompat.getColor(this@MainActivity, R.color.table_cell_selected_text_color)
            }
        }
    }

    /***
     * 初始化表格点击事件
     */
    private fun initTableClick()
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
                    val idString = colInfo.idList?.get(row) ?: ""
                    val channelInt = colInfo.channelList?.get(row) ?: 0
                    val dateInt = colInfo.dateList?.get(row) ?: 0
                    val clickEnable = colInfo.clickEnableList?.get(row)
                    val selected = colInfo.selectedList?.get(row)

                    if (clickEnable!!)
                    {
                        Log.e(TAG, "点击事件 列:$col,行:$row,clickEnable:$clickEnable")
                        if (mCurrentClickRow == -1)
                        {
                            mCurrentClickRow = row
                        }
                        if (mCurrentClickRow == row)
                        {

                            val currentSelected = !selected!!
                            if (currentSelected)
                            {
                                mSelectedCellMap[col] = SelectedDateInfo(idString, channelInt, dateInt)
                            } else
                            {
                                mSelectedCellMap.remove(col)
                            }
                            colInfo.selectedList?.set(row, currentSelected)
                        }
                    }
                    //Log.e(TAG, "点击事件 列:$col,行:$row,rowString:$rowString,idString：$idString,channelString：$channelString,dateString：$dateString")
                    break
                }
            }
            if (mSelectedCellMap.size == 0)
            {
                mCurrentClickRow = -1
            }
            Log.i(TAG, "点击事件 列 选中size:${mSelectedCellMap.size}")
            mBinding.table.invalidate()
        }
        // 左右滑动边界回调事件
        mBinding.table.onTableScrollRangeListener = this
        // 日历区域点击事件
        mTableData?.onCalendarClickListener = TableData.OnCalendarClickListener {
            Log.e(TAG, "点击事件 点击日历：${it},")
        }
    }

    /**
     * 获取彩条颜色数组
     * @return 彩条颜色数组
     */
    private fun getYColorBar(): IntArray
    {
        val yColorBar1 = ContextCompat.getColor(this, R.color.y_color_bar_1)
        val yColorBar2 = ContextCompat.getColor(this, R.color.y_color_bar_2)
        val yColorBar3 = ContextCompat.getColor(this, R.color.y_color_bar_3)
        val yColorBar4 = ContextCompat.getColor(this, R.color.y_color_bar_4)
        return intArrayOf(yColorBar1, yColorBar2, yColorBar3, yColorBar4)
    }

    /**
     * 获取节假日名称
     *
     * @param info 房价看板信息
     * @return
     */
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

    /**
     * 初始化每行信息
     *
     * @param info
     * @param productList
     */
    private fun initRowData(info: PriceConsole, productList: MutableList<ProductPrice>?)
    {
        if (productList.isNullOrEmpty())
        {
            return
        }
        // 当前列下的每个方格价格/余数列表
        val priceDataList = mutableListOf<String>()
        // 当前列下的每个方格房型id列表
        val idList = mutableListOf<String>()
        // 当前列下的每个方格渠道列表
        val channelList = mutableListOf<Int>()
        // 当前列下的每个方格日期列表
        val dateList = mutableListOf<Int>()
        // 当前列下的每个方格价格列表
        val priceList = mutableListOf<Int>()
        // 当前列下的每个方格是否可以点击列表
        val clickEnableList = mutableListOf<Boolean>()
        // 当前列下的每个方格是否被选中列表
        val selectedList = mutableListOf<Boolean>()

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
                priceDataList.add(getPriceStockText(channelData.price, channelData.allowStock))
                idList.add(product.id)
                channelList.add(channelData.channel)
                dateList.add(info.date)
                channelData.price?.let { priceList.add(it) }
                val clickEnable: Boolean = isClickEnable(channelData.price, info.date)
                clickEnableList.add(clickEnable)
                selectedList.add(false)
            }
        }
        val fieldName = "dateCompose_${info.date}"
        val column = Column<PriceConsole>(info.dateCompose, fieldName, MultiLineDrawFormat<PriceConsole>(mDateWidth))
        column.isToday = info.date == 20210919
        column.isHoliday = isHoliday(info)
        mDataList.add(ColumnDateInfo(fieldName, priceDataList, idList, channelList, dateList, priceList, clickEnableList, selectedList))
        mColumnList.add(column)
    }

    /**
     * 添加列
     *
     * @param index
     * @param isFoot  是否在为尾部添加
     * @param startColumnPosition  开始的列坐标
     * @param startChildColumnPosition  开始的子列坐标
     */
    private fun addColumns(index: Int, isFoot: Boolean, startColumnPosition: Int, startChildColumnPosition: Int)
    {
        //index参数正式时去掉
        val tmpColumnList = mutableListOf<Column<PriceConsole>>()
        val tmpDataList: MutableList<ColumnDateInfo> = mutableListOf()

        for (i: Int in mPriceList.indices)
        {
            val info = mPriceList[i]
            val week = getWeekOrHoliday(info)
            val date = LocalDate.parse(info.date.toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))
                    .toDate()
            info.dateCompose = "${week}\n${date}"

            val productList: MutableList<ProductPrice>? = info.productPrices
            if (productList.isNullOrEmpty())
            {
                return
            }
            // 当前列下的每个方格价格/余数列表
            val priceDataList = mutableListOf<String>()
            // 当前列下的每个方格房型id列表
            val idList = mutableListOf<String>()
            // 当前列下的每个方格渠道列表
            val channelList = mutableListOf<Int>()
            // 当前列下的每个方格日期列表
            val dateList = mutableListOf<Int>()
            // 当前列下的每个方格价格列表
            val priceList = mutableListOf<Int>()
            // 当前列下的每个方格是否可以点击列表
            val clickEnableList = mutableListOf<Boolean>()
            // 当前列下的每个方格是否被选中列表
            val selectedList = mutableListOf<Boolean>()

            for (j: Int in productList.indices)
            {
                val product = productList[j]

                val channelDataList: MutableList<ProductPrice.Channel>? = product.channelList
                if (channelDataList.isNullOrEmpty())
                {
                    return
                }
                for (channelData in channelDataList)
                {
                    // 价格信息
                    priceDataList.add(getPriceStockText(channelData.price, channelData.allowStock))
                    idList.add(product.id)
                    channelList.add(channelData.channel)
                    dateList.add(info.date)
                    channelData.price?.let { priceList.add(it) }
                    val clickEnable: Boolean = isClickEnable(channelData.price, info.date)
                    clickEnableList.add(clickEnable)
                    selectedList.add(false)
                }
            }
            val fieldName = "dateCompose_${info.date}_${index}_${mPage}"
            val column = Column<PriceConsole>(info.dateCompose, fieldName, MultiLineDrawFormat<PriceConsole>(mDateWidth))
            column.isToday = info.date == 20210912
            column.isHoliday = isHoliday(info)
            tmpDataList.add(ColumnDateInfo(fieldName, priceDataList, idList, channelList, dateList, priceList, clickEnableList, selectedList))
            tmpColumnList.add(column)
        }
        mBinding.table.addColumns(tmpColumnList, tmpDataList, isFoot, startColumnPosition, startChildColumnPosition)
        mPage++
    }

    override fun onTableScrollToRight()
    {
        Toast.makeText(this, "我到最右边了", Toast.LENGTH_SHORT)
                .show()
        Log.d(TAG, "点击事件 滚动 边界 isRight")

        addColumns(1, true, 1, 3)
    }

    override fun onTableScrollToLeft()
    {

        Toast.makeText(this, "我到最左边了", Toast.LENGTH_SHORT)
                .show()
        Log.d(TAG, "点击事件 滚动 边界 isLeft")

        addColumns(2, false, 1, 3)
    }

    /**
     * 是否为周五、周六、假日
     *
     * @param info
     * @return  true:是，false:否
     */
    private fun isHoliday(info: PriceConsole): Boolean
    {
        val week: String = LocalDate.parse(info.date.toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))
                .toWeek()
        val isWeekBg = (week == "五" || week == "六")
        val isHoliday = info.dateType == 2
        return isHoliday || isWeekBg
    }

    /**
     * 判断方格是否可以点击
     *
     * @param price  价格
     * @param date   日期
     * @return true:可以点击；false:不可以点击
     */
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

    companion object
    {
        /** 彩条列宽度*/
        const val COLOR_BAR_COLUMN_WIDTH: Float = 1f

        /** 房型列宽度*/
        const val PRODUCT_NAME_COLUMN_WIDTH: Float = 60f

        /** 渠道列宽度*/
        const val CHANNEL_COLUMN_WIDTH: Float = 22f

        /** 日期列宽度*/
        const val DATE_COLUMN_WIDTH: Float = 40f

        /** 日历区域向下icon宽度*/
        const val CALENDAR_TEXT_IMAGE_WIDTH: Float = 24f

        /** 日历区域向下icon宽度*/
        const val CALENDAR_TEXT_IMAGE_HEIGHT: Float = 12f

        const val TAG = "表格"
    }
}