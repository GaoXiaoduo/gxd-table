package com.gxd.gxd_table.ext

import java.time.LocalDate

/**
 *
 * @author gaoxiaoduo
 * @date 2021/9/10 16:51
 * @version 1.0
 */
fun LocalDate.toDate(): String
{
    return dayOfMonth.toString()
}

fun LocalDate.toWeek(): String
{
    return weekToFormat(dayOfWeek.toString())
}

fun weekToFormat(weekText: String): String
{
    when (weekText)
    {
        "MONDAY"    -> return "一"
        "TUESDAY"   -> return "二"
        "WEDNESDAY" -> return "三"
        "THURSDAY"  -> return "四"
        "FRIDAY"    -> return "五"
        "SATURDAY"  -> return "六"
        "SUNDAY"    -> return "日"
    }
    return weekText
}
