package com.jladder.web;

import com.jladder.data.Record;
import com.jladder.data.UploadFile;
import com.jladder.lang.Strings;
import com.jladder.lang.Xmls;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class ArgumentMapping {


    public static Record getRequestParams(){
        return getRequestParams(WebContext.getRequest(),FileFormat.None);
    }
    public static Record getRequestParams(HttpServletRequest request){
        return getRequestParams(request,FileFormat.None);
    }
    public static Record getRequestParams(FileFormat format){
        return getRequestParams(WebContext.getRequest(),format);
    }

    public static Record getRequestParams(HttpServletRequest request,FileFormat format){
        try{
            Record ret = (Record) request.getAttribute("___ArgumentMapping_GetRequestParams____");
            if(ret != null){
                if (FileFormat.None.equals(format))return ret;
                Map<String, List<UploadFile>> files = WebContext.getUploadFiles(request);
                if(files!=null)ret.putAll(files);
                return ret;
            }
            String contentType = request.getContentType();
            if(Strings.isBlank(contentType))contentType="";
            if(contentType!=null && contentType.indexOf("application/json")>-1){
                contentType="application/json";
            }
            String postStr="";
            switch (contentType){
                case "text/xml":
                    postStr = getRequestPostStr(request);
                    System.out.println("xml:"+postStr);
                    ret = Record.parse(Xmls.toMap(Xmls.parseXml(postStr).getDocumentElement()));
                    break;
                case "application/json":
                    postStr = getRequestPostStr(request);
                    ret = Record.parse(postStr);
                    request.setAttribute("___ArgumentMapping_GetRequestParams____",ret);
                    break;
                default:
                    ret = new Record();
                    Record finalRet = ret;
                    request.getParameterMap().forEach((k, v)->{
                        if(v instanceof String[]){
                            finalRet.put(k.toString(),((String[])v)[0]);
                            return;
                        }
                        finalRet.put(k.toString(),v);
                    });
                    break;
            }
            request.setAttribute("___ArgumentMapping_GetRequestParams____",ret);

            if (FileFormat.None.equals(format))return ret;
            else {
                Map<String, List<UploadFile>> files = WebContext.getUploadFiles(request);
                if(files!=null)ret.putAll(files);
            }
            return ret;
        }catch (Exception e)
        {
            return new Record();
        }

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
    public static String getRequestPostStr(HttpServletRequest request) throws IOException {
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
