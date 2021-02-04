package com.jladder.logger;

import com.jladder.actions.impl.EnvAction;
import com.jladder.data.Record;
import com.jladder.db.DbParameter;
import com.jladder.db.Rs;
import com.jladder.db.SqlText;
import com.jladder.hub.DataHub;
import com.jladder.lang.Json;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.web.WebContext;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class LogForSql {
    /// <summary>
    /// 执行类型
    /// </summary>
    public String type = "query";
    /// <summary>
    /// sql文本
    /// </summary>
    public String sqltext;
    /// <summary>
    /// 连接器
    /// </summary>
    public String conn;
    /// <summary>
    /// 是否错误
    /// </summary>
    public boolean isError;

    /// <summary>
    /// 是否超期
    /// </summary>
    public boolean isOverTime;

    /// <summary>
    /// 开始时间
    /// </summary>
    public Date starttime =  new Date();

    /// <summary>
    /// 结束时间
    /// </summary>
    public Date endtime;

    /// <summary>
    /// 持续时长
    /// </summary>
    public String duration;

    /// <summary>
    /// 引发原因
    /// </summary>
    public String cause;

    /// <summary>
    /// 调用堆栈
    /// </summary>
    public String stacktrace;
    /// <summary>
    /// 填充数据
    /// </summary>
    public String data;

    /// <summary>
    /// 访问者
    /// </summary>
    public String visitor = EnvAction.GetEnvValue("username");
    /// <summary>
    /// 请求标示
    /// </summary>
    public String requestmark;
    /// <summary>
    /// 标签,用于模型名称
    /// </summary>
    public String tag;


    /// <summary>
    /// 设置模型标签
    /// </summary>
    /// <param name="tag"></param>
    /// <returns></returns>
    public LogForSql SetTag(String tag)
    {
        this.tag = tag;
        return this;
    }

    /// <summary>
    /// 设置引发原因
    /// </summary>
    /// <param name="message">错误信息</param>
    /// <returns></returns>
    public LogForSql setCause(String message)
    {
        this.cause = message;
        return this;
    }
    /// <summary>
    /// 设置调用堆栈
    /// </summary>
    /// <param name="trace">堆栈</param>
    /// <returns></returns>
    public LogForSql setStackTrace(String trace)
    {
        this.stacktrace = trace;
        return this;
    }
    /// <summary>
    /// 设置调用堆栈
    /// </summary>
    /// <param name="type">执行类型</param>
    /// <returns></returns>
    public LogForSql setType(String type)
    {
        if (Strings.isBlank(type)) return this;
        String[] ts = Regex.split(type.trim(), "\\s");
        this.type = ts[0];
        return this;
    }
    /// <summary>
    /// 实例化
    /// </summary>
    /// <param name="sqltext">sql文本</param>
    public LogForSql(String sqltext)
    {
        this.sqltext = sqltext;
        this.requestmark = WebContext.getAttribute("__requestmark__");
    }
    /// <summary>
    /// 实例化
    /// </summary>
    /// <param name="sqltext">sql文本</param>
    /// <param name="conn">连接器</param>
    public LogForSql(String sqltext, String conn)
    {
        this.sqltext = sqltext;
        this.conn = conn;
        this.requestmark =WebContext.getAttribute("__requestmark__");
    }
    /// <summary>
    /// 实例化
    /// </summary>
    /// <param name="sqltext">sql文本</param>
    /// <param name="conn">连接器</param>
    /// <param name="iserror">是否错误</param>
    public LogForSql(String sqltext, String conn, boolean iserror)
    {
        this.sqltext = sqltext;
        this.conn = conn;
        this.isError = iserror;
        this.requestmark = WebContext.getAttribute("__requestmark__");
    }
    /// <summary>
    /// 实例化
    /// </summary>
    /// <param name="sqltext">sql文本</param>
    /// <param name="iserror">是否错误</param>
    public LogForSql(String sqltext, boolean iserror)
    {
        this.sqltext = sqltext;
        this.isError = iserror;
        this.requestmark = WebContext.getAttribute("__requestmark__");
    }
    /// <summary>
    /// 实例化
    /// </summary>
    public LogForSql()
    {
    }
    /// <summary>
    /// 实例化
    /// </summary>
    public LogForSql(SqlText sqltext)
    {
        if(sqltext==null)return;
        this.sqltext = sqltext.cmd;
        if (Rs.isBlank(sqltext.parameters)) return;
        Record dic = new Record();
        sqltext.parameters.forEach(x -> dic.put(x.name, x.value));
        this.data = dic.toString();
    }
    public LogForSql setEnd(){
        return setEnd(false);
    }

    /***
     * 设置结束点
     * @param isError 是否含有错误
     * @return
     */

    public LogForSql setEnd(boolean isError)
    {
        this.isError = isError;
        endtime = new Date();
        long time = (endtime.getTime() - starttime.getTime());
        if (time > DataHub.SqlWarnTime) this.isOverTime = true;
        duration = time + "ms";
        return this;
    }
    public LogForSql SetData(List<DbParameter> data)
    {
        if (Rs.isBlank(data)) return this;
        Record dic = new Record();
        data.forEach(x -> dic.put(x.name, x.value));
        this.data = dic.toString();
        return this;
    }
    public LogForSql SetData(Map<String, Object> data)
    {
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
        Logs.writeSql(this);
    }

    /***
     * 设置异常信息
     * @param e 异常
     */
    public void setException(Exception e) {
        e.printStackTrace();
        String error=e.getMessage();
        setEnd(true).setCause(error);
    }
}
