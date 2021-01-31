package com.jladder.data;
import java.util.List;
import java.util.Map;
/// <summary>
/// 分页查询结果集类
/// </summary>
public class PageResult extends BasicPageResult
{
    /// <summary>
    /// 条件对象文本
    /// </summary>
    public Record condition= new Record();
    /// <summary>
    /// 全字段
    /// </summary>
    public List<Map<String, Object>> fullcolumns;
    /// <summary>
    /// 显示列
    /// </summary>
    public List<Map<String, Object>> columns;


    /// <summary>
    /// 表单查询
    /// </summary>
    public Object queryform;

    /**
     * 消息体
     */
    public String message;

    /// <summary>
    /// 基本构造
    /// </summary>
    public PageResult()
    {
//            this.condition = new Record();
//            condition.Put("pager", new Pager());
    }
    public PageResult(int code)
    {
        statusCode = code;
//            this.condition = new Record();
//            condition.Put("pager", new Pager());
    }

    /// <summary>
    /// 设置分页对象
    /// </summary>
    /// <param name="pager"></param>
    public void SetPager(Pager pager)
    {
        if (this.condition == null) this.condition = new Record();
        this.condition.put("pager", pager);
        this.pager = pager;
    }

    public AjaxResult ToAjaxResult()
    {
        Record record=new Record();
        record.put("pager", pager);
        record.put("condition", condition);
        record.put("queryform", queryform);
        record.put("records", records);
        record.put("columns", columns);
        record.put("fullcolumns", fullcolumns);
        return new AjaxResult(statusCode).setData(record);
    }
}

