package com.jladder.proxy;

import com.jladder.data.AjaxResult;
import com.jladder.data.ReStruct;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ICrossAccess{

    /**
     * 清理权限缓存
     * @param sign 密钥标识
     * @return void
     * @author YiFeng
     */

    public void   clearCache(String sign);
    /**
     * 验证接口权限
     * @param sign 标识
     * @param config 配置信息
     * @return com.jladder.proxy.CrossAccessAuthInfo
     * @author YiFeng
     */

    CrossAccessAuthInfo checkPermission(String sign, ProxyConfig config);

    /**
     * 验证接口权限
     * @param sign 标识
     * @param config 配置信息
     * @param key 密钥
     * @return com.jladder.proxy.CrossAccessAuthInfo
     * @author YiFeng
     */

    CrossAccessAuthInfo checkPermission(String sign, ProxyConfig config,String key);


    /**
     * 断言请求结果
     * @param result 返回原结果
     * @param config 代理配置信息
     * @param auth 用户信息
     * @return java.lang.Object
     * @author YiFeng
     */

    Object assertResult(AjaxResult result, ProxyConfig config, CrossAccessAuthInfo auth);


    /**
     * 更新白名单
     * @return void
     * @author YiFeng
     */
    void updateWhiteList();

    /**
     * 是否在白名单中
     * @param config 代理配置
     * @return boolean
     * @author YiFeng
     */

    boolean isWhite(ProxyConfig config);


    /**
     * 处理请求
     * @param request 请求对象
     * @param mode 模式名称
     * @param option 认证方式
     * @param config 配置信息
     * @param data 请求数据
     * @param header 请求头
     * @param asyn 是否进行异步
     * @param env 调用环境
     * @param callback 回调信息
     * @param reply 是否进行reponse回复
     * @param follow 追踪点
     * @return java.lang.Object
     * @author YiFeng
     */

    Object doRequest(HttpServletRequest request,String mode, String option, ProxyConfig config, Record data, Record header, boolean asyn, int env, String callback, boolean reply, String follow);



    /**
     * 获取多组聚合的调用信息列表
     * @param config 配置信息
     * @param polymer 多组聚合对象
     * @param running 运行时信息
     * @return com.jladder.data.Receipt<java.util.List<com.jladder.proxy.ProxyFunctionInfo>>
     * @author YiFeng
     */

    Receipt<List<ProxyFunctionInfo>> fetchPolymFunctions(ProxyConfig config, ProxyPolymer polymer, ProxyRunning running);

    /***
     * 处理多组聚合的结果
     * @param config 配置信息
     * @param polymer 多组聚合对象
     * @param result 数据结果
     * @param running 运行时信息
     * @return com.jladder.data.AjaxResult
     * @author YiFeng
     */
    AjaxResult doPolymer(ProxyConfig config,ProxyPolymer polymer, Record result, ProxyRunning running);

    ///region 各类事件结构

    /**
     * 配置组装后事件
     * @param config 代理配置
     * @return boolean
     * @author YiFeng
     */
    boolean onConfig(ProxyConfig config);


    /**
     * 最初开始事件
     * @param mode 模式
     * @param tag
     * @param requestData
     * @param header
     * @param version 版本
     * @return boolean
     * @author YiFeng
     */
    boolean onStart(String mode,String tag,Record requestData, Record header, String version);


    /**
     * 请求事件
     * @param config 配置信息
     * @param requestData 请求信息
     * @param userinfo 用户信息
     * @param header 请求头信息
     * @param env 调用环境
     * @return boolean
     * @author YiFeng
     */
    boolean onRequest(ProxyConfig config, Record requestData, CrossAccessAuthInfo userinfo, Record header, int env);


    /**
     * 验证请求
     * @param request
     * @param config 配置信息
     * @param data 请求数据
     * @param header 请求头数据
     * @param type 验证类型,强制使用的验证类型
     * @param mode 入口模式，和type区别是，一个加密验证类型，一个是用户请求的handler
     * @return com.jladder.data.AjaxResult<com.jladder.data.Record,com.jladder.proxy.CrossAccessAuthInfo>
     * @author YiFeng
     */

    AjaxResult<Record,CrossAccessAuthInfo> doAuth(HttpServletRequest request,ProxyConfig config, Record data, Record header, String type,String mode);

    /**
     * 转换数据操作
     * @param data 请求数据
     * @param config 配置
     * @param env 环境编号
     * @param header 请求头
     * @return com.jladder.data.ReStruct<com.jladder.data.Record,com.jladder.data.Record>
     * @author YiFeng
     */

    ReStruct<Record, Record> doTransition(Record data, ProxyConfig config, int env, Record header);


    /**
     * 代理请求路径纠正
     * @param oldPath
     * @param config
     * @param data
     * @param header
     * @return com.jladder.data.Receipt<java.lang.String>
     * @author YiFeng
     */

    Receipt<String> doPath(String oldPath, ProxyConfig config, Record data, Record header);


    /**
     * 处理过程事件
     * @param config 接口服务配置
     * @param requestData 请求数据
     * @param header 请求头
     * @param userinfo 认证信息
     * @return com.jladder.data.AjaxResult 如果返回结果不为null,即为拦截返回
     * @author YiFeng
     */

    AjaxResult onCall(ProxyConfig config, Record requestData, Record header,CrossAccessAuthInfo userinfo);





    /***
     * 结果事件
     * @param result 处理结果
     * @param config 接口服务配置
     * @param paramData
     * @param header
     * @param uuid
     * @param userinfo 认证权限
     * @return com.jladder.data.AjaxResult
     * @author YiFeng
     */

    AjaxResult onResult(AjaxResult result, ProxyConfig config,Record paramData,Record header,String uuid, CrossAccessAuthInfo userinfo);

    //endregion



}
