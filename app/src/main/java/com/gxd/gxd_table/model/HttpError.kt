package com.gxd.gxd_table.model

import com.google.gson.annotations.SerializedName

/**
 * 服务器端返回错误信息
 *
 * @author gaoxiaoiduo
 * @date 2019-12-09 17:34
 * @version 1.0
 */
open class HttpError
{
    @SerializedName("err_code")
    var errCode: Int = 0

    @SerializedName("err_name")
    var errName: String? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("zh_message")
    var zhMessage: String? = null

    override fun toString(): String
    {
        return "HttpError(errCode=$errCode, errName=$errName, message=$message, zhMessage=$zhMessage)"
    }
}
