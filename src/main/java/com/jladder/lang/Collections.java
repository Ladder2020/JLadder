package com.jladder.lang;
import com.jladder.data.KeyValue;
import com.jladder.lang.func.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public  class Collections {


    public static <E> Boolean isEmpty(E[] array){
        return (array == null || array.length <1);
    }

    public static <E> Boolean isEmpty(Collection<E> list){
        return (list == null || list.size() < 1 );
    }
    public static <E> boolean any(List<E> es){
        return any(es,null);
    }
    public static <E> boolean any(List<E> es, Func2<E,Boolean> fun){
        if(es==null) return false;
        for (E e : es){
            if(fun==null)return true;
            try {
                if(fun.invoke(e))return true;
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return false;
    }
    public static <E> boolean any(E[] es,Func2<E,Boolean> fun){
        if(es==null) return false;
        for (E e : es){
            if(fun==null)return true;
            try {
                if(fun.invoke(e))return true;
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return false;
    }
    public static <E> void forEach(E[] es, Action1 action){
        if(es==null)return;
        for (E e : es){
            try {
                action.invoke(e);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /***
     *
     * @param array
     * @param fun
     * @param <E>
     * @return
     */
    public static <E> Tuple2<Boolean,E> first(E[] array, Func2<E,Boolean> fun) {
        if(array==null)return new Tuple2(false);
        if(array.length==0)return new Tuple2(false);
        if(fun==null)return new Tuple2(true,array[0]);
        for (E e : array) {
            try {
                Boolean ret = fun.invoke(e);
                return new Tuple2<>(true,e);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return new Tuple2(false);
    }

    /***
     *
     * @param map
     * @param fun
     * @param <K,V>
     * @return
     */
    public static <K,V> Tuple3<Boolean,K,V> first(Map<K,V> map, Func3<K,V,Boolean> fun){
        if(map==null)return new Tuple3(false);
        for(Map.Entry<K, V> entry  : map.entrySet()){
            if(fun==null)return new Tuple3(true,entry.getKey(),entry.getValue());
            else {
                try {
                    if(fun.invoke(entry.getKey(),entry.getValue())){
                        return new Tuple3(true,entry.getKey(),entry.getValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return new Tuple3(false);
    }

    /**
     *
     * @param map
     * @param <K>
     * @param <T>
     * @return
     */
    public static <K,T> List<K> keys(Map<K,T> map){
        return null;
    }

    /***
     *
     * @param collection
     * @param fun
     * @param <E>
     * @return
     */
    public static <E> Tuple2<Boolean,E> first(Collection<E> collection, Func2<E,Boolean> fun){
        if(collection==null)return new Tuple2(false,null);
        for(E e : collection){
            if(fun==null)return new Tuple2(true,e);
            else {
                try {
                    if(fun.invoke(e)){
                        return new Tuple2(true,e);
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
        return new Tuple2(false,null);
    }

    /***
     *
     * @param dic
     * @param key
     * @param <T>
     * @return
     */
    public static <T> String getString(Map<String, T> dic, String key){
        return getString(dic,key,false);
    }

    /***
     *
     * @param map
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T toClass(Map<String,Object> map,Class<T> clazz) {

        T t = null;
        try {
            t = clazz.newInstance();
            List<Field> fs = Refs.getFields(clazz);
            for(Field field : fs) {
                String key = haveKey(map,field.getName());
                if (Strings.hasValue(key)) {
                    int mod = field.getModifiers();
                    if(Modifier.isStatic(mod) || Modifier.isFinal(mod)){
                        continue;
                    }
                    boolean flag = field.isAccessible();
                    field.setAccessible(true);
                    Object object = map.get(key);
                    if (object!= null) {
                        field.set(t, Convert.convert(field.getType(),object));
                    }
                    field.setAccessible(flag);
                }
            }
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return Json.toObject(Json.toJson(map),clazz);
        }
    }

    public static <T> int getInt(Map<String, T> dic, String key,boolean ignoreCase){
        if (dic == null || dic.size() < 1 || Strings.isBlank(key)) return 0;
        String[] keys = Regex.split(key, "\\||,");
        if (keys.length == 1)
        {
            if (ignoreCase)
            {
                key = haveKey(dic,key);
                if (Strings.isBlank(key)) return 0;
            }
            else
            {
                if (dic == null || Strings.isBlank(key) || !dic.containsKey(key)) return 0;
            }
            T val = dic.get(key);
            if (val == null) return 0;
            return Strings.toInt(val.toString());
//            if(val instanceof CharSequence || val instanceof Object)return Integer.valueOf(val.toString());
//            else return (int)val;
        }
        else
        {
            for (String s : keys)
            {
                String v = getString(dic, s, ignoreCase);
                if (v!=null) return Integer.valueOf(v.toString());
            }
        }
        return 0;
    }

    /***
     *
     * @param dic
     * @param key
     * @param ignoreCase
     * @param <T>
     * @return
     */
    public static <T> String getString(Map<String, T> dic, String key,boolean ignoreCase) {
        if (dic == null || dic.size() < 1 || Strings.isBlank(key)) return "";
        String[] keys = Regex.split(key, "\\||,");
        if (keys.length == 1)
        {
            if (ignoreCase)
            {
                key = haveKey(dic,key);
                if (Strings.isBlank(key)) return null;
            }
            else
            {
                if (dic == null || Strings.isBlank(key) || !dic.containsKey(key)) return "";
            }
            T val = dic.get(key);
            if (val == null) return "";
            if(val instanceof CharSequence)return val.toString();
            if(val instanceof Object)return Json.toJson(val);
            else  return val.toString();
        }
        else
        {
            for (String s : keys)
            {
                String v = getString(dic, s, ignoreCase);
                if (v!=null) return v;
            }
        }
        return "";
    }

    public static <T> String haveKey(Map<String, T> map,String ... key)
    {
        if (map == null || (key==null) || key.length<1) return null;
        Set<String> ks = map.keySet();
        for (String k : key)
        {
            if (Strings.isBlank(k)) continue;
            String[] keys = Regex.split(k, "\\||,");
            if (keys.length == 1)
            {
                if (map.containsKey(k)) return k;
                Tuple2<Boolean, String> first = Collections.first(ks,kkk->kkk.toLowerCase().equals(k.toLowerCase()));
                if(first.item1)return first.item2;
            }
            else
            {
                //可以转换成linq，但没有这样好理解
                for (String s : keys)
                {
                    if(map.containsKey(s))return s;
                    Tuple2<Boolean, String> first = Collections.first(ks,kkk->kkk.toLowerCase().equals(s.toLowerCase()));
                    if(first.item1)return first.item2;
                }
            }
        }
        return null;
    }
    public static <E> List<E> toList(E[] source){
        if(source==null)return null;
        List<E> list = new ArrayList<E>();
        for (E e : source) {
            list.add(e);
        }
        return list;
    }

    public static <E> List<E> distinct(Collection<E> source){
        if(source==null)return null;
        List<E> list = new ArrayList<E>();
        source.forEach(x->{
            if(list.indexOf(x)<0)list.add(x);
        });
        return list;
    }

    public static <E> List<E> create(E ... e){
        List<E> ret = new ArrayList<E>();
        if(e==null)return ret;
        for(E r : e){
            ret.add(r);
        }
        return ret;
    }

    public static <E,T> List<T> select(Collection<E> source,Func2<E,T> func){
        List<T> ret = new ArrayList<T>();
        source.forEach(x->{
            try {
                ret.add(func.invoke(x));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ret;
    }
    public static <T,K,V> List<T> select(Map<K,V> source,Func3<K,V,T> func){
        List<T> ret = new ArrayList<T>();
        source.forEach((x,y)->{
            try {
                ret.add(func.invoke(x,y));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ret;
    }
    public static <E> int count(Collection<E> list,Func2<E,Boolean> fun){
        AtomicInteger count = new AtomicInteger();
        if(list==null || list.size()==0)return 0;
        if(fun==null)return list.size();
        list.forEach(x->{
            try {
                if(fun.invoke(x)) count.getAndIncrement();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return count.get();
    }
    public static <E> int count(E[] list,Func2<E,Boolean> fun){
        AtomicInteger count = new AtomicInteger();
        if(list==null || list.length==0)return 0;
        if(fun==null)return list.length;
        for (E x : list) {
            try {
                if(fun.invoke(x)) count.getAndIncrement();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return count.get();
    }
    public static <K,V> int count(Map<K,V> map,Func3<K,V,Boolean> fun){
        AtomicInteger count = new AtomicInteger();
        if(map==null || map.size()==0)return 0;
        if(fun==null)return map.size();
        map.forEach((k,v)->{
            try {
                if(fun.invoke(k,v)) count.getAndIncrement();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return count.get();
    }
    public static <K,V> int count(Map<K,V> map,Func2<KeyValue,Boolean> fun){
        AtomicInteger count = new AtomicInteger();
        if(map==null || map.size()==0)return 0;
        if(fun==null)return map.size();
        map.forEach((k,v)->{
            try {
                if(fun.invoke(new KeyValue(k,v))) count.getAndIncrement();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return count.get();
    }
    public static <E> List<E> where(Collection<E> list,Func2<E,Boolean> fun){
        List<E> ret = new ArrayList<E>();
        if(list==null || list.size()==0)return ret;
        list.forEach(x->{
            try {
                if(fun==null){
                    ret.add(x);
                }else{
                    if(fun.invoke(x)) ret.add(x);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ret;
    }
    public static <E> List<E> where(E[] list,Func2<E,Boolean> fun){
        List<E> ret = new ArrayList<E>();
        if(list==null || list.length==0)return null;
        for (E x : list) {
            try {
                if(fun==null){
                    ret.add(x);
                }else{
                    if(fun.invoke(x)) ret.add(x);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    public static <K,V> Map<K,V> where(Map<K,V> map,Func2<KeyValue,Boolean> fun){
        Map<K,V>  ret = new HashMap<K,V>();
        if(map==null || map.size()==0)return ret;
        if(fun==null)return ret;
        map.forEach((k,v)->{
            try {
                if(fun.invoke(new KeyValue<K,V>(k,v))) ret.put(k,v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ret;
    }
    public static <K,V> Map<K,V> where(Map<K,V> map,Func3<K,V,Boolean> fun){
        Map<K,V>  ret = new HashMap<K,V>();
        if(map==null || map.size()==0)return ret;
        if(fun==null)return ret;
        map.forEach((k,v)->{
            try {
                if(fun.invoke(k,v)) ret.put(k,v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ret;
    }
    public static <T> Object get(Map<String,T> dic, String key, boolean ignoreCase){
        String kk = haveKey(dic,key);
        if(Strings.isBlank(kk))return null;
        return dic.get(kk);
    }
    public static <T,V> T get(Map<String, V> dic, String key, boolean ignoreCase, Class<T> clazz) {
        return Convert.convert(clazz,getString(dic,key,ignoreCase));
    }

    public static <E> E last(E[] array) {
        if(array==null)return null;
        return array[array.length-1];
    }
    public static <E> E last(List<E> list) {
        if(list==null)return null;
        return list.get(list.size()-1);
    }
    public static <E> int count(List<E> list,Func2<E,Boolean> fun){
        int count = 0;
        for (E e : list) {
            try {
                if(fun.invoke(e))count++;
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return count;
    }
//    public static <E> int count(E[] array,Func2<E,Boolean> fun){
//        int count = 0;
//        for (E e : array) {
//            try {
//                if(fun.invoke(e))count++;
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
//        }
//        return count;
//    }
//    public static <E> E[] toArray(Collection<E> list){
//        int count = list.size();
//        E []
//        return es;
//    }
}
