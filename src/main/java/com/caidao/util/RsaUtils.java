package com.caidao.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tom
 */
public class RsaUtils {

    /**
     * 用于封装随机产生的公钥与私钥
     */
    private static Map<String, String> keyMap = new HashMap<>(2);

    public static BASE64Encoder base64Encoder = new BASE64Encoder();
    public static BASE64Decoder base64Decoder = new BASE64Decoder();
    private static RsaUtils ourInstance = new RsaUtils();
    public static RsaUtils getInstance() {
        return ourInstance;
    }

    /**
     * 生成密钥对
     * @return
     */
    public static Map<String, String> generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator;
        keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //获取公钥
        String publicKeyStr = base64Encoder.encode(keyPair.getPublic().getEncoded());
        //获取密钥
        String privateKeyStr = base64Encoder.encode(keyPair.getPrivate().getEncoded());
        //返回公钥密钥
        keyMap.put("公钥", publicKeyStr);
        //1表示私钥
        keyMap.put("密钥", privateKeyStr);
        return keyMap;
    }

    /**
     * 获得rsa公钥
     * @return
     */
    public static String getPublicKey(){
        return keyMap.get("公钥");
    }

    /**
     * 获得rsa私钥
     * @return
     */
    public static String getPrivateKey(){
        return keyMap.get("密钥");
    }

    /**
     * 将base64编码后的公钥字符串转成PublicKey实例
     */
    private static PublicKey getPublicKey(String publicKey) throws Exception {
        byte[] keyBytes = base64Decoder.decodeBuffer(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 将base64编码后的私钥字符串转成PrivateKey实例
      */
    private static PrivateKey getPrivateKey(String privateKey) throws Exception {
        byte[] keyBytes = base64Decoder.decodeBuffer(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 公钥加密
      */
    public static String encryptByPublicKey(String publicKey,String content) throws Exception {
        // 获取公钥
        PublicKey publicKeyEntity = getPublicKey(publicKey);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKeyEntity);
        byte[] cipherText = cipher.doFinal(content.getBytes());
        String cipherStr = base64Encoder.encode(cipherText);
        return cipherStr;
    }

    /**
     * 公钥解密
     */
    public static String decryptByPublicKey(String publicKey, String content) throws Exception {
        // 获取公钥
        PublicKey publicKeyEntity = getPublicKey(publicKey);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKeyEntity);
        byte[] cipherText = base64Decoder.decodeBuffer(content);
        byte[] decryptText = cipher.doFinal(cipherText);
        return new String(decryptText);
    }

    /**
     * 私钥加密
      */
    public static String encryptByPrivateKey(String privateKey, String content) throws Exception {
        // 获取私钥
        PrivateKey privateKeyEntity = getPrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKeyEntity);
        byte[] cipherText = cipher.doFinal(content.getBytes());
        String cipherStr = base64Encoder.encode(cipherText);
        return cipherStr;
    }

    /**
     * 私钥解密
     */
    public static String decryptByPrivateKey(String privateKey, String content) throws Exception {
        // 获取私钥
        PrivateKey privateKeyEntity = getPrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKeyEntity);
        byte[] cipherText = base64Decoder.decodeBuffer(content);
        byte[] decryptText = cipher.doFinal(cipherText);
        return new String(decryptText);
    }

    public static void main(String[] args) {

    }
}
