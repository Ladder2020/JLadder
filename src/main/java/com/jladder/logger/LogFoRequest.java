package com.jladder.logger;

import com.jladder.Ladder;
import com.jladder.actions.impl.EnvAction;
import com.jladder.configs.Configure;
import com.jladder.hub.WebHub;
import com.jladder.lang.*;
import com.jladder.net.http.HttpHelper;
import com.jladder.web.ArgumentMapping;
import com.jladder.web.WebContext;
import com.jladder.web.WebScope;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Date;

public class LogFoRequest {
    /**
     * 唯一标识
     */
    public String uuid = Core.genUuid();

    /**
     * 请求类型
     */
    public String type;

    /**
     * 开始时间
     */
    public Date starttime;
    /**
     * 结束时间
     */
    public Date endtime;
    /**
     * 持续时长
     */
    public int duration;
    /**
     * 请求参数
     */
    public String request;
    /**
     * 站点
     */
    public String site;
    /**
     * 服务器
     */
    public String server;
    /**
     * 路径
     */
    public String url;
    /**
     * 请求方式
     */
    public String method;
    /**
     * 来源地
     */
    public String referer;

    /**
     * 映射路径
     */
    public String path;

    /**
     * 用户信息
     */
    public String userinfo;

    public String getWithwho() {
        return withwho;
    }

    public void setWithwho(String withwho) {
        this.withwho = withwho;
    }

    /**
     * 关联服务用户
     */
    public String withwho;
    /**
     * 请求标记
     */
    public String sessionid;

    /**
     * 请求标记
     */
    public String requestmark;

    /**
     * 请求头信息
     */
    public String header;

    /**
     * 访问者IP地址
     */
    public String ip;

    /**
     * 创建日期
     */
    public String createdate = Times.getDate();
    /**
     * 处理结果
     */
    public String result;

    /**
     * 异常数量
     */
    public int exceptions;
    /**
     * 环境，0:生成|1:开发|2:测试
     */
    private int env=0;
    /**
     * 标签,用于模型名称
     */
    public String tag;
    /**
     * 基线
     */
    public int baseline=0;

    public int getBaseline() {
        return baseline;
    }

    public void setBaseline(int baseline) {
        this.baseline = baseline;
    }



    public LogFoRequest(){
        site = Ladder.Settings().getSite();
        starttime = new Date();
        userinfo = EnvAction.getEnvValue("username");
//        //去处地址栏参数
        if (Strings.hasValue(url)){
            url = Regex.replace(url, "\\?[\\w\\W]*$", "");
        }
        ip = HttpHelper.getIp();
    }

    /**
     * 初始化
     * @param request 请求对象
     */
    public LogFoRequest(HttpServletRequest request){
        site = Ladder.Settings().getSite();
        method = request.getMethod();
        referer = request.getHeader("referer");
        url = request.getRequestURL().toString();
        this.request = Json.toJson(ArgumentMapping.getRequestParams(request));
        starttime = new Date();
        //去处地址栏参数
        if (Strings.hasValue(url)) {
            path = Regex.replace( Regex.replace(url, "\\?[\\w\\W]*$", ""),"http[s]?://[\\w\\.:]*?/","");
        }
        sessionid=request.getSession().getId();
        type="Controller";
        requestmark = WebContext.getMark(request);
        ip = HttpHelper.getIp();
    }

    /**
     * 设置访问路径
     * @param path 路径
     * @return
     */
    public LogFoRequest setPath(String path) {
        this.path = path;
        return this;
    }
    /**
     * 设置请求信息
     * @param data 数据对象
     * @return
     */
    public LogFoRequest setRequest(Object data){
        request = Json.toJson(data);
        return this;
    }

    /**
     * 设置结束点
     * @return
     */
    public LogFoRequest setEnd(){
        endtime = Times.now();
        if(starttime==null){
            duration = 0;
            return this;
        }
        long time = (endtime.getTime() - starttime.getTime());
        duration = (int)time;
        this.tag = WebContext.getTag();
        server = Configure.getString("_MachineInfo_IP_");
        if(Strings.isBlank(server)){
            server=Machine.getLocalIp();
            Configure.put("_MachineInfo_IP_",server);
        }
        if(Strings.isBlank(userinfo))userinfo = EnvAction.getEnvValue("username");
        env=Ladder.Settings().getEnv();
        return this;
    }

    /**
     * 设置请求头
     * @param data 请求头数据
     * @return
     */
    public LogFoRequest setHeader(Object data) {
        header = Json.toJson(data);
        return this;
    }
    /**
     * 增长异常数量
     * @return
     */
    public LogFoRequest increaseException()
    {
        exceptions ++;
        return this;
    }

    public int getEnv() {
        return env;
    }

    public LogFoRequest setEnv(int env) {
        this.env = env;
        return this;
    }
}
