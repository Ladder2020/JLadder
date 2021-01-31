package com.jladder.actions.impl;
import com.jladder.data.*;
import com.jladder.datamodel.IDataModel;
import com.jladder.db.DaoSeesion;
import com.jladder.db.IDao;
import com.jladder.db.SqlText;
import com.jladder.db.enums.DbDialectType;
import com.jladder.hub.DataHub;
import com.jladder.hub.LatchHub;
import com.jladder.lang.Core;
import com.jladder.lang.Security;
import com.jladder.lang.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/// <summary>
/// 数据寄存器
/// </summary>
public class LatchAction
{
    /// <summary>
    /// 操作锁
    /// </summary>
    private static Object Locked = new Object();

    /// <summary>
    /// 全局实例
    /// </summary>
    public static LatchAction Instance = new LatchAction();
    /// <summary>
    /// 一级缓存存储
    /// </summary>
    private static final Map<String, Object> RamData = new HashMap<String, Object>();




    /// <summary>
    /// 缓存策略是否可用
    /// </summary>
    public boolean Enable = false;



//        /// <summary>
//        /// 计时器
//        /// </summary>
//        private Timer Timer { get; set; } = new Timer();




    /// <summary>
    /// 安装方法
    /// </summary>
    private void install()
    {
//            Files.CreateDirectory(LatchHub.CacheFileDir);
        Enable = true;
    }


    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="dir">缓存文件目录</param>
    public static void init(String dir)
    {
        if (Strings.isBlank(dir)) Instance.install();
        else
        {
            LatchHub.CacheFileDir = dir;
            Instance.install();
        }
    }

    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="latchs">锁存器列表</param>
//        public void Init(IEnumerable<DbLatch> latchs)
//        {
//            Instance.Install();
//        }

    /// <summary>
    /// 设置分页数据
    /// </summary>
    /// <param name="dm">数据模型</param>
    /// <param name="pageResult">分页结果集</param>
    public static void setPageData(IDataModel dm, BasicPageResult pageResult)
    {
//            if (dm.Raw.CacheItems.IsBlank() || pageResult == null || pageResult.records.IsBlank()) return;
//            if (!Instance.Enable) return;
//            switch (dm.Raw.CacheItems)
//            {
//                case "CndCache":
//                    DataHub.WorkCache.AddLatchDataCache(dm.Raw.Name, dm.PagingSqlText(dm.DbDialect, pageResult.pager).ToString().Md5() + "page", pageResult,LatchHub.StayTime);
//                    break;
//                case "CndRAM":
//                    RamData.Put(dm.Raw.Name + "_" + dm.PagingSqlText(dm.DbDialect, pageResult.pager)?.ToString().Md5() + "page",
//                        pageResult);
//                    break;
//                case "CndLocal":
//                    try
//                    {
//                        File.WriteAllText(
//                            LatchHub.CacheFileDir + "/" + dm.Raw.Name + "_" +
//                            dm.PagingSqlText(dm.DbDialect, pageResult.pager)?.ToString().Md5() + "page.dat",
//                            Json.ToJson(pageResult));
//                    }
//                    catch (Exception e)
//                    {
////                        Logs.Write("CacheLocal:" + e.Message);
//                        Logs.Write(new LogForError(e.Message, "SetPageData").SetModule("LatchAction").SetStackTrace(e.StackTrace),LogOption.Exception);
//                    }
//                    break;
//                case "ExternCondition":
//                        if (LatchHub.Extern != null)
//                            LatchHub.Extern.SetCache(dm.Raw.Name + "_" + dm.PagingSqlText(dm.DbDialect, pageResult.pager)?.ToString().Md5() + "page", pageResult);
//                    break;
//            }
    }

    /// <summary>
    /// 获取分页的缓存
    /// </summary>
    /// <param name="dm"></param>
    /// <param name="pager"></param>
    /// <returns></returns>
    public static BasicPageResult getPageData(IDataModel dm, Pager pager)
    {

        if (Strings.isBlank(dm.Raw.CacheItems)) return new PageResult(404);
        //return null;
        if (!Instance.Enable) return new PageResult(405);
        BasicPageResult pageresult = null;
        if (dm.DbDialect == DbDialectType.Default)
        {
//                dm.DbDialect = (dm.Dao == null ? (DaoSeesion.GetDao(dm.Conn)?.Dialect) : dm.Dao.Dialect);
            if (dm.Dao == null)
            {
                IDao dao = DaoSeesion.GetDao(dm.Conn);
                dm.DbDialect = dao.getDialect();
            }
            else
            {
                dm.DbDialect = dm.Dao.getDialect();
            }
        }
        SqlText sqlText=new SqlText();//缓存的sql文
        switch (dm.Raw.CacheItems)
        {
            case "CndCache":
                sqlText = dm.PagingSqlText(dm.DbDialect, pager);
                pageresult =(BasicPageResult) DataHub.WorkCache.getLatchDataCache(dm.Raw.Name, Security.md5(sqlText.toString()) + "page");
                break;
            case "CndRAM":
                sqlText = dm.PagingSqlText(dm.DbDialect, pager);
                if (RamData.containsKey(dm.Raw.Name + "_" + Security.md5(sqlText.toString())))
                {
                    pageresult = (BasicPageResult)RamData.get(dm.Raw.Name + "_" + Security.md5(sqlText.toString()) + "page");
                }
                break;
            case "CndLocal":
//                try
//                {
//                    sqlText = dm.PagingSqlText(dm.DbDialect, pager);
//                    if (Files.IsExistFile(LatchHub.CacheFileDir + "/" + dm.Raw.Name + "_" + sqlText?.ToString().Md5() + "page.dat"))
//                    {
//                        var content = File.ReadAllText(LatchHub.CacheFileDir + "/" + dm.Raw.Name + "_" + sqlText?.ToString().Md5() + "page.dat");
//                        pageresult = Json.ToObject<BasicPageResult>(content);
//                    }
//                }
//                catch (Exception e)
//                {
////                        Logs.Write("CacheLocal:" + e.Message);
//                    Logs.Write(new LogForError(e.Message, "GetPageData").SetModule("LatchAction").SetStackTrace(e.StackTrace),LogOption.Exception );
//                }

                break;
            case "Table":
//                if (com.jladder.core.lang.Files.exist(LatchHub.CacheFileDir + "/" + dm.Raw.Name + "_alldata.dat"))
//                {
//                    var content = File.ReadAllText(LatchHub.CacheFileDir + "/" + dm.Raw.Name + "_alldata.dat");
//                    if (content.IsBlank() || content == "null")
//                    {
//                        SetDataForAllTable(dm.Raw.Name);
//                        break;
//                    }
//                    var rs = Json.ToObject<List<Record>>(content);
//                    if(rs==null)break;
//                    rs = rs.Find(dm);
//                    if (rs.IsBlank()) break;
//                    pager.SetRecordCount(rs.Count);
//                    rs = rs.Paging(pager);
//                    pageresult = new BasicPageResult
//                    {
//                        statusCode = 200,
//                        pager = pager,
//                        records = rs
//                    };
//                }
//                else
//                {
//                    SetDataForAllTable(dm.Raw.Name);
//                }
                break;
            //全表内存，可用于模版的交换数据
            case "TableRAM":
//                if(RamData.containsKey(dm.Raw.Name))
//                {
//                    List<Record> rows = (List<Record>)RamData.get(dm.Raw.Name);
//                    if (rows == null) break;
//                    rows = rows.find(dm);
//                    if (rows.IsBlank()) break;
//                    pager.SetRecordCount(rows.Count);
//                    rows = rows.Paging(pager);
//                    pageresult = new BasicPageResult
//                    {
//                        statusCode = 200,
//                        pager = pager,
//                        records = rows
//                    };
//                }
                break;
            case "DataModel":
                if (Strings.hasValue(dm.Raw.Data))
                {
//                    List<Record> rs = Json.toObject(dm.Raw.Data, new TypeReference<List<Record>>() { });
//                    rs = rs.Find(dm);
//                    if (rs.IsBlank()) break;
//                    pager.SetRecordCount(rs.Count);
//                    rs = rs.Paging(pager);
//                    pageresult = new BasicPageResult
//                    {
//                        statusCode = 200,
//                        pager = pager,
//                        records = rs
//                    };
                }
                break;
            case "ExternCondition":
//                if (LatchHub.Extern != null)
//                {
//                    sqlText = dm.PagingSqlText(dm.DbDialect, pager);
//                    pageresult = LatchHub.Extern.GetCache<BasicPageResult>(dm.Raw.Name + "_" + sqlText?.ToString().Md5() + "page");
//                }
                break;
            case "ExternAll":

//                if (LatchHub.Extern != null)
//                {
//                    var rs = LatchHub.Extern.GetTableCache(dm.Raw.Name);
//                    if (rs == null)
//                    {
//                        LatchHub.Extern.SetTableCache(dm.Raw.Name);
//                        break;
//                    }
//                    rs = rs.Find(dm);
//                    if (rs.IsBlank()) break;
//                    pager.SetRecordCount(rs.Count);
//                    rs = rs.Paging(pager);
//                    pageresult = new BasicPageResult
//                    {
//                        statusCode = 200,
//                        pager = pager,
//                        records = rs
//                    };
//                }

                break;

        }
        //if(DataHub.SqlDebug)Logs.Write(new LogForSql(sqlText.Text, dm.Conn).SetType("latch").SetData(sqlText.Parameters).SetEnd(),LogOption.Sql);
        if(pageresult!=null)return pageresult;
        return new BasicPageResult(404);
    }

    /// <summary>
    /// 获取缓存数据
    /// </summary>
    /// <param name="dm">数据模型</param>
    /// <returns></returns>

    public static Receipt getData(IDataModel dm)
    {
        if (Strings.isBlank(dm.Raw.CacheItems)) return new Receipt(false, "无缓存策略");
        if(!Instance.Enable && dm.Raw.CacheItems!= "DataModel") return new Receipt(false,"缓存策略禁用");
        List<Record> rs = null;
        switch (dm.Raw.CacheItems)
        {
            case "CndCache":
                //rs = (List<Record>) DataHub.WorkCache.GetLatchDataCache(dm.Raw.Name, dm.SqlText()?.ToString().Md5());
                break;
            case "CndRAM":
//                    if (RamData.ContainsKey(dm.Raw.Name + "_" + dm.SqlText()?.ToString().Md5()))
//                    {
//                        rs = (List<Record>)RamData[dm.Raw.Name + "_" + dm.SqlText()?.ToString().Md5()];
//                    }
                break;
            case "CndLocal":
//                    if (Files.IsExistFile(LatchHub.CacheFileDir + "/" + dm.Raw.Name + "_" + dm.SqlText()?.ToString().Md5() + ".dat"))
//                    {
//                        var content = File.ReadAllText(LatchHub.CacheFileDir + "/" + dm.Raw.Name + "_" + dm.SqlText()?.ToString().Md5() + ".dat");
//                        rs = Json.ToObject<List<Record>>(content);
//                    }
                break;
            case "Table":
//                    if (Files.IsExistFile(LatchHub.CacheFileDir + "/" + dm.Raw.Name + "_alldata.dat"))
//                    {
//                        var content = File.ReadAllText(LatchHub.CacheFileDir + "/" + dm.Raw.Name + "_alldata.dat");
//                        if (content.IsBlank() || content == "null")
//                        {
//                            SetDataForAllTable(dm.Raw.Name);
//                            break;
//                        }
//                        rs = Json.ToObject<List<Record>>(content);
//                        rs = rs.Find(dm);
//                    }
//                    else
//                    {
//                        SetDataForAllTable(dm.Raw.Name);
//                    }
                break;
            case "TableRAM":
//                    if (RamData.ContainsKey(dm.Raw.Name))
//                    {
//                        rs = RamData.Get<List<Record>>(dm.Raw.Name);
//                        rs = rs.Find(dm);
//                    }
                break;
            case "DataModel":
//                    if (dm.Raw.Data.HasValue())
//                    {
//                        rs = Json.FromJson<List<Record>>(dm.Raw.Data);
//                        rs = rs.Find(dm);
//                    }
                break;
            case "ExternAll":
//                    if (LatchHub.Extern != null)
//                    {
//                        rs = LatchHub.Extern.GetTableCache(dm.Raw.Name);
//                        if (rs == null)
//                        {
//                            LatchHub.Extern.SetTableCache(dm.Raw.Name);
//                            break;
//                        }
//                        rs = rs.Find(dm);
//                    }
                break;
            case "ExternCondition":
//                    if (LatchHub.Extern != null)
//                    {
//                        rs = LatchHub.Extern.GetCache<List<Record>>(dm.Raw.Name + "_" + dm.SqlText().toString().Md5());
//                    }
                break;

        }
        return rs == null ? new Receipt(false, "存储数据为空") : new Receipt().setData(rs);
    }
    /// <summary>
    /// 获取缓存数据
    /// </summary>
    /// <param name="dm">数据模型</param>
    /// <param name="rs">记录集</param>
    /// <returns></returns>
    public static void setData(IDataModel dm, List<Record> rs)
    {
        if (Strings.isBlank(dm.Raw.CacheItems) || Core.isEmpty(rs)) return;
        if (!Instance.Enable) return;
        switch (dm.Raw.CacheItems)
        {
            case "CndCache":
                //DataHub.WorkCache.AddLatchDataCache(dm.Raw.Name, dm.SqlText().ToString().Md5(), rs,LatchHub.StayTime);
                break;
            case "CndRAM":
                //RamData.Put(dm.Raw.Name + "_" + dm.SqlText()?.ToString().Md5(), rs);
                break;
            case "CndLocal":
//                    try
//                    {
//                        File.WriteAllText(LatchHub.CacheFileDir + "/" + dm.Raw.Name + "_" + dm.SqlText()?.ToString().Md5() + ".dat",
//                            Json.ToJson(rs));
//                    }
//                    catch (Exception e)
//                    {
////                        Logs.Write("CacheLocal:" + e.Message);
//                        Logs.Write(new LogForError(e.Message, "SetData").SetModule("LatchAction").SetStackTrace(e.StackTrace), LogOption.Exception);
//                    }
                break;
            case "TableRAM":
                RamData.put(dm.Raw.Name, rs);
                break;

            case "ExternCondition":
//                    if (LatchHub.Extern != null)
//                    {
//                        LatchHub.Extern.SetCache(dm.Raw.Name + "_" + dm.SqlText().toString().Md5(), rs);
//                    }
                break;
//                case "Table":
//                    try
//                    {
//                        File.WriteAllText(CacheFileDir + "/" + dm.Raw.Name + "_alldata.dat", Json.ToJson(rs));
//                    }
//                    catch (Exception e)
//                    {
//                        Logs.Write("CacheLocal:" + e.Message);
//                    }
//                    break;
        }

    }

    /// <summary>
    /// 设置静态全表数据
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="rs">记录集</param>
    public static void setDataForAllTable(String tableName, List<Record> rs)
    {
//            if (rs == null)
//            {
//                Task.Factory.StartNew(() =>
//                {
//                    var dm = DaoSeesion.GetDataModel(tableName);
//                    var dao = new Dao(dm.Conn);
//                    try
//                    {
//                        var data = dao.Query(dm.SqlText());
//                        if (!data.IsBlank()) File.WriteAllText(LatchHub.CacheFileDir + "/" + tableName + "_alldata.dat", Json.ToJson(data));
//                    }
//                    catch (Exception e)
//                    {
//                        //Logs.Write("CacheLocal:" + e.Message);
//                        Logs.Write(new LogForError(e.Message, "SetDataForAllTable").SetModule("LatchAction").SetStackTrace(e.StackTrace), LogOption.Exception);
//
//                    }
//                    finally
//                    {
//                        dao.Close();
//                    }
//                });
//                return;
//            }
//            try
//            {
//                File.WriteAllText(LatchHub.CacheFileDir + "/" + tableName + "_alldata.dat", Json.ToJson(rs));
//            }
//            catch (Exception e)
//            {
//                Logs.Write(new LogForError(e.Message, "SetDataForAllTable").SetModule("LatchAction").SetStackTrace(e.StackTrace), LogOption.Exception);
//            }
    }

    /// <summary>
    /// 扫描活动的表
    /// </summary>
    /// <param name="second">间隔时间(秒)</param>

//        public static void Activate(int second)
//        {
//            Instance.Timer.Stop();
//            Instance.Timer.Elapsed += Instance.OnTimer;
//            Instance.Timer.Interval = second*1000;
//            Instance.Timer.Enabled = true;
//            Instance.Timer.Start();
//        }
//        /// <summary>
//        /// 时间轮询事件
//        /// </summary>
//        /// <param name="sender"></param>
//        /// <param name="e"></param>
//        private void OnTimer(Object sender, ElapsedEventArgs e)
//        {
//            var dao = new Dao(DataHub.TemplateConn);
//            var tableName = Configs.GetString("_ScanWrokingTable") ?? "sys_scanworking";
//            string tn = DataHub.TemplateTableName;
//            var device = Core.GenUuid();
//            try
//            {
//                lock (Locked)
//                {
//                    //获取第一条记录的sql语句
//                    var onesql = "(" +
//                                 dao.PagingSqlText(
//                                     new SqlText("select c.id from (select id from sys_scanworking where ishandle=0) c"),
//                                     new Pager(1, 1)) + ")";
//                    //更新顶上一条数据
//                    var count = dao.Update(tableName, new Record("device", device).Put("ishandle", 1),
//                        new Cnd("*id", onesql));
//                    if (count > 0)
//                    {
//                        var bean = dao.Fetch(tableName, new Cnd("device", device));
//                        while (bean != null && count > 0)
//                        {
//                            //是实表
//                            if (bean.GetInt("type") == 0)
//                            {
//                                var rs =
//                                    dao.Query(new SqlText("select name from " + tn + " where cacheitems is not null and depends like '%" +
//                                              bean.GetString("tablename") + "%'"));
//                                if (!rs.IsBlank())
//                                {
//                                    rs.ForEach(x => RemoveLatch(x.GetString("name")));
//                                }
//                            }
//                            //是模版
//                            else
//                            {
//                                RemoveLatch(bean.GetString("tablename"));
//                            }
//                            dao.Delete(tableName, new Cnd("device", device));
//                            count = dao.Update(tableName, new Record("device", device).Put("ishandle", 1),
//                                new Cnd("*id", onesql));
//                            bean = dao.Fetch(tableName, new Cnd("device", device));
//                        }
//                    }
//                }
//            }
//            catch (Exception error)
//            {
//                Logs.Write(new LogForError(error.Message, "SetDataForAllTable").SetModule("LatchAction").SetStackTrace(error.StackTrace), LogOption.Exception);
//            }
//            finally
//            {
//                dao.Close();
//            }
//        }

    /// <summary>
    /// 添加缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="cryptosql">sql密文</param>
    /// <param name="data">模版</param>
    private static void addCache(String key, String cryptosql, Object data)
    {
//            var cache = Cache.Instance;
//            cache.Set("YiFeng_DataCache_" + key + "_" + cryptosql, data, TimeSpan.FromMinutes(LatchHub.StayTime));
    }

    /// <summary>
    /// 添加缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="cryptosql">sql密文</param>
    /// <param name="data">模版</param>
    /// <param name="staytime">停留时间</param>
    public static void addCache(String key, String cryptosql, Object data, int staytime)
    {
//            var cache = Cache.Instance;
//            cache.Set("YiFeng_DataCache_" + key + "_" + cryptosql, data, TimeSpan.FromMinutes(staytime));
    }

    /// <summary>
    /// 获取缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="cryptosql">sql密文</param>
    /// <returns></returns>
    private static Object getCache(String key, String cryptosql)
    {
        return null;
//            var cache = Cache.Instance;
//            return cache.Get("YiFeng_DataCache_" + key + "_" + cryptosql);
    }

    /// <summary>
    /// 移除缓存
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="cryptosql">sql密文</param>
    private static void removeCache(String key, String cryptosql)
    {
        //            if (cryptosql.IsBlank())
        //            {
        //                var cache = Cache.Cache.Instance;
        //                IDictionaryEnumerator cacheEnum = (cache as MemoryCache).Count;
        //
        //
        //
        //                while (cacheEnum.MoveNext())
        //                {
        //                    if (key.StartsWith("YiFeng_DataCache_" + key)) cache.Remove(key);
        //                }
        //            }
        //            else
        //            {
        //                HttpRuntime.Cache.Remove("YiFeng_DataCache_" + key + "_" + cryptosql);
        //            }
    }

    /// <summary>
    /// 清理关联表的缓存
    /// </summary>
    /// <param name="tableName">实表名</param>
    /// <returns></returns>

    public static void clearLatch(String tableName)
    {
        if(!Instance.Enable)return;
        return;
        //throw Core.makeThrow("未实现");
//
//            //web请求作用域
//            if (WebScope.Configs!=null && WebScope.Configs.IsLatch == false) return;
//            Task.Factory.StartNew(() =>
//            {
//                lock (Locked)
//                {
//                    var dependcaches = DataHub.WorkCache.GetLatchDependsCache(DataHub.TemplateTableName + DataHub.TemplateConn + tableName);
//
//                    if (dependcaches == null)
//                    {
//                        string tn = DataHub.TemplateTableName;
//                        var dao = new Dao(DataHub.TemplateConn);
//                        try
//                        {
//                            var rs = dao.Query(new SqlText("select name from " + tn + " where cacheitems is not null and depends like '%" + tableName + "%'"));
//                            if (!rs.IsBlank())
//                            {
//                                rs.ForEach(x => RemoveLatch(x.GetString("name")));
//                                DataHub.WorkCache.AddLatchDependsCache(DataHub.TemplateTableName + DataHub.TemplateConn + tableName, rs.Select(x=>x.GetString("name")).ToList());
//                            }
//                            else
//                            {
//                                DataHub.WorkCache.AddLatchDependsCache(DataHub.TemplateTableName + DataHub.TemplateConn + tableName,new List<string>());
//                            }
//                            return;
//                        }
//                        catch (Exception)
//                        {
//                            return;
//                        }
//                        finally
//                        {
//                            dao.Close();
//                        }
//                    }
//                    else
//                    {
//                        dependcaches.ForEach(x =>
//                        {
//                            RemoveLatch(x);
//                        });
//                    }
//                    return;
//                }
//            });
    }


    /// <summary>
    /// 移除缓存
    /// </summary>
    /// <param name="key">键名</param>
    private static boolean removeLatch(String key)
    {
        try
        {
            throw Core.makeThrow("未实现");
//                if(LatchHub.Extern!=null)LatchHub.Extern.RemoveNotice(key);
//                //清除文件
//                DirectoryInfo di = new DirectoryInfo(LatchHub.CacheFileDir);
//                var files = di.GetFiles(key + "_*.dat");
//                if (!files.IsBlank())
//                {
//                    files.ForEach(x => Files.DeleteFile(x.FullName));
//                }
//                //清除二级缓存
//                DataHub.WorkCache.RemoveLatchDataCache(key);
//                //清除一级缓存
//                List<string> keys = new List<string>();
//                RamData.ForEach(x =>
//                {
//                    if (x.Key.StartsWith(key + "_")) keys.Add(x.Key);
//                });
//                keys.ForEach(x => RamData.Remove(x));
//                Logs.Write("CacheLocal:"+ key + "清理缓存文件-" + files?.Length+"，一级缓存:"+ keys?.Count,LogOption.Need);
//                return true;
        }
        catch (Exception e)
        {
            return false;
        }

    }


}

