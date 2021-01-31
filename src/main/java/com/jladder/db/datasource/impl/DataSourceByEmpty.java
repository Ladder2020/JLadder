package com.jladder.db.datasource.impl;

import com.jladder.db.DbInfo;
import com.jladder.db.datasource.Database;
import com.jladder.db.datasource.IDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class DataSourceByEmpty extends IDataSource {

    public DataSourceByEmpty(DbInfo info) {
        System.out.println("使用空数据库连接池->DataSourceByEmpty");
        dsMap.put(info.getName(),new Database(createDataSource(info),info));
    }
    protected DataSource createDataSource(DbInfo info) {
        final DataSource ds = new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                try {
                    Class.forName(info.getDriver());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
                return DriverManager.getConnection(info.getConnection(),info.getUsername(),info.getPassword());
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                try {
                    Class.forName(info.getDriver());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
                return DriverManager.getConnection(info.getConnection(),username,password);
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {

            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {

            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }
        };
        return ds;
    }
}
