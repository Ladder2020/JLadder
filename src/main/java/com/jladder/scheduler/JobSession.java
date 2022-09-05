package com.jladder.scheduler;
import com.jladder.lang.func.*;
import org.quartz.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
public abstract class JobSession {
   public abstract Scheduler getScheduler();

    /// <summary>
    /// 启动调度
    /// </summary>
   public abstract void startScheduler();

    /// <summary>
    ///     暂停全部调度
    /// </summary>
 public abstract void pauseAll();

    /// <summary>
    ///     恢复全部调度
    /// </summary>
   public abstract void resumeAll();

    /// <summary>
    ///     强制关闭调度
    /// </summary>
    public abstract void shutdownScheduler();

    /// <summary>
    ///     任务完成后关闭任务
    /// </summary>
    /// <param name="waitForJobsToComplete"></param>
    public abstract void shutdownScheduler(boolean waitForJobsToComplete);

    /// <summary>
    ///     获取全部任务组名称
    /// </summary>
    /// <returns></returns>
    public abstract List<String> getAllGroupName();

    /// <summary>
    ///     获取全部触发器名称
    /// </summary>
    /// <returns></returns>
    public abstract List<String> getAllTriggerGroupNames();

    /// <summary>
    ///     获取任务组下全部调度
    /// </summary>
    /// <param name="groupName"></param>
    /// <returns></returns>
    public abstract List<ScheduleJob> getAllJobDetail(String groupName);

    /// <summary>
    ///     获取触发器组下全部触发器
    /// </summary>
    /// <param name="triggerName"></param>
    /// <returns></returns>
    public abstract List<CronTrigger> getAllTrigger(String triggerName);
    /// <summary>
    /// 获取全部任务
    /// </summary>
    /// <returns></returns>
    public abstract List<ScheduleJob> getAllSchedule();

    /// <summary>
    /// 获取调度任务
    /// </summary>
    /// <param name="groupName">分组名称</param>
    /// <param name="jobName">任务名称</param>
    /// <returns></returns>
    public abstract JobDetail getScheduleJob(String jobName,String groupName);
    /// <summary>
    /// 获取触发器
    /// </summary>
    /// <param name="groupName"></param>
    /// <param name="jobName"></param>
    /// <returns></returns>
    public abstract CronTrigger getTrigger(String jobName,String groupName);

    /// <summary>
    ///  删除任务
    /// </summary>
    /// <param name="jobName">任务名称</param>
    /// <param name="groupName">分组名称</param>
    /// <returns></returns>
    public abstract boolean deleteScheduleJob(String jobName,String groupName);

    /// <summary>
    /// 暂停任务
    /// </summary>
    /// <param name="groupName">分组名</param>
    /// <param name="jobName">任务名称</param>
    public abstract void pauseScheduleJob(String jobName,String groupName);

    /// <summary>
    ///  恢复任务
    /// </summary>
    /// <param name="jobName">任务名称</param>
    /// <param name="groupName">分组名</param>
    public abstract void resumeScheduleJob(String groupName, String jobName);

    /// <summary>
    /// 创建任务调度
    /// </summary>
    /// <param name="startTime">开始时间</param>
    /// <param name="action">执行过程</param>
    /// <returns></returns>
    public abstract Tuple2<Boolean,String> createScheduleJob(Date startTime, Action0 action);
    /// <summary>
    /// 创建任务调度
    /// </summary>
    /// <param name="schedu">任务信息</param>
    /// <param name="fun">执行过程</param>
    /// <returns></returns>

   public abstract Tuple2<Boolean, String> createScheduleJob(ScheduleJob schedu, Func2<ScheduleJob, Boolean> fun);

    /// <summary>
    /// 创建任务调度
    /// </summary>
    /// <param name="schedu">任务信息</param>
    /// <param name="fun">执行过程</param>
    /// <returns></returns>
    public abstract Tuple2<Boolean, String> createScheduleJobWithContext(ScheduleJob schedu, Func2<JobExecutionContext, Boolean> fun);

    /// <summary>
    /// 创建任务调度
    /// </summary>
    /// <param name="schedu">任务信息</param>
    /// <param name="fun">执行过程</param>
    /// <returns></returns>
    public abstract Tuple2<Boolean, String> createScheduleJob(ScheduleJob schedu, Func1<Boolean> fun);

    /// <summary>
    /// 创建任务调度
    /// </summary>
    /// <param name="schedu">任务信息</param>
    /// <param name="action">执行过程</param>
    /// <returns></returns>
    public abstract Tuple2<Boolean, String> createScheduleJobWithContext(ScheduleJob schedu, Action1<JobExecutionContext> action);

    /// <summary>
    /// 创建任务调度
    /// </summary>
    /// <param name="schedu">任务信息</param>
    /// <param name="action">执行过程</param>
    /// <returns></returns>
    public abstract Tuple2<Boolean, String> createScheduleJob(ScheduleJob schedu, Action1<ScheduleJob> action);

    /// <summary>
    /// 创建任务
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="schedu"></param>
    /// <param name="param"></param>
    /// <returns></returns>
    public abstract <T extends Job> Tuple2<Boolean, String> createScheduleJob(ScheduleJob schedu, Map<String, Object> param,Class<T> glass);

    /// <summary>
    ///     是否存在同名任务
    /// </summary>
    /// <param name="groupName"></param>
    /// <param name="jobName"></param>
    /// <returns></returns>
    public abstract boolean isExistsSchedule(String jobName,String groupName);

    /// <summary>
    ///     触发器状态转义
    /// </summary>
    /// <param name="ts"></param>
    /// <returns></returns>
    public abstract String transFromEnum(Trigger.TriggerState ts);

    /// <summary>
    ///     任务状态转义
    /// </summary>
    /// <param name="ts"></param>
    /// <returns></returns>
    public abstract String getJobState(int ts);

    /// <summary>
    /// 添加监听
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="groupName"></param>
    /// <param name="jobName"></param>
    /// <param name="listener"></param>
    public abstract <T extends JobListener> void  addListener( String jobName, String groupName,T listener);



    /// <summary>
    ///  修改定时任务
    /// </summary>
    /// <param name="schedu"></param>
    public abstract void modifyScheduler(ScheduleJob schedu);
}
