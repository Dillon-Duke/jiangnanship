package com.caidao.controller;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.caidao.entity.SysUser;
import com.caidao.param.Menu;
import com.caidao.param.UserParam;
import com.caidao.service.SysMenuService;
import com.caidao.util.PropertyUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
public class LoginController {
	
	@Autowired
	private StringRedisTemplate redis;
	
	@Autowired
	private SysMenuService sysMenuService;

	/**
	 * 获取用户的验证码
	 * @param uuid
	 * @param response
	 */
	@ApiOperation("获取验证码")
	@ApiImplicitParams(@ApiImplicitParam(name="uuid",value="前端传来的uuid"))
	@GetMapping("/captcha.jpg")
	public void validataCode(@RequestParam(required = true)String uuid,HttpServletResponse response) {
		//生成验证码
		 CircleCaptcha createCircleCaptcha = CaptchaUtil.createCircleCaptcha(200, 50, 1, 2);
		ServletOutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			createCircleCaptcha.write(outputStream);
			redis.opsForValue().set(PropertyUtils.VALCODE_PRIFAX+uuid, createCircleCaptcha.getCode(), Duration.ofSeconds(60));
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (outputStream !=null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}finally {
					outputStream = null;
				}
			}
		}
		 
	}
	
	/**
	 * 用户的登录接口
	 * @param userParam
	 * @return
	 */
	@ApiOperation("login接口")
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody UserParam userParam) {
		
		Subject subject = SecurityUtils.getSubject();
		UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(userParam.getPrincipal(), userParam.getCredentials());
		String token = null;
		try {
			//校验验证码
			checkCode(userParam.getSessionUUID(),userParam.getImageCode());
			//校验登录信息
			subject.login(usernamePasswordToken);
			token = subject.getSession().getId().toString();		
			
			//设置UUID
			redis.opsForValue().set(PropertyUtils.USER_LOGIN_SESSION_ID+userParam.getPrincipal(), token);
		} catch (CredentialsException e) {
			return ResponseEntity.badRequest().body("密码错误");
		}	catch (AccountException e) {
			return ResponseEntity.badRequest().body("账户异常");
		}catch (AuthenticationException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok(token);
	}

	//校验验证码
	private void checkCode(String sessionUUID, String imageCode) {
		if (!StringUtils.hasText(imageCode) ||!StringUtils.hasText(sessionUUID) ) {
			throw new AuthenticationException("验证码校验失败");
		}
		
		//获取redis里面的验证码并校验
		String redisImageCode = redis.opsForValue().get(PropertyUtils.VALCODE_PRIFAX+sessionUUID);
		if (!StringUtils.hasText(redisImageCode)) {
			throw new AuthenticationException("验证码超时，请重新验证");
		}
		
		if (!imageCode.equals(redisImageCode)) {
			throw new AuthenticationException("验证码错误，请重新输入");
		}
		
		//验证码使用之后 删除
		redis.delete(PropertyUtils.VALCODE_PRIFAX+sessionUUID);
	}
	
	/**
	 * 菜单数据的加载
	 * @return
	 */
	@GetMapping("sys/menu/nav")
	@ApiOperation("用户登录查询对应菜单")
	public ResponseEntity<Object> getMenuList(){	
		SysUser user = (SysUser)SecurityUtils.getSubject().getPrincipal();
		if (user == null) {
			throw new NullPointerException("shiro无此用户");
		}
		log.info("{}用户登录",user.getUsername());
		Map<String, Object> result = new HashMap<String,Object>();
		List<Menu> menuList = sysMenuService.getMenuListByUserId(user.getUserId());
		//用户的菜单列表
		result.put("menuList", menuList);
		//用户的菜单权限
		List<String> authorities = sysMenuService.getAuth2ByUslerId(user.getUserId());
		result.put("authorities", authorities);
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 登录之后或等登录对象
	 * @return
	 */
	@GetMapping("/sys/user/info")
	@ApiOperation("登录获得用户对象")
	public ResponseEntity<Object> getUserInfo(){
		SysUser user = (SysUser)SecurityUtils.getSubject().getPrincipal();
		log.info("登录获取{}对象",user.getUsername());
		return ResponseEntity.ok(user);
	}
	
	@PostMapping("sys/logout")
	public ResponseEntity<Void> userLogOut(){
		return ResponseEntity.ok().build();
	}


}
