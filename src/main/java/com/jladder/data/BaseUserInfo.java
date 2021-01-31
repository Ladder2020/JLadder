package com.jladder.data;

import java.io.Serializable;
import java.util.List;

public class BaseUserInfo implements Serializable {
    /// <summary>
    /// 用户名
    /// </summary>
    public String username;

    /// <summary>
    /// 唯一用户
    /// </summary>
    public String uuid;


    /// <summary>
    /// 用户全名
    /// </summary>
    public String fullname;


    /// <summary>
    /// SessionID
    /// </summary>
    public String sessionid;


    /// <summary>
    /// 账户中心token码
    /// </summary>
    public String usertoken;


    /// <summary>
    /// 分组ID
    /// </summary>
    public String groupId;


    /// <summary>
    /// 下辖分组
    /// </summary>
    public List<String> groups;

    /// <summary>
    /// 密钥标识
    /// </summary>
    public String sign;

    /// <summary>
    /// 密钥
    /// </summary>
    public String secret;
}
