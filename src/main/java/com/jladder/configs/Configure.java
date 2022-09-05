package com.jladder.configs;
import com.alibaba.fastjson.JSONObject;
import com.jladder.data.Record;
import com.jladder.hub.DataHub;
import com.jladder.lang.*;
import com.jladder.lang.func.Func2;
import com.jladder.lang.func.Tuple3;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 动态计算型配置
 */
public class Configure{
    /// <summary> 内部配置字典 </summary>
    private static ConcurrentMap<String, ConfigItem> _configs = new ConcurrentHashMap<>();
    /// <summary>
    /// 获取实例
    /// </summary>
    private final static  Configure Instance=new Configure();


    /// <summary>
    /// 自动重新加载
    /// </summary>
    public boolean AutoReload= false;
    /// <summary>
    /// 自动更新配置的时间项，以分钟计划
    /// </summary>
    public int ReloadTime = 10;
    /// <summary>
    /// 配置文件的最后修改时间
    /// </summary>
    private Date FileDateTime = Times.convert("0000-00-00");
    /// <summary>
    /// 上次处理时间
    /// </summary>
    private Date LastDateTime = Times.now();
    /// <summary>
    /// 配置文件的地址
    /// </summary>
    public static String Location;
    /// <summary>
    /// 上次远程配置数据
    /// </summary>
    public static String LastRemoteData;


    /// <summary>
    /// 默认初始化
    /// </summary>
    private Configure()
    {
//            if(HttpContext.Current!=null) Put("ServicePath", HttpContext.Current.Server.MapPath(""));
//            Put("BasicPath", AppDomain.CurrentDomain.BaseDirectory);
//            var st = System.Reflection.MethodBase.GetCurrentMethod().DeclaringType.Namespace;
    }

    /// <summary>
    /// 从文件加载配置
    /// </summary>
    /// <param name="path"></param>
    /// <returns></returns>
    public static boolean loadSettingsFromFile(String path){
        if (Strings.isBlank(path)) return false;
        Location = path;
        if (path.startsWith("http"))
        {
            return loadSettingRemote(path);
        }
        File file = null;
        try {
            file = new ClassPathResource(path).getFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String remote = "";
        if (Files.exist(file))
        {
            Location = path;
            String itemStr = Json.FromFile(file);
            Record settings = Json.toObject(itemStr,Record.class);
            if (settings == null) return false;
            for (Map.Entry<String,Object> keyValuePair : settings.entrySet())
            {
                if ("configserver".equals(keyValuePair.getKey().toLowerCase() ))
                {
                    remote = keyValuePair.getValue().toString();
                    continue;
                }
                Configure.put(keyValuePair.getKey(), keyValuePair.getValue(), SourceDirection.ConfigFile);
            }
            DataHub.setFromConfig();
            if (Strings.hasValue(remote))
            {
                String finalRemote = remote;
                Task.start(new Runnable() {
                    @Override
                    public void run() {
                        loadSettingRemote(finalRemote);
                    }
                });
            }
            return true;
        }
        return false;
    }
    /// <summary>
    ///
    /// </summary>
    /// <param name="url"></param>
    /// <returns></returns>

    /**
     * 加载远程配置文件c
     * @param url 路径
     * @return
     */
    public static boolean loadSettingRemote(String url)
    {
        throw Core.makeThrow("未实现");
//        var ret = HttpHelper.GetData(url);
//        if (ret.IsBlank()) return false;
//        var content = ret.ToString(Encoding.UTF8);
//        if (content.IsBlank() || content.Equals(LastRemoteData)) return true;
//        LastRemoteData = content;
//        var encrypt = GetString("configEncrypt");
//        if (encrypt.HasValue())
//        {
//            try
//            {
//                var sk = encrypt.Split("|");//分割加密方式和密钥
//                var mode = sk.First();
//                var secret = sk.Last();
//                switch (mode)
//                {
//                    case "DES":
//                        content = Security.DecryptByDes(content, secret);
//                        break;
//                    case "AES":
//                        content = Security.DecryptByAES(content, secret);
//                        break;
//                    case "SM4":
//                        content = Security.DecryptBySM4(content, secret);
//                        break;
//                    case "BASE64":
//                        content = Security.DecryptByBase64(content, Encoding.UTF8);
//                        break;
//                }
//            }
//            catch (Exception e)
//            {
//                Logs.Write(new LogForError(e.Message).SetStackTrace(e.StackTrace), LogOption.Error);
//            }
//        }
//
//
//        content = Json.DeleteComments(content);
//        var settings = Json.ToObject<Record>(content);
//        if (settings == null) return false;
//        foreach (var keyValuePair in settings)
//        {
//            Configs.Put(keyValuePair.Key, keyValuePair.Value, SourceDirection.ConfigFile);
//        }
//        DataHub.SetFromConfig();
//        return true;
    }

    /**
     * 重新加载配置项
     * @return
     */
    public static boolean reload()
    {
        if (Strings.isBlank(Location)) return false;
        return loadSettingsFromFile(Location);
    }
    /// <summary>
    /// 从web配置项加载配置(过期)
    /// </summary>
    /// <param name="configNode">节点名</param>
    /// <returns></returns>
    public static boolean loadSettingsFromWebConfig(String configNode)
    {
//            if (String.IsNullOrEmpty(configNode)) configNode = "Settings";
//            var itemname = ConfigurationManager.AppSettings[configNode];
//            if (itemname.Contains("~"))
//            {
//                itemname = HttpContext.Current.Server.MapPath(itemname);
//            }
//            if (File.Exists(itemname))
//            {
//                String itemStr = Json.fromFile(itemname);
//                var settings = Json.fromJson<Record>(itemStr);
//                if (settings == null) return false;
//                foreach (var keyValuePair in settings)
//                {
//                    Configs.Put(keyValuePair.Key, keyValuePair.Value, SourceDirection.ConfigFile);
//                }
//                return true;
//            }
        return false;
    }
    public static void clearCache(){
        List<String> deletes = new ArrayList<String>();
        _configs.forEach((k,v)->{
            if(SourceDirection.CaChe.equals(v.direct))deletes.add(k);
        });
        deletes.forEach(x->_configs.remove(x));
    }

    /**
     * 获取配置值
     * @param key 键名
     * @param clazz 类
     * @param <T> 泛型
     * @return
     */
    public static <T> T getValue(String key,Class<T> clazz){
        Tuple3<Boolean, String, ConfigItem> find = Collections.first(_configs, (k, v) -> k.toLowerCase().equals(key.toLowerCase()));
        if (!find.item1) return null;
        else{
            if (clazz.isAssignableFrom(String.class)){
                if (find.item3.Value instanceof String) return (T) find.item3.Value;
                else
                {
                    Object v = find.item3.Value.toString();
                    return (T) v;
                }
            }
            if(find.item3.Value instanceof JSONObject){
                T v =  ((JSONObject) find.item3.Value).toJavaObject(clazz);
                put(key,v,find.item3.direct);
                return v;
            }
            return (T)find.item3.Value;

        //如果不是类,基本数据类型
//        if (clazz.()) return (T) _configs[k].Value;
//        if (_configs[k].Value.GetType() == typeof (Dictionary<string, object>))
//            return ((Dictionary<string, object>) _configs[k].Value).ToClass<T>();
//        else
//        {
//            if (_configs[k].Value.GetType() == typeof (T)) return (T) _configs[k].Value;
//            return Record.Parse(_configs[k].Value).ToClass<T>();
//        }
        }
    }
    /// <summary>
    /// 获取配置的原值
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="ignoreCase">是否忽略大小写</param>
    /// <returns></returns>
    public static Object get(String key,boolean ignoreCase){
        if (ignoreCase == false){
            return _configs.containsKey(key) ? _configs.get(key).Value : null;
        }
        else{

            key = Collections.haveKey(_configs,key);
            return Strings.isBlank(key) ? null : _configs.get(key).Value;
        }
    }
    public static <T> T get(String key,Class<T> clazz, Func2<Object,T> fun){
        if(Strings.isBlank(key))return null;
        Object raw = get(key,false);
        if (raw == null) return null;
        if (raw.getClass().equals(clazz)) {
            return (T)raw;
        }
        T ret = null;
        try {
            if(fun!=null)ret = fun.invoke(raw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ret != null) put(key, ret,SourceDirection.Memory);
        return ret;
    }

    /**
     * 获取配置值(文本组)
     * @param key 键名
     * @return
     */
    public static String[] getStringList(String key){
        if (_configs.containsKey(key)) {
            ConfigItem dd = _configs.get(key);
            String jsonStr = Json.toJson(dd);
            return Json.toObject(jsonStr,String[].class);
        }
        else return null;
    }


    /**
     * 获取文本配置
     * @param key 键名
     * @return
     */
    public static String getString(String key)
    {
        return Configure.getValue(key,String.class);
    }


    /**
     * 获取整数配置
     * @param key 键名
     * @return
     */
    public static int getInt(String key){
        Integer val = Configure.getValue(key, Integer.class);
        if(val==null)return 0;
        return val;
    }
    public static int getInt(ConfigKey key){
        return getInt(key.getName());
    }
    public static String getString(ConfigKey key){
        return getString(key.getName());
    }
    public static boolean getBoolean(ConfigKey key){
        String name = key.getName();
        Object val = get(name,false);
        if(val==null)return false;
        if(val instanceof Boolean)return (Boolean)val;
        if(val instanceof Integer)return !val.equals(0);
        if(val instanceof String){
            if(val.equals(""))return false;
            if(val.equals("0"))return false;
            if(val.toString().equalsIgnoreCase("false"))return false;
            return true;
        }
        return true;
    }
    public static Configure put(ConfigKey key, Object value){
        return put(key.getName(),value);
    }

    /**
     * 向全局配置项中插入选项
     * @param prop 属性名
     * @param value 值
     * @return
     */
    public static Configure put(String prop, Object value) {
        return Configure.put(prop, value,SourceDirection.Memory);
    }
    /**
     * 放置配置
     * @param prop 属性名称
     * @param value 值
     * @param sd 来源方向
     * @return
     */
    public  static Configure put(String prop, Object value, SourceDirection sd){
        ConfigItem v = new ConfigItem(prop, value, sd);
        Tuple3<Boolean, String, ConfigItem> find = Collections.first(_configs, (x, y) -> Core.is(x.toLowerCase(), prop.toLowerCase()) );
        if(!find.item1)_configs.put(prop, v);
        else {
            _configs.put(find.item2,v);
        }
        return Configure.Instance;
    }
    public static boolean exist(String key){
        return exist(key,false);
    }

    /**
     * 是否存在配置
     * @param key 键名
     * @return
     */
    public static boolean exist(String key,boolean ignoreCase){
        if(!ignoreCase)return _configs.containsKey(key);
        boolean have =  _configs.containsKey(key);
        if(have)return true;
        Tuple3<Boolean, String, ConfigItem> find = Collections.first(_configs, (k, v) -> k.toLowerCase().equals(key.toLowerCase()));
        return find.item1;
    }

    /**
     * 获取项目的基本目录
     * @return
     */
    public static String getBasicPath(){
        try{
            String path = getString("ServicePath");
            if(Strings.hasValue(path))return path;
            path = getString("BasicPath");
            if(Strings.hasValue(path))return path;
            path = getString("_temp_base_dir_");
            if(Strings.hasValue(path))return path;
            path = new File(new ClassPathResource("./").getPath()).getCanonicalPath();
            put("_temp_base_dir_",path);
            return path;
        }catch (Exception e){
            return "./";
        }

    }
//    /// <summary>
//    /// 设置数据库默认
//    /// </summary>
//    /// <param name="provider">数据库驱动类</param>
//    /// <param name="dialect">数据库方言</param>
//    public static void SetDefaultDbInfo(DbProviderFactory provider, DbDialectType dialect)
//    {
//        Put("_ProviderFactory", provider);
//        Put("_DialectType", dialect);
//    }

    /**
     * 设置数据库默认
     * @param tableName
     * @param dbName
     */
    public static void setDataModelInfo(String tableName, String dbName){
        put("_TemplateTableName", tableName,SourceDirection.Application);
        put("_TemplateDbName", dbName,SourceDirection.Application);
    }


    public static String getNameSpace()
    {
        return getString("webnamespace");
    }
    /// <summary>
    /// 设置自动重载配置文件
    /// </summary>
    /// <param name="minutes"></param>
    public static void setAutoReload(int minutes)
    {
        if (minutes <= 0)
        {
            Instance.AutoReload = false;
        }
        else
        {
            Instance.AutoReload = true;
            Instance.ReloadTime = minutes;
        }
    }

    /// <summary>
    /// 自动纠正配置文件
    /// </summary>
    public static void judgeReload()
    {
//        if (Location.IsBlank() || !Instance.AutoReload) return;
//        //网络文件
//        if (Location.StartsWith("http"))
//        {
//            if ((DateTime.Now - Instance.LastDateTime).Minutes >= Instance.ReloadTime)
//            {
//                Task.Factory.StartNew(() => { LoadSettingsFromFile(Location); });
//                return;
//            }
//        }
//        else
//        {
//            FileInfo fi = new FileInfo(Location);
//            if (Instance.FileDateTime == DateTime.MinValue)
//            {
//                Instance.FileDateTime = fi.LastWriteTime;
//                return;
//            }
//
//            if (Instance.FileDateTime != fi.LastWriteTime)
//            {
//                Instance.FileDateTime = fi.LastWriteTime;
//                Task.Factory.StartNew(() => { LoadSettingsFromFile(Location); });
//                return;
//            }
//            if ((DateTime.Now - Instance.LastDateTime).Minutes >= Instance.ReloadTime)
//            {
//                Instance.LastDateTime = DateTime.Now;
//                Task.Factory.StartNew(() => { LoadSettingsFromFile(Location); });
//                return;
//            }
//        }
    }
}
