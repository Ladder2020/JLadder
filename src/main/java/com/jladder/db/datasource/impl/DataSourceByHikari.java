package com.jladder.db.datasource.impl;
import com.jladder.configs.Configure;
import com.jladder.db.DbInfo;
import com.jladder.db.datasource.Database;
import com.jladder.db.datasource.IDataSource;
import com.jladder.lang.Strings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

public class DataSourceByHikari extends IDataSource {


    /**
     * 构造
     *
     * @param info 数据源名称
     */
    public DataSourceByHikari(DbInfo info) {
        dsMap.put(info.getName(),new Database(createDataSource(info),info,"hikari"));
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

        int idleTimeout= Configure.exist("db_idletimeout")?Configure.getInt("db_idletimeout"):60000;
        int connectionTimeout= Configure.exist("db_connectiontimeout")?Configure.getInt("db_connectiontimeout"):60000;
        int validationTimeout= Configure.exist("db_validationtimeout")?Configure.getInt("db_validationtimeout"):3000;
        int maxLifeTime= Configure.exist("db_maxlifetime")?Configure.getInt("db_maxlifetime"):60000;
        int maximumPoolSize= Configure.exist("db_maximumpoolsize")?Configure.getInt("db_maximumpoolsize"):100;
        hikariConfig.setMaximumPoolSize(maximumPoolSize);
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setValidationTimeout(validationTimeout);
        dataSource.setMaxLifetime(maxLifeTime);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(5);
        return dataSource;
    }
    public static void closeConnection(DataSource source, Connection conn){
        ((HikariDataSource)source).evictConnection(conn);
    }
}
