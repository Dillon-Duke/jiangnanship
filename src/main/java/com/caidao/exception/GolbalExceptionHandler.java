package com.caidao.exception;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * @Author dillon
 * @Date 2020/3/22 15:02
 * @Version 1.0
 *
 * 全局异常捕获 拦截web错误日志
 */

@RestControllerAdvice
public class GolbalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GolbalExceptionHandler.class);

    /** 自定义异常抛出显示 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
	public ResponseEntity<String> runtimeException(RuntimeException runtimeException){
		log.info("自定义异常",runtimeException);
		return ResponseEntity.badRequest().body(runtimeException.getMessage());
	}

	/** 用户权限认证 无权抛异常 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(AuthorizationException.class)
	public ResponseEntity<String> authorizationException(AuthorizationException authorizationException){
		log.info("用户无此权限",authorizationException);
		return ResponseEntity.badRequest().body("用户无此权限");
	}

	/** 用户权限认证 用户密码错误 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(CredentialsException.class)
	public ResponseEntity<String> credentialsException(CredentialsException credentialsException){
		log.info("用户密码错误",credentialsException);
		return ResponseEntity.badRequest().body("用户密码错误");
	}

	/** 用户权限认证 用户账户异常 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(AccountException.class)
	public ResponseEntity<String> accountException(AccountException accountException){
		log.info("用户账户异常",accountException);
		return ResponseEntity.badRequest().body("用户账户异常");
	}

	/** 用户权限认证 IO异常 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(IOException.class)
	public ResponseEntity<String> ioException(IOException ioException){
		log.info("IO异常",ioException);
		return ResponseEntity.badRequest().body(ioException.getMessage());
	}

}
