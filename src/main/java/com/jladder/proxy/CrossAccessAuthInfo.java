package com.jladder.proxy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CrossAccessAuthInfo implements Serializable {

    public String whitelist;
    /// <summary>
    /// 中文全称
    /// </summary>
    public String fullname;
    /// <summary>
    /// 用户名
    /// </summary>
    public String username;
    /// <summary>
    /// 用户组列表
    /// </summary>
    public List<String> groups;
    /// <summary>
    /// 标识码
    /// </summary>
    public String sign;
    /// <summary>
    /// 密钥
    /// </summary>
    public String secret;

    /// <summary>
    /// 是否通过认证
    /// </summary>
    public boolean ispass;
    /// <summary>
    /// 错误信息
    /// </summary>
    public String error;

    /// <summary>
    /// 跟踪用户
    /// </summary>
    public String withwho;

    /// <summary>
    /// 请求模式
    /// </summary>
    public String mode;


    /// <summary>
    /// 认证模式
    /// </summary>
    public String authoption;


    /// <summary>
    /// 客户端
    /// </summary>
    public String client;

    /// <summary>
    /// 访问Ip
    /// </summary>
    public String ip ;

    /// <summary>
    /// 本次请求的加密码
    /// </summary>
    public String token;
    /// <summary>
    /// 初始化
    /// </summary>
    public CrossAccessAuthInfo() { }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="username">用户名</param>
    /// <param name="groups">用户组列表</param>
    /// <param name="sign">标识码</param>
    public CrossAccessAuthInfo(String username, List<String> groups, String sign)
    {
        this.username = username;
        this.groups = groups != null ? groups: new ArrayList<String>();
        this.sign = sign;
    }
    /// <summary>
    /// 设置密钥
    /// </summary>
    /// <param name="secret"></param>
    /// <returns></returns>
    public CrossAccessAuthInfo SetSecret(String secret)
    {
        this.secret = secret;
        return this;
    }
    /// <summary>
    /// 设置通过
    /// </summary>
    /// <returns></returns>
    public static CrossAccessAuthInfo Ok()
    {
        CrossAccessAuthInfo authinfo = new CrossAccessAuthInfo();
        authinfo.ispass=true;
        return authinfo;
    }
    /// <summary>
    /// 设置失败
    /// </summary>
    /// <param name="error">失败原因</param>
    /// <returns></returns>
    public static CrossAccessAuthInfo Fail(String error)
    {
        CrossAccessAuthInfo authinfo = new CrossAccessAuthInfo();
        authinfo.error=error;
        return authinfo;
    }


}
