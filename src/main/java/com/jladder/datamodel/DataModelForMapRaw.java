package com.jladder.datamodel;

import com.jladder.data.Record;
import com.jladder.lang.Json;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.lang.TypeReference;

import java.util.List;
import java.util.Map;

public class DataModelForMapRaw
{
    /// <summary>
    /// 模式，db xml json
    /// </summary>
    public String Scheme;

    /// <summary>
    /// 真实表名
    /// </summary>
    public String Table;

    /// <summary>
    /// 缓存策略
    /// </summary>
    public String CacheItems;

    /// <summary>
    /// 全字段
    /// </summary>
    public List<Map<String, Object>> AllColumns;

    /// <summary>
    /// 事件队列
    /// </summary>
//        public Dictionary<String, List<Record>> Events { get; set; }
    public String Events;
    /// <summary>
    /// 模版的data区
    /// </summary>
    public String Data;

    /// <summary>
    /// 执行脚本
    /// </summary>
    public String Script;

    /// <summary>
    /// 参数区
    /// </summary>
    public List<Map<String, Object>> Params;

    /// <summary>
    /// 表单查询列表
    /// </summary>
    public String QueryForm;

    /// <summary>
    /// 数据库连接文本，用于跨域操作，数据库连接键名或者数据库连接的json文本
    /// </summary>
    public String Conn;

    /// <summary>
    /// 类型
    /// </summary>
    public String Type;

    /// <summary>
    /// 键名
    /// </summary>
    public String Name;

    /// <summary>
    /// 扩展存储
    /// </summary>
    public Record Extra;
    /// <summary>
    /// 放置数据
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="value">值</param>
    /// <returns></returns>
    public Record Put(String key, Object value)
    {
        if (Extra == null) Extra = new Record();
        return Extra.put(key, value);
    }
    /// <summary>
    /// 获取数据
    /// </summary>
    /// <param name="key">键名</param>
    /// <returns></returns>
    public Object Get(String key)
    {
        if (Strings.isBlank(key)) return AllColumns;
        // ReSharper disable once PossibleNullReferenceException
        key = key.toLowerCase();
        Object ret = null;
        switch (key)
        {
            case "name":
                ret = Table;
                break;
            case "type":
                ret = Type;
                break;
            case "column":
            case "columns":
                return AllColumns;
            case "event":
            case "events":
                return Events;
            case "script":
                return Script;
            case "param":
            case "params":
                return Params;
            case "conn":
                ret = Conn;
                break;
            case "queryform":
                ret = QueryForm;
                break;
            default:
                ret = Extra.get(key);
                break;
        }
        return ret;
    }

    /// <summary>
    /// 使能位
    /// </summary>
    public String Enable;

    /// <summary>
    /// 权密算法
    /// </summary>
    public String Permission;

    /// <summary>
    /// 统计项目
    /// </summary>
    public String AnalyzeItems;


    public static DataModelForMapRaw From(IDataModel dm){
        DataModelForMapRaw raw = new DataModelForMapRaw();

        raw.Put("el_columns", Strings.hasValue(dm.GetColumn()) && Regex.isMatch(dm.GetColumn(), "\\$\\{([\\W\\w])*?\\}"));
        //raw.Put("el_evnets", Strings.hasValue(Json.toJson(dm.Get)) &&  Regex.isMatch(Json.toJson(dm.Events), "\\$\\{([\\W\\w])*?\\}"));
        raw.Put("el_tablename", Strings.hasValue(dm.TableName) && Regex.isMatch(dm.TableName, "\\$\\{([\\W\\w])*?\\}"));
        raw.AllColumns = dm.GetFullColumns();
        raw.QueryForm = dm.GetQueryForm()==null?"":dm.GetQueryForm().toString();
        raw.Type = dm.Type.name();
        raw.Script = dm.GetScript();
        return raw;


    }

}