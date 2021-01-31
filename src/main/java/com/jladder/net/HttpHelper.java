package com.jladder.net;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.lang.Json;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import okhttp3.*;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class HttpHelper {
    static private OkHttpClient OkClient = new OkHttpClient();;

    private static final MediaType URLENCODED = MediaType.parse("application/x-www-form-urlencoded;charset=utf-8");
    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");

    public static String getIp() {
        return "";
    }

    public static String toFormData(Object data) {
        return toFormData(data, false);
    }

    public static String toFormData(Object data, boolean encoding) {
        final String[] postData = {""};
        boolean ok = false;
        if (data instanceof String) {
            ok = true;
            String poststr = data.toString().trim();
            if (Strings.isJson(poststr, 1)) {
                Record record = Record.parse(poststr);
                if (record != null) {
                    record.forEach((k, v) -> {
                        postData[0] += k + "=" + (encoding ? UriEncoder.encode(v.toString()) : v) + "&";
                    });
                }
                postData[0] = Strings.rightLess(postData[0], 1);
            } else {
                postData[0] = poststr;
            }
        }
        if (ok) return postData[0];
        {
            Record record = Record.parse(data);
            if (record != null) {
                record.forEach((k, v) -> {
                    postData[0] += k + "=" + v + "&";
                });
            }
            postData[0] = Strings.rightLess(postData[0], 1);
        }
        return postData[0];
    }


    public static String encode(String data) {
        try {
            return URLEncoder.encode(data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String decode(String data) {
        try {
            return URLDecoder.decode(data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Receipt<String> RequestByJson(String url, Object data, Record header) {
        try {
            Request.Builder request = new Request.Builder().url(url);
            if (data != null) {
                if (data instanceof String) {
                    request.post(RequestBody.create(JSON,data.toString()));
                } else {
                    request.post(RequestBody.create(JSON, Json.toJson(data)));
                }
            }
            if (header != null && header.size() > 0) {
                header.forEach((k, v) -> {
                    if (Regex.isMatch(k, "(accept)|(connection)|(content-length)|(content-type)|(host)|(expect)|(if-modified-since)|(accept-encoding)"))
                        return;
                    String value = v.toString();
                    value = encode(value);//处理中文
                    //以_name_形式，为Ladder类库增加
                    if (Regex.isMatch(k, "_(\\w*)_")) {
                        request.header(k, Json.toJson(value));
                        return;
                    }
                    switch (k.toLowerCase()) {
    //                    case "user-agent":
    //                        //request.UserAgent = UrlEncode(x.Value.ToString());
    //                        break;
    //                    case "referer":
    //                    case "if-modified-since":
    //                        request.referer = value;
    //                        break;
    //                    case "cookie":
    //                        break;
    //                        if (cookies.IsBlank()) request.Headers.Add("Cookie", value);
    //                        break;
    //                    case "accept-encoding":
    //                        break;
    //                    case "x-forwarded-for":
    //                        break;
                        default:
                            request.header(k, Json.toJson(value));
                            break;
                    }
                    //request.header(k,v==null?"":v.toString());
                });
            }
            Call call = OkClient.newCall(request.build());
            Response response = call.execute();
            if (response.isSuccessful()) {
                return new Receipt<String>(true).setData(response.body().string());
            }else{
                return new Receipt<String>(false).setData(response.code()+"["+response.body().toString()+"]");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Receipt<String>(false,e.getMessage());
        }

    }
    public static Receipt<String> request(String url, Object data){
        return request(url,data,"POST",null);
    }
    public static Receipt<String> request(String url, Object data, String method, Record header) {
        if (Strings.isBlank(method)) method = "post";
        try {
            String bodyString = toFormData(data);
            Request.Builder request = new Request.Builder().url(url);
            if(Regex.isMatch(method,"post")){
                RequestBody body = RequestBody.create(URLENCODED,bodyString);
                request.post(body);
            }else{

                request.url(url+(url.contains("?")?"&"+bodyString:"?"+encode(bodyString))).get();
            }
            if (header != null && header.size() > 0) {
                header.forEach((k, v) -> {
                    if (Regex.isMatch(k, "(accept)|(connection)|(content-length)|(content-type)|(host)|(expect)|(if-modified-since)|(accept-encoding)"))
                        return;
                    String value = v.toString();
                    value = encode(value);//处理中文
                    //以_name_形式，为Ladder类库增加
                    if (Regex.isMatch(k, "_(\\w*)_")) {
                        request.header(k, Json.toJson(value));
                        return;
                    }
                    switch (k.toLowerCase()) {
    //                    case "user-agent":
    //                        //request.UserAgent = UrlEncode(x.Value.ToString());
    //                        break;
    //                    case "referer":
    //                    case "if-modified-since":
    //                        request.referer = value;
    //                        break;
    //                    case "cookie":
    //                        break;
    //                        if (cookies.IsBlank()) request.Headers.Add("Cookie", value);
    //                        break;
    //                    case "accept-encoding":
    //                        break;
    //                    case "x-forwarded-for":
    //                        break;
                        default:
                            request.header(k, Json.toJson(value));
                            break;
                    }
                    //request.header(k,v==null?"":v.toString());
                });
            }
            //创建"调用" 对象
            Call call = OkClient.newCall(request.build());
            Response response = call.execute();//执行
            if (response.isSuccessful()) {
                return new Receipt<String>(true).setData(response.body().string());
                //System.out.println(response.body().string());
            }else{
                return new Receipt<String>(false).setData(response.code()+"["+response.body().string()+"]");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Receipt<String>(false).setData(e.toString());
        }
    }

    public static String getResource(String url) {
        String uri;
        int index = url.indexOf("?");
        if (index > -1) url = url.substring(0, index);
        if (Regex.isMatch(url, "^\\s*http")) {
            index = url.indexOf("/", 9);
            uri = index > -1 ? url.substring(index + 1) : "";
        } else {
            url = Regex.replace(url, "^\\s*(\\.\\./)*", "");
            uri = url;
        }
        return uri;
    }

    public static Object post(String url, Record data) {
        return request(url, data, "POST", null).data;
    }


    /**
     * 网址转IP
     *
     * @param url
     * @return
     */
    public static String toIp(String url) {
        if(url==null)return null;
        try {
            java.net.URL address = new  java.net.URL(url);
            return InetAddress.getByName(address.getHost()).getHostAddress();
        } catch (Exception e) {
            return null;
        }


    }
}