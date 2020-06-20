package com.caidao.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此方法定义的是将返回值通过密文加签的方法传回前端，并不是做里明文加密，前端只需要通过明文验签的方法则可知道是不是对应的服务区往前传的
 * @author tom
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Encrypt {
}
