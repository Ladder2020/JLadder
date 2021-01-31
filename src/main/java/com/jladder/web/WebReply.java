package com.jladder.web;

import com.jladder.lang.Json;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
}
