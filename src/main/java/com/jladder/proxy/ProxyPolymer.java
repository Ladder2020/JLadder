package com.jladder.proxy;

import java.util.List;

public class ProxyPolymer {
    /// <summary>
    /// 接口节选
    /// </summary>
    public static final int Extract = 1;
    /// <summary>
    /// 事件函数
    /// </summary>
    public static final int Event = 2;
    /// <summary>
    /// 调用配置
    /// </summary>
    public static final int RouteConfig = 3;

    public static final String SetMap = "SetMap";
    public static final String Function = "Function";
    public static final String Callback = "Callback";
    /**
     * 项目名称
     */
    public String project;

    /***
     * 异步同步方式
     */
    public int asyn = 0;

    /// <summary>
    /// 聚合方式,SetMap|Function|Callback
    /// </summary>
    public String together;
    /// <summary>
    /// 数据取回,增量:diff|全量:all
    /// </summary>
    public String recycle;

    /// <summary>
    /// 轮询等待 不等待:0|等候增量:1
    /// </summary>
    public int wait ;

    /// <summary>
    /// 聚合来源类型 接口节选|事件函数|调用配置
    /// </summary>
    public int sourcetype ;

    /// <summary>
    /// 整理接口
    /// </summary>
    public String totag ;

    /// <summary>
    /// 函数标识名称
    /// </summary>
    public String label;
    /// <summary>
    /// 接口节选
    /// </summary>
    public String tags;
    /// <summary>
    /// 接口调用路由配置
    /// </summary>
    public List<ProxyRouteFunctionInfo> routes;
    /// <summary>
    /// 缓存数据保存时长
    /// </summary>
    public int remain = 5;
}
