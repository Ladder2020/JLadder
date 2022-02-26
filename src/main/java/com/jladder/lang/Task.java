package com.jladder.lang;

import com.jladder.lang.func.Action0;
import jodd.time.TimeUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Task {

    private final static ScheduledExecutorService factory = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "LadderTask");
            t.setDaemon(true);
            return t;
        }
    });
    public static void start(Runnable run){
        Executors.newSingleThreadScheduledExecutor().submit(run);
    }
    public static void start(Runnable run, int time, TimeUnit unit){
        factory.schedule(run,time,unit);
    }
    public static void startNew(Action0 fun){
        new Thread(() -> {
            try {
                fun.invoke();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },"线程3").start();
//        factory.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    fun.invoke();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }
}
