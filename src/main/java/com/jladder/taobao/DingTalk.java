package com.jladder.taobao;

import com.jladder.data.Receipt;
import com.jladder.data.Record;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.jladder.net.HttpHelper;
import sun.misc.BASE64Encoder;
import java.net.URLEncoder;

public class DingTalk {
    public static Receipt SendTextMessageByRobot(String token, String secret, String content)
    {
        try{
            Long timestamp = System.currentTimeMillis();


            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = URLEncoder.encode(new String(new BASE64Encoder().encode(signData)),"UTF-8");
            System.out.println(sign);

            String msg = new Record("msgtype", "text").put("text", new Record("content", content)).toString();
            Receipt<String> ret = HttpHelper.RequestByJson("https://oapi.dingtalk.com/robot/send?access_token=" + token + "&timestamp=" + timestamp + "&sign=" + sign, msg, new Record("timestamp", timestamp).put("sign", sign));

            if (!ret.result) return ret;
            Record record = Record.parse(ret.data);
            return record.getString("errcode")=="0" ? new Receipt() : ret;
        }catch (Exception e){
            return new Receipt(false);
        }


    }
}
