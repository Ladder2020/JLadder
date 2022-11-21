package com.jladder.lang;
import com.jladder.configs.Configure;
import com.jladder.data.Record;
import org.springframework.core.io.ClassPathResource;
import java.io.*;
import java.nio.file.Path;
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
                        out.insert(0,line+System.lineSeparator());
                        //out.append(line+System.lineSeparator(),0,line.length()+1);
                        System.out.println(out.toString());
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
                    out.insert(0,first+System.lineSeparator(),0,first.length()+1);
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

    /**
     * 根据ClassPathResource进行修复
     * @param path 路径
     * @return
     */
    public static String repairPath(String path) {
        try {
            return new ClassPathResource(path).getURL().getFile();
        } catch (IOException e) {
            e.printStackTrace();
            return path;
        }
    }

    /**
     * 获取的规范全路径
     * @param path 路径
     * @return
     */
    public static String getFullPath(String path){
        if(Strings.isBlank(path))return Configure.getBasicPath();
        path = path.replace("~",Configure.getBasicPath());
        File file = new File(path);
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件扩展名
     * @param filename
     * @return
     */
    public static String getExt(String filename){
        if(Strings.isBlank(filename))return "";
        if(filename.indexOf(".")<1)return "";
        return  filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 获取文件树
     * @param path 路径
     * @return
     */
    public static Record getFileTree(String path){
        File dir = new File(path);
        return getFileTree(dir,null);
    }
    /**
     * 获取文件树
     * @param dir 文件目录
     * @return
     */
    public static Record getFileTree(File dir){
        return getFileTree(dir,null);
    }

    /**
     * 获取文件树
     * @param dir 文件目录
     * @param parent 指定父对象
     * @return
     */
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

    /**
     * 创建文件夹
     * @param dir 文件目录
     * @return
     */
    public static boolean createDirectory(String dir){
        File directory = new File(dir);
        return directory.mkdir();
    }

    /**
     * 创建文件
     * @param filename 文件名
     * @return
     */
    public static boolean createFile(String filename) {
        File testFile = new File(filename);
        File fileParent = testFile.getParentFile();//返回的是File类型,可以调用exsit()等方法
        if (!fileParent.exists()) {
            fileParent.mkdirs();// 能创建多级目录
        }
        if (!testFile.exists()) {
            try {
               return testFile.createNewFile();//有路径才能创建文件
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 是否处于使用状态
     * @param filename 文件名
     * @return
     */
    public static boolean isUse(String filename) {
        File file=new File(filename);
        return !file.renameTo(file);
    }

    public static long getSize(String filename) {
        File file=new File(filename);
        return file.length();
    }
    /**
     * 删除文件或文件夹
     * @param path 路径
     * @return
     */
    public static boolean delete(Path path){
        return new File(path.toString()).isFile()?deleteFile(path.toString()):deleteDirectory(path.toString());
    }
    /**
     * 删除文件或文件夹
     * @param path 路径
     * @return
     */
    public static boolean delete(String path){
        return new File(path).isFile()?deleteFile(path):deleteDirectory(path);
    }
    /**
     * 删除文件
     * @param fileName
     * @return
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 删除目录
     * @param dir
     * @return
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子文件夹
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子文件夹
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) return false;
        return dirFile.delete();
    }
}
