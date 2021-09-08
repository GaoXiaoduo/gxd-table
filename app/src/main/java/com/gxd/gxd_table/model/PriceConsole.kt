package com.gxd.gxd_table.model

/**
 * 房价看板
 * @author gaoxiaoduo
 * @date 2021/8/30 17:29
 * @version 1.0
 */
class PriceConsole
{
    var color: Int = 0

    var calendar: String? = null

    var houseName: String? = null

    var channel: String? = null

    var date: String? = null

    /** 周/假日+日期组合*/
    var dateCompose: String? = null

    /** 日期类型 -1空 0补班 1假日当天 2假日 3不放假的假日 */
    var dateType: Int? = null

    /** 节假日是否上班 -1空 0不上班 1上班 */
    var dateWorkState: Int? = null

    /** 节假日名称 */
    var dateName: String? = null

    var price: Int? = null
}