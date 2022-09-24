package com.jladder.net;

import com.jladder.actions.impl.QueryAction;
import com.jladder.actions.impl.SaveAction;
import com.jladder.data.AjaxResult;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.lang.Times;
import com.jladder.web.WebContext;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @author YiFeng
 * @date 2022年09月14日 22:41
 */

public class Urls {
    public static String Domain="";
    public static final Map<String,Compress> compresses = new HashMap<String,Compress>();
    public static String Table_Short="data_shorturl";
    public static void reg(Compress compress){
        compresses.put("_default_",compress);
    }
    public static void reg(String channel,Compress compress){
        compresses.put(channel,compress);
    }
    public static Receipt<String>  compress(String channel,String url,String method,Map<String,Object> data){
        Compress compress = compresses.get(channel);
        if(compress==null)return new Receipt<String> (false,"未注册[024]");
        return compress.compress(url,method,data);

    }
    public static Receipt<String> compress(String channel,String url){
        Compress compress = compresses.get(channel);
        if(compress==null)return new Receipt<String> (false,"未注册[024]");
        return compress.compress(url,"GET",new Record());
    }
    public static Receipt<String>  compress(String url,Map<String,Object> data){
        Compress compress = compresses.get("_default_");
        if(compress==null)return new Receipt<String> (false,"未注册[024]");
        return compress.compress(url,"GET",data);
    }
    public static Receipt<String>  compress(String url){
        Compress compress = compresses.get("_default_");
        if(compress==null)return new Receipt<String> (false,"未注册[024]");
        return compress.compress(url,"GET",new Record());
    }
    /**
     * 创建短链接接口
     * key：分配给注册用户的开发者密钥，可以根据该值对用户的创建短链接数量进行限制；
     * original_url：需要生成短链接的原始URL；
     * title ：用户对于URL自定义的名称；
     * username ：可以用在编码中的用户名；
     * hour ：短链接的时间；
     *
     * @return
     */
    public static Receipt<String> compress(String url, String method, Map<String,Object> data, Date expire) {
        String code = null;
        try {
            Record bean = new Record();
            do {
                //base64转码
                String stringBase64 = Base64.getEncoder().encodeToString((url).getBytes());
                //去掉==
                stringBase64 = stringBase64.substring(0, stringBase64.length() - 2);
                // 利用md5生成32位固长字符串
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] result = md5.digest(stringBase64.getBytes());
                //要使用生成URL的字符
                String[] chars = new String[]{
                        "a", "b", "c", "d", "e", "f", "g", "h",
                        "i", "j", "k", "l", "m", "n", "o", "p",
                        "q", "r", "s", "t", "u", "v", "w", "x",
                        "y", "z", "0", "1", "2", "3", "4", "5",
                        "6", "7", "8", "9", "A", "B", "C", "D",
                        "E", "F", "G", "H", "I", "J", "K", "L",
                        "M", "N", "O", "P", "Q", "R", "S", "T",
                        "U", "V", "W", "X", "Y", "Z"
                };
                String hex = "";
                for (int i = 0; i < result.length; i++) {
                    //与0xFF做“&”运算，是因为byte转int，正数存储的二进制原码,负数存储的是二进制的补码。
                    //补码是负数的绝对值反码加1。
                    String hs = Integer.toHexString(result[i] & 0xFF);
                    if (hs.length() == 1) {
                        hex = hex + "0" + hs;
                    } else {
                        hex = hex + hs;
                    }
                }

                int hexLen = hex.length();
                int subHexLen = hexLen / 8;
                String[] ShortStr = new String[4];
                for (int i = 0; i < subHexLen; i++) {
                    String outChars = "";
                    int j = i + 1;
                    String subHex = hex.substring(i * 8, j * 8);
                    long idx = Long.valueOf("3FFFFFFF", 16) & Long.valueOf(subHex, 16);

                    for (int k = 0; k < 6; k++) {
                        int index = (int) (Long.valueOf("0000003D", 16) & idx);
                        outChars += chars[index];
                        idx = idx >> 5;
                    }
                    ShortStr[i] = outChars;
                }
                Random random1 = new Random();
                Random random2 = new Random();
                int t1 = random1.nextInt(4);
                int t2 = random2.nextInt(4);
                code = ShortStr[t1] + ShortStr[t2];
                long y = QueryAction.getCount(Table_Short, new Record("code", code));
                if (y == 0) {
                    bean.put("code", code);
                    bean.put("url", url);
                    bean.put("expire",Times.timestamp(expire,10));
                    bean.put("method",method);
                    bean.put("data",data);
                }

            } while (Record.isBlank(bean));
            AjaxResult ret = SaveAction.insert(Table_Short, bean);
            return ret.toReceipt().setData((Strings.isBlank(Urls.Domain)?WebContext.getHost():Urls.Domain)+"/"+code);
        } catch (Exception ex) {
            return Receipt.create(ex);
        }
    }
    public interface Compress{
        Receipt<String> compress(String url, String method, Map<String,Object> data);
    }
}
