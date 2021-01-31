package com.jladder.configs;

/// <summary>
/// 配置源的来源
/// </summary>
public enum SourceDirection
{
    /// <summary>
    /// 内存
    /// </summary>
    Memory(0),
    /// <summary>
    /// 本地配置文件
    /// </summary>
    ConfigFile(1),
    /// <summary>
    /// 应用程序注入
    /// </summary>
    Application(2),
    /// <summary>
    /// 网站或者软件的配置文件
    /// </summary>
    WebApp(100);

    SourceDirection(int i) {

    }
}
