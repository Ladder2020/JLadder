package com.jladder.lang;
import com.jladder.data.ReStruct;
import com.jladder.data.Receipt;
import com.jladder.data.Record;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Refs {

    public static ReStruct<Class, Method> getMethod(String methodPath){
        return getMethod(methodPath,null);
    }
    public static ReStruct<Class, Method> getMethod(String className,String methodName){
        if (Strings.isBlank(className)) return new ReStruct<Class, Method>("类名不能为空");
        if (Strings.isBlank(methodName))
        {
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


    public static Receipt invoke(Object obj,String methodName, Object... args){
        if (null == obj || Strings.isBlank(methodName)) {
            return new Receipt(false);
        }
        try {
            Method method = obj.getClass().getMethod(methodName);
            Object ret = method.invoke(obj,args);
            return new Receipt().setData(ret);
        } catch (Exception e) {
            e.printStackTrace();
            return new Receipt(false,e.getMessage());
        }
    }


    public static Receipt invoke (String className, Object data,String methodName){

        boolean canNull = false;//返回值是否为void类型
        ReStruct<Class, Method> re = getMethod(className, methodName);
        if (!re.Success)return new Receipt(false);
        if (re.ResultB.getReturnType().equals(Void.class)) canNull = true;
        int modify = re.ResultB.getModifiers();
        Object[] param=mappingMethodParam(re.ResultB,data);
        Object ret;
        try {
            if(Modifier.isStatic(modify)){
                ret = re.ResultB.invoke(null,param);
            }else{

                ret = re.ResultB.invoke( re.ResultA.newInstance(),param);
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

    public static Object[] mappingMethodParam(Method method, Object data){
        Record record = Record.parse(data);
        int count = method.getParameters().length;
        Parameter[] ps = method.getParameters();
        Object[] ret = new Object[count];
        for(int i=0;i<count;i++){
            Class<?> type = ps[i].getType();
            String name = ps[i].getName();
            if (String.class.equals(type)) {
                ret[i] = Regex.isMatch(name,"arg\\d+") ? record.getString(Integer.parseInt(name.replace("arg",""))) :  record.getString(name,true);
                continue;
            }
            if(CharSequence.class.equals(type)){
                ret[i] = (CharSequence)(Regex.isMatch(name,"arg\\d+") ? record.getString(Integer.parseInt(name.replace("arg",""))) :  record.getString(name,true));
                continue;
            }
            if (Integer.class.equals(type) || int.class.equals(type)) {
                ret[i] = Regex.isMatch(name,"arg\\d+") ? record.getInt(Integer.parseInt(name.replace("arg",""))) :  record.getInt(name,true);
                continue;
            }
            if(String[].class.equals(type)){
                Object v = record.get(name);
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
                ret[i] =  Convert.convert(type, Regex.isMatch(name,"arg\\d+") ? record.get(Integer.parseInt(name.replace("arg","")),Object.class) :  record.getObject(name,true));
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
}
