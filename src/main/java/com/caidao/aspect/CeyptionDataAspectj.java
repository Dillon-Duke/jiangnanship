package com.caidao.aspect;


import com.caidao.anno.DecryptData;
import com.caidao.entity.AppBaseMsg;
import com.caidao.exception.MyException;
import com.caidao.util.PropertyUtils;
import com.caidao.util.RsaUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
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
public class CeyptionDataAspectj {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Order(1)
    @Before(value = "@annotation(com.caidao.anno.DecryptData)")
    public ResponseEntity<String> captionsDataBeforeAspectj(JoinPoint joinPoint) throws Exception {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DecryptData annotation = method.getAnnotation(DecryptData.class);
        String value = annotation.value();
        if ("encryption".equals(value)){
            Object[] args = joinPoint.getArgs();

            //获取传参的值
            AppBaseMsg appBaseMsg = (AppBaseMsg) args[0];


            String userId = appBaseMsg.getUserId();
            if (userId == "" ||userId == null){

                //获得用户登录的uuid
                String uuid = appBaseMsg.getUuid();

                //替换解密内容
                String primaryKey = redisTemplate.opsForValue().get(PropertyUtils.APP_USER_PRIVATE_KEY + uuid);
                String decrypt = RsaUtils.decrypt(appBaseMsg.getEncryption(), primaryKey);
                appBaseMsg.setEncryption(decrypt);
                redisTemplate.delete(PropertyUtils.APP_USER_PRIVATE_KEY + uuid);
                redisTemplate.delete(PropertyUtils.APP_USER_PUBLIC_KEY + uuid);
            } else {

                //获取反射中特定属性的值
                String encryption = appBaseMsg.getEncryption();
                if (encryption == null || encryption.isEmpty()){
                    throw new MyException("数据中加密数据没有传值");
                }

                //判断已登录用户没有传ID
                if (userId == null || userId.isEmpty()){
                    throw new MyException("数据中用户ID没有传值");
                }

                //获得用户token
                String token = SecurityUtils.getSubject().getSession().getId().toString();
                //替换解密内容
                String decrypt = RsaUtils.decrypt(appBaseMsg.getEncryption(), redisTemplate.opsForValue().get(PropertyUtils.APP_USER_PRIVATE_KEY + token));
                appBaseMsg.setEncryption(decrypt);
            }

        } else {
            //TODO 以后如果需要做其他属性的解码，则在此写对应方法
        }
        return ResponseEntity.ok().build();
    }

    @Order(10)
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
