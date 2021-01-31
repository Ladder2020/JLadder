package com.jladder.lang;

import java.util.List;
import java.util.Map;

public class Linq<E,K,V> {

    public List<E> list;
    public E[] array;

    public Map<K,V> map;

    public static <E> Linq create(List<E> list){
        Linq<E,Object,Object> linq = new Linq<E,Object,Object>();
        linq.list=list;
        return linq;
    }
    public static <E> Linq create(E[] array){
        Linq<E,Object,Object> linq = new Linq<E,Object,Object>();
        linq.array=array;
        return linq;
    }
    public static <K,V> Linq create(Map<K,V> map){
        Linq<Object,K,V> linq = new Linq<Object,K,V>();
        linq.map=map;
        return linq;
    }
//    public Linq where(Func2 fun){
//
//    }
}
