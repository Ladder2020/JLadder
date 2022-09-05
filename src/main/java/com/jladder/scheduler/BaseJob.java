package com.jladder.scheduler;
import com.jladder.lang.Core;
import com.jladder.lang.func.Action0;
import com.jladder.lang.func.Action1;
import com.jladder.lang.func.Func1;
import com.jladder.lang.func.Func2;
import org.quartz.*;
import java.util.List;

public class BaseJob implements Job {
        /**
         * 执行具体任务操作
         *
         * @param context
         * @throws JobExecutionException
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
                if (context.getJobDetail().getJobDataMap() == null) return;
                try {

                        JobDataMap map = context.getJobDetail().getJobDataMap();
                        int type = map.getIntValue("type");
                        JobKey jobKey = new JobKey(context.getJobDetail().getKey().getName(), context.getJobDetail().getKey().getGroup());
                        List<? extends Trigger> ts = context.getScheduler().getTriggersOfJob(jobKey);
                        CronTrigger crontrigger = null;
                        if (!Core.isEmpty(ts)) crontrigger = (CronTrigger) ts.get(0);
                        switch (type) {
                                case 0: {
                                        Action0 action = (Action0) context.getJobDetail().getJobDataMap().get("fun");
                                        action.invoke();
                                }
                                break;
                                case 1: {
                                        Action1<ScheduleJob> action = (Action1<ScheduleJob>) context.getJobDetail().getJobDataMap().get("fun");
                                        ScheduleJob bean = (ScheduleJob) context.getJobDetail().getJobDataMap().get("__ScheduleJob__");
                                        bean.setId(map.getString("id")).setStarttime(context.getTrigger().getStartTime()).setCronexpress(crontrigger == null ? bean.getCronexpress() : crontrigger.getCronExpression());
                                        List<? extends Trigger> triggers = context.getScheduler().getTriggersOfJob(jobKey);
                                        if (!Core.isEmpty(triggers))
                                                bean.setState(context.getScheduler().getTriggerState(triggers.get(0).getKey()).toString());
                                        if (context.getTrigger().getEndTime() != null)
                                                bean.setEndtime(context.getTrigger().getEndTime());
                                        action.invoke(bean);
                                }
                                break;
                                case 2: {
                                        Action1<JobExecutionContext> action = (Action1<JobExecutionContext>) context.getJobDetail().getJobDataMap().get("fun");
                                        action.invoke(context);
                                }
                                break;
                                case 3: {
                                        Func1<Boolean> action = (Func1<Boolean>) context.getJobDetail().getJobDataMap().get("fun");
                                        boolean result = action.invoke();
                                        if (!result) context.getScheduler().deleteJob(jobKey);
                                }
                                break;
                                case 4: {
                                        Func2<JobExecutionContext, Boolean> action = (Func2<JobExecutionContext, Boolean>) context.getJobDetail().getJobDataMap().get("fun");
                                        boolean result = action.invoke(context);
                                        if (!result) context.getScheduler().deleteJob(jobKey);
                                }
                                break;
                                case 5: {
                                        Func2<ScheduleJob, Boolean> action = (Func2<ScheduleJob, Boolean>) context.getJobDetail().getJobDataMap().get("fun");
                                        ScheduleJob bean = (ScheduleJob) context.getJobDetail().getJobDataMap().get("__ScheduleJob__");
                                        bean.setStarttime(context.getTrigger().getStartTime())
                                                .setId(map.getString("id"))
                                                .setCronexpress(crontrigger == null ? "" : crontrigger.getCronExpression());

                                        List<? extends Trigger> triggers = context.getScheduler().getTriggersOfJob(jobKey);
                                        if (!Core.isEmpty(triggers))
                                                bean.setState(context.getScheduler().getTriggerState(triggers.get(0).getKey()).toString());
                                        if (context.getTrigger().getEndTime() != null)
                                                bean.setEndtime(context.getTrigger().getEndTime());
                                        boolean result = action.invoke(bean);
                                        if (!result) context.getScheduler().deleteJob(jobKey);
                                }
                                break;

                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

}