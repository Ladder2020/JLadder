package com.jladder.weixin;

import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.lang.*;
import com.jladder.net.http.HttpHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/// <summary>
/// 微信客户端
/// </summary>
public class WeiXinClient
{
    /// <summary>
    /// AppId
    /// </summary>
    public String AppId;
    /// <summary>
    /// AppSecret
    /// </summary>
    public String AppSecret = "";
    /// <summary>
    /// 失效时间
    /// </summary>
    private Date ExpireTime = Times.MinValue;
    /// <summary>
    /// Access_Token
    /// </summary>
    private String Token = "";
    /// <summary>
    ///
    /// </summary>
    private String Pay_MCH="";
    /// <summary>
    ///
    /// </summary>
    private String Pay_Key="";

    private Map<String, String> dic = new HashMap<String, String>();

    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="appid">Appid</param>
    /// <param name="appsecret">密令</param>
    public WeiXinClient(String appid, String appsecret)
    {
        AppId = appid;
        AppSecret = appsecret;
    }
    /// <summary>
    /// 设置支付密钥
    /// </summary>
    /// <param name="key">支付密钥</param>
    /// <returns></returns>
    public WeiXinClient SetPayKey(String key)
    {
        Pay_Key = key;
        return this;
    }
    /// <summary>
    /// 设置支付商户号
    /// </summary>
    /// <param name="mch">商户号</param>
    /// <returns></returns>
    public WeiXinClient SetPayMch(String mch)
    {
        Pay_MCH = mch;
        return this;
    }
    /// <summary>
    /// 设置全局推送地址
    /// </summary>
    /// <param name="url"></param>
    /// <returns></returns>
    public WeiXinClient SetNotifyUrl(String url)
    {
        dic.put("NotifyUrl", url);
        return this;
    }
    /// <summary>
    /// 设置IP地址
    /// </summary>
    /// <param name="ip">IP地址</param>
    /// <returns></returns>
    public WeiXinClient SetIp(String ip)
    {
        dic.put("ip", ip);
        return this;
    }
    /// <summary>
    /// 放置键值
    /// </summary>
    /// <param name="key">键名</param>
    /// <param name="value">值</param>
    /// <returns></returns>
    public WeiXinClient Put(String key, String value)
    {
        dic.put(key, value);
        return this;
    }
    /// <summary>
    /// 设置证书路径
    /// </summary>
    /// <param name="path"></param>
    /// <returns></returns>
    public WeiXinClient SetCertPath(String path)
    {
        dic.put("cert_path", path);
        return this;
    }
    /// <summary>
    /// 设置证书密码
    /// </summary>
    /// <param name="password">证书密码</param>
    /// <returns></returns>
    public WeiXinClient SetCertPassword(String password)
    {
        dic.put("cert_password", password);
        return this;
    }
    /// <summary>
    /// 获取支付的密钥
    /// </summary>
    /// <returns></returns>
    public String GetPayKey()
    {
        return Pay_Key;
    }

    /// <summary>
    /// 获取商户号
    /// </summary>
    /// <returns></returns>
    public String GetPayMch()
    {
        return Pay_MCH;
    }
    /// <summary>
    /// 支付结果通知回调url，用于商户接收支付结果
    /// </summary>
    /// <returns></returns>
    public String GetNotifyUrl()
    {
        return dic.get("NotifyUrl");
    }
    /// <summary>
    /// 获取APPID
    /// </summary>
    /// <returns></returns>
    public String GetAppId()
    {
        return AppId;
    }
    /// <summary>
    /// 获取应用的口令
    /// </summary>
    /// <returns></returns>
    public String GetAppSecret()
    {
        return AppSecret;
    }

    /// <summary>
    /// 获取用户IP
    /// </summary>
    /// <returns></returns>
    public String GetIp(){
        if(Strings.hasValue(dic.get("ip")))return dic.get("ip");
        return HttpHelper.getIp();
    }
    /// <summary>
    /// 获取证书路径
    /// </summary>
    /// <returns></returns>
    public String GetCertPath()
    {
        return null;
        //return dic.get("cert_path") ?? Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "cert/"+ Pay_MCH+".p12"));
    }
    /// <summary>
    /// 获取证书的密码
    /// </summary>
    /// <returns></returns>
    public String GetCertPassword()
    {
        if(Strings.hasValue(dic.get("cert_password") ))return dic.get("cert_password");
        return Pay_MCH;
    }
    /// <summary>
    /// 获取AccessToken
    /// </summary>
    /// <returns></returns>
    public String GetAccessToken()
    {
        if (ExpireTime.getTime() > new Date().getTime() && Strings.hasValue(Token)) return Token;
        else
        {

            Record ret = Record.parse(HttpHelper.get("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + AppId + "&secret=" + AppSecret));

            if (!ret.containsKey("errcode") ||  ret.getString("errcode") == "0")
            {
                ExpireTime = Times.addSecond(ret.getInt("expires_in"));
                Token = ret.getString("access_token");
                return Token;
            }else{
                Core.makeThrow(ret.toString());
            }
        }
        return "";
    }
    /// <summary>
    /// 获取用户基本信息
    /// </summary>
    /// <param name="openid"></param>
    /// <returns></returns>
    public Receipt<Record> GetUserInfo(String openid)
    {
        String token = GetAccessToken();
        if (Strings.isBlank(token)) return new Receipt(false);
        Record ret = Record.parse(HttpHelper.get("https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + token + "&openid=" + openid + "&lang=zh_CN"));

        if (ret!=null && !ret.containsKey("errcode") || ret.getString("errcode") == "0")
        {
            return new Receipt<Record>().setData(ret);
        }
        return new Receipt(false);
    }
    /// <summary>
    /// 发送消息
    /// </summary>
    /// <param name="openid">OpenID</param>
    /// <param name="content">发送内容</param>
    /// <returns></returns>
    public Receipt sendTextMessage(String openid,String content)
    {
        String token = GetAccessToken();
        if (Strings.isBlank(token)) return new Receipt(false);
        Record message = new Record("touser", openid).put("msgtype","text").put("text",new Record("content", content));
        Receipt<String> req = HttpHelper.requestByJson("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + token, message, null);
        Record ret = Record.parse(req.getData());
        return new Receipt();
    }
    /// <summary>
    /// 发送模型消息
    /// </summary>
    /// <param name="temid"></param>
    /// <param name="touser"></param>
    /// <param name="data"></param>
    public void SendTemplete(String temid, String touser,String url, Object data)
    {
        String  token = GetAccessToken();
        String postUrl = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", token);
        Record msgData = new Record("touser",touser).put("template_id",temid).put("data",data).put("url",url);
        String j = Json.toJson(msgData);
        Receipt<String> res = HttpHelper.requestByJson(postUrl, j, null);
    }


    /// <summary>
    /// 创建二维码
    /// </summary>
    /// <param name="data">携带数据</param>
    /// <param name="expire_seconds">失效时间</param>
    /// <returns></returns>
    public Receipt<Record> CreateQrCode(String data, int expire_seconds)
    {
        String token = GetAccessToken();
        if (Strings.isBlank(token)) return new Receipt(false);
        Record message = new Record("expire_seconds",expire_seconds).put("action_name", "QR_STR_SCENE").put("action_info", new Record("scene",new Record("scene_str",data)));
        Receipt<String> req = HttpHelper.requestByJson("https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + token, message, null);
        Record ret = Record.parse(req.getData());
        if (!ret.containsKey("errcode") || ret.getString("errcode") == "0")
        {
            return new Receipt<Record>().setData(ret);
        }
        return new Receipt<>(false);
    }
    /// <summary>
    /// 小程序通过Code换取openid等信息
    /// </summary>
    /// <param name="code"></param>
    /// <returns></returns>
    public Receipt<Record> CodeToSession(String code)
    {
        String  url ="https://api.weixin.qq.com/sns/jscode2session?appid="+AppId+"&secret="+AppSecret+"&js_code="+code+"&grant_type=authorization_code";

        Record ret = Record.parse(HttpHelper.get(url));

        if (ret != null && !ret.containsKey("errcode") || ret.getString("errcode") == "0")
        {
            return new Receipt<Record>().setData(ret);
        }

        return new Receipt<>(false);

    }


    public int GetReportLevel()
    {
        return 0;
    }

    public static WeiXinClient Create(Record config)
    {
        WeiXinClient wx = new WeiXinClient(config.getString("appid,app_id", true), config.getString("appsecret,app_secret,secret"));
        String c = config.getString("cert_path");
        if(Strings.hasValue(c))wx.SetCertPath(c);
        c = config.getString("cert_password");
        if (Strings.hasValue(c)) wx.SetCertPassword(c);
        c = config.getString("mchid,mch,mch_id,pay_mch,pay_mch_id");
        wx.SetPayMch(c);
        c = config.getString("paykey,key,pay_key");
        wx.SetPayKey(c);
        c = config.getString("notify,notify_url");
        wx.SetNotifyUrl(c);
        return wx;

    }
}
