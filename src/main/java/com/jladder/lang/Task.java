package com.jladder.lang;

import com.jladder.lang.func.Action0;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Task {

    private static ScheduledExecutorService factory = Executors.newSingleThreadScheduledExecutor();


    public static void start(Runnable run){
        Executors.newSingleThreadScheduledExecutor().submit(run);
    }

    public static void startNew(Action0 fun){
        new Thread(() -> {
            try {
                fun.invoke();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },"线程3").run();
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
