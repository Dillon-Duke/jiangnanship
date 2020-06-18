package com.caidao.aspect;


import com.caidao.anno.DecryptData;
import com.caidao.pojo.AppBaseMsg;
import com.caidao.util.PropertyUtils;
import com.caidao.util.RsaUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author tom
 * @destription 用户加密解密的切面
 */
@Aspect
@Component
public class EndeptAndDecryptDataAspectj {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Before(value = "@annotation(com.caidao.anno.DecryptData)")
    public ResponseEntity<String> captionsDataBeforeAspectj(JoinPoint joinPoint) throws Exception {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DecryptData annotation = method.getAnnotation(DecryptData.class);
        String value = annotation.value();
        if ("".equals(value)){
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                String name = arg.getClass().getTypeName();

            }


        } else {
            //TODO 以后如果需要做其他属性的解码，则在此写对应方法
        }
        return ResponseEntity.ok().build();
    }

    @Around("@annotation(com.caidao.anno.EneptData)")
    public ResponseEntity<String> captionDataAfterAspectj(ProceedingJoinPoint joinPoint) throws Throwable {

        //获取对应的结果
        Object proceed = joinPoint.proceed(joinPoint.getArgs());
        ResponseEntity responseEntity = (ResponseEntity) proceed;
        Object body = responseEntity.getBody();
        String string = body.toString();

        //获取方法参数传值ID
        AppBaseMsg baseMsg = (AppBaseMsg) joinPoint.getArgs()[0];
        String userId = baseMsg.getUserId();

        String encrypt = RsaUtils.encrypt(string, redisTemplate.opsForValue().get(PropertyUtils.APP_USER_PUBLIC_KEY + userId));

        return ResponseEntity.ok(encrypt);

    }
}
