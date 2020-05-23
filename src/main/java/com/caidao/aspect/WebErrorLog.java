package com.caidao.aspect;

import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author dillon
 * @Date 2020/3/22 15:02
 * @Version 1.0
 *
 * 全局异常捕获 拦截web错误日志
 */

@RestControllerAdvice
public class WebErrorLog {

    private static final Logger log = LoggerFactory.getLogger(WebErrorLog.class);


	/**
	 * 运行时异常
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<String> runtimeException(RuntimeException e){
		log.error("系统内部错误",e );

		return ResponseEntity.badRequest().body(e.getMessage());
	}

	/**
	 * 用户权限认证 无权抛异常
	 * @param authorizationException
	 * @return
	 */
	@ExceptionHandler(AuthorizationException.class)
	public ResponseEntity<String> authorizationException(AuthorizationException authorizationException){
		log.info("该用户无此权限",authorizationException);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("该用户无此权限");

		
	}
  

}
