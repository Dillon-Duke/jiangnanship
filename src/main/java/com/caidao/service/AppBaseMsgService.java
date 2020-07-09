package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.AppBaseMsg;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author Dillon
 * @since 2020-06-08
 */
public interface AppBaseMsgService extends IService<AppBaseMsg> {

    /**
     * 获取用户的公钥
     * @param appBaseMsg
     * @return
     * @throws NoSuchAlgorithmException
     */
    Map<String, String> appLunch(AppBaseMsg appBaseMsg) throws NoSuchAlgorithmException;

}
