package com.jladder.db.datasource.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.jladder.db.DbInfo;
import com.jladder.db.datasource.Database;
import com.jladder.db.datasource.IDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceByDruid  extends IDataSource {



    public DataSourceByDruid(DbInfo info) {
        System.out.println("使用阿里巴巴Druid数据库链接池->DataSourceByDruid");
        dsMap.put(info.getName(),new Database(createDataSource(info),info,"druid"));
    }

    protected DataSource createDataSource(DbInfo info) {
		final DruidDataSource ds = new DruidDataSource();
		ds.setUrl(info.getConnection());
		ds.setDriverClassName(info.getDriver());
		ds.setUsername(info.getUsername());
		ds.setPassword(info.getPassword());

        ds.setInitialSize(5);
        ds.setMinIdle(30);
        ds.setMaxActive(200);
        ds.setMaxWait(60000);
        //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        ds.setTimeBetweenEvictionRunsMillis(60000);
        //配置一个连接在池中最小生存的时间，单位是毫秒
        ds.setMinEvictableIdleTimeMillis(300000);
        //配置一个连接在池中最大生存的时间，单位是毫秒
        ds.setMaxEvictableIdleTimeMillis(300000);
        ds.setValidationQuery("SELECT 1 FROM DUAL");
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);
        return ds;
    }

    public static void closeConnection(DataSource source, Connection conn){
        try {
            conn.close();
            //((DruidDataSource)source).discardConnection(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
