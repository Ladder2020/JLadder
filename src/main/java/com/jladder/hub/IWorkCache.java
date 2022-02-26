package com.jladder.hub;

import com.jladder.datamodel.DataModelForMapRaw;
import com.jladder.lang.func.Func1;
import com.jladder.lang.script.Script;
import com.jladder.proxy.ProxyConfig;

import java.util.Date;
import java.util.List;

public interface IWorkCache {

    //region 数据模型
    /**
     * 添加数据模版的缓存
     * @param key 模版键名
     * @param raw 模版的原型数据
     */
    void addDataModelCache(String key, DataModelForMapRaw raw);

    /**
     * 获取数据模版的缓存
     * @param key 模版键名
     * @return
     */
    DataModelForMapRaw getDataModelCache(String key);
    /**
     * 清除指定数据模版缓存
     * @param key
     */
    void removeDataModelCache(String key);

    /**
     * 清除全部数据模版缓存
     */
    void removeAllDataModelCache();
    //endregion
    //region 函数库
    /**
     * 添加函数库缓存
     * @param lib
     * @param raw
     */
    void addScriptCache(String lib, Script script);

    /**
     * 获取指定函数库缓存
     * @param lib
     * @return
     */
    Script getScriptCache(String lib);

    /**
     * 清除指定函数库缓存
     * @param lib 库名
     */
    void removeScriptCache(String lib);


    /**
     * 清除全部函数库缓存
     */
    void removeAllScriptCache();

    // endregion

    /// <summary>
    /// 添加路由代理的缓存
    /// </summary>
    /// <param name="key">模版键名</param>
    /// <param name="raw">模版的原型数据</param>
    void addProxyCache(String key, ProxyConfig raw);

    /// <summary>
    /// 获取路由代理的缓存
    /// </summary>
    /// <param name="key">模版键名</param>
    ProxyConfig getProxyCache(String key);

    /// <summary>
    /// 获取代理路由的缓存
    /// </summary>
    /// <param name="key">模版键名</param>
    public void removeProxyCache(String key);

    /// <summary>
    /// 清除全部代理路由缓存
    /// </summary>
    public void removeAllProxyCache();

    /// <summary>
    /// 添加数据锁存器缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="cryptosql">sql密文</param>
    /// <param name="data">模版</param>
    void addLatchDataCache(String key, String cryptosql, Object data,int stayTime);

    /// <summary>
    /// 添加锁存器依赖缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="names">模版</param>
    void addLatchDependsCache(String key, List<String> names);

    /// <summary>
    /// 添加依赖缓存
    /// </summary>
    /// <param name="key">键名</param>
    List<String> getLatchDependsCache(String key);

    /// <summary>
    /// 获取缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="cryptosql">sql密文</param>
    /// <returns></returns>
    Object getLatchDataCache(String key, String cryptosql);

    /// <summary>
    /// 移除缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="cryptosql">sql密文</param>
    void removeLatchDataCache(String key, String cryptosql);

    /// <summary>
    /// 添加功能模块缓存
    /// </summary>
    /// <typeparam name="T">类型</typeparam>
    /// <param name="key">键名</param>
    /// <param name="data">数据</param>
    /// <param name="module">模块</param>
    <T> void  addModuleCache(String key, T data, String module);

    /// <summary>
    /// 添加功能模块缓存
    /// </summary>
    /// <typeparam name="T">类型</typeparam>
    /// <param name="key">键名</param>
    /// <param name="data">数据</param>
    /// <param name="module">模块</param>

    /**
     * 添加模块缓存
     * @param key 键名
     * @param data 数据对象
     * @param module 模型名称
     * @param second 划过时间间隔
     * @param <T>
     */
    <T> void  addModuleCache(String key, T data, String module,int second);


    /**
     * 添加模块缓存
     * @param key 键名
     * @param data 数据对象
     * @param module 模型名称
     * @param invalid 失效日期
     * @param <T>
     */
    <T> void  addModuleCache(String key, T data, String module, Date invalid);

    /// <summary>
    /// 获取功能模块缓存
    /// </summary>
    /// <typeparam name="T">类型</typeparam>
    /// <param name="key">键名</param>
    /// <param name="module">模块</param>
    <T> T getModuleCache(String key, String module);
    /// <summary>
    /// 清理功能缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="module">模块</param>
    void removeModuleCache(String key, String module);
    boolean hasModuleCache(String key, String module);

    <T> T tryModuleCache(String key,String module,Class<T> clazz,int second, Func1<T> callback);

}
