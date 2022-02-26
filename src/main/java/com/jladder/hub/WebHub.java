package com.jladder.hub;


import com.jladder.proxy.BaseCrossAccess;
import com.jladder.proxy.ICrossAccess;

public class WebHub
{

    public static boolean IsDebug;   //= Configs.GetString("debug")=="True";






    /// <summary>
    /// 请求日志
    /// </summary>
    public static boolean RequestLog = true;


    public static ICrossAccess CrossAccess=new BaseCrossAccess();
}
