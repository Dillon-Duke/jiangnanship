package com.caidao.controller.front;


import com.caidao.common.ResponseEntity;
import com.caidao.exception.MyException;
import com.caidao.param.UserParam;
import com.caidao.pojo.DeptUser;
import com.caidao.service.DeptConfigService;
import com.caidao.service.DeptUserService;
import com.caidao.util.PropertyUtils;
import com.caidao.util.RsaUtils;
import com.caidao.util.UserLoginTokenUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Assert;
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author tom
 */

@RestController
@Slf4j
public class AppLoginController {

	public static final Logger logger =  LoggerFactory.getLogger(AppLoginController.class);
	
	@Autowired
	private StringRedisTemplate redis;

	@Autowired
	private DeptUserService deptUserService;

	@Autowired
	private DeptConfigService deptConfigService;

	/**
	 * 获取用户登录的公钥
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	@ApiOperation("获取用户登录的公钥")
	@GetMapping("/getPublicKey")
	public ResponseEntity<Map<String,String>> getAppUserPublicKey() throws NoSuchAlgorithmException {

		log.info("获取用户登录用的公钥");
		//获取加密解密数据
		Map<Integer, String> integerStringMap = RsaUtils.genKeyPair();
		String uuid = UUID.randomUUID().toString();
		redis.opsForValue().set(PropertyUtils.APP_USER_PRIVATE_KEY + uuid,integerStringMap.get(1),5, TimeUnit.MINUTES);
		Map<String, String> map = new HashMap<>(2);
		map.put("publicKey",integerStringMap.get(0));
		map.put("uuid",uuid);
		return ResponseEntity.ok(map);
	}


	/**
	 * app用户登录
	 * @param userParam
	 * @return
	 */
	@ApiOperation("前台登录接口")
	@PostMapping("/appLogin")
	public ResponseEntity<Map<String,String>> login(@RequestBody UserParam userParam) {

		Assert.notNull(userParam,"用户名不能为空");
		log.info("用户名为{}请求登录",userParam.getPrincipal());

		try {

			Subject subject = SecurityUtils.getSubject();
			UserLoginTokenUtils userLoginTokenUtils = new UserLoginTokenUtils(userParam.getPrincipal(), userParam.getCredentials(),PropertyUtils.APP_USER_REALM);

			//校验登录信息
			subject.login(userLoginTokenUtils);
			String token = subject.getSession().getId().toString();

			//将登录的公钥私钥信息存进redis里面
			Map<Integer, String> integerStringMap = RsaUtils.genKeyPair();
			redis.opsForValue().set(PropertyUtils.APP_USER_PUBLIC_KEY + token,integerStringMap.get(0),30, TimeUnit.MINUTES);
			redis.opsForValue().set(PropertyUtils.APP_USER_PRIVATE_KEY + token,integerStringMap.get(1),30, TimeUnit.MINUTES);

			//将所有登录用户信息token放在redis中，之后修改密码时删除对应的token
			DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
			redis.opsForHash().put(PropertyUtils.ALL_USER_TOKEN,deptUser.getUserSalt(),token);

			Map<String, String> map = new HashMap<>(2);
			map.put("publicKey",integerStringMap.get(0));
			map.put("token",token);
			return ResponseEntity.ok(map);
		} catch (Exception e){
			throw new MyException("账号或者密码错误，登录失败");
		}
    }

	/**
	 * 通过用户ID获取用户的权限
	 * @return
	 */
	@ApiOperation("获得用户的权限")
	@GetMapping("/getHomePage")
	public ResponseEntity<Map<String, Object>> getHomePage(){

		DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> map = new HashMap<>(3);
		//获取用户的权限列表
		List<String> deptUserAuthorities = deptConfigService.getPowerByUserId(deptUser.getUserId());
		map.put("userAuthorities", deptUserAuthorities);

		//获得用户首页个人信息列表
		Map<String,String> userMassage = deptUserService.getDeptUserMassage(deptUser);
		map.put("userMassage",userMassage);

		//获取用户的首页信息列表
		Integer userMassageCount = deptUserService.getAppMassageCount(deptUser.getUserId());
		map.put("appHomePage", userMassageCount);
		return ResponseEntity.ok(map);
	}

	/**
	 * 根据用户名和手机判断是否有这个人
	 * @param username
	 * @return
	 */
	@GetMapping("/app/user/checkNameAndPhone")
	@ApiOperation("检查用户名和手机是否正确")
	public ResponseEntity<DeptUser> beforeForgetPass(String username, String phone)  {

		Assert.notNull(username,"用户名不能为空");
		Assert.notNull(phone,"手机号不能为空");
		log.info("查询用户名为{}，手机号为{}的用户",username,phone);
		DeptUser deptUser = deptUserService.findUserByUsernameAndPhone(username,phone);
		if (deptUser == null){
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.ok(deptUser);
	}

	/**
	 * 发送短信验证码
	 * @param phone
	 * @return
	 */
	@GetMapping("/app/user/message")
	@ApiOperation("调用三方接口想用户发送验证码")
	public ResponseEntity<Void> getMessage(String phone){

		Assert.notNull(phone,"手机号不能为空");

		//TODO 是否要使用短信业务 使用哪个短信业务系统 ，或者是只能是后台工作人员改密码

		//生成随机6位数字
		int code = (int)((Math.random()*9+1)*100000);
		//向redis 里面存6位的随机数组 redis储存时间为60S
		redis.opsForValue().set(PropertyUtils.MASSAGE_CODE + phone, code + "", 1,TimeUnit.MINUTES);

		return ResponseEntity.ok().build();
	}

	/**
	 * 更新用户的密码
	 * @param userParam
	 * @return
	 */
	@ApiOperation("忘记密码，更新用户的密码")
	@PostMapping("/app/user/updatePass")
	public ResponseEntity<String> updateUserPassword(@RequestBody UserParam userParam){
		Assert.notNull(userParam,"参数不能为空");
		log.info("用户手机号为{}的用户更新了密码",userParam.getPhone());

		//校验验证码是否正确
		checkCode(PropertyUtils.MASSAGE_CODE+userParam.getPhone(), userParam.getImageCode());

		//更新用户密码
		boolean update = deptUserService.updatePassByPhone(userParam);
		if (update){
			return ResponseEntity.ok("用户密码更新成功");
		}
		return ResponseEntity.error("用户密码更新失败");
	}

	/**
	 * app用户退出登录
	 * @return
	 */
	@PostMapping("/appLogout")
	@ApiOperation("app退出账号")
	public ResponseEntity<Void> logout(){

		//删除用户在redis 里面的token
		String token = SecurityUtils.getSubject().getSession().getId().toString();
		DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
		redis.delete(PropertyUtils.USER_SESSION + token);
		redis.delete(PropertyUtils.APP_USER_PRIVATE_KEY+token);
		redis.delete(PropertyUtils.APP_USER_PUBLIC_KEY+token);
		redis.opsForHash().delete(PropertyUtils.ALL_USER_TOKEN,deptUser.getUserSalt());
		return ResponseEntity.ok().build();
	}

	/**
	 * 校验验证码
	 */
	private void checkCode(String phone, String code) {
		if (!StringUtils.hasText(code) || !StringUtils.hasText(phone)) {
			throw new MyException("验证码校验失败");
		}

		//获取redis里面的验证码并校验
		String redisImageCode = redis.opsForValue().get(PropertyUtils.VALCODE_PRIFAX + phone);
		if (!StringUtils.hasText(redisImageCode)) {
			throw new MyException("验证码超时，请重新验证");
		}
		if (!code.equals(redisImageCode)) {
			throw new MyException("验证码错误，请重新输入");
		}
	}
}