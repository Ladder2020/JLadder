package com.jladder.actions;

import com.jladder.data.Record;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.lang.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class QueryRelation implements CharSequence{
    private String tableName;
    private String type="event";

    public QueryRelation(String tableName){
        this.tableName=tableName;
    }
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Record> getEvents() {
        return events;
    }

    public void setEvents(List<Record> events) {
        this.events = events;
    }

    private List<Record> events=new ArrayList<Record>();
    /**
     *添加一对一关系
     * @param linkType 只能是 DbSqlDataType.OneToOne,DbSqlDataType.OneToMany,DbSqlDataType.ManyToMany
     * @param tableName 关联模型
     * @param columns 列选
     * @param fieldname 析出字段名称 ，*fieldname:单字段;其他：｛fieldname:data}
     * @param relation 字段关系，关联模型的字段为key，源模型的字段为value
     * @return
     */
    public QueryRelation addRelation(DbSqlDataType linkType,String tableName,String columns,String fieldname,Record relation){
        events.add(new Record("tableName",tableName).put("option",linkType.getIndex()).put("columns",columns).put("fieldname",fieldname).put("relation",relation));
        return this;
    }
    /**
     *添加一对一关系
     * @param linkType 只能是 DbSqlDataType.OneToOne,DbSqlDataType.OneToMany,DbSqlDataType.ManyToMany
     * @param tableName 关联模型
     * @param columns 列选
     * @param cnd 查询条件
     * @param fieldname 析出字段名称 ，*fieldname:单字段;其他：｛fieldname:data}
     * @param relation 字段关系，关联模型的字段为key，源模型的字段为value
     * @return
     */
    public QueryRelation addRelation(DbSqlDataType linkType,String tableName,String columns,String fieldname,Object cnd,Record relation){
        events.add(new Record("tableName",tableName).put("option",linkType.getIndex()).put("columns",columns).put("condition",cnd).put("fieldname",fieldname).put("relation",relation));
        return this;
    }
    /**
     *添加一对一关系
     * @param tableName 关联模型
     * @param columns 列选
     * @param fieldname 析出字段名称 ，*fieldname:单字段Object;其他：｛fieldname:Record}
     * @param relation 字段关系，关联模型的字段为key，源模型的字段为value
     * @return
     */
    public QueryRelation addOneToOne(String tableName,String columns,String fieldname,Record relation){
        events.add(new Record("tableName",tableName).put("option",15).put("columns",columns).put("fieldname",fieldname).put("relation",relation));
        return this;
    }

    /**
     *添加一对一关系
     * @param tableName 关联模型
     * @param columns 列选
     * @param cnd 查询条件
     * @param fieldname 析出字段名称 ，*fieldname:单字段Object;其他：｛fieldname:Record}
     * @param relation 字段关系，关联模型的字段为key，源模型的字段为value
     * @return
     */
    public QueryRelation addOneToOne(String tableName,String columns,String fieldname,Object cnd,Record relation){
        events.add(new Record("tableName",tableName).put("option",15).put("columns",columns).put("condition",cnd).put("fieldname",fieldname).put("relation",relation));
        return this;
    }
    /**
     *添加一对一关系
     * @param tableName 关联模型
     * @param columns 列选
     * @param cnd 查询条件
     * @param fieldname 析出字段名称 ，*fieldname:单字段Object;其他：｛fieldname:Record}
     * @param relation 关联字段名称，关联模型的字段为key，源模型的字段为value
     * @return
     */
    public QueryRelation addOneToOne(String tableName,String columns,String fieldname,Object cnd,String relation){
        events.add(new Record("tableName",tableName).put("option",15).put("columns",columns).put("condition",cnd).put("fieldname",fieldname).put("relation",new Record(relation,relation)));
        return this;
    }
    /**
     *添加一对一关系
     * @param tableName 关联模型
     * @param columns 列选
     * @param fieldname 析出字段名称 ，*fieldname:单字段Object;其他：｛fieldname:Record}
     * @param relation 关联字段名称，关联模型的字段为key，源模型的字段为value
     * @return
     */
    public QueryRelation addOneToOne(String tableName,String columns,String fieldname,String relation){
        events.add(new Record("tableName",tableName).put("option",15).put("columns",columns).put("fieldname",fieldname).put("relation",new Record(relation,relation)));
        return this;
    }
    /**
     *添加一对多关系
     * @param tableName 关联模型
     * @param columns 列选
     * @param fieldname 析出字段名称 ，*fieldname:单字段列表List<Object>;其他：List<Record>
     * @param relation 字段关系，关联模型的字段为key，源模型的字段为value
     * @return
     */
    public QueryRelation addOneToMany(String tableName,String columns,String fieldname,Record relation){
        events.add(new Record("tableName",tableName).put("option",16).put("columns",columns).put("fieldname",fieldname).put("relation",relation));
        return this;
    }
    /**
     *添加一对多关系
     * @param tableName 关联模型
     * @param columns 列选
     * @param cnd 查询条件
     * @param fieldname 析出字段名称 ，*fieldname:单字段列表List<Object>;其他：List<Record>
     * @param relation 字段关系，关联模型的字段为key，源模型的字段为value
     * @return
     */
    public QueryRelation addOneToMany(String tableName,String columns,String fieldname,Object cnd,Record relation){
        events.add(new Record("tableName",tableName).put("option",16).put("columns",columns).put("condition",cnd).put("fieldname",fieldname).put("relation",relation));
        return this;
    }
    /**
     * 添加多对多关系
     * @param tableName 关联模型
     * @param columns 列选
     * @param fieldname 析出字段名称 ，*fieldname:单字段列表，其他：List<Record>
     * @param relation 字段关系，关联模型的字段为key，源模型的字段为value
     * @return
     */
    public QueryRelation addManyToMany(String tableName,String columns,String fieldname,Record relation){
        events.add(new Record("tableName",tableName).put("option",17).put("columns",columns).put("fieldname",fieldname).put("relation",relation));
        return this;
    }
    /**
     * 添加多对多关系
     * @param tableName 关联模型
     * @param columns 列选
     * @param cnd 查询条件
     * @param fieldname 析出字段名称 ，*fieldname:单字段列表，其他：List<Record>
     * @param relation 字段关系，关联模型的字段为key，源模型的字段为value
     * @return
     */
    public QueryRelation addManyToMany(String tableName,String columns,String fieldname,Object cnd,Record relation){
        events.add(new Record("tableName",tableName).put("option",17).put("columns",columns).put("condition",cnd).put("fieldname",fieldname).put("relation",relation));
        return this;
    }

    public int length() {
        return events.size();
    }

    @Override
    public char charAt(int index) {
        return 0;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return null;
    }

    /**
     * 文本话
     * @return
     */
    public String toString(){
        return Json.toJson(this);
    }
}
