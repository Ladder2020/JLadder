package com.jladder.openapi30;

public class Server {
    private String url;
    private String description="服务器地址";
//    private String variables="";

    public Server(){}

    public Server(String url){
        this.url=url;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public String getVariables() {
//        return variables;
//    }
//
//    public void setVariables(String variables) {
//        this.variables = variables;
//    }



}
