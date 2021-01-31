package com.jladder.proxy;

import com.jladder.actions.impl.QueryAction;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.db.Rs;
import com.jladder.entity.DbProxy;
import com.jladder.lang.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ProxyConfig {

    /// <summary>
    /// 权重比例
    /// </summary>
    private List<Integer> weightmaplist = new ArrayList<Integer>();

    /// <summary>
    /// 代理名称
    /// </summary>
    public String name;

    /// <summary>
    /// 原型数据
    /// </summary>
    public DbProxy raw;
    /// <summary>
    /// 参数列表
    /// </summary>
    public List<ProxyParam> params;

    /// <summary>
    /// 映射列表
    /// </summary>
    public List<ProxyMapping> mappings;

    /// <summary>
    /// 执行方法的信息
    /// </summary>
    public ProxyFunctionInfo functioninfo;

    /// <summary>
    /// 执行方法的信息
    /// </summary>
    public ProxyFunctionInfo debugfunctioninfo;
    /// <summary>
    /// 代理限制
    /// </summary>
    public List<ProxyRule> rules;
    /// <summary>
    /// 调用信息
    /// </summary>
    public Map<Integer, ProxyFunctionInfo> callinfo = new HashMap<Integer, ProxyFunctionInfo>();

    /// <summary>
    /// 扩展数据
    /// </summary>
    public Record extdata = new Record();

    /// <summary>
    /// 初始化构造
    /// </summary>
    /// <param name="name"></param>
    public ProxyConfig(String name)
    {
        this.name = name;
    }

    /// <summary>
    /// 初始化构造
    /// </summary>
    /// <param name="raw">数据表原型数据</param>
    public ProxyConfig(DbProxy raw)
    {
        this.raw = raw;
        if (Strings.hasValue(raw.params))
        {
            params = Json.toObject(raw.params, new TypeReference<List<ProxyParam>>() {});
        }
        if (Strings.hasValue(raw.mappings))
        {
            this.mappings = Json.toObject(raw.mappings, new TypeReference<List<ProxyMapping>>() {});
        }
        if (Strings.hasValue(raw.funinfo))
        {
            if (Strings.isJson(raw.funinfo))
            {
                functioninfo = Json.toObject(raw.funinfo, ProxyFunctionInfo.class);
            }
            else
            {
                functioninfo = new ProxyFunctionInfo();
                functioninfo.type="http";
                functioninfo.path = raw.funinfo;
            }

        }
        if (Strings.hasValue(raw.debugging))
        {
            if (Strings.isJson(raw.debugging))
            {
                debugfunctioninfo = Json.toObject(raw.debugging,ProxyFunctionInfo.class);
            }
            else
            {
                debugfunctioninfo = new ProxyFunctionInfo();
                functioninfo.type="http";
                functioninfo.path = raw.debugging;
            }
        }

        if (Strings.hasValue(raw.callinfo))
        {
            List<Record> debugs = Json.toObject(raw.callinfo, new TypeReference<List<Record>>() {
            });
            if(debugs!=null){
                debugs.forEach(x -> {
                    callinfo.put(x.getInt("envcode"), Json.toObject(x.getString("config"),ProxyFunctionInfo.class));
                });
            }
        }
        if(functioninfo!=null) callinfo.put(0, functioninfo);
        if (debugfunctioninfo != null) callinfo.put(1, debugfunctioninfo);
        this.name = raw.name;
        this.rules = QueryAction.getData("sys_service_limit", new Cnd("type", "proxy").put("itemcode", raw.id),null,null,ProxyRule.class);


        callinfo.forEach((index,info) ->{
            switch (info.type.toLowerCase())
        {
            case "route":
                try
                {
                    info.result = Json.toObject(Json.toJson(info.result), new TypeReference<List<ProxyRouteFunctionInfo>>() {});
                }
                catch (Exception e)
                {
                    //Logs.Write(new LogForError(e.Message) { StackTrace = e.StackTrace, Module = Name, Type = "Proxy" }, LogOption.Error);
                }

                break;
            case "balance":
                try
                {
                    List<ProxyRouteFunctionInfo> fs = Json.toObject(Json.toJson(info.result),new TypeReference<List<ProxyRouteFunctionInfo>>(){});
                    if (fs != null)
                    {
                        for (int i = 0; i < fs.size(); i++)
                        {
                            int weight = fs.get(i).weight > 0 ? fs.get(i).weight : 1;
                            for (int j = 0; j < weight; j++)
                            {
                                weightmaplist.add(i);
                            }
                        }
                        info.result = fs;
                    }
                }
                catch (Exception e)
                {
                    //Logs.Write(new LogForError(e.Message) { StackTrace = e.StackTrace, Module = Name, Type = "Proxy" }, LogOption.Error);
                }
                break;
            case "random":
            {
                String jsonText = Json.toJson(info.result);
                if (jsonText.startsWith("[") && jsonText.endsWith("]"))
                {
                    info.result = Json.toObject(jsonText, new TypeReference<List<Object>>() {});
                }
                else
                {
                    ArrayList<Object> result = new ArrayList<Object>();
                    result.add(info.result);
                    info.result = result;
                    
                }
            }
            break;
            case "polymer":
            {
                info.result = Json.toObject(Json.toJson(info.result),ProxyPolymer.class);
            }
            break;
        }


            });


    }
    /// <summary>
    /// 获取调用环境配置
    /// </summary>
    /// <param name="env"></param>
    /// <returns></returns>
    public ProxyFunctionInfo Get(int env)
    {
        if (callinfo.containsKey(env)) return callinfo.get(env);
        else return null;
        // if (CallInfo.ContainsKey(0)) return CallInfo[0];
        // if (CallInfo.ContainsKey(1)) return CallInfo[1];
        // return CallInfo.First().Value;
    }
    /// <summary>
    /// 检查是否监控选项是否开启
    /// </summary>
    /// <param name="option"></param>
    /// <returns></returns>
    public boolean CheckOption(int option)
    {
        if (raw == null) return false;
        return Maths.isBitEq1(raw.logoption,(byte)option);
    }
    /// <summary>
    /// 获取权重
    /// </summary>
    /// <returns></returns>
    public int GetWeightIndex()
    {

        int index = R.random(0, weightmaplist.size());
        return weightmaplist.get(index);
    }
    public List<String> GetRuleValues(String eventcode){
        return GetRuleValues(eventcode,null);
    }
    /// <summary>
    /// 获取规则数据数组
    /// </summary>
    /// <param name="eventcode">动作代码</param>
    /// <param name="userinfo">用户信息</param>
    /// <returns></returns>
    public List<String> GetRuleValues(String eventcode,CrossAccessAuthInfo userinfo)
    {
        if(Rs.isBlank(rules))return null;
        if (userinfo == null)
        {
            return Collections.select(Collections.where(rules,x -> x.eventcode == eventcode && (x.mappingtype == 1111)),x->x.value);
        }
        else
        {
            return Collections.select(Collections.where(rules,x -> x.eventcode == eventcode && (
                    (x.mappingtype == 1 && x.mappingvalue == userinfo.username) ||
                            (x.mappingtype == 2 && userinfo.groups.contains(x.mappingvalue)) ||
                            (x.mappingtype == 3 && x.mappingvalue == userinfo.secret) ||
                            (x.mappingtype == 1111))),x -> x.value);
        }
    }

    /***
     *
     * @param eventcode
     * @return
     */
    public List<ProxyRule> GetRules(String eventcode){
        return GetRules(eventcode,null);
    }

    /***
     * 获取规则列表
     * @param eventcode 动作代码
     * @param userinfo 用户信息
     * @return
     */
    public List<ProxyRule> GetRules(String eventcode, CrossAccessAuthInfo userinfo)
    {
        if (Rs.isBlank(rules)) return null;
        if (userinfo == null)
        {
            return Collections.where(rules,x->x.eventcode == eventcode && (x.mappingtype == 1111));
        }
        else
        {
            return Collections.where(rules,x -> x.eventcode == eventcode && (
                (x.mappingtype == 1 && x.mappingvalue == userinfo.username) ||
                        (x.mappingtype == 2 && userinfo.groups.contains(x.mappingvalue)) ||
                        (x.mappingtype == 3 && x.mappingvalue == userinfo.secret) ||
                        (x.mappingtype == 1111)));
        }
    }
    /// <summary>
    /// 添加扩展数据
    /// </summary>
    /// <param name="name">名称</param>
    /// <param name="value">值</param>
    /// <returns></returns>
    public ProxyConfig PutExt(String name, Object value)
    {
        extdata.put(name, value);
        return this;
    }
    /// <summary>
    /// 或者扩展数据
    /// </summary>
    /// <param name="name">名称</param>
    /// <param name="value">值</param>
    /// <returns></returns>
    public <T> T GetExt(String name,Class<T> glass)
    {
        return extdata.get(name,glass);
    }
}
