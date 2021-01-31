package com.jladder.db;

import com.jladder.data.Record;
import com.jladder.lang.Collections;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;

import java.util.*;
import java.util.stream.Collectors;

public class Rs {
    public static <T> boolean isBlank(List<T> rs){
        return Core.isEmpty(rs);
    }
    public static List<Record> create(Record ... record ){
        List ret = new ArrayList<Record>();
        if(record==null)return ret;
        for(Record r : record){
            ret.add(r);
        }
        return ret;
    }


    public static <T> List<T> toClass(List<Record> rs,Class<T> clazz){
        List<T> ret = new ArrayList<>();
        rs.forEach(x->ret.add(x.toClass(clazz)));
        return ret;
    }


    public static <V> Map<String,Map<String,V>> rever(Collection<Map<String,V>> dlist,String propName){
        if (dlist == null) return null;
        if (propName == null) return null;
        Map<String, Map<String, V>> re_map = new HashMap<String, Map<String, V>>();
        for (Map<String, V> cmap : dlist)
        {
            V v = cmap.get(propName);
            String str_fn = v!=null ? v.toString():"";
            if (!Strings.isBlank(str_fn)) re_map.put(str_fn, cmap);
        }
        return re_map.size() < 1 ? null : re_map;
    }


    public static Record turn(List<Record> rs,String propName){
        return turn(rs,propName,false);
    }
    public static <T> Record turn(List<T> rs,String propName,boolean append){
        if (rs == null || rs.size()<1) return null;
        Record record=new Record();
        if (Strings.isBlank(propName)) propName = "id";
        for (T re : rs)
        {
            Record current  = Record.parse(re);
            String key = Collections.getString(current,propName);
            if(append){
                List<Record> old = (List<Record>) record.get(key);
                if(old==null)old = new ArrayList<Record>();
                old.add(current);
                record.put(key,old);
            }else{
                record.put(key,re);
            }

        }
        if (record.size() < 1) return null;
        return record;
    }


    /// <summary>
    /// 过滤符合条件的记录
    /// </summary>
    /// <param name="rs">记录集</param>
    /// <param name="cndss">条件组合</param>
    /// <returns></returns>
    public static List<Record> find(List<Record> rs, List<List<CndStruct>> cndss)
    {
        if (Rs.isBlank(rs)) return null;
        if (isBlank(cndss)) return rs;
        try
        {
            return rs.stream().filter(x->{
                boolean ret = true;

                for (List<CndStruct> cndStructs : cndss)
                {
                    boolean dret = false;
                    for (CndStruct cndStruct : cndStructs)
                    {
                        if (cndStruct.Value == null || Strings.isBlank(cndStruct.Value.toString())) continue;
                        switch (cndStruct.Op.trim())
                        {
                            case "=":
                                if (x.getString(cndStruct.Name) .equals(cndStruct.Value.toString()) ) dret = true;
                                break;
                            case "like":
                                if (x.getString(cndStruct.Name).contains(cndStruct.Value.toString())) dret = true;
                                break;
                            case ">":
                            case ">=":
                            case "<":
                            case "<=":
//                                    if (Regex.IsMatch(x.GetString(cndStruct.Name), "^\\d+\\.?\\d*") &&
//                                        Regex.IsMatch(cndStruct.Value.ToString(), "^\\d+\\.?\\d*"))
//                                    {
//                                        var d = ScriptAction.ExecScript(x.GetString(cndStruct.Name) + cndStruct.Op + cndStruct.Value.ToString());
//                                        if (Regex.IsMatch(d, "true", RegexOptions.IgnoreCase)) dret = true;
//                                    }
//                                    else
//                                    {
//                                        var d = ScriptAction.ExecScript("'" + x.GetString(cndStruct.Name) + "'" + cndStruct.Op + "'" + cndStruct.Value.ToString() + "'");
//                                        if (Regex.IsMatch(d, "true", RegexOptions.IgnoreCase)) dret = true;
//                                    }
                                break;
                            case "in":
                                if (Arrays.stream(cndStruct.Value.toString().split(",")).anyMatch(t -> Regex.replace(t, "^'(?<str>.*?)'$", "${str}").equals(x.getString(cndStruct.Name))))
                                dret = true;
                                break;
                        }

                        if (dret) break;
                    }
                    if (!dret)
                    {
                        ret = false;
                        break;
                    }
                }
                return ret;
            }).collect(Collectors.toList());
        }
        catch (Exception e)
        {
            return null;
        }

    }
    public static List<Record> find(List<Record> rs, Cnd condition)
    {
        if (condition == null) return rs;
        return find(rs, condition.Most);
    }


}
