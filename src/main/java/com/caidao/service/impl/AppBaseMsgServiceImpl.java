package com.caidao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.AppBaseMsgMapper;
import com.caidao.pojo.AppBaseMsg;
import com.caidao.service.AppBaseMsgService;
import com.caidao.util.PropertyUtils;
import com.caidao.util.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Dillon
 * @since 2020-06-08
 */
@Service
@Slf4j
public class AppBaseMsgServiceImpl extends ServiceImpl<AppBaseMsgMapper, AppBaseMsg> implements AppBaseMsgService {

    @Autowired
    private Jedis jedis;

    @Autowired
    private AppBaseMsgMapper appBaseMsgMapper;

    /**
     * 获取用户的公钥，并且收集手机的数据
     * @param appBaseMsg
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<Map<String, String>> appLunch(AppBaseMsg appBaseMsg) throws NoSuchAlgorithmException {
        int insert = appBaseMsgMapper.insert(appBaseMsg);
        if (insert == 0) {
            throw new MyException("基本信息插入失败，请重新登录");
        }
        //获取加密解密数据
        Map<String, String> integerStringMap = RsaUtils.generateKeyPair();
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        jedis.setex(PropertyUtils.APP_USER_PUBLIC_KEY + uuid,1800,integerStringMap.get("公钥"));
        jedis.setex(PropertyUtils.APP_USER_PRIVATE_KEY + uuid,1800,integerStringMap.get("密钥"));
        Map<String, String> map = new HashMap<>(2);
        map.put("publicKey",integerStringMap.get("公钥"));
        map.put("uuid",uuid);
        return ResponseEntity.ok(map);
    }

}
