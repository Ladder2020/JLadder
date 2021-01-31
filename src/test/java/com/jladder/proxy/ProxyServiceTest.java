package com.jladder.proxy;

import com.jladder.configs.Configs;
import com.jladder.data.AjaxResult;
import com.jladder.data.Record;
import com.jladder.hub.DataHub;
import com.jladder.lang.Json;
import junit.framework.TestCase;

public class ProxyServiceTest extends TestCase {



    public void testTestExecute() {
    }

    public void testGetProxyConfig() {

        DataHub.TemplateTableName="ladder.sys_data";
        Configs.put("defaultSchema","ladder");
        //AjaxResult ret0 = QueryAction.getData("sys_settings");
        ProxyConfig config = ProxyService.getProxyConfig("LeNiao.Breaker.Close", "1.0");
        AjaxResult ret = ProxyService.Execute(config, new Record("dss", 11), 0, new Record(), null, null);
        System.out.println(Json.toJson(ret));
    }

    public void testExecute() {

        AjaxResult ret = ProxyService.Execute("http://localhost:9901/paas", "Comm.SendSMS", new Record("number", "18543014989").put("sign", "易惠科技").put("content", "9635")
                ,"1488957880908","696e7d69d46042f1b4b5bdc7be7a4f5a", "", "POST", 0, new Record());
        System.out.println(Json.toJson(ret));
    }
}