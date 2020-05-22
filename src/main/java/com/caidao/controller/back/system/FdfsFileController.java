package com.caidao.controller.back.system;

import com.caidao.anno.SysLogs;
import com.caidao.service.FdfsUpAndDowService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author tom
 */

@RestController
@RequestMapping("/file")
@Slf4j
public class FdfsFileController {

    @Autowired
    private FdfsUpAndDowService fdfsUpAndDowService;

    /**
     * 单个上传文件
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public ResponseEntity<Map<String, String>> uploadFile(MultipartFile file) {
        log.info("上传文件名为{}的文件",file.getOriginalFilename());
        Assert.notNull(file);
        Map<String, String> uploadFile = fdfsUpAndDowService.uploadFile(file);
        return ResponseEntity.ok(uploadFile);
    }

    /**
     * 单个下载文件
     * @return
     */
    @GetMapping("/download")
    @ApiOperation("下载文件")
    public ResponseEntity<String> downloadFile(String filename, HttpServletResponse response) throws IOException {
        fdfsUpAndDowService.downloadFile(filename,response);
        return ResponseEntity.ok().build();
    }

    /**
     * 单个文件删除
     * @param filename
     * @return
     */
    @SysLogs("删除文件")
    @DeleteMapping("/delete")
    @ApiOperation("删除文件")
    public ResponseEntity<String> deleteFile(String filename) {

        //TODO 调用该接口之后需要刷新缓存
        String file = fdfsUpAndDowService.deleteFileByFileUrl(filename);

        if (file == null){
            return ResponseEntity.ok("删除成功");
        }
        return ResponseEntity.ok("失败");
    }

}

