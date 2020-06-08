package com.caidao.exception;

/**
 * @author tom
 * @since 2020-05-12
 */
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

    /** 后两个参数暂时不知道意思，请大神来填写这个注解 */
    public MyException(String message, Throwable e, boolean enableSuppression, boolean writableStackTrace) {
        super(message,e,enableSuppression,writableStackTrace);
    }

    //TODO 以后异常需要处理的时候可以在这边进行处理
}
