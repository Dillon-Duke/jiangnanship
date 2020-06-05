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

    /** 提供一个有参数的构造方法，可自动生成 */
    public MyException(String message) {
        super(message);
    }

    /** 提供一个有参数的构造方法，可自动生成 */
    public MyException(String code,String message) {
        super(message);
    }
}
