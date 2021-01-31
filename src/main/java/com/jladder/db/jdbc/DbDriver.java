package com.jladder.db.jdbc;


import com.jladder.db.enums.DbDialectType;
import com.jladder.lang.Strings;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class DbDriver {
    public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    public static final String DRIVER_MYSQL_V6 = "com.mysql.cj.jdbc.Driver";
    public static final String DRIVER_ORACLE = "oracle.jdbc.OracleDriver";
    public static final String DRIVER_ORACLE_OLD = "oracle.jdbc.driver.OracleDriver";
    public static final String DRIVER_POSTGRESQL = "org.postgresql.Driver";
    public static final String DRIVER_SQLLITE3 = "org.sqlite.JDBC";
    public static final String DRIVER_SQLSERVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String DRIVER_HIVE = "org.apache.hadoop.hive.jdbc.HiveDriver";
    public static final String DRIVER_HIVE2 = "org.apache.hive.jdbc.HiveDriver";
    public static final String DRIVER_H2 = "org.h2.Driver";
    public static final String DRIVER_DERBY = "org.apache.derby.jdbc.AutoloadedDriver";
    public static final String DRIVER_HSQLDB = "org.hsqldb.jdbc.JDBCDriver";
    public static final String DRIVER_DM7 = "dm.jdbc.driver.DmDriver";


    public static String getDriver(String dialect){

        switch (dialect){
            case "mysql":
                return DRIVER_MYSQL;
            case "mysql8":
                return DRIVER_MYSQL_V6;
            case "sqlserver":
                return DRIVER_SQLSERVER;

        }
        return "";
    }
    public static DbDialectType getDialect(String dialect){
        if(Strings.isBlank(dialect))return DbDialectType.MYSQL;
        switch (dialect.toLowerCase()){
            case "mysql":
                return DbDialectType.MYSQL;
            case "mysql8":
                return DbDialectType.MYSQL;
            case "sqlserver":
                return DbDialectType.SQLSERVER;
            case "oracle":
                return DbDialectType.ORACLE;
        }
        return DbDialectType.MYSQL;
    }
    /**
     * 根据驱动名创建方言<br>
     * 驱动名是不分区大小写完全匹配的
     *
     * @param driverName JDBC驱动类名
     * @return 方言
     */
    public static DbDialectType getDialectName(String driverName) {
        
        if (!Strings.isBlank(driverName)) {
            if (DRIVER_MYSQL.equalsIgnoreCase(driverName) || DRIVER_MYSQL_V6.equalsIgnoreCase(driverName)) {
                return DbDialectType.MYSQL;
            } else if (DRIVER_ORACLE.equalsIgnoreCase(driverName) || DRIVER_ORACLE_OLD.equalsIgnoreCase(driverName)) {
                return DbDialectType.ORACLE;
            } else if (DRIVER_SQLLITE3.equalsIgnoreCase(driverName)) {
                return DbDialectType.SQLITE;
            } else if (DRIVER_POSTGRESQL.equalsIgnoreCase(driverName)) {
                return DbDialectType.PostgreSql;
            } else if (DRIVER_H2.equalsIgnoreCase(driverName)) {
                return DbDialectType.H2;
            } else if (DRIVER_SQLSERVER.equalsIgnoreCase(driverName)) {
                return DbDialectType.SQLSERVER;
            }
        }
        // 无法识别可支持的数据库类型默认使用ANSI方言，可兼容大部分SQL语句
        return DbDialectType.Default;
    }
    public static DbDialectType getDialectName(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            if (!Strings.isBlank(meta.getDatabaseProductName())) {
                String  product = meta.getDatabaseProductName();
                switch(product.toLowerCase()){
                    case "sqlite":
                        return DbDialectType.SQLITE;
                    case "mysql":
                        return DbDialectType.MYSQL;
                    case "sqlserver":
                        return DbDialectType.SQLSERVER;
                    case "oracle":
                        return DbDialectType.ORACLE;
                    case "postgresql":
                        return DbDialectType.PostgreSql;
                }

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // 无法识别可支持的数据库类型默认使用ANSI方言，可兼容大部分SQL语句
        return DbDialectType.Default;
    }
}
