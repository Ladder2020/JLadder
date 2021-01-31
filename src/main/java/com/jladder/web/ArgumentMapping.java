package com.jladder.web;

import com.jladder.data.Record;
import com.jladder.lang.Strings;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

public class ArgumentMapping {



    public static Record GetRequestParams(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Record ret = new Record();
        if(null != requestAttributes) {
            HttpServletRequest request = requestAttributes.getRequest();
            request.getParameterMap().forEach((k,v)->{
                ret.put(k.toString(),v);
            });
        }
        return ret;
    }

    public static Record GetRequestParams(HttpServletRequest request) throws IOException {

        Record ret = (Record) request.getAttribute("___ArgumentMapping_GetRequestParams____");
        if(ret != null)return ret;
        String contentType = request.getContentType();
        if(Strings.hasValue(contentType) && contentType.contains("application/json")){
            String postStr = getRequestPostStr(request);
            ret = Record.parse(postStr);
            request.setAttribute("___ArgumentMapping_GetRequestParams____",ret);
        }else{
            ret = new Record();
            Record finalRet = ret;
            request.getParameterMap().forEach((k, v)->{
                if(v instanceof String[]){
                    finalRet.put(k.toString(),((String[])v)[0]);
                    return;
                }
                finalRet.put(k.toString(),v);
            });
        }
        request.setAttribute("___ArgumentMapping_GetRequestParams____",ret);
        return ret;
    }
    public static byte[] getRequestPostBytes(HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if(contentLength<0){
            return null;
        }
        byte buffer[] = new byte[contentLength];
        for (int i = 0; i < contentLength;) {

            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return buffer;
    }
    public static String getRequestPostStr(HttpServletRequest request)
            throws IOException {
        byte buffer[] = getRequestPostBytes(request);
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
        return new String(buffer, charEncoding);
    }
    public static Record GetHeadParam(){
        return GetHeadParam(WebContext.getRequest());
    }
    public static Record GetHeadParam(HttpServletRequest request) {

        Record record = new Record();
        Enumeration er = request.getHeaderNames();//获取请求头的所有name值
        while(er.hasMoreElements()){
            String name	=(String) er.nextElement();
            String value = request.getHeader(name);
            record.put(name,value);
        }
        return record;
    }
}
