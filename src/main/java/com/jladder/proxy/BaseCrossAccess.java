package com.jladder.proxy;

import com.jladder.data.AjaxResult;
import com.jladder.data.ReStruct;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.hub.WebHub;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.net.http.HttpHelper;
import com.jladder.web.WebContext;
import com.jladder.web.WebReply;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/// <summary>
/// 基本实现的CrossAccess
/// </summary>
public class BaseCrossAccess implements ICrossAccess {

    private ThreadPoolExecutor pool= new ThreadPoolExecutor(1, 2, 1000, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());

    /// <summary>
    /// 验证请求
    /// </summary>
    /// <param name="config">配置信息</param>
    /// <param name="type">验证类型,强制使用的验证类型</param>
    /// <param name="data">请求数据</param>
    /// <param name="header">请求头数据</param>
    /// <param name="mode">入口模式，和type区别是，一个加密验证类型，一个是用户请求的handler</param>
    /// <returns></returns>
    public AjaxResult<Record, CrossAccessAuthInfo> DoAuth(HttpServletRequest request,ProxyConfig config, Record data, Record header, String type, String mode) {
        return new AjaxResult<Record, CrossAccessAuthInfo>().setData(data);
    }

    /// <summary>
    /// 转换数据操作
    /// </summary>
    /// <param name="data">请求数据</param>
    /// <param name="config">配置</param>
    /// <param name="env">环境编号</param>
    /// <param name="header">请求头</param>
    /// <returns></returns>
    public ReStruct<Record, Record> DoTransition(Record data, ProxyConfig config, int env, Record header) {




        return new ReStruct<Record, Record>(true, data, header);
    }

    /// <summary>
    /// 路径转换
    /// </summary>
    /// <param name="oldPath"></param>
    /// <param name="config"></param>
    /// <param name="data"></param>
    /// <param name="header"></param>
    /// <returns></returns>
    public Receipt<String> DoPath(String oldPath, ProxyConfig config, Record data, Record header) {
        return new Receipt(true).setData(oldPath);
    }


    /// <summary>
    /// 验证接口权限
    /// </summary>
    /// <param name="sign">标识</param>
    /// <param name="config">配置信息</param>
    /// <param name="key">密钥</param>
    /// <returns></returns>
    public CrossAccessAuthInfo CheckPermission(String sign, ProxyConfig config) {
        return CheckPermission(sign,config,null);
    }

    /// <summary>
    /// 验证接口权限
    /// </summary>
    /// <param name="sign">标识</param>
    /// <param name="config">配置信息</param>
    /// <param name="key">密钥</param>
    /// <returns></returns>
    public CrossAccessAuthInfo CheckPermission(String sign, ProxyConfig config, String key) {
        return CrossAccessAuthInfo.Ok();
    }

    /// <summary>
    /// 断言处理结果
    /// </summary>
    /// <param name="result">处理原结果</param>
    /// <param name="config">代理配置信息</param>
    /// <returns></returns>
    public Object AssertResult(AjaxResult result, ProxyConfig config, CrossAccessAuthInfo userinfo) {
        return result;
    }



    /***
     * 处理请求
     * @param request 请求对象
     * @param mode 处理模式 比如Paas，Md5，为前台请求服务
     * @param option 加密模式, 与处理模式相对应，但是两种模式可能一种解密
     * @param config 接口配置
     * @param requestData 请求数据
     * @param header 请求头
     * @param asyn 异步
     * @param env 调用环境
     * @param callback 回调处理
     * @param reply 是否进行response回复
     * @param follow 跟踪码
     * @return
     */
    public Object DoRequest(HttpServletRequest request, String mode, String option, ProxyConfig config, Record requestData, Record header, boolean asyn, int env, String callback, boolean reply, String follow) {

        if(request==null)request= WebContext.getRequest();
        String mothed = request!=null?request.getMethod():"Get";
        //回复数据对象
        Object rett = null;
        //判断请求方式
        if (Strings.hasValue(config.raw.method) && !"all".equals(config.raw.method.toLowerCase())  && !config.raw.method.equals(mothed)) {
            rett = WebHub.CrossAccess.AssertResult(new AjaxResult(456, "请求方式被拒绝[0124]"), config, null);
            if (reply) WebReply.reply(rett);
            return rett;
        }
        ///region 接口请求协议->解密
        AjaxResult<Record, CrossAccessAuthInfo> auth;
        String entype = config.raw.type;
        if (Strings.isBlank(entype) || entype.equals("0")) entype = option;
        if (entype.equals(option) || entype.equals("2")) {
            auth = WebHub.CrossAccess.DoAuth(request,config, requestData, header, entype, mode);
            if (auth.success) {
                requestData = auth.data;
            } else {
                rett = WebHub.CrossAccess.AssertResult(auth, config, null);
                if (reply) WebReply.reply(rett);
                return rett;
            }
        } else {
            rett = WebHub.CrossAccess.AssertResult(new AjaxResult(601, "请求模式不识别[0142]"), config, null);
            if (reply) WebReply.reply(rett);
            return rett;
        }
        ///endregion

        CrossAccessAuthInfo authinfo = (CrossAccessAuthInfo) auth.xdata;
        if (authinfo == null) {
            authinfo = new CrossAccessAuthInfo();
            authinfo.mode=mode;
            authinfo.authoption=option;
        } else {
            authinfo.mode = mode;
        }

        OnRequest(config, requestData, authinfo, header, env);

        if (asyn && Strings.hasValue(callback)) {
            Record finalRequestData = requestData;
            CrossAccessAuthInfo finalAuthinfo = authinfo;
            pool.execute(()->{
                try {
                    AjaxResult result = ProxyService.execute(config, finalRequestData, env, header, finalAuthinfo, null);
                    Object ret = WebHub.CrossAccess.AssertResult(result, config, finalAuthinfo);
                    if (Strings.isJson(callback,1)) {
                        Record cb = Record.parse(callback);
                        switch (cb.getString("type").toLowerCase())
                        {
                            case "httpget":
                                HttpHelper.request(cb.getString("url"), new Record("data", ret),"GET",null);
                                break;
                            case "httppost":
                                HttpHelper.request(cb.getString("url"), new Record("data", ret),"POST",null);
                                break;
                            case "websocket":
                                //WebSocketServerByFleck.Send(cb.GetString("user"), ret);
                                break;
                        }
                    } else {
                        //无动作
                        if (Regex.isMatch(callback, "^(false)|(0)$")) return;
                        //get回执
                        if (Regex.isMatch(callback, "^http[s]?:\\\\")) {
                            HttpHelper.request(callback, new Record("data", ret),"GET",null);
                            return;
                        }
                        //if (callback.Length == 32) WebSocketServerByFleck.Send(callback, ret);
                    }
                } catch (Exception e) {
    //                Logs.Write(new LogForError(e.Message) {
    //                    StackTrace =e.StackTrace,Module =config.Name,Type ="Proxy"
    //                }, LogOption.Error);
                }

        });
            rett = WebHub.CrossAccess.AssertResult(new AjaxResult(200, "请从回调中获取结果").setData(callback), config, authinfo);
            if (reply) WebReply.reply(rett);
            return rett;
        } else {
            AjaxResult result = ProxyService.execute(config, requestData, env, header, authinfo, follow);
            rett = WebHub.CrossAccess.AssertResult(result, config, authinfo);
            //执行回调的断言处理
            if (reply) WebReply.reply(rett);
            return rett;
        }
    }

    /// <summary>
    /// 获取聚合处理过程的集合
    /// </summary>
    /// <param name="config">接口配置</param>
    /// <param name="polymer">聚合配置</param>
    /// <param name="running">运行时信息</param>
    /// <returns></returns>
    public Receipt<List<ProxyFunctionInfo>> FetchPolymFunctions(ProxyConfig config, ProxyPolymer polymer, ProxyRunning running) {
        throw Core.makeThrow("未实现");
    }

    public AjaxResult DoPolymer(ProxyConfig config, ProxyPolymer polymer, Record result, ProxyRunning running) {
        throw Core.makeThrow("未实现");
    }

    public void UpdateWhiteList() {

    }

    /// <summary>
    /// 是否在白名单中
    /// </summary>
    /// <param name="config"></param>
    /// <returns></returns>
    public  boolean IsWhite(ProxyConfig config) {
        return true;
    }


    ///region 各类事件
    public boolean OnConfig(ProxyConfig config) {
        return true;
    }

    /// <summary>
/// 最初开始事件
/// </summary>
/// <param name="mode">模式</param>
/// <param name="tag"></param>
/// <param name="version">版本</param>
/// <param name="requestData"></param>
/// <param name="header"></param>
/// <returns></returns>
    public boolean OnStart(String mode, String tag, Record requestData, Record header, String version) {
        return true;
    }

    /// <summary>
    /// 开始处理请求
    /// </summary>
    /// <param name="config">接口服务配置</param>
    /// <param name="requestData">请求数据</param>
    /// <param name="header">请求头数据</param>
    /// <param name="isdebug">是否为调试模式</param>
    /// <returns></returns>
    public boolean OnRequest(ProxyConfig config, Record requestData, CrossAccessAuthInfo userinfo, Record header, int env) {
        return true;
    }

    /// <summary>
    /// 结果事件
    /// </summary>
    /// <param name="result">处理结果</param>
    /// <param name="config">接口服务配置</param>
    /// <param name="header"></param>
    /// <param name="uuid"></param>
    /// <param name="userinfo">认证权限</param>
    /// <param name="paramData"></param>
    /// <returns></returns>
    public AjaxResult OnResult(AjaxResult result, ProxyConfig config, Record paramData, Record header, String uuid, CrossAccessAuthInfo userinfo) {
        return result;
    }

    /// <summary>
    /// 开始处理过程事件
    /// </summary>
    /// <param name="config">接口服务配置</param>
    /// <param name="requestData">请求数据</param>
    /// <param name="header">请求头</param>
    /// <param name="userinfo">认证信息</param>
    /// <returns></returns>
    public AjaxResult OnCall(ProxyConfig config, Record requestData, Record header, CrossAccessAuthInfo userinfo) {
        return null;
    }
}