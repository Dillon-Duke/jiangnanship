package com.caidao.aspect;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import com.caidao.anno.SysLogs;
import com.caidao.entity.SysLog;
import com.caidao.entity.SysUser;
import com.caidao.service.SysLogService;
import com.caidao.util.SysLogIpUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.json.JSONUtil;

/**
 * @author Dillon
 * @since 2020-05-18
 * @Component  注入到spring中
 */

@Aspect
@Component
public class SysLogAspectj {
	
	@Autowired
	private SysLogService sysLogService;
	
	@Around("@annotation(com.caidao.anno.SysLogs)")
	public Object sysLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
		
		SysLog sysLog = new SysLog();
		
		//设置创建日志的时间 为当前时间
		sysLog.setCreateDate(LocalDateTime.now());
		 
		//获取IP为使用者用户的真实ip  使用requestcontextholder 。get之后 强转为servletrequestattribute 获取request
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();
		sysLog.setIp(SysLogIpUtils.getSysLogIpUtils(request));
		
		//从反射中获取一个签名 强转为方法签名
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		sysLog.setMethod(method.getName());
		
		//通过反射获得方法 通过方法获得方法上面的注解  并且注解上面的值一定不为空
		SysLogs operation = method.getAnnotation(SysLogs.class);
		sysLog.setOperation(operation.value());
		
		//从反射中获取方法的参数 使用huto的json工具
		Object[] args = joinPoint.getArgs();		
		sysLog.setParams(args == null?"":JSONUtil.toJsonStr(args));
		
		//方法结束的时间减去方法开始的时间为方法调用的时间

		Long start = System.currentTimeMillis();
		Object result = joinPoint.proceed(joinPoint.getArgs());
		Long end = System.currentTimeMillis();
		sysLog.setTime(end-start);
		
		//设置操作用户 从session中查询用户 找到用户名
		SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
		sysLog.setUsername(sysUser.getUsername());
		
		//将日志记录在数据库中
		sysLogService.save(sysLog);
		
		return result;
	}

}
