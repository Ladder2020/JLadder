package com.jladder.logger;

public class LogForDataModelByKeep
{
    /// <summary>
    /// 数据
    /// </summary>
    public Object Data;
    /// <summary>
    /// 是否是增加
    /// </summary>
    public boolean IsAdd;
    /// <summary>
    /// 模版名称
    /// </summary>
    public String TableName;

    /// <summary>
    /// 用户信息
    /// </summary>
    public String UserInfo;

    public LogForDataModelByKeep(String name, String userinfo) {
        this.TableName = name;
        this.UserInfo = userinfo;
    }
}