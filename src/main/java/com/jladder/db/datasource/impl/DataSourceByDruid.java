package com.jladder.db.datasource.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.jladder.db.DbInfo;
import com.jladder.db.datasource.Database;
import com.jladder.db.datasource.IDataSource;

import javax.sql.DataSource;

public class DataSourceByDruid  extends IDataSource {



    public DataSourceByDruid(DbInfo info) {
        System.out.println("使用阿里巴巴Druid数据库链接池->DataSourceByDruid");
        dsMap.put(info.getName(),new Database(createDataSource(info),info));
    }

    protected DataSource createDataSource(DbInfo info) {
		final DruidDataSource ds = new DruidDataSource();
		ds.setUrl(info.getConnection());
		ds.setDriverClassName(info.getDriver());
		ds.setUsername(info.getUsername());
		ds.setPassword(info.getPassword());
        return ds;
    }

}
