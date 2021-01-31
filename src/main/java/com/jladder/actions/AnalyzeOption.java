package com.jladder.actions;

/// <summary>
/// 分析的选项
/// </summary>
public enum AnalyzeOption
{
    /// <summary>
    /// 默认
    /// </summary>
    Default(0),
    /// <summary>
    /// 访问统计
    /// </summary>
    Visit(1),
    /// <summary>
    /// 删减统计
    /// </summary>
    Keep(2),
    /// <summary>
    /// 数据吞吐量
    /// </summary>
    Rate(3),
    /// <summary>
    /// 数据对照
    /// </summary>
    Compare(4);

    AnalyzeOption(int i) {

    }
}
