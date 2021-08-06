package com.jladder.lang;



import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class Files {



    public static BufferedInputStream getInputStream(File file){
        return Streams.toBuffered(Streams.toStream(file));
    }

    /**
     * 判断文件是否存在，如果path为null，则返回false
     *
     * @param path 文件路径
     * @return 如果存在返回true
     */
    public static boolean exist(String path) {
        return (null != path) && new File(path).exists();
    }


    public static boolean exist(File file) {
        return (null != file) && file.exists();
    }
    public static String read(File file) {
        if(!exist(file))return "";
        String jsonStr = "";
        try{
            FileReader fileReader = new FileReader(file);
            Reader reader = new InputStreamReader(new FileInputStream(file),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static String read(String path) {
        File file = new File(path);
        return read(file);
    }
    public static File getFile(String relative_path){
        try {
            return new ClassPathResource(relative_path).getFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String repairPath(String path) {
        try {
            return new ClassPathResource(path).getURL().getFile();
        } catch (IOException e) {
            e.printStackTrace();
            return path;
        }
    }
    public static String getExt(String path){
        if(path.indexOf(".")<1)return "";
        return  path.substring(path.lastIndexOf("."));
    }
}
