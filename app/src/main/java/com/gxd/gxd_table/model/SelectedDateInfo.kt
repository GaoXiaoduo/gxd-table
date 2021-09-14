package com.gxd.gxd_table.model

/**
 * 选中的方格信息
 * @author gaoxiaoduo
 * @date 2021/9/14 16:37
 * @version 1.0
 */
data class SelectedDateInfo(
        /** 房型id */
        var id: String,
        /** 渠道信息 */
        var channel: Int,
        /** 日期信息 */
        var date: Int
)