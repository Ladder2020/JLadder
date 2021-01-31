package com.jladder.proxy;

public class ProxyLogOption {
/// <summary>
    /// 请求数据
    /// </summary>
    public static final int Request = 0;

    /// <summary>
    /// 请求数据
    /// </summary>
    public static final int Head = 1;

    /// <summary>
    /// 请求数据
    /// </summary>
    public static final int Call = 2;

    /// <summary>
    /// 请求数据
    /// </summary>
    public static final int Result = 3;

    /// <summary>
    /// 接口基本信息
    /// </summary>
    public static final int Info = 4;

    /// <summary>
    /// 前置请求Hook
    /// </summary>
    public static final int HookBefore = 5;

    /// <summary>
    /// 前置请求Hook
    /// </summary>
    public static final int HookAfter = 6;

    /// <summary>
    /// 错误异常
    /// </summary>
    public static final int Error = 7;
    /// <summary>
    /// 忽略请求日志
    /// </summary>
    public static final int Ignore = 8;

    /// <summary>
    /// 行为跟踪
    /// </summary>
    public static final int Follow = 9;
    /// <summary>
    /// 幂等性
    /// </summary>
    public  static final int Idempotency = 10;




}
