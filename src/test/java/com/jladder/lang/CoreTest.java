package com.jladder.lang;

import com.jladder.data.Record;
import junit.framework.TestCase;

public class CoreTest extends TestCase {

    public void testGenUuid() {
    }

    public void testMakeThrow() {
    }

    public void testGetTypeArguments() {
    }

    public void testWrapThrow() {
    }

    public void testIsEmpty() {
    }

    public void testTestClone() {
    }

    public void testIsAssignableFrom() {

        TypeReference type = new TypeReference<Record>(){};
        Record data = new Record();
        data.put("xiao",343);
        data.put("ttt",true);
        String t1 = data.getClass().getName();


        String t2 = type.getName();

        boolean ret = Core.isType(data,type);


    }

    public void testIs() {
    }

    public void testIsBaseType() {
    }
}