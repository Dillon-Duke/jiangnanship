package com.caidao.controller.back;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
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
import com.caidao.service.SysUserService;
import com.caidao.util.PropertyUtils;
import com.caidao.util.RestTemplateUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Assert;
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
import org.springframework.web.client.RestTemplate;

/**
 * @author tom
 */

@RestController
@Slf4j
public class LoginController {
	
	@Autowired
	private StringRedisTemplate redis;
	
	@Autowired
	private SysMenuService sysMenuService;

	@Autowired
	private SysUserService sysUserService;

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
		Map<String, Object> result = new HashMap<String,Object>(2);
		List<Menu> menuList = sysMenuService.getMenuListByUserId(user.getUserId());

		//用户的菜单列表
		result.put("menuList", menuList);
		//用户的菜单权限
		List<String> authorities = sysMenuService.getAuth2ByUslerId(user.getUserId());
		result.put("authorities", authorities);
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 登录之后获取登录对象
	 * @return
	 */
	@GetMapping("/sys/user/info")
	@ApiOperation("登录获得用户对象")
	public ResponseEntity<Object> getUserInfo(){
		SysUser user = (SysUser)SecurityUtils.getSubject().getPrincipal();
		log.info("登录获取{}对象",user.getUsername());
		return ResponseEntity.ok(user);
	}

	/**
	 * 根据用户名和手机判断是否有这个人
	 * @param username
	 * @param phone
	 * @return
	 */
	@GetMapping("/sys/user/checkNameAndPhone")
	@ApiOperation("检查用户名和手机是否正确")
	public ResponseEntity<SysUser> beforeForgetPass(String username,String phone)  {
		if (username == null || phone == null){
			Assert.notNull("用户名或者手机号错误");
		}

		log.info("查询用户名为{}，手机号为{}的用户",username,phone);
		SysUser sysUser = sysUserService.findUserByUsernameAndPhone(username,phone);
		if (sysUser == null){
			return ResponseEntity.ok(null);
		}
		return ResponseEntity.ok(sysUser);
	}

	/**
	 * 发送短信验证码
	 * @param phone
	 * @return
	 */
	@GetMapping("sys/user/message")
	@ApiOperation("调用三方接口想用户发送验证码")
	public ResponseEntity<Void> getMessage(String phone){

		//TODO 是否要使用短信业务 使用哪个短信业务系统 ，或者是只能是后台工作人员改密码

		//生成随机6位数字
		int code = (int)((Math.random()*9+1)*100000);

		//向redis 里面存6位的随机数组 redis储存时间为60S
		redis.opsForValue().set(PropertyUtils.MASSAGE_CODE + phone, code + "", Duration.ofSeconds(60));

		return ResponseEntity.ok().build();
	}

	/**
	 * 更新用户的密码
	 * @param sysUser
	 * @param code
	 * @return
	 */
	@ApiOperation("忘记密码，更新用户的密码")
	@PostMapping("/sys/user/updatePass")
	public ResponseEntity updateUserPassword(@RequestBody SysUser sysUser , String code){
		Assert.notNull(sysUser,"更新参数不能为空");
		log.info("用户{}更新了密码",sysUser.getUsername());

		//校验验证码是否正确
		checkCode(PropertyUtils.MASSAGE_CODE+sysUser.getPhone(),code);

		//更新用户验证码
		boolean update = sysUserService.updatePassById(sysUser);
		if (update){
			return ResponseEntity.ok("用户密码更新成功");
		}
		return ResponseEntity.ok("用户密码更新失败");

	}

	/**
	 * 校验验证码
	 */
	private void checkCode(String sessionUuid, String imageCode) {
		if (!StringUtils.hasText(imageCode) ||!StringUtils.hasText(sessionUuid) ) {
			throw new AuthenticationException("验证码校验失败");
		}

		//获取redis里面的验证码并校验
		String redisImageCode = redis.opsForValue().get(PropertyUtils.VALCODE_PRIFAX+sessionUuid);
		if (!StringUtils.hasText(redisImageCode)) {
			throw new AuthenticationException("验证码超时，请重新验证");
		}
		if (!imageCode.equals(redisImageCode)) {
			throw new AuthenticationException("验证码错误，请重新输入");
		}

		//验证码使用之后 删除
		redis.delete(PropertyUtils.VALCODE_PRIFAX+sessionUuid);
	}

	@PostMapping("/sys/logout")
	@ApiOperation("退出账号")
	public ResponseEntity<Void> logout(){
		return ResponseEntity.ok().build();
	}

}