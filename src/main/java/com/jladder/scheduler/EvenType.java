package com.jladder.scheduler;

public enum EvenType
{
    /// <summary>
    /// 年
    /// </summary>
    Year(1),
    /// <summary>
    /// 月份
    /// </summary>
    Month (2),
    /// <summary>
    /// 按天
    /// </summary>
    Day(3),
    /// <summary>
    /// 星期
    /// </summary>
    Week (4 ),
    /// <summary>
    /// 小时
    /// </summary>
    Hour (5),
    /// <summary>
    /// 分钟
    /// </summary>
    Minute( 6 ),
    /// <summary>
    /// 按秒
    /// </summary>
    Second (0);

    private int index;
    private EvenType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}