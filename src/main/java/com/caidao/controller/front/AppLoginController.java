package com.caidao.controller.front;

import com.caidao.controller.front.flatcar.FlatcarPlanController;
import com.caidao.entity.SysUser;
import com.caidao.param.UserParam;
import com.caidao.service.SysUserService;
import com.caidao.util.PropertyUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author tom
 */

@RestController
@Slf4j
public class AppLoginController {

	public Logger logger =  LoggerFactory.getLogger(AppLoginController.class);
	
	@Autowired
	private StringRedisTemplate redis;

	@Autowired
	private SysUserService sysUserService;

	/**
	 * app用户登录
	 * @param userParam
	 * @return
	 */
	@ApiOperation("前台登录接口")
	@PostMapping("/appLogin")
	public ResponseEntity<String> login(@RequestBody UserParam userParam) {

		Assert.notNull(userParam,"用户参数不能为空");
		log.info("用户{}登录了",userParam.getPrincipal());

		Subject subject = SecurityUtils.getSubject();

		UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(userParam.getPrincipal(), userParam.getCredentials());
		String token = null;
		try {

			//校验登录信息
			subject.login(usernamePasswordToken);
			token = subject.getSession().getId().toString();

			//设置UUID  默认存贮30分钟
			redis.opsForValue().set(PropertyUtils.APP_USER_LOGIN_SESSION_ID+userParam.getPrincipal(), token, 30, TimeUnit.SECONDS);
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
	 * 根据用户名和手机判断是否有这个人
	 * @param username
	 * @param phone
	 * @return
	 */
	@GetMapping("/app/user/checkNameAndPhone")
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
	@GetMapping("/app/user/message")
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
	@PostMapping("/app/user/updatePass")
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
	 * app用户退出登录
	 * @return
	 */
	@PostMapping("/sys/AppLogout")
	@ApiOperation("app退出账号")
	public ResponseEntity<Void> logout(){

		//删除用户在redis 里面的token
		SysUser sysUser = (SysUser)SecurityUtils.getSubject().getPrincipal();
		redis.delete(PropertyUtils.APP_USER_LOGIN_SESSION_ID+sysUser.getUsername());
		return ResponseEntity.ok().build();
	}

	/**
	 * 校验验证码
	 */
	private void checkCode(String massageCode, String code) {
		if (!StringUtils.hasText(code) || !StringUtils.hasText(massageCode)) {
			throw new AuthenticationException("验证码校验失败");
		}

		//获取redis里面的验证码并校验
		String redisImageCode = redis.opsForValue().get(PropertyUtils.VALCODE_PRIFAX + massageCode);
		if (!StringUtils.hasText(redisImageCode)) {
			throw new AuthenticationException("验证码超时，请重新验证");
		}
		if (!code.equals(redisImageCode)) {
			throw new AuthenticationException("验证码错误，请重新输入");
		}
	}
}