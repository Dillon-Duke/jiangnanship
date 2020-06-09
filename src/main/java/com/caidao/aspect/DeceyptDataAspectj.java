package com.caidao.aspect;


import com.caidao.anno.DecryptData;
import com.caidao.entity.AppBaseMsg;
import com.caidao.exception.MyException;
import com.caidao.util.RsaUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author tom
 */
@Aspect
@Component
public class DeceyptDataAspectj {

    @Around("@annotation(com.caidao.anno.DecryptData)")
    @Transactional(rollbackFor = RuntimeException.class)
    public Object before(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DecryptData annotation = method.getAnnotation(DecryptData.class);
        String value = annotation.value();
        if ("encryption".equals(value)){
            Object[] args = joinPoint.getArgs();

            AppBaseMsg appBaseMsg = (AppBaseMsg) args[0];
            //获取反射中特定属性的值
            String encryption = appBaseMsg.getEncryption();
            if (encryption == null || encryption.isEmpty()){
                throw new MyException("数据中加密数据没有传值");
            }
            String userId = appBaseMsg.getUserId();
            if (userId == null || userId.isEmpty()){
                throw new MyException("数据中用户ID没有传值");
            }

            //获取加密解密数据
            Map<Integer, String> integerStringMap = RsaUtils.genKeyPair(userId);
            if (appBaseMsg.getEncrypt() != "" && appBaseMsg.getEncrypt() != null && (!appBaseMsg.getEncrypt().equals(integerStringMap.get(0)))){
                throw new MyException("公钥数据没有传值或者传值错误");
            }

            //替换解密内容
            String decrypt = RsaUtils.decrypt(appBaseMsg.getEncryption(), integerStringMap.get(1));
            appBaseMsg.setEncryption(decrypt);

            Object proceed = joinPoint.proceed(args);
            return proceed;

        } else {
            //TODO 以后如果需要做其他属性的解码，则在此写对应方法
        }

        return null;
    }
}
