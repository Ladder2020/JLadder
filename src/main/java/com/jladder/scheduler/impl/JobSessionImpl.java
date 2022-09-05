package com.jladder.scheduler.impl;

import com.jladder.db.Rs;
import com.jladder.lang.Collections;
import com.jladder.lang.Strings;
import com.jladder.lang.func.*;
import com.jladder.scheduler.BaseJob;
import com.jladder.scheduler.JobSession;
import com.jladder.scheduler.ScheduleJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.KeyMatcher;

import java.util.*;

public class JobSessionImpl extends JobSession {
    private Scheduler scheduler;
    public JobSessionImpl(){
        try {
            this.scheduler=(new StdSchedulerFactory()).getScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized Scheduler getScheduler() {
        if(scheduler!=null)return scheduler;
        try {
            this.scheduler=(new StdSchedulerFactory()).getScheduler();
            return scheduler;
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
   public void startScheduler(){
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pauseAll() {
        try {
            scheduler.pauseAll();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resumeAll() {
        try {
            scheduler.resumeAll();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdownScheduler() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdownScheduler(boolean waitForJobsToComplete) {
        try {
            scheduler.shutdown(waitForJobsToComplete);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getAllGroupName() {
        try {
            return scheduler.getJobGroupNames();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> getAllTriggerGroupNames() {
        try {
            return scheduler.getTriggerGroupNames();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ScheduleJob> getAllJobDetail(String groupName) {
        Set<JobKey> jobkeys = null;
        try {
            jobkeys = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName));
            List<ScheduleJob> jobDetail = new ArrayList<ScheduleJob>();
            for (JobKey jk : jobkeys)
            {
                JobDetail job = scheduler.getJobDetail(jk);
                ScheduleJob sd  = Collections.toClass(job.getJobDataMap(),ScheduleJob.class);
                ScheduleJob raw = (ScheduleJob)job.getJobDataMap().get("__ScheduleJob__");
                if(raw!=null)sd = raw;
                CronTrigger tri = getTrigger(jk.getName(),jk.getGroup());
                if (tri!=null)
                {
                    sd.setStarttime(tri.getStartTime());
                    if (tri.getEndTime() != null)
                    {
                        sd.setEndtime(tri.getEndTime());
                    }
                    sd.setCronexpress(tri.getCronExpression());
                    sd.setState(transFromEnum(scheduler.getTriggerState(tri.getKey()))) ;
                }
                else
                {
//                    switch (job.Status)
//                    {
//                        case TaskStatus.RanToCompletion:
//                            sd.State = "执行成功";
//                            break;
//                        case TaskStatus.Canceled:
//                            sd.State = "取消";
//                            break;
//                        case TaskStatus.Created:
//                            sd.State = "创建未执行";
//                            break;
//                        case TaskStatus.Running:
//                            sd.State = "正在运行";
//                            break;
//                        case TaskStatus.WaitingToRun:
//                            sd.State = "等待运行";
//                            break;
//                        case TaskStatus.WaitingForActivation:
//                            sd.State = "等待激活";
//                            break;
//                        case TaskStatus.WaitingForChildrenToComplete:
//                            sd.State = "等待子任务";
//                            break;
//                        case TaskStatus.Faulted:
//                            sd.State = "失败";
//                            break;
//                    }

                }
                sd.setGroupname(job.getKey().getGroup());
                sd.setJobname(job.getKey().getName());
                jobDetail.add(sd);
            }
            return jobDetail;
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<CronTrigger> getAllTrigger(String triggerName) {
        try{
            Set<TriggerKey> triggers = scheduler.getTriggerKeys(GroupMatcher.groupEquals(triggerName));

            List<CronTrigger> triigerDetail = new ArrayList<CronTrigger>();
            for (TriggerKey tk : triggers)
            {
                triigerDetail.add((CronTrigger) scheduler.getTrigger(tk));
            }
            return triigerDetail;
        }catch (Exception e){
            return null;
        }

    }

    @Override
    public List<ScheduleJob> getAllSchedule() {
        List<ScheduleJob> jobs = new ArrayList<ScheduleJob>();
        for (String name : getAllGroupName())
        {
            List<ScheduleJob> schedules = getAllJobDetail(name);
            jobs.addAll(schedules);
        }
        return jobs;
    }

    @Override
    public JobDetail getScheduleJob(String jobName, String groupName) {
        JobKey jobKey = new JobKey(jobName, groupName);
        try {
            return scheduler.getJobDetail(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CronTrigger getTrigger(String jobName, String groupName) {
        JobKey jobKey = new JobKey(jobName, groupName);
        try {
            List<? extends Trigger> ts = scheduler.getTriggersOfJob(jobKey);
            if(!Rs.isBlank(ts))return (CronTrigger) ts.get(0);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public boolean deleteScheduleJob(String jobName, String groupName) {
        JobKey jobKey = new JobKey(jobName, groupName);
        try {
            return scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void pauseScheduleJob(String jobName, String groupName) {
        JobKey jobKey = new JobKey(jobName, groupName);
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resumeScheduleJob(String groupName, String jobName) {
        JobKey jobKey = new JobKey(jobName, groupName);
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tuple2<Boolean, String> createScheduleJob(Date startTime, Action0 action) {
        try
        {
            JobDetail job = JobBuilder.newJob(BaseJob.class).build();
            job.getJobDataMap().put("type", 0);
            job.getJobDataMap().put("fun", action);
            Trigger trigger = TriggerBuilder.newTrigger().startAt(startTime).build();
            scheduler.scheduleJob(job, trigger);
            return new Tuple2<Boolean, String>(true,"");
        }
        catch (Exception e)
        {
            return new Tuple2<Boolean, String>(false,e.getMessage());
        }
    }

    @Override
    public Tuple2<Boolean, String> createScheduleJob(ScheduleJob schedule, Func2<ScheduleJob, Boolean> fun) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("fun",fun);
        param.put("type",5);
        return createScheduleJob(schedule, param,BaseJob.class);
    }

    @Override
    public Tuple2<Boolean, String> createScheduleJobWithContext(ScheduleJob schedule, Func2<JobExecutionContext, Boolean> fun) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("fun", fun);
        param.put("type", 4);
        return createScheduleJob(schedule, param,BaseJob.class);
    }

    @Override
    public Tuple2<Boolean, String> createScheduleJob(ScheduleJob schedule, Func1<Boolean> fun) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("fun", fun);
        param.put("type", 3);
        return createScheduleJob(schedule, param,BaseJob.class);
    }

    @Override
    public Tuple2<Boolean, String> createScheduleJobWithContext(ScheduleJob schedule, Action1<JobExecutionContext> action) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("fun", action);
        param.put("type", 2);
        return createScheduleJob(schedule, param,BaseJob.class);
    }

    @Override
    public Tuple2<Boolean, String> createScheduleJob(ScheduleJob schedule, Action1<ScheduleJob> action) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("fun", action);
        param.put("type", 1);
        return createScheduleJob(schedule, param,BaseJob.class);
    }

    @Override
    public <T extends Job> Tuple2<Boolean, String> createScheduleJob(ScheduleJob schedu, Map<String, Object> param,Class<T> glass) {
        try
        {
            deleteScheduleJob(schedu.getJobname(), schedu.getGroupname());//先删除任务
            //创建出来一个具体的作业
            JobDetail job = JobBuilder.newJob(glass).withIdentity(schedu.getJobname(), schedu.getGroupname()).build();
            //处理一些变量的存储
            if (param != null)
            {
                param.put("id", schedu.getId());
                param.put("option", schedu.getType());
                param.put("__ScheduleJob__", schedu);
                job.getJobDataMap().putAll(param);
            }
            //创建并配置一个触发器
            TriggerBuilder<Trigger> triggercreater = TriggerBuilder.newTrigger();
            if (schedu.getStarttime() !=null && schedu.getStarttime().getTime()>0) triggercreater.startAt(schedu.getStarttime());
            if (schedu.getEndtime()!=null && schedu.getEndtime().getTime()>0) triggercreater.endAt(schedu.getEndtime());
            if (Strings.hasValue(schedu.getCronexpress())) {
                triggercreater.withSchedule(CronScheduleBuilder.cronSchedule(schedu.getCronexpress()).withMisfireHandlingInstructionDoNothing());
            }
            Trigger trigger = triggercreater.build();
            //                  (string.IsNullOrEmpty(schedu.CronExpress)
            //                            ? "* * * * * ? 2000-2001"
            //                            : )
            //                        .Build();
            //加入作业调度池中
            scheduler.scheduleJob(job, trigger);

//                    51 46 14 29 11 ? 2016
            //默认任务是暂停的
            //ResumeScheduleJob(schedu.GroupName, schedu.JobName);
                    /*
             * Cron表达式
                            quartz中的cron表达式和Linux下的很类似，比如 "/5 * * ? * * *"  这样的7位表达式，最后一位年非必选。
                            表达式从左到右，依此是秒、 分、时、月第几天、月、周几、年。下面表格是要遵守的规范：
                            字段名	允许的值	允许的特殊字符
                            Seconds	0-59	, - * /
                            Minutes	0-59	, - * /
                            Hours	0-23	, - * /
                            Day of month	1-31	, - * ? / L W
                            Month	1-12 or JAN-DEC	, - * /
                            Day of week	1-7 or SUN-SAT	, - * ? / L #
                            Year	空, 1970-2099	, - * /

                             
                            特殊字符	解释
                            ,	或的意思。例：分钟位 5,10  即第5分钟或10分都触发。 
                            /	a/b。 a：代表起始时间，b频率时间。 例； 分钟位  3/5，  从第三分钟开始，每5分钟执行一次。
                            *	频率。 即每一次波动。    例；分钟位 *  即表示每分钟 
                            -	区间。  例： 分钟位   5-10 即5到10分期间。 
                            ?	任意值 。   即每一次波动。只能用在DayofMonth和DayofWeek，二者冲突。指定一个另一个一个要用?
                            L	表示最后。 只能用在DayofMonth和DayofWeek，4L即最后一个星期三
                            W	工作日。  表示最后。 只能用在DayofWeek
                            #	4#2。 只能用DayofMonth。 某月的第二个星期三  



                            实例介绍

                            ”0 0 10,14,16 * * ?"    每天10点，14点，16点 触发。

                            "0 0/5 14,18 * * ?"    每天14点或18点中，每5分钟触发 。

                            "0 4/15 14-18 * * ?"       每天14点到18点期间,  从第四分钟触发，每15分钟一次。

                             "0 15 10 ? * 6L"        每月的最后一个星期五上午10:15触发。

             */
            return new Tuple2<Boolean, String>(true,"");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return new Tuple2<Boolean, String>(false,e.getMessage());
        }
    }

    @Override
    public boolean isExistsSchedule(String jobName, String groupName) {
        JobDetail job = getScheduleJob(groupName, jobName);
        if (job == null)
            return false;
        return true;
    }

    @Override
    public String transFromEnum(Trigger.TriggerState ts) {
        switch (ts)
        {
            case NORMAL:
                return "等待";
            case PAUSED:
                return "暂停";
            case COMPLETE:
                return "完成";
            case ERROR:
                return "执行出错";
            case BLOCKED:
                return "阻塞";
            case NONE:
                return "无";
            default:
                return "无";
        }
    }

    @Override
    public String getJobState(int ts) {
        switch (ts)
        {
            case 0:
                return "未执行";
            case 1:
                return "执行中";
            case 2:
                return "已完成";
            case 3:
                return "执行异常";
            default:
                return "无";
        }
    }

    @Override
    public <T extends JobListener> void addListener(String jobName, String groupName, T listener) {
        JobKey jobKey = new JobKey(jobName, groupName);

        KeyMatcher<JobKey> matcher = KeyMatcher.keyEquals(jobKey);
        try {
            this.scheduler.getListenerManager().addJobListener(listener,matcher);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifyScheduler(ScheduleJob schedu) {
        JobKey jobKey = new JobKey(schedu.getJobname(), schedu.getGroupname());
        try {
            //创建并配置一个触发器
            CronTrigger trigger = (CronTrigger) TriggerBuilder.newTrigger().startAt(schedu.getStarttime()).endAt(schedu.getEndtime())
                    .withSchedule(CronScheduleBuilder.cronSchedule(Strings.isBlank(schedu.getCronexpress())
                            ? "* * * * * ? 2000-2001"
                            : schedu.getCronexpress()) )
                    .build();
            List<? extends Trigger> oldtri = scheduler.getTriggersOfJob(jobKey);
            if(oldtri!=null&&oldtri.size()>0){
                scheduler.rescheduleJob(oldtri.get(0).getKey(), trigger);
                pauseScheduleJob(schedu.getGroupname(), schedu.getJobname());
            }

        }catch (Exception e){

        }

    }


}
