package com.jladder.lang;

import java.util.concurrent.ThreadLocalRandom;

/***
 * 随机数据操作类
 */
public class R {

    /// <summary>
    /// 取随机数
    /// </summary>
    /// <param name="start"></param>
    /// <param name="end"></param>
    /// <returns></returns>
    public static int random(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    /// <summary>
    /// 取随机数
    /// </summary>
    /// <param name="length">长度</param>
    /// <returns></returns>
    public static String random(int length)
    {
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < length; i++)
        {

            sb.append(random(0,9) + "");
        }
        return sb.toString();
    }

}
