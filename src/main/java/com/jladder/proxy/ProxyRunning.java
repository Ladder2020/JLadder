package com.jladder.proxy;

import com.jladder.data.Record;
import com.jladder.lang.Core;
import com.jladder.lang.Stopwatch;
import com.jladder.logger.LogFoRequest;

public class ProxyRunning {
    public String follow;
    /// <summary>
    /// 调用环境代码
    /// </summary>
    public int envcode;
    /// <summary>
    /// 本次请求的唯一码
    /// </summary>
    public String uuid = Core.genUuid();
    /// <summary>
    /// 配置信息
    /// </summary>
    public ProxyConfig config;
    /// <summary>
    /// 认证信息
    /// </summary>
    public CrossAccessAuthInfo authinfo;

    /// <summary>
    /// 跟踪堆栈信息
    /// </summary>
    public Record trace = new Record();

    /// <summary>
    /// 参数数据
    /// </summary>
    public Record paramdata  = new Record();
    /// <summary>
    /// 请求头信息
    /// </summary>
    public Record header;


    /// <summary>
    /// 原始进入数据
    /// </summary>
    public Record data;
    /// <summary>
    /// Hash值
    /// </summary>
    public String hashcode;

    /// <summary>
    /// 静态数据状态
    /// </summary>
    public boolean hashstatus = false;

    /// <summary>
    /// 请求的Log信息
    /// </summary>
    public LogFoRequest requesting;

    /// <summary>
    /// 计数器
    /// </summary>
    public Stopwatch stopWatch  = new Stopwatch();

    public ProxyRunning(Record data, CrossAccessAuthInfo authinfo, Record header, ProxyConfig config, int env, String follow) {
        this.header=header;
        this.data=data;
        this.authinfo=authinfo;
        this.config=config;
        this.envcode=env;
        this.follow=follow;
        if(this.header==null)this.header=new Record();
    }
}
