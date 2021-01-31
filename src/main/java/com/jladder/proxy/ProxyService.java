package com.jladder.proxy;
import com.jladder.actions.WebScope;
import com.jladder.actions.impl.QueryAction;
import com.jladder.actions.impl.SaveAction;
import com.jladder.data.*;
import com.jladder.db.Cnd;
import com.jladder.db.Rs;
import com.jladder.db.SqlText;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.entity.DbProxy;
import com.jladder.hub.DataHub;
import com.jladder.hub.WebHub;
import com.jladder.lang.*;
import com.jladder.logger.LogFoRequest;
import com.jladder.net.HttpHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ProxyService {
//    private static TimedCache<String, AjaxResult> TimedCache = CacheUtil.newTimedCache(3600);
    /// <summary>
    /// 获取交互权限
    /// </summary>
    /// <returns></returns>
    public static CrossAccessAuthInfo GetAccessAuthInfo()
    {
//        return (CrossAccessAuthInfo)WebScope.GetValue("proxy_auth_info");
        return null;
    }
    /// <summary>
    /// 代理服务执行方法
    /// </summary>
    /// <param name="config">代理配置信息</param>
    /// <param name="data">用户参数数据</param>
    /// <param name="env">调用环境代码</param>
    /// <param name="header">请求头信息</param>
    /// <param name="authinfo">认证信息</param>
    /// <returns></returns>
    public static AjaxResult Execute(ProxyConfig config, Record data, int env, Record header, CrossAccessAuthInfo authinfo, String follow)
    {
        //声明运行时实例对象
        ProxyRunning running = new ProxyRunning(data, authinfo, header, config, env, follow);
        try
        {
            //获取调用环境
            ProxyFunctionInfo funinfo = config.Get(env);
            if (funinfo == null) return End(new AjaxResult(500, "未找到相应调用环境[0292]"), running);
            if ("polymer".equals(funinfo.type ) && Strings.hasValue(follow))
            {
                return End(HandlePolymer((ProxyPolymer)funinfo.result, running), running);
            }
            if (authinfo != null)
            {
                //WebScope.setValue("proxy_auth_info", authinfo);
            }
            if (Maths.isBitEq1(config.raw.logoption,ProxyLogOption.Head) || Maths.isBitEq1(config.raw.logoption,ProxyLogOption.Error) || Maths.isBitEq1(config.raw.logoption,ProxyLogOption.Follow))
            {
                running.trace.put("head", Json.toJson(header));
            }
            //是否忽略请求日志
            if (!Maths.isBitEq1(config.raw.logoption,ProxyLogOption.Ignore))
            {
                running.requesting = new LogFoRequest();
                running.requesting.type=authinfo == null ? "proxy" : authinfo.mode;
                running.requesting.path=config.raw.name;
                running.requesting.header= Json.toJson(header.match((x, y) -> x.startsWith("_")));
                running.requesting.userinfo =(authinfo == null ? "" : authinfo.username + (Strings.isBlank(authinfo.withwho) ? "" : "|" + authinfo.withwho));
                running.requesting.withwho = (authinfo == null ? "" :authinfo.withwho);
                running.requesting.uuid = running.uuid;
                running.requesting.setReqeust(data);
                //Logs.Write(running.requesting, LogOption.Request);
            }

            if (config.CheckOption(ProxyLogOption.Info) || config.CheckOption(ProxyLogOption.Error) || config.CheckOption(ProxyLogOption.Follow))
            {
                running.trace.put("info", "版本号:" + config.raw.version + System.lineSeparator()
                        + "加密方式:" + config.raw.type + System.lineSeparator()
//                        + "关联请求ID:" + WebScope.getRequestMark() + System.lineSeparator()
                        + "客户端Ip:" + header.getString("ladder-client-ip") + System.lineSeparator()
//                        + "请求地址:" + WebContext.Current?.Request.Path.Value
                );
            }

            AjaxResult res = new AjaxResult(456); //返回结果
            if (data == null) data = new Record(); //解请求参数
            //region 用户参数映射列表,无用户级参数配置，直接把请求数据传递给调用级数据
            String ignoreLogKeys = "";
            if (!Rs.isBlank(config.mappings))
            {
                config.mappings.sort(Comparator.comparing(x -> x.level));

                for (ProxyMapping mapping : config.mappings)
                {
                    String key = data.haveKey(mapping.paramname);
                    if (Strings.isBlank(key) && "0".equals(mapping.ignore)) return End(new AjaxResult(400, "参数不足"+mapping.paramname), running);
                    //移除大文本的日志记录
                    if (Regex.isMatch(mapping.datatype, "(file)|(text)")) ignoreLogKeys += key + ",";
                    Object v = Strings.hasValue(key) ? data.get(key) : mapping.dvalue;
                    if (("1".equals(mapping.ignore) || "启用".equals(mapping.ignore)) && (v == null || Strings.isBlank(v.toString())))
                    {
                        if (Strings.hasValue(mapping.valid)) return End(new AjaxResult(444, "参数[" + mapping.paramname + "]未通过验证[0125]"), running);
                        continue;
                    }
                    //默认日期时间
                    if (v instanceof String && Regex.isMatch(mapping.datatype, "(date)|(time)") && Regex.isMatch(v.toString(), "^\\s*\\$"))
                    {
                        if (Regex.isMatch(mapping.datatype, "^\\s*date\\s*$"))
                        {
                            if (Regex.isMatch(v.toString(), "^\\$date$"))
                                v = Times.getDate();
                            if (Regex.isMatch(v.toString(), "^\\$((datetime)|(now))$"))
                                v = Times.getNow();
                            if (Regex.isMatch(v.toString(), "^\\d*$"))
                            {
                                v = Times.sD(Times.D(Long.parseLong(v.toString())));
                            }
                        }
                        else
                        {
                            if (Regex.isMatch(v.toString(), "^\\$((datetime)|(now))$"))
                                v = Times.getNow();
                            if (Regex.isMatch(v.toString(), "^\\d*$"))
                            {
                                v = Times.sDT(Times.D(Long.parseLong(v.toString())));;
                            }
                        }
                    }
                    //验证器
                    if (Strings.hasValue(mapping.valid))
                    {
                        if (v == null) return End(new AjaxResult(444, "参数[" + mapping.paramname + "]未通过验证[0155]"), running);
                        Receipt check = Strings.check(v.toString(),mapping.valid);
                        if (!check.isSuccess())
                            return End(new AjaxResult(444, "参数[" + mapping.paramname + "]" + check.message), running);
                    }
                    //格式化
                    if (Strings.hasValue(mapping.format))
                    {

                        v = Strings.mapping(Strings.mapping(Strings.mapping(mapping.format),"value", v.toString()),data);
                    }
                    //映射参数
                    String[] ps = { mapping.paramname };
                    if (!Strings.isBlank(mapping.mapping))
                    {
                        String[] mmss = mapping.mapping.split("\\|",-1);
                        Record pd = running.paramdata;
                        for(String m : mmss){
                            ps = m.split(m.contains("/") ? "/" : "\\.");
                            if (ps.length == 1)
                            {
                                if (pd.get(ps[0]) instanceof Record && Strings.isJson(v.toString(),1))
                                    pd.get(ps[0],Record.class).merge(Record.parse(v));
                                else pd.put(ps[0], v);
                            }
                            else
                            {
                                Record record = Record.parse(pd.get(ps[0]));
                                if(record==null)record =  new Record();
                                pd.put(ps[0], record);
                                for (int i = 1; i < ps.length - 1; i++)
                                {
                                    Record rec =  Record.parse(record.get(ps[i]));
                                    if(rec==null)rec =  new Record();
                                    record.put(ps[i], rec);
                                    record = rec;
                                }
                                record.put(ps[ps.length - 1], v);
                            }
                        }
                    }
                    else running.paramdata.put(ps[0], v);
                }
            }
            else running.paramdata = Record.parse(data);
            //记录请求参数
            if (config.CheckOption(ProxyLogOption.Request) || config.CheckOption(ProxyLogOption.Error) || config.CheckOption(ProxyLogOption.Follow))
            {
                running.trace.put("request", Json.toJson(data.filter(ignoreLogKeys)));
            }
            //endregion
            ///region填充参数
            ignoreLogKeys = "";
            if (!Rs.isBlank(config.params))
            {
                Record newparamData = new Record();
                config.params.sort(Comparator.comparing(x->x.level));
                //调用环境参数列表
                for (ProxyParam param : config.params)
                {
                    String key = running.paramdata.haveKey(param.paramname);
                    String stringvalue = (Strings.isBlank(key) ? param.dvalue : running.paramdata.getString(key));
                    if ("1".equals(param.required) && Strings.isBlank(stringvalue)){
                        return End(new AjaxResult(444).setMessage("[" + param.paramname + "]未被填充[0211]"), running);
                    }
                    //参数可空,且默认值为空，用户传值也为空
                    if("0".equals(param.required) && Strings.isBlank(key) && Strings.isBlank(stringvalue))continue;
                    //参数可忽略
                    if("2".equals(param.required) && Strings.isBlank(key))continue;
                    //默认日期时间
                    if (Strings.hasValue(stringvalue) && Regex.isMatch(param.datatype, "(date)|(time)") && Regex.isMatch(stringvalue, "^\\s*\\$")) {
                        if (Regex.isMatch(param.datatype, "^\\s*date\\s*$"))
                        {
                            if (Regex.isMatch(stringvalue, "^\\$date$"))
                                stringvalue = Times.getDate();
                            if (Regex.isMatch(stringvalue, "^\\$((datetime)|(now))$"))
                                stringvalue = Times.getNow();
                            if (Regex.isMatch(stringvalue, "^\\d*$"))
                            {
                                stringvalue = Times.sD(Times.D(Long.parseLong(stringvalue)));
                            }
                        }
                        else
                        {
                            if (Regex.isMatch(stringvalue, "^\\$((datetime)|(now))$")) stringvalue = Times.getNow();
                            if (Regex.isMatch(stringvalue, "^\\d*$")) stringvalue = Times.sDT(Times.D(Long.parseLong(stringvalue)));
                        }
                    }
                    if (Strings.hasValue(param.valid)){
                        Receipt check = Strings.check(stringvalue,param.valid);
                        if (!check.isSuccess())return End(new AjaxResult(444, "映射参数[" + param.paramname + "]" + check.message), running);
                    }
                    switch (param.datatype.toLowerCase()){
                        case "int":
                            if (Strings.isBlank(stringvalue)){
                                newparamData.put(param.paramname, 0);
                            }
                            else{
                                if (!Strings.isNumber(stringvalue)) return End(new AjaxResult(444, "映射参数[" + param.paramname + "]不是整数类型"), running);
                                newparamData.put(param.paramname, Convert.toInt(stringvalue));
                            }
                            break;
                        case "string":
                        case "json":
                        case "string&json":
                            newparamData.put(param.paramname, stringvalue);
                            break;
                        case "text":
                            newparamData.put(param.paramname, stringvalue);
                            ignoreLogKeys += key + ",";
                            break;
                        case "file":
                            newparamData.put(param.paramname, Strings.isBlank(key) ? null : running.paramdata.get(key));
                            ignoreLogKeys += key + ",";
                            break;
                        case "date":
                            if (!Strings.isDate(stringvalue))
                                return End(new AjaxResult(444, "映射参数[" + param.paramname + "]不是日期类型"), running);
                            newparamData.put(param.paramname, Convert.toDate(stringvalue));
                            break;
                        case "datetime":
                            if (!Strings.isDateTime(stringvalue)) return End(new AjaxResult(444, "映射参数[" + param.paramname + "]不是日期时间类型"), running);
                            newparamData.put(param.paramname, Convert.toDate(stringvalue));
                            break;
                        case "number":
                        case "decimal":
                            if (Strings.isBlank(stringvalue)){
                                newparamData.put(param.paramname, 0);
                            }
                            else{
                                if (!Strings.isDecimal(stringvalue)) return End(new AjaxResult(444, "映射参数[" + param.paramname + "]不是数字类型"), running);
                                newparamData.put(param.paramname, Convert.toDouble(stringvalue));
                            }
                            break;
                        case "bool":
                            newparamData.put(param.paramname, Convert.toBool(stringvalue));
                            break;
                        default:
                            newparamData.put(param.paramname, Strings.isBlank(key) ? param.dvalue : running.paramdata.get(key));
                            break;
                    }
                }
                running.paramdata = newparamData.merge(running.paramdata.filter(Strings.ArrayToString(Collections.select(config.params,x-> x.paramname),",","")));
            }
            //endregion

            if (config.CheckOption(ProxyLogOption.Call))
            {
                Record filterData = running.paramdata.filter(ignoreLogKeys);
                running.trace.put("call", Json.toJson(filterData) + System.lineSeparator() + HttpHelper.toFormData(filterData,false));
            }

            if (config.CheckOption(ProxyLogOption.Idempotency))
            {
                Record filterData = running.paramdata.filter(ignoreLogKeys);
                running.hashcode = HttpHelper.toFormData(filterData,false);
                if (Strings.hasValue(running.hashcode) && authinfo != null)
                {
                    running.hashcode += authinfo.username + authinfo.withwho + authinfo.ip;
                    running.hashcode = Security.md5(running.hashcode);
                }
                if (Strings.hasValue(running.hashcode))
                {

                    AjaxResult old = (AjaxResult)DataHub.WorkCache.getModuleCache(running.hashcode,"_Proxy_Idempotency_");
                    if (old != null)
                    {
                        running.hashstatus = true;
                        return End(old, running);
                    }
                    else
                    {
                        DataHub.WorkCache.addModuleCache(running.hashcode, new AjaxResult(AjaxResultCode.ProxyMethodRefused).setMessage("禁止重复请求[0325]"),"_Proxy_Idempotency_",5);
                    }
                }
            }

            //替换参数
            if(funinfo.param!=null)funinfo.param.forEach((k,v) -> running.paramdata.put(k, v));
            //配置调用环境类型直接返回结果
            if (Strings.isBlank(funinfo.type)) return End(res.Ok().setData(funinfo), running);
                //region 转换调用环境的数据，包括Data和Head

            ReStruct<Record, Record> tResult = WebHub.CrossAccess.DoTransition(running.paramdata, config, env, header);
            if (!tResult.Success) return End(res.set(506, "不具备调用执行条件[0300]"), running);
            running.paramdata = tResult.ResultA;
            header = tResult.ResultB;
                //endregion

            //处理过程事件
            AjaxResult callResult = WebHub.CrossAccess.OnCall(config, running.paramdata, header, authinfo);
            if (callResult != null) return End(callResult, running);

                //region 处理条件分发
            if ("route".equals(funinfo.type.toLowerCase()))
            {
                boolean ismatch = false;
                List<ProxyRouteFunctionInfo> routes = (List<ProxyRouteFunctionInfo>)funinfo.result ;
                if (routes == null || routes.size() < 1) return End(new AjaxResult(840, "分发路由未匹配"), running);
                ProxyFunctionInfo def = null;
                for (ProxyRouteFunctionInfo ri : routes)
                {
                    if ("default".equals(ri.condition))
                    {
                        def = ri.funinfo;
                        continue;
                    }
                    boolean ret = Script.eval(Strings.mapping(Strings.mapping(ri.condition,running.paramdata, false),header),Boolean.class);
                    if (ret)
                    {
                        funinfo = ri.funinfo;
                        ismatch = true;
                        break;
                    }
                }

                if (!ismatch)
                {
                    if (def == null) return End(new AjaxResult(841, "分发路由未匹配"), running);
                    else funinfo = def;
                }
            }

                //endregion

                //region 负载平衡
            if ("balance".equals(funinfo.type.toLowerCase()))
            {
                List<ProxyRouteFunctionInfo> routes = (List<ProxyRouteFunctionInfo>)funinfo.result;
                if (routes == null || routes.size() < 1) return End(new AjaxResult(842, "负载路由未匹配"), running);
                try
                {
                    funinfo = routes.get(config.GetWeightIndex()).funinfo;
                }
                catch (Exception e)
                {
                    //Logs.Write(new LogForError(e.Message) { StackTrace = e.StackTrace, Module = config.Name, Type = "Proxy" }, LogOption.Error);
                    return End(new AjaxResult(843, "负载路由计算异常"), running);
                }
            }
            ///endregion


            switch (funinfo.type.toLowerCase())
            {
                case "polymer":

                    return End(HandlePolymer((ProxyPolymer)funinfo.result, running), running);
                case "job":



                    break;
                default:
                    return End(Call(funinfo, running), running);
            }
            return new AjaxResult(400, "未进行有效处理[0390]");
        }
        catch (Exception e)
        {
            //Logs.Write(new LogForError(e.Message) { StackTrace = e.StackTrace, Module = config.Name, Type = "Proxy" }, LogOption.Error);
            AjaxResult result = new AjaxResult(500, e.toString()).setXData(Core.getStackTrace(e));
            //WebHub.DoRequest.ProxyError(config, result);
            return result;
        }
        finally
        {
            StringBuffer logText = new StringBuffer();
            running.trace.forEach((k,v) ->{
                logText.append("--" + k+ "_start--" + System.lineSeparator() + v + System.lineSeparator() + "--" + k + "_end--" + System.lineSeparator());
            });
            //if (logText.length() > 0) Logs.WriteLog("----" + running.Uuid + "----" + System.lineSeparator() + logText + "----" + running.Uuid + "----", "Proxy/" + config.Name);
        }
    }


    /// <summary>
    /// 收尾处理
    /// </summary>
    /// <param name="result">处理结果</param>
    /// <param name="running">运行时信息</param>
    /// <returns></returns>
    private static AjaxResult End(AjaxResult result, ProxyRunning running)
    {
        if (result.success)
        {
            if (!running.config.CheckOption(ProxyLogOption.Request) && !running.config.CheckOption(ProxyLogOption.Follow)) running.trace.delete("request");
            if (!running.config.CheckOption(ProxyLogOption.Head) && !running.config.CheckOption(ProxyLogOption.Follow)) running.trace.delete("head");
            if (!running.config.CheckOption(ProxyLogOption.Call) && !running.config.CheckOption(ProxyLogOption.Follow)) running.trace.delete("call");
            if (!running.config.CheckOption(ProxyLogOption.Info) && !running.config.CheckOption(ProxyLogOption.Follow)) running.trace.delete("info");
            if (running.config.CheckOption(ProxyLogOption.Result) && !running.config.CheckOption(ProxyLogOption.Follow))
            {
                if (running.requesting != null) running.requesting.result = Json.toJson(result);
                running.trace.put("result", Json.toJson(result));
            }
        }
        else
        {
            if (running.requesting != null) running.requesting.result = Json.toJson(result);
            running.trace.put("result", Json.toJson(result));
        }
        if (running.requesting != null)
        {
            running.requesting.setEnd();
            //Logs.Write(running.Requesting, LogOption.Request);
        }
        if (running.config.CheckOption(ProxyLogOption.Idempotency) && Strings.hasValue(running.hashcode) && !running.hashstatus)
        {
            List<String> limits = running.config.GetRuleValues("Idempotency", running.authinfo);
            if (Rs.isBlank(limits)) DataHub.WorkCache.addModuleCache(running.hashcode, result, "_Proxy_Idempotency_",5);
            else
            {
                //Arrays.stream(limits)
                Optional<String> max =limits.stream().max(Comparator.comparing(x->Integer.parseInt(x)));
                int maxint = Integer.parseInt(max.get());
                if (maxint > 0) DataHub.WorkCache.addModuleCache(running.hashcode, result, "_Proxy_Idempotency_",maxint);
            }
        }
        result = WebHub.CrossAccess.OnResult(result, running.config, running.paramdata, running.header, running.uuid, running.authinfo);
        try
        {
            //控制节点路径
            List<ProxyRule> rule_list = running.config.GetRules(ProxyRuleOption.NodePath, running.authinfo);

            ProxyRule dore = rule_list==null ? null: rule_list.get(rule_list.size() - 1);
            if (dore != null)
            {
                try
                {
                    result.data = Record.parse(result.data).find(dore.nodepath);
                }
                catch (Exception e)
                {
                    result.set(AjaxResultCode.DataTransaction.getCode(), "数据转换错误[137]");
                }
            }
            //切割结构体
            List<String> cut = running.config.GetRuleValues(ProxyRuleOption.CutResult, running.authinfo);
            if (!Rs.isBlank(cut) && Collections.any(cut,x -> "1".equals(x))) result.setCut(true);
            //保存数据缓存
            rule_list = running.config.GetRules(ProxyRuleOption.Cache, running.authinfo);
            ProxyRule cacherule = (rule_list==null?null:rule_list.get(rule_list.size() - 1));
            if (cacherule != null)
            {
                String  cachek = null;//缓存键名
                switch (cacherule.nodepath)
                {
                    case "tagwho":
                        cachek = running.config.name + running.authinfo.withwho;
                        break;
                    case "token":
                        cachek = running.authinfo.token;
                        break;
                    case "uuid":
                        cachek = running.uuid;
                        break;
                }
                if (Strings.hasValue(cachek)) DataHub.WorkCache.addModuleCache(cachek, new Record("Result",result).put("Request",running.data).put("AuthInfo",running.authinfo), "Proxy_Result_Cache", Integer.parseInt(cacherule.value));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
//            result.Set((int)AjaxResultCode.DataTransaction, "数据转换错误[146]");
//            Logs.Write(new LogForError(e.Message, "ProxyEnd").SetStackTrace(e.StackTrace).SetModule("ProxyService"), LogOption.Error);
        }
        running.stopWatch.stop();
        Record rel = new Record("uuid", running.uuid).put("name", running.config.name);
        if (running.hashstatus) rel.put("hashcode", running.hashcode);
        return result.setDuration(running.stopWatch).setRel(rel.toString());
    }
    /// <summary>
    /// 调用执行
    /// </summary>
    /// <param name="funInfo">调用信息</param>
    /// <param name="running">运行时信息</param>
    /// <returns></returns>
    private static AjaxResult Call(ProxyFunctionInfo funInfo, ProxyRunning running)
    {
        ///region 进行环境方法调用
        switch (funInfo.type.toLowerCase())
        {
            ///region 类库方法
            case "lib":
                AjaxResult ret; //处理的返回结果
                Object rev = null;
                switch (funInfo.functionname.toLowerCase())
            {
                case "getbean":
                    if (Strings.hasValue(running.paramdata.getString("conn")))
                        WebScope.SetDataModelConn(running.paramdata.getString("tableName", true),
                                running.paramdata.getString("conn"));
                    ret = QueryAction.getBean(running.paramdata.getString("tableName", true),
                            running.paramdata.getString("condition", true),
                            running.paramdata.getString("columns", true),
                            running.paramdata.getString("param", true),
                            running.paramdata.getString("rel", true)
                    );
                    return ret;
                case "getdata":
                    if (Strings.hasValue(running.paramdata.getString("conn")))
                        WebScope.SetDataModelConn(running.paramdata.getString("tableName", true),
                                running.paramdata.getString("conn"));
                    ret = QueryAction.getData(
                            running.paramdata.getString("tableName", true),
                            running.paramdata.getString("condition", true),
                            running.paramdata.getString("columns", true),
                            running.paramdata.getString("param", true),
                            running.paramdata.getString("rel", true)
                    );
                    return ret;
                case "getvalues":
                    if (Strings.hasValue(running.paramdata.getString("conn")))
                        WebScope.SetDataModelConn(running.paramdata.getString("tableName", true),
                                running.paramdata.getString("conn"));
                    rev = QueryAction.getValues(
                            running.paramdata.getString("tableName", true),
                            running.paramdata.getString("columns", true),
                            running.paramdata.getString("condition", true),
                            running.paramdata.getString("param", true)
                            );
                    return rev == null
                            ? new AjaxResult(new AjaxResult(404))
                            : new AjaxResult(new AjaxResult(rev));
                case "getvalue":
                    if (Strings.hasValue(running.paramdata.getString("conn")))
                        WebScope.SetDataModelConn(running.paramdata.getString("tableName", true),
                                running.paramdata.getString("conn"));
                    ret = QueryAction.getValue(
                            running.paramdata.getString("tableName", true),
                            running.paramdata.getString("columns", true),
                            running.paramdata.getString("condition", true),
                            running.paramdata.getString("param", true), ""
                    );
                    return ret;
                case "querydata":
                    if (Strings.hasValue(running.paramdata.getString("conn")))
                        WebScope.SetDataModelConn(running.paramdata.getString("tableName", true),
                                running.paramdata.getString("conn"));
                    ret = QueryAction.queryData(
                            running.paramdata.getString("tableName", true),
                            running.paramdata.getString("condition", true),
                            running.paramdata.getInt("psize", true),
                            running.paramdata.getString("param", true),
                            running.paramdata.getString("columns", true)
                    );
                    return ret;
                case "savebean":
                    if (Strings.hasValue(running.paramdata.getString("conn")))
                        WebScope.SetDataModelConn(running.paramdata.getString("tableName", true),running.paramdata.getString("conn"));

                    ret = SaveAction.saveBean(
                            running.paramdata.getString("tableName",true),
                            running.paramdata.getString("bean", true),
                            running.paramdata.getString("condition", true),
                            running.paramdata.getInt("option", true),
                            running.paramdata.getString("rel", true),true);
                    return ret;
                case "getcount":
                    if (Strings.hasValue(running.paramdata.getString("conn")))
                        WebScope.SetDataModelConn(running.paramdata.getString("tableName", true),
                                running.paramdata.getString("conn"));
                    long count = QueryAction.getCount(running.paramdata.getString("tableName", true),
                            running.paramdata.getString("condition", true), running.paramdata.getString("param", true));
                    ret = new AjaxResult().setData(count);
                    return ret;
                case "getpagedata":
                    if (Strings.hasValue(running.paramdata.getString("conn")))
                        WebScope.SetDataModelConn(running.paramdata.getString("tableName", true),
                                running.paramdata.getString("conn"));
                    ret = QueryAction.getPageData(
                            running.paramdata.getString("tableName", true),
                            running.paramdata.getString("condition", true),
                            running.paramdata.getString("columns", true),
                            running.paramdata.getString("param", true),
                            running.paramdata.getInt("start"),
                            running.paramdata.getInt("pageNumber"),
                            running.paramdata.getInt("psize"),
                            running.paramdata.getInt("recordCount", true)
                    );
                    return ret;
                case "savebeans":
                    if (Strings.hasValue(running.paramdata.getString("conn")))
                        WebScope.setConn(running.paramdata.getString("conn"));
                    ret = SaveAction.saveBeans(running.paramdata.getString("settings,beans"),true);
                    return ret;
                case "querys":
                    if (Strings.hasValue(running.paramdata.getString("conn")))
                        WebScope.setConn(running.paramdata.getString("conn"));
                    ret = QueryAction.querys(running.paramdata.getString("settings,beans"));
                    return ret;
            }
            break;
            case "sql":
                if (Strings.isBlank(funInfo.code)) return new AjaxResult(500, "执行命令不能空");
                Dao dao = new Dao(funInfo.path);
                try
                {
                    Object dat = null;
                    switch (funInfo.functionname.toLowerCase())
                    {
                        case "getvalue":
                            dat = dao.getValue(new SqlText(Strings.mapping(HttpHelper.decode(funInfo.code),running.paramdata)),String.class);
                            break;
                        case "getvalues":
                            //dat = dao.getValues(new SqlText(Strings.mapping(HttpHelper.decode(funInfo.code),running.paramdata)));
                            break;
                        case "getdata":
                            dat = dao.query(new SqlText(Strings.mapping(HttpHelper.decode(funInfo.code),running.paramdata)));
                            break;
                        case "getbean":
                            dat = dao.fetch(new SqlText(Strings.mapping(HttpHelper.decode(funInfo.code),running.paramdata)));
                            break;
                        case "exec":
                            dat = dao.exec(new SqlText(Strings.mapping(HttpHelper.decode(funInfo.code),running.paramdata)));
                            break;
                    }
                    return Strings.isBlank(dao.getErrorMessage()) ? new AjaxResult().setData(dat) : new AjaxResult(500, "执行错误").setXData(dao.getErrorMessage());
                }
                catch (Exception e)
                {
                    return new AjaxResult(500, "执行错误").setXData(e.getMessage());
                }
                finally
                {
                    dao.close();
                }
                ///endregion
                ///region 内部应用环境

            case "controller":
                Receipt handleresult = Refs.invoke(funInfo.path, running.paramdata,null);
                return handleresult.result ? new AjaxResult(handleresult.data) : new AjaxResult(500).setMessage(handleresult.message);
            case "script":

                break;
            //内部程序
            case "service":
            case "services":
            {
                Receipt result = Refs.invoke(funInfo.path, running.paramdata, null);
                return result.result ? new AjaxResult().setData(result.data) : new AjaxResult(500, result.message);
            }
            case "program":
            {
                Receipt result = Refs.invoke(funInfo.path, running.paramdata, funInfo.functionname);
                return result.result ? new AjaxResult().setData(result.data) : new AjaxResult(500, result.message);
                //throw Core.makeThrow("未实现[0692]","PorxyService");
//                Receipt result = null;
//                var ext = Path.GetExtension(funInfo.Path ?? "").ToLower();
//                result = (ext == ".dll" ? Refs.Invoke(funInfo.FunctionName, running.ParamData, null, funInfo.Path) : Refs.Invoke(funInfo.Path, running.ParamData, funInfo.FunctionName));
//                return result.Result ? new AjaxResult().SetData(result.Data) : new AjaxResult(500, result.Message);
//                break;
            }
            case "replace":
            {
                ProxyConfig rconigs = getProxyConfig(funInfo.functionname, funInfo.uri);
                return rconigs == null ? new AjaxResult(506, "未找到相应调用环境") : ProxyService.Execute(rconigs, running.paramdata, running.envcode, running.header, running.authinfo,null);
            }
                ///endregion
                ///region 网络请求

            case "httpjson":
            case "postjson":
            {
                Receipt<String> rpath = WebHub.CrossAccess.DoPath(funInfo.path, running.config, running.paramdata, running.header);
                if (!rpath.result) return new AjaxResult(501, rpath.message);
                Receipt<String> re = HttpHelper.RequestByJson(rpath.data, running.paramdata.toString(), running.header);
                if (!re.isSuccess() && re.message.contains("基础连接已经关闭"))
                {
                    re = HttpHelper.RequestByJson(rpath.data, running.paramdata.toString(), running.header);
                }

                if (!re.isSuccess() && re.message.contains("(502) 错误的网关"))
                {
                    re = HttpHelper.RequestByJson(rpath.data, running.paramdata.toString(), running.header);
                }

                if (!re.isSuccess())
                {

//                    Logs.WriteLog($"错误消息:{re.Message}\n数据备注:{re.Data}", "proxy_error_httpjson");
                    return new AjaxResult(505, re.message+"[0717]").setData(re.data);
                }
                else
                {
                    String retText = re.data;
                    boolean isJson = Strings.isJson(retText);
                    if (isJson)
                    {
                        //WebReply.SetContentTypeToJson();
                        return Strings.hasValue(funInfo.uri) ? new AjaxResult().setData(Record.parse(retText).find(funInfo.uri)).setDataType("json") : new AjaxResult().setData(Json.toObject(retText)).setDataType("json");
                    }
                    else
                    {
                        return new AjaxResult().setData(retText).setDataType("html");
                    }
                }
            }
            case "httpget":
            case "http":
            case "get":
            case "web":
            {
                Receipt<String> rpath = WebHub.CrossAccess.DoPath(funInfo.path, running.config, running.paramdata, running.header);
                if (!rpath.result) return new AjaxResult(501, rpath.message);
                Receipt<String> re = HttpHelper.request(rpath.data, running.paramdata, "get", running.header);
                if (!re.isSuccess() && re.message.contains("基础连接已经关闭"))
                {
                    re = HttpHelper.request(rpath.data, running.paramdata, "get",running.header);
                }
                if (!re.isSuccess() && re.message.contains("(502) 错误的网关"))
                {
                    re = HttpHelper.request(rpath.data, running.paramdata, "get", running.header);
                }
                if (!re.isSuccess())
                {

//                    Logs.WriteLog($"错误消息:{re.Message}\n数据备注:{re.Data}", "proxy_error_httpget");
                    return new AjaxResult(505, re.message+"[0754]").setData(re.data);
                }
                else
                {
                    String retText = re.data;
                    boolean isJson = Strings.isJson(retText);
                    if (isJson)
                    {
//                        WebReply.SetContentTypeToJson();
                        return Strings.hasValue(funInfo.uri) ? new AjaxResult().setData(Record.parse(retText).find(funInfo.uri)).setDataType("json") : new AjaxResult().setData(Json.toObject(retText)).setDataType("json");
                    }
                    else
                    {
                        return new AjaxResult().setData(retText).setDataType("html");
                    }
                }
            }
            case "httppost":
            case "post":
            case "webpost":
            {
                Receipt<String> rpath = WebHub.CrossAccess.DoPath(funInfo.path, running.config, running.paramdata, running.header);
                if (!rpath.result) return new AjaxResult(501, rpath.message);
                Receipt<String> re = HttpHelper.request(rpath.data, running.paramdata, "post", running.header);
                if (!re.isSuccess() && re.message.contains("基础连接已经关闭"))
                {
                    re = HttpHelper.request(rpath.data, running.paramdata, "post", running.header);
                }
                if (!re.isSuccess() && re.message.contains("(502) 错误的网关"))
                {
                    re = HttpHelper.request(rpath.data, running.paramdata, "post", running.header);
                }
                if (!re.isSuccess())
                {

//                    Logs.WriteLog($"错误消息:{re.Message}\n数据备注:{re.Data}", "proxy_error_httppost");

                    return new AjaxResult(505, re.message+"[0791]").setData(re.data);
                }
                else
                {
                    String  retText = re.data;
                    boolean isJson = Strings.isJson(retText);
                    if (isJson)
                    {
//                        WebReply.SetContentTypeToJson();
                        return Strings.hasValue(funInfo.uri) ? new AjaxResult().setData(Record.parse(retText).find(funInfo.uri)).setDataType("json") : new AjaxResult().setData(Json.toObject(retText)).setDataType("json");
                    }
                    else
                    {
                        return new AjaxResult().setData(retText).setDataType("html");
                    }
                }
            }
            case "upload":
            case "httpupload":
            {
                throw Core.makeThrow("未实现[0822]","ProxyService");
//                var rpath = WebHub.CrossAccess.DoPath(funInfo.Path, running.Config, running.ParamData, running.Header);
//                if (!rpath.Success) return new AjaxResult(501, rpath.Message);
//                var files = new List<UploadFile>();
//                var ps = new Dictionary<String, Object>();
//                foreach (var kv in running.ParamData)
//                {
//                    if (kv.Value is UploadFile)
//                    {
//                        var uploadfile = kv.Value as UploadFile;
//                        if (uploadfile == null || uploadfile.Data.IsBlank()) continue;
//                        files.Add(uploadfile);
//                    }
//                            else ps.Put(kv.Key, kv.Value);
//                }
//
//                var re = HttpHelper.Upload(rpath.Result, files, ps);
//                if (!re.IsSuccess() && re.Message.Contains("基础连接已经关闭"))
//                {
//                    re = HttpHelper.Upload(rpath.Result, files, ps);
//                }
//                if (!re.IsSuccess() && re.Message.Contains("(502) 错误的网关"))
//                {
//                    re = HttpHelper.Upload(rpath.Result, files, ps);
//                }
//                if (!re.IsSuccess())
//                {
//
//                    Logs.WriteLog($"错误消息:{re.Message}\n数据备注:{re.Data}", "proxy_error_upload");
//
//                    return new AjaxResult(505, re.Message);
//                }
//                else
//                {
//
//                    StringBuilder uploadlog = new StringBuilder($"上传文件个数:" + files.Count + System.lineSeparator());
//                    files.ForEach(fileinfo =>
//                            {
//                    if (fileinfo == null) return;
//                    var savefilename = $"{Times.TimeStamp}_" + fileinfo.FileName;
//                    uploadlog.Append($"文件名:{fileinfo.FileName};表单名:{fileinfo.FormName};文件码:{fileinfo.FileCode};Md5:{fileinfo.Md5}" + System.lineSeparator());
//                    uploadlog.Append($"文件保存到:{savefilename}" + System.lineSeparator());
//                    fileinfo.SaveAs(Logs.GetCatalogDir("Proxy/" + running.config.name + "/Files/") + savefilename);
//                            });
//                    running.Trace.Put("upload", uploadlog);
//                    var retText = re.Data.ToString();
//                    return (retText.IsJson() ? new AjaxResult().SetData(Json.ToObject<object>(retText)).SetDataType("html") : new AjaxResult().SetData(retText).SetDataType("html"));
//                }

            }

                //endregion
                //region 静态结果

            case "result":

                return new AjaxResult().setData(funInfo.result);
            case "random":
            {
                if (funInfo.result == null) return new AjaxResult(404, "无数据结果");
                if (funInfo.result instanceof List)
                {
                    List al = (List)funInfo.result;
                    return al.size() == 0 ? new AjaxResult(404, "无数据结果") : new AjaxResult().setData(al.get(R.random(0, al.size())));
                }
//                if (funInfo.Result is List<object>)
//                {
//                    var al = (List<object>)funInfo.Result;
//                    return al.IsBlank() ? new AjaxResult(404, "无数据结果") : new AjaxResult().SetData(al[R.Random(0, al.Count)]);
//                }
                if (funInfo.result instanceof String)
                {
                    List al = Json.toObject(funInfo.result.toString(),List.class);
                    return al.size()==0 ? new AjaxResult(404, "无数据结果") : new AjaxResult().setData(al.get(R.random(0, al.size())));
                }
                String jsontext = Json.toJson(funInfo.result);
                if (jsontext.startsWith("{") && jsontext.endsWith("}"))
                {
                    return new AjaxResult().setData(funInfo.result);
                }
                else if (jsontext.startsWith("[") && jsontext.endsWith("]"))
                {
                    List al = Json.toObject(jsontext, List.class);
                    return al.size()==0? new AjaxResult(404, "无数据结果") : new AjaxResult().setData(al.get(R.random(0, al.size())));
                }
                return new AjaxResult(404, "无数据结果");
            }
            default:
                return new AjaxResult().setData(funInfo);
                   //endregion
        }
            //endregion
        return new AjaxResult(400, "未进行有效逻辑处理");
    }




    /// <summary>
    /// 处理多组聚合
    /// </summary>
    /// <param name="polymer">多组聚合对象</param>
    /// <param name="running">运行时信息</param>
    /// <returns></returns>
    private static AjaxResult HandlePolymer(ProxyPolymer polymer, ProxyRunning running)
    {
        //redis数据库的序号
        throw Core.makeThrow("未实现");
        /*
        var redisindex = Configs.GetInt("ProxyFollow_Redis_Index");
        if (running.Follow.IsBlank())
        {
            //任务集合
            var tasks = new List<Task>();
            //首结果和首代码
            AjaxResult firsResult = null;
            string firstCode = null;
            //任务信息字典
            var taskDic = new Dictionary<string, string>();
            //结果
            var ret = new ProxyPolymerResult();
            //处理器字典
            ConcurrentDictionary<string, ProxyPolymerTaskInfo> TaskInfo = new ConcurrentDictionary<string, ProxyPolymerTaskInfo>();
            //线程锁事件
            AutoResetEvent _resetEvent = new AutoResetEvent(false);
            //任务序号
            var index = 0;
            switch (polymer.SourceType)
            {
                //接口节选类型
                case ProxyPolymer.Extract:
                {
                    if (polymer.Tags.IsBlank()) return new AjaxResult(500, "Tags未配置[0984]");
                    var tags = polymer.Tags.Split(",");
                    if (tags.Length < 2)
                    {
                        var taskCode = "T" + (index++);
                        var task = new ProxyPolymerTaskInfo(taskCode) { Explain = polymer.Tags, Sign = polymer.Tags };
                        TaskInfo.TryAdd(taskCode, task);
                        var rconigs = GetProxyConfig(tags.FirstOrDefault(), "");
                        firsResult = Execute(rconigs, running.ParamData, running.EnvCode, running.Header, running.AuthInfo);
                        firstCode = Core.GenUuid();
                        taskDic.Put(Core.GenUuid(), polymer.Tags);
                        PluginHub.Redis.AddCache("P_" + running.Uuid + "_" + taskCode, firsResult, polymer.Remain, redisindex);
                        break;
                    }
                    else
                    {

                        tags.ForEach(x =>
                                {
                                        var taskCode = "T" + (index++);
                        var task = new ProxyPolymerTaskInfo(taskCode) { Explain =x, Sign = x };
                        TaskInfo.TryAdd(taskCode, task);
                        tasks.Add(Task.Factory.StartNew(() =>
                                {
                                        var rconigs = GetProxyConfig(x, "");
                        var result = Execute(rconigs, running.ParamData, running.EnvCode, running.Header, running.AuthInfo);
                        firsResult = result;
                        firstCode = taskCode;
                        task.Result = result;
                        _resetEvent.Set();
                        if (polymer.Asyn != 1) return;
                        PluginHub.Redis.AddCache("P_" + running.Uuid + "_" + taskCode, result, polymer.Remain,redisindex);
                                    }));
                                });
                    }
                }
                break;
                case ProxyPolymer.Event:
                {
                    var funs = WebHub.CrossAccess.FetchPolymFunctions(running.Config, polymer,running);
                    if (!funs.Result || funs.Data.IsBlank()) return new AjaxResult(500, funs.Message + "[1010]");
                    if (funs.Data.Count == 1)
                    {
                        var x = funs.Data.FirstOrDefault();
                        var taskCode = "T" + index++;
                        var task = new ProxyPolymerTaskInfo(taskCode) { Explain = x.Descr, Sign = x.Id };
                        TaskInfo.TryAdd(taskCode, task);
                        firsResult = Call(funs.Data.FirstOrDefault(), running);
                        firstCode = Core.GenUuid();
                        taskDic.Put(Core.GenUuid(), funs.Data.FirstOrDefault()?.Id);
                        PluginHub.Redis.AddCache("P_" + running.Uuid + "_" + taskCode, firsResult, polymer.Remain, redisindex);
                        break;
                    }
                    else
                    {
                        funs.Data.ForEach(x =>
                                {
                                        var taskCode = "T" + (index++);
                        var task = new ProxyPolymerTaskInfo(taskCode) { Explain = x.Descr ,Sign = x.Id};
                        TaskInfo.TryAdd(taskCode, task);
                        tasks.Add(Task.Factory.StartNew(() =>
                                {
                                        var result = Call(x, running);
                        firsResult = result;
                        firstCode = taskCode;
                        task.Result = result;
                        _resetEvent.Set();
                        if (polymer.Asyn != 1) return;
                        PluginHub.Redis.AddCache("P_" + running.Uuid + "_" + taskCode, result, polymer.Remain, redisindex);
                                    }));
                                });
                    }
                }
                break;
                case ProxyPolymer.RouteConfig:
                {
                    var routes = polymer.Routes.Where(x => x.Condition == "default" || Expression.Expression.Eval<bool>(x.Condition.Mapping(running.ParamData, false).Mapping(running.Header)));
                    if (routes.IsBlank()) return new AjaxResult(500, "路由配置不存在[1033]");
                    if (routes.Count() == 1)
                    {
                        var x = routes.FirstOrDefault();
                        var taskCode = "T" + (index++);
                        var task = new ProxyPolymerTaskInfo(taskCode) { Explain = x.Name, Sign = x.Name };
                        TaskInfo.TryAdd(taskCode, task);
                        firsResult = Call(routes.FirstOrDefault().FunInfo, running);
                        firstCode = Core.GenUuid();
                        taskDic.Put(Core.GenUuid(), routes.FirstOrDefault()?.Name);
                        PluginHub.Redis.AddCache("P_" + running.Uuid + "_" + taskCode, firsResult, polymer.Remain, redisindex);
                        break;
                    }
                    else
                    {
                        routes.ForEach(x =>
                                {
                                        var taskCode = "T" + (index++);
                        var task = new ProxyPolymerTaskInfo(taskCode) {Explain = x.Name, Sign = x.Name };
                        TaskInfo.TryAdd(taskCode, task);
                        tasks.Add(Task.Factory.StartNew(() =>
                                {
                                        var result = Call(x.FunInfo, running);
                        firsResult = result;
                        firstCode = taskCode;
                        task.Result = result;
                        _resetEvent.Set();
                        if (polymer.Asyn != 1) return;
                        PluginHub.Redis.AddCache("P_" + running.Uuid + "_" + taskCode, result, polymer.Remain, redisindex);
                                    }));
                                });
                    }
                }
                break;
            }
            //任务处理
            if (tasks.Count > 1)
            {
                //添加聚合回收运算任务
                tasks.Add(Task.Factory.StartNew(() =>
                        {
                while (TaskInfo.Count(x => x.Value.Finish < 1) > 0)
                {
                    _resetEvent.WaitOne();
                    var record = new Record();
                    TaskInfo.ForEach(x =>
                            {
                    if (x.Value.Result != null)
                    {
                        x.Value.Code = x.Value.Result.statusCode;
                        record.Put(x.Key, x.Value.Result.data);
                    }
                            });
                    switch (polymer.Together)
                    {
                        case "SetMap":
                            ret.Result = record;
                            break;
                        case "Function":
                            ret.Result = WebHub.CrossAccess.DoPolymer(running.Config, polymer, record, running);
                            break;
                        case "Callback":
                            ret.Result = Execute(GetProxyConfig(polymer.ToTag), new Record("_uuid_",running.Uuid).Put("_taskinfo_",TaskInfo).Put("_data_",record).Merge(running.ParamData), running.EnvCode, running.Header, running.AuthInfo, running.Follow);
                            break;
                    }
                    TaskInfo.ForEach(x => x.Value.Finish = x.Value.Result != null ? 1 : 0);
                    //计算已完成任务
                    if (TaskInfo.All(x => x.Value.Finish == 1))
                    {
                        ret.Code = 200;
                        ret.Current = TaskInfo.Select(x => x.Key).ToString(",");
                    }
                            else
                    {
                        ret.Code = 2000;
                        ret.Current = TaskInfo.Where(x => x.Value.Finish == 1).Select(x => x.Key).ToString(",");
                    }
                    //不进行异步处理
                    if (polymer.Asyn != 1) continue;
                    PluginHub.Redis.AddCache("P_" + running.Uuid + "_running", ret, polymer.Remain, redisindex);
                    PluginHub.Redis.Publish(running.Uuid, ret.Current);
                    //_resetEvent.Reset();
                }

                    }));
                //是否进行异步
                switch (polymer.Asyn)
                {
                    case 1:
                        Task.WaitAny(tasks.ToArray());
                        ret.Code = 2000;
                        break;
                    case 0:
                        Task.WaitAll(tasks.ToArray());
                        ret.Code = 200;
                        return new AjaxResult().SetData(ret);
                    break;
                }
            }
            else
            {
                ret.Code = 200;
            }
            ret.Tasks = TaskInfo.Select(x => x.Key).ToString(",");
            ret.Current = firstCode;
            switch (polymer.Together)
            {
                case ProxyPolymer.SetMap:
                    ret.Result = new Record(firstCode, firsResult?.data);
                    break;
                case ProxyPolymer.Function:
                    ret.Result = WebHub.CrossAccess.DoPolymer(running.Config, polymer, new Record(firstCode, firsResult), running);
                    break;
                case ProxyPolymer.Callback:
                    ret.Result = Execute(GetProxyConfig(polymer.ToTag), new Record("_uuid_", running.Uuid).Put("_taskinfo_", TaskInfo).Put("_data_", firsResult).Merge(running.ParamData), running.EnvCode, running.Header, running.AuthInfo, running.Follow);
                    break;
            }
            return new AjaxResult().SetData(ret);
        }
        else
        {
            if (polymer.Asyn != 1) return new AjaxResult(900, "非异步请求[1194]");
            var uuid = running.Follow;
            if (polymer.Wait != 1)
            {
                var result = PluginHub.Redis.GetCache("P_" + uuid + "_running", redisindex).Data;
                if (result.HasValue() && result.IsJson()) return new AjaxResult().SetData(Json.ToObject<object>(result));
                return new AjaxResult().SetData(result);
            }
            else
            {
                var current = running.Data.GetString("_current_") ?? "";
                var result = Json.ToObject<ProxyPolymerResult>(PluginHub.Redis.GetCache("P_" + uuid + "_running", redisindex).Data);
                var times = 0;
                while (result == null || (result.Current.Length == current.Length && result.Code != 200))
                {
                    times++;
                    PluginHub.Redis.Subscribe(uuid, TimeSpan.FromSeconds(10));
                    result = Json.ToObject<ProxyPolymerResult>(PluginHub.Redis.GetCache("P_" + uuid + "_running", redisindex).Data);
                    if (times >= 3)break;
                }
                return result == null ? new AjaxResult(409,"等待超时未结果[1144]"):new AjaxResult().SetData(result);
            }
        }
        return new AjaxResult();

         */
    }
    /// <summary>
    /// 执行代理服务
    /// </summary>
    /// <param name="key">服务的键名</param>
    /// <param name="data">请求数据</param>
    /// <returns></returns>
    public static AjaxResult Execute(String key, Record data)
    {
        ProxyConfig config = DataHub.WorkCache.getProxyCache(key);
        if (config == null)
        {
            DbProxy bean = QueryAction.getObject("sys_service", new Cnd("name", key),"","",DbProxy.class);
            if (bean == null)
            {
                //WebReply.Reply(new AjaxResult(404));
                return new AjaxResult(404);
            }
            config = new ProxyConfig(bean);
            DataHub.WorkCache.addProxyCache(key, config);
        }
        return Execute(config, data,0,null,null,null);
    }

    /****
     * 执行代理服务
     * @param service
     * @param tag
     * @param data
     * @param sign
     * @param key
     * @param version
     * @param method
     * @param envcode
     * @param header
     * @return
     */
    public static AjaxResult Execute(String service, String tag, Record data, String sign, String key, String version, String method, int envcode, Record header)
    {

        Receipt<Record> hhh = Security.encryptByHead(tag, data, sign, key, version, envcode, header);
        if (hhh.isSuccess())
        {
            return HttpHelper.request(service, data, method, hhh.data).toResult();
        }
        return new AjaxResult(500,hhh.message);
    }

    public static Object GetResultCache(String key)
    {
        throw Core.makeThrow("未实现[1215]","ProxyService");
//        return LocalCache.GetCache(key, "Proxy_Result_Cache");
    }

    /// <summary>
    /// 获取代理的配置
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="version">版本号</param>
    /// <returns></returns>
    public static ProxyConfig getProxyConfig(String key, String version)
    {
        if (Strings.isBlank(version)) version = "";
        ProxyConfig config = DataHub.WorkCache.getProxyCache(key + version);
        ProxyConfig newest = DataHub.WorkCache.getProxyCache(key);
        if (config == null)
        {
            if (Strings.isBlank(version))
            {
                DbProxy bean = QueryAction.getObject("sys_service", new Cnd("name", key),"","",DbProxy.class);
                if (bean == null) return null;
                config = new ProxyConfig(bean);
                if (!WebHub.CrossAccess.OnConfig(config)) return null;
                DataHub.WorkCache.addProxyCache(key, config);
            }
            else
            {
                if (newest == null)
                {
                    DbProxy bean = QueryAction.getObject("sys_service", new Cnd("name", key),"","", DbProxy.class);
                    if (bean != null)
                    {
                        newest = new ProxyConfig(bean);
                        if (!WebHub.CrossAccess.OnConfig(newest)) return null;
                        DataHub.WorkCache.addProxyCache(key, newest);
                    }
                }
                if (newest != null && newest.raw.version.equals(version)) return newest;

                Record data = QueryAction.getRecord("sys_service_versions", new Cnd("name", key).put("version", version),"","");
                if (data == null)
                {
                    DbProxy bean = QueryAction.getObject("sys_service", new Cnd("name", key),"","",DbProxy.class);
                    if (bean == null) return null;
                    config = new ProxyConfig(bean);
                    if (!WebHub.CrossAccess.OnConfig(config)) return null;
                    DataHub.WorkCache.addProxyCache(key + version, config);
                }
                else
                {
                    data.put("id", data.getString("serviceid"));
                    config = new ProxyConfig(data.toClass(DbProxy.class));
                    if (!WebHub.CrossAccess.OnConfig(config)) return null;
                    DataHub.WorkCache.addProxyCache(key + version, config);
                }
            }

        }
        return config;
    }


    public static void RemoveCache(String key){
        DataHub.WorkCache.removeProxyCache(key);
    }

}
