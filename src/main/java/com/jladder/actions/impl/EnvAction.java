package com.jladder.actions.impl;
import com.jladder.configs.Configure;
import com.jladder.configs.SourceDirection;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.lang.Collections;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.lang.func.Func2;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统环境变量
 */
public class EnvAction {
    /**
     * 环境变量的存储
     */
    public static Map<String, Func2<String,String>> Env=new HashMap<>();
    private static Record _envValues = new Record();


    /**
     * 设置配置项
     * @param name 属性名
     * @param value 数据值
     * @param token 密令
     * @return
     */
    public Receipt setConfig(String name, String value, String token){
        if (Strings.isBlank(name) || Strings.isBlank(token) || !Regex.isMatch(token, "^\\d*xzhy\\d*")){
            return new Receipt(false, "权限不足");
        }
        Configure.put(name, value, SourceDirection.Memory);
        return new Receipt();
    }
    /**
     * 重置数据库链接
     * @param server 服务配置
     * @param token 口令
     * @return
     */
    public Receipt resetDefaultDataBase(String server, String token){
        throw Core.makeThrow("系安全因素,未实现");
    }

    /**
     * 获取环境变量的值
     * @param key 键名
     * @return
     */
    public static String getEnvValue(String key){
        return getEnvValue(key,null);
    }


    /**
     * 获取环境变量的值
     * @param key 键名
     * @param arg 参数
     * @return
     */
    public static String getEnvValue(String key,String arg) {
        String k = Collections.haveKey(Env,key);;
        if (Strings.hasValue(k)) {
            try {
                return Env.get(k).invoke(arg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /***
     * 添加环境变量的值
     * @param key 键名
     * @param fun 回调函数
     */
    public static void putEnvValue(String key, Func2<String,String> fun){
        Env.put(key, fun);
    }

    /**
     * 环境变量的值
     * @param key 键名
     * @param value 数据值
     * @return
     */
    public static Object value(String key, Object value){
        if (Strings.isBlank(key)) return null;
        if (value == null) return _envValues.get(key);
        _envValues.put(key, value);
        return value;
    }
}
