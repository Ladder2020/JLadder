package com.jladder.scheduler;

import com.jladder.data.Record;
import com.jladder.db.bean.BaseEntity;
import com.jladder.lang.Json;
import com.jladder.lang.Strings;
import com.jladder.lang.Task;
import com.jladder.lang.Times;
import com.jladder.lang.func.Action0;
import com.jladder.net.http.HttpHelper;

import java.util.Date;
import java.util.Map;

public class ScheduleJob extends BaseEntity {
    /// <summary>
    /// 数据库id
    /// </summary>
    private String id;
    /// <summary>
    /// 任务名称
    /// </summary>
    private String jobname;
    /// <summary>
    /// 任务分组名称
    /// </summary>
    private String groupname;


    /// <summary>
    /// 开始时间
    /// </summary>
    private Date starttime;
    /// <summary>
    /// 结束时间
    /// </summary>
    private Date endtime;
    /// <summary>
    /// Cron表达式
    /// </summary>
    private String cronexpress;

    /// <summary>
    /// 状态
    /// </summary>
    private String state;

    /// <summary>
    /// 处理结果
    /// </summary>
    private String data;

    /// <summary>
    /// 执行类型
    /// </summary>
    private String type;


    /// <summary>
    /// 使能位
    /// </summary>
    private int enable = 1;

    /// <summary>
    /// 执行代码
    /// </summary>

    private String cmdcode;

    /// <summary>
    /// 周期类型
    /// </summary>
    private String eventype;

    /// <summary>
    /// 周期值
    /// </summary>
    private int evenvalue;
    /// <summary>
    /// 创建者
    /// </summary>
    private String creator;

    /// <summary>
    /// 功能模块
    /// </summary>
    private String module;

    /// <summary>
    /// 保持容量
    /// </summary>
    private int keepsize;

    /// <summary>
    /// 详细描述
    /// </summary>
    private String descr;

    /// <summary>
    /// 运行次数
    /// </summary>
    private int runtimes;

    /// <summary>
    /// 跳出周期规则
    /// </summary>
    private String breakrule;

    /// <summary>
    /// 监控选项
    /// </summary>
    private int watchoption;
    /// <summary>
    /// 上次运行时间
    /// </summary>
    private Date lasttime;

    /// <summary>
    /// 超时时间
    /// </summary>
    private int timeout = 0;

    /// <summary>
    /// 限制次数
    /// </summary>
    private int limittimes;



    private int workday;


    public ScheduleJob(){

    }
    public ScheduleJob(String jobname,String groupname){
        this.jobname=jobname;
        this.groupname=groupname;
    }
    public ScheduleJob(String jobname,String groupname,String cronexpress){
        this.jobname=jobname;
        this.groupname=groupname;
        this.cronexpress=cronexpress;
    }
    public ScheduleJob setExecute(String type,String cmdcode){
        this.type=type;
        this.cmdcode=cmdcode;
        return this;
    }
    /// <summary>
    /// 设置周期
    /// </summary>
    /// <param name="type"></param>
    /// <param name="num"></param>
    /// <param name="endTime"></param>
    /// <returns></returns>
    public ScheduleJob SetEven(EvenType type, int num, Date endtime) {
        if (endtime != null) this.endtime = endtime;
        if (starttime == null) starttime = new Date();
        if (endtime == null) endtime = new Date(Long.MAX_VALUE);
        eventype = type.toString().toLowerCase();
        evenvalue = num;
        return this;
    }

    /// <summary>
    /// 设置键名
    /// </summary>
    /// <param name="name">名称</param>
    /// <param name="group">分组</param>
    /// <returns></returns>
    public ScheduleJob SetKey(String name, String group) {
        if (starttime == null) starttime = new Date();
        if (endtime == null) endtime = Times.max();
        jobname = name;
        group = group;
        return this;
    }

    /// <summary>
    /// 快速发布
    /// </summary>
    /// <param name="url">服务器路径</param>
    public boolean Publish(String url) {
        return Publish(url, "schedule");
    }

    /// <summary>
    /// 快速发布
    /// </summary>
    /// <param name="url">服务器路径</param>
    /// <param name="paramName">参数名称</param>
    /// <returns></returns>
    public boolean Publish(String url, String paramName) {
        if (Strings.isBlank(paramName) || Strings.isBlank(url)) return false;
        Record dat = Record.parse(this);
        Record request = new Record();
        if (Strings.hasValue(paramName)) {
            request.put(paramName, Json.toJson(this));
        }
        Task.start(() -> {
            String ret = HttpHelper.post(url, request);
        });
        return true;
    }

    /// <summary>
    /// 普通发布
    /// </summary>
    /// <param name="url">服务器路径</param>
    /// <param name="data">参数配置</param>
    /// <returns></returns>
    public boolean Publish(String url, Map<String, Object> data) {
        if (Strings.isBlank(url) || data == null) return false;
        Record dat = Record.parse(this);
        Record request = Record.parse(data).mapping(dat);
        Task.startNew(() -> {
            HttpHelper.post(url, request);
        });
        return true;
    }

    public void increaseRunTimes() {
        this.runtimes++;
    }
    public void decreaseLimitTimes() {
        this.limittimes-- ;
    }
    public String getId() {
        return id;
    }

    public ScheduleJob setId(String id) {
        this.id = id;
        return this;
    }

    public String getJobname() {
        return jobname;
    }

    public ScheduleJob setJobname(String jobname) {
        this.jobname = jobname;
        return this;
    }

    public String getGroupname() {
        return groupname;
    }

    public ScheduleJob setGroupname(String groupname) {
        this.groupname = groupname;
        return this;
    }

    public Date getStarttime() {
        return starttime;
    }

    public ScheduleJob setStarttime(Date starttime) {
        this.starttime = starttime;
        return this;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public String getCronexpress() {
        return cronexpress;
    }

    public void setCronexpress(String cronexpress) {
        this.cronexpress = cronexpress;
    }

    public String getState() {
        return state;
    }

    public ScheduleJob setState(String state) {
        this.state = state;
        return this;
    }

    public String getData() {
        return data;
    }

    public ScheduleJob setData(String data) {
        this.data = data;
        return this;
    }

    public String getType() {
        return type;
    }

    public ScheduleJob setType(String type) {
        this.type = type;
        return this;
    }

    public int getEnable() {
        return enable;
    }

    public ScheduleJob setEnable(int enable) {
        this.enable = enable;
        return this;
    }

    public String getCmdcode() {
        return cmdcode;
    }

    public ScheduleJob setCmdcode(String cmdcode) {
        this.cmdcode = cmdcode;
        return this;
    }

    public String getEventype() {
        return eventype;
    }

    public ScheduleJob setEventype(String eventype) {
        this.eventype = eventype;
        return this;
    }

    public int getEvenvalue() {
        return evenvalue;
    }

    public ScheduleJob setEvenvalue(int evenvalue) {
        this.evenvalue = evenvalue;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public ScheduleJob setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getModule() {
        return module;
    }

    public ScheduleJob setModule(String module) {
        this.module = module;
        return this;
    }

    public int getKeepsize() {
        return keepsize;
    }

    public ScheduleJob setKeepsize(int keepsize) {
        this.keepsize = keepsize;
        return this;
    }

    public String getDescr() {
        return descr;
    }

    public ScheduleJob setDescr(String descr) {
        this.descr = descr;
        return this;
    }

    public int getRuntimes() {
        return runtimes;
    }

    public ScheduleJob setRuntimes(int runtimes) {
        this.runtimes = runtimes;
        return this;
    }

    public String getBreakrule() {
        return breakrule;
    }

    public ScheduleJob setBreakrule(String breakrule) {
        this.breakrule = breakrule;
        return this;
    }

    public int getWatchoption() {
        return watchoption;
    }

    public ScheduleJob setWatchoption(int watchoption) {
        this.watchoption = watchoption;
        return this;
    }

    public Date getLasttime() {
        return lasttime;
    }

    public ScheduleJob setLasttime(Date lasttime) {
        this.lasttime = lasttime;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public ScheduleJob setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getLimittimes() {
        return limittimes;
    }

    public void setLimittimes(int limittimes) {
        this.limittimes = limittimes;
    }
    public int getWorkday() {
        return workday;
    }

    public void setWorkday(int workday) {
        this.workday = workday;
    }
    public boolean workDay(){
        return this.workday!=0;
    }
    public boolean offDay(){
        return this.workday==0;
    }
}
