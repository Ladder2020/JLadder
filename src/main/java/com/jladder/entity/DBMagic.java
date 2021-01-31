package com.jladder.entity;

import com.jladder.db.bean.BaseEntity;

import java.io.Serializable;
import java.util.Date;

public class DBMagic extends BaseEntity implements Serializable {

    /// <summary>
    ///ID
    ///</summary>
    public String Id ;

    /// <summary>
    /// 类型
    /// </summary>
    public String Type;

    /// <summary>
    /// pc端设置
    /// </summary>
    public String PSettings;
    /// <summary>
    /// 移动端设置
    /// </summary>
    public String MSettings;
    /// <summary>
    ///键值名
    ///</summary>
    public String Name;
    /// <summary>
    ///标题名称
    ///</summary>
    public String Title;
    /// <summary>
    /// 关联类型
    ///</summary>
    public String RelationType;
    /// <summary>
    ///模版名称
    ///</summary>
    public String TableName;
    /// <summary>
    ///布局类型
    ///</summary>
    public String LayoutType;
    /// <summary>
    ///PC端列模型
    ///</summary>
    public String PColumns;
    /// <summary>
    ///移动端列模型
    ///</summary>
    public String MColumns;
    /// <summary>
    ///资源ID
    ///</summary>
    public String ResId;
    /// <summary>
    ///备注说明
    ///</summary>
    public String Descr;
    /// <summary>
    /// 使能位
    /// </summary>
    public int Enable;
    /// <summary>
    ///更新时间
    ///</summary>
    public Date UpdateTime;
    /// <summary>
    ///创建时间
    ///</summary>
    public Date CreateTime;
    /// <summary>
    ///创建人
    ///</summary>
    public String Creator;
    /// <summary>
    ///图标
    ///</summary>
    public String Icon;
    /// <summary>
    /// 自建表
    /// </summary>
    public String SelfTable;
}
