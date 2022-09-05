package com.jladder.scheduler;

public interface OnScheduleResult {
    void callback(ScheduleJob job, Object result, String error);
}
