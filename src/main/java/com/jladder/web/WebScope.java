package com.jladder.web;

import com.jladder.data.Record;
import com.jladder.datamodel.IDataModel;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.lang.func.Tuple2;

public class WebScope {
    public static Tuple2<Boolean,String> MappingConn(String conn, String tableName){
        return new Tuple2(false);
    }

    private static ThreadLocal<Record> local = new ThreadLocal<Record>();

    public static IDataModel MappingConn(IDataModel dm, String name)
    {
        if (dm == null) return null;
        if (Strings.isBlank(name)) name = dm.getName();
        Tuple2<Boolean, String> ret = MappingConn(dm.getConn(), name);
        if (ret.item1) dm.setConn(ret.item2);
        return dm;
    }


    public static void SetDataModelConn(String tableName, String conn) {
        Core.makeThrow("未实现[026]","WebScope");
    }

    public static void setConn(String conn) {
        Core.makeThrow("未实现[030]","WebScope");
    }

    /**
     * 设置配置值
     * @param option 选项
     * @param value 数值
     * @return
     */
    public static boolean setValue(WebScopeOption option, Object value){
        try {
            if(WebContext.isWeb()){
                WebContext.setAttribute("_webscope_"+option.getName(),value);
            }else{
                Record dt = local.get();
                if(dt==null) {
                    local.set(new Record("_webscope_"+option.getName(),value));
                    return true;
                }
                local.get().put("_webscope_"+option.getName(),value);
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static Object getValue(WebScopeOption option){
        if(WebContext.isWeb()){
           return WebContext.getAttribute("_webscope_"+option.getName());
        }else{
            Record dt = local.get();
            if(dt==null)return null;
           return dt.get("_webscope_"+option.getName());
        }
    }
    public static Object getValue(WebScopeOption option,Object dvalue){
        if(WebContext.isWeb()){
            Object v =  WebContext.getAttribute("_webscope_"+option.getName());
            if(v==null)return dvalue;
            else return v;
        }else{
            Record dt = local.get();
            if(dt==null)return dvalue;
            Object v  = dt.get("_webscope_"+option.getName());
            if(v==null)return dvalue;
            else return v;
        }
    }

    public static boolean ignoreRequestLog(){
        return setValue(WebScopeOption.IgnoreRequestLog,true);
    }

    public static void put(WebScopeOption option,boolean value){
        setValue(option,value);
    }

    public static Boolean get(WebScopeOption option,boolean dvalue){
        return (Boolean)getValue(option,dvalue);
    }

}
