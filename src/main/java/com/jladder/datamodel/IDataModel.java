package com.jladder.datamodel;


import com.jladder.actions.Curd;
import com.jladder.data.Pager;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.db.IDao;
import com.jladder.db.SqlText;
import com.jladder.db.enums.DbDialectType;
import com.jladder.db.jdbc.GroupBy;
import com.jladder.db.jdbc.OrderBy;
import com.jladder.entity.DBMagic;
import com.jladder.entity.DataModelTable;

import java.util.List;
import java.util.Map;

/// <summary>
/// 动态数据模型
/// </summary>
public abstract class  IDataModel
{
//    /// <summary>
//    /// 列模型,模型根据条件会变短
//    /// </summary>
    protected List<Map<String, Object>> ColumnList;
//
//    /// <summary>
//    /// 整体模型，原型集合
//    /// </summary>
    protected DataModelForMapRaw Raw = new DataModelForMapRaw();



    //
//    /// <summary>
//    /// 数据库连接文本，用于跨域操作，数据库连接键名或者数据库连接的json文本
//    /// </summary>
    protected String Conn;
//
//    /// <summary>
//    /// 条件对象
//    /// </summary>
    protected  Cnd Condition = new Cnd();
//
//    /// <summary>
//    /// 排序对象
//    /// </summary>
    protected OrderBy Order;
//
//    /// <summary>
//    /// 数据库操作对象
//    /// </summary>
      protected IDao Dao;
//
//    /// <summary>
//    /// 分组对象
//    /// </summary>
    protected GroupBy Group;
//

    /***
     * 真实的数据
     */
     public String TableName;
//    /// <summary>
//    /// 全字段
//    /// </summary>
//    public List<Map<String, Object>> FullColumns;
//    /// <summary>
//    /// 类型
//    /// </summary>
    public DataModelType Type = DataModelType.Table;;
//
//    /// <summary>
//    /// 数据库类型
//    /// </summary>
    protected DbDialectType DbDialect ;

    public String getName(){
        if(Raw==null)return "";
        return Raw.Name;
    }
    public DataModelType getType() {
        return Type;
    }
    public IDao getDao(){
        return Dao;
    }
    public String getConn() {
        return Conn;
    }

    public void setConn(String conn) {
        Conn = conn;
    }

    public DbDialectType getDialect(){
        return DbDialect;
    }
    public Cnd getCondition(){
        return Condition;
    }

    /// <summary>
    /// 从JSON文件初始化
    /// </summary>
    /// <param name="path">文件路径</param>
    /// <param name="nodeName">节点名称</param>
    public abstract boolean fromJsonFile(String path, String nodeName);

    /// <summary>
    /// 从魔法实体类中获取
    /// </summary>
    public abstract boolean fromMagic(DBMagic magic, String client);

    /// <summary>
    /// 从数据库键名解析
    /// </summary>
    /// <param name="name">键名表名</param>
    /// <returns></returns>
    public abstract  int fromTemplate(String name);

    /// <summary>
    /// 从数据库实表解析
    /// </summary>
    /// <param name="table">表名或sql语句</param>
    /// <returns></returns>
    public abstract int fromDbTable(String table);

//    /// <summary>
//    /// 从xml元素解析
//    /// </summary>
//    /// <param name="element">元素节点</param>
//    /// <param name="param">扩展参数</param>
//    /// <returns></returns>
//   public abstract boolean FromXml(XElement element,String param=null);

    /// <summary>
    /// 从原型数据解析
    /// </summary>
    /// <param name="raw">原型数据</param>
    /// <param name="param">参数数据</param>
    public abstract void fromRaw(DataModelForMapRaw raw, String param);

    /// <summary>
    /// 从模版实体类中获取
    /// </summary>
    /// <param name="dao">数据库操作对象</param>
    /// <param name="tableName">键表名</param>
    /// <param name="param">参数列表</param>
    /// <returns></returns>
    public abstract int fromDataTable(IDao dao, CharSequence tableName, String param);

    /// <summary>
    /// 从模版实体类中获取
    /// </summary>
    public abstract int fromDataTable(DataModelTable dt);

    /// <summary>
    /// 从模版实体类中获取
    /// </summary>
    /// <param name="dt">数据库模版类</param>
    /// <param name="param">参数列表</param>
    /// <returns></returns>
    public abstract int fromDataTable(DataModelTable dt, String param);

    /***
     * 添加列模型
     * @param fieldname 列名称
     * @param title 列标题
     * @param as 列别名
     */
    public abstract void addColumn(String fieldname,String title,String as);

    /***
     * 添加列模型
     * @param column 列模型
     */
    public abstract void addColumn(Map<String,Object> column);

    /***
     * 添加列模型
     * @param json JSON数据，支持Array Object，String
     */
    public abstract void addColumn(String json);
    /// <summary>
    /// 过滤列字段
    /// </summary>
    /// <param name="filterCName">字段名</param>
    /// <returns></returns>
    public abstract List<Map<String, Object>> filterColumns(String filterCName);

    /**
     * 字段匹配，默认以fieldname,支持排序，自序，反向,变名
     * @param columnString 文本型以,分割
     * @return
     */
    public abstract List<Map<String, Object>> matchColumns(String columnString);

    /**
     * 具体执行字段匹配，默认以fieldname,支持排序，自序，反向,变名
     * @param columnString 文本型以,分割
     * @param propName 属性名
     * @return
     */
    public abstract List<Map<String, Object>> matchColumns(String columnString, String propName);

    public abstract List<Map<String, Object>> parseColumsList();
    /// <summary>
    /// 从模版中取出Columns段
    /// <para>注意：这是字段原型数据</para>
    ///  </summary>
    public abstract List<Map<String, Object>> parseColumsList(Object rawData);

    /// <summary>
    /// 生成Bean实体对象
    /// </summary>
    /// <param name="bean">默认文本</param>
    /// <param name="option">选项</param>
    /// <param name="message">回馈信息</param>
    /// <returns></returns>
    public abstract Record genBean(String bean, int option, StringBuilder message);

    /// <summary>
    /// 获取重复检查的字段(bean中字段)
    /// </summary>
    /// <param name="bean"></param>
    /// <returns></returns>
    public abstract List<String> hasUniqueFields(Record bean);

    /// <summary>
    /// 获取重复检查的字段
    /// </summary>
    /// <returns></returns>
    public abstract List<String> hasUniqueFields();

    /// <summary>
    /// 获取字段配置
    /// </summary>
    /// <param name="fieldname">字段名称,可以是别名</param>
    /// <returns></returns>
    public abstract Map<String, Object> getFieldConfig(String fieldname);

    /// <summary>
    /// 根据属性以及属性数值获取字段组，可变参数为或的关系
    /// </summary>
    /// <param name="propName">属性名</param>
    /// <param name="val">值,如果为null，所有含有此属性的</param>
    /// <returns></returns>
    public abstract List<String> getFields(String propName, Object ... val);
    /// <summary>
    /// 更新字段配置,注意最后是字段数组
    /// </summary>
    /// <param name="propName">欲修改属性名</param>
    /// <param name="value">属性值</param>
    /// <param name="fields">字段名数组</param>

    public abstract void updateFieldConfig(String propName, Object value, String ... fields);
    /// <summary>
    /// 更新字段配置,注意最后是字段名集合
    /// </summary>
    /// <param name="propName">欲修改属性名</param>
    /// <param name="value">属性值</param>
    /// <param name="fields">字段名称集合</param>
    public abstract void updateFieldConfig(String propName, Object value, List<String> fields);


    /// <summary>
    /// 寻找字段
    /// </summary>
    /// <param name="matchStr">
    /// 欲匹配的字段串
    /// <example>id,name</example>
    /// </param>
    /// <returns></returns>
    public abstract String matchFieldName(String matchStr);

    /// <summary>
    /// 添加事件
    /// </summary>
    /// <param name="name">事件名称</param>
    /// <param name="action">事件配置</param>
    public abstract void addEvent(String name, Record action);

    /// <summary>
    /// 添加事件
    /// </summary>
    /// <param name="name">事件名称</param>
    /// <param name="action">事件配置</param>
    public abstract void addEvent(String name, Curd action);
    /// <summary>
    /// 获得关联动作
    /// </summary>
    /// <param name="key">动作事件名称</param>
    public abstract  List<Record> getRelationAction(String key);

    /// <summary>组装sql字符串，conditionText可为
    /// <para>1,[]数组文本,</para>
    /// <para>2,{}对象文本</para>
    /// <para>3,name='ddd' 键值文本</para>
    /// </summary>
    public abstract void setCondition(String conditionText);

    /// <summary>
    ///
    /// </summary>
    /// <param name="sqldic"></param>
    /// <returns></returns>
    public abstract void setCondition(Map<String, Object> sqldic);

    /// <summary>
    /// 插入条件
    /// </summary>
    /// <param name="cnd">条件对象</param>
    public abstract void setCondition(Cnd cnd);

    /// <summary>
    /// 获取文本化sql语句
    /// </summary>
    /// <returns></returns>
    public abstract SqlText getSqlText();

    /// <summary>
    /// 获取模版的分页语句(为什么需要一个数据库方言呢)
    /// </summary>
    /// <param name="dialect">数据库方言</param>
    /// <param name="pager">分页对象</param>
    /// <returns></returns>
    public abstract SqlText pagingSqlText(DbDialectType dialect, Pager pager);

    /// <summary>
    /// 清空条件语句
    /// </summary>
    public abstract void clearCondition();


    /// <summary>
    /// 获取原始数据模板
    /// </summary>
    /// <param name="pname">排序属性</param>
    /// <returns></returns>
    public abstract List<Map<String, Object>> getRawColumnList(String pname);
    public abstract List<Map<String, Object>> getColumnList();
    /// <summary>
    /// 返回模版的列模型
    /// </summary>
    public abstract List<Map<String, Object>> getColumnList(String propName);

    /// <summary>
    /// 获取当前的条件文本
    /// </summary>
    /// <returns></returns>
    public abstract SqlText getWhere();

    /// <summary>
    /// 获取表名
    /// </summary>
    /// <returns></returns>
    public abstract String getTableName();

    /// <summary>
    /// 获取节选的字段和别名
    /// </summary>
    /// <returns></returns>

    public abstract Map<String, String> getSelect();
    /// <summary>
    /// 获取字段文本串
    /// </summary>
    /// <returns></returns>
    public abstract String getColumn();




    /// <summary>
    /// 获取字段文本串
    /// </summary>
    /// <param name="prefix">前缀文本</param>
    /// <param name="splitStr">分隔符</param>
    /// <returns></returns>
    public abstract String getColumn(String prefix, String splitStr);

    /// <summary>
    /// 获取分组文本串
    /// </summary>
    /// <returns></returns>
    public abstract String getGroup();

    /// <summary>
    /// 获取排序的串
    /// </summary>
    public abstract String getOrder();

    /// <summary>
    /// 判断是否为空模版
    /// </summary>
    /// <returns></returns>
    public abstract boolean isNull();

    /// <summary>
    /// 从模版中取出queryform段
    /// </summary>
    public abstract Object getQueryForm();

    /// <summary>
    /// 获取可用于过滤查询的列
    /// <para>1,和FullColumn之处：顺序，负值隐藏</para>
    /// </summary>
    public abstract List<Map<String, Object>> getAllQueryColumns();

    /**
     * 获取全字段
     * @return
     */
    public abstract List<Map<String, Object>> getFullColumns();
    /**
     * 匹配参数数据
     * @param source 待匹配的数据
     * @param paramDataDic 匹配的数据字典
     * @param ignore 忽略严格匹配，3个参数，3个param数据
     * @return
     */
    public abstract String matchParam(String source, String paramDataDic, boolean ignore);

    /// <summary>
    /// 重置扩展参数
    /// </summary>
    /// <param name="param"></param>
    public abstract void reset(String param);

    /**
     * 获取模版连接器对应的数据库连接操作对象
     * @return
     */
    public abstract IDao fetchConnDao();

    /**
     * 获取原型数据
     * @return
     */
    public abstract DataModelForMapRaw getRaw();

    /**
     * 获取脚本代码
     * @return
     */
    public abstract String getScript();

    /**
     * 是否可用
     * @return
     */
    public abstract boolean enable();

    public GroupBy groupby(){
       if(this.Group==null)this.Group=new GroupBy();
       return this.Group;
    }
    public OrderBy orderby(){
        if(this.Order==null)this.Order=new OrderBy();
        return this.Order;
    }
    public void setDialect(DbDialectType dialect) {
        this.DbDialect=dialect;
    }


}

