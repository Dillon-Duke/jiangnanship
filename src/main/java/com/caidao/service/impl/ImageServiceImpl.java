package com.caidao.service.impl;

import com.caidao.entity.Image;
import com.caidao.entity.SysUser;
import com.caidao.mapper.ImageMapper;
import com.caidao.service.ImageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-18
 */
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {


    @Override
    public boolean saveImage(File file) {

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();

        Image image = new Image();

        image.setFileName(file.getName());

        image.setFileSize(file.length()%1024);

        BufferedImage imageIO;
        try {
            imageIO = ImageIO.read(new FileInputStream(file));

            image.setFileLength(Long.valueOf(imageIO.getHeight()));

            image.setFileWidth(Long.valueOf(imageIO.getWidth()));

            image.setFileContent(String.valueOf(imageIO.getSource()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        image.setCreateId(sysUser.getUserId());

        image.setCreateDate(LocalDateTime.now());

        boolean save = this.save(image);

        return save;
    }
}
