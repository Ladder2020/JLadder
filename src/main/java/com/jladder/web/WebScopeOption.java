package com.jladder.web;

public enum WebScopeOption {
    SqlDebug("SqlDebug"),
    Latch("Latch"),
    Analyz("Analyz"),
    IgnoreRequestLog("IgnoreRequestLog")
    ;


    private String name;

    WebScopeOption(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
