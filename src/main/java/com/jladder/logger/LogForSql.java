package com.jladder.logger;

import com.jladder.Ladder;
import com.jladder.actions.impl.EnvAction;
import com.jladder.configs.Configure;
import com.jladder.data.Record;
import com.jladder.db.DbParameter;
import com.jladder.db.Rs;
import com.jladder.db.SqlText;
import com.jladder.lang.Json;
import com.jladder.lang.Machine;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.web.WebContext;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogForSql {
    private String site;
    private String watchpoint;
    /**
     * 执行类型
     */
    public String type = "query";
    /**
     * sql文本
     */
    public String sqltext;
    /**
     * 连接器
     */
    public String conn;
    /**
     * 是否错误
     */
    public boolean isError;

    /**
     * 是否超期
     */
    public boolean isOverTime;

    /**
     * 开始时间
     */
    public Date starttime =  new Date();
    /**
     * 结束时间
     */
    public Date endtime;

    /**
     * 持续时长
     */
    public int duration;

    /**
     * 引发原因
     */
    public String cause;

    /**
     * 调用堆栈
     */
    public String stacktrace;
    /**
     * 填充数据
     */
    public String data;

    /// <summary>
    /// 访问者
    /// </summary>
//    public String visitor = EnvAction.GetEnvValue("username");
    public String visitor;
    /**
     * 请求标识
     */
    public String requestmark;
    /**
     * 标签,用于模型名称
     */
    public String tag;


    /**
     * 设置模型标签
     * @param tag 标签名称
     */
    public LogForSql SetTag(String tag){
        this.tag = tag;
        return this;
    }
    /**
     * 设置引发原因
     * @param message 错误信息
     */
    public LogForSql setCause(String message){
        this.cause = message;
        return this;
    }
    /**
     * 设置调用堆栈
     * @param trace 堆栈
     */
    public LogForSql setStackTrace(String trace){
        this.stacktrace = trace;
        return this;
    }

    /**
     * 设置调用堆栈
     * @param type 执行类型
     */
    public LogForSql setType(String type){
        if (Strings.isBlank(type)) return this;
        String[] ts = Regex.split(type.trim(), "\\s");
        this.type = ts[0];
        return this;
    }
    /**
     * 实例化
     * @param cmd sql命令
     */
    public LogForSql(String cmd){
        this.sqltext = cmd;
        this.requestmark = WebContext.getAttributeString("__requestmark__");
    }

    /**
     * 实例化
     * @param cmd sql命令
     * @param conn 连接器
     */
    public LogForSql(String cmd, String conn){
        this.sqltext = cmd;
        this.conn = conn;
        this.requestmark =WebContext.getAttributeString("__requestmark__");
    }
    /// <summary>
    /// 实例化
    /// </summary>
    /// <param name="sqltext">sql文本</param>
    /// <param name="conn">连接器</param>
    /// <param name="iserror">是否错误</param>
    public LogForSql(String cmd, String conn, boolean isError){
        this.sqltext = cmd;
        this.conn = conn;
        this.isError = isError;
        this.requestmark = WebContext.getAttributeString("__requestmark__");
    }
    /// <summary>
    /// 实例化
    /// </summary>
    /// <param name="sqltext">sql文本</param>
    /// <param name="iserror">是否错误</param>
    public LogForSql(String cmd, boolean isError){
        this.sqltext = cmd;
        this.isError = isError;
        this.requestmark = WebContext.getMark();
    }
    /// <summary>
    /// 实例化
    /// </summary>
    public LogForSql(){}

    /**
     * 实例化
     * @param sqltext sql语句
     */
    public LogForSql(SqlText sqltext){
        if(sqltext==null)return;
        this.sqltext = sqltext.cmd;
        if (Rs.isBlank(sqltext.parameters)) return;
        Record dic = new Record();
        sqltext.parameters.forEach(x -> {
            if(x.value instanceof InputStream)return;
            if(x.value instanceof byte[])return;
            dic.put(x.name, x.value);
        });
        this.data = dic.toString();
        this.requestmark = WebContext.getMark();
        setType(sqltext.cmd);
        if("select".equals(type))this.sqltext=sqltext.toString();
    }
    public LogForSql setEnd(){
        return setEnd(false);
    }

    /***
     * 设置结束点
     * @param isError 是否含有错误
     */

    public LogForSql setEnd(boolean isError){
        this.isError = isError;
        if(isError){
            LogFoRequest request_log = WebContext.getLogger();
            if(request_log!=null)request_log.increaseException();
        }
        endtime = new Date();
        long time = (endtime.getTime() - starttime.getTime());
        if (time > Ladder.Settings().getSqlWarnTime()) this.isOverTime = true;
        duration = (int)time;
        requestmark=WebContext.getMark();
        if(Strings.isBlank(this.type))setType(this.sqltext);
        if(Strings.isBlank(visitor))visitor = EnvAction.getEnvValue("username");
        watchpoint = Configure.getString("_MachineInfo_CPUID_");
        if(Strings.isBlank(watchpoint)){
            watchpoint= Machine.getCpuId();
            Configure.put("_MachineInfo_CPUID_",watchpoint);
        }
        site=Ladder.Settings().getSite();
        return this;
    }
    public LogForSql setData(List<DbParameter> data) {
        if (Rs.isBlank(data)) return this;
        Record dic = new Record();
        data.forEach(x -> dic.put(x.name, x.value));
        this.data = dic.toString();
        return this;
    }
    public LogForSql setData(Map<String, Object> data){
        if (data == null || data.size()<1) return this;
        this.data = Json.toJson(data);
        return this;
    }
    public LogForSql setTag(String tag){
        this.tag =tag;
        return this;
    }

    public LogForSql setConn(String conn) {
        this.conn = conn;
        return this;
    }
    public void write(){
        Logs.write(this);
    }
    /***
     * 设置异常信息
     * @param e 异常
     */
    public void setException(Exception e) {
        e.printStackTrace();
        this.stacktrace="";
        List<StackTraceElement> stacks = Arrays.stream(e.getStackTrace()).limit(25).collect(Collectors.toList());
        stacks.forEach(x->this.stacktrace+=x.getClassName()+"$"+x.getMethodName()+"("+x.getLineNumber()+")"+System.lineSeparator());
        setEnd(true).setCause(e.getMessage());
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}
