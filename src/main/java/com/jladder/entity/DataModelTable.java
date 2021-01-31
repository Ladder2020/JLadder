package com.jladder.entity;

import com.jladder.db.IDao;
import com.jladder.db.annotation.Column;
import com.jladder.db.bean.BaseEntity;
import com.jladder.db.enums.DbGenType;
import com.jladder.lang.Core;

import java.util.Date;
import java.util.List;

public class DataModelTable extends BaseEntity {


    /**
     * 主键
     */
    @Column(pk = true, gen = DbGenType.UUID)
    public String id;

    /// <summary>
    /// 键名
    /// </summary>
    public String name;
    /***
     * 分属分类
     */
    public String sort;

    /**
     * 真实表名
     */
    public String tablename;
    /**
     * 标题
     */
    public String title;

    /**
     * 字段映射
     */
    public String columns;

    /**
     * 查询字段
     */
    public String queryform;

    /**
     * 类型
     */
    public String type;

    /**
     * 是否可用
     */
    public int enable = 1;

    /**
     * 创建者
     */
    public String fromer;

    /**
     * 权限
     */
    public String permission;

    /**
     * 交换数据
     */
    public String data;

    /**
     * 更新时间
     */
    public Date updatetime;

    /**
     * 事件队列
     */
    public String events;

    /**
     * 脚本数据
     */
    public String script;

    /***
     *扩展参数
     */
    public String params;

    /**
     *
     */
    public String conn;

    /**
     * 访问次数
     */
    public String visittimes;

    /**
     * 缓存项
     */
    public String cacheitems;

    /**
     * 依赖项
     */
    public String depends;

    /**
     * 分析项目
     */
    public String analyzeitems;

    /**
     * 备注说明
     */
    public String descr;

    /**
     * 初始化
     */
    public DataModelTable() {
    }


    /// <summary>
    /// 模版键名转换模版
    /// </summary>
    /// <param name="tableName"></param>
    /// <returns></returns>
    public DataModelTable(String tableName) {
        DBtoDataTable(null, tableName);
    }

    /// <summary>
    /// 模版键名转换模版
    /// </summary>
    /// <param name="dao">数据库连接对象</param>
    /// <param name="tableName"></param>
    /// <returns></returns>
    public DataModelTable(IDao dao, String tableName) {
        DBtoDataTable(dao, tableName);
    }

    /// <summary>
    /// 模版键名转换模版
    /// </summary>
    /// <param name="dao">数据库操作对象</param>
    /// <param name="tableName">键表名</param>
    /// <returns></returns>
    private void DBtoDataTable(IDao dao, String tableName) {
        throw Core.makeThrow("未实现");
    }

    /// <summary>
    /// 从文件解析基本模版配置
    /// </summary>
    /// <param name="fileName">文件名</param>
    /// <param name="node">节点</param>
    /// <returns></returns>
    public static List<DataModelTable> Parse(String fileName, String node) {
        throw Core.makeThrow("未实现");

    }
}

