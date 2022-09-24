package com.jladder.utils;

import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.lang.Collections;
import com.jladder.lang.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YiFeng
 * @date 2022年09月03日 14:41
 */
public class Telecom {
    /**
     * 短信
     */

    public final static int SMS=2;
    /**
     * 打电话
     */

    public final static int CALL=1;
    private final static Map<String,TelecomSend> coms = new HashMap<String,TelecomSend>();

    public final static String DefaultChannel="_default_";
    /**
     * 注册电信服务
     * @param channel 通道名称
     * @param fun 执行回调
     * @return void
     * @author YiFeng
     */
    public static void reg(String channel, TelecomSend fun){
        reg(0,channel,fun);
    }
    /**
     * 注册电信服务
     * @param way Telecom 类型
     * @param channel 通道名称 通道名称
     * @param fun 执行回调 执行回调
     * @return void
     * @author YiFeng
     */
    public static void reg(int way,String channel, TelecomSend fun){
        if(Strings.isBlank(channel))channel=DefaultChannel;
        for (String key : channel.split(",")) {
            if(Strings.hasValue(key))coms.put(way+key,fun);
        }

    }
    /**
     * 注册默认电信服务
     * @param way Telecom 类型
     * @param fun 执行回调 执行回调
     * @return void
     * @author YiFeng
     */
    public static void reg(int way,TelecomSend fun){
        coms.put(way+DefaultChannel,fun);
    }
    public static Receipt call(String phone,String content,Record data){
        return send(CALL,DefaultChannel,phone,content,data);
    }
    /**
     * 拨打电话
     * @param channel 通道
     * @param phone 手机号码
     * @param content 播放内容
     * @param data 扩展参数
     * @return com.jladder.data.Receipt
     * @author YiFeng
     */

    public static Receipt call(String channel,String phone,String content,Record data){
        return send(CALL,channel,phone,content,data);
    }
    public static Receipt sms(String phone,String content,Record data){
        return send(SMS,DefaultChannel,phone,content,data);
    }
    /**
     * 发送手机短信
     * @param channel 通道
     * @param phone 手机号码
     * @param content 短信内容
     * @param data 扩展内容
     * @return com.jladder.data.Receipt
     * @author YiFeng
     */

    public static Receipt sms(String channel,String phone,String content,Record data){
        return send(SMS,channel,phone,content,data);
    }
    public static Receipt send(int way,String phone,String content,Record data){
        return send(way,DefaultChannel,phone,content,data);
    }
    public static Receipt send(String channel,String phone,String content,Record data){
        return send(0,channel,phone,content,data);
    }
    /**
     * 发送手机短信
     * @param channel 通道
     * @param phone 手机号码
     * @param content 短信内容
     * @param data 扩展内容
     * @return com.jladder.data.Receipt
     * @author YiFeng
     */

    public static Receipt send(int way,String channel,String phone,String content,Record data){
        if(Strings.isBlank(channel))channel=DefaultChannel;
        String key = Collections.haveKey(coms,way+channel,0+channel);
        if(data==null)data=new Record();
        if(Strings.hasValue(key)){
            try {
                return coms.get(key).send(phone,content,data);
            } catch (Exception e) {
                e.printStackTrace();
                return new Receipt(e);
            }
        }
        else{
            return new Receipt(false,"通道未注册[028]");
        }
    }
    public interface TelecomSend{
        Receipt send(String phone,String content,Record data);
    }
}
