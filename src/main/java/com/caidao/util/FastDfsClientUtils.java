package com.caidao.util;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.exception.FdfsServerException;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author tom
 */

@Component
public class FastDfsClientUtils {

    private final Logger logger = LoggerFactory.getLogger(FastDfsClientUtils.class);

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    /**
     * 文件上传
     * 最后返回fastDFS中的文件名称;group1/M00/01/04/CgMKrVvS0geAQ0pzAACAAJxmBeM793.doc
     *
     * @param bytes     文件字节
     * @param fileSize  文件大小
     * @param extension 文件扩展名
     * @return fastDfs路径
     */
    public String uploadFile(byte[] bytes, long fileSize, String extension) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        StorePath storePath = fastFileStorageClient.uploadFile(byteArrayInputStream, fileSize, extension, null);
        return storePath.getFullPath();
    }

    /**
     * 下载文件
     *  返回文件字节流大小
     * @param fielname 文件URL
     * @return 文件字节
     * @throws IOException
     */
    public byte[] downloadFile(String fielname) {

        String group = fielname.substring(0, fielname.indexOf("/"));
        String path = fielname.substring(fielname.indexOf("/") + 1);
        DownloadByteArray downloadByteArray = new DownloadByteArray();
        byte[] bytes = fastFileStorageClient.downloadFile(group, path, downloadByteArray);
        return bytes;
    }

    /**
     * 删除单个文件
     * @param fielname
     * @return
     */
    public String deleteFile(String fielname) {

        //获得文件的总体路径并且自动分开文件组和真是路径
        StorePath storePath = StorePath.parseFromUrl(fielname);
        try {
            fastFileStorageClient.deleteFile(storePath.getGroup(),storePath.getPath());
        }catch (FdfsServerException exception){
             return exception.getMessage();
        }
        return null;
    }
}
