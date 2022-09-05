package com.jladder.lang;

import com.jladder.data.Receipt;
import com.jladder.data.Record;
import junit.framework.TestCase;

public class RefsTest extends TestCase {

    public void testGetMethod() {
    }

    public void testTestGetMethod() {
    }

    public void testInvoke() {

        Receipt ret = Refs.invoke("com.jladder.core.lang.Strings.mapping(chars)", new Record("str", "${$date}"));


    }

    public void testMappingMethodParam() {
    }
}