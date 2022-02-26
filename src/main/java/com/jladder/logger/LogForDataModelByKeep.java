package com.jladder.logger;

public class LogForDataModelByKeep
{
    /// <summary>
    /// 数据
    /// </summary>
    public Object data;
    /// <summary>
    /// 是否是增加
    /// </summary>
    public boolean isAdd;
    /// <summary>
    /// 模版名称
    /// </summary>
    public String tablename;

    /// <summary>
    /// 用户信息
    /// </summary>
    public String userinfo;

    public LogForDataModelByKeep(String name, String userinfo) {
        this.tablename = name;
        this.userinfo = userinfo;
    }
}