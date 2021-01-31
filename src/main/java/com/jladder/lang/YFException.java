package com.jladder.lang;

public class YFException extends Exception{

    public String message;


    YFException(String message){
        super();

    }

    public String getMessage(){
        return message;
    }
}
