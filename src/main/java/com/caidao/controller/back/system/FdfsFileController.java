package com.caidao.controller.back.system;

import com.caidao.anno.SysLogs;
import com.caidao.service.FdfsUpAndDowService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author tom
 * @since 2020-05-23
 */

@RestController
@RequestMapping("/file")
@Slf4j
public class FdfsFileController {

    public static final Logger logger = LoggerFactory.getLogger(FdfsFileController.class);

    @Value("${fdfs-imgUpload-prifax}")
    public String fdfsProfix;

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
        Assert.notNull(file,"上传文件不能为空");
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

        Assert.notNull(filename,"下载文件名不能为空");
        log.info("下载文件名为{}的文件",filename);

        fdfsUpAndDowService.downloadFile(filename,response);
        return ResponseEntity.ok().build();
    }

    /**
     * 单个文件删除
     * @param filenames
     * @return
     */
    @SysLogs("删除文件")
    @DeleteMapping("/delete")
    @ApiOperation("删除文件")
    public ResponseEntity<String> deleteFile(@RequestBody List<String> filenames) {

        Assert.notNull(filenames,"文件名不能为空");
        log.info("删除文件名为{}的这些文件",filenames);

        for (String filename : filenames) {
            String file = fdfsUpAndDowService.deleteFileByFileUrl(fdfsProfix + filename);
            if (file == null){
                return ResponseEntity.ok("删除成功");
            }
            return ResponseEntity.ok("失败");
        }
        return ResponseEntity.ok("失败");
    }

}

