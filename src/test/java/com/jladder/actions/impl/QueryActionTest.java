package com.jladder.actions.impl;
import com.jladder.configs.Configs;
import com.jladder.data.AjaxResult;
import com.jladder.data.PageResult;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.hub.DataHub;
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



        Configs.put("defaultSchema","ladder");
        Object ret = QueryAction.getValue("sys_user","fullname", new Record("username","xiaoxiao").toString());

//        Jedis jedis = new Jedis("localhost");
//
//        jedis.set("runoobkey", "www.runoob.com");

    }

    public void testTestGetData() {
        DataHub.TemplateTableName="tmcp_ladder.sys_data";
        String start= "0";
        String pageNum= "0";
        String pageSize= "10";
        Cnd cnd = new Cnd();

        PageResult pageResult = QueryAction.getPageData("unionpay_coupon_setting_view", Integer.parseInt(start),
                Integer.parseInt(pageNum), Integer.parseInt(pageSize), cnd, "创建时间:desc", "", 0);





        AjaxResult ret0 = QueryAction.getData("unionpay_crm_traveller");
        AjaxResult ret1 = QueryAction.getData("unionpay_crm_traveller");
        AjaxResult ret = QueryAction.getData("unionpay_crm_traveller");


        System.out.println(Json.toJson(ret));
    }

    public void testTestGetData1() {
    }

    public void testTestGetData2() {
    }

    public void testTestGetData3() {
    }

    public void testTestGetData4() {
    }

    public void testTestGetData5() {
    }

    public void testTestGetData6() {
    }

    public void testTestGetData7() {
    }

    public void testGetPageData() {
    }

    public void testTestGetPageData() {
    }

    public void testTestGetPageData1() {
        DataHub.TemplateTableName="tmcp_ladder.sys_data";
        Cnd cnd = new Cnd("delete_flag", 0);
        PageResult rs = QueryAction.getPageData("unionpay_sys_info_type", 0, 2, 10, cnd, "type_code,type_name,classify,state,remark", "", 0);
    }

    public void testQueryData() {
        DataHub.TemplateTableName="tmcp_ladder.sys_data";
        AjaxResult result = QueryAction.queryData("tmcp_sys_permission", "{\"columnString\":\"id,enable,name,descr,updatetime\",\"forms\":{\"type\":3,\"enterprise_code\":\"1001\"}}", 20, null, null);


    }

    public void testTestQueryData() {
        //Configs.Put("defaultDatabase1","unionpay_sysframe");
//        DataHub.TemplateTableName="tmcp_ladder.sys_data";
        DataHub.TemplateTableName="tmcp_ladder.sys_data";

        //Record data = Record.parse(FileUtil.readString("D:\\1.json", StandardCharsets.UTF_8));
//
//        AjaxResult ret = QueryAction.queryData(data.getString("tableName"), data.getString("condition"), 6, null, null);
//
//        return;

        //QueryAction.queryData("*sys_user", "{\"pager\":{\"pageNumber\":2,\"pageSize\":10,\"pageCount\":2,\"recordCount\":15,\"pageoffset\":-1}}", 10, null, null);
        //QueryAction.queryData("*sys_user", "{\"pager\":{\"pageNumber\":2,\"pageSize\":10,\"pageCount\":2,\"recordCount\":15,\"pageoffset\":-1,\"field\":\"\",\"first\":true,\"last\":false,\"offset\":0}}", 10, null, null);

        //QueryAction.queryData("*sys_user", "{\"forms\":\"{\\\"pro_code\\\":null}\"}", 15, null, null);


//        Cnd cnd = new Cnd("delete_flag", 0);
//        QueryAction.getPageData("sys_info_type", Integer.parseInt("1"),Integer.parseInt("2"), Integer.parseInt("10"), cnd, "", "", 0);












    }

    public void testGetRecord() {
    }

    public void testGetObject() {
    }

    public void testGetBean() {
    }

    public void testTestGetBean() {
    }

    public void testTestGetBean1() {
    }

    public void testTestGetBean2() {
    }

    public void testTestGetBean3() {
    }

    public void testGetValue() {
    }

    public void testTestGetValue() {
    }

    public void testGetValues() {
    }

    public void testTestGetValues() {
    }

    public void testGetCount() {
        DataHub.TemplateTableName="tmcp_ladder.sys_data";
        long actionCount = QueryAction.getCount("unionpay_resourceIsHavePermission",
                new Cnd("menu.enable", "1")
                        //.put("maping.`enable`", '1')
//                        .put("permission.`enable`", '1').put("member.`enable`", '1')
                .put("member.membervalue","GLY1591605954000").put("menu.params", "edit").put("menu.parent", "4400dee2e19d4705b3bf98747cf3e849"), "");
        System.out.println(actionCount);
    }

    public void testHandleQueryResult() {
    }

    public void testTestHandleQueryResult() {
    }

    public void testSubActionQuery() {
    }

    public void testQuery() {
    }

    public void testQuerys() {
    }
}