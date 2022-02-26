package com.jladder.lang;
import com.jladder.data.Receipt;
import com.jladder.lang.func.Func1;
import com.jladder.lang.func.Tuple2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Core {

    public static String genUuid(){
        return UUID.randomUUID().toString().replace("-", "");
    }
    public static String genNuid(){
        return SnowFlake.Instance().nextId()+"";
    }
    public static RuntimeException makeThrow(String format, Object... args) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement father = stackTrace[2];
        System.out.println("类:"+father.getClassName()+ ",方法:"+father.getMethodName());
        return new RuntimeException(String.format(format, args));
    }
    /**
     * 判断对象实现的所有接口中是否包含szInterface
     *
     * @param clazz
     * @param szInterface
     * @return
     */
    public static boolean isImplementsOf(Class<?> clazz, Class<?> szInterface) {
        boolean flag = false;
        Class<?>[] face = clazz.getInterfaces();
        for (Class<?> c : face) {
            if (c == szInterface) {
                flag = true;
            } else {
                flag = isImplementsOf(c, szInterface);
            }
        }
        if (!flag && null != clazz.getSuperclass()) {
            return isImplementsOf(clazz.getSuperclass(), szInterface);
        }
        return flag;
    }
    public static Type[] getTypeArguments(Type type) {
        if (null == type) {
            return null;
        } else {
            ParameterizedType parameterizedType = Types.toParameterizedType(type);
            return null == parameterizedType ? null : parameterizedType.getActualTypeArguments();
        }
    }

    /**
     * 用运行时异常包裹抛出对象，如果抛出对象本身就是运行时异常，则直接返回
     * 如果是 InvocationTargetException，那么将其剥离，只包裹其 TargetException
     * @param e 抛出对象
     * @return 运行时异常
     */
    public static RuntimeException wrapThrow(Throwable e) {
        if (e instanceof RuntimeException)
            return (RuntimeException) e;
        if (e instanceof InvocationTargetException)
            return wrapThrow(((InvocationTargetException) e).getTargetException());
        return new RuntimeException(e);
    }
    /**
     * 判断一个对象是否为空。它支持如下对象类型：
     * <ul>
     * <li>null : 一定为空
     * <li>数组
     * <li>集合
     * <li>Map
     * <li>其他对象 : 一定不为空
     * </ul>
     * @param obj 任意对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null)
            return true;
        if (obj.getClass().isArray())
            return Array.getLength(obj) == 0;
        if (obj instanceof Collection<?>)
            return ((Collection<?>) obj).isEmpty();
        if (obj instanceof Map<?, ?>)
            return ((Map<?, ?>) obj).isEmpty();
        return false;
    }


    public static <T> T clone(T obj){

        if(obj==null)return obj;
        if(obj instanceof Cloneable){
            return (T)Refs.invoke(obj,"clone").data;
        }
        T cloneObj = null;
        //写入字节流
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(out);
            obs.writeObject(obj);
            obs.close();

            //分配内存，写入原始对象，生成新对象
            ByteArrayInputStream ios = new ByteArrayInputStream(out.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(ios);
            //返回生成的新对象
            cloneObj = (T) ois.readObject();
            ois.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return cloneObj;
    }

    /***
     * 判断类型是否相同
     * @param obj 对象
     * @param type 类型
     * @return
     */
    public static boolean isType(Object obj,Type type){
        if(obj==null)return false;
        if(type==null)return false;
        if(obj.getClass().getName().equals(Regex.replace(type.getTypeName(),"^class\\s*","")))return true;
        return false;
    }
//    public static <T> T get1(Object parent,T val){
//        if(parent==null)return null;
//        return val;
//    }
    public static <T extends Object> T or(T... value){
        if(value==null)return null;
        for (int i = 0; i < value.length; i++) {
            if(value[i]!=null && Strings.hasValue(value[i].toString()))return value[i];
        }
        return null;
    }
    public static boolean is(Object v,Object ... dv){
        for (Object o : dv) {
            if(o == null && v==null)return true;
            if(o!=null){
                if(o.equals(v))return true;
            }
        }
        return false;
    }
    /**
     * 判断对象属性是否是基本数据类型,包括是否包括string
     * @param className
     * @param incString 是否包括string判断,如果为true就认为string也是基本数据类型
     * @return
     */
    public static boolean isBaseType(Class className, boolean incString) {
        if (incString && className.equals(String.class)) {
            return true;
        }
        return className.equals(Integer.class) ||
                className.equals(int.class) ||
                className.equals(Byte.class) ||
                className.equals(byte.class) ||
                className.equals(Long.class) ||
                className.equals(long.class) ||
                className.equals(Double.class) ||
                className.equals(double.class) ||
                className.equals(Float.class) ||
                className.equals(float.class) ||
                className.equals(Character.class) ||
                className.equals(char.class) ||
                className.equals(Short.class) ||
                className.equals(short.class) ||
                className.equals(Boolean.class) ||
                className.equals(boolean.class);
    }

    public static String getStackTrace(Exception e){
        StringBuffer sb = new StringBuffer();
        Arrays.stream(e.getStackTrace()).forEach(x->sb.append(x.getClassName()+"["+x.getLineNumber()+"]"+System.lineSeparator()));
        return sb.toString();
    }

    public static String toString(Object obj) {
        if(obj==null)return null;
        return obj.toString();
    }
    public static Receipt watch(Func1<Tuple2<Boolean,Object>> func, int span){
        return watch(func,span,100);
    }
    /// <summary>
    /// 监控变化
    /// </summary>
    /// <param name="func">监控函数</param>
    /// <param name="span">超时时间</param>
    /// <param name="interval">间隔轮训时间</param>
    /// <returns></returns>
    public static Receipt watch(Func1<Tuple2<Boolean,Object>> func, int span, int interval)
    {
        if (func == null) return new Receipt(false, "无运行表达式");
        try{
            AtomicReference<Tuple2<Boolean, Object>> ret = new AtomicReference<>(func.invoke());
            if (ret.get().item1) return new Receipt().setData(ret.get().item2);
            long now = Times.getTime();
            long end = Times.addSecond(span).getTime();
            AutoResetEvent auto = new AutoResetEvent(false);
            Task.startNew(() ->{
                while (end > now)
                {
                    ret.set(func.invoke());
                    if (ret.get().item1)
                    {
                        auto.set();
                        break;
                    }
                    Thread.sleep(interval);
                }
            });
            auto.waitOne(span*1000);
            return ret.get().item1 ? new Receipt().setData(ret.get().item2) : new Receipt(false,"超时无结果");
        }
        catch (Exception e){

        }
        return new Receipt(false,"超时无结果");
    }
}
