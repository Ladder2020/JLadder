package com.jladder.datamodel;
/// <summary>
/// 数据模型信息
/// </summary>
public class DataModelInfo
{
    public DataModelInfo(String type, String path,String node, long lastWriteTime) {
        this.type = type;
        this.path = path;
        this.node = node;
        this.lastWriteTime = lastWriteTime;
        this.enable=true;
    }

    /// <summary>
    /// 类型
    /// </summary>
    public String type ;

    /// <summary>
    /// 类型
    /// </summary>
    public String path;

    /// <summary>
    /// 节点
    /// </summary>
    public String node;

    /// <summary>
    /// 最后修改时间
    /// </summary>
    public long lastWriteTime;

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /// <summary>
    /// 是否可用
    /// </summary>
    public boolean enable;

}
