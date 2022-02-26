package com.jladder.actions.impl;
import com.jladder.data.AjaxResult;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.hub.DataHub;
import com.jladder.lang.Json;
import com.jladder.lang.Strings;
import com.jladder.lang.TypeReference;
import com.jladder.lang.script.Script;

/**
 * 函数库类
 */
public class ScriptAction {
    /**
     * 执行函数库方法
     * @param lib 函数库名
     * @param fun 函数名
     * @param args 参数
     * @return
     */
    public static AjaxResult script(String lib, String fun, String args){
        Receipt<Script> rs = getScript(lib);
        if(!rs.isSuccess())return rs.toResult();
        Receipt<Object> ret=null;
        if(Strings.isJson(args,2)){
            ret = rs.getData().invoke(fun, Json.toObject(args, new TypeReference<Object[]>() {}));
        }else{
            ret = rs.getData().invoke(fun, args);
        }
        return ret.toResult();
    }

    /**
     * 获取函数库
     * @param lib 函数库名
     * @return
     */
    public static Receipt<Script> getScript(String lib){
        Script script=DataHub.WorkCache.getScriptCache(lib);
        if(script==null){
            Record db = QueryAction.getRecord("sys_code", new Cnd("name", lib));
            if(db==null)return new Receipt<Script>(false,"函数库不存在");
            String code = db.getString("content");
            if(Strings.isBlank(code))return new Receipt<Script>(false,"函数内容为空");
            script = new Script(code);
            if(Strings.hasValue(script.getError()))return new Receipt<Script>(false,script.getError());
            DataHub.WorkCache.addScriptCache(lib,script);
        }
        return new Receipt<Script>().setData(script);
    }
}
