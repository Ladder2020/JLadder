package com.jladder.lang;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jladder.data.MappingInfo;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.db.Rs;
import com.jladder.db.SqlText;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.script.Script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class Mappings {

    /**
     * 整理后返回数据
     */
    private Record m_ret = new Record();
    /**
     * 源数据
     */
    private JSONObject m_data;


    private Map<String, MappingInfo> m_dic = new HashMap<String, MappingInfo>();

    private boolean pass=true;
    private String message=null;

    /**
     * 获取整理返回数据
     * @return
     */
    public Record getRet(){
        return m_ret;
    }

    public void setData(Object data){
        m_data = (JSONObject) JSONObject.parse(Json.toJson(data));
    }
    public void setMapping(List<MappingInfo> config){
        if (!Rs.isBlank(config)) {
            config.forEach(x -> { m_dic.put(x.getSourcepath(), x); });
        }
    }

    /**
     * 修复数据
     * @param source
     * @param mappings
     * @return
     */
    public static Receipt<Record> repair(Object source, List<MappingInfo> mappings){
        Mappings mapping = new Mappings();
        mapping.setData(source);
        mapping.setMapping(mappings);
        mapping.parse();
        if(mapping.pass)return new Receipt<Record>().setData(mapping.m_ret);
        else return new Receipt(false,mapping.message).setData(mapping.m_ret);
    }





    public Receipt put(MappingInfo mapping, Object value){
        if(mapping==null || Strings.isBlank(mapping.getDestpath())) return new Receipt();
        if(mapping.getEnable()!=1)return new Receipt();
        try{
            String[] keys = mapping.getDestpath().split("/");
            Object r = m_ret;
            int type = 1;//2数组 0//基本型
            if(keys.length - 1 > 0){
                for (String key : keys) {
                    if (Strings.isBlank(key)) continue;
                    if (Regex.isMatch(key,"^[\\w\\W]*?\\[[\\w]*\\]$")){
                        Matcher match = Regex.match(key, "^([\\w\\W]*?)\\[([\\w]*)\\]$");
                        String k = match.group(1);
                        String n = match.group(2);
                        if (Strings.isBlank(n)){
                            if (type == 1){
                                Record d = (Record)r;
                                String kk = d.haveKey(k);
                                if (Strings.isBlank(kk)){
                                    r = new Record();
                                    List newlist = new ArrayList<Record>();
                                    newlist.add((Record)r);
                                    d.put(k, newlist);
                                }
                                else{
                                    r = new Record();
                                    List<Record> newlist = (List<Record>) d.get(kk);
                                    newlist.add((Record)r);
                                }
                            }
                            else{
                                List<Record> d = (List<Record>) r;
                                //可能没有

                            }
                        }
                        else{
                            if (type == 1){
                                Record d = (Record) r;
                                String  kk = d.haveKey(k);
                                if (Strings.isBlank(kk)){
                                    List newlist = new ArrayList<Record>();
                                    while (newlist.size() < (Convert.toInt(n)+1)){
                                        newlist.add(new Record());
                                    }
                                    d.put(k, newlist);
                                    r = newlist.get(Convert.toInt(n));
                                }
                                else{
                                    r = new Record();
                                    List<Record> newlist = (List<Record>) d.get(kk);
                                    while (newlist.size() < (Convert.toInt(n) + 1)){
                                        newlist.add(new Record());
                                    }
                                    r = newlist.get(Convert.toInt(n));
                                }
                            }
                            else{
                                List<Record> d = (List<Record>)r;
                                //可能没有
                            }
                        }
                        type = 2;
                    }
                    else{
                        Record d = (Record)r;
                        String k = d.haveKey(key);
                        if (Strings.isBlank(k)) d.put(key, new Record());
                        r = d.get(key);
                        type = 1;
                    }
                }
            }
            String last = Collections.last(keys);
            if (Regex.isMatch(last, "^[\\w\\W]*?\\[[\\w]*\\]$")){
                Matcher match = Regex.match(last, "^(\\w\\W]*?)\\[([\\w]*)\\]$");
                String k = match.group(1);
                String n = match.group(2);
                type = 2;
            }
            else{
                Record d = (Record) r;
//            if (value is JToken)
//            {
//                var jv = value as JToken;
//
//                value = jv.ToString();
//            }
                if(Strings.hasValue(mapping.getValid())){
                    if("required".equalsIgnoreCase(mapping.getValid())){
                        if(value==null || Strings.isBlank(value.toString()))return new Receipt(false,mapping.getDestpath()+"不能为空");
                    }else{
                        if(!Script.eval(Strings.mapping(mapping.getValid(),d),Boolean.class))return new Receipt(false,mapping.getDestpath()+"未通过验证");
                    }
                }
                Receipt reData = reData(mapping, value);
                if (reData.result){
                    d.put(last, reData.data);
                    return new Receipt().setData(reData.data);
                }else{
                    return reData;
                }

            }
            return new Receipt();
        }catch (Exception e){
            return new Receipt(false,e.getMessage());
        }
    }

    public Receipt reData(MappingInfo x, Object data){
        Object v = null;
        //存在固定值选项
        v = Strings.hasValue(x.getValue()) ?Strings.mapping( x.getValue(),m_ret) : data;
        String strv = v.toString();
        if (Strings.hasValue(x.getCmdtype())) {
            String cmd = x.getCmdtext();
            switch (x.getCmdtype()){
                //字典列表
                case "dic":
                    {
                        Record rec = Record.parse(cmd);
                        if (v == null || Strings.isBlank(strv)){
                            v = rec.get("_default_,_else_");
                        }
                        else{
                            String k = rec.haveKey(v.toString());
                            v = rec.get(Strings.hasValue(k) ? k : "_else_");
                        }
                    }
                    break;
                case "fun": //BiilForBasicFunctions的函数
                    //v = typeof(BiilForBasicFunctions).GetMethod(cmd)?.Invoke(null, new object[] { dic, product });
                    break;
                case "ref": //反射函数
                    v = Refs.invoke(cmd, m_ret);
                    break;
                //包含字符处理
                case "has":
                {
                    Record rec = Record.parse(cmd);
                    if (v == null || Strings.isBlank(strv)){
                        v = rec.get("_default_,_else_");
                    }
                    else{
                        boolean find = false;
//                        foreach (var kv in rec)
//                        {
//                            if (strv.Contains(kv.Key))
//                            {
//                                v = kv.Value;
//                                find = true;
//                                break;
//
//                            }
//                        }
//                        if (!find && rec.ContainsKey("_else_"))
//                        {
//                            v = rec.Get("_else_");
//                        }
                    }
                }
                break;
                case "rep": //替换字符
                    if (Strings.hasValue(strv)){
                        String[] d = cmd.split("@@");
                        v = strv.replace(d[0], d.length > 1 ? Collections.last(d) : "");
                    }
                    break;
                case "sql":
                    int index = cmd.indexOf("|");
                    Dao dao = new Dao(index == -1 ? "" : cmd.substring(0, index));
                    try{
                        v = dao.getValue(new SqlText(Strings.mapping(Strings.mapping(cmd.substring(index + 1),m_ret),m_ret)),String.class);
                    }
                    finally{
                        dao.close();
                    }
                    break;
            }
        }
        return new Receipt().setData(v);
    }


    /**
     * 解析数据
     */
    public void parse(){
        parse("",m_data);
        for (Map.Entry<String,MappingInfo> dic : m_dic.entrySet()){
          String key = dic.getValue().getDestpath();
          if(dic.getValue().getIgnore()==1 && Strings.isBlank(m_ret.getString(key))){
              m_ret.delete(key);
          }
        }
    }

    /**
     * 解析对象型数据
     * @param pre 前缀
     * @param dat 对象数据
     */
    public void parse(String pre,JSONObject dat){
        for (Map.Entry<String,Object> d : dat.entrySet()){
            if (d.getValue() == null){

            }
            else{
                String path = Strings.hasValue(pre)?( pre + "/" + d.getKey()): d.getKey();
                String iPath = Collections.haveKey(m_dic,path);
                if (Strings.hasValue(iPath)){
                    put(m_dic.get(iPath), d.getValue());
                }
                else{
                    if(d.getValue() instanceof JSONObject){
                        parse(path, (JSONObject)d.getValue());
                    }
                    if(d.getValue() instanceof JSONArray){
                        parse(pre, d.getKey(), (JSONArray) d.getValue());
                    }
                }
            }
        }
    }

    /**
     * 解析数组型数据
     * @param pre 前缀
     * @param key 键值
     * @param dat 数据
     */
    public void parse(String pre,String key, JSONArray dat){
        for(int index = 0; index < dat.size(); index++){
            Object data = dat.get(index);
            if(data instanceof JSONObject){
                parse(pre + "/" + key + "[]", (JSONObject)data);
                parse(pre + "/" + key + "["+index+"]", (JSONObject)data);
            }
            if(data instanceof JSONArray){
                parse(pre + "/" + key + "[]","[]", (JSONArray)data);
                parse(pre + "/" + key + "["+index+"]","[]",(JSONArray)data);
            }
        }
    }




}
