package com.naah69.algolia.utils;

import com.naah69.algolia.FilePath;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileUtils
 *
 * @author naah
 * @date 2018-09-01 下午11:54
 * @desc
 */
public class FileUtils {

    /**
     * 通过流读取文件
     * @param is 流
     * @return 文本
     */
    public static String readFileByInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();

    }

    public static boolean existsFile(String path){
        File file=new File(path);
        boolean result = file.exists();
        if(!result){
            System.out.println("exists error: "+path+" not found");
        }
        return result;

    }

    /**
     * 读取文件
     *
     * @param file file对象
     * @return 文本
     */
    public static String readFileByLines(File file) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file));) {

            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                sb.append(tempString).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 覆盖写入文件
     *
     * @param filePath 文件路径
     * @param content  内容
     */
    public static void writeToFile(String filePath, String content) {

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, false)));
            out.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取所有文件的File对象
     *
     * @param dir 目录路径
     * @return File对象列表
     */
    public static List<File> getAllFiles(String dir) {
        List<File> files = new ArrayList();
        File file = new File(dir);
        if (file.exists() && file.isDirectory()) {
            longErgodic(file, files);
        }
        return files;
    }

    private static void longErgodic(File file, List<File> files) {
        File[] fillArr = file.listFiles();
        if (fillArr == null) {
            return;
        }
        for (File file2 : fillArr) {
            files.add(file2);
            longErgodic(file2, files);
        }
    }
}
