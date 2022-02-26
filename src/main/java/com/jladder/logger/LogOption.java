package com.jladder.logger;

import com.jladder.db.enums.DbSqlDataType;

public enum LogOption{

    /// <summary>
    /// sql日志
    /// </summary>
    Sql(111) ,

    /// <summary>
    /// 分析型数据
    /// </summary>

    Analysis(112),

    /// <summary>
    /// 异常日志
    /// </summary>
    Exception(10),

    /// <summary>
    /// 逻辑错误，可断言的错误
    /// </summary>
    Error(3),

    /// <summary>
    /// 调试日志
    /// </summary>
    Debug(0),

    /// <summary>
    /// 自定义
    /// </summary>
    Custom(1),

    /// <summary>
    /// 必要记录的
    /// </summary>
    Need(2) ,
    /// <summary>
    /// Http请求
    /// </summary>
    Request(110),
    Proxy(200);

    private int index;
    private LogOption(int index) {
        this.index=index;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index=index;
    }

    public static LogOption get(int index){
        for (LogOption c : LogOption.values()) {
            if (c.getIndex() == index) {
                return c;
            }
        }
        return null;
    }
}