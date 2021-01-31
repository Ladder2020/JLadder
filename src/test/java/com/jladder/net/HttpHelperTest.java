package com.jladder.net;

import com.jladder.data.Record;
import junit.framework.TestCase;

public class HttpHelperTest extends TestCase {

    public void testDnToIp() {
        System.out.println(HttpHelper.request("http://www.baidu.com",new Record("s","b1c40679-1e49-11eb-bb85-6c2b59bec1ff"),"post",null).data);

    }
}