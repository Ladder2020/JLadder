package com.jladder.actions.impl;
import com.jladder.data.AjaxResult;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.db.DaoSeesion;
import com.jladder.db.Rs;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.hub.DataHub;
import com.jladder.lang.*;
import com.jladder.script.FunctionParam;
import com.jladder.script.FunctionBody;
import com.jladder.script.Script;

import java.util.ArrayList;
import java.util.List;

/**
 * 函数库类
 */
public class ScriptAction {

    public static AjaxResult fun(String name,Record data){
        try{
            FunctionBody fun = DataHub.WorkCache.getFunctionCache(name);
            if(fun==null){
                fun = QueryAction.getObject("sys_function",new Cnd("name",name), FunctionBody.class);
                List<FunctionParam> params = QueryAction.getData("sys_function_params", new Cnd("functionid", fun.getId()), FunctionParam.class);
                fun.setParams(params);
                if("Script".equals(fun.getType())){
                    String code = fun.getCode();
                    if(Strings.isBlank(code))return new AjaxResult(404,"函数执行内容为空[037]");
                    Script script = new Script(code);
                    if(Strings.hasValue(script.getError()))return new AjaxResult(500,script.getError());
                    fun.setScript(script);
                }
                DataHub.WorkCache.addFunctionCache(name,fun);
            }
            if(fun==null)return new AjaxResult(404,"对不起，函数不存在[029]");
            if(data==null)data=new Record();
            AjaxResult ret=new AjaxResult(400);
            List<Object> args=new ArrayList<Object>();
            Record ms = new Record();
            String ignoreLogKeys="";
            if(!Rs.isBlank(fun.getParams())){
                for (FunctionParam param : fun.getParams()) {
                    String key = data.haveKey(param.getParamname());
                    if(param.getRequired()==1 && !data.containsKey(key)){
                        return new AjaxResult(405,"["+param.getParamname()+"]参数未填充");
                    }
                    if(Strings.hasValue(param.getValid())){
                        Receipt check = Strings.check(data.getString(key),param.getValid());
                        if (!check.isSuccess()){
                            return new AjaxResult(444, "参数[" + param.getParamname() + "]" + check.getMessage());
                        }
                    }

                    String stringvalue = (Strings.isBlank(key) ? param.getDvalue(): data.getString(key));
                    Object value=null;
                    switch (param.getDatatype().toLowerCase()){
                        case "int":
                            if (Strings.isBlank(stringvalue)){
                                value=0;
                            }
                            else{
                                if (!Strings.isNumber(stringvalue)) return new AjaxResult(444, "映射参数[" + param.getParamname() + "]不是整数类型");
                                value = Convert.toInt(stringvalue);
                            }
                            break;
                        case "string":
                        case "json":
                        case "string&json":
                            value = stringvalue;
                            break;
                        case "text":
                            value = stringvalue;
                            ignoreLogKeys += key + ",";
                            break;
                        case "file":
                            value = Strings.isBlank(key) ? null : data.get(key);
                            ignoreLogKeys += key + ",";
                            break;
                        case "date":
                            if (!Strings.isDate(stringvalue)){
                                return new AjaxResult(444, "映射参数[" + param.getParamname() + "]不是日期类型");
                            }
                            value =  Convert.toDate(stringvalue);
                            break;
                        case "datetime":
                            if (!Strings.isDateTime(stringvalue)) return new AjaxResult(444, "映射参数[" + param.getParamname() + "]不是日期时间类型");
                            value = Convert.toDate(stringvalue);
                            break;
                        case "number":
                        case "decimal":
                            if (Strings.isBlank(stringvalue)){
                                value = 0;
                            }
                            else{
                                if (!Strings.isDecimal(stringvalue)) return new AjaxResult(444, "映射参数[" + param.getParamname() + "]不是数字类型");
                                value =  Convert.toDouble(stringvalue);
                            }
                            break;
                        case "bool":
                            value =  Convert.toBool(stringvalue);
                            break;
                        default:
                            value =  Strings.isBlank(key) ? param.getDvalue() : data.get(key);
                            break;
                    }
                    ms.put(param.getParamname(),value);
                    args.add(value);
                }
            }
            switch (fun.getType()){
                case "Script":
                {
                    Script script=fun.getScript();
                    if(script==null){
                        String code = fun.getCode();
                        if(Strings.isBlank(code))return new AjaxResult(404,"函数执行内容为空[037]");
                        script = new Script(code);
                        if(Strings.hasValue(script.getError()))return new AjaxResult(500,script.getError());
                        fun.setScript(script);
                    }
                    if(script==null)return new AjaxResult(404,"对不起，函数不存在[043]");
                    if(Rs.isBlank(fun.getParams())){
                        if(data!=null){
                            data.forEach((k,v)->{
                                args.add(v);
                            });
                        }
                    }
                    ret = script.invoke(fun.getFunctionname(), args.toArray()).toResult();
                }
                break;
            }
            return ret;
        }catch (Exception e){
            return new AjaxResult(500,e.getMessage());
        }
    }

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
     * 创建脚本引擎，并缓存
     * @param uuid 唯一标识码
     * @param code 脚本代码
     * @return
     */
    public static Receipt<Script> createScript(String uuid,String code){
        Script script=DataHub.WorkCache.getScriptCache(uuid);
        if(script==null){
            if(Strings.isBlank(code))return new Receipt<Script>(false,"函数内容为空");
            script = new Script(code);
            if(Strings.hasValue(script.getError()))return new Receipt<Script>(false,script.getError());
            DataHub.WorkCache.addScriptCache(uuid,script);
        }
        return new Receipt<Script>().setData(script);
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
