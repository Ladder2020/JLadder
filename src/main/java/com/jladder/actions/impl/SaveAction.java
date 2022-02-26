package com.jladder.actions.impl;

import com.jladder.actions.Curd;
import com.jladder.data.AjaxResult;
import com.jladder.data.ReStruct;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.datamodel.DataModelType;
import com.jladder.datamodel.GenBeanTool;
import com.jladder.datamodel.IDataModel;
import com.jladder.db.*;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.hub.DataHub;
import com.jladder.lang.*;
import com.jladder.lang.func.Func1;
import com.jladder.net.http.HttpHelper;
import com.jladder.web.WebContext;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 增删改的执行处理服务类
 */
public class SaveAction{
    //    /// <summary>
//    /// 最近执行语句
//    /// </summary>
//    public static List<KeyValue<String, IEnumerable<DbParameter>>> Recent => BaseSupport.Recent;
//
    /// <summary>
    /// 清除模版日志
    /// </summary>
    /// <param name="name">模型名称</param>
    /// <returns></returns>
    public static AjaxResult clearCache(String name){
        if (Strings.isBlank(name)) DataHub.WorkCache.removeAllDataModelCache();
        else DataHub.WorkCache.removeDataModelCache(name);
        return new AjaxResult();
    }
    public static AjaxResult insert(String tableName, Object bean){
        return saveBean(DaoSeesion.getDataModel(tableName), bean, null, DbSqlDataType.Insert, true);
    }
    /// <summary>
    /// 新增记录
    /// </summary>
    /// <param name="tableName">模版名称</param>
    /// <param name="bean">记录对象</param>
    /// <param name="supportTran">是否支持事务</param>
    /// <returns></returns>
    public static AjaxResult insert(String tableName, Object bean, boolean supportTran)
    {
        return saveBean(DaoSeesion.getDataModel(tableName), bean, null, DbSqlDataType.Insert, supportTran);
    }
    public static AjaxResult insert(String tableName, Object bean, Cnd cnd){
        return insert(tableName,bean,cnd,true);
    }
    /// <summary>
    /// 新增记录(带校验条件)
    /// </summary>
    /// <param name="tableName">模版名称</param>
    /// <param name="bean">记录对象</param>
    /// <param name="cnd">校验重复条件</param>
    /// <param name="supportTran">是否支持事务</param>
    /// <returns></returns>
    public static AjaxResult insert(String tableName, Object bean, Cnd cnd, boolean supportTran){
        if(cnd==null || cnd.isBlank())throw Core.makeThrow("条件插入操作必须有条件");
        // 是否已有记录
        long count = QueryAction.getCount(tableName, cnd,null);
        if (count > 0){
            return new AjaxResult(601);
        }
        return saveBean(DaoSeesion.getDataModel(tableName), bean, null, DbSqlDataType.Insert, supportTran);
    }

    /// <summary>
    /// 新增记录
    /// </summary>
    /// <param name="dataModel">动态数据模型</param>
    /// <param name="bean">记录对象</param>
    /// <param name="supportTran">是否支持事务</param>
    public static AjaxResult insert(IDataModel dataModel, Object bean, boolean supportTran){
        return saveBean(dataModel, bean, DbSqlDataType.Insert, supportTran);
    }

    /**
     * 修改更新记录
     * @param dataModel 动态数据模型
     * @param bean 记录对象
     * @param condition 条件对象[cnd,record,dic,string等]
     * @param supportTran 是否支持事务
     * @return
     */
    public static AjaxResult update(IDataModel dataModel, Object bean, Object condition, boolean supportTran){
        return saveBean(dataModel, bean, condition, DbSqlDataType.Update, supportTran);
    }

    /***
     * 修改更新记录
     * @param tableName 模版名称
     * @param bean 记录对象
     * @param cnd 条件对象[cnd,record,dic,string等]
     * @return
     */
    public static AjaxResult update(String tableName, Object bean, Object cnd) {
        return update(tableName,bean,cnd,true);
    }

    /***
     * 修改更新记录
     * @param tableName 模版名称
     * @param bean 记录对象
     * @param condition 条件对象[cnd,record,dic,string等]
     * @param supportTran 是否支持事务
     * @return
     */

    public static AjaxResult update(String tableName, Object bean, Object condition, boolean supportTran)
    {
        if (condition == null) return new AjaxResult(444);
        return saveBean(DaoSeesion.getDataModel(tableName), bean, condition, DbSqlDataType.Update, supportTran);
    }

    /***
     * 删除记录
     * @param tableName 模版名称
     * @param condition 条件对象[cnd,record,dic,string等]
     * @return
     */
    public static AjaxResult delete(String tableName, Object condition){
        return delete(tableName,condition,true);
    }

    /**
     * 删除记录
     * @param tableName 模版名称
     * @param condition 条件对象[cnd,record,dic,string等]
     * @param supportTran 是否支持事务
     * @return
     */
    public static AjaxResult delete(String tableName, Object condition, boolean supportTran)
    {
        if (condition == null) return new AjaxResult(444);
        return saveBean(DaoSeesion.getDataModel(tableName), null, condition, DbSqlDataType.Delete, supportTran);
    }

    /***
     * 删除记录
     * @param dataModel 动态数据模型
     * @param condition 条件对象[cnd,record,dic,string等]
     * @param supportTran 是否支持事务
     * @return
     */
    public static AjaxResult delete(IDataModel dataModel, Object condition, boolean supportTran)
    {
        return saveBean(dataModel, null, condition, DbSqlDataType.Delete, supportTran);
    }

    /***
     * 保存对象
     * @param tableName
     * @param bean
     * @param condition
     * @return
     */
    public static AjaxResult save(String tableName, Object bean, Object condition){
        return save(tableName,bean,condition,true);
    }
    /**
     * 新增或者保存方法
     * @param tableName 模版名称
     * @param bean 数据对象
     * @param condition 条件
     * @param supportTran 是否支持事务
     * @return
     */
    public static AjaxResult save(String tableName, Object bean, Object condition, boolean supportTran)
    {
        IDataModel dm = DaoSeesion.getDataModel(tableName);
        if(dm==null||dm.isNull())return new AjaxResult(701);
        dm.setCondition(Cnd.parse(condition, dm));
        return save(dm, bean,null, supportTran);
    }

    /***
     * 新增或者保存方法
     * @param dataModel 数据模型对象
     * @param bean 数据对象
     * @param condition 条件
     * @param supportTran 是否支持事务
     * @return
     */
    public static AjaxResult save(IDataModel dataModel, Object bean,Object condition, boolean supportTran)
    {
        if(dataModel==null||dataModel.isNull())return new AjaxResult(701);
        return saveBean(dataModel,bean, condition, DbSqlDataType.Save,supportTran);
    }

    public static AjaxResult saveBean(String tableName, String bean, String condition, int option, String rel){
        return saveBean(tableName,bean,condition,option,rel,true);
    }
    /**
     * 通用保存方法
     * @param tableName 模版名称(键名)
     * @param bean 记录文本
     * @param condition 条件文本
     * @param option 操作选项 1:Insert 插入数据,2:Update 更新数据,3:Save保存数据(主键存在更新，主键不存在新增)
     * @param rel 携带回执
     * @param supportTran
     * @return 是否支持事务
     */
    public static AjaxResult saveBean(String tableName, String bean, String condition, int option, String rel, boolean supportTran){
        KeepDaoPool pool = new KeepDaoPool(supportTran);
        AjaxResult result = saveBean(pool, tableName, bean, option, condition, rel);
        if (pool.getTranDiff() != 0){
            pool.AllRollBack();
            result.data = result.data !=null ? result.data : result.message;
            if (result.success) result.set(500, "操作未有效保存");
        }
        else pool.AllCommit();
        return result;
    }
    public static AjaxResult saveBeans(String beans){
        return saveBeans(beans,true);
    }
    /***
     * 保存多个beans对象
     * @param beans beans对象文本
     * @param supportTran 是否支持事务
     * @return
     */
    public static AjaxResult saveBeans(String beans, boolean supportTran) {
        if (Strings.isBlank(beans)) return new AjaxResult(444);
        boolean isHaveContext = Regex.isMatch(beans, "(@@)|(\\$\\{)");
        KeepDaoPool keepDaoPool = new KeepDaoPool(supportTran);
        Record results = new Record();
        //以对象的形式访问
        if (beans.startsWith("{") && beans.endsWith("}")) {
            Record record = Json.toObject(beans, Record.class);
            if (record == null || record.size() < 1) return new AjaxResult(444);
            //以普通savebean的方式保存
            if (Strings.hasValue(Collections.haveKey(record, "tableName")) && Strings.hasValue(Collections.haveKey(record, "option"))) {
                AjaxResult result = action(record,keepDaoPool,null,isHaveContext);
                keepDaoPool.end();
                return result;
            }
            //以多键的方式访问
            else {
                try {
                    //具体执行代码-->数据绑定+
                    for (String key : record.keySet()) {
                        Record dic = Record.parse(record.get(key));
                        AjaxResult ajaxJson = action(dic, keepDaoPool, results,isHaveContext);
                        results.put(key,ajaxJson.data);
                        if (ajaxJson.statusCode != 200) {
                            keepDaoPool.AllRollBack();
                            return ajaxJson.setData(results);
                        }
                    }
                } catch (Exception e) {
                    keepDaoPool.AllRollBack();
                    return new AjaxResult(500, "执行过程发生异常").setData(results.put("ErrorString", e.getMessage()));
                }
                keepDaoPool.end();
                return new AjaxResult(200, "访问成功").setData(results);
            }
        }
        //以数组的形式多bean的形式，无回调和数据绑定
        if (beans.startsWith("[") && beans.endsWith("]")) {
            List<Record> records = Json.toObject(beans, new TypeReference<List<Record>>(){});
            if (records == null || records.size() < 1) return new AjaxResult(444);
            if (records.size() == 1) {
                AjaxResult result = action(records.get(0), keepDaoPool, null, false);
                if (result.statusCode != 200) {
                    keepDaoPool.AllRollBack();
                    return result;
                }
                keepDaoPool.end();
                return result;
            }
            AjaxResult re_t = new AjaxResult();
            try {
                for (int i=0;i<records.size();i++) {
                    AjaxResult result = action(records.get(i), keepDaoPool, results, isHaveContext);
                    if(result.statusCode!=200){
                        re_t.set(result.statusCode,result.message);
                        keepDaoPool.AllRollBack();
                        return re_t;
                    }
                    results.put(i+"",result.data);
                    re_t.pushData(result);
                }
            } catch (Exception e) {
                keepDaoPool.AllRollBack();
                return re_t.setStatusCode(500).setMessage("执行过程出现异常").pushData(e.getMessage());
            }
            keepDaoPool.end();
            return re_t;
        }
        return new AjaxResult(400, "请求格式未识别");
    }
    public static Receipt saveBeans(Collection<Curd> curds){
        return saveBeans(curds,true);
    }

    public static Receipt saveBeans(Collection<Curd> curds,boolean supportTran){
        KeepDaoPool keepDaoPool = new KeepDaoPool(supportTran);
        List<AjaxResult> results = new ArrayList<AjaxResult>();
        for (Curd curd : curds)
        {
            AjaxResult receipt = saveBean(keepDaoPool, curd.TableName, curd.Bean, curd.Option, curd.Condition, curd.Rel);
            results.add(receipt);
            if (!receipt.success)
            {
                keepDaoPool.AllClose();
                return new Receipt(false, receipt.message).setData(results);
            }
        }
        keepDaoPool.end();
        return new Receipt().setData(results);
    }


    /***
     * 保存数据对象
     * @param dataModel 动态数据模型
     * @param entry 实体数据
     * @param option 操作选项
     * @param supportTran 支付事务
     * @return
     */
    public static AjaxResult saveBean(IDataModel dataModel, Object entry, int option, boolean supportTran) {
        KeepDaoPool keepDaoPool = new KeepDaoPool(supportTran);
        AjaxResult result = saveBean(keepDaoPool, dataModel, Record.parse(entry), option, null, null);
        if (keepDaoPool.getTranDiff() != 0) {
            keepDaoPool.AllRollBack();
            result.data = result.data !=null ? result.data : result.message;
            if (result.success) result.set(500, "操作未有效保存");
        }
        else keepDaoPool.AllCommit();
        return result;
    }


    /***
     * 保存数据对象
     * @param dataModel 动态数据模型
     * @param entity 实体数据
     * @param cnd 条件对象
     * @param action 操作动作
     * @param supportTran 是否支持事务
     * @return
     */
    public static AjaxResult saveBean(IDataModel dataModel, Object entity, Object cnd, DbSqlDataType action, boolean supportTran){
        if (dataModel == null || dataModel.isNull()) return new AjaxResult(700);
        KeepDaoPool keepDaoPool = new KeepDaoPool(supportTran);
        Record entityBean = GenBeanTool.gen(dataModel, Record.parse(entity), action);
        if (cnd != null) dataModel.setCondition(Cnd.parse(cnd, dataModel));
        AjaxResult result = saveBean(keepDaoPool, dataModel, entityBean, action.getIndex(), null, null);
        if (keepDaoPool.getTranDiff() != 0){
            keepDaoPool.AllRollBack();
            result.data = result.data!=null ? result.data : result.message;
            if (result.success){
                result.set(500, "操作未有效保存");
            }
        }
        else keepDaoPool.AllCommit();
        return result;
    }

    /***
     * 保存数据对象
     * @param dataModel  动态数据模型
     * @param entity 实体数据
     * @param action 操作动作
     * @param supportTran
     * @param <T> 实体泛型对象 支持事务
     * @return
     */

    public static <T> AjaxResult saveBean(IDataModel dataModel, T entity, DbSqlDataType action, boolean supportTran){
        if (dataModel == null || dataModel.isNull()) return new AjaxResult(700, "配置条件不满足请求");
        KeepDaoPool keepDaoPool = new KeepDaoPool(supportTran);
        Record bean = Record.parse(entity);
        if ( DbSqlDataType.Insert.equals(action) ||  DbSqlDataType.Update.equals(action)){
            bean = GenBeanTool.gen(dataModel, bean, action);
        }
        AjaxResult result = saveBean(keepDaoPool, dataModel, bean, action.getIndex(), null, null);
        if (keepDaoPool.getTranDiff() != 0){
            keepDaoPool.AllRollBack();
            result.data = result.data!=null? result.data : result.message;
            if (result.success) result.set(500, "操作未有效保存");
        }
        else keepDaoPool.AllCommit();
        return result;
    }

    /***
     * 操作数据方法(最终方法)
     * 1,keepDaoPool必须外部实例，由过程新建的数据库连接由过程管理
     * 2,condition可以为空，即修改条件值DaoHeiper参数中
     * @param keepDaoPool 持续数据库连接池
     * @param dm 动态数据模型
     * @param bean 实体对象
     * @param option 操作选项
     * @param condition 条件文本
     * @param rel 资源回馈
     * @return
     */
    public static AjaxResult saveBean(KeepDaoPool keepDaoPool, IDataModel dm, Record bean, int option, String condition, String rel){
        long startTime = System.currentTimeMillis() ;
        if (keepDaoPool == null) return new AjaxResult(201);
        if (dm == null || dm.isNull()) return new AjaxResult(700).setDuration(startTime);
        KeepDao keepDao = keepDaoPool.createKeepDao(dm.getConn());
        IDao dao = keepDao.Dao;
        dao.setTag(dm.getRaw().Name);
        //提请一次
        keepDao.take();
        //置当前连接为最前链接
        keepDaoPool.setActive(keepDao);
        AnalyzeAction analyze = new AnalyzeAction(dm);
        try{
            final List<Record>[] _rs = new List[]{null};//要进行修改的记录
            int rows = 0;
            if ((bean == null || bean.size() < 1) && option != DbSqlDataType.Delete.getIndex()){
                keepDaoPool.AllRollBack();
                analyze.endPoint();
                return new AjaxResult(404, "保存数据不存在").setDuration(startTime);
            }
            AjaxResult ajaxJson = new AjaxResult(200, "新增保存成功").setRel(rel).setDataName(dm.getRaw().Name);
            Func1<AjaxResult> EndPoint = () -> { analyze.endPoint(); return ajaxJson;};
            if (option == DbSqlDataType.Save.getIndex()){
                dm.setCondition(condition);
                condition = null;
                if (dm.getWhere().isBlank()){
                    keepDaoPool.AllRollBack();
                    analyze.endPoint();
                    return new AjaxResult(404, "保存数据条件未指定").setDuration(startTime);
                }
                int count = dao.count(dm.getTableName(),dm.getWhere());
                option = count < 1 ? DbSqlDataType.Insert.getIndex() : DbSqlDataType.Update.getIndex();
                //如果是新增重新加载一下默认值
                if (option ==  DbSqlDataType.Insert.getIndex()){
                    bean = GenBeanTool.gen(dm, bean, DbSqlDataType.Insert);
                }
            }
            if (option == DbSqlDataType.Insert.getIndex()){
                analyze.action = DbSqlDataType.Insert;
                //是否含有唯一性字段
                List<String> cols = dm.hasUniqueFields();
                if (cols.size() > 0){
                    Cnd cnd = new Cnd();
                    for (String key : cols){
                        //保存对象不含有唯一性字段，则直接报错误
                        if (Strings.isBlank(bean.haveKey(key))) return EndPoint.invoke().setStatusCode(500).setMessage("新增数据不存在必填项").setDuration(startTime);
                        cnd.put(key, "=", bean.getString(key, true));
                    }
                    int c = dao.count(dm.getTableName(), cnd);
                    if (c > 0){
                        final String[] fieldnametitles = {""};
                        cols.forEach(key -> fieldnametitles[0] += Collections.getString(dm.getFieldConfig(key),"title","")+"["+key+"],");
                        return EndPoint.invoke().setStatusCode(600).setMessage("新增数据出现重复:" + Strings.rightLess(fieldnametitles[0],1)).setDuration(startTime).setDataName(dm.getName());
                    }
                }
                List rs = new ArrayList<Record>();
                rs.add(bean);
                //新增前检查
                List<Record> checkrelations = dm.getRelationAction("insertcheck");
                Receipt ret = handAction(keepDaoPool, checkrelations, rs, null, dm, true,null);
                if (checkrelations != null && !ret.isSuccess()){
                    return EndPoint.invoke().setStatusCode(405).setMessage("删除未通过检查").setData(ret).setDuration(startTime).setDataName(dm.getName());
                }
                //新增前动作
                List<Record> relations = dm.getRelationAction("insertbefore");
                ret = handAction(keepDaoPool, relations, rs, null, dm,false,(Record)ret.data);
                if (relations != null && !ret.isSuccess()){
                    return EndPoint.invoke().setStatusCode(500).setMessage("新增操作失败").setData(ret).setDuration(startTime).setDataName(dm.getName());
                }
                Record finalSaveEntity = bean;
                rows = dao.insertData(dm.getTableName(), bean, (rn, conn)->{
                    //处理自增字段
                    List<String> autos = dm.getFields("gen", "autonum");
                    if (autos != null && autos.size() > 0){
                        ResultSet resultSet=null;
                        PreparedStatement pre = conn.prepareStatement("select @@IDENTITY AS 'identity'");
                        try {
                            resultSet = pre.executeQuery();
                            if(resultSet.next()){
                                Object id = resultSet.getObject(1);
                                finalSaveEntity.put(autos.get(0),id);
                            }
                        }
                        finally {
                            if(resultSet!=null)resultSet.close();
                            if(pre!=null) pre.close();
                        }
                    }
                    return rn;
                });
                if (rows < 0){
                    return EndPoint.invoke().setStatusCode(500).setMessage("新增操作失败").setData(dao.getErrorMessage()).setDuration(startTime).setDataName(dm.getName());
                }
                analyze.setDataForAfter(bean);
                List<Record> irelations = dm.getRelationAction("insertafter");
                ret = handAction(keepDaoPool, irelations, rs, null, dm,false, (Record)ret.data);
                if (irelations != null && !ret.isSuccess()){
                    return EndPoint.invoke().setStatusCode(500).setMessage("更新操作失败").setData(ret).setDuration(startTime).setDataName(dm.getName());
                }
                LatchAction.clearLatch(dm.getTableName());
                keepDao.finish();
                analyze.endPoint();
                //Log.writelog(daoHelper.GetTableName() + "新增:" + Json.toJson(saveEntity));

                //如果是以主键外置的方式新增，则进行一次查询
                //                    var pks= dataModel.GetFields("pk");
                //                    if (pks == null || pks.Count < 1) pks = dataModel.GetFields("gen", "uuid");
                //                    else pks.AddRange(dataModel.GetFields("gen", "uuid"));
                //                    if(pks==null)pks=new ArrayList<string>();
                //                    pks.Add("id");
                //                    var pkkey = saveEntity.HaveKey(Core.ArrayToString(pks, ",", null));
                //                    if (pkkey.HasValue())
                //                    {
                //                        Record rebean = dao.Fetch(dataModel.GetTableName(), pkkey, saveEntity.GetString(pkkey));
                //                        return ajaxJson.SetDataName(dataModel.GetTableName()).SetData(rebean);
                //                    }
                //                    else
                return ajaxJson.setDataName(dm.getTableName()).setData(bean).setDuration(startTime).setDataName(dm.getName());
            }

            if (option == DbSqlDataType.Delete.getIndex() || option == DbSqlDataType.Update.getIndex() || option == DbSqlDataType.Truncate.getIndex()){
                dm.setCondition(condition);
                if (dm.getWhere().isBlank()){
                    analyze.endPoint();
                    return new AjaxResult(500, "对不起，您的更新条件不合法").setDataName(dm.getName()).setRel(rel).setDuration(startTime).setDataName(dm.getName());
                }
            }
            final boolean[] geted = {false};//已经取过记录集
            Func1<List<Record>> old = () ->{
                if (geted[0]){
                    return _rs[0];
                }
                else{
                    _rs[0] = dao.query(dm.getSqlText());
                    geted[0] = true;
                    if (Strings.hasValue(dao.getErrorMessage())){
                        EndPoint.invoke().setStatusCode(500).setMessage("原数据记录获取出错").setData(dao.getErrorMessage()).setDuration(startTime).setDataName(dm.getName());
                    }
                    if (Core.isEmpty(_rs[0])){
                        analyze.endPoint();
                        keepDao.finish();
                    }
                    return _rs[0];
                }
            };
            Func1<AjaxResult> assertBlank=() -> ajaxJson.setStatusCode(200).setMessage("操作的数据记录不存在").setDuration(startTime).setDataName(dm.getName());
            if (option == DbSqlDataType.Update.getIndex()){
                analyze.action = DbSqlDataType.Update;
                //重复字段的验证
                List<String> cols = dm.hasUniqueFields(bean);
                if (cols.size() > 0){
                    if (Core.isEmpty(old.invoke())) return assertBlank.invoke();
                    if (!Core.isEmpty(old.invoke()) && old.invoke().size() > 1){
                        return EndPoint.invoke().setStatusCode(500).setMessage("修改操作中含有重复检查，不能进行批量修改").setDuration(startTime).setDataName(dm.getName());
                    }
                    List<String> gens = dm.getFields("pk");
                    if (gens == null || gens.size() < 1) gens = dm.getFields("gen", "uuid", "id", "autonum");
                    List<String> finalGens = gens;
                    if (gens == null || gens.size() < 1 || Collections.count(old.invoke(), x -> Strings.isBlank(Collections.haveKey(x, finalGens.get(0)))) > 0){
                        return EndPoint.invoke().setStatusCode(500).setMessage("重复检查操作中，不存在主键,或者主键值为空").setDuration(startTime).setDataName(dm.getName());
                    }
                    Cnd cnd = new Cnd(gens.get(0),"<>", Collections.getString(old.invoke().get(0),gens.get(0),"", true));
                    for (String key : cols){
                        //var whereStr = key + "='" + saveEntity?.GetString(key, true) + "' and " + gens[0] + " <> '" + old()[0].GetString(gens[0], true) + "'";
                        cnd.put(key, bean.getString(key, true));
                    }
                    int c = dao.count(dm.getTableName(), cnd);
                    if (c > 0){
                        final String[] fieldnametitles = {""};
                        cols.forEach(key -> fieldnametitles[0] += Collections.getString(dm.getFieldConfig(key),"title","")+"["+key+"],");
                        return EndPoint.invoke().setStatusCode(600).setMessage("对不起,修改操作中出现数据重复:" + Strings.rightLess(fieldnametitles[0],1)).setDuration(startTime).setDataName(dm.getName());
                    }
                }
                Receipt ret=null;//后续动作的处理结果
                analyze.setDataForUpdateBefore(old);
                analyze.setDataForAfter(bean);
                //判断数据是过期
                if(!analyze.checkOutDate(old, bean)){
                    return EndPoint.invoke().setStatusCode(500).setMessage("数据过期检查没有通过").setDuration(startTime).setDataName(dm.getName());
                }
                //修改的检查
                List<Record> relations = dm.getRelationAction("updatecheck");
                if (!Core.isEmpty(relations)){
                    if (Core.isEmpty(old.invoke())) return assertBlank.invoke();
                    ret = handAction(keepDaoPool, relations, old.invoke(), bean, dm, true,null);
                    if (!ret.isSuccess()){
                        return EndPoint.invoke().setStatusCode(405).setMessage("删除未通过检查").setData(ret).setDuration(startTime).setDataName(dm.getName());
                    }
                }
                //更新前的后续动作
                relations = dm.getRelationAction("updatebefore");
                if (!Core.isEmpty(relations)){
                    if (Core.isEmpty(old.invoke())) return assertBlank.invoke();
                    ret = handAction(keepDaoPool, relations, old.invoke(), bean, dm, false, (Record)ret.data);
                    if (ret.isSuccess()){
                        return EndPoint.invoke().setStatusCode(500).setMessage("更新操作失败").setData(ret).setDuration(startTime).setDataName(dm.getName());
                    }
                }
                relations = dm.getRelationAction("updateafter");
                if (!Core.isEmpty(relations)){
                    if (Core.isEmpty(old.invoke())) return assertBlank.invoke();
                }
                if (geted[0] && Rs.isBlank(_rs[0])) assertBlank.invoke();
                rows = dao.update(dm.getTableName(), bean, dm.getCondition());
                if (rows < 0){
                    return EndPoint.invoke().setStatusCode(500).setMessage("修改操作失败").setData(dao.getErrorMessage()).setDuration(startTime).setDataName(dm.getName());
                }
                if (rows == 0){
                    keepDao.finish();
                    return assertBlank.invoke();
                }
                //更新后事件
                relations = dm.getRelationAction("updateafter");
                if (!Core.isEmpty(relations)){
                    ret = handAction(keepDaoPool, relations, old.invoke(), bean, dm, false, ret==null?null:(Record)ret.data);
                    if (!ret.isSuccess()){
                        return EndPoint.invoke().setStatusCode(500).setMessage("更新操作失败").setData(ret).setDuration(startTime).setDataName(dm.getName());
                    }
                }
                //更新修改报告
                relations = dm.getRelationAction("updatereport");
                if (!Core.isEmpty(relations)){
                    Record report = new Record();
                    report.put("oldrows", old.invoke().size());
                    report.put("summary", analyze.getDifferentReport(null));
                    ret = handAction(keepDaoPool, relations, old.invoke(), bean.put("$report", report), dm, false, (Record)ret.data);
                    if (!ret.isSuccess()){
                        return EndPoint.invoke().setStatusCode(500).setMessage("更新操作失败").setData(ret).setDuration(startTime).setDataName(dm.getName());
                    }
                }
                LatchAction.clearLatch(dm.getTableName());
                keepDao.finish();
                analyze.endPoint();
                return new AjaxResult(200, "成功修改"+rows+"条数据").setData(bean).setDuration(startTime).setDataName(dm.getName()).setXData(analyze.differentChange(null,null)).setRel(rel);
            }
            if (option == DbSqlDataType.Delete.getIndex() || option == DbSqlDataType.Truncate.getIndex()){
                Receipt ret = null;
                analyze.action = DbSqlDataType.Delete;
                //删除前检查
                List<Record> relations = dm.getRelationAction("deletecheck");
                if (!Core.isEmpty(relations)){
                    if (Core.isEmpty(old.invoke())) return assertBlank.invoke();
                    ret = handAction(keepDaoPool, relations, old.invoke(), null, dm, true,null);
                    if (!ret.isSuccess()){
                        return EndPoint.invoke().setStatusCode(405).setMessage("删除未通过检查").setDataName(dm.getName()).setData(ret).setDuration(startTime).setDataName(dm.getName());
                    }
                }
                //删除前关联操作
                relations = dm.getRelationAction("deletebefore");
                if (!Core.isEmpty(relations)){
                    if (Core.isEmpty(old.invoke())) return assertBlank.invoke();
                    ret = handAction(keepDaoPool, relations, old.invoke(), null, dm, false, ret==null?null:(Record)ret.data);
                    if ( !ret.isSuccess()){
                        return EndPoint.invoke().setStatusCode(500).setMessage("删除操作失败").setDataName(dm.getName()).setData(ret).setDuration(startTime).setDataName(dm.getName());
                    }
                }
                //删除后事件，进行前置检查
                relations = dm.getRelationAction("deleteafter");
                if (!Core.isEmpty(relations) && Core.isEmpty(old.invoke())) return assertBlank.invoke();
                if (geted[0] && Rs.isBlank(_rs[0])) return assertBlank.invoke();
                //强制删除
                if (option == DbSqlDataType.Truncate.getIndex()){
                    analyze.action = DbSqlDataType.Delete;
                    analyze.setDataForDeleteBefore(old);
                    if (geted[0] && Rs.isBlank(_rs[0])) return assertBlank.invoke();
                    rows = dao.delete(dm.getTableName(), dm.getWhere());
                }
                else{
                    //是否含有删除位
                    List<String> deleteFields = dm.getFields("sign", "isdelete");
                    if (Core.isEmpty(deleteFields)){
                        analyze.setDataForDeleteBefore(old);
                        if (geted[0] && Rs.isBlank(_rs[0])) return assertBlank.invoke();
                        rows = dao.delete(dm.getTableName(), dm.getWhere());
                    }
                    else{
                        Record record = new Record();
                        record.put(deleteFields.get(0), "1");
                        analyze.action=DbSqlDataType.Update;
                        analyze.setDataForUpdateBefore(old);
                        analyze.setDataForAfter(record);
                        if (geted[0] && Rs.isBlank(_rs[0])) return assertBlank.invoke();
                        rows = dao.update(dm.getTableName(), record, dm.getCondition());
                    }
                }
                if (rows < 0){
                    return EndPoint.invoke().setStatusCode(500).setMessage("删除操作失败").setDataName(dm.getName()).setData(dao.getErrorMessage()).setDuration(startTime).setDataName(dm.getName());
                }
                if (rows == 0){
                    keepDao.finish();
                    return assertBlank.invoke();
                }
                //删除后事件处理
                if (!Core.isEmpty(relations)){
                    ret = handAction(keepDaoPool, relations, old.invoke(), null, dm, false, ret==null?null:(Record)ret.data);
                    if (!ret.isSuccess()){
                        return EndPoint.invoke().setStatusCode(500).setMessage("删除操作失败").setDataName(dm.getName()).setData(ret).setDuration(startTime).setDataName(dm.getName());
                    }
                }
                LatchAction.clearLatch(dm.getTableName());
                keepDao.finish();
                analyze.endPoint();
                return new AjaxResult(200, "成功删除"+rows+"条数据数据").setRel(rel).setDataName(dm.getName()).setDuration(startTime).setDataName(dm.getName());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            analyze.endPoint();
            return new AjaxResult(500, "执行操作失败").setRel(rel).setData(e.getMessage()).setDataName(dm.getName()).setDuration(startTime).setDataName(dm.getName());
        }
        analyze.endPoint();
        keepDao.finish();
        return new AjaxResult(400, "未知错误").setDataName(dm.getName()).setRel(rel).setDuration(startTime).setDataName(dm.getName());
    }

    /***
     * 操作数据
     * @param keepDaoPool 持续数据库连接池
     * @param tableName 键表名
     * @param bean 实体数据对象
     * @param option 操作选项
     * @param condition 条件
     * @param rel 资源回馈
     * @return
     */

    public static AjaxResult saveBean(KeepDaoPool keepDaoPool, String tableName, String bean, int option, String condition, String rel)
    {
        IDataModel dm = DaoSeesion.getDataModel(tableName);
        if (dm.isNull()) return new AjaxResult(700);
        if (option == 3)
        {
            KeepDao keyDao = keepDaoPool.createKeepDao(dm.getConn());
            dm.setDialect(keyDao.Dao.getDialect());
            dm.setCondition(Cnd.parse(condition, dm));
            long count = 0;
            if (DataModelType.Data.equals(dm.Type ))
            {
                Receipt latch = LatchAction.getData(dm);
                if (latch.isSuccess())
                {
                    count = ((List<Record>) latch.data).size();
                }
            }
            count = keyDao.Dao.getValue(new SqlText("select count(1) from " + dm.getTableName() + dm.getWhere().cmd + dm.getGroup(),dm.getCondition().parameters),Long.class);
            if(count==-1)return new AjaxResult(404,"获取条件数据失败");
            option = count > 0 ? 2 : 1;
        }
        Record entityBean = GenBeanTool.gen(dm, bean, option);
        return saveBean(keepDaoPool, dm, entityBean, option, condition, rel);
    }

    /***
     * 操作数据
     * @param keepDaoPool 持续数据连接池
     * @param dataModel 动态数据模型
     * @param entityRecord 实体数据记录
     * @param action 操作动作
     * @param cnd 条件
     * @return
     */

    public static AjaxResult saveBean(KeepDaoPool keepDaoPool, IDataModel dataModel, Record entityRecord, DbSqlDataType action, Cnd cnd)
    {
        return saveBean(keepDaoPool, dataModel, entityRecord,action, cnd);
    }

    /***
     * 处理后续动作操作和前置检查
     * 1，daoheip是独立的，考虑到保存数据过程不会存在大量模版，待日后改进
     * 2，前置检查，只为查询记录数量实现，可扩展到查询字段以及匹配表达式
     * 3，缺失后续动作和后续动作之间数据关联
     * 4，以上所有欠缺实际应用都未涉及到，客观都不可以忽略
     * @param keepDaoPool 持续数据库连接池
     * @param relationList 逻辑关系列表
     * @param rs 当前记录集
     * @param updateBean 更新的记录
     * @param dm 数据模型
     * @param check 是否检查
     * @param context 上下文数据对象
     * @return
     */
    public static Receipt handAction(KeepDaoPool keepDaoPool, List<Record> relationList, List<Record> rs, Record updateBean, IDataModel dm, boolean check,Record context)
    {
        if (relationList == null) return new Receipt(false);
        if (keepDaoPool == null) return new Receipt(false);
        //throw Core.makeThrow("未实现");
        IDao activeDao = keepDaoPool.getActive().Dao;

        Record retData = new Record();
        for (Record r : rs)
        {
            r.merge(updateBean);
            for (Record act : relationList)
            {
                String actionName = act.getString("name", true);
                String option = act.getString("option", true);
                //执行script脚本
                if (Strings.hasValue(option) && Convert.toInt(option)==DbSqlDataType.Script.getIndex()){
                        return new Receipt(false, "不支持脚本命令");
//                        String jscode = act.getString("script", true);
//                        if (Strings.isBlank(jscode)) jscode = dm.getScript();
//                        if (Strings.isBlank(jscode)) continue;
//                        String fname = act.getString("function,funname,tablename", true);
//                        if (Strings.isBlank(fname)) continue;
//                        fname = fname.replace("()", "").trim();
//                        continue;
                    }
                    //执行反射
                    if (Strings.hasValue(option) && Convert.toInt(option) == DbSqlDataType.Program.getIndex())
                    {
                        String className = act.getString("class,type,function,tablename", true);
                        if (Strings.isBlank(className)) continue;
                        String methodName = act.getString("method,function", true);
                        if (Strings.isBlank(methodName))
                        {

                            methodName = Collections.last(className.split("."));
                            className = Strings.rightLess(className,methodName.length() + 1);
                        }
                        Record methodRecord = Core.or(Record.parse(act.getString("params,bean")),new Record()).mapping(r);
                        methodRecord.put("_bean", r);
                        methodRecord.put("_tableName", dm.TableName);
                        String path = act.getString("path", true);
                        Class pclass = null;
                        if (Strings.hasValue(path)){
                            return new Receipt(false, "不支持从dll文件加载");
                        }
                        else{
                            if (className.indexOf("|") > 0){
                                className = className.replace("|", ".");
                            }
                            pclass = Refs.getClazz(className);
                        }
                        if (pclass == null) throw Core.makeThrow("类[" + className + "]不存在");
                        Method method = Refs.getMethod(pclass, methodName);
                        if (method == null) continue;
                        Receipt ret = Refs.invoke(method, methodRecord);
                        if (ret == null) continue;
                        if (ret.data instanceof Boolean && !(Boolean)ret.data) return new Receipt(false);
                        if (ret.data instanceof AjaxResult && !((AjaxResult)ret.data).success) return new Receipt(false, ((AjaxResult)ret.data).message);
                        if (Strings.hasValue(actionName)) retData.put(actionName, ret);
                        continue;
                    }
                    //网络请求
                    if (Strings.hasValue(option) && Convert.toInt(option)==DbSqlDataType.Http.getIndex()){
                        String url = Strings.mapping( act.getString("url,class,type,function,tablename", true),"host",WebContext.getHost());
                        String ps = Strings.mapping(act.getString("params,bean"),r);
                        String type = act.getString("method,type");
                        Receipt<String> ret = Regex.isMatch(type, "post") ? HttpHelper.request(url, ps, "POST", null) : HttpHelper.request(url, ps, "GET", null);
                        if (Strings.hasValue(actionName)) retData.put(actionName, ret.getData());
                        continue;
                    }
                //不含有sql语句的情况
                String sqlstr = act.getString("sql");
                if (Strings.isBlank(sqlstr)){
                    //var beanjson = UrlEncoder.Default.Encode(r.ToString());
                    String beanjson = r.toString();
                    String tableName = act.getString("tableName", true);
                    String beans = Strings.mapping(Json.toJson(act.getObject("bean", true)),r.put("_bean", beanjson));
                    String condition = Json.toJson(act.getObject("condition", true));
                    if (Strings.isBlank(tableName) || Strings.isBlank(option)) continue;
                    if (Strings.hasValue(condition)) condition = Strings.mapping(condition,r);
                    //当后续动作是前置检查时
                    if (check){
                        if ("123".equals(option)){
                            IDataModel cdm = DaoSeesion.getDataModel(tableName, Json.toJson(act.getString("param", true)));
                            if (cdm == null || cdm.isNull() || !cdm.enable()) return new Receipt(false, "模版解析错误或者禁用");
                            KeepDao keepdao = keepDaoPool.get(cdm.getConn());
                            String cSqlText = "select count(0) from (select 1 from " + cdm.TableName + cdm.getWhere().cmd + cdm.getGroup() + ")";
                            if (keepdao == null){
                                IDao dao = DaoSeesion.NewDao(cdm.getConn());
                                int total = dao.getValue(new SqlText(cSqlText, cdm.getCondition().getParameters()),Integer.class);
                                dao.close();
                                if (total > 0) return new Receipt(false);
                                if (Strings.hasValue(actionName)) retData.put("actionName", total);
                                continue;
                                //return new Receipt();
                            }
                            else{
                                int total = keepdao.Dao.getValue(new SqlText(cSqlText, cdm.getCondition().getParameters()),Integer.class);
                                if (total > 0) return new Receipt(false);
                                if (Strings.hasValue(actionName)) retData.put("actionName", total);
                                continue;
                                //return new Receipt();
                            }
                        }
                        return new Receipt(false,"暂时不支持handlebean的方式进行查询检查" );
                    }
                    //唯一性字段，一般指ID,快速对应，简便，但扩展性不强
                    String fieldname = act.getString("fieldname", true);
                    if (Strings.hasValue(fieldname)){
                        condition = "{" + fieldname + ":'" + r.getString(fieldname, true) + "'}";
                    }
                    IDataModel daoHelp = DaoSeesion.getDataModel(tableName);
                    beans = Strings.mapping(beans,retData);
                    beans = Strings.mapping(beans,context);
                    AjaxResult ajaxJson = saveBean(keepDaoPool, daoHelp, GenBeanTool.gen(daoHelp, beans, Integer.parseInt(option)), Integer.parseInt(option), condition, null);
                    if (ajaxJson.statusCode != 200) return new Receipt(false).setData(ajaxJson.message).setData(ajaxJson.data);
                    else if (Strings.hasValue(actionName)) retData.put(actionName, ajaxJson.data);
                }
                //含有sql语句的情况
                else{
                    IDao dao;
                    boolean newCreate = false;
                    sqlstr = Strings.mapping(sqlstr,r);
                    String conn = act.getString("conn", true);
                    if (Strings.isBlank(conn)) dao = activeDao;
                    else{
                        dao = DaoSeesion.NewDao(conn);
                        newCreate = true;
                    }
                    //如果是非操作检查的，进行update delete insert 等操作
                    if (!check){
                        int n = dao.exec(new SqlText(sqlstr));
                        if (newCreate) dao.close();
                        if (n < 0) return new Receipt(false);
                        else{
                            if (Strings.hasValue(actionName)) retData.put(actionName, n);
                        }
                    }
                    else{
                        //操作检查的，进行记录查询工作
                        if (Regex.isMatch(sqlstr,"select[\\s]*count[\\w\\W]")){
                            //含有select count 语句
                            Record rec = dao.fetch(new SqlText(sqlstr));
                            if (rec == null && Strings.hasValue(dao.getErrorMessage())) return new Receipt(false);
                            int resultcount = rec.getInt(0);
                            if (newCreate) dao.close();
                            if (resultcount > 0) return new Receipt(false);
                            else if (Strings.hasValue(actionName)) retData.put(actionName, resultcount);
                        }
                        else{
                            List<Record> rec = dao.query(new SqlText(sqlstr));
                            if (newCreate) dao.close();
                            if (rec.size() > 0) return new Receipt(false);
                            else if (Strings.hasValue(actionName)) retData.put(actionName, rec);
                        }
                    }
                }
            }
        }
        return new Receipt().setData(retData);
    }

    public static AjaxResult action(Record act,KeepDaoPool pool,Record results,boolean context){
        String option = act.getString("option");
        if(Strings.isBlank(option))return new AjaxResult(400,"操作选项不能为空[1175]");
        AjaxResult result;
        Receipt ret;
        boolean pool_created=false;
        switch (option.toLowerCase()){
            case "insert":
                option="1";
            case "update":
                option="2";
            case "save":
                option="3";
            case "delete":
                option="-1";
            case "truncate":
                option="-1111";
            case "1":
            case "2":
            case "3":
            case "-1":
            case "-1111":
            case "0":
            case "11":
            case "22":
                String tableName=act.getString("tablename",true);
                if(Strings.isBlank(tableName))return new AjaxResult(404,"数据模型不存在[1098]");
                result = new AjaxResult(444,"未处理[1103]");
                IDataModel dm = DaoSeesion.getDataModel(tableName);
                if (dm.isNull()) {
                    return new AjaxResult(700, "[" + tableName + "]动态数据模版未找到").setData(results);
                }
                String rel = act.getString("rel");
                String param = Json.toJson(act.get("param"));
                String condition = Json.toJson(act.get("condition"));
                String columns = Json.toJson(act.get("columns"));
                AjaxResult ajaxJson = null;//当次的处理结果
                Record entrybean = null;//当次的对象数据
                if ("1".equals(option)  || "2".equals(option) || "3".equals(option)) {
                    Object bean = act.get("bean");
                    if (bean == null) {
                        return new AjaxResult(500, "新增数据未组装").setData(results).setDataName(tableName);
                    }
                    if (bean instanceof String) {
                        //转化bean的类型
                        String beanStr = bean.toString().trim();
                        if (beanStr.startsWith("{") && beanStr.endsWith("}")) {
                            bean = Json.toObject(beanStr, Record.class);
                        }
                    }
                    bean = Record.parse(bean);
                    if (bean != null) {
                        //bean = Record.Parse(bean);
                        StringBuilder message = new StringBuilder();
                        if (context) {
                            Receipt<Record> re_t = GenBeanTool.match(results, (Record) bean);
                            if (!re_t.result) {
                                return new AjaxResult(400, re_t.message).setData((Record) bean).setDataName(tableName);
                            }
                            entrybean = GenBeanTool.gen(dm, re_t.data, DbSqlDataType.get(Convert.toInt(option)), message);
                            //entrybean =  re_t.data;
                        } else{
                            entrybean = GenBeanTool.gen(dm, (Record) bean, DbSqlDataType.get(Convert.toInt(option)), message);
                            //entrybean = (Record) bean;
                        }
                        if (message.length() > 0) {
                            return new AjaxResult(400, message.toString()).setData(results).setDataName(tableName);
                        }
                        if (entrybean == null || entrybean.size() < 1) {
                            return new AjaxResult(400, "保存对象未有效数据").setData(results).setDataName(tableName);
                        }
                    }
                }
                if ("-1".equals(option) || "0".equals(option)  || "2".equals(option) || "3".equals(option)   ) {
                    if (Strings.isBlank(condition)) {
                        return new AjaxResult(500, "未有修改条件").setData(results).setDataName(tableName);
                    }
                }
                //动态联想数据
                if (Convert.toInt(option) != 1 && context) {
                    condition = Strings.mapping(condition, results);
                }
                if(pool==null){
                    pool = new KeepDaoPool(true);
                    pool_created=true;
                }
                switch (Convert.toInt(option)) {
                    case 0:
                        result = QueryAction.getBean(tableName, columns, condition, param, rel);
                        break;
                    case 1://新增数据
                        result = saveBean(pool, dm, entrybean, Convert.toInt(option), "", rel);
                        break;
                    case 2:
                    case 3://保存动作
                        result = saveBean(pool, dm, entrybean,  Convert.toInt(option), condition, rel);
                        break;
                    case -1:
                        result = saveBean(pool, dm, new Record(),  Convert.toInt(option), condition, rel);
                        break;
                    case 11:
                        result = QueryAction.getData(tableName, condition, columns, param, rel);
                        break;
                    case 22:
                        int psize = act.get("psize") == null ? 0 : act.getInt("psize");
                        result = QueryAction.queryData(tableName, condition, psize, param, act.getString("columns"));
                        break;
                }
                if(pool_created){
                    if (pool.getTranDiff() != 0){
                        pool.AllRollBack();
                        result.data = ajaxJson.data !=null ? ajaxJson.data : result.message;
                        if (result.success) result.set(500, "操作未有效保存");
                    }
                    else pool.AllCommit();
                    return result;
                }
                return result;
            case "6":
            case "http":
                ret =  HttpHelper.request(
                        act.getString("url,path,tablename",true),
                        act.getString("data,params,bean",true),
                        act.getString("method,type","POST",true),
                        Record.parse(act.getString("header",true))
                );
                return ret.toResult();
            case "4":
            case "script":
                return new AjaxResult(500,"不支持Script脚本");
            case "5":
            case "program":
                String className = act.getString("class,type,function,tablename", true);
                if (Strings.isBlank(className)) return new AjaxResult(500,"类路径不存在");
                String methodName = act.getString("method,function", true);
                if (Strings.isBlank(methodName)) {
                    methodName = Collections.last(className.split("."));
                    className = Strings.rightLess(className,methodName.length() + 1);
                }
                Record methodRecord = Core.or(Record.parse(act.getString("params,bean")),new Record());
                String path = act.getString("path", true);
                Class pclass = null;
                if (Strings.hasValue(path)){
                    return new AjaxResult(500, "不支持从文件加载");
                }
                else{
                    if (className.indexOf("|") > 0){
                        className = className.replace("|", ".");
                    }
                    pclass = Refs.getClazz(className);
                }

                if (pclass == null) throw Core.makeThrow("类[" + className + "]不存在");
                Method method = Refs.getMethod(pclass, methodName);
                if (method == null)  return new AjaxResult(500,"执行方法不存在");
                ret = Refs.invoke(method, methodRecord);
                if (ret == null) return new AjaxResult();
                if (ret.data instanceof Boolean && !(Boolean)ret.data) return new AjaxResult(false);
                if (ret.data instanceof AjaxResult && !((AjaxResult)ret.data).success) return (AjaxResult)ret.data;
                return new AjaxResult().setData(ret.data);
            case "9999":
            case "sql":
                if(pool==null){
                    IDao dao = new Dao(act.getString("conn"));
                    int row = dao.exec(new SqlText(act.getString("sql")));
                    dao.close();
                    return row>-1 ? new AjaxResult().setData(row):new AjaxResult(500,dao.getErrorMessage());
                }else {
                    KeepDao keepDao = pool.createKeepDao(act.getString("conn"));
                    IDao dao = keepDao.Dao;
                    //提请一次
                    keepDao.take();
                    //置当前连接为最前链接
                    pool.setActive(keepDao);
                    int row = dao.exec(new SqlText(act.getString("sql")));
                    keepDao.finish();
                    return row>-1 ? new AjaxResult().setData(row):new AjaxResult(500,dao.getErrorMessage());
                }
        }
        return new AjaxResult(400,"操作选项未识别[1175]");

    }
    /***
     * 保存对象
     * @param dao 数据库操作对象
     * @param dataModel 数据模版
     * @param entityRecord 实体对象
     * @param action 执行动作
     * @param cnd 条件
     * @return
     */
    public static AjaxResult saveBean(IDao dao, IDataModel dataModel, Record entityRecord, DbSqlDataType action, Cnd cnd){
        if (dao == null) return new AjaxResult(201);
        if (dataModel == null || dataModel.isNull()) return new AjaxResult(700);
        Record saveEntity = new Record();
        if (DbSqlDataType.Insert.equals(action) || DbSqlDataType.Update.equals(action)) {
            StringBuilder message = new StringBuilder();
            saveEntity = GenBeanTool.gen(dataModel, entityRecord, action,message);
            if (saveEntity == null) return new AjaxResult(500, message.toString());
        }
        boolean meCreateTran = false;
        if (!dao.isTraning()){
            meCreateTran = true;
            dao.beginTran();
        }

        KeepDaoPool keepDaoPool = new KeepDaoPool(dao);
        AjaxResult result = saveBean(keepDaoPool, dataModel, saveEntity,action.getIndex(), cnd.getWhere(false,true), null);
        if (meCreateTran) {
            if (result.success) dao.commitTran();
            else dao.rollback();
        }
        keepDaoPool.end();
        return result;
    }

    /***
     * 保存bean对象
     * @param dao 数据库操作对象
     * @param tableName 键表名
     * @param bean 实体对象文本
     * @param option 操作选项
     * @param condition 条件文本
     * @param rel 资源回馈索引
     * @return
     */
    public static AjaxResult saveBean(IDao dao, String tableName, String bean, int option, String condition, String rel){
        if (Strings.isBlank(tableName)) return new AjaxResult(444).setRel(rel);
        if (dao == null) return new AjaxResult(201);
        IDataModel daoHelp = DaoSeesion.getDataModel(dao, tableName, null);
        if (daoHelp.isNull()) return new AjaxResult(700);
        if (daoHelp.Type != DataModelType.Table) return new AjaxResult(702);
        if (option == DbSqlDataType.Save.getIndex()){
            int count = dao.count(daoHelp.getTableName(), Cnd.parse(condition, daoHelp));
            if (count < 1) option = DbSqlDataType.Insert.getIndex();
            else option = (int)DbSqlDataType.Update.getIndex();
        }
        Record record = null;
        if (option != (int)DbSqlDataType.Delete.getIndex()) {
            StringBuilder message = new StringBuilder();
            record = daoHelp.genBean(bean, option, message);
            if (record == null) return new AjaxResult(500, message.toString());
        }
        boolean meCreateTran = false;
        if (!dao.isTraning()) {
            meCreateTran = true;
            dao.beginTran();
        }
        KeepDaoPool keepDaoPool = new KeepDaoPool(dao);
        AjaxResult result = saveBean(keepDaoPool, daoHelp, record, option, condition, rel);
        if (meCreateTran){
            if (result.success) dao.commitTran();
            else dao.rollback();
        }
        keepDaoPool.end();
        return result;
    }

    /***
     * 保存CURD对象
     * @param dao 数据库操作对象
     * @param dataModel 动态数据模型
     * @param curd CURD对象
     * @return
     */
    public static AjaxResult saveBean(IDao dao, IDataModel dataModel, Curd curd){
        return saveBean(dao, dataModel, curd.Bean, curd.Option, curd.Condition, curd.Rel);
    }

    /***
     * 保存bean对象
     * @param dao 数据库操作对象
     * @param dataModel 数据模版
     * @param bean 实体对象文本
     * @param option 操作动作
     * @param condition 条件文本
     * @param rel 资源回馈文本
     * @return
     */

    public static AjaxResult saveBean(IDao dao, IDataModel dataModel, String bean, int option, String condition, String rel){
        if (dataModel == null || dataModel.isNull()) return new AjaxResult(700);
        Record saveEntity = new Record();
        if (option == 1 || option == 2){
            StringBuilder message = new StringBuilder();
            saveEntity = dataModel.genBean(bean, option, message);
            if (saveEntity == null) return new AjaxResult(500, message.toString());
        }
        KeepDaoPool keepDaoPool = new KeepDaoPool(dao);
        AjaxResult result = saveBean(keepDaoPool, dataModel, saveEntity, option, condition, rel);
        keepDaoPool.end();
        return result;
    }

    /***
     * 保存单个CURD对象（此方法和CURD操作还未形成独立的体系--＞2016-01-01）
     * @param dao
     * @param curd
     * @return
     */
    public static Receipt saveBean(IDao dao, Curd curd){
        AjaxResult ajaxjson = saveBean(dao, curd.TableName, curd.Bean, curd.Option, curd.Condition, curd.Rel);
        if (ajaxjson.statusCode != 200){
            return new Receipt(ajaxjson.message).setData(ajaxjson);
        }
        return new Receipt();
    }

    /***
     * 保存多个CURD对象
     * @param curds 增删改列表
     * @param supportTran 是否支持事务
     * @return
     */

    public static Receipt saveBeans(List<Curd> curds, boolean supportTran ){
        KeepDaoPool keepDaoPool = new KeepDaoPool(supportTran);
        List<AjaxResult> results = new ArrayList<AjaxResult>();
        for (Curd curd : curds){
            AjaxResult receipt = saveBean(keepDaoPool, curd.TableName, curd.Bean, curd.Option, curd.Condition, curd.Rel);
            results.add(receipt);
            if (!receipt.success) {
                keepDaoPool.AllClose();
                return new Receipt(false, receipt.message).setData(results);
            }
        }
        keepDaoPool.end();
        return new Receipt().setData(results);
    }


    /**
     * 有外部操作操作事务对象
     * 注意：如果操作成功,keepdaopool必须处理
     * @param curds CURD对象
     * @return
     */
    public static ReStruct<Receipt, KeepDaoPool> saveBeansWithOut(List<Curd > curds) {
        KeepDaoPool keepDaoPool = new KeepDaoPool();
        List<AjaxResult> results = new ArrayList<AjaxResult>();
        for (Curd curd : curds)
        {
            AjaxResult receipt = saveBean(keepDaoPool, curd.TableName, curd.Bean, curd.Option, curd.Condition, curd.Rel);
            results.add(receipt);
            if (!receipt.success)
            {
                keepDaoPool.AllClose();
                return new ReStruct<Receipt, KeepDaoPool>(false, new Receipt(false, receipt.message).setData(results),null);
            }
        }
        return new ReStruct<Receipt, KeepDaoPool>(true, new Receipt().setData(results), keepDaoPool);
    }
    /// <summary>
    /// 保存多个beans对象
    /// <para>注意：如果操作成功,keepdaopool必须处理</para>
    /// </summary>
    /// <param name="beans">beans对象文本</param>
    /// <returns></returns>
    public static ReStruct<Receipt, KeepDaoPool> saveBeansWithOut(String beans)
    {
        throw Core.makeThrow("未实现");
//            if (String.IsNullOrEmpty(beans)) return new ReStruct<Receipt, KeepDaoPool>("参数不能为空");
//            var isHaveContext = Regex.IsMatch(beans, "(@@)|(\\$\\{)");
//            KeepDaoPool keepDaoPool = new KeepDaoPool();
//            //以对象的形式访问
//            if (beans.IsJson(1))
//            {
//                Record record = Json.FromJson<Record>(beans);
//                if (record == null || record.Count < 1) return new ReStruct<Receipt, KeepDaoPool>("参数不能为空");
//                //以普通savebean的方式保存
//                if (record.HaveKey("tableName").HasValue() && record.HaveKey("option").HasValue())
//                {
//                    var result = SaveBean(
//                            keepDaoPool,
//                            record.GetString("tableName", true),
//                            Json.ToJson(record.Get("bean")),
//                            short.Parse(record.GetString("option")),
//                            Json.ToJson(record.Get("condition")),
//                            record.GetString("rel")
//                    );
//                    return new ReStruct<Receipt, KeepDaoPool>(true,result.ToReceipt(),keepDaoPool);
//                }
//                //以多键的方式访问
//                else
//                {
//                    Record results = new Record();
//                    try
//                    {//具体执行代码-->数据绑定+
//
//                        foreach (String key in record.Keys)
//                        {
//                            Dictionary<String, Object> dic = (Dictionary<String, Object>)record[key];
//                            if (dic.GetString("tableName", true).HasValue() && dic.GetString("option", true).HasValue())
//                            {
//                                String tableName = dic.GetString("tableName", true);
//                                IDataModel tDaoHelp = DaoSeesion.GetDataModel(tableName);
//                                if (tDaoHelp.IsNull())
//                                {
//                                    keepDaoPool.AllRollBack();
//                                    return new ReStruct<Receipt, KeepDaoPool>(false, new Receipt(false, "[" + tableName + "]动态数据模版未找到").SetData(results));
//                                }
//                                String rel = dic.GetString("rel");
//                                int option = dic.Get<int>("option");
//                                String param = Json.ToJson(dic.Get("param"));
//                                String condition = Json.ToJson(dic.Get("condition"));
//                                String columns = Json.ToJson(dic.Get("columns"));
//                                AjaxResult ajaxJson = null;//当次的处理结果
//                                Record entrybean = null;//当次的对象数据
//                                if (option == 1 || option == 2 || option == 3)
//                                {
//                                    var bean = dic.Get("bean", true);
//                                    if (bean == null)
//                                    {
//                                        keepDaoPool.AllRollBack();
//                                        return new ReStruct<Receipt, KeepDaoPool>(false, new Receipt(false, "新增数据未组装").SetData(results));
//                                    }
//                                    if (bean is string)
//                                    {
//                                        //转化bean的类型
//                                        String beanstr = bean.ToString().Trim();
//                                        if (beanstr.StartsWith("{") && beanstr.EndsWith("}"))
//                                        {
//                                            bean = Json.ToObject<Dictionary<string, object>>(beanstr);
//                                        }
//                                    }
//                                    if (bean is Dictionary<string, object>)
//                                    {
//                                        String message;
//                                        if (isHaveContext)
//                                        {
//                                            var re_t = GenBeanTool.DataMatch(results, (Dictionary<string, object>)dic["bean"], out entrybean);
//                                            if (!re_t.Result) ajaxJson = new AjaxResult(400, re_t.Message);
//                                            entrybean = GenBeanTool.GenBean(tDaoHelp, entrybean, option, out message);
//                                        }
//                                        else entrybean = GenBeanTool.GenBean(tDaoHelp, (Dictionary<string, object>)dic["bean"], option, out message);
//                                        if (message.HasValue())
//                                        {
//                                            keepDaoPool.AllRollBack();
//                                            return new ReStruct<Receipt, KeepDaoPool>(false, new Receipt(false, message).SetData(results));
//                                        }
//                                        if (entrybean == null || entrybean.Count < 1)
//                                        {
//                                            keepDaoPool.AllRollBack();
//                                            return new ReStruct<Receipt, KeepDaoPool>(false, new Receipt(false, "保存对象未有效数据").SetData(results));
//                                        }
//                                    }
//
//                                }
//                                if (option == 0 || option == -1 || option == 2 || option == 3)
//                                {
//                                    if (String.IsNullOrEmpty(condition))
//                                    {
//                                        keepDaoPool.AllRollBack();
//                                        return new ReStruct<Receipt, KeepDaoPool>(false, new Receipt(false, "未有修改条件").SetData(results));
//
//                                    }
//                                }
//                                //动态联想数据
//                                if (option != 1 && isHaveContext)
//                                {
//                                    condition = condition.Mapping(results);
//                                }
//                                switch (option)
//                                {
//                                    case 0:
//                                        ajaxJson = QueryAction.GetBean(tableName, columns, condition, param, rel);
//                                        break;
//                                    case 1://新增数据
//                                        ajaxJson = SaveBean(keepDaoPool, tDaoHelp, entrybean, option, "", rel);
//                                        break;
//                                    case 2:
//                                    case 3://保存动作
//                                        ajaxJson = SaveBean(keepDaoPool, tDaoHelp, entrybean, option, condition, rel);
//                                        break;
//                                    case -1:
//                                        ajaxJson = SaveBean(keepDaoPool, tDaoHelp, new Record(), option, condition, rel);
//                                        break;
//                                    case 11:
//                                        ajaxJson = QueryAction.GetData(tableName, condition, columns, param, rel);
//                                        break;
//                                    case 22:
//                                        int psize = dic.Get("psize") == null ? 0 : dic.GetInt("psize");
//                                        ajaxJson = QueryAction.QueryData(tableName, condition, psize, param);
//                                        break;
//                                    default:
//                                        ajaxJson = new AjaxResult(444);
//                                        break;
//                                }
//                                results.Put(key, ajaxJson.GetData());
//                                if (ajaxJson.statusCode != 200)
//                                {
//                                    keepDaoPool.AllRollBack();
//                                    return new ReStruct<Receipt, KeepDaoPool>(false, ajaxJson.SetData(results).ToReceipt().SetData(results));
//                                }
//
//                            }
//                            else
//                            {
//                                keepDaoPool.AllRollBack();
//                                return new ReStruct<Receipt, KeepDaoPool>(false,new Receipt(false, "未含有操作必要条件[键名(tableName)或动作(option)]").SetData(results));
//                            }
//                        }
//
//                    }
//                    catch (Exception e)
//                    {
//                        keepDaoPool.AllRollBack();
//                        return new ReStruct<Receipt, KeepDaoPool>(true, new Receipt(false, "执行过程发生异常").SetData(e.Message));
//
//                    }
//                    return  ReStruct<Receipt, KeepDaoPool>.SetResult(new Receipt().SetData(results),keepDaoPool);
//                }
//            }
//            //以数组的形式多bean的形式，无回调和数据绑定
//            if (beans.IsJson(2))
//            {
//                List<Record> records = Json.FromJson<List<Record>>(beans);
//                if (records == null || records.Count < 1) return new ReStruct<Receipt, KeepDaoPool>(false, new Receipt(false, "参数未组装")); ;
//                if (records.Count == 1)
//                {//以单对象形式，不提供事务
//                    var record = records[0];
//                    var result = SaveBean(
//                            keepDaoPool,
//                            record.GetString("tableName", true),
//                            Json.ToJson(record.Get("bean", true)),
//                            record.GetInt("option"),
//                            Json.ToJson(record.Get("condition", true)),
//                            record.GetString("rel")
//                    );
//                    if (result.statusCode != 200)
//                    {
//                        keepDaoPool.AllRollBack();
//                        return new ReStruct<Receipt, KeepDaoPool>(false, result.ToReceipt());
//                    }
//                    return ReStruct<Receipt, KeepDaoPool>.SetResult(result.ToReceipt(), keepDaoPool);
//                }
//                AjaxResult re_t = new AjaxResult();
//                try
//                {
//                    foreach (Record record in records)
//                    {
//                        String tableName = record.GetString("tableName", true);
//                        String option = record.GetString("option", true);
//                        if (tableName.IsBlank() || option.IsBlank() || !Regex.IsMatch(option, "\\d*"))//如果表名为空
//                        {
//                            re_t.PushData(new AjaxResult(444, "未含有操作必要条件[键名(tableName)或动作(option)]"));
//                            keepDaoPool.AllRollBack();
//                            re_t.SetStatusCode(500).SetMessage("执行过程出现错误");
//                            return new ReStruct<Receipt, KeepDaoPool>(false, re_t.ToReceipt());
//                        }
//                        var dm = DaoSeesion.getDataModel(tableName, record.GetString("param", true));
//                        var op = Int32.Parse(option);
//                        var bean = GenBeanTool.GenBean(dm, record.GetString("bean"), op);
//                        AjaxResult ajaxJson = SaveBean(keepDaoPool, dm, bean, op, record.GetString("condition", true), record.GetString("rel"));
//                        re_t.PushData(ajaxJson);
//                        if (ajaxJson.statusCode != 200)
//                        {
//                            keepDaoPool.AllRollBack();
//                            re_t.SetStatusCode(500).SetMessage("过程中出现错误");
//                            return new ReStruct<Receipt, KeepDaoPool>(false, re_t.ToReceipt());
//                        }
//                    }
//                }
//                catch (Exception e)
//                {
//                    keepDaoPool.AllRollBack();
//                    re_t.SetStatusCode(500).SetMessage("执行过程出现异常").PushData(e.Message);
//                    return new ReStruct<Receipt, KeepDaoPool>(false, re_t.ToReceipt());
//                }
//                return ReStruct<Receipt, KeepDaoPool>.SetResult(re_t.ToReceipt(),keepDaoPool);
//            }
//            return new ReStruct<Receipt, KeepDaoPool>(false);
    }


}

