package com.gxd.gxd_table.helper

import com.gxd.gxd_table.R
import com.gxd.gxd_table.contract.ShopkeeperConstants

/**
 *
 * @author gaoxiaoduo
 * @date 2021/9/10 18:53
 * @version 1.0
 */
object ChannelViewHelper
{
    /**
     * 获取渠道图标
     *
     * @param channelType 渠道类型
     * @return
     */
    fun getChannelIcon(channelType: Int?): Int
    {
        when (channelType)
        {
            ShopkeeperConstants.ChannelType.TUJIA   ->
            {
                return R.mipmap.base_component_shopkeeper_ic_channel_tujia
            }
            ShopkeeperConstants.ChannelType.AIRBNB  ->
            {
                return R.mipmap.base_component_shopkeeper_ic_channel_airbnb
            }
            ShopkeeperConstants.ChannelType.XIAOZHU ->
            {
                return R.mipmap.base_component_shopkeeper_ic_channel_xiaozhu
            }
            ShopkeeperConstants.ChannelType.ZHENGUO ->
            {
                return R.mipmap.base_component_shopkeeper_ic_channel_zhenguo
            }
            ShopkeeperConstants.ChannelType.XIANXIA ->
            {
                return R.mipmap.base_component_shopkeeper_ic_channel_xianxia
            }
        }
        return R.mipmap.base_component_shopkeeper_ic_channel_tujia
    }
}