package com.jladder.lang;



import com.jladder.data.Record;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.*;


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
    public static String readReversedLines(File file,long rows){
        return readReversedLines(file,rows,"utf-8");
    }
    public static String readReversedLines(File file,long rows,String charset){
        RandomAccessFile rf = null;
        StringBuffer out = new StringBuffer();
        try {
            if(Strings.isBlank(charset))charset="utf-8";
            rf = new RandomAccessFile(file, "r");
            long len = rf.length();
            long start = rf.getFilePointer();
            long nextend = start + len - 1;
            String line;
            rf.seek(nextend);
            int c = -1;
            long num=0;
            while (nextend > start) {
                c = rf.read();
                if (c == '\n' || c == '\r') {
                    line = rf.readLine();
                    if (line != null) {
                        num++;
                        line = new String(line.getBytes("ISO-8859-1"), charset);
                        out.append(line+System.lineSeparator(),0,line.length()+1);
                    } else {
                        //System.out.println(line);
                    }
                    nextend--;
                }
                nextend--;
                rf.seek(nextend);
                if (nextend == 0) {// 当文件指针退至文件开始处，输出第一行
                    String first = rf.readLine();
                    first = new String(first.getBytes("ISO-8859-1"), charset);
                    out.append(first+System.lineSeparator(),0,first.length()+1);
                }
                if(rows>0 && num>=rows)break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rf != null)
                    rf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return out.toString();
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

    public static Record getFileTree(String path){
        File dir = new File(path);
        return getFileTree(dir,null);
    }
    public static Record getFileTree(File dir){
        return getFileTree(dir,null);
    }
    public static Record getFileTree(File dir,Record parent){
        if(dir.isFile())return null;
//        if(dir.isHidden())return null;
        if(parent==null)parent=new Record();
        parent.put("path",dir.getPath())
                .put("size",dir.length())
                .put("name",dir.getName())
                .put("lasttime",Times.sDT(new Date(dir.lastModified())));

        if (dir.isDirectory()) {
            String[] children = dir.list();
            if(children==null)return parent;
            List<Record> ls = new ArrayList<Record>();
            for (int i = 0; i < children.length; i++) {
                Record child = getFileTree(new File(dir, children[i]), new Record());
                if(child!=null)ls.add(child);
            }
            parent.put("children",ls);
        }
        return parent;
    }
}
