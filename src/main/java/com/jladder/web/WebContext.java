package com.jladder.web;

import com.jladder.lang.Collections;
import com.jladder.lang.func.Tuple2;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.*;
import java.io.IOException;

public class WebContext {

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        HttpServletRequest request = requestAttributes.getRequest();
        return request;
    }

    ;

    public static HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        HttpServletResponse res = requestAttributes.getResponse();
        return res;
    }

    ;

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

    public static void SetCookiesForServer(String name, String value) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return;
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(true);
//        cookie.setHttpOnly(true);
        cookie.setPath("/");
        requestAttributes.getResponse().addCookie(cookie);
    }

    public static String getAttribute(String name) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return null;
        HttpServletRequest request = requestAttributes.getRequest();
        if (request == null) return null;
        Object val = request.getAttribute(name);
        if (null == val) return null;
        return val.toString();
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
        int index = url.lastIndexOf("/", 10);
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

    public static void redirect(String url) {
        try {
            getResponse().sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
