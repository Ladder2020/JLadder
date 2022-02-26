package com.jladder.data;

import java.io.Serializable;
import java.util.List;

public class BaseUserInfo implements Serializable {
    /// <summary>
    /// 用户名
    /// </summary>
    private String username;

    /// <summary>
    /// 唯一用户
    /// </summary>
    public String uuid;
    /// <summary>
    /// 用户全名
    /// </summary>
    private String fullname;
    /// <summary>
    /// SessionID
    /// </summary>
    private String sessionid;
    /// <summary>
    /// 账户中心token码
    /// </summary>
    private String usertoken;
    /// <summary>
    /// 分组ID
    /// </summary>
    private String groupid;


    /// <summary>
    /// 下辖分组
    /// </summary>
    private List<String> groups;

    /// <summary>
    /// 密钥标识
    /// </summary>
    private String sign;

    /// <summary>
    /// 密钥
    /// </summary>
    private String secret;

    public BaseUserInfo() {
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getUsertoken() {
        return usertoken;
    }

    public void setUsertoken(String usertoken) {
        this.usertoken = usertoken;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
