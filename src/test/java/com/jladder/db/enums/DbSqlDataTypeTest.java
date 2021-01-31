package com.jladder.db.enums;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DbSqlDataTypeTest extends TestCase {

    public void testGetIndex() {
        final int dt = DbSqlDataType.GetCount.ordinal();
        Assert.assertEquals(1,dt);
    }

    public void testValues() {
    }

    public void testValueOf() {
    }
}