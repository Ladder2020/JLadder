package com.jladder.proxy;

public class ProxyRouteFunctionInfo {
    /// <summary>
    /// 分发名称
    /// </summary>
    public String name;
    /// <summary>
    /// 条件
    /// </summary>
    public String condition;
    /// <summary>
    /// 处理函数信息
    /// </summary>
    public ProxyFunctionInfo funinfo;
    /// <summary>
    /// 权重
    /// </summary>
    public int weight;
}
