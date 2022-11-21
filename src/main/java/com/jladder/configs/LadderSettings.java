package com.jladder.configs;

import com.jladder.data.Record;
import com.jladder.db.DbInfo;
import com.jladder.db.enums.DbDialectType;
import com.jladder.lang.Strings;
import com.jladder.logger.LogOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;


import java.util.HashMap;
import java.util.Map;

@EnableConfigurationProperties(LadderSettings.class)
@ConfigurationProperties(prefix = "spring.ladder")
public class LadderSettings {
    /**
     * 站点名称
     */
    private String site;
    /**
     * 应用名称
     */
    private String app;
    /**
     * 0,生产，1，开发，3，测试
     */
    private int env=0;
    /**
     * 自定义表单的数据库表
     */
    private String magicTableName="sys_magic";
    /**
     * 模型的数据库表
     */
    private String templateTableName="sys_data";

    /**
     * 数据分析功能
     */
    private boolean analyze=true;
    /**
     * 模版的链接器
     */
    private String templateConn="defaultDatabase";
    /**
     * 接口服务的连接器
     */
    private String proxyConn="defaultDatabase";
    /**
     * 接口服务表模型名称
     */
    private String proxyTableName="sys_service";
    /**
     * ladder的数据库库名
     */
    private String ladderSchema="ladder";
    /**
     * sql日志开关
     */
    private boolean sqlDebug=true;
    /**
     * sql执行记录选
     */
    private String sqlDebugItem="update,delete,insert,create";
    /**
     * sql警告线
     */
    private int sqlWarnTime=60000;
    /**
     * 输出日志等级
     */
    private LogOption logLevel=LogOption.Debug;
    /**
     * 日志输出路径
     */
    private String logPath="~/log/{yyyy-MM-dd}";
    /**
     * 日志收集服务地址
     */
    private String logServer=null;
    /**
     * 日志输出大小
     */
    private long logSize=1024*1024*10;

    private Map<String,DbInfo> database= new HashMap<>();

    private Record settings;

    private DbDialectType dbDialect=DbDialectType.Default;


    private BusinessSettings business=new BusinessSettings();


    private String dataDifferentReport="${title}[${fieldname}]:${old}=>${current}";

    public String getLadderSchema() {
        return ladderSchema;
    }

    public void setLadderSchema(String ladderSchema) {
        this.ladderSchema = ladderSchema;
    }

    public String getMagicTableName() {
        if(Strings.isBlank(magicTableName))return ladderSchema+".sys_magic";
        if(magicTableName.contains("."))return magicTableName;
        return  ladderSchema+"."+magicTableName;
    }

    public void setMagicTableName(String magicTableName) {
        this.magicTableName = magicTableName;
    }

    public String getProxyConn() {
        return proxyConn;
    }

    public void setProxyConn(String proxyConn) {
        this.proxyConn = proxyConn;
    }

    public String getTemplateConn() {
        return templateConn;
    }

    public void setTemplateConn(String templateConn) {
        this.templateConn = templateConn;
    }

    public boolean isAnalyze() {
        return analyze;
    }

    public void setAnalyz(boolean analyze) {
        this.analyze = analyze;
    }

    public String getProxyTableName() {
        if(Strings.isBlank(proxyTableName))return ladderSchema+".sys_service";
        if(proxyTableName.contains("."))return proxyTableName;
        return  ladderSchema+"."+proxyTableName;
    }

    public void setProxyTableName(String proxyTableName) {
        this.proxyTableName = proxyTableName;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public boolean isSqlDebug() {
        return sqlDebug;
    }

    public void setSqlDebug(boolean sqlDebug) {
        this.sqlDebug = sqlDebug;
    }

    public String getSqlDebugItem() {
        return sqlDebugItem;
    }

    public void setSqlDebugItem(String sqlDebugItem) {
        this.sqlDebugItem = sqlDebugItem;
    }

    public int getSqlWarnTime() {
        return sqlWarnTime;
    }

    public void setSqlWarnTime(int sqlWarnTime) {
        this.sqlWarnTime = sqlWarnTime;
    }

    public LogOption getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogOption logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getLogServer() {
        return logServer;
    }

    public void setLogServer(String logServer) {
        this.logServer = logServer;
    }

    public Map<String, DbInfo> getDatabase() {
        return database;
    }

    public void setDatabase(Map<String, DbInfo> database) {
        this.database = database;
    }

    public Record getSettings() {
        return settings;
    }

    public void setSettings(Record settings) {
        this.settings = settings;
    }

    public String getTemplateTableName() {
        if(Strings.isBlank(templateTableName))return ladderSchema+".sys_data";
        if(templateTableName.contains("."))return templateTableName;
        return ladderSchema+"."+templateTableName;
    }

    public void setTemplateTableName(String templateTableName) {
        this.templateTableName = templateTableName;
    }

    public DbDialectType getDbDialect() {
        return dbDialect;
    }

    public void setDbDialect(DbDialectType dbDialect) {
        this.dbDialect = dbDialect;
    }

    public BusinessSettings getBusiness() {
        return business;
    }

    public void setBusiness(BusinessSettings business) {
        this.business = business;
    }

    public String getDataDifferentReport() {
        return dataDifferentReport;
    }

    public void setDataDifferentReport(String dataDifferentReport) {
        this.dataDifferentReport = dataDifferentReport;
    }

    public int getEnv() {
        return env;
    }

    public void setEnv(int env) {
        this.env = env;
    }

    /**
     * 获取日志分割大小
     * @return
     */
    public long getLogSize() {
        return logSize;
    }

    /**
     * 设置日志分割大小
     * @param logSize 大小，1024*1024*10=10MB
     */
    public void setLogSize(long logSize) {
        this.logSize = logSize;
    }

}
