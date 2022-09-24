package com.jladder.web;

import com.jladder.data.Record;
import com.jladder.lang.Json;
import com.jladder.net.http.HttpHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

public class WebReply {
    public static void reply(Object data) {

        try {
            reply(data,WebContext.getResponse());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void reply(Object data, HttpServletResponse response) throws IOException {
        //if (response == null) response = WebContext.Current?.Response;
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/plain; charset=utf-8");

        if(data instanceof CharSequence){
            response.setContentType("text/plain;charset=utf-8");
            response.getWriter().println(data.toString());
            return;
        }


        switch (data.getClass().getName())
        {
            case "system.string":
                response.setContentType("text/plain;charset=utf-8");
                response.getWriter().println(data.toString());
                break;
            default:
                response.setCharacterEncoding("utf-8");
                response.setContentType("text/javascript");
                response.getWriter().println(Json.toJson(data));
                break;
        }
        response.flushBuffer();
    }
    /**
     * 以post跳转到其他网站
     * @param url 网址
     * @param data 携带数据
     * @return void
     * @author YiFeng
     */
    public static void turn(String url,Object data){
        turn(null,data);
    }
    /**
     * 以post跳转到其他网站
     * @param response 回复头对象
     * @param url 网址
     * @param data 携带数据
     * @return void
     * @author YiFeng
     */
    public static void turn(HttpServletResponse response,String url,Object data){
        Record parameter = Record.parse(data);
        response = WebContext.getResponse();
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
            out.println("<HTML>");
            out.println(" <HEAD>");
            out.println(" <meta http-equiv=Content-Type content=\"text/html; charset=utf-8\">");
            out.println(" <TITLE>loading</TITLE>");
            out.println(" <meta http-equiv=\"Content-Type\" content=\"text/html charset=GBK\">\n");
            out.println(" </HEAD>");
            out.println(" <BODY>");
            out.println("<form name=\"submitForm\" action=\"" + url + "\" method=\"post\">");
            Iterator<String> it = parameter.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                out.println("<input type=\"hidden\" name=\"" + key + "\" value=\"" + parameter.get(key) + "\"/>");
            }
            out.println("</from>");
            out.println("<script>window.document.submitForm.submit();</script> ");
            out.println(" </BODY>");
            out.println("</HTML>");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(out!=null)out.close();
        }
    }
}
