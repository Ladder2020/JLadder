package com.jladder.entity;

import com.jladder.db.annotation.Table;

import java.io.Serializable;
@Table("sys_service")
public class DbProxy implements Serializable {
    /// <summary>id</summary>
    public String id;
    /// <summary>名称</summary>
    public String name ;
    /// <summary>标题</summary>
    public String title ;
    /// <summary>入口参数</summary>
    public String params;
    /// <summary>映射信息</summary>
    public String mappings ;
    /// <summary>是否可用</summary>
    public int enable ;
    /// <summary>返回处理</summary>
    public String returns ;
    /// <summary>请求方式</summary>
    public String Method ;
    /// <summary>类型</summary>
    public String type ;
    /// <summary>调用者</summary>
    public String caller ;
    /// <summary>维护者</summary>
    public String maintainer ;
    /// <summary>开发语言</summary>
    public String language ;
    /// <summary>描述信息</summary>
    public String descr ;
    /// <summary>版本号</summary>
    public String version ;
    /// <summary>更新时间</summary>
    public String updatetime ;
    /// <summary>创建时间</summary>
    public String creaetetime;
    /// <summary>分属</summary>
    public String sort ;
    /// <summary>团队</summary>
    public String team ;
    /// <summary>功能信息</summary>
    public String funinfo ;
    /// <summary>上级</summary>
    public String superior ;
    /// <summary>上级</summary>
    public String debugging ;
    /// <summary>
    /// 重要等级
    /// </summary>
    public String level;

    /// <summary>
    /// 日志级别
    /// </summary>
    public int logoption;

    /// <summary>
    /// 返回样板
    /// </summary>
    public String sample;
    /// <summary>
    /// 请求示例
    /// </summary>
    public String example;
    /// <summary>
    /// 调用信息
    /// </summary>
    public String callinfo;

}
