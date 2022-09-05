package com.jladder.openapi30;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Doc {


    private String openapi="3.0.0";
    private Info info=new Info();
    private List<Server> servers=new ArrayList<Server>();
    private Map<String,PathItem> paths = new HashMap<String,PathItem>();

    public void addServer(String url){
        if(servers==null)servers=new ArrayList<Server>();
        servers.add(new Server(url));
    }

    public void addPath(String path,PathItem item){
        if(paths==null)paths=new HashMap<String,PathItem>();
        paths.put(path,item);
    }
    public String getOpenapi() {
        return openapi;
    }

    public void setOpenapi(String openapi) {
        this.openapi = openapi;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public Map<String, PathItem> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, PathItem> paths) {
        this.paths = paths;
    }
}
