package com.jladder.net.mail;

import com.jladder.lang.Strings;

import java.util.HashMap;
import java.util.Map;

public class MailAccount {

    private final static Map<String,MailAccount> channels  = new HashMap<String,MailAccount>();
    public static void reg(MailAccount account){
        channels.put("_default_",account);
    }
    public static void reg(String channel, MailAccount account){
        channels.put(channel,account);
    }
    public static MailAccount get(){
        return channels.get("_default_");
    }
    public static MailAccount get(String channel){
       return channels.get(channel);
    }
    private String host;
    private String sender;
    private String sendname;
    private String username;
    private String password;
    private int port=25;
    private boolean auth=true;
    private boolean ssl=false;
    private boolean debug=true;
    public MailAccount(){}
    public MailAccount(String host,String username,String password){
        this.host=host;
        this.username=username;
        this.password=password;
    }

    public MailAccount setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public MailAccount setAuth(boolean auth){
        this.auth=auth;
        return this;
    }
    public MailAccount setSsl(boolean ssl) {
        this.ssl = ssl;
        return this;
    }
    public String getHost() {
        return host;
    }

    public MailAccount setHost(String host) {
        this.host = host;
        return this;
    }

    public String getSender() {
        return sender;
    }

    public MailAccount setSender(String sender) {
        this.sender = sender;
        if(Strings.isBlank(username))username=sender;
        return this;
    }

    public String getSendname() {
        if(Strings.isBlank(sendname))return sender;
        return sendname;
    }

    public MailAccount setSendName(String sendname) {
        this.sendname = sendname;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public MailAccount setUsername(String username) {
        this.username = username;
        if(Strings.isBlank(sender))sender=username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public MailAccount setPassword(String password) {
        this.password = password;
        return this;
    }
    public boolean isAuth(){
        return this.auth;
    }

    public boolean isSsl() {
        return this.ssl;
    }

    public boolean isDebug() {
        return debug;
    }

    public int getPort() {
        return port;
    }

    public MailAccount setPort(int port) {
        this.port = port;
        return this;
    }
}
