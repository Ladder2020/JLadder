package com.jladder.actions.impl;
import com.jladder.actions.Curd;
import com.jladder.data.AjaxResult;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.hub.DataHub;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SaveActionTest extends TestCase {

    public void testInsert() {

        AjaxResult ret = SaveAction.insert("*del_sys_site", new Record("title", "fdadsfasd").put("id",111));

    }

    public void testTestInsert() {

        try {
            Class<?> clazz = Class.forName("com.jladder.lang.Strings");
            Method meth = clazz.getMethod("mapping",CharSequence.class);
            Object ret = meth.invoke(null,"${$date}");
            System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}