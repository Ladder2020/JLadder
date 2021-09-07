package com.jladder.actions.impl;

import com.jladder.configs.Configs;
import com.jladder.actions.AnalyzeOption;
import com.jladder.data.Record;
import com.jladder.datamodel.IDataModel;
import com.jladder.db.Rs;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.hub.DataHub;
import com.jladder.lang.Collections;
import com.jladder.lang.func.Func1;
import com.jladder.logger.LogForDataModelByCompare;
import com.jladder.logger.LogForDataModelByKeep;
import com.jladder.logger.LogForDataModelByRate;
import com.jladder.logger.LogForDataModelByVisit;
import com.jladder.net.http.HttpHelper;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.lang.Times;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/// <summary>
/// 数据应用分析
/// </summary>
public  class AnalyzeAction
{
    /// <summary>
    /// 是否使用缓存
    /// </summary>
    public boolean IsCache;
    /// <summary>
    /// 是否已经终点
    /// </summary>
    public boolean IsEnd ;
    /// <summary>
    /// 数据模型
    /// </summary>
    public IDataModel DataModel;
    /// <summary>
    /// 实际本次统计科目
    /// </summary>
    public List<AnalyzeOption> Subjects=new ArrayList<>();

    /// <summary>
    /// 日志集
    /// </summary>
    private final Map<AnalyzeOption,Object> logs=new HashMap<AnalyzeOption, Object>();

    /// <summary>
    /// 执行动作
    /// </summary>
    public DbSqlDataType Action = DbSqlDataType.Query;
    /// <summary>
    /// 是否检查数据过期
    /// </summary>
    public boolean CheckDataOutDate;

    /// <summary>
    /// 实例化
    /// </summary>
    /// <param name="dm">数据模型</param>
    public AnalyzeAction(IDataModel dm)
    {
        DataModel = dm;
        Init();
    }
    /// <summary>
    /// 实例化
    /// </summary>
    /// <param name="dm">数据模型</param>
    /// <param name="action">执行动作</param>
    public AnalyzeAction(IDataModel dm,DbSqlDataType action)
    {
        DataModel = dm;
        Action = action;
        Init();
    }
    /// <summary>
    /// Init方法
    /// </summary>
    private void Init()
    {
        if (!DataHub.Analyz) return;
        String items = "," + DataModel.getRaw().AnalyzeItems + ",";
        //访问统计
        if (items.indexOf(",visit,") > -1)
        {
            String userinfo = EnvAction.GetEnvValue("username")+"<"+ HttpHelper.getIp()+">";
            Subjects.add(AnalyzeOption.Visit);
            logs.put(AnalyzeOption.Visit, new LogForDataModelByVisit(DataModel.getName(),userinfo,"模版访问"));
        }
        //记录比对
        if (items.indexOf(",compare,") > -1)
        {
            logs.put(AnalyzeOption.Compare, new LogForDataModelByCompare(DataModel.getName(),EnvAction.GetEnvValue("userinfo")));
        }
        //数据持久化
        if (items.indexOf(",keep,") > -1)
        {
            logs.put(AnalyzeOption.Keep, new LogForDataModelByKeep(DataModel.getName(),EnvAction.GetEnvValue("userinfo")));
        }
        //数据吞吐率
        if (items.indexOf(",rate,") > -1)
        {
            logs.put(AnalyzeOption.Rate, new LogForDataModelByRate(DataModel.getName()));
        }
        //检查数据过期
        if (items.indexOf(",outdate,") > -1)
        {
            CheckDataOutDate = true;
        }
        if (logs.containsKey(AnalyzeOption.Visit))
        {
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



    /// <summary>
    /// 分析结束点
    /// </summary>

    public  void EndPoint()
    {
        if (IsEnd) return;
        IsEnd = true;
        if (!DataHub.Analyz) return;
//        if(WebScope.Configs!=null && !WebScope.Configs.IsAnalyz)return;
        if (logs.containsKey(AnalyzeOption.Visit))
        {
            LogForDataModelByVisit visit = (LogForDataModelByVisit) logs.get(AnalyzeOption.Visit);
            visit.SetEnd();
        }
        if (logs.containsKey(AnalyzeOption.Rate)&&Subjects.contains(AnalyzeOption.Rate))
        {
            LogForDataModelByRate rate = (LogForDataModelByRate) logs.get(AnalyzeOption.Rate);
            rate.SetEnd();
        }
        if (logs.containsKey(AnalyzeOption.Compare) && Action != DbSqlDataType.Update)
        {
            logs.remove(AnalyzeOption.Compare);
        }
        if (logs.containsKey(AnalyzeOption.Keep) && (Action!=DbSqlDataType.Delete&&Action!=DbSqlDataType.Insert&&Action!=DbSqlDataType.Truncate))
        {
            logs.remove(AnalyzeOption.Keep);
        }
        if (logs.containsKey(AnalyzeOption.Keep))
        {
            LogForDataModelByKeep keep = (LogForDataModelByKeep) logs.get(AnalyzeOption.Keep);
            keep.IsAdd = (DbSqlDataType.Insert.equals(Action));
        }


//        var logdata = Collections.first(logs,(x,y) -> Subjects.contains(x)).ToDictionary(x => x.Key, y => y.Value);
//        if (logdata.Any())
//        {
//            Logs.Write(logdata, LogOption.Analysis);
//        }

    }
    /// <summary>
    /// 统计缓存数据量
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="rs"></param>
    /// <returns></returns>
    public <T> AnalyzeAction CountCacheData(List<T> rs)
    {
        if (logs.containsKey(AnalyzeOption.Rate))
        {
            Subjects.add(AnalyzeOption.Rate);
            LogForDataModelByRate rate = (LogForDataModelByRate) logs.get(AnalyzeOption.Rate);
            rate.IsCache = true;
            rate.RecordCount = Core.isEmpty(rs) ? 0 : rs.size();
        }
        return this;
    }
    /// <summary>
    /// 统计缓存数据量并进入结束点
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="rs"></param>
    /// <returns></returns>
    public <T> List<T> CountCacheDataEnd(List<T> rs)
    {
        if (logs.containsKey(AnalyzeOption.Rate))
        {
            Subjects.add(AnalyzeOption.Rate);
            LogForDataModelByRate rate = (LogForDataModelByRate) logs.get(AnalyzeOption.Rate);
            rate.IsCache = true;
            rate.RecordCount = Rs.isBlank(rs)?0:rs.size();
            rate.SetEnd();
        }
        EndPoint();
        return rs;
    }
    /// <summary>
    /// 统计查询数据量并进入结束点
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="rs"></param>
    /// <returns></returns>
    public <T> List<T> CountDataEnd(List<T> rs)
    {
        if (logs.containsKey(AnalyzeOption.Rate))
        {
            Subjects.add(AnalyzeOption.Rate);
            LogForDataModelByRate rate = (LogForDataModelByRate) logs.get(AnalyzeOption.Rate);
            rate.RecordCount = Rs.isBlank(rs) ? 0 : rs.size();
            rate.SetEnd();
        }
        EndPoint();
        return rs;
    }
    /// <summary>
    /// 统计单数据查询结束
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="rs"></param>
    /// <returns></returns>
    public <T> T CountDataEnd(T rs)
    {
        if (logs.containsKey(AnalyzeOption.Rate))
        {
            Subjects.add(AnalyzeOption.Rate);
            LogForDataModelByRate rate = (LogForDataModelByRate) logs.get(AnalyzeOption.Rate);
            rate.RecordCount = 1;
            rate.SetEnd();
        }
        EndPoint();
        return rs;
    }
    /// <summary>
    /// 设置修改前期数据
    /// </summary>
    /// <param name="fun">前期数据回调方法</param>
    public void SetDataForUpdateBefore(Func1<List<Record>> fun)
    {
        if (logs.containsKey(AnalyzeOption.Compare))
        {
            Subjects.add(AnalyzeOption.Compare);
            LogForDataModelByCompare compare = (LogForDataModelByCompare) logs.get(AnalyzeOption.Compare);
            try {
                compare.OldRawData = fun.invoke();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /// <summary>
    /// 获取记录集的不同之处
    /// </summary>
    /// <param name="fun"></param>
    /// <param name="data"></param>
    /// <returns></returns>
    public List<Record> DifferentChange(Object data, Func1<List<Record>> fun )
    {
        if (logs.containsKey(AnalyzeOption.Compare))
        {
            try
            {
                Subjects.add(AnalyzeOption.Compare);
                LogForDataModelByCompare compare = (LogForDataModelByCompare) logs.get(AnalyzeOption.Compare);
                if (data == null && fun == null && compare != null && compare.Different != null)
                {
                    return compare.Different;
                }
                if (compare == null) return null;
                if (fun != null) compare.OldRawData = fun.invoke();
                if (compare.OldRawData == null) return null;
                if (data != null) compare.Data = data;
                if (compare.Data == null) return null;
                List<Record> rs = (List<Record>) compare.OldRawData;
                Record bean = Record.parse(compare.Data);
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
                        else
                        {
                            if(v !=null)newValue = v.toString();
                        }
                        if (Strings.isBlank(key) || (oldvalue != newValue && Strings.hasValue((oldvalue + newValue))))
                        {
                            Map<String, Object> config = DataModel.getFieldConfig(k);
                            if(config==null)config = new HashMap<String, Object>();
                            diff.put("fieldname", key).put("old", raw.get(key)).put("current", v).put("title", com.jladder.lang.Collections.getString(config,"title")).put("$rn", index);
                            ret.add(diff);
                        }
                    });
                    index.getAndIncrement();
                });
                compare.Different = ret;
                return ret;
            }
            catch (Exception e)
            {
                //Logs.Write(e, "Ladder.Actions.AnalyzeAction.DifferentChange");
            }



        }
        return null;
    }

    /// <summary>
    /// 获取更新报告
    /// </summary>
    public String GetDifferentReport(String template)
    {
        if (Strings.isBlank(template)) template = Configs.getString("differentreport") ;
        if (Strings.isBlank(template)) template = "${title}[${fieldname}]:${old}=>${current}";

        List<Record> list = DifferentChange(null, null);

        StringBuilder outtext=new StringBuilder();
        if (list != null)
        {
            if (list.size() == 0) return "未有数据变化";
            String finalTemplate = template;
            list.forEach(x->outtext.append(Strings.mapping(finalTemplate,x) + "\n"));
            return outtext.toString();
        }
        return "";

    }


    /// <summary>
    /// 设置删除前期数据
    /// </summary>
    /// <param name="fun">前期数据回调方法</param>
    public void SetDataForDeleteBefore(Func1<List<Record>> fun)
    {
        if (logs.containsKey(AnalyzeOption.Keep))
        {
            Subjects.add(AnalyzeOption.Keep);
            LogForDataModelByKeep compare = (LogForDataModelByKeep) logs.get(AnalyzeOption.Keep);
            try {
                compare.Data = fun.invoke();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /// <summary>
    /// 设置后期数据
    /// </summary>
    /// <param name="data">数据</param>
    public void SetDataForAfter(Object data)
    {
        if (logs.containsKey(AnalyzeOption.Compare))
        {
            Subjects.add(AnalyzeOption.Compare);
            LogForDataModelByCompare compare = (LogForDataModelByCompare) logs.get(AnalyzeOption.Compare);
            compare.Data = data;
        }
        if (logs.containsKey(AnalyzeOption.Keep))
        {
            Subjects.add(AnalyzeOption.Keep);
            LogForDataModelByKeep compare = (LogForDataModelByKeep) logs.get(AnalyzeOption.Keep);
            compare.Data = data;
        }
    }
    /// <summary>
    /// 检查过期时间
    /// </summary>
    /// <param name="fun">获取记录集过程</param>
    /// <param name="current">当前记录</param>
    /// <returns></returns>
    public boolean CheckOutDate(Func1<List<Record>> fun,Record current)
    {
        if (CheckDataOutDate)
        {
            List<String> fieldnames = DataModel.getFields("sign", "updatetime");

            long easydate = Times.getTS();
            String easyfieldname = "";
            for (String fieldname : fieldnames)
            {
                String f = Collections.haveKey(current,"!" + fieldname,fieldname);
                if(Strings.isBlank(f))continue;
                String old = current.getString(f);

                long d = Times.ams(old);
                if (easydate > d)
                {
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

