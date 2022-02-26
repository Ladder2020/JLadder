package com.jladder.actions;

/**
 * 分析选项
 */
public enum AnalyzeOption{
    /**
     * 默认
     */
    Default(0),
    /**
     * 访问统计
     */
    Visit(1),
    /**
     * 删减统计
     */
    Keep(2),
    /**
     * 数据吞吐量
     */
    Rate(3),
    /**
     * 数据对照
     */
    Compare(4);

    AnalyzeOption(int i) {

    }
}
