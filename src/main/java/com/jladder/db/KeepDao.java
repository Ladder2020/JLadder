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
    public boolean IsManageTran;
    /// <summary>
    /// 是否允许关闭
    /// </summary>
    public boolean IsAllowClose= true;
    /// <summary>
    /// 提请次数
    /// </summary>
    public int TakeTimes = 0;
    /// <summary>
    /// 完成次数
    /// </summary>
    public int FinishTimes = 0;
    /// <summary>
    /// 是否当前活动
    /// </summary>
    public boolean IsActive= true;



    public int Sequence= 0;

    /// <summary>
    /// 提请一次
    /// </summary>
    /// <returns></returns>
    public int Take()
    {
        TakeTimes++;
        return TakeTimes;
    }
    /// <summary>
    /// 完成放置一次
    /// </summary>
    /// <returns></returns>
    public int Finish()
    {
        FinishTimes++;
        return FinishTimes;
    }



}
