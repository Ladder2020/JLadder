package com.jladder.lang;

import com.jladder.data.Record;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class StringsTest extends TestCase {

    public void testMapping1() {



        String source = "${$now+d20=y2018=s10}";
        String  ddtt = Strings.mapping(source);
        System.out.println(ddtt);
    }
    public void testMapping2() {

//        cn.hutool.core.lang.TypeReference<Map<String, Object>> dt= new cn.hutool.core.lang.TypeReference<Map<String, Object>>(){
//
//        };
//        System.out.println(dt.getTypeName());
//        TypeReference2<Map<String, Object>> dd = new TypeReference2<Map<String, Object>>(){};
//
//
//
//        Type ttt = dd.getType();

//        ParameterizedType dt = Types.toParameterizedType();

//
        Object record = new Record();

        Map<String,Object> rere = new HashMap<>();
        System.out.println(rere.getClass().getTypeName());



        if ((new HashMap<String,Object>().getClass()).isAssignableFrom(record.getClass())){
            System.out.println(1111);
        }


//        Class<?>[] clazz = (record.getClass().getClasses());
//        if(record.getClass().isAssignableFrom(((ParameterizedType)ttt).getRawType())){
//            System.out.println(111);
//        }

//        if(record.getClass().isAssignableFrom(Types.getClass((new TypeReference<Map<String,Object>>()).getType()))){
//            System.out.println(111);
//        }


    }
}