package com.jladder.scheduler;
import com.jladder.actions.impl.QueryAction;
import com.jladder.actions.impl.SaveAction;
import com.jladder.data.AjaxResult;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.db.Rs;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.lang.*;
import com.jladder.lang.func.*;
import com.jladder.script.Script;
import com.jladder.logger.Logs;
import com.jladder.net.http.HttpHelper;
import com.jladder.proxy.ProxyService;
import com.jladder.scheduler.impl.JobSessionImpl;
import com.jladder.utils.rabbit.RabbitConfig;
import com.jladder.utils.rabbit.RabbitHelper;
import org.quartz.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduleJobService {
    /// <summary>
    ///
    /// </summary>
    private static ScheduleJobService jds = null;
    /// <summary>
    /// 任务会话
    /// </summary>
    private JobSession session = new JobSessionImpl();

    public void setSession(JobSession session){
        this.session = session;
    }
    /// <summary>
    /// 获取实例
    /// </summary>
    public synchronized static ScheduleJobService instance(){
        if(jds!=null)return jds;
        else {
            jds = new ScheduleJobService();
            return jds;
        }
    }


    /// <summary>
    /// 每次计划事件
    /// </summary>
    public Func2<ScheduleJob,Receipt> onSchedule;
//    /// <summary>
//    ///
//    /// </summary>
    public OnScheduleResult onScheduleResult;


    public static JobDetail getSchedule(String groupname,String jobname){
        return instance().session.getScheduleJob(jobname,groupname);
    }

    //public IJobSession Session ->() session ?? (session = new JobSession());
    public static List<ScheduleJob> getAllSchedule(){
        return getAllSchedule(null);
    }
    /// <summary>
    /// 获取全部调度任务
    /// </summary>
    /// <returns></returns>
    public static List<ScheduleJob> getAllSchedule(String groupname)
    {
        if (Strings.isBlank(groupname)) return instance().session.getAllSchedule();
        return Collections.where(instance().session.getAllSchedule(),x->x.getGroupname().equals(groupname));
    }
    /// <summary>
    /// 暂停当前调度
    /// </summary>
    /// <param name="groupName"></param>
    /// <param name="jobName"></param>
    public void pauseScheduleJob(String jobName,String groupName)
    {
        session.pauseScheduleJob(jobName,groupName);
    }
    /// <summary>
    /// 启动当前调度
    /// </summary>
    /// <param name="groupName"></param>
    /// <param name="jobName"></param>
    public void resumeScheduleJob(String jobName,String groupName)
    {
        session.resumeScheduleJob(jobName,groupName);
    }
    /// <summary>
    /// 修改调度
    /// </summary>
    /// <param name="schedu"></param>
    public void modifyScheduler(ScheduleJob schedu)
    {
        session.modifyScheduler(schedu);
    }
    /// <summary>
    /// 启动调度
    /// </summary>
    public static void startAllScheduler()
    {
        try {
            if(!instance().session.getScheduler().isStarted()){
                instance().session.startScheduler();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
    /// <summary>
    /// 强制关闭调度
    /// </summary>
    public static void shutdownScheduler()
    {
        instance().session.shutdownScheduler();
    }
    /// <summary>
    /// 移除任务调度
    /// </summary>
    /// <param name="jobName">任务名称</param>
    /// <param name="groupName">分组名称</param>
    public static void removeScheduler(String jobName, String groupName)
    {
        if(Strings.isBlank(groupName))groupName="DEFAULT";
        instance().session.deleteScheduleJob(jobName,groupName);
    }


    /// <summary>
    /// 获取任务调度的状态
    /// </summary>
    /// <param name="groupName">分组名称</param>
    /// <param name="jobName">任务名称</param>
    /// <returns></returns>

    public static String getStatus(String jobName,String groupName) {
        try{
            List<? extends Trigger> triggers = instance().session.getScheduler().getTriggersOfJob(new JobKey(jobName, groupName));
            if (Rs.isBlank(triggers)) return "未运行";
            Trigger crontrigger = triggers.get(0);
            Trigger.TriggerState str = instance().session.getScheduler().getTriggerState(crontrigger.getKey());
            return str.toString();

        }catch (Exception e){
            return null;
        }
    }

    /// <summary>
    /// 添加自动运行任务
    /// </summary>
    /// <param name="job"></param>
    /// <returns></returns>
    public static AjaxResult addAutoRunJob(ScheduleJob job){
        if (0==job.getEnable()){
            if (Maths.isBitEq1(job.getWatchoption(),SchedulerWatchOption.Begin)||Maths.isBitEq1(job.getWatchoption(),SchedulerWatchOption.Create))
                Logs.writeLog("任务计划状态被禁用"+ System.lineSeparator()  + Json.toJson(job) + System.lineSeparator() , "ScheduleJob_Run");
            return new AjaxResult(200, "任务未运行");
        }
        if (job.getEndtime()!=null && job.getEndtime().getTime() < new Date().getTime()){
            if (Maths.isBitEq1(job.getWatchoption(),SchedulerWatchOption.Begin)|| Maths.isBitEq1(job.getWatchoption(),SchedulerWatchOption.Create))
                Logs.writeLog("结束时间过早未运行"+ System.lineSeparator() + Json.toJson(job) +System.lineSeparator(), "ScheduleJob_Run");
            return new AjaxResult(200, "结束时间过早未运行");
        }
        if (Strings.hasValue(job.getEventype()) && Strings.isBlank(job.getCronexpress())){
            switch (job.getEventype())
            {
                case "year":
                    job.setCronexpress( "1 1 1 1 1/" + job.getEvenvalue());
                    break;
                case "month":
                    job.setCronexpress(  "1 1 1 1/" + job.getEvenvalue());
                    break;
                case "day":
                    job.setCronexpress ( "1 1 0 1/" + job.getEvenvalue() + " * ? *");
                    break;
                case "week":
                    job.setCronexpress(  "1 1 0 ? ? " + job.getEvenvalue() + " *");
                    break;
                case "hour":
                    job.setCronexpress( "1 1 1/" + job.getEvenvalue() + " * * ? *");
                    break;
                case "minute":
                    job.setCronexpress ( "1 1/" + job.getEvenvalue() + " * * * ? *");
                    break;
                case "second":
                    job.setCronexpress("1/" + job.getEvenvalue() + " * * * * ? *");
                    break;
            }
        }
        if (Maths.isBitEq1(job.getWatchoption(),SchedulerWatchOption.Create)){
            Logs.writeLog("任务名称:"+job.getJobname()+" 任务分组:"+job.getGroupname()+System.lineSeparator()+
                    "时间表达式:"+job.getCronexpress()+"操作类型:"+job.getType()+System.lineSeparator() +
                    "命令代码:"+job.getCmdcode(), "ScheduleJob_Create");
        }

        Tuple2<Boolean, String> result = instance().session.createScheduleJob(job, (x) ->{
            try {
                x.setLasttime(Times.now());
                if(instance().onSchedule!=null){
                    Receipt ret = instance().onSchedule.invoke(x);
                    if(!ret.isSuccess()){
                        if(instance().onScheduleResult!=null){
                            instance().onScheduleResult.callback(job,"ScheduleSkip",ret.message);
                        }
                        Logs.writeLog("任务名称:" + x.getJobname() + System.lineSeparator() +
                                "任务分组:" + x.getGroupname() + System.lineSeparator() +
                                "Cron表达式:" + x.getCronexpress() + System.lineSeparator()+
                                "跳出原因:" + ret.message+ System.lineSeparator(), "ScheduleJob_onSchedule");
                        return;
                    }
                }
                if(!job.canDay()){
                    String msg = Times.getDate()+" in("+job.getExclude()+")";
                    if(instance().onScheduleResult!=null){
                        instance().onScheduleResult.callback(job,"ScheduleSkip",msg);
                    }
                    Logs.writeLog("任务名称:" + x.getJobname() + System.lineSeparator() +
                            "任务分组:" + x.getGroupname() + System.lineSeparator() +
                            "Cron表达式:" + x.getCronexpress() + System.lineSeparator()+
                            "跳出原因:" + msg + System.lineSeparator(), "ScheduleJob_onSchedule");
                    return;
                }
                if (Maths.isBitEq1(job.getWatchoption(),SchedulerWatchOption.Begin))
                    Logs.writeLog("任务名称:" + x.getJobname() + System.lineSeparator() +
                            "任务分组:" + x.getGroupname() + System.lineSeparator() +
                            "Cron表达式:" + x.getCronexpress() + System.lineSeparator()+
                            "执行命令:" + x.getCmdcode() + System.lineSeparator(), "ScheduleJob_Run");
                if (x.getEnable() != 1) {
                    Logs.writeLog("任务计划:\n" + x.getJobname() + "状态为"+x.getEnable()+",返回处理\n", "ScheduleJob_Enable");
                    return;
                }
                Object ret = null;
                String cmd = x.getCmdcode();
                String type = x.getType();
                String error = "";
                //如果type是整数,为类库的方案
                if (Strings.hasValue(type) && Regex.isMatch(type, "-?\\d")) {
                    Record config = Record.parse(cmd);
                    if (Regex.isMatch(type, "\\d?")) {
                        switch (DbSqlDataType.get(Convert.toInt(type))) {
                            case Program: {
                                String className = config.getString("class,type,function,tablename", true);
                                if (Strings.isBlank(className)) break;
                                String methodName = config.getString("method,function", true);
                                if (Strings.isBlank(className)) break;
                                if (Strings.isBlank(methodName)) {
                                    methodName = Collections.last(className.split("."));
                                    className = Strings.rightLess(className,methodName.length() + 1);
                                }
                                String ps = Strings.mapping(config.getString("params,bean"));
                                Record methodRecord = Core.or(Record.parse(ps),new Record());
                                String path = config.getString("path", true);
//                                Type pclass = null;
//                                if (path.HasValue()) {
//                                    className = className.Replace("|", ".");
//                                    if (path.Contains("~")) path = path.Replace("~", Configs.BasicPath());
//                                    if (!path.Contains("/") && !path.Contains("\\")) {
//                                        path = Configs.BasicPath() + "/bin/" + path;
//                                        path = Path.GetFullPath(path);
//                                        if (!Files.IsExistFile(path))
//                                            throw new Exception("dll文件[" + path + "]不存在或者内容为空");
//                                        var assembly = Assembly.LoadFile(path);
//                                        pclass = assembly.GetType(className);
//                                    } else {
//                                        path = Path.GetFullPath(path);
//                                        byte[] btyes = Files.GetFileBytes(path); //驱动文件的字节集
//                                        if (btyes == null) throw new Exception("dll文件[" + path + "]不存在或者内容为空");
//                                        var assembly = Assembly.Load(btyes);
//                                        pclass = assembly.GetType(className);
//                                    }
//                                } else {
//                                    var cp = className.Split('.')[0];
//                                    if (className.IndexOf("|", StringComparison.Ordinal) > 0) {
//                                        cp = className.Split('|')[0];
//                                        className = className.Replace("|", ".");
//                                    }
//                                    var assembly = Assembly.Load(cp);
//                                    pclass = assembly.GetType(className);
//                                }
//                                if (pclass == null) throw new Exception("类[" + className + "]不存在");
//                                var method = pclass.GetMethod(methodName,
//                                        BindingFlags.Instance | BindingFlags.NonPublic | BindingFlags.Public |
//                                                BindingFlags.Static);
//                                if (method == null) break;
//                                var instance = Activator.CreateInstance(pclass, true);
//                                ret = method.Invoke(instance, ArgumentMapping.MappingMethodParam(method, methodRecord));
                            }
                            break;

                            case  Http:
                                ret = Regex.isMatch(config.getString("method,type"), "post")
                                        ? HttpHelper.post(config.getString("url,class,type,function,tablename", true),
                                        Record.parse(Strings.mapping(config.getString("params,bean"))))
                                        : HttpHelper.get(config.getString("url,class,type,function,tablename", true),
                                        Record.parse(Strings.mapping(config.getString("params,bean"))));
                                break;
                            case Querys:
                                ret = QueryAction.querys(cmd);
                                break;
                            case SaveBean:
                                ret = SaveAction.saveBean(config.getString("tableName", true), config.getString("bean"), config.getString("condition"), config.getInt("option"),null,true);
                                break;
                            case Insert:
                                ret = SaveAction.insert(config.getString("tableName", true), config.getString("bean"));
                                break;
                            case Update:
                                ret = SaveAction.update(config.getString("tableName", true), config.getString("bean"), config.getString("condition"));
                                break;
                            case Delete:
                                ret = SaveAction.delete(config.getString("tableName", true), config.getString("condition"));
                                break;
                            case Save:
                                ret = SaveAction.save(config.getString("tableName", true), config.getString("bean"), config.getString("condition"));
                                break;
                            case SaveBeans:
                                ret = SaveAction.saveBeans(cmd);
                                break;
                            case  GetData:
                                ret = QueryAction.getData(config.getString("tableName", true),
                                        config.getString("condition"),
                                        config.getString("columns"), config.getString("param"));
                                break;
                            case GetBean:
                                ret = QueryAction.getBean(config.getString("tableName", true),
                                        config.getString("condition"), config.getString("param"),
                                        config.getString("columns"));
                                break;
                            case GetValue:
                                ret = QueryAction.getValue(config.getString("tableName", true),
                                        config.getString("columns"),
                                        config.getString("condition"), config.getString("param"), null);
                                break;
                            case Service:
                                if (Strings.isJson(cmd)) {
                                    ret = ProxyService.execute(config.getString("key"), config);
                                } else {
                                    if (Strings.hasValue(cmd))
                                        ret = ProxyService.execute(cmd,new Record());
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    //SaveAction.Insert("scheduleresult", new Record("result", Json.Json.ToJson(ret)).Put("jobid", x.Id));
                    //                    AppConfig.RunSizes.Put(x.Id, AppConfig.RunSizes[x.Id] - 1);
                }
                //文本型 为外部方案
                else {
                    if (Strings.isBlank(type)) type = "httppost";
                    switch (type.toLowerCase()) {
                        case "task":
                        case "exe":
                        case "jar":
                        {
                            String command = "java -jar "+Strings.mapping(cmd);
                            String line = null;
                            StringBuilder sb = new StringBuilder();
                            Runtime runtime = Runtime.getRuntime();
                            try {
                                Process process = runtime.exec(command);
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                while ((line = bufferedReader.readLine()) != null) {
                                    sb.append(line + "\n");
                                }
                                ret=sb.toString();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                            break;
                        case "httppost":
                        case "post": {
                            for (int i = 0; i < 5; i++) {
                                Receipt<String> r = HttpHelper.request(Strings.mapping(cmd), Strings.mapping(job.getData()), "post", null);
                                if (!r.isSuccess() && r.message.contains("基础连接已经关闭")) {
                                    r = HttpHelper.request(Strings.mapping(cmd), Strings.mapping(job.getData()), "post", null);
                                }
                                if (!r.isSuccess() && r.message.contains("(502) 错误的网关")) {
                                    r = HttpHelper.request(Strings.mapping(cmd), Strings.mapping(job.getData()), "post", null);
                                }
                                if (r.result) {
                                    ret = r.getData();
                                    break;
                                } else error = r.message;
                            }
                        }
                        break;
                        case "httpget":
                        case "get": {
                            for (int i = 0; i < 5; i++) {
                                Receipt<String> r = HttpHelper.request(Strings.mapping(cmd), Strings.mapping(job.getData()), "get");
                                if (!r.isSuccess() && r.message.contains("基础连接已经关闭")) {
                                    r = HttpHelper.request(Strings.mapping(cmd), Strings.mapping(job.getData()), "get");;
                                }
                                if (!r.isSuccess() && r.message.contains("(502) 错误的网关")) {
                                    r = HttpHelper.request(Strings.mapping(cmd), Strings.mapping(job.getData()), "get");;
                                }

                                if (r.result) {
                                    ret = r.getData();
                                    break;
                                } else error = r.message;
                            }
                        }
                        break;
                        case "postjson":
                        case "json":
                        {
                            for (int i = 0; i < 5; i++) {
                                Receipt<String>  r = HttpHelper.requestByJson(Strings.mapping(cmd), Strings.mapping(job.getData()),null);
                                if (!r.isSuccess() && r.message.contains("基础连接已经关闭")) {
                                    r =HttpHelper.requestByJson(Strings.mapping(cmd), Strings.mapping(job.getData()),null);
                                }
                                if (!r.isSuccess() && r.message.contains("(502) 错误的网关")) {
                                    r = HttpHelper.requestByJson(Strings.mapping(cmd), Strings.mapping(job.getData()),null);
                                }

                                if (r.result) {
                                    ret = r.getData();
                                    break;
                                } else error = r.message;
                            }
                        }
                        break;
                        case "script":
                            ret = Script.execute(cmd);
                            break;
                        case "mq":
                            ret = RabbitHelper.send(Json.toObject(cmd, RabbitConfig.class));
                            break;
                    }
                }
                x.increaseRunTimes();
                //限制运行次数
                if (x.getLimittimes() > -100) x.decreaseLimitTimes();
                if (x.getLimittimes() > -100 && x.getLimittimes() < 0) {
                    x.setEnable(0);
                    instance().session.deleteScheduleJob(x.getJobname(),x.getGroupname());
                }
                //提前结束
                if (Strings.hasValue(x.getBreakrule())) {
                    if (Script.eval(Strings.mapping(x.getBreakrule(),Record.parse(ret)),Boolean.class))
                    {
                        x.setEnable(0);
                        instance().session.deleteScheduleJob(x.getJobname(),x.getGroupname());
                    }
                }

                //如果任务的是单次运行使能停止
                if ((Strings.isBlank(x.getCronexpress()) && Strings.isBlank(x.getEventype())) || Strings.isBlank(x.getState()) || "0".equals(x.getState())) {
                    //                    var dao = new Dao(dm.Conn);
                    //                    sj.Enable = 0;
                    //                    sj.Update(dao);
                    //                    dao.Close();
                }
                if (x.getEnable() == 0) {
                    instance().session.deleteScheduleJob(x.getJobname(),x.getGroupname());
                }

                if (instance().onScheduleResult != null)
                {
                    instance().onScheduleResult.callback(job, ret, error);
                }
                else
                {
                    if (Maths.isBitEq1(job.getWatchoption(),SchedulerWatchOption.Result)) {
                        Logs.writeLog("任务名称" + job.getJobname() + System.lineSeparator() +
                                "任务分组:" + job.getGroupname() + System.lineSeparator() +
                                "运行结果:" + Json.toJson(ret) +  System.lineSeparator() +
                                "错误信息:" + error, "ScheduleJob_Result");
                    }
                }
            } catch (Exception e) {
                Logs.writeLog("计划任务名称:" +x.getJobname()+System.lineSeparator()+
                        "计划任务分组:{x.GroupName}" +x.getGroupname()+System.lineSeparator()+
                        "错误信息:" +e.getMessage()+System.lineSeparator()+
                        "堆栈信息:{e.StackTrace}", "ScheduleJob_Error");
                instance().onScheduleResult.callback(job, null, e.getMessage());
            }
        });
        startAllScheduler();
        return new AjaxResult(result.item1).setData(result.item2);
    }
    /// <summary>
    /// 添加自动运行任务
    /// </summary>
    /// <param name="job">任务信息</param>
    /// <param name="func">回调委托</param>
    /// <returns></returns>
    public static Receipt addAutoRunJob(ScheduleJob job, Func2<ScheduleJob, Boolean> func) {
        if (func == null) return addAutoRunJob(job).toReceipt();
        if (job.getEnable() == 0) return new Receipt(false, "任务未运行");
        if (job.getEndtime()!=null && job.getEndtime().getTime() < new Date().getTime()) return new Receipt(false, "结束时间过早未运行");
        if (Strings.hasValue(job.getEventype()) && Strings.isBlank(job.getCronexpress())){
            switch (job.getEventype().toLowerCase()){
                case "year":
                case "1":
                    job.setCronexpress( "1 1 1 1 1/" + job.getEvenvalue());
                    break;
                case "month":
                case "2":
                    job.setCronexpress ( "1 1 1 1/" + job.getEvenvalue());
                    break;
                case "day":
                case "3":
                    job.setCronexpress ("1 1 0 1/" + job.getEvenvalue() + " * ? *" );
                    break;
                case "week":
                case "4":
                    job.setCronexpress ("1 1 0 ? ? " + job.getEvenvalue() + " *");
                    break;
                case "hour":
                case "5":
                    job.setCronexpress ("1 1 1/" + job.getEvenvalue() + " * * ? *");
                    break;
                case "minute":
                case "6":
                    job.setCronexpress("1 1/" + job.getEvenvalue() + " * * * ? *") ;
                    break;
                case "second":
                case "":
                case "0":
                    job.setCronexpress( "1/" + job.getEvenvalue() + " * * * * ? *" );
                    break;
                default:
                    job.setCronexpress ("1/" + job.getEvenvalue() + " * * * * ? *");
                    break;
            }
        }
        Tuple2<Boolean, String> result = instance().session.createScheduleJob(job, func);
        startAllScheduler();
        return result.item1 ? new Receipt() : new Receipt(false, result.item2);
    }


    /// <summary>
    /// 添加自动完成任务
    /// </summary>
    /// <param name="startTime"></param>
    /// <param name="action"></param>
    /// <param name="jobGroup"></param>
    /// <returns></returns>
    public static Receipt addAutoRunJob(Date startTime, Action0 action, String jobGroup) {
        try
        {
            JobDetail job = Strings.isBlank(jobGroup) ? JobBuilder.newJob(BaseJob.class).build() : JobBuilder.newJob(BaseJob.class).withIdentity(jobGroup, jobGroup).build();
            job.getJobDataMap().put("type", 0);
            job.getJobDataMap().put("fun", action);
            Trigger trigger = TriggerBuilder.newTrigger().startAt(startTime).build();
            instance().session.getScheduler().scheduleJob(job, trigger);
            startAllScheduler();
            return new Receipt();
        }
        catch (Exception e)
        {
            return new Receipt(false,e.getMessage());
        }
    }
    /// <summary>
    /// 添加自动运行工作
    /// </summary>
    /// <param name="cron">cron表达式</param>
    /// <param name="func">回调委托，如果返回值为false,取消任务下次运行</param>
    /// <returns></returns>
    public static Receipt addAutoRunJob(String cron, Func1<Boolean> func)
    {
        try
        {
            JobDetail job = JobBuilder.newJob(BaseJob.class).build();
            job.getJobDataMap().put("type", 3);
            job.getJobDataMap().put("fun", func);
            CronTrigger trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
            instance().session.getScheduler().scheduleJob(job, trigger);
            startAllScheduler();
            return new Receipt();
        }
        catch (Exception e)
        {
            return new Receipt(false,e.getMessage());
        }
    }

    /// <summary>
    /// 间隔N秒执行
    /// </summary>
    /// <param name="second">秒 1-59之间</param>
    /// <param name="func">回调委托，如果返回值为false,取消任务下次运行</param>
    /// <returns></returns>
    public static Receipt addAutoRunJob(int second, Func1<Boolean> func)
    {
        return addAutoRunJob(EvenType.Second, second, func);


    }
    /// <summary>
    /// 创建周期自动运行任务
    /// </summary>
    /// <param name="eventype">周期类型</param>
    /// <param name="num">间隔周期数量,不能超出周期的容量，比如second必须在1-59之间</param>
    /// <param name="func">回调委托，如果返回值为false,取消任务下次运行</param>
    /// <returns></returns>

    public static Receipt addAutoRunJob(EvenType eventype, int num, Func1<Boolean> func)
    {
        String cron = null;
        switch (eventype)
        {
            case Year:
                cron = "1 1 1 1 1/" + num;
                break;
            case Month:
                cron = "1 1 1 1/" + num;
                break;
            case Day:
                cron = "1 1 0 1/" + num + " * ? *";
                break;
            case Week:
                cron = "1 1 0 ? ? " + num + " *";
                break;
            case Hour:
                cron = "1 1 1/" + num + " * * ? *";
                break;
            case Minute:
                cron = "1 1/" + num + " * * * ? *";
                break;
            case Second:
                cron = "1/" + num + " * * * * ? *";
                break;
        }
        if (Strings.isBlank(cron)) return new Receipt(false, "时间表达式为空");
        return addAutoRunJob(cron, func);
    }

    /// <summary>
    /// 添加自动运行的任务调度
    /// </summary>
    /// <param name="bean">任务信息</param>
    /// <returns></returns>
    public static AjaxResult addAutoRunJob(Record bean)
    {
        if (bean == null) return new AjaxResult(false);
        ScheduleJob job = bean.toClass(ScheduleJob.class);
        return addAutoRunJob(job);
    }


    /// <summary>
    /// 扫码本地程序的任务
    /// </summary>
    public static void initFormLoction()
    {
        throw Core.makeThrow("未实现[626]");
//        try
//        {
//            Assembly asm = Assembly.GetExecutingAssembly();
//            Type[] types = asm.GetTypes();
//            IDictionary<int, Type> typeMap = new Dictionary<int, Type>();
//            int counter = 1;
//            List<Type> typeList = new List<Type>();
//            foreach (Type t in types)
//            {
//                if (new List<Type>(t.GetInterfaces()).Contains(typeof(ITask)))
//                {
//                    typeList.Add(t);
//                }
//            }
//            typeList.Sort();
//            foreach (var t in typeList)
//            {
//                typeMap.Add(counter++, t);
//            }
//            foreach (var key in typeMap.Keys)
//            {
//                var eType = typeMap[key];
//                var example = ObjectUtils.InstantiateType<ITask>(eType);
//                example.Run();
//            }
//            //启动全部调度
//            StartAllScheduler();
//        }
//        catch (Exception e)
//        {
//            // ignored
//        }
    }

    /// <summary>
    /// 任务服务初始化启动器
    /// </summary>
    /// <returns></returns>
    public int init()
    {
        AtomicInteger count = new AtomicInteger();
        Record cnd = new Record("enable", 1).put("endtime:>", Times.now());
        List<Record> rs = QueryAction.getData("schedulejob", cnd.toString(), null, null);
        if (!Rs.isBlank(rs))
        {
            rs.forEach(x ->{
                if (addAutoRunJob(x).success) count.getAndIncrement();
            });
        }
        return count.get();
    }



}
