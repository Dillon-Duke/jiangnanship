package com.caidao.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author tom
 */
public interface FdfsUpAndDowService {

    /**
     * 文件上传
     * @param file 需要上传的文件
     * @return
     */
    Map<String, String> uploadFile(MultipartFile file);

    /**
     * 文件下载
     * @param fielname 文件在fastdfs里面的全路径
     * @param response 设置相应类型
     * @throws IOException
     */
    void downloadFile(String fielname, HttpServletResponse response) throws IOException;

    /**
     * 删除文件
     * @param filename 文件在fastdfs里面的全路径
     * @return
     */
    String deleteFileByFileUrl(String filename);
}
