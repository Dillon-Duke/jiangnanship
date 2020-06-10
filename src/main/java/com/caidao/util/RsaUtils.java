package com.caidao.util;


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
        byte[] decoded = Base64.getDecoder().decode(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        String outStr = new String(cipher.doFinal(inputByte));
        return outStr;
    }
    public static void main(String[] args) throws Exception {

        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCWHrdSwXP5VBqVBtuiDAS43UhmQFuRNraAjtT+Lg2yEN16g0j9bPhyHQkzWjUL00GxuVHqSLvt2jCCsZInQNBkVvsQxlkaPQW+xp3KgGl1/IqsojaiTxOkrd1Uyvh0E3bKYP7LAwly3sYxuwgESQK9Qpq+h2QDm8/sxdPTMVGFkwIDAQAB";
        String pri = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJYet1LBc/lUGpUG26IMBLjdSGZAW5E2toCO1P4uDbIQ3XqDSP1s+HIdCTNaNQvTQbG5UepIu+3aMIKxkidA0GRW+xDGWRo9Bb7GncqAaXX8iqyiNqJPE6St3VTK+HQTdspg/ssDCXLexjG7CARJAr1Cmr6HZAObz+zF09MxUYWTAgMBAAECgYA8Zh9cLHl811nam+fCSxObTWzTtxEgW+MR1mXb3quj+SUjJa84R5+uSgBpqOGu4FCOMQHEQX9bM25RJyCmqm0tU9OcDLNUjhXL3V5B3phi8R16yXnzOBoNSX999Ll26s99vENzjkgtmRXPqlttPt/w5F6LBDjHds3uYaVI1XIwAQJBAM88y+PRIywZiZ4C3bKL92HEmtTG3XZkqra6KBAQ9VP/ywCfKKVIAOJXJ7MyBcto0gI/nIAx5onOJHeBdVPXJ8kCQQC5cWCISOKMKXhj2MH1/6eFpLYVn6VK4n82ZFhW8uCSCxvYSM0RXrBrn7sdyVZ34Yhp1lLHKK4BpNbBtCEF6ih7AkAOlKV3wugpmyZekq8Md+KQ42dNXyyHvu9v6csVzkukfFPJGGmgr184jY9CgVt9A0P7WoRlVJ12xqU0L0yU+IdZAkAHqlUYLRMYQhqU3f/egEvXbWCNckTt3IDvqaQxK+b2gnkLLq8zWZunRzn/2DO3zt3Jqi9PxjGzJbIu3ZhD21MDAkBGwnr3p4P+ocm8bop3THCiyGX5PBXHuMzZknai3MjudRcev+yietEJv8JFpFBGmljI6EAvdSjmjqtY+MjjKqaB";
        String message = "{'username':'zhangsan','password':'123'}";
        System.out.println("原文:" + message);
        String messageEn = encrypt(message, pub);
        System.out.println("加密：" + messageEn);
        String messageDe = decrypt(messageEn, pri);
        System.out.println("解密:" + messageDe);

    }
}

