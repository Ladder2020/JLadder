package com.jladder.logger;

import com.jladder.data.Record;
import com.jladder.lang.Times;

import java.util.Date;

public class LogForDataModelByVisit
{
    /// <summary>
    /// 开始时间
    /// </summary>
    public Date starttime= Times.now();
    /// <summary>
    /// 结束时间
    /// </summary>
    public Date endtime;
    /// <summary>
    /// 持续时长
    /// </summary>
    public int duration = 0;
    /// <summary>
    /// 模版名称
    /// </summary>
    public String tablename;
    /// <summary>
    /// 请求参数
    /// </summary>
    public Record request ;// = ArgumentMapping.GetRequestParams();
    /// <summary>
    /// 设置结束点
    /// </summary>
    /// <returns></returns>
    /// <summary>
    /// 用户信息
    /// </summary>
    public String userinfo ;

    /// <summary>
    /// 类型
    /// </summary>
    public String type;

    /// <summary>
    /// 设置结束点
    /// </summary>
    /// <returns></returns>
    public LogForDataModelByVisit setEnd()
    {
        endtime=Times.now();
//        TimeSpan ts = EndTime.subtract(StartTime);
//        Duration = Math.Round(ts.TotalMilliseconds, 0) + "ms";
        return this;
    }
    public LogForDataModelByVisit(String tableName,String userInfo,String type){
        this.tablename = tableName;
        this.userinfo = userInfo;
        this.type = type;
    }


}