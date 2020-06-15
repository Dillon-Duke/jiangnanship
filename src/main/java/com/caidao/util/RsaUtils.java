package com.caidao.util;


import com.caidao.exception.MyException;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户登录的加密解密算法
 * @author tom
 * @since 2020-06-08
 */
public class RsaUtils {

    /**
     * 用于封装随机产生的公钥与私钥
     */
    private static Map<Integer, String> keyMap = new HashMap<Integer, String>();
    /**
     * 随机生成密钥对
     * @return
     */
    public static Map<Integer, String> genKeyPair() throws NoSuchAlgorithmException {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器
        keyPairGen.initialize(PropertyUtils.KEY_SIZE, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        // 得到私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        // 得到私钥字符串
        String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        // 将公钥和私钥保存到Map
        //0表示公钥
        keyMap.put(0, publicKeyString);
        //1表示私钥
        keyMap.put(1, privateKeyString);
        return keyMap;
    }
    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String str, String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = Base64.getDecoder().decode(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String outStr = Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }
    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String str, String privateKey) throws Exception {
        //64位解码加密后的字符串
        byte[] inputByte = Base64.getDecoder().decode(str);
        //base64编码的私钥
        if (privateKey == null || privateKey == "" || privateKey.isEmpty()){
            throw new MyException("私钥为空");
        }
        byte[] decoded = Base64.getDecoder().decode(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        String outStr = new String(cipher.doFinal(inputByte));
        return outStr;
    }
    public static void main(String[] args) throws Exception {

        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCSnhDx5Og17lGzp7RX/wDOgZSF391iT2jSfYKZe8qtZokMgAQKCX9khIFUS+/x4bw/rFIO0MNEK5YFS71D/ey2FqSoL24av0S/yn64n5TH5PleMdY6g7p8q3TOnXZoWn0Rt2cf6iqBbL+JLfBUJlJWEWQTOmjT8fpoYV68H9VghQIDAQAB";
        String pri = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIFFzhsp/8AD6W6ZNWXcubJV72Bit0+h3cyb0N0jdqW41QevkYZ6XnAS0wBW3hwkR30AgpG2MOvgAf3S5Q5t8/eKOlwoUQO40NJr0TyWiMgOo4vKo05LVNZPxzrH9fMQFjwMt+jgYkjH32uN1PkSdQ2W1EnuZtSaZA7Ph5TTq7I1AgMBAAECgYBnxSEtrQkfmgRIQ7QZkJWAEnuVY6jCw6zIEkwXN+CGJ1jaUpT+3jvwuyulmhWxgW3hagdl8FpU/fybRC+a5ahwaKO6xZoJQgGFxJqT5SIByyxVYnmfyYYgneiZhTedEuGE7ZfgcC2h+93rRr0HmDWMpnIr6GdUyHCcDQu4XtEgAQJBAL6g53CtzhMbIsBuuy3a1bpiXrZVRkDgSOUAuHRzrQ7MgN3FIR0WpB+OOPWRSKnmZ3BFEavf/Fr74OojTPmygTECQQCtmoeDhsvvJ/H5tYohaO4Pg6o0T0Qh8Jwwzba8YRgsReDLJhlR3U5/1L6P+JA6Ks9vcOvUL1hTn7+ox5osxOBFAkBTtn1scicJJOWB9B6m6G7rRr+o+pG7c8MPLd3S5emdkhkVSrhb+dFhOiexT3fFhLTwDKTSAa7klbAToiTvkoBBAkEAqK01sED/0z5vqR2PuciXTjGy5aKC46tCR1UJkV2RvCqlMR2yik1KIYBXK9HqaOoBpRkFuqPJ4W6dLWPLVGZyNQJANR939SjmpuDP3j0PRB2S1V9IjZZjqa3MbLZgVaT2sH4Z9VPGce2waCM9xOxQHlqe8OYOg4MSPW2bWFTbFhSi9w==";
        String message = "{'username':'zhangsan','password':'123'}";
        System.out.println("原文:" + message);
        String messageEn = encrypt(message, pub);
        System.out.println("加密：" + messageEn);
        String messageDe = decrypt(messageEn, pri);
        System.out.println("解密:" + messageDe);

    }
}

