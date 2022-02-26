package com.jladder.lang;

import com.jladder.data.Record;
import junit.framework.TestCase;

public class CoreTest extends TestCase {

    public void testGenUuid() {
        Record dic = new Record();
        String ip = Machine.getLocalIp();
        int wrokId=  R.random(0,31);
        int datacenterId = R.random(0,31);
        if(Strings.hasValue(ip)){
            String[] ips = ip.split("\\.");
            int last1 = Convert.toInt(ips[ips.length-1]);
            int last2 = Convert.toInt(ips[ips.length-2]);
            wrokId=last1 % 31;
            datacenterId=last2 % 31;
        }
        int i = 0;
        while (true){
            i++;
            String nuid = new SnowFlake(wrokId,datacenterId).nextId()+"";
            if(dic.containsKey(nuid)){
                System.out.println(i+"|"+nuid);
            }else {
                dic.put(nuid,true);
            }

        }
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