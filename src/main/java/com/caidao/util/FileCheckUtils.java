package com.caidao.util;

import com.caidao.param.FileParam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author tom
 * @since 2020-05-25
 * 检查用户文件上传是否符合标准 ，所有参数设置从前端传过来做校验
 */
public class FileCheckUtils {

    /**
     * 检查文件是否符合上传标准
     * @param fileParam
     */
    public static String checkFile(FileParam fileParam){
        File file = fileParam.getFile();
        //判断是否为文件
        if (!file.isFile()){
            return "文件类型错误";
        }
        //判断文件类型是否符合要求
        String fileType = fileParam.getFileType();
        String[] strings = fileType.split(",");
        String fileName = file.getName();
        String fileType1 = fileName.substring(fileName.lastIndexOf("." + 1));
        int count = 0;
        for (int i = 0; i < strings.length; i++) {
            if (fileType1.equals(strings[i])){
                count++;
                continue;
            }
        }
        if (count == 0){
            return "上传文件类型不在规定类型之内";
        }
        //判断上传文件大小是否超过默认最大值
        if (fileParam.getMaxFileSize() < file.length()){
            return "上传文件过大";
        }
        //判断上传文件长宽是否超过最大
        FileInputStream inputStream;
        BufferedImage read;
        try {
            inputStream = new FileInputStream(file);
            read = ImageIO.read(inputStream);
            if ((fileParam.getWidth() <read.getWidth()) || (fileParam.getLength() < read.getHeight())){
                return "上传图片宽高不正确";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
