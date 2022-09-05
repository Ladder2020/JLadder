package com.jladder.lang;

import com.jladder.data.Receipt;

import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author YiFeng
 * @date 2022年07月23日 22:40
 */
public class Zips {
    private static final int  BUFFER_SIZE = 2 * 1024;
    /**
     * 压缩文件字节
     * @param src 文件字节码Map，k:fileName，v：byte[]
     * @param filename 保存文件名
     */
    public static Receipt<String> zip(Map<String, byte[]> src, String filename) {
        if(src==null)return new Receipt<String>(false,"源文件不存在");
        if(Strings.isBlank(filename))filename=Core.genUuid()+".zip";
        //如果文件夹不存在就创建文件夹，防止报错
        File file = new File(filename);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            src.forEach((k, v) -> {
                //写入一个条目，我们需要给这个条目起个名字，相当于起一个文件名称
                try {
                    zipOutputStream.putNextEntry(new ZipEntry(k));
                    zipOutputStream.write(v);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.print("写入文件失败");
                }
            });
            //关闭条目
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            fileOutputStream.close();
            return new Receipt<String>().setData(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return new Receipt<String>(false,e.getMessage());
        }
    }

    /**
     * 压缩文件字节
     * @param src 文件字节码Map，k:fileName，v：byte[]
     */
    public static Receipt<byte[]> zip(Map<String, byte[]> src) {
        if(src==null)return new Receipt<byte[]>(false,"源文件不存在");
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(output);
            src.forEach((k, v) -> {
                //写入一个条目，我们需要给这个条目起个名字，相当于起一个文件名称
                try {
                    zipOutputStream.putNextEntry(new ZipEntry(k));
                    zipOutputStream.write(v);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.print("写入文件失败");
                }
            });
            //关闭条目
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            byte[] data = output.toByteArray();
            return new Receipt<byte[]>().setData(data);
        } catch (IOException e) {
            e.printStackTrace();
            return new Receipt<byte[]>(false,e.getMessage());
        }
    }

    public static Receipt<String> zip(List<File> src , String filename) {
        try {
            return zip(src,new FileOutputStream(new File(filename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new Receipt<String>(false,e.getMessage());
        }
    }
    /**
     * 压缩成ZIP 方法2
     * @param srcFiles 需要压缩的文件列表
     * @param out           压缩文件输出流
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static Receipt<String> zip(List<File> srcFiles , OutputStream out) {
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null ;
        try {
            zos = new ZipOutputStream(out);
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[BUFFER_SIZE];
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                while ((len = in.read(buf)) != -1){
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
            }
            long end = System.currentTimeMillis();
            System.out.println("压缩完成，耗时：" + (end - start) +" ms");
            return new Receipt<String>();
        } catch (Exception e) {
            e.printStackTrace();
            return new Receipt<String>(false, e.getMessage());
        }finally{
            if(zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Receipt<String> zip(String dir, String filename){
        try {
            File file = new File(filename);
            return zip(dir,new FileOutputStream(file),true).setData(file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new Receipt<String>(false,e.getMessage());
        }
    }

    /**
     * 压缩成ZIP
     * @param dir 压缩文件夹路径
     * @param out    压缩文件输出流
     * @param KeepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     */
    public static Receipt<String> zip(String dir, OutputStream out, boolean KeepDirStructure){
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null ;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(dir);
            compress(sourceFile,zos,sourceFile.getName(),KeepDirStructure);
            long end = System.currentTimeMillis();
            System.out.println("压缩完成，耗时：" + (end - start) +" ms");
            return new Receipt<String>();
        } catch (Exception e) {
            e.printStackTrace();
            return new Receipt<String>(false,e.getMessage());
        }finally{
            if(zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 递归压缩方法
     * @param sourceFile 源文件
     * @param zos        zip输出流
     * @param name       压缩后的名称
     * @param KeepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean KeepDirStructure) throws Exception{
        byte[] buf = new byte[BUFFER_SIZE];
        if(sourceFile.isFile()){
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1){
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if(listFiles == null || listFiles.length == 0){
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if(KeepDirStructure){
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }

            }else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(),KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(),KeepDirStructure);
                    }

                }
            }
        }
    }
    /**
     * zip解压
     * @param src        zip源文件
     * @param dest     解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static Receipt unZip(File src, String dest){
        long start = System.currentTimeMillis();
        // 判断源文件是否存在
        if (!src.exists()) {
            throw new RuntimeException(src.getPath() + "所指文件不存在");
        }
        // 开始解压
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(src);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                System.out.println("解压" + entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = dest + "/" + entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(dest + "/" + entry.getName());
                    // 保证这个文件的父文件夹必须要存在
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[BUFFER_SIZE];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("解压完成，耗时：" + (end - start) + " ms");
            return new Receipt();
        } catch (Exception e) {
            e.printStackTrace();
            return new Receipt(false,e.getMessage());
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
