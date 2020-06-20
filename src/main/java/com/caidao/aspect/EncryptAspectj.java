package com.caidao.aspect;

import com.caidao.common.ResponseEntity;
import com.caidao.exception.MyException;
import com.caidao.util.PropertyUtils;
import com.caidao.util.RsaUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Signature;
import java.util.Base64;

/**
 * @author tom
 */
@Aspect
@Component
public class EncryptAspectj {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Around("@annotation(com.caidao.anno.Encrypt)")
    public ResponseEntity<Object> encryptDateAspectj(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed;
        try {
            proceed = joinPoint.proceed(joinPoint.getArgs());
            //给数据加签
            String uuid = SecurityUtils.getSubject().getSession().getId().toString();
            String privateKey = redisTemplate.opsForValue().get(PropertyUtils.APP_USER_PRIVATE_KEY + uuid);
            //需要填入特定的方法签名
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(RsaUtils.getPrivateKey(privateKey));
            signature.update(proceed.toString().getBytes("UTF-8"));
            byte[] signed = signature.sign();
            //将方法签名转成字符串，便于像前端传输
            String string = Base64.getEncoder().encodeToString(signed);
            proceed = string;
        } catch (Throwable e) {
            throw new MyException("返回数据加密失败，请联系管理员");
        }
        return ResponseEntity.ok(proceed);
    }
}
