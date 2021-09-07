package com.jladder.actions.impl;

import com.jladder.configs.Configs;
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

/// <summary>
/// 系统环境变量
/// </summary>
public class EnvAction
{

    /// <summary>
    /// 环境变量的存储
    /// </summary>
    public static Map<String, Func2<String,String>> Env=new HashMap<>();
    private static Record _envValues = new Record();
    /// <summary>
    /// 清除数据库模版缓存
    /// </summary>
    public static void ClearCache()
    {
//            var cache = HttpRuntime.Cache;
//            IDictionaryEnumerator cacheEnum = cache.GetEnumerator();
//            while (cacheEnum.MoveNext())
//            {
//                string key = cacheEnum.Key.ToString();
//                if (key.StartsWith("YiFeng_DataModel_"))
//                    cache.Remove(key);
//            }
    }



    /// <summary>
    /// 设置配置项
    /// </summary>
    /// <param name="name"></param>
    /// <param name="value"></param>
    /// <param name="token"></param>
    /// <returns></returns>
    public Receipt SetConfig(String name, String value, String token)
    {
        if (Strings.isBlank(name) || Strings.isBlank(token) || !Regex.isMatch(token, "^\\d*xzhy\\d*"))
        {
            return new Receipt(false, "权限不足");
        }
        Configs.put(name, value, SourceDirection.Memory);
        return new Receipt();
    }
    /// <summary>
    /// 重置数据库链接
    /// </summary>
    /// <param name="server">服务配置</param>
    /// <param name="token">口令</param>
    /// <returns></returns>
    public Receipt ResetDefaultDataBase(String server, String token)
    {
        throw Core.makeThrow("未实现");
//        if (Strings.isBlank(server) || Strings.isBlank(token) || !Regex.isMatch(token, "^\\d*xzhy\\d*"))
//        {
//            return new Receipt(false, "权限不足");
//        }
//        Record config = null;
//        String name = "";
//        if (Regex.isMatch(server, "^\\s*\\{[\\W\\w]*\\}\\s*$"))
//        {
//            config = Record.parse(server);
//            name = config.getString("name", true);
//        }
//        if (Strings.isBlank(name)) name = "defaultDatabase";
//        DbInfo dbinfo = Configs.GetValue(name, DbInfo.class);
//        if (config != null)
//        {
//            String serveraddress = config.getString("server", true);
//            if (serveraddress.HasValue())
//                dbinfo.Server = serveraddress;
//            var port = config.GetString("port", true);
//            if (port.HasValue())
//                dbinfo.Port = Int32.Parse(port);
//            var username = config.GetString("username", true);
//            if (username.HasValue())
//                dbinfo.Username = username;
//            var password = config.GetString("password", true);
//            if (password.HasValue())
//                dbinfo.Password = password;
//            var database = config.GetString("database", true);
//            if (database.HasValue())
//                dbinfo.Database = database;
//        }
//        else
//        {
//            dbinfo.Server = server;
//        }
//        Configs.Put(dbinfo.Name, dbinfo);
//        DaoSeesion.dao = null;
////            var cache = HttpRuntime.Cache;
////            IDictionaryEnumerator cacheEnum = cache.GetEnumerator();
////            while (cacheEnum.MoveNext())
////            {
////                string key = cacheEnum.Key.ToString();
////                if (key.StartsWith("YiFeng_DataModel_"))
////                    cache.Remove(key);
////            }
//        return new Receipt().SetData(dbinfo);
    }

    public static String GetEnvValue(String key){
        return GetEnvValue(key,null);
    }

    /// <summary>
    /// 获取环境变量的值
    /// </summary>
    /// <param name="key"></param>
    /// <param name="p"></param>
    /// <returns></returns>
    public static String GetEnvValue(String key,String p)
    {

        String k = Collections.haveKey(Env,key);;
        if (Strings.hasValue(k)) {
            try {
                return Env.get(k).invoke(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /// <summary>
    /// 添加环境变量的值
    /// </summary>
    /// <param name="key"></param>
    /// <param name="fun"></param>
    /// <returns></returns>
    public static void PutEnvValue(String key, Func2<String,String> fun)
    {
        Env.put(key, fun);
    }
    /// <summary>
    /// 环境变量的值
    /// </summary>
    /// <param name="key"></param>
    /// <param name="value"></param>
    /// <returns></returns>
    public static Object Value(String key, Object value)
    {
        if (Strings.isBlank(key)) return null;
        if (value == null) return _envValues.get(key);
        _envValues.put(key, value);
        return value;
    }
}
