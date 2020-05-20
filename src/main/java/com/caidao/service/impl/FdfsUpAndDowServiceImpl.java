package com.caidao.service.impl;

import com.caidao.service.FdfsUpAndDowService;
import com.caidao.util.FastDfsClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tom
 */
@Service
@Slf4j
public class FdfsUpAndDowServiceImpl implements FdfsUpAndDowService {

    @Autowired
    private FastDfsClientUtils fastDfsClientUtils;

    private final Logger logger = LoggerFactory.getLogger(FdfsUpAndDowServiceImpl.class);

    /**
     *  单个文件上传
     * @param file 页面提交时文件
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> uploadFile(MultipartFile file){
        byte[] bytes = new byte[0];
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            logger.error("获取文件错误");
            e.printStackTrace();
        }
        //获取源文件名称
        String originalFileName = file.getOriginalFilename();
        //获取文件后缀--.doc .jpg
        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);

        String fileName = file.getName();
        //获取文件大小
        long fileSize = file.getSize();
        log.info(originalFileName + "==" + fileName + "==" + fileSize + "==" + extension + "==" + bytes.length);
         String fdfsname = fastDfsClientUtils.uploadFile(bytes, fileSize, extension);
         
         //将文件fast路径名 文件真实名字放入map中
        Map<String, String> hashMap = new HashMap<>(2);
        hashMap.put("fdfsUrl",fdfsname);
        hashMap.put("filename",originalFileName);
        return hashMap;
    }

    /**
     *  单个文件下载
     * @param fielname 当前对象文件名称
     * @param response   HttpServletResponse 内置对象
     * @throws IOException
     */
    @Override
    public void downloadFile(String fielname, HttpServletResponse response) throws IOException {

        byte[] bytes = fastDfsClientUtils.downloadFile(fielname);

        // 这里只是为了整合fastdfs，所以写死了文件格式。需要在上传的时候保存文件名。下载的时候使用对应的格式

        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fielname, "UTF-8"));
        response.setCharacterEncoding("UTF-8");
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除文件
     * @param filename
     * @return
     */
    @Override
    public String deleteFileByFileUrl(String filename) {

        String deleteFile = fastDfsClientUtils.deleteFile(filename);

        return deleteFile;

    }




}
