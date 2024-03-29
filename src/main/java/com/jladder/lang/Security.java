package com.jladder.lang;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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
        int len = md5code.length();
        for (int i = 0; i < 32 - len; i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

    public static String md5(byte[] data){
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(data);
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

    /**
     * 解密Base64
     * @param data 源文本数据
     * @return
     */
    public static String decryptByBase64(String data){
        try {
            return new String(Base64.getDecoder().decode(data),StandardCharsets.UTF_8);
           // return new String((new BASE64Decoder()).decodeBuffer(data),StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String encryptByBase64(String data){
       // return (new BASE64Encoder()).encode(data.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String encryptByBase64(byte[] data){
        return new String(Base64.getEncoder().encode(data));
    }

    public static String encryptByBase2(String source)
    {
        if (Strings.isBlank(source)) return "";
        String base64Str = encryptByBase64(source);
        int position = R.random(0, source.length()>9 ? 9 : source.length());
        int len = R.random(0, 5);
        String md5 = md5(base64Str + position + len) + "qwerrttyuioplkjhgfgddasszxxccvvbnnm963257411";
        return len == 0 ? position +""+ len + base64Str : position+ "" + len + base64Str.substring(0, position) + md5.substring(0, len) +base64Str.substring(position);
    }
    public static String decryptByBase2(String source)
    {
        if (Strings.isBlank(source) || !Regex.isMatch(source, "^\\d{2}")) return null;
        try {
            int position = Convert.toInt(source.substring(0, 1));
            int len = Convert.toInt(source.substring(1, 2));
            String data = source.substring(2);
            if (len == 0) return decryptByBase64(data);
            String raw = data.substring(0, position) + data.substring(position + len);
            return decryptByBase64(raw);
        }catch (Exception e){
            return null;
        }
    }
    /**
     * 生成key
     *
     * @param password
     * @return
     * @throws Exception
     */
    private static Key generateKey(String password) throws Exception {
        DESKeySpec dks = new DESKeySpec(password.getBytes("utf-8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance( "DES/CBC/PKCS5Padding");
        return keyFactory.generateSecret(dks);
    }

    /**
     * DES加密字符串
     *
     * @param password 加密密码，长度不能够小于8位
     * @param data 待加密字符串
     * @return 加密后内容
     */
    public static String encryptByDES(String password, String data) {
        if (password== null || password.length() < 8) {
            throw new RuntimeException("加密失败，key不能小于8位");
        }
        if (data == null)
            return null;
        try {
            Key secretKey = generateKey(password);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec("12345678".getBytes("utf-8"));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] bytes = cipher.doFinal(data.getBytes("utf-8"));
            //JDK1.8及以上可直接使用Base64，JDK1.7及以下可以使用BASE64Encoder
            //Android平台可以使用android.util.Base64
            return new String(Base64.getEncoder().encode(bytes));

        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }
    /**
     * DES解密字符串
     *
     * @param password 解密密码，长度不能够小于8位
     * @param data 待解密字符串
     * @return 解密后内容
     */
    public static String decryptByDES(String password, String data) {
        if (password== null || password.length() < 8) {
            throw new RuntimeException("加密失败，key不能小于8位");
        }
        if (data == null)
            return null;
        try {
            Key secretKey = generateKey(password);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec("12345678".getBytes("utf-8"));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes("utf-8"))), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }


}
