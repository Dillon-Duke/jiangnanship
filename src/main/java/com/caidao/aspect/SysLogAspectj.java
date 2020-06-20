package com.caidao.aspect;

import cn.hutool.json.JSONUtil;
import com.caidao.anno.SysLogs;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.SysLog;
import com.caidao.pojo.SysUser;
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

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

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
	public Object sysLogAroundAspectj(ProceedingJoinPoint joinPoint) throws Throwable {
		
		SysLog log = new SysLog();
		
		//设置创建日志的时间 为当前时间
		log.setCreateDate(LocalDateTime.now());
		 
		//获取IP为使用者用户的真实ip  使用requestcontextholder 。get之后 强转为servletrequestattribute 获取request
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();
		log.setIp(SysLogIpUtils.getSysLogIpUtils(request));
		
		//从反射中获取一个签名 强转为方法签名
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		log.setMethod(method.getName());
		
		//通过反射获得方法 通过方法获得方法上面的注解  并且注解上面的值一定不为空
		SysLogs operation = method.getAnnotation(SysLogs.class);
		log.setOperation(operation.value());
		
		//从反射中获取方法的参数 使用huto的json工具
		Object[] args = joinPoint.getArgs();
		String jsonStr = JSONUtil.toJsonStr(args);

		if (jsonStr.length() > 250){
			jsonStr = jsonStr.substring(0,250) + "...";
		}
		log.setParams(args == null?"": jsonStr);
		
		//方法结束的时间减去方法开始的时间为方法调用的时间
		Long start = System.currentTimeMillis();
		Object result = joinPoint.proceed(joinPoint.getArgs());
		Long end = System.currentTimeMillis();
		log.setTime(end-start);
		
		//设置操作用户 从session中查询用户 找到用户名
		try {
			DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
			log.setUsername(deptUser.getUsername());
		} catch (Exception e){
			SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
			log.setUsername(sysUser.getUsername());
		}
		
		//将日志记录在数据库中
		sysLogService.save(log);
		return result;
	}

}
