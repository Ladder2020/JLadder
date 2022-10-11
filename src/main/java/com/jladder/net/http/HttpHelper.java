package com.jladder.net.http;

import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.data.UploadFile;
import com.jladder.lang.*;
import com.jladder.lang.func.Action1;
import com.jladder.lang.func.Action2;
import com.jladder.web.WebContext;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpHelper{
    private static final OkHttpClient OkClient = setSSL(new OkHttpClient.Builder()
            .connectTimeout(60 , TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES))
            //.hostnameVerifier(new TrustAllHostnameVerifier())//trust all?信任所有host
            .build();
    private static final MediaType URLENCODED = MediaType.parse("application/x-www-form-urlencoded;charset=utf-8");
    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    private static final MediaType MutilPart_Form_Data = MediaType.parse("multipart/form-data; charset=utf-8");

    /**
     * 获取客户端IP
     * @return 当前请求的Ip
     */
    public static String getIp(){
        return getIp(WebContext.getRequest());
    }

    /**
     * 获取客户端IP
     * @param request 请求头
     * @return  客户端IP
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    public static String toFormData(Object data) {
        return toFormData(data, false);
    }

    /**
     * 转为formdata数据 a=1&b=3
     * @param data 源数据
     * @param encoding 是否URL编码
     * @return 客户端IP
     */
    public static String toFormData(Object data, boolean encoding) {
        final String[] postData = {""};
        boolean ok = false;
        if (data instanceof String) {
            ok = true;
            String poststr = data.toString().trim();
            if (Strings.isJson(poststr, 1)) {
                Record record = Record.parse(poststr);
                if (record != null) {
                    record.forEach((k, v) -> postData[0] += k + "=" + (encoding ? encode(v.toString()) : v) + "&");
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
                record.forEach((k, v) -> postData[0] += k + "=" + (encoding ? encode(v==null?"":v.toString()) : v)  + "&");
            }
            postData[0] = Strings.rightLess(postData[0], 1);
        }
        return postData[0];
    }
    /**
     * 编码数据
     * @param data 数据
     * @return 编码数据
     */
    public static String encode(String data) {
        try {
            return URLEncoder.encode(data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
    /**
     * 解码数据
     * @param data 数据
     * @return 解码数据
     */
    public static String decode(String data) {
        try {
            return URLDecoder.decode(data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 以PostJson方式请求
     * @param url 请求地址
     * @param data 请求数据
     * @param header 请求头
     * @param method 请求方式
     * @return
     */
    public static Receipt<String> requestByJson(String url, Object data, Record header,String method){
        return requestByJson(url,data,header,null,null,method);
    }
    public static Receipt<String> requestByJson(String url, Object data, Record header){
        return requestByJson(url,data,header,null,null,"post");
    }
    public static Receipt<String> requestByJson(String url, Object data, Record header, Action1<Request.Builder> onRequest,Action2<String,Response> onResponse){
        return requestByJson(url,data,header,onRequest,onResponse,"post");
    }
    public static Receipt<String> requestByJson(String url, Object data, Record header, Action1<Request.Builder> onRequest, Action2<String,Response> onResponse, String method) {
        try {
            Request.Builder request = new Request.Builder().url(url);
            if(Strings.isBlank(method))method="post";
            RequestBody json = null;
            if (data != null) {
                if (data instanceof String) {
                    json= RequestBody.create(JSON,data.toString());
                } else {
                    json= RequestBody.create(JSON, Json.toJson(data));
                }
            }
            switch (method.toLowerCase()){
                case "post":
                    request.post(json);
                    break;
                case "put":
                    request.put(json);
                    break;
                case "delete":
                    request.delete(json);
                    break;
                default:
                    request.get();
                    break;
            }
            //request.head().addHeader("Content-Type","application/json");
            if (header != null && header.size() > 0) {
                header.forEach((k, v) -> {
                    if (Regex.isMatch(k, "(accept)|(connection)|(content-length)|(content-type)|(host)|(expect)|(if-modified-since)|(accept-encoding)"))
                        return;
                    String value = v.toString();
                    //value = encode(value);//处理中文
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
            if(onRequest!=null)onRequest.invoke(request);
            Call call = OkClient.newCall(request.build());
            Response response = call.execute();
            String text =  response.body().string();
            if(onResponse!=null)onResponse.invoke(text,response);
            if (response.isSuccessful()) {
                return new Receipt<String>(true).setData(text);
            }else{
                return new Receipt<String>(false).setData(response.code()+"["+text+"]");
            }
        } catch (Exception e) {
            System.out.println("ladder-httperror:"+url+","+e.getMessage());
            return new Receipt<String>(false,e.getMessage()).setData(e.toString());
        }
    }
    public static Receipt<String> request(String url, Object data){
        return request(url,data,"POST",null);
    }
    public static Receipt<String> request(String url, Object data, String method){return request(url,data,method,null);}
    public static Receipt<String> request(String url, Object data, String method, Record header) {
        if(Strings.isBlank(url))return new Receipt<String>(false,"访问地址未填写");
        if (Strings.isBlank(method)) method = "post";
        try {
            String bodyString = toFormData(data,true);
//            if(url.startsWith("${host}")){
//                url.replace("${host}",WebContext.getHost());
//            }
            Request.Builder request = new Request.Builder().url(url);
            switch (method.toLowerCase()){
                case "post":
                    request.post(RequestBody.create(URLENCODED,bodyString));
                    break;
                case "put":
                    request.put(RequestBody.create(URLENCODED,bodyString));
                    break;
                case "delete":
                    request.delete(RequestBody.create(URLENCODED,bodyString));
                    break;
                default:
                    request.url(url+(url.contains("?")?"&"+bodyString:"?"+bodyString)).get();
                    break;
            }
            if (header != null && header.size() > 0) {
                header.forEach((k, v) -> {
                    if (Regex.isMatch(k, "(accept)|(connection)|(content-length)|(content-type)|(host)|(expect)|(if-modified-since)|(accept-encoding)"))
                        return;
                    if(v==null)return;
                    String value = v.toString();
                    //value = encode(value);//处理中文
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
//            try{
//                TrustManager[] trustAllCerts = buildTrustManagers();
//
//                final SSLContext sslContext = SSLContext.getInstance("SSL");
//
//                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//
//                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//
//
//            }catch (Exception e){
//
//            }


            //创建"调用" 对象
            Call call = OkClient.newCall(request.build());
            Response response = call.execute();//执行
            if (response.isSuccessful()) {
                return new Receipt<String>(true).setData(response.body().string());
                //System.out.println(response.body().string());
            }else{
                return new Receipt<String>(false).setData(response.body().string());
            }
        } catch (Exception e) {
            System.out.println("ladder-httperror:"+url+","+e.getMessage());
            return new Receipt<String>(false,e.getMessage()).setData(e.toString());
        }
    }
    public static Receipt<String> upload(String url, Collection<UploadFile> files, Map<String,Object> data){
        if (Core.isEmpty(files)) return new Receipt(false, "未有上传数据[204]");
        Map record = new HashMap<String, byte[]>();
        files.forEach(x -> record.put(x.getFormName() + ":" + x.getFileName(), x.getData()));
        return upload(url, record, data);
    }
    public static Receipt<String> upload(String url, Map<String,byte[]> files, Map<String,Object> data){
        if (Core.isEmpty(files)) return new Receipt(false, "未有上传数据[209]");
        try{
            MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            if(data!=null){
                data.forEach((k,v)->{
                    if(v!=null)
                        requestBodyBuilder.addFormDataPart(k,v.toString());
                });
            }
            files.forEach((k,v)->{
                String[] ks = k.split(":");
                String formName = Collections.first(ks);
                String filename = Collections.last(ks);
                requestBodyBuilder.addFormDataPart(formName,filename,RequestBody.create(MutilPart_Form_Data,v));
            });
            MultipartBody requestBody = requestBodyBuilder.build();
            Request request = new Request.Builder().url(url).post(requestBody).build();
            Response response = OkClient.newCall(request).execute();
            if (!response.isSuccessful()) return new Receipt<String>(false).setData(response.code()+"["+response.body().string()+"]");
            return new Receipt<String>().setData(response.body().string());

        }catch (Exception e){
            return new Receipt<String>(false,e.getMessage());
        }

    }

    /**
     * 下载文件
     * @param url 网络地址
     * @param filename 文件名称
     * @return
     */
    public static Receipt<String> downFile(String url,String filename){
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response =  OkClient.newCall(request).execute();
            InputStream is = null;
            byte[] buf = new byte[2048];
            int len = 0;
            FileOutputStream fos = null;
            File file = new File(filename);
            String parent = file.getParent();
            //储存下载文件的目录
            File dir = new File(parent);
            if (!dir.exists()) dir.mkdirs();
            try {
                is = response.body().byteStream();
                fos = new FileOutputStream(file);
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
                return new Receipt<String>();
                //下载完成
            } catch (Exception e) {
                return new Receipt<String>(false,e.getMessage());
            }finally {
                try {
                    if (is != null)  is.close();
                    if (fos != null) fos.close();
                } catch (IOException e) {}
            }
        } catch (IOException e) {
           return new Receipt<String>(false,e.getMessage());
        }
    }

    /**
     * 异步下载文件
     * @param url 网络路径
     * @param filename 文件名称
     */
    public static void downFileByAsync(String url,String filename){
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                String parent = new File(filename).getParent();
                //储存下载文件的目录
                File dir = new File(parent);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(filename);

                try {

                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        //下载中更新进度条
                        //listener.onDownloading(progress);
                    }
                    fos.flush();
                    //下载完成
                    //listener.onDownloadSuccess(file);
                } catch (Exception e) {
                    //listener.onDownloadFailed(e);
                }finally {

                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {

                    }

                }

            }
        });
    }

    /**
     * 获取服务主机地址
     * @param url 路径
     * @return
     */
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

    public static String post(String url, Record data) {
        return request(url, data, "POST", null).getData();
    }
    public static String get(String url){
        return request(url, null, "GET", null).getData();
    }
    public static String get(String url, Record data) {
        return request(url, data, "GET", null).getData();
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
    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override

                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override

                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override

                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};

                    }

                }

        };
    }
    private static OkHttpClient.Builder setSSL(OkHttpClient.Builder builder){
        try{
            TrustManager[] trustAllCerts = new TrustManager[1];
            TrustManager tm = new TrustAllCerts();
            trustAllCerts[0] = tm;
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            builder.sslSocketFactory(sc.getSocketFactory(), (X509TrustManager) tm);
            builder.hostnameVerifier((hostname,session)->true);
            return builder;
        }catch (Exception e){

        }
        return builder;
    }
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    private static class TrustAllCerts implements TrustManager,X509TrustManager {

        //checkServerTrusted和checkClientTrusted 这两个方法好像是用于，server和client双向验证
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}




