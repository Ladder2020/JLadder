package com.jladder.data;
import com.alibaba.fastjson.JSONObject;
import com.jladder.db.Rs;
import com.jladder.lang.Collections;
import com.jladder.lang.func.Action1;
import com.jladder.lang.func.Func2;
import com.jladder.lang.func.Func3;
import com.jladder.lang.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;

public class Record extends LinkedHashMap<String,Object> implements java.io.Serializable, Cloneable, Comparable<Record> {




    public Record(){}

    public Record(String key,Object value){
        super.put(key,value);
    }
    public Record(Map<String,Object> map){
        super.putAll(map);
    }

    public static boolean isBlank(Record record){
        if(record == null )return true;
        if(record.size()<1)return true;
        return false;
    }
    public Record put(boolean isPut,String key,Object value){
        if(!isPut)return this;
        super.put(key,value);
        return this;
    }
    /***
     * 放置键值
     * @param key 键名
     * @param value 值
     * @return
     */
    public Record put(String key,Object value){
        super.put(key,value);
        return this;
    }
    /**
     * 重命名
     * @param oldKey 原键名
     * @param newKey 新键名
     * @return
     */
    public Record re(String oldKey, String newKey)
    {
        if (this.containsKey(oldKey) && !this.containsKey(newKey))
        {
            this.put(newKey, this.get(oldKey));
            this.remove(oldKey);
        }
        return this;
    }
    public static Record parse(Object obj){
        return parseWithCallback(obj,null);
    }
    public static Record parseWithAction(Object obj , Action1<KeyValue<String,Object>> fun){
        return parseWithCallback(obj,x->{
            fun.invoke(x);
            return true;
        });
    }
    public static Record parseWithCallback(Object obj, Func2<KeyValue<String,Object>,Boolean> fun){
        if(obj == null )return null;
        Class<?> clazz = obj.getClass();
        Record ret = new Record();
        if(obj instanceof Record)return (Record)obj;
        if(obj instanceof JSONObject){
            ret.putAll(((JSONObject)obj).getInnerMap());
            return ret;
        }
        if(obj instanceof Map){
            ret.putAll((Map<String,Object>)obj);
//            ((Map<Object,Object>)obj).forEach((k,v)->{
//                ret.put(k.toString(),v);
//            });
            return ret;
        }
        if(obj instanceof String){
            String data = obj.toString();
            if(Strings.isJson(data)){
                return Json.toObject(obj.toString(), Record.class);
            }else return null;
        }
        if(Strings.hasValue(clazz.getSimpleName())){
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    boolean flag = field.isAccessible();
                    field.setAccessible(true);
                    Object o = field.get(obj);
                    ret.put(field.getName(), o);
                    field.setAccessible(flag);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ret;
        }
        return ret;
        
        

    }
    public <T> T toClass(Class<T> clazz){
        return com.jladder.lang.Collections.toClass(this,clazz);
    }

    /***
     * 获取值
     * @param index 序号
     * @param clazz 类型
     * @return T 泛型
     * @author YiFeng
     */
    public <T> T get(int index,Class<T> clazz){
        int i = 0;
        Object v=null;
        if(index<0)index=this.size()+index;
        if(index >= 0 && index<this.size()){
            for(String key:this.keySet()){
                if(i==index){
                    v=this.get(key);
                    break;
                }
                else i++;
            }
        }
        if(Object.class.equals(clazz))return (T)v;
        if(v==null)return null;
        if(String.class.equals(clazz))return (T)v.toString();
        return Convert.convert(clazz,v);
    }

    public <T> T get(String key,Class<T> clazz){

        Object v= get(key);
        if(v==null)return null;
        if(clazz.equals(v.getClass()))return (T)v;
        else {
            if(Core.isBaseType(clazz,false))return Convert.convert(clazz,v);
            if(Date.class.equals(clazz))return (T)Convert.toDate(v);
            return Json.toObject(Json.toJson(v),clazz);
        }
    }

    public Object getObject(String key){
        return getObject(key,false);
    }
    public Object getObject(String key,Boolean ignoreCase){
        String[] keys = key.split(",");
        for(String k : keys){
            if(this.containsKey(k))return this.get(k);
        }
        return null;
    }

    @Override
    public int compareTo(Record o) {
        return 0;
    }

    @Override
    public String toString(){
        return Json.toJson(this);
    }
    public String getString(String key){
        return com.jladder.lang.Collections.getString(this,key,"",false);
    }
    public String getString(String key, boolean ignoreCase) {
        return com.jladder.lang.Collections.getString(this,key,"",ignoreCase);
    }
    public String getString(String key,String dValue){
        return getString(key,dValue,false);
    }
    public String getString(String key,String dValue,boolean ignoreCase){
        String v=getString(key,ignoreCase);
        return Strings.isBlank(v) ? dValue:v;

    }
    public int getInt(int index) {
        return get(index,Integer.class);
    }
    public int getInt(String key) {
        return com.jladder.lang.Collections.getInt(this,key,false);
    }
    public int getInt(String key, boolean ignoreCase) {

        return com.jladder.lang.Collections.getInt(this,key,ignoreCase);
    }
    public KeyValue<String,Object> first(){
        for(Map.Entry<String, Object> kv:this.entrySet()){
            return new KeyValue<>(kv.getKey(),kv.getValue());
        }
        return null;
    }
    public static Record turn(List<Map<String,Object>> rs,String propName){
        return Rs.turn(rs,propName,false);
    }
    /***
     * 翻转记录集
     * @param rs 记录集
     * @param propName
     * @param append
     * @return
     */
    public static <T> Record turn(List<T> rs,String propName,boolean append){
        return Rs.turn(rs,propName,append);
    }
    public Record merge(Map<String,Object> dic) {
        if (dic == null) return this;
        for (String key : dic.keySet())
        {
            this.put(key, dic.get(key));
        }
        return this;
    }
    public void delete(Collection<String> keys)
    {
        if (size()<1) return;
        if (keys == null || keys.size() < 1) return;
        for (String key : keys)
        {
            String turekey = this.haveKey(key);
            if (Strings.hasValue(turekey)) remove(turekey);
        }
    }

    public Record delete(String ... keys) {
        if (keys == null || keys.length < 1) return this;
        for(String key : keys)
        {
            String turekey = com.jladder.lang.Collections.haveKey(this, key);
            if (Strings.hasValue(turekey)) this.remove(turekey);
        }
        return this;
    }
    public Record getKeyValue(String keyName, String valueName){
        return getKeyValue(keyName,valueName,true,null,"children");
    }
    public Record getKeyValue(String keyName, String valueName, boolean isAll){
        return getKeyValue(keyName,valueName,isAll,null,"children");
    }
    /// <summary>
    /// 获取键值数据结构
    /// </summary>
    /// <param name="keyName">键名</param>
    /// <param name="valueName">值名</param>
    /// <param name="isAll">是否全部数据结构</param>
    /// <param name="kv">键值集合</param>
    /// <param name="childrenName">子数据键名</param>
    /// <returns></returns>
    public Record getKeyValue(String keyName, String valueName, boolean isAll, Record kv, String childrenName)
    {
        if (kv == null) kv = new Record();
        List<Record> children = (List<Record>)this.get(childrenName);
        if (children!=null && children.size() > 0)
        {
            Record record = new Record();
            children.forEach(x -> x.getKeyValue(keyName, valueName, isAll, record,"children"));
            if (isAll || record.size() > 0)
                kv.put(this.getString(keyName), record);
        }
        else
        {
            if (isAll || this.containsKey(valueName))
                kv.put(this.getString(keyName), this.get(valueName));
        }
        return kv;
    }
    public String haveKey(String columns) {
        return Collections.haveKey(this,columns);
    }

    public String getString(int index) {
        return get(index,String.class);

    }
    /// <summary>
    /// 过滤键(以，分隔数组)
    /// </summary>
    /// <param name="keys">键的数组文本</param>
    /// <returns>剩余的键对</returns>
    public Record filter(String keys)
    {
        if (Strings.isBlank(keys)) return this;
        keys = ("," + keys + ",").toLowerCase();
        Record record= new Record();
        String finalKeys = keys;
        this.forEach((k, v) ->{
            if (!finalKeys.contains("," + k + ",")) record.put(k, v);
        });
        return record;
    }
    /// <summary>
    /// 寻找节点
    /// </summary>
    /// <param name="path">路径</param>
    /// <returns></returns>
    public Object find(String path)
    {
        if (Strings.isBlank(path)) return null;
        String[] nodes = path.split(path.contains("/")?"/":"\\.");
        Object data = this;
        try
        {
            for (String node : nodes)
            {
                if (data == null) return null;
//                if(data is JObject)
//                {
//                    data = Record.Parse(data);
//                }
                if(data instanceof String)
                {
                    if (Strings.isJson(((String)data),1)) data = Record.parse(data);
                }
                if(!(data instanceof Record)){
                    data= Record.parse(data);
                }
                if (data instanceof Record)
                {

                    Matcher match = Regex.match(node, "^([\\w]*)\\[(\\d*)\\]$");
                    if (match.find())
                    {
                        data = ((Record)data).get(match.group(1));
                        int index = Integer.parseInt(match.group(2));
                        if (data instanceof List)
                        {
                            List<Object> ds = (List<Object>)data;
                            //var enumerable = ds as object[] ?? ds.ToArray();
                            if (index < ds.size())
                            {
                                data = ds.get(index);
                            }
                        }
                    }
                    else
                    {
                        data = ((Record)data).get(node);
                    }
                }

            }
        }
        catch (Exception e)
        {
            return null;
        }

        return data;

    }

    public Record match(Collection<String> keys)
    {
        if (keys == null) return this;
        Record record = new Record();
        for (String key : keys)
        {
            String turekey = this.haveKey(key);
            if(Strings.hasValue(turekey))record.put(turekey, this.get(turekey));
        }
        return record;
    }
    /// <summary>
    ///
    /// </summary>
    /// <param name="fun"></param>
    /// <returns></returns>
    public Record match(Func3<String, Object, Boolean> fun)
    {
        if (fun == null) return this;
        Record record = new Record();
        this.forEach((k,v) ->{
            try {
                if (fun.invoke(k, v))
                {
                    record.put(k, v);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return record;
    }
    /// <summary>
    /// 匹配的一些键(以，分隔数组)
    /// </summary>
    /// <param name="keys">键的数组文本</param>
    public Record match(String keys)
    {
        if (Strings.isBlank(keys)) return this;
        Record temp=new Record();
        String[] keyArray = keys.split(",");
        for (String key : keyArray)
        {
            String turekey = this.haveKey(key);
            if(Strings.hasValue(turekey))temp.put(turekey, this.get(turekey));
        }
        return temp;
    }
    public Record clone(){
        Record ret = new Record();
        ret.putAll((Map<String,Object>)super.clone());
        return ret;
    }

    public String[] sortKeys() {
        return this.keySet().stream().sorted(Comparator.comparing(x -> x.toString())).toArray(String[]::new);
    }

    public Record mapping(Map<String,Object> data){
        return mapping(data,true);
    }
    /**
     *
     * @param data
     * @param ispaading
     * @return
     */
    public Record mapping(Map<String,Object> data,boolean ispaading){
        if (this.size() < 1) return this;
        for (String key : this.keySet()){
            if (get(key) instanceof String){
                put(key, Strings.mapping(get(key).toString(),data,ispaading));
            }
        }
        return this;
    }
}
