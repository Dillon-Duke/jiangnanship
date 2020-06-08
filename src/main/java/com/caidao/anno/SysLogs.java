package com.caidao.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用来记录用户方法的操作 将注解加到方法上面就行
 * @author tom
 * @since 2020-5-12
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SysLogs {
	
	/* 当前是什么操作 */
	String value() default "";

}
