package com.caidao.util;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.util.ByteSource;

/**
 * @author tom
 * @since 2020-5-12
 * 处理用户密码的盐值生成类，目前用户使用的是 {哈希+加盐+次数} 前两个没有使用
 */

public class Md5Utils {

	/**
	 * 私有构造器
	 */
	private Md5Utils() {}

	public static String getMd5EncryptWithSalt(String content, String salt) {
		ByteSource bytes = ByteSource.Util.bytes(salt.getBytes());
		Md5Hash md5Hash = new Md5Hash(content, bytes);
		return md5Hash.toString();
	}

	/**
	 * 哈希+加盐+次数
	 * @param password
	 * @param salt
	 * @param hashIterations
	 * @return
	 */
	public static String getHashAndSaltAndTime(String password,String salt,Integer hashIterations) {
		ByteSource bytes = ByteSource.Util.bytes(salt.getBytes());
		Md5Hash md5SaltIterateHash = new Md5Hash(password, bytes, hashIterations);
		return md5SaltIterateHash.toString();
	}

	public static void main(String[] args) {
	}

}
