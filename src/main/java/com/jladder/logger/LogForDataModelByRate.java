package com.jladder.logger;

import com.jladder.lang.Times;

import java.util.Date;

public class LogForDataModelByRate
{
    /// <summary>
    /// 模版名称
    /// </summary>
    public String tablename;
    /// <summary>
    /// 记录总数
    /// </summary>
    public long recordcount;
    /// <summary>
    /// 开始时间
    /// </summary>
    public Date starttime = Times.now();
    /// <summary>
    /// 结束时间
    /// </summary>
    public Date endtime;
    /// <summary>
    /// 持续时长
    /// </summary>
    public int duration = 0;
    /// <summary>
    /// 单条比例
    /// </summary>
    public String rate = "0us";
    /// <summary>
    /// 是否应用的缓存
    /// </summary>
    public boolean IsCache;

    public LogForDataModelByRate(String tablename){
        this.tablename = tablename;
    }
    public LogForDataModelByRate setEnd()
    {
        endtime = Times.now();
//        TimeSpan ts = EndTime.Subtract(StartTime);
//        var diff = ts.TotalMilliseconds;
//        Duration = Math.Round(diff, 0) + "ms";
//        if (RecordCount > 0)
//        {
//            Rate = Math.Round(diff*1000 / RecordCount,0) + "us";
//        }
        return this;
    }
}