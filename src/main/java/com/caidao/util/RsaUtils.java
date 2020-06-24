package com.caidao.util;


import com.alibaba.fastjson.JSONObject;
import com.caidao.exception.MyException;
import com.caidao.param.UserParam;

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
     * 获得rsa公钥
     * @return
     */
    public static String getPublicKey(){
        return keyMap.get(0);
    }

    /**
     * 获得rsa私钥
     * @return
     */
    public static String getPrivateKey(){
        return keyMap.get(1);
    }

    /**
     * 将base64编码后的公钥字符串转成PublicKey实例
     */
    public static PublicKey getPublicKey(String publicKey) throws Exception{
        byte[ ] keyBytes= Base64.getDecoder().decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec=new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory=KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 将base64编码后的私钥字符串转成PrivateKey实例
     */
    public static PrivateKey getPrivateKey(String privateKey) throws Exception{
        byte[] keyBytes= Base64.getDecoder().decode(privateKey.getBytes());
        PKCS8EncodedKeySpec keySpec=new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory=KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
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

        Map<Integer, String> map = genKeyPair();
        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCoULZRTJ+VJAj+IKFp+5ZVkejaUQswBpuIFnmv+MdL52XrkCAVeGcnGKe+am1IOHuU2Pxv+g8yONh8e+oArEjD9Q54U7kfp9a7KONRaUWEYSpmPkXpgQnuacIYDqZjpLVMR/ymKM+pguFQWs7rCXogy+lGLm/zWc2Ewi6kqhZFrQIDAQAB";
        String pri = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMIi02hZeA9fYhmuokXVGDopuxfRGhcDNzxoo3bzKR+7rydoh7kuQQSUOFE1Z5ueaOniQ9P8Fzb/jBg8qm1ZxUyC5vb7iKbNFYMJvJsEvU//Ntq0wRPZ3AZw+UI2/yXMAWWA+pQizOk77EbMFSn6q/au/YX6gzc4OgocZL7BjINhAgMBAAECgYAyrAhmSkQOLyVZ8r0kYRSrycyt0MRwkURPnjhcieeIAuMa9CvI6AvMeCui9r+OXPCha4+suzYMAvO8N8l8NVxLwT7NkyDHnciQ2Ee56LBcjc72mu79EGC0uAhtcG7ALHeDOvgoWnfoZ5xysOb4N0DiT8iYOQ2HsDFIUOVBKLmiAQJBAPaUuvd4i7iBqFl0xIWaTD6DxUyoEbDLe6LL7fLkuKzj1rRDVL6Gptfw4yXymymQ9YzES86K7/dawPni77pgdrECQQDJjT8Cj6F2VFx08uQMljNXK4UUr58Re3/PRHGdkG9oc36KN98QdrRgBLF6tc0/5Majc74F7mPXeHcL2w/rNGOxAkEA52FvBDMYoP0BtVet5VSBgRPzOthnKUf37y5/TTI03P87BJI93j7KJs3CyGQcF2gQEpRMMjcLsEd318SMgY5tMQJAAxssJ2vzPxGZwyujHBaMgAFpsaHrP6e5loYlghohvWhaQOMPiv9pVDl+SrfWi++IqCg2e3zrCP0QSJx9qFBMYQJBAOUC+nIX5loPnmpWf/Lp371OFIaWikOD1SyYQKUZ4P2R+ZeBvRrSO3xXKiKNPxCo5t3vYDQ5qK4wEvkrL/9mC4A=";
        UserParam param = new UserParam();
        param.setPrincipal("zhangsan");
        param.setCredentials("123");
        String message = JSONObject.toJSONString(param);
        System.out.println("原文:" + message);
        String messageEn = encrypt(message, pub);
        System.out.println("加密：" + messageEn);
        String messageDe = decrypt(messageEn, pri);
        System.out.println("解密:" + messageDe);

    }
}

