package com.jladder.db;

/// <summary>
/// 持续数据库访问操作对象
/// </summary>
public class KeepDao
{
    /// <summary>
    /// 数据库访问操作对象
    /// </summary>
    public IDao Dao;
    /// <summary>
    /// 数据库事务是否可以管理
    /// </summary>
    public boolean isManageTran;
    /// <summary>
    /// 是否允许关闭
    /// </summary>
    public boolean isAllowClose= true;
    /// <summary>
    /// 提请次数
    /// </summary>
    public int takeTimes = 0;
    /// <summary>
    /// 完成次数
    /// </summary>
    public int finishTimes = 0;
    /// <summary>
    /// 是否当前活动
    /// </summary>
    public boolean isActive= true;



    public int sequence= 0;

    /// <summary>
    /// 提请一次
    /// </summary>
    /// <returns></returns>
    public int take()
    {
        takeTimes++;
        return takeTimes;
    }
    /// <summary>
    /// 完成放置一次
    /// </summary>
    /// <returns></returns>
    public int finish()
    {
        finishTimes++;
        return finishTimes;
    }



}
