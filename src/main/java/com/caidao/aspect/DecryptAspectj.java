package com.caidao.aspect;

import com.caidao.common.ResponseEntity;
import com.caidao.exception.MyException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author tom
 */
public class DecryptAspectj {

    @Around("@annotation(com.caidao.anno.Decrypt)")
    public ResponseEntity<Object> decryptAspectjDate(ProceedingJoinPoint joinPoint){

        //原始字符串
        String srcData = null;
        //公钥
        PublicKey publicKey = null;
        //签名
        String sign = null;

        Object proceed;
        try {
            proceed = joinPoint.proceed(joinPoint.getArgs());
            byte[] keyBytes = publicKey.getEncoded();
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(key);
            signature.update(srcData.getBytes());
            boolean verify = signature.verify(Base64.decodeBase64(sign.getBytes()));
            if (verify) {
                return ResponseEntity.ok(proceed);
            }
            throw new MyException("后台验签失败，请联系管理员");
        } catch (Throwable throwable) {
            throw new MyException("后台解签失败，请联系管理员");
        }
    }

}
