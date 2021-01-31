package com.jladder.db;

public class ReCall
{
    /// <summary>
    /// 键表名
    /// </summary>
    public String TableName;
    /// <summary>
    /// 节选列
    /// </summary>
    public String Columns;
    /// <summary>
    /// 条件对象
    /// </summary>
    public Object Condition;
    /// <summary>
    /// 扩展参数
    /// </summary>
    public String Param;
    /// <summary>
    /// 是否进行查询
    /// </summary>
    public boolean Query;

    public ReCall()
    {
    }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="columns">节选列</param>
    /// <param name="condition">条件对象</param>
    /// <param name="param">扩展参数</param>
    public ReCall(String tableName, String columns, Object condition)
    {
        TableName = tableName;
        Columns = columns;
        Condition = condition;
    }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="columns">节选列</param>
    /// <param name="condition">条件对象</param>
    /// <param name="param">扩展参数</param>
    public ReCall(String tableName, String columns, Object condition,String param)
    {
        TableName = tableName;
        Columns = columns;
        Condition = condition;
        Param = param;

    }
    /// <summary>
    /// 设置是否查值
    /// </summary>
    /// <param name="query"></param>
    /// <returns></returns>
    public ReCall SetQuery(boolean query)
    {
        Query = query;
        return this;
    }
    /// <summary>
    /// 设置查询条件对象
    /// </summary>
    /// <param name="condition">条件对象</param>
    /// <returns></returns>
    public ReCall SetCondition(Object condition)
    {
        Condition = condition;
        return this;
    }
    /// <summary>
    /// 设置扩展参数
    /// </summary>
    /// <param name="param">扩展参数</param>
    /// <returns></returns>
    public ReCall SetParam(String param)
    {
        Param = param;
        return this;
    }
}