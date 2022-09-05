package com.jladder.taobao;

import com.jladder.data.Record;

import java.io.Serializable;

public class DingSecret implements Serializable {
    private String id;
    private String agentid;
    private String appsecret;
    private String appkey;
    private String corpid;
    private String authurl="${host}/DingTalk/getScanUserInfo";
    private String comcode;
    private Record extra = new Record();
    public DingSecret(){

    }
    public DingSecret(String appkey,String appsecret) {
        this.appsecret = appsecret;
        this.appkey = appkey;
    }

    public String getAgentid() {
        return agentid;
    }

    public DingSecret setAgentid(String agentid) {
        this.agentid = agentid;
        return this;
    }

    public String getAppsecret() {
        return appsecret;
    }

    public DingSecret setAppsecret(String appsecret) {
        this.appsecret = appsecret;
        return this;
    }

    public String getAppkey() {
        return appkey;
    }

    public DingSecret setAppkey(String appkey) {
        this.appkey = appkey;
        return this;
    }

    public String getCorpid() {
        return corpid;
    }

    public DingSecret setCorpid(String corpid) {
        this.corpid = corpid;
        return this;
    }

    public String getAuthurl() {
        return authurl;
    }

    public DingSecret setAuthurl(String authurl) {
        this.authurl = authurl;
        return this;
    }

    public String getId() {
        return id;
    }

    public DingSecret setId(String id) {
        this.id = id;
        return this;
    }

    public String getComcode() {
        return comcode;
    }

    public void setComcode(String comcode) {
        this.comcode = comcode;
    }
    public void put(String name,Object value){
        this.extra.put(name,value);
    }
    public Object get(String name){
        return this.extra.get(name);
    }
}
