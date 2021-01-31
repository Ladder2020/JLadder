package com.jladder.proxy;

import com.jladder.data.Record;

import java.util.List;

public class ProxyFunctionInfo {
    /***
     * 编号
     */
    public String id ;
    /***
     * 类型
     */
    public String type;
    /***
     * 项目或者命名空间
     */
    public String project;
    /***
     * 访问链接
     */
    public String uri;
    /**
     * 路径
     */
    public String path;
    /***
     * 方法名
     */

    public String functionname;
    /***
     * 解释
     */
    public String express;
    /***
     * 备注信息
     */
    public String descr;
    /***
     * 更新时间
     */
    public String updatetime ;
    /***
     * 创建时间
     */
    public String createtime;
    /***
     * 执行代码
     */
    public String code;
    /***
     * 加密算法
     */
    public Record transition;

    /***
     * 返回结果数据
     */
    public Object result;

    /***
     * 扩展参数数据[方法的入口参数数据实例]
     */
    public Record param;

    /***
     * 参数列表[方法的入口参数结构]
     */
    public List<ProxyParam> paramater;
}
