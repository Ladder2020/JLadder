package com.jladder.db;

import com.jladder.db.enums.DbDialectType;
import com.jladder.db.jdbc.DbDriver;
import com.jladder.lang.Core;
import com.jladder.lang.Security;
import com.jladder.lang.Strings;

public class DbInfo {
    private String server="";
    private String username="";
    private String password="";
    private String dialect="";
    private String database="";
    private String port="";
    private String name="";
    private String connection="";
    private String driver;
    private String parameters;
    public DbInfo(){

    }
    public DbInfo(String url){
        this.connection = url;
    }
    public String getConnection() {
        if(Strings.isBlank(connection)){
            if(Strings.isBlank(dialect))throw Core.makeThrow("数据库方言未配置");

            try {
                switch (dialect.toLowerCase()){
                    case "mysql":
                        this.connection = "jdbc:mysql://" + server + (Strings.isBlank(port) ? "" : ":" + port) + "/" + database + (Strings.isBlank(parameters)?"":"?"+parameters);
                        break;
                    case "sqlite":
                        this.connection = "jdbc:sqlite:"+server+database + (Strings.isBlank(parameters)?"":"?"+parameters);
                        break;
                }
                return this.connection;
            }catch (Exception e){
                e.printStackTrace();
                //throw e;
            }
            return "";
        }else return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public DbInfo setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDialect() {
        return dialect;
    }

    public DbInfo setDialect(String dialect) {
        this.dialect = dialect;
        return this;
    }

    public String getPort() {
        return port;
    }

    public DbInfo setPort(String port) {
        this.port = port;
        return this;
    }
    public DbInfo setPort(int port) {
        this.port = port+"";
        return this;
    }
    public String getName() {
        if(Strings.isBlank(name)){
            return Security.md5(this.server+this.driver+this.dialect+this.username+this.password+this.port+this.database);
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public DbInfo setServer(String server) {
        this.server=server;
        return this;
    }
    public String getServer() {
        return this.server;
    }

    public DbInfo setDatabase(String database) {
        this.database = database;
        return this;
    }
    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    public String getDatabase() {
        return this.database;
    }

    public DbInfo setDialect(DbDialectType type){
        switch (type){
            case MYSQL:
                this.dialect = "mysql";
                break;
            case SQLITE:
                this.dialect = "sqlite";
                break;
            case ORACLE:
                this.dialect = "oracle";
                break;
            case Mssql2012:
                this.dialect="mssql2012";
                break;
            case H2:
                this.dialect="h2";
                break;
            case PostgreSql:
                this.dialect="postgresql";
                break;
        }
        return this;
    }

    public DbInfo setDriver(String driver) {
        this.driver = driver;
        return this;
    }
    public String getDriver(){
        if(Strings.isBlank(driver)){
            return DbDriver.getDriver(dialect);
        }else return this.driver;
    }
}
