package com.gxd.gxd_table.contract

/**
 *
 * @author gaoxiaoduo
 * @date 2021/9/10 17:11
 * @version 1.0
 */
object ShopkeeperConstants
{
    /**
     * 渠道分类 10:途家 20:airbnb 30:小猪 40:榛果 50:线下
     */
    object ChannelType
    {

        /**
         * 途家
         */
        const val TUJIA = 10

        /**
         * airbnb
         */
        const val AIRBNB = 20

        /**
         * 小猪
         */
        const val XIAOZHU = 30

        /**
         * 榛果
         */
        const val ZHENGUO = 40

        /**
         * 线下
         */
        const val XIANXIA = 50

        /**
         * 全部渠道（本地添加）
         */
        const val ALL_CHANNEL = 9999
    }

    /**
     * 日期类型 -1空 0补班 1假日当天 2假日 3不放假的假日
     */
    object DateType
    {

        /**
         * -1 空 (正常的周一到周日，无法定假日)
         */
        const val DEFAULT_DATE: Int = -1

        /**
         * 0 补班
         */
        const val WORK_DATE: Int = 0

        /**
         * 1 假日当天
         */
        const val HOLIDAY_TODAY: Int = 1

        /**
         * 2 假日
         */
        const val HOLIDAY_DATE: Int = 2

        /**
         * 3 不放假的假日
         */
        const val NOT_HOLIDAY: Int = 3
    }
}