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

    public void testTestInsert1() {


    }

    public void testTestInsert2() {
    }

    public void testTestInsert3() {
    }

    public void testUpdate() {
    }

    public void testTestUpdate() {
    }

    public void testDelete() {
    }

    public void testTestDelete() {
    }

    public void testSave() {
    }

    public void testTestSave() {
    }

    public void testSaveBean() {
    }

    public void testSaveBeans() {
        //临时添加
        DataHub.TemplateTableName="tmcp_ladder.sys_data";

        List<Curd> curds = new ArrayList<>();

        //1
        Curd curd1 = new Curd("jtabletest",new Record("name","ddd1").put("id",100), DbSqlDataType.Insert,null);
        Curd curd2 = new Curd("jtabletest",new Record("name","ddd2").put("id",101), DbSqlDataType.Insert,null);
        Curd curd3 = new Curd("jtabletest",new Record("name","ddd3").put("id",102), DbSqlDataType.Insert,null);
        Curd curd4 = new Curd("jtabletest",new Record("name","ddd3"), DbSqlDataType.Update,new Cnd("id","=",2));
        curds.add(curd1);
        curds.add(curd2);
        curds.add(curd3);
        curds.add(curd4);

        Receipt ret = SaveAction.saveBeans(curds, true);

    }

    public void testTestSaveBean() {
    }

    public void testTestSaveBean1() {
    }

    public void testTestSaveBean2() {
    }

    public void testTestSaveBean3() {
    }

    public void testTestSaveBean4() {
    }

    public void testTestSaveBean5() {
    }

    public void testHandAction() {
    }

    public void testTestSaveBean6() {
    }

    public void testTestSaveBean7() {
    }

    public void testTestSaveBean8() {
    }

    public void testTestSaveBean9() {
    }

    public void testTestSaveBean10() {
    }

    public void testTestSaveBeans() {
        DataHub.TemplateTableName="tmcp_ladder.sys_data";

//        String content = FileUtil.readString("D:\\saveBeansjson.json", StandardCharsets.UTF_8);
//
//        //Object ret1 = Json.toObject(content, new TypeReference2<List<Record>>(){});
//        //Object ret2 = Json.toObject(content,new TypeReference<List<Record>>(){});
//        Object ret3 = Json.toObject(content,new com.jladder.core.lang.TypeReference<List<Record>>(){});
//        AjaxResult ret = SaveAction.saveBeans(content, true);
//
//        System.out.println(ret);


    }

    public void testSaveBeansWithOut() {
    }

    public void testTestSaveBeansWithOut() {
    }
}