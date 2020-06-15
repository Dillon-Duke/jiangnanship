package com.caidao.exception;

import com.caidao.common.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author tom
 * @since 2020-05-12
 */
@RestControllerAdvice
@Slf4j
public class MyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 提供无参数的构造方法 */
    public MyException() {
        super();
    }

    /** 提示信息参数的构造方法*/
    public MyException(String message) {
        super(message);
    }

    /** 提示信息，异常详细参数的构造方法 */
    public MyException(String message,Throwable e) {
        super(message,e);
    }

    /** 提示信息，异常信息的构造方法 */
    public MyException(Throwable e) {
        super(e);
    }

    /** 后两个参数暂时不知道意思，请大神来填写这个注解 */
    public MyException(String message, Throwable e, boolean enableSuppression, boolean writableStackTrace) {
        super(message,e,enableSuppression,writableStackTrace);
    }

    /** 自定义异常抛出显示 */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MyException.class)
    public ResponseEntity<String> myException(MyException myException){
        log.info("自定义异常",myException);
        return ResponseEntity.error(myException.getMessage());
    }
}
