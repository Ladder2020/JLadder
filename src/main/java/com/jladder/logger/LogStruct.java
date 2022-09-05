package com.jladder.logger;

import com.jladder.lang.Strings;
import org.apache.commons.logging.Log;

import java.util.Date;

/// <summary>
/// 日志基本结构
/// </summary>
public class LogStruct
{
    /**
     * 日志选项
     */
    private LogOption option;
    /**
     * 日志实体
     */
    private Object log;
    /**
     * 日志文件名
     */
    private String filename;
    /**
     * 当前用户
     */
    private String user;

    /**
     * 创建时间
     */
    private Date createtime=new Date();

    private boolean isUpload  = true;


    /// <summary>
    /// 实例化
    /// </summary>
    public LogStruct()
    {
    }
    /// <summary>
    /// 实例化
    /// </summary>
    /// <param name="log">日志对象</param>
    public LogStruct(Object log)
    {
        this.option = LogOption.Debug;
        this.log = log;
    }
    public LogStruct(Object log, LogOption option){
        this.option = option;
        this.log = log;
    }
    /// <summary>
    /// 实例化
    /// </summary>
    /// <param name="log">日志对象</param>
    /// <param name="option">日志选项</param>
    /// <param name="isUp">是否上传</param>
    public LogStruct(Object log, LogOption option,boolean isUp)
    {
        this.option = option;
        this.log = log;
        this.isUpload = isUp;
    }

    /**
     * 构造方法
     * @param log 日志对象
     * @param option 日志选项
     * @param filename 日志文件名
     */
    public LogStruct(Object log, LogOption option, String filename)
    {
        this.option = option;
        this.log = log;
        this.filename = filename;
    }

    public LogOption getOption() {
        return option;
    }

    public void setOption(LogOption option) {
        this.option = option;
    }

    public Object getLog() {
        return log;
    }

    public void setLog(Object log) {
        this.log = log;
    }

    public String getFilename() {
        return filename;
    }

    public LogStruct setFilename(String filename) {
        if(Strings.hasValue(filename)) this.filename = filename;
        return this;
    }

    public String getUser() {
        return user;
    }

    public LogStruct setUser(String user) {
        this.user = user;
        return this;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public LogStruct setUpload(boolean isUp) {
        isUpload = isUp;
        return this;
    }







}
