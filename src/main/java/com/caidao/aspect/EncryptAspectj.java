package com.caidao.aspect;

import com.caidao.exception.MyException;
import com.caidao.util.AesUtils;
import com.caidao.util.PropertyUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

/**
 * @author tom
 */
@Aspect
@Component
public class EncryptAspectj {

    @Autowired
    private Jedis jedis;

    @Around("@annotation(com.caidao.anno.Encrypt)")
    public Object encryptDateAspectj(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed;
        try {
            proceed = joinPoint.proceed(joinPoint.getArgs());
            //给数据加密
            String token = SecurityUtils.getSubject().getSession().getId().toString();
            String sKey = jedis.get(PropertyUtils.AES_PREFIX + token);
            //数据加密
            proceed = AesUtils.encrypt(proceed.toString(), sKey);
        } catch (Throwable e) {
            throw new MyException("返回数据加密失败，请联系管理员");
        }
        return proceed;
    }
}
