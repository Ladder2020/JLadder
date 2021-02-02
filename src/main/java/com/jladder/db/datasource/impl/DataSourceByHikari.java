package com.jladder.db.datasource.impl;
import com.jladder.db.DbInfo;
import com.jladder.db.datasource.Database;
import com.jladder.db.datasource.IDataSource;
import com.jladder.lang.Strings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class DataSourceByHikari extends IDataSource {


    /**
     * 构造
     *
     * @param info 数据源名称
     */
    public DataSourceByHikari(DbInfo info) {
        dsMap.put(info.getName(),new Database(createDataSource(info),info));
    }

    protected DataSource createDataSource(DbInfo info) {
        // remarks等特殊配置，since 5.3.8


        final Properties config = new Properties();


        config.put("jdbcUrl", info.getConnection());
        config.put("driverClassName", info.getDriver());
        if(Strings.hasValue(info.getUsername()))
            config.put("username", info.getUsername());
        if(Strings.hasValue(info.getPassword()))
            config.put("password", info.getPassword());

        final HikariConfig hikariConfig = new HikariConfig(config);
        //hikariConfig.setDataSourceProperties(connProps);
        //hikariConfig.setMaximumPoolSize(20);
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
//        dataSource.setIdleTimeout(60000);
//        dataSource.setConnectionTimeout(60000);
//        dataSource.setValidationTimeout(3000);
////        dataSource.setLoginTimeout(5);
//        dataSource.setMaxLifetime(60000);
//        dataSource.setMaximumPoolSize(20);
        return dataSource;
    }
}
