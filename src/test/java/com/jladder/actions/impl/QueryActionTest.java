package com.jladder.actions.impl;
import com.jladder.configs.ConfigKey;
import com.jladder.configs.Configure;
import com.jladder.configs.SourceDirection;
import com.jladder.data.AjaxResult;
import com.jladder.data.PageResult;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.lang.Json;
import junit.framework.TestCase;

public class QueryActionTest extends TestCase {

    public void testGetDataByTree() {
    }

    public void testGetDataByRecord() {
    }

    public void testGetDataByEntry() {
    }

    public void testGetData() {



        Configure.put("defaultSchema","ladder", SourceDirection.Memory);
        Object ret = QueryAction.getValue("sys_user","fullname", new Record("username","xiaoxiao").toString());

//        Jedis jedis = new Jedis("localhost");
//
//        jedis.set("runoobkey", "www.runoob.com");

    }

    public void testTestGetData() {
        Configure.put(ConfigKey.TemplateTableName,"ladder.sys_data");
        String start= "0";
        String pageNum= "0";
        String pageSize= "10";
        Cnd cnd = new Cnd();

        PageResult pageResult = QueryAction.getPageData("unionpay_coupon_setting_view", Integer.parseInt(start),
                Integer.parseInt(pageNum), Integer.parseInt(pageSize), cnd, "创建时间:desc", "", "0");





        AjaxResult ret0 = QueryAction.getData("unionpay_crm_traveller");
        AjaxResult ret1 = QueryAction.getData("unionpay_crm_traveller");
        AjaxResult ret = QueryAction.getData("unionpay_crm_traveller");


        System.out.println(Json.toJson(ret));
    }

}