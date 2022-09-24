package com.jladder.proxy;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jladder.actions.impl.QueryAction;
import com.jladder.actions.impl.SaveAction;
import com.jladder.actions.impl.ScriptAction;
import com.jladder.data.*;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.db.Rs;
import com.jladder.db.SqlText;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.entity.DbProxy;
import com.jladder.hub.DataHub;
import com.jladder.hub.WebHub;
import com.jladder.lang.*;
import com.jladder.script.Script;
import com.jladder.logger.LogFoRequest;
import com.jladder.logger.Logs;
import com.jladder.net.http.HttpHelper;
import com.jladder.openapi30.Operation;
import com.jladder.openapi30.PathItem;
import com.jladder.web.WebContext;
import com.jladder.web.WebScope;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ProxyService {

    /// <summary>
    /// 获取交互权限
    /// </summary>
    /// <returns></returns>
    public static CrossAccessAuthInfo GetAccessAuthInfo()
    {
//        return (CrossAccessAuthInfo)WebScope.GetValue("proxy_auth_info");
        return null;
    }

    /**
     * 代理服务执行方法
     * @param config 代理配置信息
     * @param data 用户参数数据
     * @param env 调用环境代码
     * @param header 请求头信息
     * @param authinfo 认证信息
     * @param follow
     * @return
     */
    public static AjaxResult execute(ProxyConfig config, Record data, int env, Record header, CrossAccessAuthInfo authinfo, String follow){
        //声明运行时实例对象
        ProxyRunning running = new ProxyRunning(data, authinfo, header, config, env, follow);
        try{
            //获取调用环境
            ProxyFunctionInfo funinfo = config.get(env);
            if (funinfo == null) return end(new AjaxResult(500, "未找到相应调用环境[0292]"), running);
            if ("polymer".equals(funinfo.type ) && Strings.hasValue(follow)){
                return end(handlePolymer((ProxyPolymer)funinfo.result, running), running);
            }
            if (authinfo != null){
                //WebScope.setValue("proxy_auth_info", authinfo);
            }
            if (Maths.isBitEq1(config.raw.logoption,ProxyLogOption.Head) || Maths.isBitEq1(config.raw.logoption,ProxyLogOption.Error) || Maths.isBitEq1(config.raw.logoption,ProxyLogOption.Follow))
            {
                running.trace.put("head", Json.toJson(running.header));
            }
            //是否忽略请求日志
            if (!Maths.isBitEq1(config.raw.logoption,ProxyLogOption.Ignore)) {
                running.requesting = new LogFoRequest();
                running.requesting.type=authinfo == null ? "proxy" : authinfo.mode;
                running.requesting.path=config.raw.name;
                running.requesting.header= Json.toJson(running.header.match((x, y) -> x.startsWith("_")));
                running.requesting.userinfo =(authinfo == null ? "" : authinfo.username + (Strings.isBlank(authinfo.withwho) ? "" : "|" + authinfo.withwho));
                running.requesting.withwho = (authinfo == null ? "" :authinfo.withwho);
                running.requesting.uuid = running.uuid;
                running.requesting.setRequest(data);

                //Logs.Write(running.requesting, LogOption.Request);
            }
            if (config.checkOption(ProxyLogOption.Info) || config.checkOption(ProxyLogOption.Error) || config.checkOption(ProxyLogOption.Follow)) {
                running.trace.put("info", "版本号:" + config.raw.version + System.lineSeparator()
                        + "加密方式:" + config.raw.type + System.lineSeparator()
//                        + "关联请求ID:" + WebScope.getRequestMark() + System.lineSeparator()
                        + "客户端Ip:" + running.header.getString("ladder-client-ip")
//                        + "请求地址:" + WebContext.Current?.Request.Path.Value
                );
            }
            AjaxResult res = new AjaxResult(456); //返回结果
            if (data == null) data = new Record(); //解请求参数
            //region 用户参数映射列表,无用户级参数配置，直接把请求数据传递给调用级数据
            String ignoreLogKeys = "";
            if (!Rs.isBlank(config.mappings)){
                for (ProxyMapping mapping : config.mappings){
                    String key = data.haveKey(mapping.paramname);
                    if (Strings.isBlank(key) && "0".equals(mapping.ignore)) return end(new AjaxResult(400, "参数不足"+mapping.paramname), running);
                    //移除大文本的日志记录
                    if (Regex.isMatch(mapping.datatype, "(file)|(text)")) ignoreLogKeys += key + ",";
                    Object v = Strings.hasValue(key) ? data.get(key) : mapping.dvalue;
                    if (("1".equals(mapping.ignore) || "启用".equals(mapping.ignore)) && (v == null || Strings.isBlank(v.toString()))){
                        if (Strings.hasValue(mapping.valid)) return end(new AjaxResult(444, "参数[" + mapping.paramname + "]未通过验证[0125]"), running);
                        continue;
                    }
                    //默认日期时间
                    if (v instanceof String && Regex.isMatch(mapping.datatype, "(date)|(time)") && Regex.isMatch(v.toString(), "^\\s*\\$")){
                        if (Regex.isMatch(mapping.datatype, "^\\s*date\\s*$")) {
                            if (Regex.isMatch(v.toString(), "^\\$date$"))
                                v = Times.getDate();
                            if (Regex.isMatch(v.toString(), "^\\$((datetime)|(now))$"))
                                v = Times.getNow();
                            if (Regex.isMatch(v.toString(), "^\\d*$")){
                                v = Times.sD(Times.D(Long.parseLong(v.toString())));
                            }
                        }
                        else{
                            if (Regex.isMatch(v.toString(), "^\\$((datetime)|(now))$"))
                                v = Times.getNow();
                            if (Regex.isMatch(v.toString(), "^\\d*$")){
                                v = Times.sDT(Times.D(Long.parseLong(v.toString())));;
                            }
                        }
                    }
                    //验证器
                    if (Strings.hasValue(mapping.valid)){
                        if (v == null) return end(new AjaxResult(444, "参数[" + mapping.paramname + "]未通过验证[0155]"), running);
                        Receipt check = Strings.check(v.toString(),mapping.valid);
                        if (!check.isSuccess())
                            return end(new AjaxResult(444, "参数[" + mapping.paramname + "]" + check.getMessage()), running);
                    }
                    //格式化
                    if (Strings.hasValue(mapping.format)){

                        v = Strings.mapping(Strings.mapping(Strings.mapping(mapping.format),"value", v.toString()),data);
                    }
                    //映射参数
                    String[] ps = { mapping.paramname };
                    if (!Strings.isBlank(mapping.mapping)){
                        String[] mmss = mapping.mapping.split("\\|",-1);
                        Record pd = running.paramdata;
                        for(String m : mmss){
                            ps = m.split(m.contains("/") ? "/" : "\\.");
                            if (ps.length == 1){
                                if (pd.get(ps[0]) instanceof Record && Strings.isJson(v.toString(),1))
                                    pd.get(ps[0],Record.class).merge(Record.parse(v));
                                else pd.put(ps[0], v);
                            }
                            else{
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
            if (config.checkOption(ProxyLogOption.Request) || config.checkOption(ProxyLogOption.Error) || config.checkOption(ProxyLogOption.Follow)){
                running.trace.put("request", Json.toJson(data.filter(ignoreLogKeys)));
            }
            //endregion
            ///region填充参数
            ignoreLogKeys = "";
            if (!Rs.isBlank(config.params)){
                Record newparamData = new Record();
                //调用环境参数列表
                for (ProxyParam param : config.params){
                    String key = running.paramdata.haveKey(param.paramname);
                    String stringvalue = (Strings.isBlank(key) ? param.dvalue : running.paramdata.getString(key));
                    if ("1".equals(param.required) && Strings.isBlank(stringvalue)){
                        return end(new AjaxResult(444).setMessage("[" + param.paramname + "]未被填充[0211]"), running);
                    }
                    //参数可空,且默认值为空，用户传值也为空
                    if("0".equals(param.required) && Strings.isBlank(key) && Strings.isBlank(stringvalue))continue;
                    //参数可忽略
                    if("2".equals(param.required) && Strings.isBlank(key))continue;
                    //默认日期时间
                    if (Strings.hasValue(stringvalue) && Regex.isMatch(param.datatype, "(date)|(time)") && Regex.isMatch(stringvalue, "^\\s*\\$")) {
                        if (Regex.isMatch(param.datatype, "^\\s*date\\s*$")){
                            if (Regex.isMatch(stringvalue, "^\\$date$"))
                                stringvalue = Times.getDate();
                            if (Regex.isMatch(stringvalue, "^\\$((datetime)|(now))$"))
                                stringvalue = Times.getNow();
                            if (Regex.isMatch(stringvalue, "^\\d*$")){
                                stringvalue = Times.sD(Times.D(Long.parseLong(stringvalue)));
                            }
                        }
                        else{
                            if (Regex.isMatch(stringvalue, "^\\$((datetime)|(now))$")) stringvalue = Times.getNow();
                            if (Regex.isMatch(stringvalue, "^\\d*$")) stringvalue = Times.sDT(Times.D(Long.parseLong(stringvalue)));
                        }
                    }
                    if (Strings.hasValue(param.valid)){
                        Receipt check = Strings.check(stringvalue,param.valid);
                        if (!check.isSuccess())return end(new AjaxResult(444, "映射参数[" + param.paramname + "]" + check.getMessage()), running);
                    }
                    switch (param.datatype.toLowerCase()){
                        case "int":
                            if (Strings.isBlank(stringvalue)){
                                newparamData.put(param.paramname, 0);
                            }
                            else{
                                if (!Strings.isNumber(stringvalue)) return end(new AjaxResult(444, "映射参数[" + param.paramname + "]不是整数类型"), running);
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
                                return end(new AjaxResult(444, "映射参数[" + param.paramname + "]不是日期类型"), running);
                            newparamData.put(param.paramname, Convert.toDate(stringvalue));
                            break;
                        case "datetime":
                            if (!Strings.isDateTime(stringvalue)) return end(new AjaxResult(444, "映射参数[" + param.paramname + "]不是日期时间类型"), running);
                            newparamData.put(param.paramname, Convert.toDate(stringvalue));
                            break;
                        case "number":
                        case "decimal":
                            if (Strings.isBlank(stringvalue)){
                                newparamData.put(param.paramname, 0);
                            }
                            else{
                                if (!Strings.isDecimal(stringvalue)) return end(new AjaxResult(444, "映射参数[" + param.paramname + "]不是数字类型"), running);
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

            if (config.checkOption(ProxyLogOption.Call)){
                Record filterData = running.paramdata.filter(ignoreLogKeys);
                running.trace.put("call", Json.toJson(filterData) + System.lineSeparator() + HttpHelper.toFormData(filterData,false));
            }
            if (config.checkOption(ProxyLogOption.Idempotency)){
                Record filterData = running.paramdata.filter(ignoreLogKeys);
                running.hashcode = HttpHelper.toFormData(filterData,false);
                if (Strings.hasValue(running.hashcode) && authinfo != null) {
                    running.hashcode += authinfo.username + authinfo.withwho + authinfo.ip;
                    running.hashcode = Security.md5(running.hashcode);
                }
                if (Strings.hasValue(running.hashcode)){

                    AjaxResult old = (AjaxResult)DataHub.WorkCache.getModuleCache(running.hashcode,"_Proxy_Idempotency_");
                    if (old != null){
                        running.hashstatus = true;
                        return end(old, running);
                    }
                    else{
                        DataHub.WorkCache.addModuleCache(running.hashcode, new AjaxResult(AjaxResultCode.ProxyMethodRefused).setMessage("禁止重复请求[0325]"),"_Proxy_Idempotency_",5);
                    }
                }
            }
            //替换参数
            if(funinfo.param!=null)funinfo.param.forEach((k,v) -> running.paramdata.put(k, v));
            //配置调用环境类型直接返回结果
            if (Strings.isBlank(funinfo.type)) return end(res.ok().setData(funinfo), running);
            //region 转换调用环境的数据，包括Data和Head

            ReStruct<Record, Record> tResult = WebHub.CrossAccess.doTransition(running.paramdata, config, env, running.header);
            if (!tResult.isSuccess()) return end(res.set(506, "不具备调用执行条件[0300]"), running);
            running.paramdata = tResult.getA();
            running.header=tResult.getB();;
            //endregion
            //region 处理过程事件
            AjaxResult callResult = WebHub.CrossAccess.onCall(config, running.paramdata, running.header, authinfo);
            if (callResult != null) return end(callResult, running);
            //endregion
            switch (funinfo.type.toLowerCase()){
                //region 多接口聚合结果
                case "join":{
                    String joinconfig = Json.toJson(funinfo.result);
                    //对象
                    if(Strings.isJson(joinconfig,1)){
                        JSONObject c = JSONObject.parseObject(joinconfig);
                        Record r = new Record();
                        c.forEach((k,v)->{
                            Record p = Record.parse(v);
                            String tag =  p.getString("tag");
                            String version =  p.getString("version");
                            Record pm =  Record.parse(Strings.mapping(p.getString("data"),running.paramdata) );
                            AjaxResult tt = ProxyService.execute(ProxyService.getProxyConfig(tag, version), pm, running.envcode, running.header, running.authinfo, running.follow);
                            r.put(k,tt.data);
                        });
                        return end(new AjaxResult().setData(r), running);
                    }
                    //数组
                    if(Strings.isJson(joinconfig,2)){
                        JSONArray c = JSONArray.parseArray(joinconfig);
                        List<Object> r = new ArrayList<Object>();
                        c.forEach((v)->{
                            Record p = Record.parse(v);
                            String tag =  p.getString("tag");
                            String version =  p.getString("version");
                            Record pm =  Record.parse(Strings.mapping(p.getString("data"),running.paramdata) );
                            AjaxResult tt = ProxyService.execute(ProxyService.getProxyConfig(tag, version), pm, running.envcode, running.header, running.authinfo, running.follow);
                            r.add(tt.data);
                        });
                        return end(new AjaxResult().setData(r), running);
                    }
                }
                break;
                //endregion
                //region 自定义配置
                case "config":{
                    String configjson = Json.toJson(funinfo.result);
                    //对象
                    if(Strings.isJson(configjson,1)){
                        JSONObject c = JSONObject.parseObject(configjson);
                        Record r = new Record();
                        c.forEach((k,v)->{
                            ProxyFunctionInfo func = Json.toObject(Strings.mapping(Json.toJson(v),running.paramdata), ProxyFunctionInfo.class);
                            AjaxResult tt = call(func,running);
                            r.put(k,tt.data);
                        });
                        return end(new AjaxResult().setData(r), running);
                    }
                    //数组
                    if(Strings.isJson(configjson,2)){
                        JSONArray c = JSONArray.parseArray(configjson);
                        List<Object> r = new ArrayList<Object>();
                        c.forEach((v)->{
                            ProxyFunctionInfo func = Json.toObject(Strings.mapping(Json.toJson(v),running.paramdata), ProxyFunctionInfo.class);
                            AjaxResult tt = call(func,running);
                            r.add(tt.data);
                        });
                        return end(new AjaxResult().setData(r), running);
                    }
                }
                break;
                //endregion
                //region 处理条件分发
                case "route":
                {
                    boolean ismatch = false;
                    List<ProxyRouteFunctionInfo> routes = (List<ProxyRouteFunctionInfo>)funinfo.result ;
                    if (routes == null || routes.size() < 1) return end(new AjaxResult(840, "分发路由未匹配"), running);
                    ProxyFunctionInfo def = null;
                    for (ProxyRouteFunctionInfo ri : routes)
                    {
                        if ("default".equals(ri.condition))
                        {
                            def = ri.funinfo;
                            continue;
                        }
                        boolean ret = Script.eval(Strings.mapping(Strings.mapping(ri.condition,running.paramdata, false),running.header),Boolean.class);
                        if (ret)
                        {
                            funinfo = ri.funinfo;
                            ismatch = true;
                            break;
                        }
                    }

                    if (!ismatch)
                    {
                        if (def == null) return end(new AjaxResult(841, "分发路由未匹配"), running);
                        else funinfo = def;
                    }
                }
                break;
                //endregion
                //region 负载平衡
                case "balance":{
                        List<ProxyRouteFunctionInfo> routes = (List<ProxyRouteFunctionInfo>)funinfo.result;
                        if (routes == null || routes.size() < 1) return end(new AjaxResult(842, "负载路由未匹配"), running);
                        try
                        {
                            funinfo = routes.get(config.getWeightIndex()).funinfo;
                        }
                        catch (Exception e)
                        {
                            //Logs.Write(new LogForError(e.Message) { StackTrace = e.StackTrace, Module = config.Name, Type = "Proxy" }, LogOption.Error);
                            return end(new AjaxResult(843, "负载路由计算异常"), running);
                        }
                    }
                    break;
                //endregion
                case "polymer":
                    return end(handlePolymer((ProxyPolymer)funinfo.result, running), running);
                case "job":
                    break;
                case "replace":
                    return ProxyService.execute(ProxyService.getProxyConfig(funinfo.functionname,funinfo.uri),running.paramdata,running.envcode,running.header,running.authinfo,running.follow);
                default:
                    return end(call(funinfo, running), running);
            }
            return new AjaxResult(400, "未进行有效处理[0390]");
        }
        catch (Exception e){
            System.out.println(running.config.name);
            e.printStackTrace();
            //Logs.Write(new LogForError(e.Message) { StackTrace = e.StackTrace, Module = config.Name, Type = "Proxy" }, LogOption.Error);
            AjaxResult result = new AjaxResult(500, e.toString()).setXData(Core.getStackTrace(e));
            //WebHub.DoRequest.ProxyError(config, result);
            return result;
        }
        finally{
            Logs.write(running);
            Logs.write(running.requesting);
//            String out = logText.toString();
//            if(Strings.hasValue(out)) Logs.write(out,"logs/proxy/"+ config.name+"/" +Times.getDate()+".log",LogOption.Proxy);
            //if (logText.length() > 0) Logs.WriteLog("----" + running.Uuid + "----" + System.lineSeparator() + logText + "----" + running.Uuid + "----", "Proxy/" + config.Name);
        }
    }

    /**
     * 收尾处理
     * @param result 处理结果
     * @param running 运行时信息
     * @return
     */
    private static AjaxResult end(AjaxResult result, ProxyRunning running){
        if (result.success){
            if (!running.config.checkOption(ProxyLogOption.Request) && !running.config.checkOption(ProxyLogOption.Follow)) running.trace.delete("request");
            if (!running.config.checkOption(ProxyLogOption.Head) && !running.config.checkOption(ProxyLogOption.Follow)) running.trace.delete("head");
            if (!running.config.checkOption(ProxyLogOption.Call) && !running.config.checkOption(ProxyLogOption.Follow)) running.trace.delete("call");
            if (!running.config.checkOption(ProxyLogOption.Info) && !running.config.checkOption(ProxyLogOption.Follow)) running.trace.delete("info");
            if (running.config.checkOption(ProxyLogOption.Result) || running.config.checkOption(ProxyLogOption.Follow)){
                if (running.requesting != null) running.requesting.result = Json.toJson(result);
                running.trace.put("result", Json.toJson(result));
            }
        }
        else{
            if (running.requesting != null) running.requesting.result = Json.toJson(result);
            running.trace.put("result", Json.toJson(result));
        }
        if (running.requesting != null){
            running.requesting.setEnd();
            List<String> baselines = running.config.getRuleValues(ProxyRuleOption.Timeout, running.authinfo);
            if(!Rs.isBlank(baselines))running.requesting.baseline=Convert.toInt(Collections.first(baselines,null));
            //Logs.Write(running.Requesting, LogOption.Request);
        }
        if (running.config.checkOption(ProxyLogOption.Idempotency) && Strings.hasValue(running.hashcode) && !running.hashstatus){
            List<String> limits = running.config.getRuleValues(ProxyRuleOption.Idempotency, running.authinfo);
            if (Rs.isBlank(limits)) DataHub.WorkCache.addModuleCache(running.hashcode, result, "_Proxy_Idempotency_",5);
            else
            {
                //Arrays.stream(limits)
                Optional<String> max =limits.stream().max(Comparator.comparing(x->Integer.parseInt(x)));
                int maxint = Integer.parseInt(max.get());
                if (maxint > 0) DataHub.WorkCache.addModuleCache(running.hashcode, result, "_Proxy_Idempotency_",maxint);
            }
        }
        result = WebHub.CrossAccess.onResult(result, running.config, running.paramdata, running.header, running.uuid, running.authinfo);
        try{
            //控制节点路径
            List<ProxyRule> rule_list = running.config.getRules(ProxyRuleOption.NodePath, running.authinfo);
            ProxyRule dore = Rs.isBlank(rule_list)? null: rule_list.get(rule_list.size() - 1);
            if (dore != null){
                try {
                    result.data = Record.parse(result.data).find(dore.nodepath);
                }
                catch (Exception e) {
                    result.set(AjaxResultCode.DataTransaction.getCode(), "数据转换错误[137]");
                }
            }
            //切割结构体
            List<String> cut = running.config.getRuleValues(ProxyRuleOption.CutResult, running.authinfo);
            if (!Rs.isBlank(cut) && Collections.any(cut,x -> "1".equals(x))) result.cut(true);
            //保存数据缓存
            rule_list = running.config.getRules(ProxyRuleOption.Cache, running.authinfo);
            ProxyRule cacherule = (Rs.isBlank(rule_list)?null:rule_list.get(rule_list.size() - 1));
            if (cacherule != null){
                String  cachek = null;//缓存键名
                switch (cacherule.nodepath){
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
        catch (Exception e) {
            e.printStackTrace();
//            result.Set((int)AjaxResultCode.DataTransaction, "数据转换错误[146]");
//            Logs.Write(new LogForError(e.Message, "ProxyEnd").SetStackTrace(e.StackTrace).SetModule("ProxyService"), LogOption.Error);
        }
        running.stopWatch.stop();
        return result.setDuration(running.stopWatch).setRel(running.hashstatus?  new Record("uuid", running.uuid).put("hashcode", running.hashcode).put("name", running.config.name).toString():running.uuid);
    }
    /**
     * 调用执行
     * @param funInfo 调用信息
     * @param running 运行时信息
     * @return
     */
    private static AjaxResult call(ProxyFunctionInfo funInfo, ProxyRunning running){
        ///region 进行环境方法调用
        switch (funInfo.type.toLowerCase()){
            ///region 类库方法
            case "lib":
                AjaxResult ret; //处理的返回结果
                Object rev = null;
                switch (funInfo.functionname.toLowerCase()){
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
                        return rev == null ? new AjaxResult(new AjaxResult(404)): new AjaxResult(new AjaxResult(rev));
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
                        long count = QueryAction.getCount(running.paramdata.getString("tableName", true),running.paramdata.getString("condition", true), running.paramdata.getString("param", true));
                        ret = new AjaxResult().setData(count);
                        return ret;
                    case "getpagedata":
                        if (Strings.hasValue(running.paramdata.getString("conn")))
                            WebScope.SetDataModelConn(running.paramdata.getString("tableName", true),running.paramdata.getString("conn"));
                        ret = QueryAction.getPageData(
                                running.paramdata.getString("tableName", true),
                                running.paramdata.getString("condition", true),
                                running.paramdata.getString("columns", true),
                                running.paramdata.getString("param", true),
                                running.paramdata.getInt("start"),
                                running.paramdata.getInt("pageNumber"),
                                running.paramdata.getInt("psize"),
                                running.paramdata.getString("recordCount", true)
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
                try{
                    Object dat = null;
                    switch (funInfo.functionname.toLowerCase()){
                        case "getvalue":
                            dat = dao.getValue(new SqlText(Strings.mapping(HttpHelper.decode(funInfo.code),running.paramdata)),String.class);
                            break;
                        case "getvalues":
                            dat = dao.getValues(new SqlText(Strings.mapping(HttpHelper.decode(funInfo.code),running.paramdata)));
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
                catch (Exception e){
                    return new AjaxResult(500, "执行错误").setXData(e.getMessage());
                }
                finally{
                    dao.close();
                }
                ///endregion
                ///region 内部应用环境
            case "controller":
                Receipt handleresult = Refs.invoke(funInfo.path,Core.or(funInfo.uri,funInfo.functionname),running.paramdata);
                return handleresult.isSuccess() ? new AjaxResult(handleresult.getData()) : new AjaxResult(500).setMessage(handleresult.getMessage());
            case "script":
                {
                    if(Strings.isBlank(funInfo.code))return new AjaxResult(500).setMessage("脚本指令为空[0670]");
                    String token = Security.md5(funInfo.code);
                    Receipt<Script> ret_script = ScriptAction.createScript("Proxy_Script_" + token, Security.decryptByBase64(funInfo.code));
                    if(!ret_script.isSuccess())return ret_script.toResult();
                    Receipt<Object> ret_rr = ret_script.getData().invoke(funInfo.functionname, running.paramdata.values().toArray());
                    if(!ret_rr.isSuccess())return ret_rr.toResult();
                    return new AjaxResult().setData(ret_rr.getData()).setDataType(AjaxResultDataType.Variant);
                }
            //内部程序
            case "service":
            case "services":
                {
                    Receipt result = Refs.invoke(funInfo.path,Core.or(funInfo.uri,funInfo.functionname), running.paramdata);
                    return result.isSuccess() ? new AjaxResult().setData(result.getData()) : new AjaxResult(500, result.getMessage());
                }
            case "program":
                {
                    Receipt result = Refs.invoke(funInfo.path,funInfo.functionname, running.paramdata);
                    return result.isSuccess() ? new AjaxResult().setData(result.getData()) : new AjaxResult(500, result.getMessage());
                }
            case "replace":
                {
                    ProxyConfig rconigs = getProxyConfig(funInfo.functionname, funInfo.uri);
                    return rconigs == null ? new AjaxResult(506, "未找到相应调用环境") : ProxyService.execute(rconigs, running.paramdata, running.envcode, running.header, running.authinfo,null);
                }
                ///endregion
                ///region 网络请求
            case "httpjson":
            case "postjson":{
                fillLadderHeader(running);
                Receipt<String> rpath = WebHub.CrossAccess.doPath(Strings.mapping(funInfo.path,"host", WebContext.getHost()), running.config, running.paramdata, running.header);
                if (!rpath.isSuccess()) return new AjaxResult(501, rpath.getMessage());
                Receipt<String> re = HttpHelper.requestByJson(rpath.getData(), running.paramdata.toString(), running.header);
                if (!re.isSuccess() && re.getMessage().contains("基础连接已经关闭")){
                    re = HttpHelper.requestByJson(rpath.getData(), running.paramdata.toString(), running.header);
                }
                if (!re.isSuccess() && re.getMessage().contains("(502) 错误的网关")) {
                    re = HttpHelper.requestByJson(rpath.getData(), running.paramdata.toString(), running.header);
                }
                if (!re.isSuccess()){
//                    Logs.WriteLog($"错误消息:{re.Message}\n数据备注:{re.Data}", "proxy_error_httpjson");
                    return new AjaxResult(505, re.getMessage()+"[0717]").setData(re.getData());
                }
                else{
                    String retText = re.getData();
                    boolean isJson = Strings.isJson(retText);
                    if (isJson){
                        //WebReply.SetContentTypeToJson();
                        return Strings.hasValue(funInfo.uri) ? new AjaxResult().setData(Record.parse(retText).find(funInfo.uri)).setDataType(AjaxResultDataType.Json) : new AjaxResult().setData(Json.toObject(retText)).setDataType(AjaxResultDataType.Json);
                    }
                    else{
                        return new AjaxResult().setData(retText).setDataType(AjaxResultDataType.Text);
                    }
                }
            }
            case "httpget":
            case "http":
            case "get":
            case "web":{
                fillLadderHeader(running);
                Receipt<String> rpath = WebHub.CrossAccess.doPath(Strings.mapping(funInfo.path,"host", WebContext.getHost()), running.config, running.paramdata, running.header);
                if (!rpath.isSuccess()) return new AjaxResult(501, rpath.getMessage());
                Receipt<String> re = HttpHelper.request(rpath.getData(), running.paramdata, "get", running.header);
                if (!re.isSuccess() && re.getMessage().contains("基础连接已经关闭")){
                    re = HttpHelper.request(rpath.getData(), running.paramdata, "get",running.header);
                }
                if (!re.isSuccess() && re.getMessage().contains("(502) 错误的网关")){
                    re = HttpHelper.request(rpath.getData(), running.paramdata, "get", running.header);
                }
                if (!re.isSuccess()){
                    Logs.writeLog(rpath.getData()+System.lineSeparator()+"错误消息:"+re.getMessage()+"\n数据备注:"+re.getData(), "proxy_error_httpget");
                    return new AjaxResult(505, re.getMessage()+"[0754]").setData(re.getData());
                }
                else {
                    String retText = re.getData();
                    boolean isJson = Strings.isJson(retText);
                    if (isJson){
//                        WebReply.SetContentTypeToJson();
                        return Strings.hasValue(funInfo.uri) ? new AjaxResult().setData(Record.parse(retText).find(funInfo.uri)).setDataType(AjaxResultDataType.Json) : new AjaxResult().setData(Json.toObject(retText)).setDataType(AjaxResultDataType.Json);
                    }
                    else{
                        return new AjaxResult().setData(retText).setDataType(AjaxResultDataType.Text);
                    }
                }
            }
            case "httppost":
            case "post":
            case "webpost":
                {
                    fillLadderHeader(running);
                    Receipt<String> rpath = WebHub.CrossAccess.doPath(Strings.mapping(funInfo.path,"host", WebContext.getHost()), running.config, running.paramdata, running.header);
                    if (!rpath.isSuccess()) return new AjaxResult(501, rpath.getMessage());
                    Receipt<String> re = HttpHelper.request(rpath.getData(), running.paramdata, "post", running.header);
                    if (!re.isSuccess() && re.getMessage().contains("基础连接已经关闭")){
                        re = HttpHelper.request(rpath.getData(), running.paramdata, "post", running.header);
                    }
                    if (!re.isSuccess() && re.getMessage().contains("(502) 错误的网关")){
                        re = HttpHelper.request(rpath.getData(), running.paramdata, "post", running.header);
                    }
                    if (!re.isSuccess()){
                        Logs.writeLog(rpath.getData()+System.lineSeparator()+"错误消息:"+re.getMessage()+"\n数据备注:"+re.getData(), "proxy_error_httppost");
                        return new AjaxResult(505, re.getMessage()+"[0791]").setData(re.getData());
                    }
                    else{
                        String  retText = re.getData();
                        boolean isJson = Strings.isJson(retText);
                        if (isJson){
    //                        WebReply.SetContentTypeToJson();
                            return Strings.hasValue(funInfo.uri) ? new AjaxResult().setData(Record.parse(retText).find(funInfo.uri)).setDataType(AjaxResultDataType.Json) : new AjaxResult().setData(Json.toObject(retText)).setDataType(AjaxResultDataType.Json);
                        }
                        else{
                            return new AjaxResult().setData(retText).setDataType(AjaxResultDataType.Text);
                        }
                    }
                }
            case "upload":
            case "httpupload":{
                fillLadderHeader(running);
                Receipt<String> rpath = WebHub.CrossAccess.doPath(Strings.mapping(funInfo.path,"host", WebContext.getHost()), running.config, running.paramdata, running.header);
                if (!rpath.isSuccess()) return new AjaxResult(501, rpath.getMessage());
                List<UploadFile> files = new ArrayList<UploadFile>();
                Record ps = new Record();
                running.paramdata.forEach((k,v)->{
                    if (v instanceof UploadFile){
                        UploadFile uploadfile = (UploadFile)v;
                        if (uploadfile == null || Core.isEmpty(uploadfile.getData())) return;
                        files.add(uploadfile);
                        return;
                    }
                    if (v instanceof List){
                        List<UploadFile> uploadfiles = (List<UploadFile>)v;
                        if (uploadfiles == null || uploadfiles.size()<1) return;
                        files.addAll(uploadfiles);
                        return;
                    }
                    ps.put(k,v);
                });
                Receipt<String> re = Core.isEmpty(files) ? HttpHelper.request(rpath.getData(), running.paramdata, "post", running.header) : HttpHelper.upload(rpath.getData(), files, ps);
                if (!re.isSuccess() && re.getMessage().contains("基础连接已经关闭")){
                    re = HttpHelper.upload(rpath.getData(), files, ps);
                }
                if (!re.isSuccess() && re.getMessage().contains("(502) 错误的网关")){
                    re = Core.isEmpty(files) ? HttpHelper.request(rpath.getData(), running.paramdata, "post", running.header) : HttpHelper.upload(rpath.getData(), files, ps);
                }
                if (!re.isSuccess()){
                    Logs.writeLog(rpath.getData()+System.lineSeparator()+"错误消息:"+re.getMessage()+"\n数据备注:"+re.getData(), "proxy_error_upload");
                    return new AjaxResult(505, re.getMessage());
                }
                else{
                    if(!Core.isEmpty(files)){
                        StringBuilder uploadlog = new StringBuilder("上传文件个数:" + files.size() + System.lineSeparator());
                        files.forEach(fileinfo ->{
                            if (fileinfo == null) return;
                            String savefilename = Times.timestamp()+"_" + fileinfo.getFileName();
                            uploadlog.append("文件名:"+fileinfo.getFileName()+";表单名:"+fileinfo.getFormName()+";文件码:"+fileinfo.getFileCode()+";Md5:"+fileinfo.getMd5() + System.lineSeparator());
                            uploadlog.append("文件保存到:"+savefilename + System.lineSeparator());
                            //fileinfo.SaveAs(Logs.GetCatalogDir("Proxy/" + running.config.name + "/Files/") + savefilename);
                        });
                        running.trace.put("upload", uploadlog);
                    }
                    String  retText = re.getData();
                    boolean isJson = Strings.isJson(retText);
                    if (isJson){
                        return Strings.hasValue(funInfo.uri) ? new AjaxResult().setData(Record.parse(retText).find(funInfo.uri)).setDataType(AjaxResultDataType.Json) : new AjaxResult().setData(Json.toObject(retText)).setDataType(AjaxResultDataType.Json);
                    }
                    else{
                        return new AjaxResult().setData(retText).setDataType(AjaxResultDataType.Text);
                    }
                }
            }

                //endregion
                //region 静态结果

            case "result":
                return new AjaxResult().setData(funInfo.result);
            case "random":
                {
                    if (funInfo.result == null) return new AjaxResult(404, "无数据结果");
                    if (funInfo.result instanceof List){
                        List al = (List)funInfo.result;
                        return al.size() == 0 ? new AjaxResult(404, "无数据结果") : new AjaxResult().setData(al.get(R.random(0, al.size())));
                    }
                    if (funInfo.result instanceof String) {
                        List al = Json.toObject(funInfo.result.toString(),List.class);
                        return al.size()==0 ? new AjaxResult(404, "无数据结果") : new AjaxResult().setData(al.get(R.random(0, al.size())));
                    }
                    String jsontext = Json.toJson(funInfo.result);
                    if (jsontext.startsWith("{") && jsontext.endsWith("}")){
                        return new AjaxResult().setData(funInfo.result);
                    }
                    else if (jsontext.startsWith("[") && jsontext.endsWith("]")){
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

    /**
     * 处理多组聚合
     * @param polymer 多组聚合对象
     * @param running 运行时信息
     * @return
     */
    private static AjaxResult handlePolymer(ProxyPolymer polymer, ProxyRunning running)
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
    /**
     * 执行代理服务
     * @param key 服务的键名
     * @param data 请求数据
     * @return
     */
    public static AjaxResult execute(String key, Record data){
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
        return execute(config, data,0,null,null,null);
    }

    /**
     * 执行接口代理
     * @param service 服务器
     * @param tag 接口标签
     * @param data 请求数据
     * @param sign 用户标识
     * @param key 用户密钥
     * @param version 版本
     * @return
     */
    public static AjaxResult execute(String service, String tag, Record data, String sign, String key, String version){
        return execute(service,tag,data,sign,key,version,"POST",0,null);
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
    public static AjaxResult execute(String service, String tag, Record data, String sign, String key, String version, String method, int envcode, Record header)
    {
        Receipt<Record> hhh = Security.encryptByHead(tag, data, sign, key, version, envcode, header);
        if (hhh.isSuccess())
        {
            return HttpHelper.request(service, data, method, hhh.getData()).toResult();
        }
        return new AjaxResult(500,hhh.getMessage());
    }

    public static Object getResultCache(String key)
    {
        throw Core.makeThrow("未实现[1215]","ProxyService");
//        return LocalCache.GetCache(key, "Proxy_Result_Cache");
    }

    /**
     * 获取代理的配置
     * @param key 键名
     * @param version 版本号
     * @return
     */
    public static ProxyConfig getProxyConfig(String key, String version) {
        if (Strings.isBlank(version)) version = "";
        ProxyConfig config = DataHub.WorkCache.getProxyCache(key + version);
        ProxyConfig newest = DataHub.WorkCache.getProxyCache(key);
        if (config == null){
            if (Strings.isBlank(version)){
                DbProxy bean = QueryAction.getObject("sys_service", new Cnd("name", key),"","",DbProxy.class);
                if (bean == null) return null;
                config = new ProxyConfig(bean);
                if (!WebHub.CrossAccess.onConfig(config)) return null;
                DataHub.WorkCache.addProxyCache(key, config);
            }
            else{
                if (newest == null){
                    DbProxy bean = QueryAction.getObject("sys_service", new Cnd("name", key),"","", DbProxy.class);
                    if (bean != null){
                        newest = new ProxyConfig(bean);
                        if (!WebHub.CrossAccess.onConfig(newest)) return null;
                        DataHub.WorkCache.addProxyCache(key, newest);
                    }
                }
                if (newest != null && newest.raw.version.equals(version)) return newest;
                Record data = QueryAction.getRecord("sys_service_versions", new Cnd("name", key).put("version", version),"","");
                if (data == null){
                    DbProxy bean = QueryAction.getObject("sys_service", new Cnd("name", key),"","",DbProxy.class);
                    if (bean == null) return null;
                    config = new ProxyConfig(bean);
                    if (!WebHub.CrossAccess.onConfig(config)) return null;
                    DataHub.WorkCache.addProxyCache(key + version, config);
                }
                else{
                    data.put("id", data.getString("serviceid"));
                    config = new ProxyConfig(data.toClass(DbProxy.class));
                    if (!WebHub.CrossAccess.onConfig(config)) return null;
                    DataHub.WorkCache.addProxyCache(key + version, config);
                }
            }
        }
        return config;
    }

    /**
     * 填充代理请求的header头
     * @param running 运行信息
     */
    private static void fillLadderHeader(ProxyRunning running){
        if(running.authinfo!=null){
            if(Strings.hasValue(running.authinfo.ip)) running.header.put("ladder_client_ip",running.authinfo.ip);
            if(Strings.hasValue(running.authinfo.mode))running.header.put("ladder_client_mode",running.authinfo.mode);
            if(Strings.hasValue(running.authinfo.withwho))running.header.put("ladder_client_withwho",running.authinfo.withwho);
            if(Strings.hasValue(running.authinfo.sign))running.header.put("ladder_client_sign",running.authinfo.sign);
            if(Strings.hasValue(running.authinfo.client))running.header.put("ladder_client_client",running.authinfo.client);
            running.header.put("ladder_client_mark",WebContext.getMark());
        }
    }
    public static void RemoveCache(String key){
        DataHub.WorkCache.removeProxyCache(key);
    }

    public static String getMarkDown(String key){
        return "";
    }
    public static PathItem getOpenAPI(String key){
        ProxyConfig config = getProxyConfig(key,null);
        PathItem item = new PathItem();
        item.setSummary(config.raw.title);
        item.setDescription(config.raw.descr);
        Operation request = new Operation();
        request.setTag(config.name);
        if(Strings.isBlank(config.raw.method))config.raw.method="ALL";
        switch (config.raw.method){
            case "ALL":
            case "POST":
                request.setParametersByPost(config.mappings);
                item.setPost(request);
                break;
            case "GET":
                request.setParametersByGet(config.mappings);
                item.setGet(request);
                break;
        }
        return item;

    }
}
