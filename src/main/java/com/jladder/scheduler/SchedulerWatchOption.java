package com.jladder.scheduler;

public class SchedulerWatchOption {
    /// <summary>
    /// 创建计划
    /// </summary>
    public static final int Create = 0;

    /// <summary>
    /// 开始运行
    /// </summary>
    public static final int Begin = 1;

    /// <summary>
    /// 运行过程
    /// </summary>
    public static final int Running = 2;

    /// <summary>
    /// 处理结果
    /// </summary>
    public static final int Result = 3;


    /// <summary>
    /// 错误异常
    /// </summary>
    public static final int Error = 4;

    /// <summary>
    /// 同步记录
    /// </summary>
    public static final int Sync = 5;
}
