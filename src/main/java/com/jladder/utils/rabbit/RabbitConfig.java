package com.jladder.utils.rabbit;

public class RabbitConfig {

    private String host;
    private int port;
    private String username;
    private String pasword;
    private String queue;
    private String route;
    private String exchange;
    private String message;
    public String getHost() {
        return host;
    }

    public RabbitConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public RabbitConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public RabbitConfig setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPasword() {
        return pasword;
    }

    public RabbitConfig setPasword(String pasword) {
        this.pasword = pasword;
        return this;
    }

    public String getQueue() {
        return queue;
    }

    public RabbitConfig setQueue(String queue) {
        this.queue = queue;
        return this;
    }

    public String getRoute() {
        return route;
    }

    public RabbitConfig setRoute(String route) {
        this.route = route;
        return this;
    }

    public String getExchange() {
        return exchange;
    }

    public RabbitConfig setExchange(String exchange) {
        this.exchange = exchange;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RabbitConfig setMessage(String message) {
        this.message = message;
        return this;
    }

}
