package com.jladder.logger;

import com.jladder.actions.impl.EnvAction;
import com.jladder.hub.WebHub;
import com.jladder.lang.*;
import com.jladder.net.http.HttpHelper;
import com.jladder.web.ArgumentMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class LogFoRequest {
    /// <summary>
    /// 唯一标识
    /// </summary>
    public String uuid = Core.genUuid();

    /// <summary>
    /// 请求类型
    /// </summary>
    public String type;

    /// <summary>
    /// 开始时间
    /// </summary>
    public Date starttime;
    /// <summary>
    /// 结束时间
    /// </summary>
    public Date endtime;
    /// <summary>
    /// 持续时长
    /// </summary>
    public String duration;
    /// <summary>
    /// 请求参数
    /// </summary>
    public String request;
    /// <summary>
    /// 站点
    /// </summary>
    public String site;
    /// <summary>
    /// 服务器
    /// </summary>
    public String server;
    /// <summary>
    /// 路径
    /// </summary>
    public String url;
    /// <summary>
    /// 请求方式
    /// </summary>
    public String method;
    /// <summary>
    /// 来源地
    /// </summary>
    public String referer;

    /// <summary>
    /// 映射路径
    /// </summary>
    public String path;

    /// <summary>
    /// 用户信息
    /// </summary>
    public String userinfo;
    /// <summary>
    /// 关联服务用户
    /// </summary>
    public String withwho;
    /// <summary>
    /// 请求标记
    /// </summary>
    public String sessionid;

    /// <summary>
    /// 请求标记
    /// </summary>
    public String requestmark;

    /// <summary>
    /// 请求头信息
    /// </summary>
    public String header;

    /// <summary>
    /// 访问者IP地址
    /// </summary>
    public String ip;

    /// <summary>
    /// 创建日志
    /// </summary>
    public String createdate = Times.getDate();
    /// <summary>
    /// 处理结果
    /// </summary>
    public String result;

    /// <summary>
    /// 异常数量
    /// </summary>
    public int exceptions;


    /// <summary>
    /// 标签,用于模型名称
    /// </summary>
    public String tag;
    public LogFoRequest(){
        site = WebHub.SiteName;
        starttime = new Date();
        userinfo = EnvAction.GetEnvValue("username");
//        //去处地址栏参数
        if (Strings.hasValue(url))
        {
            url = Regex.replace(url, "\\?[\\w\\W]*$", "");
        }
        ip = HttpHelper.getIp();
    }
    /// <summary>
    /// 初始化
    /// </summary>
    public LogFoRequest(HttpServletRequest request)
    {
        site = WebHub.SiteName;
        method = request.getMethod();
        referer = request.getHeader("referer");
        url = request.getRequestURL().toString();
        this.request = Json.toJson(ArgumentMapping.getRequestParams(request));
        starttime = new Date();
        //userinfo = EnvAction.GetEnvValue("username");
//        //去处地址栏参数
        if (Strings.hasValue(url))
        {
            url = Regex.replace(url, "\\?[\\w\\W]*$", "");
        }
        requestmark = Core.toString(request.getAttribute("__requestmark__"));
        ip = HttpHelper.getIp();
    }
    /// <summary>
    /// 设置访问路径
    /// </summary>
    /// <param name="path"></param>
    /// <returns></returns>
    public LogFoRequest setPath(String path)
    {
        path = path;
        return this;
    }
    /// <summary>
    /// 设置请求信息
    /// </summary>
    /// <param name="data"></param>
    /// <returns></returns>
    public LogFoRequest setRequest(Object data)
    {
        request = Json.toJson(data);
        return this;
    }
    /// <summary>
    /// 设置请求信息
    /// </summary>
    /// <param name="data"></param>
    /// <returns></returns>
    public LogFoRequest setReqeust(Object data)
    {
        request = Json.toJson(data);
        return this;
    }
    /// <summary>
    /// 设置结束点
    /// </summary>
    /// <returns></returns>
    public LogFoRequest setEnd()
    {
        endtime = Times.now();
        if(starttime==null){
            duration = "0ms";
            return this;
        }
        long time = (endtime.getTime() - starttime.getTime());
        duration = time+"ms";
//        TimeSpan ts = endtime.subtract(StartTime);
//        Duration = Math.round(ts., 0) + "ms";
//        var tag = WebScope.GetTag();
//        if (tag.HasValue()) Tag = tag;
        return this;
    }
    /// <summary>
    /// 设置请求头
    /// </summary>
    /// <param name="data"></param>
    /// <returns></returns>
    public LogFoRequest setHeader(Object data)
    {
        header = Json.toJson(data);
        return this;
    }
    /// <summary>
    /// 增长异常数量
    /// </summary>
    /// <returns></returns>
    public LogFoRequest increaseException()
    {
        exceptions ++;
        return this;
    }
}
