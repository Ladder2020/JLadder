package com.jladder.data;

import java.util.List;

public class BasicPageResult
{
    /// <summary>
    /// 状态码
    /// </summary>
    public int statusCode= 200;
    /// <summary>
    /// 记录集
    /// </summary>
    public List<Record> records;
    /// <summary>
    /// 分页
    /// </summary>
    public Pager pager;
    public BasicPageResult(){}
    public BasicPageResult(List<Record> records,Pager pager){
        this.records = records;
        this.pager = pager;
    }
    public BasicPageResult(int statusCode){
        this.statusCode = statusCode;
    }
}
