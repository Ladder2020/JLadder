package com.jladder.lang;

import com.jladder.lang.func.Action0;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Task {

    private static ScheduledExecutorService factory = Executors.newSingleThreadScheduledExecutor();


    public static void start(Runnable run){
        factory.submit(run);
    }

    public static void start(Action0 fun){
        factory.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    fun.invoke();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
