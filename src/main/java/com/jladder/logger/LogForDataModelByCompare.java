package com.jladder.logger;

import com.jladder.data.Record;
import com.jladder.lang.Strings;
import com.jladder.lang.Times;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LogForDataModelByCompare
{
    /// <summary>
    /// 模版名称
    /// </summary>
    public String TableName;
    /// <summary>
    /// 原数据
    /// </summary>
    public List<Record> OldRawData;
    /// <summary>
    /// 当前数据
    /// </summary>
    public Object Data;

    /// <summary>
    /// 创建时间
    /// </summary>
    public Date CreateTime= Times.now();

    /// <summary>
    /// 用户信息
    /// </summary>
    public String UserInfo;

    /// <summary>
    /// 不同之处
    /// </summary>
    public List<Record> Different;
    public LogForDataModelByCompare(String tableName,String userinfo){
        this.TableName = tableName;
        this.UserInfo = userinfo;
    }

    /// <summary>
    /// 统计不同之处
    /// </summary>
    public void Diff()
    {
        List<Record> rs = OldRawData;
        Record bean = Record.parse(Data);
        List<Record> ret = new ArrayList<Record>();
        AtomicInteger index = new AtomicInteger();
        rs.forEach(raw ->{
             for (Map.Entry<String,Object> kv : bean.entrySet()){
                 Record diff = new Record();
                 String k = kv.getKey();
                 String oldvalue = raw.getString(k);
                 if (oldvalue.equals(kv.getValue().toString()) || Strings.isBlank((oldvalue + kv.getValue().toString()))) return;
                //Dictionary<string, object> config = DataModel?.GetFieldConfig(kv.Key) ?? new Dictionary<string, object>();
                 diff.put("fieldname", k ).put("old", raw.getObject(k )).put("current", kv.getValue()).put("$rn", index);
                 ret.add(diff);
             }
             index.getAndIncrement();
        });
        Different = ret;
    }

}