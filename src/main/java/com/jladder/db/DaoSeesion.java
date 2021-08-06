package com.jladder.db;
import com.jladder.actions.WebScope;
import com.jladder.datamodel.DataModelForMap;
import com.jladder.datamodel.DataModelForMapRaw;
import com.jladder.datamodel.IDataModel;
import com.jladder.db.enums.DbDialectType;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.hub.DataHub;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DaoSeesion {

    private static Map<String, DataSource> datasources = new HashMap<>();

    private static Map<String,DbDialectType> dialects=new HashMap<>();

    public static Connection getConnection(String conn){
        try{
            DataSource datasource = datasources.get(conn);
            if(datasource!=null) return datasource.getConnection();
            else{
                Properties pro = new Properties();
                pro.load(DaoSeesion.class.getClassLoader().getResourceAsStream( conn+".properties" ) );

                //  3.获取DataSource
//                DataSource ds = DruidDataSourceFactory.createDataSource(pro);
//                datasources.put(conn,ds);
//                return ds.getConnection();
            }
        }catch (Exception e){

        }
        return null;
    }
    /// <summary>
    /// 获取数据库方言
    /// </summary>
    /// <param name="conn">连接器名称</param>
    /// <returns></returns>
    public static DbDialectType GetDialect(String conn)
    {
        if(Strings.isBlank(conn))conn="defaultDatabase";
        if(dialects.containsKey(conn))return dialects.get(conn);
        IDao dao = new Dao(conn);
        try{
            if(dao==null)return DbDialectType.Default;
            dialects.put(conn,dao.getDialect());
            return dao.getDialect();
        }finally {
            dao.close();
        }
        //throw Core.makeThrow("未实现");
        //return GetDao(conn).Dialect;
    }
    /// <summary>
    /// 新建一个数据库连接
    /// </summary>
    /// <param name="key">键名，如果为空，以新建默认连接返回</param>
    /// <returns></returns>
    public static IDao NewDao(String key)
    {
        return Strings.isBlank(key) ? new Dao("defaultDatabase") : new Dao(key);
    }
    /// <summary>
    /// 新建一个数据库连接(默认)
    /// </summary>
    /// <returns></returns>
    public static IDao NewDao()
    {
        return new Dao("defaultDatabase");
    }
    /// <summary>
    /// 获取DataModel实例
    /// </summary>
    /// <param name="tableName">键名</param>
    /// <param name="param">参数数据</param>
    /// <returns></returns>
    public static IDataModel getDataModel(String tableName, String param)
    {
        return getDataModel(null, tableName, param);
    }

    /// <summary>
    /// 获取DataModel实例
    /// </summary>
    /// <param name="dao">数据库链接操作对象</param>
    /// <param name="tableName">键名</param>
    /// <param name="param">参数数据</param>
    /// <returns></returns>
    public static IDataModel getDataModel(IDao dao, String tableName, String param)
    {
        if (Strings.isBlank(tableName)) return null;
        DataModelForMapRaw raw = DataHub.WorkCache.getDataModelCache(tableName);
        if (raw == null || (Strings.isBlank(raw.Type) || (Regex.isMatch("table|sql", raw.Type)) && (raw.AllColumns == null || raw.AllColumns.size() < 1)))
        {
            DataModelForMap dm = DataHub.gen(tableName,true);
            if (dm != null)
            {
                raw = dm.getRaw();
                DataHub.WorkCache.addDataModelCache(tableName,raw);
                return Strings.isBlank(param) ? WebScope.MappingConn(dm,tableName)  : WebScope.MappingConn(new DataModelForMap(raw, param),tableName);
            }
            dm = new DataModelForMap(dao, tableName, param);
            DataHub.WorkCache.addDataModelCache(tableName, dm.getRaw());
            WebScope.MappingConn(dm, tableName);
            return dm;
        }
        else
        {
//            if (raw.Scheme.HasValue() && raw.Scheme != "db")
//            {
//                var change = DataHub.IsChangeed(tableName, out var info);
//                if (change != 1) return WebScope.MappingConn(new DataModelForMap(raw, param),tableName);
//                DataHub.Load(info);
//                var dm = DataHub.Gen(tableName);
//                if (dm == null) return null;
//                DataHub.WorkCache.AddDataModelCache(tableName, dm.Raw);
//                return WebScope.MappingConn(param.IsBlank() ? dm : new DataModelForMap(dm.Raw, param),tableName);
//            }
            return WebScope.MappingConn(new DataModelForMap(raw, param),tableName);
        }
    }

    /// <summary>
    /// 获取DataModel实例
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <returns></returns>
    public static IDataModel getDataModel(String tableName)
    {
        DataModelForMapRaw raw = DataHub.WorkCache.getDataModelCache(tableName);
        if (raw == null || (Strings.isBlank(raw.Type) || (Regex.isMatch("table|sql", raw.Type)) && (raw.AllColumns == null || raw.AllColumns.size() < 1)))
        {
//            var dm = DataHub.Gen(tableName);
//            if (dm != null)
//            {
//                DataHub.WorkCache.AddDataModelCache(tableName, dm.Raw);
//                return WebScope.MappingConn(dm, tableName);
//            }
            DataModelForMap dm= new DataModelForMap(tableName);
            DataHub.WorkCache.addDataModelCache(tableName, dm.getRaw());
            return WebScope.MappingConn(dm, tableName);
        }
        else
        {
//            if (raw.Scheme.HasValue() && raw.Scheme != "db")
//            {
//                DataModelInfo info;
//                var change = DataHub.IsChangeed(tableName, out info);
//                if (change == 1)
//                {
//                    DataHub.Load(info);
//                    var dm = DataHub.Gen(tableName);
//                    if (dm == null) return null;
//                    DataHub.WorkCache.AddDataModelCache(tableName, dm.Raw);
//                    return WebScope.MappingConn(dm, tableName);
//                }
//            }
           return WebScope.MappingConn(new DataModelForMap(raw,""),tableName);
        }
    }

    /***
     * 获取IDao
     * @return
     */
    public static IDao GetDao() {
        return GetDao("");
    }

    /***
     * 获取IDao
     * @param conn 连接器
     * @return Dao实例
     */
    public static IDao GetDao(String conn) {
        //System.out.println("此应维修[0164]");
        return Strings.isBlank(conn) ? new Dao("defaultDatabase") : new Dao(conn);
    }
}
