package com.jladder.hub;

import com.jladder.actions.ILatch;

/// <summary>
/// 锁存器集线器
/// </summary>
public class LatchHub
{
    /// <summary>
    /// 静止停留时间(分钟)
    /// </summary>
    public static int StayTime = 30;

    /// <summary>
    /// 缓存文件目录
    /// </summary>
    public static String CacheFileDir;// Configs.BasicPath() + "/CacheData";

    /// <summary>
    /// 扫描的时间间隔
    /// </summary>
    public static int ScanTimespan= -1;

    /// <summary>
    /// 外接第三方缓存管理
    /// </summary>
    public static ILatch Extern;
}
