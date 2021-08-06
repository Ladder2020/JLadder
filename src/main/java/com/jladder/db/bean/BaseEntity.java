package com.jladder.db.bean;

import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.db.IDao;
import com.jladder.db.SqlText;
import com.jladder.db.annotation.Column;
import com.jladder.db.annotation.Table;
import com.jladder.db.enums.DbGenType;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.db.jdbc.impl.SaveColumn;
import com.jladder.lang.Collections;
import com.jladder.lang.Core;
import com.jladder.lang.Strings;
import com.jladder.lang.Times;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class BaseEntity {

    private IDao _mdao = null;
    private FieldInfo _pk = null;

    public BaseEntity() { }


    public void SetDao(IDao dao)
    {
        this._mdao = dao;
    }
    public int update(){
        return update(null,null);
    }
    public int update(String columns){
       return update(null,columns);
    }
    public int update(IDao dao){
        return update(dao,null);
    }
    /***
     * 更新实体
     * @param dao 操作数据库链接对象
     * @param columns 列选
     * @return
     */
    public int update(IDao dao, String columns)
    {
        String tableName = getTableName();
        if(Strings.isBlank(tableName))return -1;
        FieldInfo field = _pk ;
        if(field==null)field = getPk();
        if (field == null) return -1;
        Record bean = GenBean(DbSqlDataType.Update);
        if (Strings.isBlank(Collections.haveKey(bean,field.fieldname))|| bean.get(field.fieldname) == null) return -1;
        boolean iscreate = false;//自创建
        if (dao == null)
        {
            if (_mdao == null)
            {
                iscreate = true;
                dao = new Dao();
            }
            else dao = _mdao;
        }
        if (dao == null) return -1;
        int count = dao.update(tableName, SaveColumn.Clip(bean, columns), new Cnd(field.fieldname, field.value));
        if (iscreate) dao.close();
        return count;
    }

    public int save(IDao dao,String columns){
        FieldInfo field = getPk();
        if(field==null) return -1;
        return Core.isEmpty(field.value) ? insert(dao,columns):update(dao,columns);
    }

    public int delete(){
        return delete(null);
    }

    /// <summary>
    /// 删除本对象
    /// </summary>
    /// <param name="dao">可以为空,当成员mdao和参数dao同时存在，以参数为准</param>
    /// <returns></returns>

    public int delete(IDao dao)
    {
        String tableName = getTableName();
        if(Strings.isBlank(tableName))return -1;
        FieldInfo field = _pk ;
        if(field==null)field = getPk();
        if (field == null) return -1;
        boolean iscreate = false;
        if (dao == null)
        {
            if (_mdao == null)
            {
                iscreate = true;
                dao = new Dao();
            }
            else dao = _mdao;
        }
        if (dao == null) return -1;
        int count = dao.delete(tableName,new Cnd(field.fieldname, field.value));
        if (iscreate) dao.close();
        return count;
    }
    public int insert(){return insert(null,null);}
    public int insert(String columns){return insert(null,columns);}
    public int insert(IDao dao){return insert(dao,null);}
    /// <summary>
    /// 新增实体对象
    /// </summary>
    /// <returns></returns>
    public int insert(IDao dao , String columns )
    {
        String  tableName = getTableName();
        if (Strings.isBlank(tableName)) return -1;
        boolean iscreate = false;
        if (dao == null)
        {
            if (_mdao == null)
            {
                iscreate = true;
                dao = new Dao();
            }
            else dao = _mdao;
        }
        if (dao == null) return -1;
        int count = dao.insert(tableName, SaveColumn.Clip(GenBean(DbSqlDataType.Insert), columns));
        if (iscreate) dao.close();
        return count;
    }

    /**
     * 级联单条记录
     */
    public <T extends BaseEntity> T LinkOne(Class<T> clazz,String ... fieldNames)
    {
        List<T> bean = Link(clazz, fieldNames);
        return bean == null || bean.size() < 1 ? null : bean.get(0);
    }
    public <T extends BaseEntity> List<T> Link(Class<T> clazz,String ... fieldNames)
    {
        return Link(clazz,null,fieldNames);
    }
    public <T extends BaseEntity> List<T> Link(Class<T> clazz,IDao dao,String ... fieldNames)
    {
        if (fieldNames == null || fieldNames.length < 1) return null;
        Record record = Record.parse(this);
        Cnd cnd=new Cnd();
        Collections.forEach(fieldNames,x->cnd.put(x.toString(),record.get(x)));
        boolean iscreate = false;
        //置dao
        if (dao == null)
        {
            if (_mdao == null)
            {
                iscreate = true;
                dao = new Dao();
            }
            else dao = _mdao;
        }
        if (dao == null) return null;
        Table table = clazz.getAnnotation(Table.class);
        if(table==null)return null;
        List<T> result = dao.query(table.value(), cnd, clazz);
        if (iscreate) dao.close();
        return result;
    }
    public <T extends BaseEntity> List<T> select(IDao dao,Cnd cnd)
    {
        String table = getTableName();
        if (Strings.isBlank(table)) return null;
        if(cnd==null) cnd = new Cnd();
        boolean iscreate = false;
        //置dao
        if (dao == null)
        {
            if (_mdao == null)
            {
                iscreate = true;
                dao = new Dao();
            }
            else dao = _mdao;
        }
        List<T> rs = ( List<T>) dao.query(new SqlText("select "
                        + Strings.ArrayToString(getColumns(), ",", null)
                        + " from "
                        + getTableName()+" "+ cnd.getWhere(true,false)
                ,cnd.parameters), this.getClass());
        if(iscreate)dao.close();
        return rs;
    }
    /// <summary>
    /// 获取列文本数组
    /// </summary>
    /// <returns></returns>
    public List<String> getColumns()
    {
        List<String> columns = new ArrayList<String>();
        Field[] fs = this.getClass().getFields();
        for (Field field : fs)
        {
            String key = field.getName();
            Column config = field.getAnnotation(Column.class);
            if (config != null)
            {
                if (Strings.hasValue(config.fieldname())) key = config.fieldname();
                if (config.isExt() || config.readonly()) continue;
            }
            columns.add(key);
        }
        return columns;
    }

    public void SetPk(String propertyName){
        SetPk(propertyName,null,null);
    }
    public void SetPk(String propertyName, DbGenType gen){
        SetPk(propertyName,null,gen);
    }
    /// <summary>
    /// 设置主键
    /// </summary>
    /// <param name="propertyName">类的属性名</param>
    /// <param name="fieldName">表单字段名</param>
    public void SetPk(String propertyName, String fieldName,DbGenType gen){
        if (Strings.isBlank(fieldName)) fieldName = propertyName;
        try {
            Field field = this.getClass().getField(propertyName);
            if(field==null)throw Core.makeThrow("字段类型不存在");
            _pk = new FieldInfo();
            _pk.name= Strings.isBlank(fieldName) ? propertyName : fieldName;
            _pk.fieldname = _pk.name;
            _pk.oldname = propertyName;
            _pk.value = field.get(this);
            if(gen!=null){
                _pk.gen=gen.getIndex();
            }
            Column config = field.getAnnotation(Column.class);
            if (config == null) return;
            if(!DbGenType.NoGen.equals(config.gen()) && gen==null)_pk.gen = config.gen().getIndex();
            if(Strings.isBlank(fieldName) && Strings.hasValue(config.fieldname())){
                _pk.name = config.fieldname();
                _pk.fieldname = _pk.name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /// <summary>
    /// 获取表名
    /// </summary>
    /// <returns></returns>
    public String getTableName()
    {
        Table attr = getClass().getAnnotation(Table.class);
        return attr==null?"":attr.value();
    }
    /// <summary>
    /// 生成填充bean对象
    /// </summary>
    /// <param name="action">动作类型</param>
    /// <returns></returns>
    public Record GenBean(DbSqlDataType action)
    {
        Record record=new Record();
        Field[] pi = this.getClass().getFields();
        for (Field field : pi)
        {
            boolean noignore = true;
            String key = field.getName();
            Object value = null;
            try {
                value = field.get(this);
                Column config = field.getAnnotation(Column.class);
                if (config != null) {
                    if (Strings.hasValue(config.fieldname())) key = config.fieldname();
                    if (Strings.hasValue(config.dvalue())) value = config.dvalue();
                    if (config.gen() != DbGenType.NoGen) {
                        switch (config.gen()) {
                            case ID:
                                if (value == null)
                                    value = "{ sql: '(select Max(" + key + ")+1 from " + getTableName() + ")'}";
                                break;
                            case UUID:
                                if (value == null) value = Core.genUuid();
                                break;
                            case DATE:
                                if (value == null) value = Times.getNow();
                                break;
                            case TimeCode:
                                if (value == null) value = Times.TimeCode();
                                break;
                            case AutoNum:
                                noignore = false;
                                break;
                        }
                    }
                    if (config.isExt() || config.readonly()) noignore = false;
                }
            }catch (Exception e){
                throw Core.wrapThrow(e);
            }
            if(noignore)record.put(key, value);
        }
        return record;
    }
    /// <summary>
    /// 获取主键信息
    /// </summary>
    /// <returns></returns>
    public FieldInfo getPk()
    {
        String tableName = getTableName();
        if(Strings.isBlank(tableName))return null;

        FieldInfo reField = new FieldInfo();
        Field[] ps = this.getClass().getFields();
        for (Field field : ps)
        {
            String key = field.getName();
            String dbname =field.getName();
            Column config = field.getAnnotation(Column.class);
            if (config != null)
            {
                if (Strings.hasValue(config.fieldname())) dbname = config.fieldname();
                if (config.pk())
                {
                    reField.name = dbname;//数据库字段名
                    reField.fieldname = dbname;//数据库字段名
                    reField.oldname = key;//键名
                    reField.gen = config.gen().getIndex();
                    try{
                        reField.value = field.get(this);
                    }catch (Exception e){

                    }
                    return reField;
                }
            }
            if ("id".equals(key.toLowerCase()))
            {
                reField.oldname = key;
                reField.name = dbname;
                reField.fieldname = dbname;
                reField.gen = int.class.equals( field.getType()) ? DbGenType.AutoNum.getIndex():DbGenType.UUID.getIndex();
                try{
                    reField.value = field.get(this);
                }catch (Exception e){

                }
            }
        }
        return Strings.isBlank(reField.name) ? null : reField;
    }


}
