package com.jladder.db.jdbc;
/// <summary>
/// 排序结构体
/// </summary>
public class OrderStruct
{
    /// <summary>
    /// 键值
    /// </summary>
    public String key;

    /// <summary>
    /// 序别
    /// </summary>
    public String od = "asc";

    /// <summary>
    /// 索引
    /// </summary>
    public int index = 0;
    /// <summary>
    /// 别名
    /// </summary>
    public String alias  = null;
    /// <summary>
    /// 改键是否可移除
    /// </summary>
    public boolean fixed = false;

    /// <summary>
    /// 初始化
    /// </summary>
    public OrderStruct() { }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="key">键名</param>
    public OrderStruct(String key)
    {
        this.key = key;
    }
}

