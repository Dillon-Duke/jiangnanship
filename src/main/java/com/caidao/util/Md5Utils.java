package com.caidao.util;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.util.ByteSource;

/**
 * @author tom
 * @since 2020-5-12
 */

public class Md5Utils {

	/**
	 * 私有构造器
	 */
	private Md5Utils() {}

	/**
	 * 哈希
	 * @param password
	 * @return
	 */
	public static String getHash(String password) {
		Md5Hash md5Hash = new Md5Hash(password);
		return md5Hash.toString();
	}

	/**
	 * 哈希+加盐
	 * @param password
	 * @param salt
	 * @return
	 */
	public static String getHashAndSalt(String password,ByteSource salt) {
		Md5Hash md5SaltHash = new Md5Hash(password, salt);
		return md5SaltHash.toString();
	}

	/**
	 * 哈希+加盐+次数
	 * @param password
	 * @param bytes
	 * @param hashIterations
	 * @return
	 */
	public static String getHashAndSaltAndTime(String password,ByteSource bytes,Integer hashIterations) {
		Md5Hash md5SaltIterateHash = new Md5Hash(password, bytes, hashIterations);
		return md5SaltIterateHash.toString();
	}

	public static void main(String[] args) {
		System.out.println(getHashAndSaltAndTime("1",ByteSource.Util.bytes("11111111111111112222333344445555".getBytes()),1024));
	}

}
