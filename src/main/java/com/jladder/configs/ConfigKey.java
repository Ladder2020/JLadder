package com.jladder.configs;

public enum ConfigKey {
    SqlWarnTime("SqlWarnTime"),
    MagicTableName("MagicTableName"),
    TemplateTableName("TemplateTableName"),
    //数据分析开关
    Analyz("Analyz"),
    OutLogLevel("OutLogLevel"),
    OutLogPath("OutLogPath"),
    LogServer("LogServer"),
    SqlDebugItem("SqlDebugItem"),
    ProxyConn("ProxyConn"),
    ProxyTableName("ProxyTableName"),
    MainScheme("MainScheme"),
    SqlDebug("SqlDebug"),
    LadderSchema("LadderSchema"),
    TemplateConn("TemplateConn"),
    SSOServer("SSOServer"),
    ScheduleServer("ScheduleServer");


    private String name;

    ConfigKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
