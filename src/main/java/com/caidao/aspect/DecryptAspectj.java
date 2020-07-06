package com.caidao.aspect;

import com.caidao.exception.MyException;
import com.caidao.util.AesUtils;
import com.caidao.util.PropertyUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

/**
 * @author tom
 */
public class DecryptAspectj {

    @Autowired
    private Jedis jedis;

    @Around("@annotation(com.caidao.anno.Decrypt)")
    public Object decryptAspectjDate(ProceedingJoinPoint joinPoint) {
        String token = SecurityUtils.getSubject().getSession().getId().toString();
        Object proceed;
        try {
            proceed = joinPoint.proceed(joinPoint.getArgs());
            String sKey = jedis.get(PropertyUtils.MD5_PREFIX + token);
            proceed = AesUtils.decrypt(String.valueOf(proceed), sKey);
        } catch (Throwable throwable) {
            throw new MyException("后台解签失败，请联系管理员");
        }
        return proceed;
    }

}
