package com.jladder.web;

import com.jladder.data.Record;
import com.jladder.data.UploadFile;
import com.jladder.lang.Collections;
import com.jladder.lang.Core;
import com.jladder.lang.Strings;
import com.jladder.lang.func.Tuple2;
import com.jladder.logger.LogFoRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上下文环境
 */
public class WebContext {

    /**
     * 是否web环境
     * @return
     */
    public static boolean isWeb(){
        return getRequest()!=null;
    }

    /**
     * 获取请求头对象
     * @return
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        HttpServletRequest request = requestAttributes.getRequest();
        return request;
    }

    /**
     * 获取回复头对象
     * @return
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        HttpServletResponse res = requestAttributes.getResponse();
        return res;
    }
    public static Map<String, List<UploadFile>> getUploadFiles(){
        return getUploadFiles(getRequest());
    }
    public static Map<String, List<UploadFile>> getUploadFiles(HttpServletRequest request){
        try{
            MultipartHttpServletRequest mr = (MultipartHttpServletRequest) request;
            if(mr==null)return null;
            MultiValueMap<String, MultipartFile> filemap = mr.getMultiFileMap();
            Map<String,List<UploadFile>> ret = new HashMap<String,List<UploadFile>>();
            filemap.forEach((k, v) ->{
                try{
                    List<UploadFile> ups = new ArrayList<UploadFile>();
                    v.forEach(f->{
                        try{
                            ups.add(new UploadFile(f.getOriginalFilename(),f.getBytes(),k).SetLength(f.getSize()));
                        }catch (Exception e) { }
                    });
                    ret.put(k,ups);
                }catch (Exception e){}
            });
            return ret;
        }catch (Exception e){
            return null;
        }

    }

    public static HttpSession getSession() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        return requestAttributes.getRequest().getSession();
    }

    public static String getSessionStr(String name) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        HttpSession session = requestAttributes.getRequest().getSession();
        Object val = session.getAttribute(name);
        return val == null ? null : val.toString();
    }

    public static String getCookie(String name) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        Cookie[] cookiess = requestAttributes.getRequest().getCookies();
        if (null == cookiess) return null;
        Tuple2<Boolean, Cookie> ret = Collections.first(cookiess, x -> x.getName().equals(name));
        return ret.item1 ? ret.item2.getValue() : null;
    }

    public static void setCookiesForServer(String name, String value) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return;
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(true);
//        cookie.setHttpOnly(true);
        cookie.setPath("/");
        requestAttributes.getResponse().addCookie(cookie);
    }
    public static String getAttributeString(String name) {
        Object v = getAttribute(name);
        if(v==null)return null;
        return v.toString();
    }
    public static Object getAttribute(HttpServletRequest request,String name) {
        if (request == null) return null;
        Object val = request.getAttribute(name);
        if (null == val) return null;
        return val;
    }
    public static Object getAttribute(String name) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        HttpServletRequest request = requestAttributes.getRequest();
        if (request == null) return null;
        Object val = request.getAttribute(name);
        if (null == val) return null;
        return val;
    }
    public static boolean setAttribute(HttpServletRequest request,String name,Object value) {
        if (request == null) return false;
        request.setAttribute(name,value);
        return true;
    }
    public static boolean setAttribute(String name,Object value) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return false;
        HttpServletRequest request = requestAttributes.getRequest();
        if (request == null) return false;
        request.setAttribute(name,value);
        return true;
    }
    public static String getReferer() {
        return getReferer(null);
    }

    public static String getReferer(HttpServletRequest request) {
        if (request == null) request = getRequest();
        if (request == null) return null;
        return request.getHeader("referer");
    }

    public static String getHost() {
        return getHost(null);
    }

    public static String getHost(HttpServletRequest request) {
        if (request == null) request = getRequest();
        if (request == null) return null;
        String url = request.getRequestURL().toString();
        int index = url.indexOf("/", 10);
        if (index < 0) return url;
        return url.substring(0, index);
    }

    /***
     * 判断是否为手机浏览器
     * @return
     */
    public static boolean isMobileDevice() {
        HttpServletRequest request = getRequest();
        if (request == null) return false;
        return isMobileDevice(request);
    }

    /**
     * 判断是否为手机浏览器
     * @param request
     * @return
     */
    public static boolean isMobileDevice(HttpServletRequest request) {
        boolean isMoblie = false;
        String[] mobileAgents = {"iphone", "android", "ipad", "phone", "mobile", "wap", "netfront", "java", "opera mobi",
                "opera mini", "ucweb", "windows ce", "symbian", "series", "webos", "sony", "blackberry", "dopod",
                "nokia", "samsung", "palmsource", "xda", "pieplus", "meizu", "midp", "cldc", "motorola", "foma",
                "docomo", "up.browser", "up.link", "blazer", "helio", "hosin", "huawei", "novarra", "coolpad", "webos",
                "techfaith", "palmsource", "alcatel", "amoi", "ktouch", "nexian", "ericsson", "philips", "sagem",
                "wellcom", "bunjalloo", "maui", "smartphone", "iemobile", "spice", "bird", "zte-", "longcos",
                "pantech", "gionee", "portalmmm", "jig browser", "hiptop", "benq", "haier", "^lct", "320x320",
                "240x320", "176x220", "w3c ", "acs-", "alav", "alca", "amoi", "audi", "avan", "benq", "bird", "blac",
                "blaz", "brew", "cell", "cldc", "cmd-", "dang", "doco", "eric", "hipt", "inno", "ipaq", "java", "jigs",
                "kddi", "keji", "leno", "lg-c", "lg-d", "lg-g", "lge-", "maui", "maxo", "midp", "mits", "mmef", "mobi",
                "mot-", "moto", "mwbp", "nec-", "newt", "noki", "oper", "palm", "pana", "pant", "phil", "play", "port",
                "prox", "qwap", "sage", "sams", "sany", "sch-", "sec-", "send", "seri", "sgh-", "shar", "sie-", "siem",
                "smal", "smar", "sony", "sph-", "symb", "t-mo", "teli", "tim-", "tosh", "tsm-", "upg1", "upsi", "vk-v",
                "voda", "wap-", "wapa", "wapi", "wapp", "wapr", "webc", "winw", "winw", "xda", "xda-",
                "Googlebot-Mobile"};
        if (request.getHeader("User-Agent") != null) {
            String agent = request.getHeader("User-Agent");
            for (String mobileAgent : mobileAgents) {
                if (agent.toLowerCase().indexOf(mobileAgent) >= 0 && agent.toLowerCase().indexOf("windows nt") <= 0 && agent.toLowerCase().indexOf("macintosh") <= 0) {
                    isMoblie = true;
                    break;
                }
            }
        }
        return isMoblie;


    }

    public static void addHead(String name,String value){
        try {
            getResponse().setHeader(name,value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getHead(String name){
        try {
            return getRequest().getHeader(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void redirect(String url) {
        try {
            getResponse().sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取请求码
     * @return
     */
    public static String getMark() {
        String mark = getAttributeString("__requestmark__");
        if(Strings.hasValue(mark))return mark;
        mark = Core.genUuid();
        setAttribute("__requestmark__",mark);
        return mark;
    }

    /***
     * 获取请求码
     * @param request 请求头对象
     * @return
     */
    public static String getMark(HttpServletRequest request) {
        Object mark =  request.getAttribute("__requestmark__");
        if(mark!=null)return mark.toString();
        String newMask = Core.genUuid();
        request.setAttribute("__requestmark__",newMask);
        return newMask;
    }
    public static void setTag(String tag){
        setAttribute("__requesttag__",tag);
    }
    public static String getTag(){
        return getAttributeString("__requesttag__");
    }


    public static void setMark(HttpServletRequest request, String requestmask) {
        request.setAttribute("__requestmark__",requestmask);
    }

    /**
     * 设置请求日志
     * @return
     */
    public static LogFoRequest setLogger() {
        return setLogger(getRequest());
    }

    /**
     * 设置请求日志
     * @param request 请求对象
     * @return
     */
    public static LogFoRequest setLogger(HttpServletRequest request) {
        if(request==null)return null;
        LogFoRequest log = new LogFoRequest(request);
        request.setAttribute("___loggerforrequest____",log);
        return log;
    }
    public static LogFoRequest getLogger(){
        return getLogger(getRequest());
    }
    public static LogFoRequest getLogger(HttpServletRequest request){
        if(request==null)return null;
        LogFoRequest log = (LogFoRequest)request.getAttribute("___loggerforrequest____");
        return log;
    }
}
