package com.jladder.configs;

/// <summary>
/// 配置项
/// </summary>
public class ConfigItem
{
    /// <summary>
    /// 配置名称
    /// </summary>
    public String Name;
    /// <summary>
    /// 配置项的值
    /// </summary>
    public Object Value;
    /// <summary>
    /// 来源方向
    /// </summary>
    public SourceDirection direct = SourceDirection.Memory;


    public ConfigItem(String name,Object value,SourceDirection direct){
        this.Name= name;
        this.Value= value;
        this.direct= direct;
    }
}
