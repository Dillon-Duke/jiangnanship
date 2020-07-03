package com.caidao.util;

import com.caidao.exception.MyException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @author tom
 */
public class AesUtils {

    private static final Base64.Decoder decoder = Base64.getDecoder();
    private static final Base64.Encoder encoder = Base64.getEncoder();

    /**
     * 加密
     * @param sSrc  加密前的字符串
     * @param sKey 加密KEY
     * @return
     * @throws Exception
     */
    public static String encrypt (String sSrc, String sKey) throws  Exception{
        // 判断Key是否为16位
        if (sKey.length() != PropertyUtils.AES_KEY_LENGTH) {
            throw new MyException("加密算法长度不符合要求");
        }
        byte[] raw = sKey.getBytes(PropertyUtils.ENCODING);
        SecretKeySpec sKeySpec = new SecretKeySpec(raw, PropertyUtils.AES_KEY_ALGORITHM);
        //"算法/模式/补码方式"
        Cipher cipher = Cipher.getInstance(PropertyUtils.DEFAULT_CIPHER_ALGORITHM);
        //使用CBC模式，需要一个向量iv，可增加加密算法的强度
        IvParameterSpec iv = new IvParameterSpec(sKey.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());
        String encodedText = encoder.encodeToString(encrypted);
        //此处使用BASE64做转码功能，同时能起到2次加密的作用。
        return encodedText;
    }

    /**
     * 解密
     * @param sSrc  解密前的字符串
     * @param sKey 解密KEY
     * @return
     */
    public static String decrypt(String sSrc, String sKey) {
        try {
            // 判断Key是否为16位
            if (sKey.length() != PropertyUtils.AES_KEY_LENGTH) {
                throw new MyException("加密算法长度不符合要求");
            }
            byte[] raw = sKey.getBytes(PropertyUtils.ENCODING);
            SecretKeySpec sKeySpec = new SecretKeySpec(raw, PropertyUtils.AES_KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(PropertyUtils.DEFAULT_CIPHER_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(sKey.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);
            //先用base64解密
            byte[] encrypted1 = decoder.decode(sSrc);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        String cKey = "1212121212121212";
        // 需要加密的字串
        String cSrc = "2114211";
        System.out.println(cSrc);
        // 加密
        long lStart = System.currentTimeMillis();
        String enString = encrypt(cSrc, cKey);
        System.out.println("加密后的字串是：" + enString);
        long lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("加密耗时：" + lUseTime + "毫秒");
        lStart = System.currentTimeMillis();
        String deString = decrypt(enString, cKey);
        System.out.println("解密后的字串是：" + deString);
        lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("解密耗时：" + lUseTime + "毫秒");
    }
}
