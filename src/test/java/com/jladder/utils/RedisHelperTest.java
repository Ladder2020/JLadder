package com.jladder.utils;

import com.jladder.lang.Strings;
import junit.framework.TestCase;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisHelperTest extends TestCase {

    public void testPopCache() {
        Config config = new Config();
//        //config.useClusterServers().addNodeAddress("127.0.0.1:6379");
        String host = "127.0.0.1";
        String port = "6379";
        String database = "14";
        String password = "123456";
//
        config.useSingleServer().setAddress("redis://"+host+":"+(Strings.isBlank(port)?"6379":port)).setDatabase(Strings.isBlank(database)?0:Integer.valueOf(database)).setPassword(password);
        RedissonClient client = Redisson.create(config);
        RedisHelper.Instance.setRedissonClient(client);
        String ddd = RedisHelper.Instance.getString1("kk");
    }

    public void testTestPopCache() {



    }
}