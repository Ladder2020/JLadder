package com.jladder.data;


import com.jladder.db.DaoSeesion;
import com.jladder.db.IDao;
import com.jladder.db.SqlText;
import com.jladder.lang.Strings;
import com.jladder.lang.func.Action1;
import com.jladder.lang.func.Func2;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 树型数据模型
/// </summary>
public class TreeModel
{
    /// <summary>
    /// 现场的记录集
    /// </summary>
    private List<Record> rs;

    public List<Record> getRs() {
        return rs;
    }

    public void setRs(List<Record> rs) {
        this.rs = rs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public boolean isSerialize() {
        return serialize;
    }

    public void setSerialize(boolean serialize) {
        this.serialize = serialize;
    }

    public Action1<Record> getCallback() {
        return Callback;
    }

    public void setCallback(Action1<Record> callback) {
        Callback = callback;
    }

    /// <summary>
    /// 本字段属性名
    /// </summary>
    private String id = "id";
    /// <summary>
    /// 父字段属性名
    /// </summary>
    private String pid = "pid";
    /// <summary>
    /// 是否进行序列化
    /// </summary>
    private boolean serialize  = true;
    /// <summary>
    /// 回调委托
    /// </summary>
    private Action1<Record> Callback;
    /// <summary>
    /// 散落自由的节点，其ID属性值的集合
    /// </summary>
    private List<String> _deletes=new ArrayList<String>();
    /// <summary>
    /// 用于递归循环的变量储存
    /// </summary>
    private List<String> _ids = new ArrayList<String>();
    /// <summary>
    /// id的映射表
    /// </summary>
    private Record _idmap = new Record();
    /// <summary>
    ///
    /// </summary>
    public TreeModel(){}
    /// <summary>
    ///
    /// </summary>
    /// <param name="rs"></param>
    public TreeModel(List<Record> rs)
    {
        this.rs = rs;
    }
    /// <summary>
    /// 获取整体集合
    /// </summary>
    /// <returns></returns>
    public Record GetMap()
    {
        return _idmap;
    }
    /// <summary>
    /// 获取ID序列键的集合
    /// </summary>
    /// <returns></returns>
    public List<String> GetIds(){return _ids;}
    /// <summary>
    /// 删除多余的分支
    /// </summary>
    /// <returns></returns>
    public List<String> GetDeletess() { return _deletes; }
    /// <summary>
    /// 获取一个的节点
    /// </summary>
    /// <param name="id">节点的id属性值</param>
    /// <returns></returns>
    public Record GetNode(String id)
    {
        return _idmap.containsKey(id) ? (Record) _idmap.get(id) : null;
    }
    /// <summary>
    /// 清理ID的缓存
    /// </summary>
    public void ClearIds(){_ids.clear();}

    /// <summary>
    /// 重置树模型
    /// </summary>
    public void Reset()
    {
        this.rs = null;
        _idmap=new Record();
        this.id="id";
        this.pid="pid";
        this.serialize=true;
        Callback = null;
        _deletes=new ArrayList<String> ();
        _ids = new ArrayList<String>();
    }
    /// <summary>
    /// 执行数据树结构处理,把rs数据记录变成tree数据结构
    /// </summary>
    public void HandleData(){
        if(this.rs==null)return;
        if(Strings.isBlank(this.id)) this.id = "id";
        if(Strings.isBlank(this.pid)) this.pid = "pid";
        if(_idmap==null)_idmap=new Record();//list->map,转化以ID为key，实际输出集合
        //List<String> deletes=new List<String> ();//后续要删除的IDs
        for(Record map : rs){
            String pid=map.getString(this.pid);//取记录集的pid
            String id = map.getString(this.id);//取记录集的id
            //处理父级对象
            if(!Strings.isBlank(pid) && !pid.equals("0")){ //记录存在pid
                if(_idmap.get(pid)==null){//集合中不存在，新建一个，并push该条记录
                    Record pmap=new Record();
                    List<Record> plist=new ArrayList<Record>();
                    plist.add(map);//map本对象，TMD是浅拷贝，大善
                    pmap.put("children",plist);//push本对象
                    _idmap.put(pid, pmap);
                }else{
                    Record pmap =(Record)_idmap.get(pid);
                    Record newMap = map;
                    List<Record> children= (List<Record>)pmap.get("children");
                    children.add(newMap);
                    pmap.put("children",children);
                    _idmap.put(pid,pmap);
                }
                _deletes.add(id);
            }
            //处理本对象
            if(_idmap.get(id)==null){
                if (Callback != null)
                {
                    try{
                        Callback.invoke(map);
                    }catch (Exception e){

                    }


                }
                map.put("children",new ArrayList<Record>());//先置一个空的children
                _idmap.put(id,map);
            }else{ //已经建立，只更新信息段，children部分保留
                map.put("children", ((Record) _idmap.get(id)).get("children"));//取已经push的children
                if (Callback != null)
                {
                    try{
                        Callback.invoke(map);
                    }catch (Exception e){

                    }
                }
                _idmap.put(id,map);
            }
        }
    }
    /// <summary>
    /// 匹配树的节点，注意列表多个数据，范围树形逐渐缩小
    /// </summary>
    /// <param name="matchrs">匹配的数据列表</param>
    public void Match(List<Record> matchrs)
    {

        if (matchrs == null || matchrs.size()<1) return;
        Record newidsmap=new Record();
        for (Record rs : matchrs)
        {
            String matchid = rs.getString(this.id);
            if (_idmap.containsKey(matchid))
            {
                newidsmap.put(matchid, _idmap.get(matchid));
            }
        }
        _idmap = newidsmap;
    }
    /// <summary>
    /// 删除散落的节点
    /// </summary>
    public void DeleteFreeChild(){
        for(String key : _deletes)_idmap.remove(key);//删除子级Map
        _deletes.clear();
    }
    /// <summary>
    /// 按原记录顺序进行排列并整理成记录集
    /// </summary>
    /// <returns></returns>
    public List<Record> OrderByRaw()
    {
        if (this.rs == null) return null;
        List<Record> ret = new ArrayList<Record>();

        for(Record m : this.rs){
            String _id = m.getString(this.id);
            if(Strings.hasValue(id)&& _idmap.containsKey(_id)){
                ret.add(m);
            }
        }
        return ret;
    }
    /// <summary>
    /// 把树形结构转换成记录集结构
    /// </summary>
    /// <returns></returns>
    public List<Record> ConvertToRs()
    {
        List<Record> ret = new ArrayList<Record>();
        _idmap.forEach((k,y)->{
            ret.add((Record)y);
        });
        return ret;
    }
    /// <summary>
    /// 转换成键值数据结构
    /// </summary>
    /// <param name="keyName">键名</param>
    /// <param name="valueName">值名</param>
    /// <param name="isAll">是否全部数据结构</param>
    /// <returns></returns>
    public Record ConvertTokeyValue(String keyName, String valueName,boolean isAll)
    {
        Record record = new Record();
        _idmap.forEach((k,v) -> record.merge(((Record)v).getKeyValue(keyName, valueName, isAll)));
        return record;
    }
    /// <summary>
    /// 获取键值数据结构
    /// </summary>
    /// <param name="data">数据</param>
    /// <param name="keyName">键名</param>
    /// <param name="valueName">值名</param>
    /// <param name="isAll">是否全部数据结构</param>
    /// <param name="kv">键值集合</param>
    /// <param name="childrenName">子数据键名</param>
    /// <returns></returns>
    public static Record getKeyValue(Record data, String keyName, String valueName,boolean isAll, Record kv,String childrenName)
    {
        if(kv==null) kv=new Record();
        List<Record> children = (List<Record>)data.get(childrenName);
        if (children !=null && children.size() > 0)
        {
            Record record = new Record();
            children.forEach(x -> getKeyValue(x, keyName, valueName, isAll, record,"children"));
            if (isAll || record.size()>0)
                kv.put(data.getString(keyName), record);
        }
            else
        {
            if (isAll || data.containsKey(valueName))
                kv.put(data.getString(keyName), data.get(valueName));
        }
        return kv;
    }

    /// <summary>
    /// 挂载tree,用于两个tree模型的挂载，ischild=true，是挂载两个idmaps的children
    /// </summary>
    /// <param name="map">树模型数据</param>
    /// <param name="isChild">是否子节点挂接</param>
    public void Mount(Record map, Boolean isChild){

        map.forEach((k,v)->{
            Record vmap = (Record)v;
            if (_idmap.containsKey(k))
            {
                Record oldMap =(Record)_idmap.get(k);
                List<Record> oldlist = (List<Record>)oldMap.get("children");
                if(isChild){
                    List<Record> list =(List<Record>) vmap.get("children");
                    oldlist.addAll(list);
                    oldMap.put("children", oldlist);
                    _idmap.put(k,oldMap);
                }
            }
        });
    }
    /// <summary>
    /// 追加子记录集
    /// </summary>
    /// <param name="childes">子记录集</param>
    public void PushBatch(List<Record> childes)
    {
        if (childes == null || childes.size() < 1) return;
        for(Record node : childes){
            PushChildren((String)node.get(this.pid),node);
        }
    }
    /// <summary>
    /// 追加子节点
    /// </summary>
    /// <param name="pid">节点的上级标识值</param>
    /// <param name="node">节点数据</param>
    public void PushChildren(String pid, Record node)
    {
        Record oldMap = (Record)_idmap.get(pid);
        if (oldMap == null) return;
        List<Record> children = (List<Record>)oldMap.get("children");
        if (children == null) children = new ArrayList<Record>();
        children.add(node);
        oldMap.put("children", children);
        _idmap.put(pid, oldMap);
    }
    /// <summary>
    /// 获取某节点及下级ID的值
    /// </summary>
    /// <param name="node">节点</param>
    public List<String> FetchTreeAttribute(Record node){
        List<String> ps = new ArrayList<String>();
        FetchTreeAttribute(ps,node, this.id,null);
        return ps;
    }
    /// <summary>
    /// 获取某节点及下级ID的值
    /// </summary>
    /// <param name="node">节点</param>
    /// <param name="fun">过滤委托</param>
    /// <returns></returns>
    public List<String> FetchTreeAttribute(Record node, Func2<Record,Boolean> fun)
    {
        List<String> ps = new ArrayList<String>();
        FetchTreeAttribute(ps,node, this.id, fun);
        return ps;
    }
    /// <summary>
    ///  获取某节点及下级某个属性的值
    /// </summary>
    /// <param name="node">节点</param>
    /// <param name="propName">属性</param>
    /// <returns></returns>
    public List<String> FetchTreeAttribute(Record node,String propName)
    {
        List <String> ps=new ArrayList<String>();
        FetchTreeAttribute(ps,node,propName, null);
        return ps;
    }
    /// <summary>
    /// 获取某节点及下级某些属性的值
    /// </summary>
    /// <param name="reList">填充对象</param>
    /// <param name="record">节点记录</param>
    /// <param name="fun">过滤委托</param>
    /// <param name="propnames">属性数组</param>
    public void FetchTreeAttribute(List<Record> reList, Record record, Func2<Record, Boolean> fun, String ... propnames)
    {
        if (reList == null) return;
        if(record==null)return;
        if(propnames == null|| propnames.length==0) propnames =new String[] {this.id};
        Record precord=new Record();
        for (String panme : propnames)
        {
            if (fun == null) precord.put(panme, record.get(panme));
            else
            {
                try {
                    if (fun.invoke(record)) precord.put(panme, record.get(panme));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        reList.add(precord);
        List<Record> children = (List<Record>)record.get("children");
        for (Record re : children)
        {
            FetchTreeAttribute(reList,re, fun, propnames);
        }
    }
    /// <summary>
    /// 获取某节点及下级某个属性的值
    /// </summary>
    /// <param name="reList">填充对象</param>
    /// <param name="record">节点记录</param>
    /// <param name="propname">属性名</param>
    /// <param name="fun">过滤委托</param>
    public void FetchTreeAttribute(List<String> reList, Record record, String propname, Func2<Record,Boolean> fun)
    {
        if (reList == null) return;
        if (record == null) return;
        if (Strings.isBlank(propname)) propname = this.id;
        if (fun == null) reList.add(record.getString(propname));
        else
        {
            try {
                if (fun.invoke(record)) reList.add(record.getString(propname));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<Record> children = (List<Record>)record.get("children");
        for (Record re : children)
        {
            FetchTreeAttribute(reList, re, propname, fun);
        }
    }


    /**  以下代码是进行数据库级别的递归推理过程******/

    /// <summary>
    /// 从表递归
    /// </summary>
    /// <param name="pidval">pid的值</param>
    /// <param name="tableName">表名</param>
    /// <param name="cFieldName">子字段名</param>
    public void RecurFromTable(String pidval, String tableName, String cFieldName)
    {
        RecurFromTable(pidval, tableName, cFieldName, this.pid, DaoSeesion.GetDao());
    }
    /// <summary>
    /// 从表递归
    /// </summary>
    /// <param name="pidval">pid的值</param>
    /// <param name="tableName">表名</param>
    public void RecurFromTable(String pidval, String tableName)
    {
        RecurFromTable(pidval, tableName, this.id,this.pid, DaoSeesion.GetDao());
    }
    /// <summary>
    /// 从表格递归
    /// </summary>
    /// <param name="pidval">pid值</param>
    /// <param name="tableName">表名</param>
    /// <param name="cFieldName">id的字段名</param>
    /// <param name="pFieldName">pid的字段名</param>
    /// <param name="dao">数据库操作对象</param>
    public void RecurFromTable(String pidval, String tableName, String cFieldName, String pFieldName, IDao dao)
    {
        if (dao == null) dao = DaoSeesion.GetDao();
        if (Strings.isBlank(cFieldName)) cFieldName = "id";
        if (Strings.isBlank(pFieldName)) pFieldName = "pid";
        tableName = Strings.mapping(tableName);
        String sqlcmd = "select " + cFieldName + " from " + tableName + " where " + pFieldName + "='" + pidval + "'";
        List<Record> rs = dao.query(new SqlText(sqlcmd));
        if (rs == null || rs.size() < 1) return;
        for (Record record : rs)
        {
            String tid = record.getString(cFieldName);
            _ids.add(tid);
            RecurFromTable(tid, tableName,cFieldName, pFieldName, dao);
        }
    }

    /// <summary>
    /// 递归导出下级的所有IDS（数据库层级）
    /// </summary>
    /// <param name="dao">数据库操作对象</param>
    /// <param name="tableName">数据库表名</param>
    /// <param name="pidval">父节点值</param>
    /// <param name="cFieldName">子节点字段名称</param>
    /// <param name="pFiledName">父节点字段名称</param>
    /// <returns></returns>

    public static List<String> RecurFromTable(IDao dao, String tableName, String pidval, String cFieldName,String pFiledName)
    {
        if (Strings.isBlank(cFieldName)) cFieldName = "id";
        if (Strings.isBlank(pFiledName)) pFiledName = "pid";
        TreeModel tm = new TreeModel();//Tree数据模型
        tm.RecurFromTable(pidval, tableName, cFieldName, pFiledName, dao);
        List<String> ids = tm.GetIds();
        return ids;
    }



}
