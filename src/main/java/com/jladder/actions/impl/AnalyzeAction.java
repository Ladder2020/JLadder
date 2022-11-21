package com.jladder.actions.impl;
import com.jladder.Ladder;
import com.jladder.actions.AnalyzeOption;
import com.jladder.data.Record;
import com.jladder.datamodel.IDataModel;
import com.jladder.db.Rs;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.lang.Collections;
import com.jladder.lang.*;
import com.jladder.lang.func.Func1;
import com.jladder.logger.*;
import com.jladder.net.http.HttpHelper;
import com.jladder.web.WebScope;
import com.jladder.web.WebScopeOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据应用分析
 */
public  class AnalyzeAction{
    /**
     * 是否使用缓存
     */
    public boolean isCache;
    /**
     * 是否已经终点
     */
    public boolean isEnd ;
    /**
     * 数据模型
     */
    public IDataModel DataModel;
    /**
     * 实际本次统计科目
     */
    public List<AnalyzeOption> subjects=new ArrayList<>();

    /**
     * 日志集
     */
    private final Map<AnalyzeOption,Object> logs=new HashMap<AnalyzeOption, Object>();

    /**
     * 执行动作
     */
    public DbSqlDataType action = DbSqlDataType.Query;
    /**
     * 是否检查数据过期
     */
    public boolean checkDataOutDate;
    /**
     * 实例化
     * @param dm 数据模型
     */
    public AnalyzeAction(IDataModel dm){
        DataModel = dm;
        init();
    }
    /**
     * 实例化
     * @param dm 数据模型
     * @param action 操作动作
     */
    public AnalyzeAction(IDataModel dm,DbSqlDataType action){
        DataModel = dm;
        action = action;
        init();
    }

    /**
     * Init方法
     */
    private void init(){
        if (!Ladder.Settings().isAnalyze()) return;
        String items = "," + DataModel.getRaw().AnalyzeItems + ",";
        //访问统计
        if (items.indexOf(",visit,") > -1){
            String userinfo = EnvAction.getEnvValue("username")+"<"+ HttpHelper.getIp()+">";
            subjects.add(AnalyzeOption.Visit);
            logs.put(AnalyzeOption.Visit, new LogForDataModelByVisit(DataModel.getName(),userinfo,"模版访问"));
        }
        //记录比对
        if (items.indexOf(",compare,") > -1){
            logs.put(AnalyzeOption.Compare, new LogForDataModelByCompare(DataModel.getName(),EnvAction.getEnvValue("userinfo")));
        }
        //数据持久化
        if (items.indexOf(",keep,") > -1){
            logs.put(AnalyzeOption.Keep, new LogForDataModelByKeep(DataModel.getName(),EnvAction.getEnvValue("userinfo")));
        }
        //数据吞吐率
        if (items.indexOf(",rate,") > -1){
            logs.put(AnalyzeOption.Rate, new LogForDataModelByRate(DataModel.getName()));
        }
        //检查数据过期
        if (items.indexOf(",outdate,") > -1){
            checkDataOutDate = true;
        }
        if (logs.containsKey(AnalyzeOption.Visit)){
//            Task.Factory.StartNew(() =>
//                    {
//                            var dao = DaoSeesion.NewDao(DataHub.TemplateConn);
//            try
//            {
//                dao.IsWriteLog = false;
//                dao.Exec(new SqlText("update " + DataHub.TemplateTableName + " set VisitTimes = VisitTimes+1 where name='" +DataModel.Raw.Name + "'"));
//            }
//            catch (Exception)
//            {
//                // ignored
//            }
//            finally
//            {
//                dao.Close();
//            }
//                });
        }
    }


    /**
     * 分析结束点
     */
    public void endPoint(){
        if (isEnd) return;
        isEnd = true;
        if (!Ladder.Settings().isAnalyze()) return;
        Object isAnalyz = WebScope.getValue(WebScopeOption.Analyz);
        if(isAnalyz != null && isAnalyz.equals(false))return;
        if (logs.containsKey(AnalyzeOption.Visit)) {
            LogForDataModelByVisit visit = (LogForDataModelByVisit) logs.get(AnalyzeOption.Visit);
            visit.setEnd();
            Logs.write(visit,LogOption.Analysis);
        }
        if (logs.containsKey(AnalyzeOption.Rate)&&subjects.contains(AnalyzeOption.Rate)){
            LogForDataModelByRate rate = (LogForDataModelByRate) logs.get(AnalyzeOption.Rate);
            rate.setEnd();
            Logs.write(rate,LogOption.Analysis);
        }
        if (logs.containsKey(AnalyzeOption.Compare) && action != DbSqlDataType.Update){
            logs.remove(AnalyzeOption.Compare);
        }
        if (logs.containsKey(AnalyzeOption.Keep) && (action!=DbSqlDataType.Delete&&action!=DbSqlDataType.Insert&&action!=DbSqlDataType.Truncate)){
            logs.remove(AnalyzeOption.Keep);
        }
        if(logs.containsKey(AnalyzeOption.Compare)){
            Logs.write((LogForDataModelByCompare) logs.get(AnalyzeOption.Compare),LogOption.Analysis);
        }
        if (logs.containsKey(AnalyzeOption.Keep)){
            LogForDataModelByKeep keep = (LogForDataModelByKeep) logs.get(AnalyzeOption.Keep);
            keep.isAdd = (DbSqlDataType.Insert.equals(action));
            Logs.write((LogForDataModelByKeep) logs.get(AnalyzeOption.Keep),LogOption.Analysis);
        }
    }
    /**
     * 统计缓存数据量
     * @param rs
     * @param <T>
     * @return
     */
    public <T> AnalyzeAction countCacheData(List<T> rs){
        if (logs.containsKey(AnalyzeOption.Rate)) {
            subjects.add(AnalyzeOption.Rate);
            LogForDataModelByRate rate = (LogForDataModelByRate) logs.get(AnalyzeOption.Rate);
            rate.IsCache = true;
            rate.recordcount = Core.isEmpty(rs) ? 0 : rs.size();
        }
        return this;
    }
    /**
     * 统计缓存数据量并进入结束点
     * @param rs
     * @param <T>
     * @return
     */
    public <T> List<T> countCacheDataEnd(List<T> rs){
        if (logs.containsKey(AnalyzeOption.Rate)){
            subjects.add(AnalyzeOption.Rate);
            LogForDataModelByRate rate = (LogForDataModelByRate) logs.get(AnalyzeOption.Rate);
            rate.IsCache = true;
            rate.recordcount = Rs.isBlank(rs)?0:rs.size();
            rate.setEnd();
        }
        endPoint();
        return rs;
    }
    /**
     * 统计查询数据量并进入结束点
     * @param rs
     * @param <T>
     * @return
     */
    public <T> List<T> countDataEnd(List<T> rs) {
        if (logs.containsKey(AnalyzeOption.Rate)){
            subjects.add(AnalyzeOption.Rate);
            LogForDataModelByRate rate = (LogForDataModelByRate) logs.get(AnalyzeOption.Rate);
            rate.recordcount = Rs.isBlank(rs) ? 0 : rs.size();
            rate.setEnd();
        }
        endPoint();
        return rs;
    }

    /**
     * 统计单数据查询结束
     * @param rs
     * @param <T>
     * @return
     */
    public <T> T countDataEnd(T rs){
        if (logs.containsKey(AnalyzeOption.Rate)){
            subjects.add(AnalyzeOption.Rate);
            LogForDataModelByRate rate = (LogForDataModelByRate) logs.get(AnalyzeOption.Rate);
            rate.recordcount = 1;
            rate.setEnd();
        }
        endPoint();
        return rs;
    }

    /**
     * 设置修改前期数据
     * @param fun 前期数据回调方法
     */
    public void setDataForUpdateBefore(Func1<List<Record>> fun){
        if (logs.containsKey(AnalyzeOption.Compare)){
            subjects.add(AnalyzeOption.Compare);
            LogForDataModelByCompare compare = (LogForDataModelByCompare) logs.get(AnalyzeOption.Compare);
            try {
                compare.oldrawdata = fun.invoke();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取记录集的不同之处
     * @param data 数据
     * @param fun 处理回调
     * @return
     */
    public List<Record> differentChange(Object data, Func1<List<Record>> fun){
        if (logs.containsKey(AnalyzeOption.Compare)){
            try{
                subjects.add(AnalyzeOption.Compare);
                LogForDataModelByCompare compare = (LogForDataModelByCompare) logs.get(AnalyzeOption.Compare);
                if (data == null && fun == null && compare != null && compare.different != null){
                    return compare.different;
                }
                if (compare == null) return null;
                if (fun != null) compare.oldrawdata = fun.invoke();
                if (compare.oldrawdata == null) return null;
                if (data != null) compare.data = data;
                if (compare.data == null) return null;
                List<Record> rs = (List<Record>) compare.oldrawdata;
                Record bean = Record.parse(compare.data);
                List<Record> ret = new ArrayList<Record>();
                AtomicInteger index = new AtomicInteger();
                rs.forEach(raw ->{
                    bean.forEach((k,v) -> {
                        Record diff = new Record();
                        String key = raw.haveKey(Regex.isMatch(k, "^[\\*\\$\\#]") ? k.substring(1) : k);
                        if (Strings.isBlank(key)) return;
                        String oldvalue = raw.getString(key);
                        String newValue="";
                        if (v instanceof Date) newValue =  Times.sDT ((Date)v);
                        else{
                            if(v !=null)newValue = v.toString();
                        }
                        if (Strings.isBlank(key) || (oldvalue != newValue && Strings.hasValue((oldvalue + newValue)))){
                            Map<String, Object> config = DataModel.getFieldConfig(k);
                            if(config==null)config = new HashMap<String, Object>();
                            diff.put("fieldname", key).put("old", raw.get(key)).put("current", v).put("title", com.jladder.lang.Collections.getString(config,"title","")).put("$rn", index);
                            ret.add(diff);
                        }
                    });
                    index.getAndIncrement();
                });
                compare.different = ret;
                return ret;
            }
            catch (Exception e)
            {
                //Logs.Write(e, "Ladder.Actions.AnalyzeAction.DifferentChange");
            }
        }
        return null;
    }
    /**
     * 获取更新报告
     * @param template 模型
     * @return
     */
    public String getDifferentReport(String template){
        if (Strings.isBlank(template)) template = Ladder.Settings().getDataDifferentReport();
        if (Strings.isBlank(template)) template = "${title}[${fieldname}]:${old}=>${current}";
        List<Record> list = differentChange(null, null);
        StringBuilder outtext=new StringBuilder();
        if (list != null){
            if (list.size() == 0) return "未有数据变化";
            String finalTemplate = template;
            list.forEach(x->outtext.append(Strings.mapping(finalTemplate,x) + "\n"));
            return outtext.toString();
        }
        return "";

    }
    /**
     * 设置删除前期数据
     * @param fun 前期数据回调方法
     */
    public void setDataForDeleteBefore(Func1<List<Record>> fun){
        if (logs.containsKey(AnalyzeOption.Keep)){
            subjects.add(AnalyzeOption.Keep);
            LogForDataModelByKeep compare = (LogForDataModelByKeep) logs.get(AnalyzeOption.Keep);
            try {
                compare.data = fun.invoke();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 设置后期数据
     * @param data 数据
     */
    public void setDataForAfter(Object data){
        if (logs.containsKey(AnalyzeOption.Compare)){
            subjects.add(AnalyzeOption.Compare);
            LogForDataModelByCompare compare = (LogForDataModelByCompare) logs.get(AnalyzeOption.Compare);
            compare.data = data;
        }
        if (logs.containsKey(AnalyzeOption.Keep)){
            subjects.add(AnalyzeOption.Keep);
            LogForDataModelByKeep compare = (LogForDataModelByKeep) logs.get(AnalyzeOption.Keep);
            compare.data = data;
        }
    }

    /**
     * 检查过期时间
     * @param fun 获取记录集过程
     * @param current 当前记录
     * @return
     */
    public boolean checkOutDate(Func1<List<Record>> fun,Record current){
        if (checkDataOutDate){
            List<String> fieldnames = DataModel.getFields("sign", "updatetime");
            long easydate = Times.getTS();
            String easyfieldname = "";
            for (String fieldname : fieldnames){
                String f = Collections.haveKey(current,"!" + fieldname,fieldname);
                if(Strings.isBlank(f))continue;
                String old = current.getString(f);
                long d = Times.ams(old);
                if (easydate > d){
                    easydate = d;
                    easyfieldname = f;
                }
            }
            if (easydate < 1 && Strings.isBlank(easyfieldname)) return false;
            easyfieldname = Regex.replace(easyfieldname, "^!", "");
            List<Record> rs=null;
            if(fun!=null){
                try {
                    rs = fun.invoke();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(Core.isEmpty(rs))return true;

            for (Record record : rs)
            {
                String raw = record.getString(easyfieldname);
                Long d=Times.ams(raw);
                if (d > easydate) return false;
            }
        }
        return true;
    }
}

