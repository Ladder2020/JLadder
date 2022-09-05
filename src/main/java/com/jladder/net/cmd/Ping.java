package com.jladder.net.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Ping {
    public static boolean cmd(String ip, int pingTimes, int timeOut) {
        BufferedReader in = null;
        String pingCommand = null;
        Runtime r = Runtime.getRuntime();
        String osName = System.getProperty("os.name");
        if(osName.contains("Windows")) {
            //-n:要发送的回显请求数   -w：每次请求的超时时间
            pingCommand = "ping " + ip + " -n " + pingTimes + " -w " + timeOut;
        }else {
            //linux下： -c是要发送的回显请求数，没有每次请求超时时间
            pingCommand = "ping " + " -c " + pingTimes + " " + ip;
        }
        try {
            Process p = r.exec(pingCommand);
            if(p == null) {
                return false;
            }
            //ping命令使用的是GBK编码
            in = new BufferedReader(new InputStreamReader(p.getInputStream(),"GBK"));
            int connectCount = 0;
            String line = null;
            while((line = in.readLine()) != null) {
                connectCount += getCheckResult(line,osName);
            }
            //只要ping通一次就说明连接成功？
            return connectCount > 0 ;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    //若含有ttl=64字样,说明已经ping通,返回1,否則返回0.
    private static int getCheckResult(String line, String osName) {
        if(osName.contains("Windows")) {
            if(line.contains("TTL")) {
                return 1;
            }
        }else {
            if(line.contains("ttl")) {
                return 1;
            }
        }
        return 0;
    }
}
