package com.caidao.aspect;

import com.caidao.service.AppCommonMsgService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author tom
 */
@Aspect
@Component
public class ApplyCallTimesAspectj {

    @Autowired
    private AppCommonMsgService appCommonMsgService;

    @Before("@annotation(com.caidao.anno.ApplyCallTimes)")
    public void  getApplyCallTimes(JoinPoint point) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();

    }
}
