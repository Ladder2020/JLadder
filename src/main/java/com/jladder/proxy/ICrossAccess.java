package com.jladder.proxy;

import com.jladder.data.AjaxResult;
import com.jladder.data.ReStruct;
import com.jladder.data.Receipt;
import com.jladder.data.Record;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ICrossAccess
{



    /// <summary>
    /// 验证接口权限
    /// </summary>
    /// <param name="sign">标识</param>
    /// <param name="config">配置信息</param>

    /// <returns></returns>
    CrossAccessAuthInfo CheckPermission(String sign, ProxyConfig config);
    /// <summary>
    /// 验证接口权限
    /// </summary>
    /// <param name="sign">标识</param>
    /// <param name="config">配置信息</param>
    /// <param name="key">密钥</param>
    /// <returns></returns>
    CrossAccessAuthInfo CheckPermission(String sign, ProxyConfig config,String key);


    /// <summary>
    /// 断言请求结果
    /// </summary>
    /// <param name="result">返回原结果</param>
    /// <param name="config">代理配置信息</param>
    /// <param name="userinfo">用户信息</param>
    /// <returns></returns>
    Object AssertResult(AjaxResult result, ProxyConfig config, CrossAccessAuthInfo auth);


    /// <summary>
    /// 更新白名单
    /// </summary>
    void UpdateWhiteList();
    /// <summary>
    /// 是否在白名单中
    /// </summary>
    /// <param name="config">代理配置</param>
    /// <returns></returns>
    boolean IsWhite(ProxyConfig config);

    /// <summary>
    /// 处理请求
    /// </summary>
    /// <param name="mode">模式名称</param>
    /// <param name="option">认证方式</param>
    /// <param name="config">配置信息</param>
    /// <param name="data">请求数据</param>
    /// <param name="header">请求头</param>
    /// <param name="asyn">是否进行异步</param>
    /// <param name="env">调用环境</param>
    /// <param name="callback">回调信息</param>
    /// <param name="reply">是否进行reponse回复</param>
    /// <param name="follow">追踪点</param>
    /// <param name="context">上线对象</param>
    Object DoRequest(HttpServletRequest request,String mode, String option, ProxyConfig config, Record data, Record header, boolean asyn, int env, String callback, boolean reply, String follow);


    /// <summary>
    /// 获取多组聚合的调用信息列表
    /// </summary>
    /// <param name="config">配置信息</param>
    /// <param name="polymer">多组聚合对象</param>
    /// <param name="running">运行时信息</param>
    /// <returns></returns>
    Receipt<List<ProxyFunctionInfo>> FetchPolymFunctions(ProxyConfig config, ProxyPolymer polymer, ProxyRunning running);
    /// <summary>
    /// 处理多组聚合的结果
    /// </summary>
    /// <param name="config">配置信息</param>
    /// <param name="polymer">多组聚合对象</param>
    /// <param name="data">数据结果</param>
    /// <param name="running">运行时信息</param>
    /// <returns></returns>
    AjaxResult DoPolymer(ProxyConfig config,ProxyPolymer polymer, Record result, ProxyRunning running);

    ///region 各类事件结构

    /// <summary>
    /// 配置组装后事件
    /// </summary>
    /// <param name="config">代理配置</param>
    /// <returns></returns>
    boolean OnConfig(ProxyConfig config);

    /// <summary>
    /// 最初开始事件
    /// </summary>
    /// <param name="mode">模式</param>
    /// <param name="tag"></param>
    /// <param name="version">版本</param>
    /// <param name="requestData"></param>
    /// <param name="header"></param>
    /// <returns></returns>
    boolean OnStart(String mode,String tag,Record requestData, Record header, String version);

    /// <summary>
    /// 请求事件
    /// </summary>
    /// <param name="config">配置信息</param>
    /// <param name="requestData">请求信息</param>
    /// <param name="userinfo">用户信息</param>
    /// <param name="header">请求头信息</param>
    /// <param name="env">调用环境</param>
    /// <returns></returns>
    boolean OnRequest(ProxyConfig config, Record requestData, CrossAccessAuthInfo userinfo, Record header, int env);

    /// <summary>
    /// 验证请求
    /// </summary>
    /// <param name="config">配置信息</param>
    /// <param name="type">验证类型,强制使用的验证类型</param>
    /// <param name="data">请求数据</param>
    /// <param name="header">请求头数据</param>
    /// <param name="mode">入口模式，和type区别是，一个加密验证类型，一个是用户请求的handler</param>
    /// <returns></returns>
    AjaxResult<Record,CrossAccessAuthInfo> DoAuth(HttpServletRequest request,ProxyConfig config, Record data, Record header, String type,String mode);

    /// <summary>
    /// 转换数据操作
    /// </summary>
    /// <param name="data">请求数据</param>
    /// <param name="config">配置</param>
    /// <param name="env">环境编号</param>
    /// <param name="header">请求头</param>
    /// <returns></returns>
    ReStruct<Record, Record> DoTransition(Record data, ProxyConfig config, int env, Record header);

    /// <summary>
    /// 代理请求路径纠正
    /// </summary>
    /// <param name="oldPath"></param>
    /// <param name="data"></param>
    /// <param name="config"></param>
    /// <param name="header"></param>
    /// <returns>TM是Data,TN是Head</returns>
    Receipt<String> DoPath(String oldPath, ProxyConfig config, Record data, Record header);


    /// <summary>
    /// 处理过程事件
    /// </summary>
    /// <param name="config">接口服务配置</param>
    /// <param name="requestData">请求数据</param>
    /// <param name="header">请求头</param>
    /// <param name="userinfo">认证信息</param>
    /// <returns>如果返回结果不为null,即为拦截返回</returns>
    AjaxResult OnCall(ProxyConfig config, Record requestData, Record header,CrossAccessAuthInfo userinfo);




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
    AjaxResult OnResult(AjaxResult result, ProxyConfig config,Record paramData,Record header,String uuid, CrossAccessAuthInfo userinfo);

    //endregion



}
