package com.jladder.lang;

import com.jladder.data.Receipt;
import com.jladder.data.Record;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

public class Security {
    public static String md5(String source){
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    source.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }


    public static  Receipt<Record> encryptByHead(String tag,Record data,String sign, String key){
        return encryptByHead(tag,data,sign,key,"",0,new Record());
    }

    /***
     * 以head头方式进行加密
     * @param data 请求数据
     * @param sign 用户标识
     * @param key 密钥
     * @param tag 标签
     * @param version 版本
     * @param envcode 运行环境
     * @param header 请求头
     * @return
     */
    public static  Receipt<Record> encryptByHead(String tag,Record data,String sign, String key, String version, int envcode, Record header){
        if (Strings.isBlank(key)) return new Receipt<Record>("未指定密钥");
        if (header == null)
        {
            if (Strings.isBlank(sign) || Strings.isBlank(tag)) return new Receipt<Record>("sign或tag未指定");
            header = new Record();
            header.put("_sign_", sign);
            header.put("_tag_", tag);
            header.put("_debugging_", envcode);
            if (Strings.hasValue(version)) header.put("_version_", version);
        }
        else
        {
            if (Strings.hasValue(sign)) header.put("_sign_", sign);
            if (Strings.hasValue(tag)) header.put("_tag_", tag);
            if (Strings.isBlank(header.getString("_sign_")) || Strings.isBlank(header.getString("_tag_"))) return new Receipt<Record>("sign或tag未指定");
            if (Strings.hasValue(sign)) header.put("_sign_", sign);
            if (Strings.hasValue(tag)) header.put("_tag_", tag);
            header.put("_debugging_", envcode);
            if (Strings.hasValue(version)) header.put("_version_", version);
        }
        header.put("_timestamp_", Times.timestamp());
        header = header.match("_sign_,_token_,_asyn_,_debugging_,_timestamp_,_tag_,_device_,_app_");
        Record raw = header.clone();
        String  s = header.getString("_sign_");
        header.merge(data);
        header.put("_key_", key);
        header.delete("_sign_", "_token_");

        String[] keys = header.keySet().stream().sorted(Comparator.comparing(x -> x.toString())).toArray(String[]::new);
        StringBuilder query = new StringBuilder();
        for(String k:keys){
            if(Strings.isBlank(k))continue;
            String v = (header.get(k)== null ? "" : Json.toJson(header.get(k)));
            query.append(k).append("=").append(v);
        }
        String unSecret = query.toString();
        System.out.println("Security_Raw:"+unSecret);
        String secret = md5(unSecret);
        raw.delete("_key_").put("_sign_", s).put("_token_", secret);
        Receipt<Record> ret = new Receipt<Record>(true, unSecret);
        ret.setData(raw);
        return ret;
    }

    public static String decryptByBase64(String data){
        try {
            return new String((new BASE64Decoder()).decodeBuffer(data),StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String encryptByBase64(String data){
        return (new BASE64Encoder()).encode(data.getBytes(StandardCharsets.UTF_8));
    }

}
