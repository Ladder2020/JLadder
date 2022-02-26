package com.jladder.db.datasource;
import com.jladder.db.DbInfo;
import com.jladder.db.datasource.impl.DataSourceByDruid;
import com.jladder.db.datasource.impl.DataSourceByHikari;
import com.jladder.lang.Strings;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class Database implements DataSource, Closeable, Cloneable{

    private String pn;
    private final DataSource ds;
//    private final String driver;
    private final DbInfo info;
    private int error=0;
    /**
     * 构造
     *
     * @param ds     原始的DataSource
     * @param info 数据库驱动类名
     */
    public Database(DataSource ds, DbInfo info,String poolName) {
        this.ds = ds;
        this.info = info;
        this.pn=poolName;
        if(Strings.isBlank(poolName))pn=ds.getClass().getName();

    }

    /**
     * 获取驱动名
     *
     * @return 驱动名
     */
    public DbInfo getInfo() {
        return this.info;
    }
    /**
     * 获取原始的数据源
     *
     * @return 原始数据源
     */
    public DataSource getRaw() {
        return this.ds;
    }

    @Override
    public void close() throws IOException {
        if (this.ds instanceof AutoCloseable) {

            if (null != this.ds) {
                try {
                    ((AutoCloseable)(this.ds)).close();
                } catch (Exception var2) {
                }
            }
        }
    }

    public void closeConnection(Connection conn){
        switch (pn){
            case "druid":
            case "com.alibaba.druid.pool.DruidDataSource":
                DataSourceByDruid.closeConnection(ds,conn);
                break;
            case "hikari":
                DataSourceByHikari.closeConnection(ds,conn);
                break;
            default:
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public Connection getConnection(){
        Connection ret;
        try{
            ret = ds.getConnection();
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        return ret;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return ds.getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return ds.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return ds.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return ds.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        ds.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        ds.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return ds.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return ds.getParentLogger();
    }
}
