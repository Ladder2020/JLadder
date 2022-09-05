package com.jladder.lang;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * 机器类
 */
public  class Machine {
    /**
     * 获取本地Ip地址
     * @return
     */
    public static String getLocalIp(){
        InetAddress ia=null;
        try {
            ia=ia.getLocalHost();
            return ia.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取本地主机名
     * @return
     */
    public static String getLocalName(){
        InetAddress ia=null;

        try {
            ia=ia.getLocalHost();
            return ia.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
    }
    /**
     * description 获取CPU序列号
     *
     * @return java.lang.String
     * @version 1.0
     * @date 2021/1/19 10:33
     */
    public static String getCpuId() {
        try{
            // linux，windows命令
            String[] linux = {"dmidecode", "-t", "processor", "|", "grep", "'ID'"};
            String[] windows = {"wmic", "cpu", "get", "ProcessorId"};

            // 获取系统信息
            String property = System.getProperty("os.name");
            Process process = Runtime.getRuntime().exec(property.contains("Window") ? windows : linux);
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream(), "utf-8");
            sc.next();
            return sc.next();
        }catch (Exception e){
            return "";
        }
    }
}
