package com.gxd.gxd_table.model

import com.google.gson.annotations.SerializedName


/**
 * 房价看板
 * @author gaoxiaoduo
 * @date 2021/9/10 11:17
 * @version 1.0
 */
class PriceConsoleList : HttpError()
{
    //    @SerializedName("_meta")
    //    var meta: Meta? = null

    /** 房间数据列表 */
    @SerializedName("data")
    var data: MutableList<PriceConsole>? = null
}

data class ColumnDateInfo
(
        /** 列名，不可更改，在Column.fillData方法内调用 */
        var columnName: String = "",
        /** 每列显示数据，不可更改，在Column.fillData方法内调用 */
        var dataList: MutableList<String>? = null,
        /** 每列房型id */
        var idList: MutableList<String>? = null,
        /** 每列显示渠道类型 */
        var channelList: MutableList<Int>? = null,
        /** 每列显示的日期 */
        var dateList: MutableList<Int>? = null,
        /** 每列显示的价格 */
        var priceList: MutableList<Int>? = null,
        /** 每个单元格是否可以点击 */
        var clickEnableList: MutableList<Boolean>? = null,
        /** 每个单元格是否选中 */
        var selectedList: MutableList<Boolean>? = null
)

class PriceConsole
{
    var columnName = ""

    /** 日期 */
    @SerializedName("date")
    var date: Int = 0

    /** 节假日 */
    @SerializedName("date_holiday")
    var dateHoliday: String? = null

    /** 节假日名称 */
    @SerializedName("date_name")
    var dateName: String? = null

    /** 日期类型 -1空 0补班 1假日当天 2假日 3不放假的假日 */
    @SerializedName("date_type")
    var dateType: Int = 0

    /** 节假日是否上班 -1空 0不上班 1上班 */
    @SerializedName("date_work_state")
    var dateWorkState: Int = 0

    /** 当日库存情况 */
    @SerializedName("day_stock")
    var dayStock: DayStock? = null

    /** */
    @SerializedName("product_prices")
    var productPrices: MutableList<ProductPrice>? = null

    /** 列名称：周/假日+日期组合 */
    var dateCompose: String? = null

    /** 彩条颜色值 */
    var color: Int = 0

    /** 房间名称 */
    var houseName: String? = null

    /** 渠道 */
    var channel: Int = 0
}

data class DayStock(
        /** 今日总库存 */
        @SerializedName("all_stock")
        var allStock: Int,
        /** 今日已售出 */
        @SerializedName("sold_num")
        var soldNum: Int
)

/** 不同房型的房价列表 */
data class ProductPrice(
        /** 房型id */
        @SerializedName("_id")
        var id: String,
        /** 房型名称 */
        @SerializedName("name")
        var houseName: String,
        /** 各渠道价格信息 */
        @SerializedName("channel")
        var channelList: MutableList<Channel>? = null
)
{
    data class Channel(
            /** 可用库存 */
            @SerializedName("allow_stock")
            var allowStock: Int,
            /** 渠道 */
            @SerializedName("channel")
            var channel: Int,
            /** 价格 */
            @SerializedName("price")
            var price: Int? = null
    )
}
