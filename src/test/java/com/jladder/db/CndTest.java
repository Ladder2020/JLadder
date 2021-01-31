package com.jladder.db;

import junit.framework.TestCase;

public class CndTest extends TestCase {

    public void testIn() {
    }

    public void testAnd() {


       Cnd cnd =  new Cnd("a",1).put("n",2).put("c","not in",new ReCall("sys_user","username",new Cnd("d",5).put("e",6),null));

       System.out.println(cnd.getWhere(true,true));

    }



    public void testPut() {
    }

    public void testTestPut() {
    }

    public void testTestPut1() {
    }

    public void testTestPut2() {
    }

    public void testTestPut3() {
    }

    public void testParseRecord() {
    }

    public void testTestPut4() {
    }

    public void testGetDataModelSql() {
    }

    public void testGetDialectType() {
    }

    public void testSetDialect() {
    }

    public void testParse() {
    }

    public void testTestParse() {
    }

    public void testHasValue() {
    }

    public void testTestParse1() {
    }

    public void testGetWhere() {
    }

    public void testGetParameters() {
    }

    public void testClear() {
    }

    public void testOr() {
    }

    public void testIsBlank() {
    }

    public void testInitFieldMapping() {
    }
}