package com.jladder.actions.impl;

import com.alibaba.fastjson.JSONObject;
import com.jladder.actions.Curd;
import com.jladder.data.Record;
import com.jladder.data.*;
import com.jladder.datamodel.DataModelType;
import com.jladder.datamodel.IDataModel;
import com.jladder.db.*;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.lang.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/// <summary>
/// 根据模版的基本数据处理类
/// </summary>
public class QueryAction
{
    /// <summary>
    /// 最近执行语句
    /// </summary>
//    public static List<KeyValue<String, List<DbParameter>>> Recent => BaseSupport.Recent;

    /// <summary>
    /// 获取数据通过树级层次
    /// </summary>
    /// <param name="tableName"></param>
    /// <param name="columns"></param>
    /// <param name="condition"></param>
    /// <param name="deptpath"></param>
    /// <param name="fields"></param>
    /// <returns></returns>
    public static AjaxResult getDataByTree(String tableName, String columns, String condition, String deptpath, String fields){
        throw Core.makeThrow("未实现");
//        Record config = new Record();
//        if (Strings.isBlank(fields))
//        {
//            config.put("department", "org_id");
//            config.put("company", "company_id");
//        }
//        else
//        {
//            if (fields.isJson())
//            {
//                config = Record.Parse(fields) ?? new Record();
//                if (config.GetString("department", true).IsBlank())
//                {
//                    config.Put("department", "org_id");
//                }
//                if (config.GetString("company", true).IsBlank())
//                {
//                    config.Put("company", "company_id");
//                }
//            }
//            else
//            {
//                var fs = fields.Split(',');
//                if (fs.Length > 0) config.Put("company", fs[0]);
//                if (fs.Length > 1) config.Put("department", fs[1]);
//            }
//
//        }
//
//        var dm = DaoSeesion.GetDataModel(tableName);
//        var dao = new Dao(dm.Conn);
//        dm.SetCondition(condition);
//        dm.MatchColumns(columns);
//        dao.Tag = dm.Raw.Name;
//        Func<IDataModel, Record, string> getsql = (x, cnd) =>
//        {
//            x.SetCondition(condition);
//            x.SetCondition(cnd);
//            x.MatchColumns(columns);
//            return x.SqlText();
//        };
//
//
//        var context = deptpath.Replace('-', ',');
//        var ipath = context.Split(',').ToList();
//        var copy = new List<string>();
//        ipath.ForEach(n =>
//                {
//        if (!n.IsBlank() && n != "0" && !n.Contains("undefined"))
//            copy.Add(n);
//        //ipath.Remove(n);
//            });
//        deptpath = string.Join(",", copy);
//
//
//
//        var departments = deptpath.Split(',');
//        if (departments.IsBlank())
//        {
//            dao.Close();
//            return new AjaxResult(400, "部门路径不能为空");
//        }
//        var sqls = new List<string>();
//        //只有公司
//        if (departments.Length < 2 && config.GetString("company") != "0")
//        {
//            sqls.Add(getsql(DaoSeesion.GetDataModel(tableName),
//                    new Record(config.GetString("company"), departments[0]).Put(config.GetString("department", true), "0")));
//        }
//        else
//        {
//            for (int i = departments.Length - 1; i > 0; i--)
//            {
//                sqls.Add(getsql(DaoSeesion.GetDataModel(tableName), new Record(config.GetString("department", true), departments[i])));
//            }
//            if (config.GetString("company") != "0")
//            {
//                sqls.Add(getsql(DaoSeesion.GetDataModel(tableName),
//                        new Record(config.GetString("company"), departments[0]).Put(config.GetString("department", true), "0")));
//            }
//        }
//        var sqltext = sqls.ToString(" UNION ");
//        var data = dao.Query(sqltext);
//        dao.Close();
//
//        if (data == null)
//        {
//            var list = new List<Record>();
//            if (tableName == "*crm_config_customeroutticket")
//            {
//                list.Add(new Record("businessdepart_name", "YDHKZB"));
//                data = list;
//            }
//            else if (tableName == "*crm_config_customerdistribution")
//            {
//                list.Add(new Record("distribution_name", "YDHKZB"));
//                data = list;
//            }
//            else if (tableName == "*crm_config_customersettlement")
//            {
//                list.Add(new Record("businessdepart_name", "YDHKZB"));
//                data = list;
//            }
//        }
//        return data == null ? new AjaxResult(404).SetData(dao.ErrorString) : new AjaxResult(data);
    }


    /// <summary>
    /// 获取数据(对内服务)
    /// </summary>
    /// <param name="tableName">表名</param>
    /// <param name="condition">条件文本</param>
    /// <param name="columns">字段文本</param>
    /// <param name="param">参数文本</param>
    /// <returns></returns>
    public static List<Record> getDataByRecord(String tableName, Record condition, String columns, String param){
        if (Strings.isBlank(tableName)) return null;
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm.isNull()) return null;
        if (!dm.enable()) return null;
        dm.matchColumns(columns);
        dm.setCondition(condition);
        return getData(dm,null,false);
    }

    /// <summary>
    /// 获取数据(对内服务)
    /// </summary>
    /// <param name="tableName">表名</param>
    /// <param name="condition">条件文本</param>
    /// <param name="columns">字段文本</param>
    /// <param name="param">参数文本</param>
    /// <returns></returns>
    public static <T> List<Record> getDataByEntry(String tableName, T condition, String columns, String param) {
        if (Strings.isBlank(tableName)) return null;
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm.isNull()) return null;
        if (!dm.enable()) return null;
        dm.matchColumns(columns);
        dm.setCondition(Record.parse(condition));
        return getData(dm,null,false);
    }


    /// <summary>
    /// 获取实体列表
    /// </summary>
    /// <param name="tableName">模版名称</param>
    /// <param name="condition">条件</param>
    /// <param name="clazz">类泛型</param>
    /// <param name="<T>"></param>
    /// <returns></returns>
    public static <T> List<T> getData(CharSequence tableName, Cnd condition, Class<T> clazz) {
        return getData(tableName,condition,null,null,clazz);
    }

    /**
     * 获取实体列表
     * @param tableName 模型名称
     * @param condition 过滤条件
     * @param columns 列选
     * @param param 扩展参数
     * @param clazz 类型
     * @param <T> 泛型
     * @return
     */
    public static <T> List<T> getData(CharSequence tableName, Cnd condition, String columns, String param,Class<T> clazz){
        if (Strings.isBlank(tableName)) return null;
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (!dm.enable()) return null;
        dm.matchColumns(columns);
        //作用域的数据控制
//        WebScope.SetDataContorl(dm);
        if (dm.isNull()) return null;
        dm.setCondition(condition);
        Receipt ret = LatchAction.getData(dm);
        if (ret.isSuccess()){
            List<Record> rs = (List<Record>) ret.getData();
            return Rs.toClass(rs,clazz);
        }
        else{
            Dao dao = new Dao(Strings.isBlank(dm.getConn())?"defaultDatabase":dm.getConn());
            dm.setDialect(dao.getDialect());
            dao.Tag = dm.getRaw().Name;
            SqlText sqltext = dm.getSqlText();
            List<T> result = dao.query(sqltext, clazz);
            dao.close();
            return result;
        }
    }

    /// <summary>
    ///
    /// </summary>
    /// <param name="dao">Dao工具</param>
    /// <param name="dm">Dao助手</param>
    /// <param name="condition">条件文本</param>
    /// <param name="param">参数文本</param>
    /// <param name="columns">字段文本</param>
    /// <returns></returns>

    /**
     * 获取数据(对外处理)
     * @param dao 数据库操作对象
     * @param dm 数据模型
     * @param condition 条件文本
     * @param param 扩展参数
     * @param columns 列选
     * @return
     */
    public static AjaxResult getData(IDao dao, IDataModel dm, String condition, String param, String columns){
        if (dm.isNull()) return null;
        dm.matchColumns(columns);
        if (dm.isNull()) return new AjaxResult(702);
        dm.setCondition(condition);
        List<Record> recordes = getData(dm, dao,false);
        return new AjaxResult().setData(recordes);
    }
    /**
     * 获取数据(对外访问)
     * @param tableName 模型名称
     * @return
     */
    public static AjaxResult getData(String tableName){
        return getData(tableName,"","","","");
    }


    /**
     * 获取数据(对外访问)
     * @param tableName 模型名称
     * @param condition 条件文本
     * @param columns 列选
     * @param param 扩展参数
     * @param rel 资源索引
     * @return
     */
    public static AjaxResult getData(CharSequence tableName, String condition, String columns, String param, String rel)
    {
        if (Strings.isBlank(tableName)) return new AjaxResult(444).setRel(rel);
        long startTime = Times.getTS();
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if(dm==null)return new AjaxResult(700);
        if(dm.isNull()) return new AjaxResult(701).setRel(rel).setDuration(startTime);
        dm.matchColumns(columns);
        if (dm.isNull()) return new AjaxResult(702).setRel(rel).setDuration(startTime);
        dm.setCondition(Strings.mapping(condition));
        String error="";
        List<Record> rs = getData(dm,null,false,error);

        if(rs == null) return new AjaxResult(500).setRel(rel).setDuration(startTime).setData(error).setMessage("查询执行错误");
        return rs.size() < 1 ? new AjaxResult(404).setRel(rel).setDuration(startTime) : new AjaxResult(rs).setRel(rel).setDuration(startTime);
    }

    /***
     * 获取数据(对内服务)
     * @param tableName 模版名称
     * @param condition 条件对象
     * @return
     */
    public static List<Record> getData(CharSequence tableName, Cnd condition){
        return getData(tableName,condition,null,null);
    }
    public static List<Record> getData(CharSequence tableName, Cnd condition, String columns){
        return getData(tableName,condition,columns,null);
    }
    /***
     * 获取数据(对内服务)
     * @param tableName 模版名称
     * @param condition 条件对象
     * @param columns 列节选
     * @param param 扩展参数
     * @return
     */

    public static List<Record> getData(CharSequence tableName, Cnd condition, String columns, Object param) {
        if (Strings.isBlank(tableName)) return null;
        IDataModel dm = DaoSeesion.getDataModel(tableName, (param instanceof String?(String)param : Json.toJson(Record.parse(param))));
        if (dm.isNull()) return null;
        dm.matchColumns(columns);
        dm.setCondition(condition);
        return getData(dm,null,false);
    }

    /**
     * 获取数据(对内服务)
     * @param tableName 模型名称
     * @param condition 条件文本
     * @param columns 列选
     * @param param 扩展参数
     * @return
     */
    public static List<Record> getData(CharSequence tableName, String condition, String columns, String param){
        if (Strings.isBlank(tableName)) return null;
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm.isNull()) return null;
        if (!dm.enable()) return null;
        dm.matchColumns(columns);
        dm.setCondition(Strings.mapping(condition));
        return getData(dm);
    }

    /**
     * 获取数据列表
     * @param keepDaoPool 数据库操作池
     * @param dm 数据模型
     * @return
     */
    public static List<Record> getData(KeepDaoPool keepDaoPool, IDataModel dm){
        if (keepDaoPool == null || dm == null) return null;
        KeepDao keepdao = keepDaoPool.createKeepDao(dm.getConn());
//        WebScope.SetDataContorl(dm);
        if (dm.isNull()) return null;
        AnalyzeAction analyze = new AnalyzeAction(dm, DbSqlDataType.GetData);
        Receipt latchresult = LatchAction.getData(dm);
        List<Record> subQueryAction = dm.getRelationAction("query");
        if (subQueryAction == null && latchresult.isSuccess()) {
            return analyze.countCacheDataEnd((List<Record>)latchresult.getData());
        }
        SqlText sqltext = dm.getSqlText();
        if (subQueryAction == null){
            List<Record> recordes = handleQueryResult(keepDaoPool, keepdao.Dao.query(sqltext), dm);
            LatchAction.setData(dm, recordes);
            return analyze.countDataEnd(recordes);
        }
        else {
            if (latchresult.isSuccess()) {
                List<Record> rs = (List<Record>) latchresult.getData();
                for (Record record : rs){
                    if (record != null) subActionQuery(keepDaoPool, record, subQueryAction, dm);
                }
                rs = handleQueryResult(keepDaoPool, keepdao.Dao.query(sqltext), dm);
                return analyze.countCacheDataEnd(rs);
            }
            else {
                List<Record> recordes = keepdao.Dao.query(sqltext, record ->{
                    if (record != null) subActionQuery(keepDaoPool, record, subQueryAction, dm);
                    return true;
                });
                handleQueryResult(keepDaoPool, recordes, dm);
                LatchAction.setData(dm, recordes);
                return analyze.countDataEnd(recordes);
            }
        }
    }
    public static List<Record> getData(IDataModel dm){
        return getData(dm,null,false);
    }
    public static List<Record> getData(IDataModel dm, IDao dao, boolean supportTran){
        String errorMessage="";
        return getData(dm, dao, supportTran,errorMessage);
    }
    /**
     * 获取数据(对内服务)
     * @param dm 数据模型
     * @param dao 数据库操作对象
     * @param supportTran 是否支撑事务
     * @param errorMessage 错误消息
     * @return
     */
    public static List<Record> getData(IDataModel dm, IDao dao,boolean supportTran,String errorMessage){
        errorMessage = "";
        if (dm == null|| !dm.enable())return null;
//        WebScope.SetDataContorl(dm);
        if (dm.isNull()) return null;
        AnalyzeAction analyze = new AnalyzeAction(dm, DbSqlDataType.GetData);
        Receipt latchresult = LatchAction.getData(dm);
        //静态数据逻辑
        if (DataModelType.Data.equals(dm.Type)){
            if(latchresult == null || !latchresult.isSuccess())return analyze.countDataEnd((List<Record>)null);
            List<Record> subQueryAction1 = dm.getRelationAction("query");
            if (subQueryAction1 == null && latchresult.isSuccess())
            {
                return analyze.countCacheDataEnd((List<Record>)latchresult.getData());
            }
        }
        //含有dao对象视为无后续动作查询
        if (dao != null){
            if (latchresult.isSuccess()){
                return analyze.countCacheDataEnd((List<Record>)latchresult.getData());
            }
            else{
                dm.setDialect(dao.getDialect());
                dao.setTag(dm.getName());
                List<Record> rs = dao.query(dm.getSqlText());
                if (rs == null) return null;
                LatchAction.setData(dm,rs);
                return analyze.countCacheDataEnd(rs);
            }
        }
        List<Record> subQueryAction = dm.getRelationAction("query");
        if (subQueryAction == null && latchresult.isSuccess()){
            return analyze.countCacheDataEnd((List<Record>)latchresult.getData());
        }
        KeepDaoPool keepDaoPool = new KeepDaoPool(supportTran);
        try{
            KeepDao keepDao = keepDaoPool.createKeepDao(dm.getConn());
            dm.setDialect(keepDao.Dao.getDialect());
            SqlText sql = dm.getSqlText();
            keepDao.Dao.setTag(dm.getName());
            if (subQueryAction == null){
                List<Record> data = keepDao.Dao.query(sql);
                errorMessage = keepDao.Dao.getErrorMessage();
                List<Record> rs = handleQueryResult(keepDaoPool, data, dm);
                keepDaoPool.end();
                LatchAction.setData(dm, rs);
                return analyze.countDataEnd(rs);
            }
            else{
                if (latchresult.isSuccess()) {
                    List<Record> rs = (List<Record>) latchresult.getData();
                    for (Record record : rs)
                    {
                        if (record != null) subActionQuery(keepDaoPool, record, subQueryAction, dm);
                    }
                    rs=handleQueryResult(keepDaoPool, keepDao.Dao.query(sql), dm);
                    keepDaoPool.end();
                    return analyze.countCacheDataEnd(rs);
                }
                else{
                    List<Record> recordes = keepDao.Dao.query(sql, record ->{
                        if (record != null) subActionQuery(keepDaoPool, record, subQueryAction, dm);
                        return true;
                    });
                    errorMessage = keepDao.Dao.getErrorMessage();
                    handleQueryResult(keepDaoPool, recordes, dm);
                    keepDaoPool.end();
                    LatchAction.setData(dm, recordes);
                    return analyze.countDataEnd(recordes);
                }
            }
        }finally {
            keepDaoPool.end();
        }
    }
    /**
     * 获取分页数据
     * @param tableName 模型名称
     * @param condition 条件文本
     * @param columns 节选列
     * @param param 扩展参数
     * @param start 起始记录号
     * @param pageNumber 页号
     * @param pageSize 页大小
     * @param recordCount 记录数量参考｛0,获取记录数;负数:不获取;正数:置到分页中｝
     * @return
     */
    public static AjaxResult getPageData(CharSequence tableName, String condition, String columns, String param, int start, int pageNumber, int pageSize, Object recordCount){
        long startTime = Times.getTS();
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm == null || dm.isNull()) return new AjaxResult(700).setDataType(AjaxResultDataType.Error).setDataName(tableName);
        dm.matchColumns(columns);
        if (dm.isNull()) return new AjaxResult(702).setDuration(startTime).setDataType(AjaxResultDataType.Error);
        dm.setCondition(Strings.mapping(condition));
        PageResult result = getPageData(dm, start, pageNumber, pageSize, recordCount);
        return new AjaxResult().setData(result).setDuration(startTime).setDataType(AjaxResultDataType.PageResult).setDataName(tableName);
    }
    /**
     * 获取分页数据
     * @param tableName 模型名称
     * @param start 起始位置
     * @param pageNumber 起始页号
     * @param pageSize 页大小
     * @param condition 条件文本
     * @param columns 节选列
     * @param param 扩展参数
     * @param recordCount 记录数量参考｛0,获取记录数;负数:不获取;正数:置到分页中｝
     * @return
     */
    public static PageResult getPageData(CharSequence tableName,int start, int pageNumber, int pageSize, Object condition, String columns, String param,Object recordCount){
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm == null || dm.isNull()) return new PageResult(700);
        dm.matchColumns(columns);
        if (dm.isNull()) return new PageResult(702);
        if (condition != null){
            Cnd cnd = Cnd.parse(condition, dm);
            dm.setCondition(cnd);
        }
        return getPageData(dm, start, pageNumber, pageSize, recordCount);
    }
    /**
     * 获取分页数据
     * @param dm 数据模型
     * @param start 起始位置
     * @param pageNumber 页号
     * @param pageSize 分页量
     * @param rc 记录总数配置
     * @return
     */
    public static PageResult getPageData(IDataModel dm, int start, int pageNumber, int pageSize, Object rc){
        if (dm == null) return new PageResult(701);
//        WebScope.SetDataContorl(dataModel);
        if (dm.isNull()) return new PageResult(701);
        if (pageSize == 0) pageSize = 20;
        if (start == pageNumber && start > 0) return new PageResult(400);
        String recordCount = (rc==null ? "":rc.toString());
        //分页对象
        Pager pager = new Pager(pageSize);
        //如果起始记录数和页号全部为空，以默认
        //if(start== pageNumber && start==0) pager.SetPageSize(psize);
        if (start != pageNumber && start > 0) pager.setOffset(start);
        if (start != pageNumber && pageNumber > 0) pager.setPageNumber(pageNumber);
        //取缓存分页数据
        BasicPageResult ret = LatchAction.getPageData(dm, pager);
        if (ret != null && !Core.isEmpty(ret.records)){
            List<Record> subQueryAction = dm.getRelationAction("query");
            List<Record> records = ret.records;
            if (subQueryAction != null){
                IDao dao = DaoSeesion.NewDao(dm.getConn());
                dao.setTag(dm.getRaw().Name);
                KeepDaoPool keepDaoPool = new KeepDaoPool(dao);
                try {
                    ret.records.forEach(record ->{
                        if (record != null) subActionQuery(keepDaoPool, record, subQueryAction, dm);
                    });
                }finally {
                    keepDaoPool.end();
                }
            }
            PageResult pr = new PageResult();
            pr.records = records;
            pr.pager = pager;
            return pr;
        }
        else{
            IDao dao = DaoSeesion.NewDao(dm.getConn());
            dao.setTag(dm.getRaw().Name);
            Long total=0L;//总记录数
            if (Strings.isBlank(recordCount) || "0".equals(recordCount)){
                SqlText page_SqlText = dm.getWhere();
                String cSqlText = "select count(*) from " +dm.TableName +page_SqlText.getCmd() + dm.getGroup();
                //cSqlText = "select count(0) from (select 1 from " + dm.TableName + page_SqlText.getCmd() + dm.getGroup() + ") yifeng";
                total = dao.getValue(new SqlText(cSqlText,page_SqlText.getParameters()),Long.class);
                pager.setRecordCount(total);
            }else{
                if(Strings.isNumber(recordCount)){
                    pager.setRecordCount(Convert.toInt(recordCount));
                }
                else{
                    if(Strings.isJson(recordCount,1)){
                        Record _config = Record.parse(recordCount);
                        total = QueryAction.getCount(_config.getString("tableName",true),_config.get("condition"),_config.getString("param"));
                    }else{
                        total = QueryAction.getCount(recordCount,null);
                    }
                }
                pager.setRecordCount(total);
            }
            SqlText pageQuerySqlText = dm.pagingSqlText(dao.getDialect(), pager);
            List<Record> subQueryAction = dm.getRelationAction("query");
            List<Record> records = null;
            PageResult pr = new PageResult();
            if (subQueryAction == null) {
                records = handleQueryResult(dao.query(pageQuerySqlText), dm);
            }
            else{
                KeepDaoPool keepDaoPool = new KeepDaoPool(dao);
                try{
                    records = dao.query(pageQuerySqlText, record->{
                        if (record != null) subActionQuery(keepDaoPool, record, subQueryAction, dm);
                        return true;
                    });
                    handleQueryResult(keepDaoPool,records, dm);
                }finally {
                    keepDaoPool.end();
                }
            }
            pr.message = dao.getErrorMessage();
            pr.records = records;
            pr.pager = pager;
            dao.close();
            LatchAction.setPageData(dm,pr);
            return pr;
        }
    }
    /**
     * 集成分页查询(处理函数)
     * @param dm 数据模型
     * @param request 请求参数
     * @param pageSize 页大小
     * @return
     */
    public static PageResult queryData(IDataModel dm, Object request, int pageSize){
        if (dm == null || dm.isNull()) return new PageResult(700);
        AnalyzeAction analyze = new AnalyzeAction(dm, DbSqlDataType.QueryData);
        if(!dm.enable())return new PageResult(600);
//        WebScope.SetDataContorl(dm);
        if(dm.isNull()) return new PageResult(702);
        if (pageSize <= 0) pageSize = 20;
        IDao dao = DaoSeesion.NewDao(dm.getConn());
        dm.setDialect(dao.getDialect());
        dao.setTag(dm.getRaw().Name);
        Record argMap = null;
        Pager pager = new Pager(1, pageSize);
        //缓存的分页数据
        BasicPageResult bpr=null;
        if (request != null) {
            if (request instanceof String && !Regex.isMatch(request.toString().trim(), "^\\{|\\[")){
                request = new Record("forms", request.toString().trim());
            }
            argMap = Record.parse(request);
            //如果自定义字段没有配置从系统配置文件里选择
            String columnstr = argMap.getString("columnString,column,columns", true);
            if (Strings.hasValue(columnstr))dm.matchColumns(columnstr, argMap.getString("columnType|type"));
            if(dm.isNull()) return new PageResult(702);
            //处理表单传递的对象
            Object form = argMap.getObject("forms,condition,form");
            if (form != null) dm.setCondition(Cnd.parse(form, dm));
            long total=-1L;//总数据量
            //处理分页对象
            if (argMap.get("pager") != null){
                Record rp = Record.parse(argMap.get("pager"));//原始pager数据
                if(!rp.containsKey("pageSize"))rp.put("pageSize",pageSize);
                String recordCount = rp.getString("recordCount");
                if(!Strings.isNumber(recordCount)){
                    rp.put("recordCount",0);
                    pager = rp.toClass(Pager.class);
                    if (Strings.isBlank(recordCount)){
                        SqlText page_SqlText = dm.getWhere();
                        String cSqlText = "select count(*) from " +dm.TableName +page_SqlText.getCmd() + dm.getGroup();
                        //cSqlText = "select count(0) from (select 1 from " + dm.TableName + page_SqlText.getCmd() + dm.getGroup() + ") yifeng";
                        total = dao.getValue(new SqlText(cSqlText,page_SqlText.getParameters()),Long.class);
                        pager.setRecordCount(total);
                    }else{
                        if(Strings.isNumber(recordCount)){
                            pager.setRecordCount(Convert.toInt(recordCount));
                        }
                        else{
                            if(Strings.isJson(recordCount,1)){
                                Record _config = Record.parse(recordCount);
                                total = QueryAction.getCount(_config.getString("tableName",true),_config.get("condition"),_config.getString("param"));
                            }else{
                                total = QueryAction.getCount(recordCount,null);
                            }
                        }
                        pager.setRecordCount(total);
                    }
                }
                else{
                    pager = rp.toClass(Pager.class);
                    total = pager.getRecordCount();
                }
                pager.reset();
                argMap.put("pager", pager);
                if (pager.getPageSize() != pageSize){
                    pager.pageSize = pageSize;
                    bpr = LatchAction.getPageData(dm, pager);
                    if (total < 0 && (bpr==null || bpr.statusCode != 200)){
                        analyze.isCache = true;
                        SqlText pagesqltext=dm.getWhere();
                        String cSqlText = "select count(*) from " + dm.TableName + dm.getWhere() + dm.getGroup();
                        total = dao.getValue(SqlText.create(cSqlText,pagesqltext.getParameters()),Long.class);
                        pager.setRecordCount(total);
                        argMap.put("pager", pager);
                    }
                    else argMap.put("pager", bpr.pager);
                }
            }else{
                bpr = LatchAction.getPageData(dm, pager);
                if (total < 0 && (bpr==null || bpr.statusCode != 200)){
                    analyze.isCache = true;
                    SqlText where = dm.getWhere();
                    String cSqlText = "select count(*) from " + dm.TableName + dm.getWhere() + dm.getGroup();
                    total = dao.getValue(SqlText.create(cSqlText,where.getParameters()),Long.class);
                    if(Strings.hasValue(dao.getErrorMessage())){
                        PageResult pr = new PageResult();
                        pr.statusCode = 500;
                        pr.message = "异常错误";
                        return pr;
                    }
                    pager.setRecordCount(total);
                    argMap.put("pager", pager);
                }
                else argMap.put("pager", bpr.pager);
            }
        }
        else{//默认进入空值进入
            bpr = LatchAction.getPageData(dm, pager);
            if (bpr==null || bpr.statusCode != 200){
                analyze.isCache = true;
                argMap = new Record();
                String sqltext = "select count(*) from " + dm.TableName + dm.getWhere() + dm.getGroup();
                int total = dao.getValue(new SqlText(sqltext),Integer.class);
                pager.setRecordCount(total);
                argMap.put("pager", pager);
            }
            else argMap.put("pager", bpr.pager);
        }
        PageResult pr = new PageResult();
        //var querySqlText = daoHelper.SqlText();
        SqlText pagesqltext = dm.pagingSqlText(dao.getDialect(), pager);
        //获取子查询动作
        List<Record> sublist = dm.getRelationAction("query");
        List<Record> records;
        if (sublist == null){
            if (bpr != null && bpr.statusCode == 200){
                records = bpr.records;
            }else{
                List<Record> rs = dao.query(pagesqltext);
                pr.message = dao.getErrorMessage();
                records = handleQueryResult(rs, dm);
                LatchAction.setPageData(dm,new BasicPageResult(records,pager));
            }
        }else{
            KeepDaoPool keeps = new KeepDaoPool(dao);
            try{
                records = dao.query(pagesqltext, record->{
                    if (record != null) subActionQuery(keeps, record, sublist, dm);
                    return true;
                });
                pr.message = dao.getErrorMessage();
                records = handleQueryResult(keeps, records, dm);
            }finally {
                keeps.end();
            }
        }
        pr.records = records;
        pr.condition = argMap;
        pr.fullcolumns = dm.getAllQueryColumns();
        pr.columns = dm.getColumnList();
        pr.queryform = dm.getQueryForm();
        dao.close();
        analyze.countDataEnd(records);
        return pr;
    }

    /**
     * 集成分页查询
     * @param tableName 键表名
     * @param condition 条件(多层包裹)
     * @param psize 页容量
     * @param param 扩展参数
     * @param columns 节选列
     * @return
     */
    public static AjaxResult queryData(CharSequence tableName, String condition, int psize, String param,String columns){
        long stopWatch = Times.getTS();
        if (psize == 0) psize = 20;
        if (Strings.isBlank(tableName)) { return new AjaxResult(444); }
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm.isNull()) return new AjaxResult(701).setDuration(stopWatch);
        //是否含有列过滤
        if (Strings.hasValue(columns)){
            dm.matchColumns(columns);
            if(dm.isNull())return new AjaxResult(702).setDuration(stopWatch);
        }

        PageResult pt = queryData(dm, Strings.mapping(condition), psize);
        return pt.ToAjaxResult().setDuration(stopWatch);
    }

    /***
     * 获取一条记录
     * @param tableName 模版名称
     * @param condition 条件
     * @return
     */
    public static Record getRecord(CharSequence tableName, Object condition){
        return getRecord(tableName,condition,null,null);
    }
    public static Record getRecord(String tableName, Object condition, String columns){
        return getRecord(tableName,condition,columns,null);
    }
    /***
     * 获取一条记录
     * @param tableName 模版名称
     * @param condition 条件
     * @param columns 列节选
     * @param param 扩展参数
     * @return
     */
    public static Record getRecord(CharSequence tableName, Object condition, String columns, String param){
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm.isNull() || !dm.enable()) return null;
        dm.matchColumns(columns);
//        WebScope.SetDataContorl(dm);//作用域的数据控制
        if (dm.isNull()) return null;
        Cnd cnd = Cnd.parse(condition, dm);
        dm.setCondition(cnd);
        AjaxResult result = getBean(dm);
        if (result.statusCode == 200){
            return ((Record) result.data);
        }
        return null;
    }

    /***
     * 获取一个实体对象
     * @param tableName 数据模型
     * @param condition 查询条件
     * @param clazz 泛型类型
     * @param <T> 泛型类型
     * @return
     */
    public static <T> T getObject(String tableName, Object condition,Class<T> clazz){
        return getObject(tableName,condition,null,null,clazz);
    }

    /***
     * 获取一个实体对象
     * @param tableName 数据模型
     * @param condition 查询条件
     * @param columns 列节选
     * @param param 扩展参数
     * @param clazz 泛型类型
     * @param <T> 泛型类型
     * @return
     */
    public static <T> T getObject(CharSequence tableName, Object condition, String columns , String param,Class<T> clazz) {
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm.isNull() || !dm.enable()) return null;
        dm.matchColumns(columns);
        if (dm.isNull()) return null;
        Cnd cnd = Cnd.parse(condition, dm);
        dm.setCondition(cnd);
        AjaxResult result = getBean(dm);
        return result.statusCode == 200 ? ((Record) result.data).toClass(clazz) : null;
    }

    /***
     * 获取单条记录(对外)
     * @param tableName 表键名
     * @param cnd 条件对象
     * @return
     */
    public static AjaxResult getBean(CharSequence tableName, Object cnd) {
        return getBean(tableName,cnd,null,null);
    }

    /***
     * 获取单条记录(对外)
     * @param tableName 表键名
     * @param cnd 条件对象
     * @param param 参数文本
     * @param columns 字段节选
     * @return
     */
    public static AjaxResult getBean(CharSequence tableName, Object cnd,String columns, Object param){
        long starttime = Times.getTS();
        if (Strings.isBlank(tableName)) return new AjaxResult(444);
        if (cnd == null) return new AjaxResult(444).setDuration(starttime);
        IDataModel dm = DaoSeesion.getDataModel(tableName, (param instanceof String ? (String) param : Json.toJson(Record.parse(param))));
        if (dm.isNull()) return new AjaxResult(701).setDuration(starttime);
        if (Strings.hasValue(columns)) dm.matchColumns(columns);
        if(dm.isNull())return new AjaxResult(702).setDuration(starttime);
        dm.setCondition(Cnd.parse(cnd,dm));
        return getBean(dm).setDuration(starttime);
    }

    /**
     * 获取单条记录(对外)
     * @param tableName 表键名
     * @param condition 查询条件
     * @param columns 列选
     * @param param 扩展参数
     * @param rel 回执索引
     * @return
     */
    public static AjaxResult getBean(CharSequence tableName, Object condition, String columns, String param, String rel){
        if (Strings.isBlank(tableName)) return new AjaxResult(444).setRel(rel);
        if (Core.isEmpty(condition)) return new AjaxResult(444).setRel(rel);
        Long starttime = Times.getTS();
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm.isNull()) return new AjaxResult().setStatusCode(701).setRel(rel).setMessage("数据模型未实例").setDuration(starttime);
        if (Strings.isBlank(columns)) dm.matchColumns(columns);
        if (dm.isNull()) return new AjaxResult().setStatusCode(702).setRel(rel).setDuration(starttime);
        dm.setCondition(Cnd.parse(condition, dm));
        return getBean(dm).setRel(rel).setDuration(starttime);
    }
    /// <summary>
    /// 获取单对象
    /// </summary>
    /// <param name="keepDaoPool">持续数据库连接池</param>
    /// <param name="dm">动态数据模型</param>
    /// <returns></returns>

    public static Record getBean(KeepDaoPool keepDaoPool, IDataModel dm)
    {
        if (dm.isNull()) return null;
        AnalyzeAction analyze = new AnalyzeAction(dm, DbSqlDataType.GetBean);
        if (keepDaoPool == null) return null;
        KeepDao keepDao = keepDaoPool.createKeepDao(dm.getConn());
        //作用域的数据控制
//        WebScope.SetDataContorl(dm);
        SqlText sqlText = dm.getSqlText();
        keepDao.Dao.setTag(dm.getRaw().Name);
        Record record = keepDao.Dao.fetch(sqlText);
        if (record == null) return null;
        List<Record> subQueryAction = dm.getRelationAction("query");
        if (subQueryAction != null) subActionQuery(keepDaoPool, record, subQueryAction, dm);
        subQueryAction = dm.getRelationAction("entity");
        if (subQueryAction != null) subActionQuery(keepDaoPool, record, subQueryAction, dm);
        List<Record> ret = handleQueryResult(keepDaoPool, Rs.create(record), dm);
        keepDaoPool.end();
        analyze.countDataEnd(record);
        return ret.get(0);
    }

    /**
     * 获取单条记录
     * @param dm 数据模型
     * @return
     */
    public static AjaxResult getBean(IDataModel dm){
        return getBean(dm,null,false);
    }

    /**
     * 获取单条记录
     * @param dm 数据模型
     * @param dao 数据库连接操作对象
     * @param supportTran 是否支持事务
     * @return
     */
    public static AjaxResult getBean(IDataModel dm,IDao dao,boolean supportTran){
        if (!dm.enable()) return new AjaxResult(703).setDataType(AjaxResultDataType.Error);
        long starttime = Times.getTS();
        //作用域的数据控制
//        WebScope.SetDataContorl(dm);
        if (dm.isNull()) return new AjaxResult(701).setDataType(AjaxResultDataType.Error);
        if (dm.Type == DataModelType.Data){
            Receipt latch = LatchAction.getData(dm);
            if(latch.isSuccess())return new AjaxResult(Collections.first(((List<Record>)latch.getData()),null)).setDataType(AjaxResultDataType.Record);
        }
        //dao不为，表示以此数据库连接执行模版的sql语句,并且不再执行事件
        AnalyzeAction analyze = new AnalyzeAction(dm, DbSqlDataType.GetBean);
        if (dao != null){
            dao.setTag(dm.getRaw().Name);
            Record result = dao.fetch(dm.getSqlText());
            return new AjaxResult(result).setDuration(starttime);
        }
        KeepDaoPool keepDaoPool = new KeepDaoPool(supportTran);
        try{
            KeepDao keepdao = keepDaoPool.createKeepDao(dm.getConn());
            dm.setDialect(keepdao.Dao.getDialect());
            keepdao.Dao.setTag(dm.getRaw().Name);
            Record record = keepdao.Dao.fetch(dm.getSqlText());
            if (record == null) {
                return Strings.hasValue(keepdao.Dao.getErrorMessage())? new AjaxResult(500, "执行错误").setDataType(AjaxResultDataType.Error).setData(keepdao.Dao.getErrorMessage()) : new AjaxResult(404,"数据记录不存在");
            }
            List<Record> subQueryAction = dm.getRelationAction("query");
            if (subQueryAction != null) subActionQuery(keepDaoPool, record, subQueryAction, dm);
            subQueryAction = dm.getRelationAction("entity");
            if (subQueryAction != null)subActionQuery(keepDaoPool, record, subQueryAction, dm);
            analyze.countDataEnd(record);
            List<Record> rrss = new ArrayList<Record>();
            rrss.add(record);
            List<Record> ret = handleQueryResult(keepDaoPool, rrss, dm);
            return new AjaxResult().setData(Collections.first(ret,null).item2).setDuration(starttime).setDataType(AjaxResultDataType.Record);
        }finally {
            keepDaoPool.end();
        }

    }
    /**
     * 取值查询(对外)
     * @param tableName 表键名
     * @param columns 列名
     * @param condition 条件文本
     * @return
     */

    public static AjaxResult getValue(CharSequence tableName, String columns, String condition){
        return getValue(tableName,columns,condition,"","");
    }
    /**
     * 取值查询(对外)
     * @param tableName 表键名
     * @param columns 列名
     * @param condition 条件文本
     * @param param 参数文本
     * @param rel 回调资源
     * @return
     */
    public static AjaxResult getValue(CharSequence tableName, String columns, String condition, String param, String rel){
        long startTime = Times.getTS();
        String v = getValue(tableName, columns, new Cnd(Strings.mapping(condition)), param, String.class);
        return v == null ? new AjaxResult(false).setRel(rel).setDuration(startTime) : new AjaxResult().setData(v).setRel(rel).setDuration(startTime);
    }

    /**
     * 取值查询
     * @param tableName 数据模型
     * @param columns 列选
     * @param cnd 条件对象
     * @return
     */
    public static String getValue(CharSequence tableName, String columns, Cnd cnd){
        return getValue(tableName,columns,cnd,null,String.class);
    }
    /**
     * 取值查询
     * @param tableName 数据模型
     * @param columns 列选
     * @param cnd 条件对象
     * @param param 扩展数据
     * @param clazz 泛型类
     * @param <T>
     * @return
     */
    public static <T> T getValue(CharSequence tableName, String columns, Cnd cnd, String param,Class<T> clazz){
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm == null || dm.isNull()) return null;
        AnalyzeAction analyze = new AnalyzeAction(dm, DbSqlDataType.GetValue);
        dm.matchColumns(columns);
        dm.setCondition(cnd);
        if (DataModelType.Data.equals(dm.Type)){
            Receipt latch = LatchAction.getData(dm);
            if (latch.isSuccess()) return Collections.first(((List<Record>)latch.getData()),null).item2.get(dm.getColumn(),clazz);
        }
        SqlText where = dm.getWhere();
        String querySqlText = "select " + dm.getColumn() + " from " + dm.getTableName() + where.getCmd() + dm.getGroup() + dm.getOrder();
        IDao dao = DaoSeesion.NewDao(dm.getConn());
        dao.setTag(dm.getRaw().Name) ;
        T value = dao.getValue(new SqlText(querySqlText,where.getParameters()), clazz);
        analyze.endPoint();
        return value;
    }
    /**
     * 获取文本值集合
     * @param tableName 模版名称
     * @param columns 列节选
     * @return
     */
    public static  List<String> getValues(CharSequence tableName, String columns){
        return getValues(tableName,columns,null,null);
    }

    /***
     * 获取文本值集合
     * @param tableName 模版名称
     * @param columns 列节选
     * @param cnd 条件
     * @return
     */

    public static List<String> getValues(CharSequence tableName, String columns, Object cnd){
        return getValues(tableName,columns,cnd,null);
    }
    /***
     * 获取文本值集合
     * @param tableName 模版名称
     * @param columns 列节选
     * @param cnd 条件
     * @param param 扩展参数
     * @return
     */
    public static  List<String> getValues(CharSequence tableName, String columns, Object cnd, String param){
        return getValues(tableName, columns, cnd, param,String.class);
    }

    /**
     * 获取值集合
     * @param tableName 模型名称
     * @param columns 列选
     * @param cnd 条件对象
     * @param param 扩展数据
     * @param clazz 泛型类
     * @param <T>
     * @return
     */
    public static <T> List<T> getValues(CharSequence tableName, String columns, Object cnd, String param,Class<T> clazz){
        IDataModel dm = DaoSeesion.getDataModel(tableName, param);
        if (dm == null || dm.isNull()) return null;
        dm.matchColumns(columns);
        if (dm.isNull()) return null;
        IDao dao = DaoSeesion.NewDao(dm.getConn());
        dao.setTag(dm.getRaw().Name);
        try{
            dm.setDialect(dao.getDialect());
            AnalyzeAction analyze = new AnalyzeAction(dm, DbSqlDataType.GetData);
            dm.setCondition(Cnd.parse(cnd, dm));
            if (DataModelType.Data.equals(dm.Type)){
                Receipt latch = LatchAction.getData(dm);
                if (latch.isSuccess()) return Collections.select(((List<Record>)latch.getData()),record -> record.get(0,clazz));
            }
            String querySqlText = "select " + dm.getColumn() + " from " + dm.getTableName() +
                    dm.getWhere() + dm.getGroup() + dm.getOrder();
            List<Record> rs = dao.query(new SqlText(querySqlText));
            if (rs == null) return Strings.hasValue(dao.getErrorMessage())? null:new ArrayList<T>();
            analyze.countCacheData(rs).endPoint();
            return Collections.select(rs,r->r.get(0,clazz));
//
//            if (typeof(T).IsPrimitive || typeof(T).ToString().ToLower().Equals("system.string"))
//            {
//                return rs.ConvertAll(r => r.Get<T>(0));
//            }
//            return rs.ConvertAll(r => r.ToClass<T>()); ;
        }
        finally{
            dao.close();
        }
    }

    /***
     * 获取记录数量
     * @param tableName 模版名称
     * @param condition 条件
     * @return
     */
    public static long getCount(CharSequence tableName, Object condition){
        return getCount(tableName,condition,null);
    }

    /***
     * 获取记录数量
     * @param tableName 模版名称
     * @param condition 条件
     * @param param 扩展参数
     * @return
     */

    public static long getCount(CharSequence tableName, Object condition, String param){
        IDataModel dm = DaoSeesion.getDataModel(tableName.toString(), param);
        if (dm.isNull()) return -1;
        IDao dao = DaoSeesion.NewDao(dm.getConn());
        dm.setDialect( dao.getDialect());
        dm.setCondition(Cnd.parse(condition, dm));
        if (DataModelType.Data.equals(dm.Type)){
            Receipt latch = LatchAction.getData(dm);
            if (latch.isSuccess()) return ((List<Record>)latch.getData()).size();
        }
        dao.setTag(dm.getRaw().Name);
        SqlText sqltext = dm.getWhere();
        long count = dao.getValue(new SqlText("select count(1) from " + dm.getTableName() + sqltext.getCmd() + dm.getGroup(),sqltext.getParameters()),Integer.class);
        dao.close();
        return count;
    }

    /**
     * 处理查询结果集
     * @param rs 纪录集
     * @param dm 数据模型
     * @return
     */
    public static List<Record> handleQueryResult(List<Record> rs, IDataModel dm){
        KeepDaoPool keep = new KeepDaoPool();
        try{
            handleQueryResult(keep, rs, dm);
        }finally {
            keep.end();
        }
        return rs;
    }

    /**
     * 处理查询结果集
     * @param keepDaoPool 持续数据连接池
     * @param rs 纪录集
     * @param dm 数据模型
     * @return
     */
    public static List<Record> handleQueryResult(KeepDaoPool keepDaoPool, List<Record> rs,IDataModel dm){
        if (Rs.isBlank(rs)) return rs;
        List<Record> actionlist = dm.getRelationAction("resultset");
        if (Rs.isBlank(actionlist)) return rs;
        String RegStr_FieldName="^\\*(\\*?)([\\w]*)\\.([\\w]*)$";
        for (Record action : actionlist){
            int option = action.getInt("option", true);
            String tablename = action.getString("tableName", true);
            String fieldname = action.getString("fieldname", true);
            String relation = action.getString("relation");
            String condition = action.getString("condition");
            switch (DbSqlDataType.get(option)){
                case GetData:
                {
                    String conditionstring = action.getString("condition");
                    String param = action.getString("param", true);
                    Record cnd = Record.parse(conditionstring);
                    //级联情况
                    if (cnd.size() == 1 && Regex.isMatch(cnd.first().key, "(^\\w*$)|(^\\w*:=$)") && !Strings.isJson(cnd.first().value.toString())){
                        String key = cnd.first().key.split(":")[0];
                        String node =
                                cnd.first()
                                        .value.toString()
                                        .replace("${", "")
                                        .replace("}", "")
                                        .replace("@@", "")
                                        .replace("&", "")
                                        .trim();
                        final String[] vs = {""};
                        rs.forEach(x -> vs[0] += Strings.mapping(cnd.first().value.toString(),x) + ",");
                        IDataModel subdm = DaoSeesion.getDataModel(tablename, param);
                        subdm.matchColumns(action.getString("columns"));
                        subdm.setCondition(new Record(key + ":in", vs[0]));
                        List<Record> data = getData(keepDaoPool, subdm);
                        Record records = Record.turn(data,key,true);;
                        rs.forEach(x -> {
                            Object d = records.get(x.getString(node));
                            x.put(fieldname, d);
                        });
                    }else{
                        rs.forEach(x -> {
                            IDataModel subdm = DaoSeesion.getDataModel(tablename, param);
                            subdm.matchColumns(action.getString("columns"));
                            subdm.setCondition(Strings.mapping(conditionstring,x));
                            List<Record> data = getData(keepDaoPool, subdm);
                            Matcher match = Regex.match(fieldname,RegStr_FieldName);
                            if(match.find() && data!=null){
                                String fn =  match.group(3);
                                List<String> vv = Collections.select(data, t -> t.getString(fn));
                                x.put(match.group(2), Strings.isBlank(match.group(1))?vv:Strings.ArrayToString(vv,",",""));
                            }
                            else  x.put(fieldname, data);
                        });
                    }
                }
                break;
                case OneToMany:
                {
                    if (Strings.isBlank(relation)) continue;
                    Record mapping = Record.parse(relation);
                    if (mapping.size() < 1) continue;
                    IDataModel subdm = DaoSeesion.getDataModel(tablename, action.getString("param", true));
                    if (mapping.size() == 1){
                        String key = mapping.first().key;
                        String wfield = Collections.getString(subdm.getFieldConfig(key), "fieldname","");
                        String kv = mapping.first().value.toString();
                        final String[] vs = {""};
                        rs.forEach(x -> vs[0] += x.getString(kv) + ",");
                        subdm.matchColumns(action.getString("columns"));
                        subdm.setCondition(new Record(wfield + ":in", vs[0]));
                        if(Strings.hasValue(condition))subdm.setCondition(condition);
                        //if(Strings.hasValue(condition))subdm.setCondition(condition);
                        List<Record> data = getData(keepDaoPool, subdm);
                        Record records = Record.turn(data,key, true);
                        if(records!=null){
                            rs.forEach(x ->{
                                Object d = records.get(x.getString(kv));
                                Matcher match = Regex.match(fieldname,RegStr_FieldName);
                                if(match.find() && d!=null){
                                    String fn =  match.group(3);
                                    List<String> vv = Collections.select(((List<Record>) d), t -> t.getString(fn));
                                    x.put(match.group(2), Strings.isBlank(match.group(1))?vv:Strings.ArrayToString(vv,",",""));
                                }
                                else  x.put(fieldname, d);
                            });
                        }
                    }
                    else{
                        Record filemapping=new Record();
                        mapping.forEach((x,y) -> filemapping.put(x, Collections.getString(subdm.getFieldConfig(x),"fieldname","")));
                        Cnd cnd = new Cnd();
                        rs.forEach(x ->{
                            Cnd c = new Cnd();
                            mapping.forEach((y,z) -> c.put(filemapping.getString(y), x.getString(z.toString())));
                            cnd.or(c);
                        });
                        subdm.matchColumns(action.getString("columns"));
                        subdm.setCondition(cnd);
                        if(Strings.hasValue(condition))subdm.setCondition(condition);
                        List<Record> data = getData(keepDaoPool, subdm);
                        Map<String, List<Record>> turndic = new HashMap<String, List<Record>>();
                        data.forEach(x ->{
                            final String[] k = {"##"};
                            mapping.forEach((y,z) -> k[0] += x.getString(y) + "##");
                            List<Record> old = turndic.get(k[0]) != null ? turndic.get(k[0]) : new ArrayList<Record>();
                            old.add(x);
                            turndic.put(k[0], old);
                        });
                        rs.forEach(x ->{
                            final String[] k = {"##"};
                            mapping.forEach((y,z) -> k[0] += x.getString(z.toString()) + "##");
                            List<Record> d = turndic.get(k[0]);
                            Matcher match = Regex.match(fieldname, RegStr_FieldName);
                            if(match.find() && d!=null){
                                String fn =  match.group(3);
                                List<String> vv = Collections.select(((List<Record>) d), t -> t.getString(fn));
                                x.put(match.group(2), Strings.isBlank(match.group(1))?vv:Strings.ArrayToString(vv,",",""));
                            }
                            else  x.put(fieldname, d);
                        });
                    }
                }
                break;
                case OneToOne:
                {
                    if (Strings.isBlank(relation)) continue;
                    Record mapping = Record.parse(relation);
                    if (mapping.size() < 1) continue;
                    IDataModel subdm = DaoSeesion.getDataModel(tablename, action.getString("param", true));
                    if (mapping.size() == 1){
                        String key = mapping.first().key;
                        String wfield = Collections.getString(subdm.getFieldConfig(key),"fieldname","");
                        String kv = mapping.first().value.toString();
                        final String[] vs = {""};
                        rs.forEach(x -> vs[0] += x.getString(kv) + ",");
                        String columns =  action.getString("columns");
                        if(Strings.hasValue(columns))columns+=","+key;
                        subdm.matchColumns(columns);
                        subdm.setCondition(new Record(wfield + ":in", vs[0]));
                        if(Strings.hasValue(condition))subdm.setCondition(condition);
                        List<Record> data = getData(keepDaoPool, subdm);
                        Record records = Record.turn(data,key,false);
                        rs.forEach(x ->{
                            Object d = (records==null ? null:records.getObject(x.getString(kv)));
                            if (Regex.isMatch(fieldname, "^\\*")){
                                String fv = fieldname.replace("*","");
                                if(Strings.isBlank(fv))
                                    x.merge(Record.parse(d));
                                else{
                                    if(d==null)x.put(fv,null);
                                    else x.put(fv,Record.parse(d).get(fv));
                                }
                            }else{
                                x.put(fieldname, d);
                            }
                        });
                    }
                    else{
                        Record filemapping = new Record();
                        final String[] require = {""};
                        mapping.forEach((x,y) -> {
                            filemapping.put(x, Collections.getString(subdm.getFieldConfig(x),"fieldname",""));
                            require[0] +=(x+",");
                        });
                        Cnd cnd = new Cnd();
                        rs.forEach(x ->{
                            Cnd c = new Cnd();
                            mapping.forEach((y,z) -> c.put(filemapping.getString(y), x.getString(z.toString())));
                            cnd.or(c);
                        });
                        String columns = action.getString("columns");
                        if(Strings.hasValue(columns))columns+=Strings.rightLess(require[0],1);
                        subdm.matchColumns(columns);
                        subdm.setCondition(cnd);
                        if(Strings.hasValue(condition))subdm.setCondition(condition);
                        List<Record> data = getData(keepDaoPool, subdm);
                        Record turndic = new Record();
                        data.forEach(x ->{
                            final String[] k = {"##"};
                            mapping.forEach((y,z) -> k[0] += x.getString(y) + "##");
                            turndic.put(k[0], x);
                        });
                        rs.forEach(x ->{
                            final String[] k = {"##"};
                            mapping.forEach((y,z) -> k[0] += x.getString(z.toString()) + "##");
                            Object d = turndic.get(k[0]);
                            if (Regex.isMatch(fieldname, "^\\*")){
                                String fv = fieldname.replace("*","");
                                if(Strings.isBlank(fv))
                                    x.merge(Record.parse(d));
                                else{
                                    if(d==null)x.put(fv,null);
                                    else x.put(fv,Record.parse(d).get(fv));
                                }
                            }
                            else  x.put(fieldname, d);
                        });
                    }
                }
                break;
                default:
                    break;
            }
        }
        return rs;
    }


    /// <summary>
    /// 子属查询动作
    /// </summary>
    /// <param name="keepDaoPool">持续数据库链接池</param>
    /// <param name="record">本记录对象</param>
    /// <param name="actionlList">子动作列表</param>
    /// <param name="parentDaoHelp"></param>
    public static void subActionQuery(KeepDaoPool keepDaoPool, Record record, List<Record> actionlList,IDataModel parentDaoHelp)
    {
        if (record == null) return;
        for (Record res : actionlList)
        {
            Curd curd = res.toClass(Curd.class);
            if (curd == null) continue;
            if (Strings.isBlank(curd.SqlText)) curd.SqlText = res.getString("sql");
            String fieldkey = res.getString("fieldname", true);
            Object result;
            boolean done = false;//已经完成本次处理
//            switch (curd.Option)
//            {
//                case DbSqlDataType.Script.getIndex():
//                {
////                    done = true;
////                    var jscode = parentDaoHelp.GetScript();
////                    if (jscode.IsBlank()) continue;
////                    var fname = res.GetString("function,funname,tablename", true);
////                    if (fname.IsBlank()) continue;
////                    fname = fname.Replace("()", "").Trim();
////                    if (Regex.IsMatch(jscode, "function\\s*" + fname + "\\s*\\(\\)"))
////                    {
////                        string re;
////                        ScriptAction scriptAction = null;
////                        try
////                        {
////                            scriptAction = new ScriptAction(jscode);
////                            scriptAction.LoadDefaultFuns();
////                            scriptAction.PushKeepDaoPool(keepDaoPool);
////                            re = scriptAction.Exec(fname);
////                            scriptAction.Dispose();
////                        }
////                        catch (Exception)
////                        {
////                            continue;
////                        }
////                        finally
////                        {
////                            scriptAction?.Dispose();
////                        }
////                        if (re.HasValue()) re = re.Trim();
////                        if (re.IsBlank() && fieldkey.IsBlank()) continue;
////
////                        if (!Regex.IsMatch(re, "^[\\{\\[][\\w\\W]*?[\\}\\]]$") && fieldkey.HasValue())
////                        {
////                            record.Put(fieldkey, re);
////                            re = null;
////                        }
////                        if (re.HasValue() && Regex.IsMatch(re, "^\\{[\\w\\W]*?}$"))
////                        {
////                            if (fieldkey.IsBlank()) record.Merge(Record.Parse(re));
////                            else record.Put(fieldkey, Record.Parse(re));
////                            re = null;
////                        }
////                        if (re.HasValue() && Regex.IsMatch(re, "^\\[[\\w\\W]*?]$") && fieldkey.IsBlank())
////                        {
////                            record.Put(fieldkey, Json.Json.ToObject<List<Record>>(re));
////                        }
////                    }
//                }
//                break;
//                case DbSqlDataType.Program.getIndex():
//                {
////                    var className = res.GetString("class,type,function,tablename", true);
////                    if (className.IsBlank()) continue;
////                    var methodName = res.GetString("method,function", true);
////                    if (className.IsBlank()) continue;
////                    if (methodName.IsBlank())
////                    {
////                        methodName = className.Split('.').Last();
////                        className=className.RightLess(methodName.Length + 1);
////                    }
////                    var ps = res.GetString("params,bean").Mapping(record);
////                    var methodRecord = Record.Parse(ps);
////                    var path = res.GetString("path", true);
////                    Type pclass;
////                    if (path.HasValue())
////                    {
////                        if (path.Contains("~")) path = path.Replace("~", Configs.BasicPath());
////                        if (!path.Contains("/") && !path.Contains("\\"))
////                        {
////                            path = Configs.BasicPath() + "/bin/" + path;
////                            path = Path.GetFullPath(path);
////                            if (!Files.IsExistFile(path)) throw new Exception("dll文件[" + path + "]不存在或者内容为空");
////                            var assembly = Assembly.LoadFile(path);
////                            pclass = assembly.GetType(className);
////                        }
////                        else
////                        {
////                            path = Path.GetFullPath(path);
////                            byte[] btyes = Files.GetFileBytes(path); //驱动文件的字节集
////                            if (btyes == null) throw new Exception("dll文件[" + path + "]不存在或者内容为空");
////                            var assembly = Assembly.Load(btyes);
////                            pclass = assembly.GetType(className);
////                        }
////                    }
////                    else
////                    {
////                        var cp = className.Split('.')[0];
////                        if (className.IndexOf("|", StringComparison.Ordinal) > 0)
////                        {
////                            cp = className.Split('|')[0];
////                            className = className.Replace("|", ".");
////                        }
////                        var assembly = Assembly.Load(cp);
////                        pclass = assembly.GetType(className);
////                    }
////                    if (pclass == null) throw new Exception("类[" + className + "]不存在");
////                    var method = pclass.GetMethod(methodName, BindingFlags.Instance | BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Static);
////                    if (method == null) continue;
////                    var instance = Activator.CreateInstance(pclass, true);
////                    var ret = method.Invoke(instance, ArgumentMapping.MappingMethodParam(method, methodRecord));
////                    if (ret == null) continue;
////                    if (fieldkey.HasValue())
////                    {
////                        record.Put(fieldkey, ret);
////                    }
////                    else
////                    {
////                        //if (ret is AjaxResult && ((AjaxResult)ret).Success) record.Put(fieldkey, ((AjaxResult)ret).data);
////                        //continue;
////                    }
//
//                }
//                break;
//
//            }
            //如果执行的是脚本
//            if (done) continue;
//            if (curd.Conn == null && parentDaoHelp.Conn != null) curd.Conn = parentDaoHelp.Conn;
//            //替代字符
//            if (Strings.hasValue(curd.Condition)) curd.Condition = Strings.Mapping(curd.Condition,record);
//            if (Strings.hasValue(curd.SqlText)) curd.SqlText = Strings.Mapping(curd.SqlText,record);
//            if (Strings.hasValue(curd.Param)) curd.Param = Strings.Mapping(curd.Param,record);
//            if (Strings.hasValue(curd.Bean)) curd.Bean = Strings.Mapping(curd.Bean,record);
//            if (Strings.hasValue(curd.TableName)) curd.TableName = Strings.Mapping(curd.TableName,record);
////                    daoHelp = DaoSeesion.GetDataModel(curd.TableName, curd.Param);
//            result = curd.execute(keepDaoPool);
//            if (curd.Option == DbSqlDataType.Query.getIndex() || curd.Option == DbSqlDataType.GetBean.getIndex())
//            {
//                if (fieldkey.IsBlank())record.Merge((Record) result);
//                else record.Put(fieldkey, result);
//            }
//            else if (curd.Option == (int) SqlDataAction.GetData&& fieldkey.HasValue())
//            {
//                record.Put(fieldkey, result);
//            }
//            else if (curd.Option == (int) SqlDataAction.GetValue || curd.Option == (int) SqlDataAction.GetCount)
//            {
//                if (fieldkey.HasValue()) record.Put(fieldkey, result);
//            }
        }
    }

    /// <summary>
    /// 单次综合查询
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="columns">列字段</param>
    /// <param name="condition">条件</param>
    /// <param name="psize">分页数量</param>
    /// <param name="param">参数列表</param>
    /// <param name="rel">资源回执</param>
    /// <param name="option">操作选项</param>
    /// <param name="start">起始页</param>
    /// <param name="pageNumber">当前页</param>
    /// <param name="recordCount">记录数量</param>
    /// <returns></returns>
    public static AjaxResult query(String tableName, String columns, String condition, String option, String param, String psize, int start, int pageNumber, String recordCount, String rel)
    {
        long starttime = Times.getTS();
        if (!Strings.isBlank(condition)) condition = Strings.mapping(condition);
        if (Strings.isBlank(tableName) ) return new AjaxResult(444);
        if (Strings.isBlank(psize)) psize = "20";
        if (Strings.isBlank(option)) option = "11";
        if (Strings.isBlank(option)  ||  option.equals("0") || option.equals("10"))
        {
            return getBean(tableName, condition, columns, param, rel);
        }
        if (option.equals("11"))
        {
            return getData(tableName, condition, columns, param, rel);
        }
        if (option.equals("12"))
        {
            int pagesize = Integer.parseInt(psize);
            return getPageData(tableName, condition, columns, param, start, pageNumber, pagesize,recordCount);
        }
        if (option.equals("22"))
        {
            return queryData(tableName, condition, Integer.parseInt(psize), param,null);
        }
        if (option.equals("111"))
        {
            return getValue(tableName, columns, condition, param, rel);
        }
        if (option.equals("123"))
        {
            IDao dao = DaoSeesion.NewDao(); //GetDao(); ?????getdao ne?
            IDataModel daoHelp = DaoSeesion.getDataModel(tableName, param);
            if (daoHelp.isNull()) return new AjaxResult(700);
            daoHelp.setCondition(condition);
            int count = dao.count(daoHelp.getTableName(), daoHelp.getWhere());
            return new AjaxResult().setData(count).setDuration(starttime);
        }
        return new AjaxResult(400, "请求不为支持").setDuration(starttime);
    }

    /**
     * 多查询
     * @param settings 配置数据
     * @return
     */
    public static AjaxResult querys(String settings){
        if (Strings.isBlank(settings)) return new AjaxResult(444);
//        settings = Strings.trim(settings) sttings.;
        settings=Strings.mapping(settings.trim()) ;
        long starttime = Times.getTS();
        if ( settings.startsWith("{") && settings.endsWith("}")){
            Record record = Json.toObject(settings,Record.class);
            if (record == null ||  record.size()  < 1) return new AjaxResult(444);
            //如果以单对象形式存在
            String tableName = record.getString("tableName", true);
            if (Strings.hasValue(tableName) && !Regex.isMatch(record.getString(tableName),"^\\s*[\\{\\[]")) {
                return query(
                        tableName,
                        Json.toJson(record.getObject("columns", true)),
                        Json.toJson(record.getObject("condition", true)),
                        record.getString("option", true),
                        Json.toJson(record.getObject("param", true)),
                        record.getString("psize", true),
                        record.getInt("start",false),
                        record.getInt("pageNumber",false),
                        record.getString("recordCount",false),
                        record.getString("rel", true)
                );
            }
            Record ret = new Record();
            for (String key : record.keySet()){
                Record item = null;
//                if (record.getObject(key) instanceof JObject)
//                {
//                    item = (record.Get(key) as JObject)?.ToObject<Dictionary<string, object>>();
//                }
                if (item == null && record.getObject(key) instanceof Record){
                    item = (Record)record.getObject(key);
                }
                if (item == null && record.getObject(key) instanceof JSONObject){
                    JSONObject val = (JSONObject) record.getObject(key);
                    item=new Record(val.getInnerMap());
                    //item = val.toJavaObject(Record.class);
                    
                }
                if (item != null){
                    AjaxResult itemAjaxJson = query(
                            item.getString("tableName", true),
                            Json.toJson(item.getObject("columns")),
                            Strings.mapping(Json.toJson(item.getObject("condition")), ret),
                            item.getString("option"),
                            Json.toJson(item.getObject("param")),
                            item.getString("psize,pageSize"),
                            record.getInt("start", false),
                            record.getInt("pageNumber", false),
                            record.getString("recordCount", false),
                            item.getString("rel"));
                    ret.put(key, itemAjaxJson);
                }
                else ret.put(key, new AjaxResult(400, "请求参数不支持"));
            }
            return new AjaxResult().setData(ret).setDuration(starttime);
        }
        if (settings.startsWith("[") && settings.endsWith("]")){
            List<Record> records = Json.toObject(settings, List.class);
            if (records == null || records.size() < 1) return new AjaxResult(444);
            AjaxResult ret = new AjaxResult();
            for (Record record : records){
                AjaxResult itemResult = query(
                        record.getString("tableName"),
                        Json.toJson(record.getObject("columns")),
                        Json.toJson(record.getObject("condition")),
                        record.getString("option"),
                        Json.toJson(record.getObject("param")),
                        record.getString("psize,pageSize"),
                        record.getInt("start"),
                        record.getInt("pageNumber"),
                        record.getString("recordCount"),
                        record.getString("rel")
                );
                ret.pushData(itemResult);
            }
            return ret.setDuration(starttime);
        }
        return new AjaxResult(444).setDuration(starttime);
    }
}
