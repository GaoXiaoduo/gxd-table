package com.bin.david.form.listener;

/**
 * 表格滚动范围事件
 *
 * @author gaoxiaoduo
 * @version 1.0
 * @date 2021/9/9 17:29
 */
public interface OnTableScrollRangeListener
{
    /**
     * 滚动到表格右侧边界
     */
    void onTableScrollToRight ();

    /**
     * 滚动到表格最左侧边界
     */
    void onTableScrollToLeft ();
}
