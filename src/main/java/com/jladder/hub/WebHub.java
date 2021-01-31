package com.jladder.hub;


import com.jladder.proxy.BaseCrossAccess;
import com.jladder.proxy.ICrossAccess;

public class WebHub
{
    /// <summary>
    /// 本站实例名称
    /// </summary>
    public static String SiteName;   //= Configs.GetString("site");

    public static boolean IsDebug;   //= Configs.GetString("debug")=="True";

    /// <summary>
    /// web环境的命名空间
    /// </summary>
    public static String WebNameSpace;

    /// <summary>
    /// 控制器的扫描文件夹，多文件夹以"|"分割
    /// </summary>
    public static String ScanDir;

    /// <summary>
    /// 拦截前置地址
    /// </summary>
    public static String InterceptPath;

    /// <summary>
    /// 拦截的后缀名
    /// </summary>
    public static String ExtName;

    /// <summary>
    /// 是否使用MVC框架
    /// </summary>
    public static boolean IsMvc= false;

    /// <summary>
    /// 请求日志
    /// </summary>
    public static boolean RequestLog = true;


    public static ICrossAccess CrossAccess=new BaseCrossAccess();
}
