package com.jladder.lang;

import com.jladder.data.ReStruct;
import com.jladder.data.Receipt;
import com.jladder.data.Record;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 反射操作类
 */
public class Refs {


    public static Class getClazz(String className){
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 获取方法对象
     * @param clazz 类型
     * @param methodName 方法名
     * @return
     */
    public static Method getMethod(Class clazz,String methodName){
        if(clazz==null)return null;
        if(Strings.isBlank(methodName))return null;
        try{
            Matcher match = Regex.match(methodName, "(\\s*\\([\\w\\W]*?\\)\\s*)\\s*$");
            if (match.find()){
                List<Class> ts = new ArrayList<Class>();
                String typestring = match.group(0);
                methodName = methodName.replace(typestring, "");
                typestring =  match.group(1).substring(1, typestring.length() - 1);
                String[] typearray = typestring.split(",");
                for (String s : typearray)
                {
                    switch (s.toLowerCase())
                    {
                        case "string":
                            ts.add(String.class);
                        case "charsequence":
                        case "chars":
                            ts.add(CharSequence.class);
                            break;
                        case "int":
                            ts.add(int.class);
                            break;
                        case "integer":
                            ts.add(Integer.class);
                            break;
                        case "bool":
                        case "boolean":
                            ts.add(boolean.class);
                            break;
                        case "object":
                            ts.add(Object.class);
                            break;
                    }
                }
                Class[] ttss = ts.toArray(new Class[ts.size()]);
                Method method = clazz.getMethod(methodName,ttss);
                return method;

            }else{
                Method[] ms = clazz.getMethods();
                for(Method m : ms){
                    if(methodName.equals(m.getName())){
                        return m;
                    }
                }
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }
    /**
     * 获取方法信息
     * @param methodPath 方法路径
     * @return
     */
    public static ReStruct<Class, Method> getMethod(String methodPath){
        return getMethod(methodPath,null);
    }

    /**
     * 获取方法信息
     * @param className 类名称
     * @param methodName 方法名
     * @return
     */
    public static ReStruct<Class, Method> getMethod(String className,String methodName){
        if (Strings.isBlank(className)) return new ReStruct<Class, Method>("类名不能为空");
        if (Strings.isBlank(methodName)){
            String[] cms = className.split("\\.");
            methodName = cms[cms.length-1];
            className = Strings.rightLess(className,methodName.length() + 1);
        }
        try{
            Class<?> clazz = Class.forName(className);
            Matcher match = Regex.match(methodName, "(\\s*\\([\\w\\W]*?\\)\\s*)\\s*$");
            if (match.find()){
                List<Class> ts = new ArrayList<Class>();
                String typestring = match.group(0);
                methodName = methodName.replace(typestring, "");
                typestring =  match.group(1).substring(1, typestring.length() - 1);
                String[] typearray = typestring.split(",");
                for (String s : typearray)
                {
                    switch (s.toLowerCase())
                    {
                        case "string":
                            ts.add(String.class);
                            break;
                        case "charsequence":
                        case "chars":
                            ts.add(CharSequence.class);
                            break;
                        case "int":
                            ts.add(int.class);
                            break;
                        case "integer":
                            ts.add(Integer.class);
                            break;
                        case "bool":
                        case "boolean":
                            ts.add(boolean.class);
                            break;
                        case "object":
                            ts.add(Object.class);
                            break;
                    }
                }
                Class[] ttss = ts.toArray(new Class[ts.size()]);
                Method method = clazz.getMethod(methodName,ttss);
                if (method == null) return new ReStruct<Class, Method>("方法[" + methodName + "]不存在");
                return new ReStruct<Class, Method>(true,clazz,method);

            }else{
                try{
                    Method ms = clazz.getMethod(methodName);
                    if(ms!=null)return new ReStruct<Class, Method>(true,clazz,ms);
                }catch (Exception e){

                }
                Method[] ms = clazz.getMethods();
                for(Method m : ms){
                    if(methodName.equals(m.getName())){
                        return new ReStruct<Class, Method>(true,clazz,m);
                    }
                }
            }
        }catch (Exception e){
            return new ReStruct<Class, Method>(e.getMessage());

        }
        return new ReStruct<Class, Method>("方法[" + methodName + "]不存在");
    }


    public static Receipt call(Object instance,String methodName, Object... args){
        if (null == instance || Strings.isBlank(methodName)) {
            return new Receipt(false);
        }
        try {
            Method method = instance.getClass().getMethod(methodName);
            Object ret = method.invoke(instance,args);
            return new Receipt().setData(ret);
        } catch (Exception e) {
            e.printStackTrace();
            return new Receipt(false,e.getMessage());
        }
    }
    public static Receipt invoke (Method method, Object data){
        boolean canNull = false;//返回值是否为void类型
        if (method.getReturnType().equals(Void.class)) canNull = true;
        int modify = method.getModifiers();
        Object[] param=mappingMethodParam(method,data);
        Object ret;
        try {
            if(Modifier.isStatic(modify)){
                ret = method.invoke(null,param);
            }else{
                ret = method.invoke(method.getDeclaringClass().newInstance(),param);
            }
            if (ret == null && !canNull)
            {
                return new Receipt(false,"返回结果为空");
            }
            return new Receipt().setData(ret);
        } catch (Exception e) {
            e.printStackTrace();
            new Receipt(false,e.getMessage());
        }
        return new Receipt(false,"未有效执行");
    }

    /**
     * 调用反射
     * @param path 调用方法路径
     * @param data 执行参数[键值数据]
     * @return com.jladder.data.Receipt
     * @author YiFeng
     */

    public static Receipt invoke (String path,Object data){
        return invoke(path,"",data);
    }
    /**
     * 调用反射
     * @param className  类名称
     * @param data 执行参数[键值数据]
     * @param methodName 方法名称
     * @return com.jladder.data.Receipt
     * @author YiFeng
     */

    public static Receipt invoke (String className,String methodName, Object data){
        boolean canNull = false;//返回值是否为void类型
        ReStruct<Class, Method> re = getMethod(className, methodName);
        if (!re.isSuccess())return new Receipt(false);
        if (re.getB().getReturnType().equals(Void.class)) canNull = true;
        int modify = re.getB().getModifiers();
        Object[] param=mappingMethodParam(re.getB(),data);
        Object ret;
        try {
            if(Modifier.isStatic(modify)){
                ret = re.getB().invoke(null,param);
            }else{
                ret = re.getB().invoke( re.getA().newInstance(),param);
            }
            if (ret == null && !canNull)
            {
                return new Receipt(false,"返回结果为空");
            }
            return new Receipt().setData(ret);
        } catch (Exception e) {
            e.printStackTrace();
            new Receipt(false,e.getMessage());
        }
        return new Receipt(false,"未有效执行");

    }

    /***
     * 匹配映射方法参数数据
     * @param method 方法
     * @param data 原始数据
     * @return java.lang.Object[]
     * @author YiFeng
     */

    public static Object[] mappingMethodParam(Method method, Object data){
        Record record = Record.parse(data);
        int count = method.getParameters().length;
        Parameter[] ps = method.getParameters();
        Object[] ret = new Object[count];
        for(int i=0;i<count;i++){
            Class<?> type = ps[i].getType();
            String name = ps[i].getName();
            int index = Regex.isMatch(name,"arg\\d+") ? Convert.toInt(name.replace("arg","")):-1;
            if(Object.class.equals(type)){
                ret[i] = index > -1 ? record.get(index,Object.class):  record.get(name);
                continue;
            }
            if (String.class.equals(type)) {
                ret[i] = index > -1 ? record.getString(index) :  record.getString(name,true);
                continue;
            }
            if(CharSequence.class.equals(type)){
                ret[i] = (CharSequence)(index > -1 ? record.getString(index) :  record.getString(name,true));
                continue;
            }
            if (Integer.class.equals(type) || int.class.equals(type)) {
                ret[i] = index > -1 ? record.getInt(index) :  record.getInt(name,true);
                continue;
            }
            if(String[].class.equals(type)){
                Object v = index > -1 ? record.get(index,Object.class): record.get(name);
                if(v instanceof List){
                    ret[i] = ((List)v).toArray(new String[]{});
                }
                if(v instanceof String[]){
                    ret[i] = (String[])v;
                }
                else{
                    ret[i] =new java.lang.String[]{v.toString()};
                }
                continue;
            }
            if(Core.isBaseType(type,false)){
                ret[i] =  Convert.convert(type,index>-1 ? record.get(index,Object.class) :  record.getObject(name,true));
                continue;
            }else{
                ret[i] = Json.toObject(record.getString(name,true),type);
            }
        }
        return ret;
    }

    public static <T> List<Field> getFields(Class<T> clazz){
        return getFields(clazz,null,null);
    }

    private static <T> List<Field> getFields(Class<T> clazz,List<String> fields,List<Field> ret){
        if(fields==null)fields = new ArrayList<>();
        if(ret==null) ret = new ArrayList<Field>();
        if(clazz !=null && !clazz.getName().toLowerCase().equals("java.lang.object")){
            Field[] fs = clazz.getDeclaredFields();
            for (Field f : fs) {
                if(fields.indexOf(f.getName())<0)ret.add(f);
            }
            getFields(clazz.getSuperclass(),fields,ret);
        }
        return ret;
    }

    public static <T> T newInstance(Class<T> clazz, Object... params) throws Exception {
        if (Core.isEmpty(params)) {
            final Constructor<T> constructor = getConstructor(clazz);
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw e;
            }
        }

        final Class<?>[] paramTypes = Clazz.getClasses(params);
        final Constructor<T> constructor = getConstructor(clazz, paramTypes);
        if (null == constructor) {
            throw Core.makeThrow("No Constructor matched for parameter types: [{}]", new Object[]{paramTypes});
        }
        try {
            return constructor.newInstance(params);
        } catch (Exception e) {
            throw e;
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        if (null == clazz) {
            return null;
        }

        final Constructor<?>[] constructors = getConstructors(clazz);
        Class<?>[] pts;
        for (Constructor<?> constructor : constructors) {
            pts = constructor.getParameterTypes();
            if (Clazz.isAllAssignableFrom(pts, parameterTypes)) {
                // 构造可访问
                setAccessible(constructor);
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }
    public static <T> Constructor<T>[] getConstructors(Class<T> beanClass) throws SecurityException {
        Constructor<?>[] constructors = null;
        if (null != constructors) {
            return (Constructor<T>[]) constructors;
        }

        constructors = getConstructorsDirectly(beanClass);
        return (Constructor<T>[]) constructors;
    }
    /**
     * 获得一个类中所有构造列表，直接反射获取，无缓存
     *
     * @param beanClass 类
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Constructor<?>[] getConstructorsDirectly(Class<?> beanClass) throws SecurityException {
        return beanClass.getDeclaredConstructors();
    }


    /**
     * 设置方法为可访问（私有方法可以被外部调用）
     *
     * @param <T> AccessibleObject的子类，比如Class、Method、Field等
     * @param accessibleObject 可设置访问权限的对象，比如Class、Method、Field等
     * @return 被设置可访问的对象
     */
    public static <T extends AccessibleObject> T setAccessible(T accessibleObject) {
        if (null != accessibleObject && false == accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
        return accessibleObject;
    }

}
