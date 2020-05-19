package com.caidao;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
public class FdfdsTese {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Test
    void upLoadImg() throws FileNotFoundException {
        File file = new File("图片"+File.separator+"timg.jpg");
        StorePath path = fastFileStorageClient.uploadFile(null, new FileInputStream(file), file.length(), "jpg");
    }
}
