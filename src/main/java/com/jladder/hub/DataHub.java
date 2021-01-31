package com.jladder.hub;

import com.jladder.actions.impl.LatchAction;
import com.jladder.configs.Configs;
import com.jladder.datamodel.DataModelForMap;
import com.jladder.datamodel.DataModelInfo;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.logger.LogOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// <summary>
/// 数据模型管理中心
/// </summary>
public class DataHub {
    /// <summary>
    /// 实例
    /// </summary>
    public static DataHub Instance = new DataHub();
    public static IWorkCache WorkCache = new WorkCache();

    /// <summary>
    /// 实例化
    /// </summary>
    private DataHub()
    {


    }

    /// <summary>
    /// 数据库模版的
    /// </summary>
    public static String TemplateConn="defaultDatabase";//= Configs.GetString("_TemplateDbName") ?? "defaultDatabase";
    /// <summary>
    /// 表名
    /// </summary>
    public static String TemplateTableName="sys_data";//= Configs.GetString("_TemplateTableName") ?? "ladder.sys_data";
    /// <summary>
    /// 自定义表单的表名
    /// </summary>
    public static String MagicTableName="sys_magic";;// = Configs.GetString("_MagicTableName") ?? "ladder.sys_magic";
    /// <summary>
    /// 代理服务的数据库
    /// </summary>
    public static String ProxyConn="defaultDatabase";// = Configs.GetString("_ProxyDbName") ?? "defaultDatabase";
    /// <summary>
    /// 代理服务的数据库表名
    /// </summary>
    public static String ProxyTableName= "sys_service";// = Configs.GetString("_ProxyTableName") ?? "ladder.sys_service";




    /// <summary>
    /// 输出日志级别
    /// </summary>
    public static LogOption OutLogLevel = LogOption.Debug;
    /// <summary>
    /// 数据分析开关
    /// </summary>
    public static Boolean Analyz = true;

    /// <summary>
    /// 输出日志路径，支持${yyyy-MM-dd}格式
    /// </summary>
    public static String OutLogPath = "~/log/{yyyy-MM-dd}";
    //        /// <summary>
    //        /// 输出日志文件类型
    //        /// </summary>
    //        public static string OutLogFileName { get; set; } = "0.log";

    /// <summary>
    /// 日志上传的服务器
    /// </summary>
    public static String LogServer;

    /// <summary>
    /// 主模式
    /// </summary>
    public static String MainScheme = "db";

    /// <summary>
    /// sqlbug
    /// </summary>
    public static boolean SqlDebug;

    /// <summary>
    /// Sql超出毫秒数
    /// </summary>
    public static int SqlWarnTime= 60000;

    /// <summary>
    /// sqlbug
    /// </summary>
    public static String SqlDebugItem = "update,delete,insert,create";

    /// <summary>
    /// Json文件列表
    /// </summary>
    public static List<String> JsonFiles;

    /// <summary>
    /// xml文件列表
    /// </summary>
    public static List<String> XmlFiles;

    /// <summary>
    /// 配置存储区
    /// </summary>
    private static Map<String, DataModelInfo> DataModelConfigs =  new HashMap<String, DataModelInfo>();


//    public static WorkCache WorkCache = new WorkCache();

    /// <summary>
    /// 加载从配置的变量
    /// </summary>
    public static void SetFromConfig()
    {
        SqlDebug = Configs.GetString("sqldebug") == "True";
        int time = Configs.GetInt("SqlWarnTime");
        if (time > 0) SqlWarnTime = time;
        if (Strings.hasValue(Configs.GetString("sqldebugitem"))) SqlDebugItem = Configs.GetString("sqldebugitem");
        LogServer = Configs.GetString("logserver");
        OutLogPath = Strings.hasValue(Configs.GetString("log")) ? Configs.GetString("log") : "~/log/{yyyy-MM-dd}";

    }


    /// <summary>
    /// 初始化方法
    /// </summary>
    /// <param name="paths">文件路径</param>

    public static void Init(String ... paths)
    {
        TemplateConn = Core.or(Configs.GetString("_TemplateDbName"),"defaultDatabase");
        TemplateTableName = Core.or(Configs.GetString("_TemplateTableName") , "sys_data");
        Configs.put("_TemplateDbName", TemplateConn);
        Configs.put("_TemplateTableName", TemplateTableName);
        DataModelConfigs.clear();
        if (paths != null && paths.length > 0)
        {
            for (String path : paths)
            {
                if (Strings.isBlank(path)) continue;
                if (Regex.isMatch(path, "((\\.xml)|(\\.config))$"))
                {
                    LoadXmlFile(path);
                    continue;
                }
                if (Regex.isMatch(path, "((\\.js)|(\\.json))$"))
                {
                    LoadJsonFile(path);
                    continue;
                }
            }
        }
        XmlFiles = Core.or(XmlFiles ,Configs.GetValue("xmltemplates",List.class)) ;
        if (XmlFiles != null && XmlFiles.size() > 0)
        {
            for (String xmlFile : XmlFiles)
            {
                LoadXmlFile(xmlFile);
            }
        }
        LatchAction.init(null);
    }

    /// <summary>
    /// 初始化方法
    /// </summary>
    /// <param name="paths">文件路径</param>
    public static void Init(List<String> paths)
    {
//        if (paths == null || !paths.Any()) return;
//        Init(paths.ToArray());
    }


    /// <summary>
    /// 维护初始模型对数据库
    /// </summary>
    /// <param name="installFileNames">安装文件</param>
    /// <returns></returns>
    public static boolean BuildDataModelForDb(String ... installFileNames)
    {
        throw Core.makeThrow("未实现");
//        var dao = new Db.Dao(TemplateConn);
//        try
//        {
//            MainScheme = "db";
//            //dao.Create(typeof(DataModelTable), TemplateConn);
//            if (!dao.Exists(TemplateTableName)) return false;
//            //默认的
//            if (installFileNames.IsBlank())
//            {
//                var dt = new DataModelTable()
//                {
//                    Id = Core.GenUuid(),
//                    Updatetime = DateTime.Now,
//                    Name = "DataModel",
//                    Title = "数据模型",
//                    Sort = "核心",
//                    Type = "table",
//                    VisitTimes = "1",
//                    CacheItems = "",
//                    TableName = TemplateTableName,
//                    Enable = 1,
//                    Columns = Json.ToJson(dao.GetFieldInfo(TemplateTableName)),
//                    QueryForm = Json.ToJson(new Record("list", new List<Record>()
//                {
//                    new Record("title", "分属").Put("formname", "sort:like"),
//                            new Record("title", "键名").Put("formname", "name:like"),
//                            new Record("title", "标题").Put("formname", "title:like"),
//                }))
//                };
//                var tables = new DataModelTable()
//                {
//                    Id = Core.GenUuid(),
//                    Updatetime = DateTime.Now,
//                    Name = "tables",
//                    Title = "获取所有表",
//                    Sort = "核心",
//                    Type = "table",
//                    VisitTimes = "1",
//                    CacheItems = "",
//                    TableName = "information_schema.tables",
//                    Enable = 1,
//                    Columns = Json.ToJson(new List<Record>()
//                    {
//                        new Record("fieldname", "table_schema").Put("title", "模型名"),
//                                new Record("fieldname", "table_name").Put("title", "数据表名"),
//                                new Record("fieldname", "table_rows").Put("title", "数据记录数"),
//                                new Record("fieldname", "create_time").Put("title", "	创建时间"),
//
//                    }),
//                    QueryForm = Json.ToJson(new Record("list", new List<Record>()
//                {
//                    new Record("title", "模型名称").Put("formname", "table_schema:like"),
//                            new Record("title", "数据表名").Put("formname", "table_name:like"),
//                }))
//                };
//
//                if (dao.Count(TemplateTableName, new Cnd("name", "DataModel")) < 1)
//                {
//                    dao.Insert(TemplateTableName, dt);
//                    dao.Insert(TemplateTableName, tables);
//                }
//                else dao.Update(TemplateTableName, dt, new Cnd("name", "DataModel"));
//            }
//            else
//            {
////                    installFileNames.ForEach(x =>
////                    {
////                        var dts = DataModelTable.Parse(x);
////                        dts.ForEach(d =>
////                        {
////                            d.Id = Core.UUID();
////                            d.Updatetime = DateTime.Now;
////                            dao.Save(TemplateTableName, d, new Cnd("name", d.Name));
////                        });
////
////                    });
//                return false;
//            }
//            return true;
//        }
//        finally
//        {
//            dao.Close();
//        }
//        return false;
    }

    /// <summary>
    /// 加载xml文件
    /// </summary>
    /// <param name="path"></param>
    public static void LoadXmlFile(String path)
    {
//        if (path.Contains("~")) path = path.Replace("~", Configs.BasicPath());
//        path = Path.GetFullPath(path);
//        if (!Files.IsExistFile(path)) return;
//        FileInfo fi = new FileInfo(path);
//        var lasttime = fi.LastWriteTime;
//        XDocument xdoc = XDocument.Load(path);
//        if (xdoc.Root != null && xdoc.Root.HasElements)
//        {
//            var elements = xdoc.Root.Elements("mapping");
//            if (elements.Any())
//            {
//                foreach (var xElement in elements)
//                {
//                    var name = xElement.Attribute("name")?.Value;
//                    if (name.IsBlank()) continue;
//
//                    DataModelInfo dminfo = new DataModelInfo()
//                    {
//                        Path = path,
//                        LastWriteTime = lasttime,
//                        Type = "xml",
//                        Node = name,
//                    };
//                    var enable = xElement.Attribute("enable")?.Value;
//                    if (enable != null && enable.ToLower() == "false") dminfo.Enable = false;
//                    DataModelConfigs.Put(name, dminfo);
//                    DataHub.WorkCache.RemoveDataModelCache(name);
//                }
//            }
//            var includes = xdoc.Root.Elements("include");
//            if (includes.Any())
//            {
//                includes.ForEach(x => LoadXmlFile(x.Value.ToString()));
//            }
//
//        }
    }

    /// <summary>
    /// 加载json文件
    /// </summary>
    /// <param name="path"></param>

    public static void LoadJsonFile(String path)
    {

//        if (path.Contains("~")) path = path.Replace("~", Configs.BasicPath());
//        path = Path.GetFullPath(path);
//        if (!Files.IsExistFile(path)) return;
//        FileInfo fi = new FileInfo(path);
//        var lasttime = fi.LastWriteTime;
//        var content = Json.FromFile(path);
//        Record jMap = Json.ToObject<Record>(content);
//        jMap.ForEach(x =>
//                {
//                        var name = x.Key;
//        if (name.IsBlank()) return;
//        DataModelInfo dminfo = new DataModelInfo()
//        {
//            Path = path,
//            LastWriteTime = lasttime,
//            Type = "json",
//            Node = name,
//        };
//
//        var enable = ((JObject)x.Value)["enable"]?.ToString();
//        if (enable != null && enable.ToLower() == "false") dminfo.Enable = false;
//        DataModelConfigs.Put(name, dminfo);
//        DataHub.WorkCache.RemoveDataModelCache(name);
//            });


    }
    /// <summary>
    /// 通过配置信息加载
    /// </summary>
    /// <param name="info"></param>

    public static void Load(DataModelInfo info)
    {
        if (info == null) return;
        switch (info.Type)
        {
            case "xml":
                LoadXmlFile(info.Path);
                break;
            case "js":
            case "json":
                LoadJsonFile(info.Path);
                break;
        }
    }
    /// <summary>
    /// 添加xml文件
    /// </summary>
    /// <param name="path">文件路径</param>

    public static void AddXmlFile(String path)
    {
//        if (path.IsBlank()) return;
//        if (XmlFiles == null) XmlFiles = new List<string>();
//        XmlFiles.Add(path);
    }
    /// <summary>
    /// 获取数据模型的信息
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="ignore">是否忽略大小写</param>
    /// <returns></returns>

    public static DataModelInfo Get(String key, boolean ignore) {
        throw Core.makeThrow("未实现");
//        if (ignore)
//        {
//            key = DataModelConfigs.HaveKey(key);
//            if (key.IsBlank()) return null;
//            else return DataModelConfigs[key];
//        }
//        else
//        {
//            if (DataModelConfigs.ContainsKey(key)) return DataModelConfigs[key];
//            else return null;
//        }
    }
    /// <summary>
    /// 生成以Map为媒介的数据模型实例
    /// </summary>
    /// <param name="key"></param>
    /// <param name="ignore"></param>
    /// <returns></returns>

    public static DataModelForMap Gen(String key, boolean ignore)
    {
        throw Core.makeThrow("未实现");
//        var info = Get(key, ignore);
//        if (info == null) return null;
//        string path;
//        DataModelForMap ret = null;
//        switch (info.Type)
//        {
//            case "xml":
//                path = Path.GetFullPath(info.Path);
//                if (!Files.IsExistFile(path)) return null;
//                XDocument xdoc = XDocument.Load(path);
//                //                var xElement=xdoc.Root.XPathSelectElements("./mapping[name=\"" + info.Node + "\"]")?.First();
//                var xElement = xdoc?.Root?.Elements()?.FirstOrDefault(x => x.Attribute("name").Value == info.Node);
//                if (xElement == null) return null;
//                return new DataModelForMap(xElement);
//            case "json":
//                path = Path.GetFullPath(info.Path);
//                if (!Files.IsExistFile(path)) return null;
//                ret = new DataModelForMap();
//                ret.FromJsonFile(path, info.Node);
//                return ret;
//        }
//        return ret;
    }
    /// <summary>
    /// 检测模板文件是否已经改变
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="dminfo">模板信息</param>
    /// <returns></returns>
    public static int IsChangeed(String key, DataModelInfo dminfo)
    {
        throw Core.makeThrow("未实现");
//        dminfo = null;
//        var info = Get(key);
//        if (info == null) return -1;
//        if (!Files.IsExistFile(info.Path)) return -1;
//        FileInfo fi = new FileInfo(info.Path);
//        if (fi.LastWriteTime != info.LastWriteTime)
//        {
//            dminfo = info;
//            return 1;
//        }
//        return 0;
    }

}
