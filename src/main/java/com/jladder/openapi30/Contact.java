package com.jladder.openapi30;

public class Contact {
    private String name="Ladder";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String url="http://www.ladder2020.com";
    private String email="xzhy527@126.com";
}
