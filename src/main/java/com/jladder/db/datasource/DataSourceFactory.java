package com.jladder.db.datasource;

import com.jladder.Ladder;
import com.jladder.db.DbInfo;
import com.jladder.db.datasource.impl.DataSourceByDruid;
import com.jladder.db.datasource.impl.DataSourceByEmpty;
import com.jladder.db.datasource.impl.DataSourceByHikari;
import com.jladder.lang.Collections;
import com.jladder.lang.Core;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.Serializable;
import java.sql.Connection;

public abstract class DataSourceFactory  implements Closeable, Serializable {
    public static final String DefaultDatabase="defaultDatabase";
    /*
     * 构造
     *

     */
    public DataSourceFactory() {}

    public abstract Database getDataBase();
    public abstract Database getDataBase(String name);

    public Connection getConnection(){
        try {
           return getDataSource().getConnection();
        }catch (Exception e){
            return null;
        }
    }

    public Connection getConnection(String name){
        try {
            return getDataSource(name).getConnection();
        }catch (Exception e){
            return null;
        }
    }
    /**
     * 获得默认数据源
     *
     * @return 数据源
     */
    public DataSource getDataSource() {
        return getDataSource(DefaultDatabase);
    }



    /**
     * 获得分组对应数据源
     *
     * @param group 分组名
     * @return 数据源
     */
    public abstract DataSource getDataSource(String group);

    /**
     * 关闭默认数据源（空组）
     */
    @Override
    public void close() {
        close(DataSourceFactory.DefaultDatabase);
    }

    /**
     * 关闭对应数据源
     *
     * @param group 分组
     */
    public abstract void close(String group);

    /**
     * 销毁工厂类，关闭所有数据源
     */
    public abstract void destroy();

    // ------------------------------------------------------------------------- Static start
    /**
     * 获得数据源<br>
     * 使用默认配置文件的无分组配置
     *
     * @return 数据源
     */
    public static DataSource get() {
        return get(DefaultDatabase);
    }

    /**
     * 获得数据源
     *
     * @param group 配置文件中对应的分组
     * @return 数据源
     */
    public static DataSource get(String group) {
        return Global.get().getDataSource(group);
    }

    /**
     * 根据Setting获取当前数据源工厂对象
     *
     * @param setting 数据源配置文件
     * @return 当前使用的数据源工厂
     */
    @Deprecated
    public static DataSourceFactory getCurrentDSFactory(DbInfo setting) {
        return create(setting);
    }

    /**
     * 设置全局的数据源工厂<br>
     * 在项目中存在多个连接池库的情况下，我们希望使用低优先级的库时使用此方法自定义之<br>
     * 重新定义全局的数据源工厂此方法可在以下两种情况下调用：
     *
     * <pre>
     * 1. 在get方法调用前调用此方法来自定义全局的数据源工厂
     * 2. 替换已存在的全局数据源工厂，当已存在时会自动关闭
     * </pre>
     *
     * @param dsFactory 数据源工厂
     * @return 自定义的数据源工厂
     */
    public static DataSourceFactory setCurrentDSFactory(DataSourceFactory dsFactory) {
        return Global.set(dsFactory);
    }

    public static DataSourceFactory create() {
//        DbInfo config = Configs.getValue(DataSourceFactory.DefaultDatabase,DbInfo.class);
        DbInfo config = (DbInfo) Collections.get(Ladder.Settings().getDatabase(),"DefaultDatabase",true);
        if(config!=null){
            config.setName(DataSourceFactory.DefaultDatabase);
            final DataSourceFactory dsFactory = doCreate(config);
            //log.debug("Use [{}] DataSource As Default", dsFactory.dataSourceName);
            return dsFactory;
        }else{
            return null;
        }

    }

    /**
     * 创建数据源实现工厂<br>
     * 此方法通过“试错”方式查找引入项目的连接池库，按照优先级寻找，一旦寻找到则创建对应的数据源工厂<br>
     * 连接池优先级：Hikari &gt; Druid &gt; Tomcat &gt; Dbcp &gt; C3p0 &gt; Hutool Pooled
     *
     * @param info 数据库配置项
     * @return 日志实现类
     */
    public static DataSourceFactory create(DbInfo info) {
        if(info==null)return null;
        final DataSourceFactory dsFactory = doCreate(info);
        //log.debug("Use [{}] DataSource As Default", dsFactory.dataSourceName);
        return dsFactory;
    }

    /**
     * 创建数据源实现工厂<br>
     * 此方法通过“试错”方式查找引入项目的连接池库，按照优先级寻找，一旦寻找到则创建对应的数据源工厂<br>
     * 连接池优先级：Hikari &gt; Druid &gt;
     *
     * @param info 数据库配置项
     * @return 日志实现类
     * @since 4.1.3
     */
    private static DataSourceFactory doCreate(DbInfo info) {

        try {
            return new DataSourceByDruid(info);
        } catch (NoClassDefFoundError e) {
            // ignore
        }

        try {
            return new DataSourceByHikari(info);
        } catch (NoClassDefFoundError e) {
            // ignore
        }

        try {
            return new DataSourceByEmpty(info);
        } catch (NoClassDefFoundError e) {
            // ignore
        }
        throw Core.makeThrow("未实现[0185]");
//        try {
//            return new TomcatDSFactory(setting);
//        } catch (NoClassDefFoundError e) {
//            //如果未引入包，此处会报org.apache.tomcat.jdbc.pool.PoolConfiguration未找到错误
//            //因为org.apache.tomcat.jdbc.pool.DataSource实现了此接口，会首先检查接口的存在与否
//            // ignore
//        }
//        try {
//            return new BeeDSFactory(setting);
//        } catch (NoClassDefFoundError e) {
//            // ignore
//        }
//        try {
//            return new DbcpDSFactory(setting);
//        } catch (NoClassDefFoundError e) {
//            // ignore
//        }
//        try {
//            return new C3p0DSFactory(setting);
//        } catch (NoClassDefFoundError e) {
//            // ignore
//        }
//        return new PooledDSFactory(setting);
    }
    // ------------------------------------------------------------------------- Static end

}
