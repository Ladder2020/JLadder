package com.jladder.lang;
import com.jladder.data.KeyValue;
import com.jladder.db.Rs;
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
    public static <E> boolean any(Collection<E> es){
        return any(es,null);
    }
    public static <E> boolean any(Collection<E> es, Func2<E,Boolean> fun){
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

    public static <E> E first(E[] array){
        return first(array,null).item2;
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
     * param &lt;K,V&gt;
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
    public static <T> String getString(Map<String, T> dic, String key,String defaultValue){
        return getString(dic,key,defaultValue,false);
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

    /**
     * 获取整数值
     * @param dic 数据
     * @param key 键名
     * @param ignoreCase 是否忽略大小写
     * @param <T> 泛型
     * @return
     */
    public static <T> int getInt(Map<String, T> dic, String key,boolean ignoreCase){
        if (dic == null || dic.size() < 1 || Strings.isBlank(key)) return 0;
        if (ignoreCase){
            key = haveKey(dic,key);
            if (Strings.isBlank(key)) return 0;
        }
        else {

            String[] keys = Regex.split(key, "\\||,");
            key=null;
            for (String s : keys) {
                if (dic.containsKey(s)){
                    key=s;
                }
            }
            if(Strings.isBlank(key))return 0;
        }
        T val = dic.get(key);
        if (val == null) return 0;
        return Strings.toInt(val.toString());
    }

    /***
     *
     * @param dic
     * @param key
     * @param ignoreCase
     * @param <T>
     * @return
     */
    public static <T> String getString(Map<String, T> dic, String key,String defaultValue,boolean ignoreCase) {
        if (dic == null || dic.size() < 1 || Strings.isBlank(key)) return defaultValue;
        if (ignoreCase){
            key = haveKey(dic,key);
        }
        else{
            String[] keys = Regex.split(key, "\\||,");
            key=null;
            for (String s : keys) {
                if (dic.containsKey(s)){
                    key=s;
                    break;
                }
            }
        }
        if(Strings.isBlank(key))return defaultValue;
        T val = dic.get(key);
        if (val == null) return defaultValue;
        if(val instanceof CharSequence)return val.toString();
        if(val instanceof Date)return Times.sDT((Date)val);
        if(val instanceof Object)return Json.toJson(val);
        else  return val.toString();
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
        return Convert.convert(clazz,getString(dic,key,null,ignoreCase));
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


    /**
     * 对list中的元素按fields和sorts进行排序,
     * fields[i]指定排序字段,sorts[i]指定排序方式.如果sorts[i]为空则默认按升序排列.
     * @param list
     * @param fields
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<?> sort(List<?> list, String[] fields) {
        if (fields != null && fields.length > 0) {
            for (int i = fields.length - 1; i >= 0; i--) {
                String[] fieldArray = fields[i].split(":");
                final String field = first(fieldArray);
                String tmpSort = "asc";
                if(fieldArray.length>1)tmpSort=last(fieldArray);
                final String sort = tmpSort;
                java.util.Collections.sort(list, (Comparator) (a, b) -> {
                    int ret = 0;
                    try {
                        Field f = a.getClass().getDeclaredField(field);
                        f.setAccessible(true);
                        Class<?> type = f.getType();
                        if (type == int.class) {
                            ret = ((Integer) f.getInt(a))
                                    .compareTo((Integer) f.getInt(b));
                        } else if (type == double.class) {
                            ret = ((Double) f.getDouble(a))
                                    .compareTo((Double) f.getDouble(b));
                        } else if (type == long.class) {
                            ret = ((Long) f.getLong(a)).compareTo((Long) f
                                    .getLong(b));
                        } else if (type == float.class) {
                            ret = ((Float) f.getFloat(a))
                                    .compareTo((Float) f.getFloat(b));
                        } else if (type == Date.class) {
                            ret = ((Date) f.get(a)).compareTo((Date) f
                                    .get(b));
                        } else if (Core.isImplementsOf(type, Comparable.class)) {
                            ret = ((Comparable) f.get(a)).compareTo((Comparable) f.get(b));
                        } else {
                            ret = String.valueOf(f.get(a)).compareTo(String.valueOf(f.get(b)));
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (sort != null && sort.equalsIgnoreCase("desc")) {
                        return -ret;
                    } else {
                        return ret;
                    }
                });
            }
        }
        return list;
    }

    /**
     * 对list中的元素进行降序排列
     * @param list 集合
     * @param field 字段
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<?> sort(List<?> list, final String field){
        return sort(list,field,true);
    }
    /**
     * 对list中的元素进行排序.
     *
     * @param list  排序集合
     * @param field 排序字段
     * @param desc 排序方式: true(降序) false(升序).
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> sort(List<T> list, final String field,final boolean desc) {
        if (Core.isEmpty(list)) {
            return list;
        }
        java.util.Collections.sort(list, new Comparator() {
            public int compare(Object a, Object b) {
                int ret = 0;
                try {
                    if(a instanceof Map){
                        Object av = ((Map) a).get(field);
                        Object bv = ((Map) b).get(field);
                        if(av instanceof Integer){
                            ret = ((Integer)av).compareTo((Integer)bv);
                        }
                        else if(av instanceof Double){
                            ret = ((Double)av).compareTo((Double)bv);
                        }
                        if(av instanceof Long){
                            ret = ((Long)av).compareTo((Long)bv);
                        }
                        else if(av instanceof Float){
                            ret = ((Float)av).compareTo((Float)bv);
                        }
                        else if(av instanceof Date){
                            ret = ((Date)av).compareTo((Date)bv);
                        }
                        else if (Core.isImplementsOf(av.getClass(), Comparable.class)){
                            ret = ((Comparable)av).compareTo(bv);
                        }
                        else ret = String.valueOf(av).compareTo(String.valueOf(bv));
                    }else{
                        Field f = a.getClass().getDeclaredField(field);
                        f.setAccessible(true);
                        Class<?> type = f.getType();
                        if (type == int.class) {
                            ret = ((Integer) f.getInt(a)).compareTo((Integer) f.getInt(b));
                        } else if (type == double.class) {
                            ret = ((Double) f.getDouble(a)).compareTo((Double) f.getDouble(b));
                        } else if (type == long.class) {
                            ret = ((Long) f.getLong(a)).compareTo((Long) f.getLong(b));
                        } else if (type == float.class) {
                            ret = ((Float) f.getFloat(a)).compareTo((Float) f.getFloat(b));
                        } else if (type == Date.class) {
                            ret = ((Date) f.get(a)).compareTo((Date) f.get(b));
                        } else if (Core.isImplementsOf(type, Comparable.class)) {
                            ret = ((Comparable) f.get(a)).compareTo((Comparable) f.get(b));
                        } else {
                            ret = String.valueOf(f.get(a)).compareTo(String.valueOf(f.get(b)));
                        }
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (desc) {
                    return -ret;
                } else {
                    return ret;
                }

            }
        });
        return list;
    }

    /**
     * 逆转记录集
     * @param list 记录集
     * @param propName 字段名
     * @param <V>
     * @return
     */
    public static <V> Map<String,Map<String,V>> reverse(Collection<Map<String,V>> list,String propName){
        if (list == null) return null;
        if (propName == null) return null;
        Map<String, Map<String, V>> re_map = new HashMap<String, Map<String, V>>();
        for (Map<String, V> cmap : list){
            V v = cmap.get(propName);
            String str_fn = v!=null ? v.toString():"";
            if (!Strings.isBlank(str_fn)) re_map.put(str_fn, cmap);
        }
        return re_map.size() < 1 ? null : re_map;
    }

    /**
     * 数组分组
     * @author 徐洪昌
     * @date 2021/12/4 13:18
     * @param a 要分组的集合
     * @param func 分组字段
     * @return java.util.List<org.apache.poi.ss.formula.functions.T>
     */
    public static <E> Map<String,List<E>> groupBy(List<E> a, Func2<E, String> func){
        Map<String,List<E>> map = new TreeMap<>();
        if(!Rs.isBlank(a) && func != null){
            a.forEach(x->{
                String key = null;
                try {
                    key = func.invoke(x);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(map.containsKey(key)) {
                    map.get(key).add(x);
                }
                else {
                    List<E> atr = new ArrayList<>();
                    atr.add(x);
                    map.put(key, atr);
                }
            });
        }
        return map;
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
