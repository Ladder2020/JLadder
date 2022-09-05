package com.jladder.es;

public class ElasticHttpHost {
    /**
     * 主机地址
     */
    private String host;
    /**
     * 端口号
     */
    private int port;
    /**
     * 协议头
     */
    private String scheme;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

    public ElasticHttpHost(String host, int port, String scheme) {
        this.host = host;
        this.port = port;
        this.scheme = scheme;
    }

    public ElasticHttpHost(String host, int port, String scheme, String username, String password) {
        this.host = host;
        this.port = port;
        this.scheme = scheme;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public ElasticHttpHost setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ElasticHttpHost setPort(int port) {
        this.port = port;
        return this;
    }

    public String getScheme() {
        return scheme;
    }

    public ElasticHttpHost setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public ElasticHttpHost setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ElasticHttpHost setPassword(String password) {
        this.password = password;
        return this;
    }
}
