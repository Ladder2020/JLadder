package com.jladder.web;

import com.jladder.lang.func.Func3;

import java.util.HashMap;
import java.util.Map;

public class EventBus {


    public static final int Event_Login=1002;

    public static final int Event_UpdateConfig=2001;


    private static final Map<Integer, Func3<Object,Object,Object>> events=new HashMap<Integer, Func3<Object,Object,Object>>();

    public static void put(int code,Func3<Object,Object,Object> fun){
        events.put(code,fun);
    }
    public static Func3<Object,Object,Object> get(int code){
        return events.get(code);
    }
    public static Object execute(int code,Object sender,Object data){
        Func3<Object, Object, Object> fun = events.get(code);
        if(fun==null)return null;
        try {
           return fun.invoke(sender,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
