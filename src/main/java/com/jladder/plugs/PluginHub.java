package com.jladder.plugs;

import java.util.HashMap;
import java.util.Map;

public class PluginHub {

    private static final Map<Class,Object> plugins = new HashMap<Class,Object>();

    public static <T> void  add (T plugin){
        plugins.put(plugin.getClass(),plugin);
    }
    public static  void  add (Class clazz, Object plugin){
        plugins.put(clazz,plugin);
    }
    public static <T> T get(Class<T> clazz){
        Object old = plugins.get(clazz);
        if(old==null)return null;
        else return (T) old;
    }

}
