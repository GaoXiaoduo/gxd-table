package com.gxd.gxd_table.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author gaoxiaoduo
 * @version 1.0
 * @date 2021/9/10 16:11
 */
public class GxdFileUtils
{
    /**
     * 获取指定文件的字符串
     *
     * @param context   上下文
     * @param file_name 文件名称
     *
     * @return 文件内容
     */
    public static String readFileContent (Context context, String file_name)
    {

        try
        {
            InputStreamReader inputReader =
                    new InputStreamReader(context.getResources().getAssets().open(file_name),
                            "UTF-8");
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String file_content = "";
            while ((line = bufReader.readLine()) != null)
                file_content += line + "\n";
            bufReader.close();
            inputReader.close();
            return file_content;
        } catch (IOException e)
        {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    private static final String TAG = "FileUtils";

}
