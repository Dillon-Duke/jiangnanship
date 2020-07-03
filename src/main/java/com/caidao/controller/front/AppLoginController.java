package com.caidao.controller.front;


import com.caidao.common.MyResponseEntity;
import com.caidao.config.AppUserRealmConfig;
import com.caidao.exception.MyException;
import com.caidao.param.UserParam;
import com.caidao.pojo.DeptUser;
import com.caidao.service.AppMassageService;
import com.caidao.service.DeptConfigService;
import com.caidao.service.DeptUserService;
import com.caidao.util.MapUtils;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author tom
 */

@RestController
@Slf4j
public class AppLoginController {

	public static final Logger logger =  LoggerFactory.getLogger(AppLoginController.class);

	@Autowired
	private Jedis jedis;

	@Autowired
	private DeptUserService deptUserService;

	@Autowired
	private DeptConfigService deptConfigService;

	@Autowired
	private AppMassageService appMassageService;

	/**
	 * 获取用户登录的公钥
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	@ApiOperation("获取用户登录的公钥")
	@GetMapping("/getPublicKey")
	public MyResponseEntity<Map<String,String>> getAppUserPublicKey() throws NoSuchAlgorithmException {
		log.info("获取用户登录用的公钥");
		//获取加密解密数据
		Map<Integer, String> integerStringMap = RsaUtils.genKeyPair();
		String uuid = UUID.randomUUID().toString().replaceAll("-","");
		jedis.setex(PropertyUtils.APP_USER_PUBLIC_KEY + uuid,1800,integerStringMap.get(0));
		jedis.setex(PropertyUtils.APP_USER_PRIVATE_KEY + uuid,1800,integerStringMap.get(1));
		Map<String, String> map = new HashMap<>(2);
		map.put("publicKey",integerStringMap.get(0));
		map.put("uuid",uuid);
		return MyResponseEntity.ok(map);
	}


	/**
	 * app用户登录
	 * @param userParam
	 * @return
	 */
	@ApiOperation("前台登录接口")
	@PostMapping("/appLogin")
	public MyResponseEntity<Map<String,String>> login(@RequestBody UserParam userParam) {
		Assert.notNull(userParam,"用户名不能为空");
		log.info("用户名为{}请求登录",userParam.getPrincipal());
		try {
			Subject subject = SecurityUtils.getSubject();
			UserLoginTokenUtils userLoginTokenUtils = new UserLoginTokenUtils(userParam.getPrincipal(), userParam.getCredentials(),PropertyUtils.APP_USER_REALM);
			//校验登录信息
			subject.login(userLoginTokenUtils);
			String token = subject.getSession().getId().toString();
			//将登录的公钥私钥信息存进redis里面
			String privateKey = jedis.get(PropertyUtils.APP_USER_PRIVATE_KEY + userParam.getSessionUuid());
			String cKey = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
			String encrypt = RsaUtils.encrypt(cKey, privateKey);
			//将密钥放到redis中
			jedis.set(PropertyUtils.AES_PREFIX + token,cKey);
			//将之前的公钥密钥删除
			jedis.del(PropertyUtils.APP_USER_PRIVATE_KEY + userParam.getSessionUuid(),PropertyUtils.APP_USER_PUBLIC_KEY + userParam.getSessionUuid());
			//将个人信息放在hashSet中，后来修改密码的时候抹掉个人的缓存信息以及session信息
			DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
			jedis.hset(PropertyUtils.ALL_USER_TOKEN,deptUser.getUserSalt(),token);
			return MyResponseEntity.ok(MapUtils.getMap("cKey",encrypt,"token",token));
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
	public MyResponseEntity<Map<String, Object>> getHomePage(){
		DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> map = new HashMap<>(3);
		//获取用户的权限列表
		List<String> deptUserAuthorities = deptConfigService.getPowerByUserId(deptUser.getUserId());
		map.put("userAuthorities", deptUserAuthorities);
		//获得用户首页个人信息列表
		Map<String,String> userMassage = deptUserService.getDeptUserMassage(deptUser);
		map.put("userMassage",userMassage);
		//获取用户的首页信息列表
		Integer userMassageCount = appMassageService.getAppMassageCount(deptUser.getUsername());
		map.put("appHomePage", userMassageCount);
		return MyResponseEntity.ok(map);
	}

	/**
	 * 根据用户名和手机判断是否有这个人
	 * @param username
	 * @return
	 */
	@GetMapping("/app/user/checkNameAndPhone")
	@ApiOperation("检查用户名和手机是否正确")
	public MyResponseEntity<DeptUser> beforeForgetPass(String username, String phone)  {
		Assert.notNull(username,"用户名不能为空");
		Assert.notNull(phone,"手机号不能为空");
		log.info("查询用户名为{}，手机号为{}的用户",username,phone);
		DeptUser deptUser = deptUserService.findUserByUsernameAndPhone(username,phone);
		if (deptUser == null){
			return MyResponseEntity.ok().build();
		}
		return MyResponseEntity.ok(deptUser);
	}

	/**
	 * 发送短信验证码
	 * @param phone
	 * @return
	 */
	@GetMapping("/app/user/message")
	@ApiOperation("调用三方接口想用户发送验证码")
	public MyResponseEntity<Void> getMessage(String phone){
		Assert.notNull(phone,"手机号不能为空");
		//TODO 是否要使用短信业务 使用哪个短信业务系统 ，或者是只能是后台工作人员改密码
		//生成随机6位数字
		int code = (int)((Math.random()*9+1)*100000);
		//向redis 里面存6位的随机数组 redis储存时间为60S
		jedis.setex(PropertyUtils.MASSAGE_CODE + phone,60,String.valueOf(code));
		return MyResponseEntity.ok().build();
	}

	/**
	 * 更新用户的密码
	 * @param userParam
	 * @return
	 */
	@ApiOperation("忘记密码，更新用户的密码")
	@PostMapping("/app/user/updatePass")
	public MyResponseEntity<Boolean> updateUserPassword(@RequestBody UserParam userParam){
		Assert.notNull(userParam,"参数不能为空");
		log.info("用户手机号为{}的用户更新了密码",userParam.getPhone());
		//校验验证码是否正确
		checkCode(PropertyUtils.MASSAGE_CODE+userParam.getPhone(), userParam.getImageCode());
		//更新用户密码
		return MyResponseEntity.ok(deptUserService.updatePassByPhone(userParam));
	}

	/**
	 * app用户退出登录
	 * @return
	 */
	@PostMapping("/appLogout")
	@ApiOperation("app退出账号")
	public MyResponseEntity<Void> logout(){
		//删除用户在redis 里面的token
		DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
		jedis.hdel(PropertyUtils.ALL_USER_TOKEN,deptUser.getUserSalt());
		//获取对应的登录用户session
		String token = SecurityUtils.getSubject().getSession().getId().toString();
		//删除用户在redis 里面的token
		jedis.del(PropertyUtils.USER_SESSION + token,PropertyUtils.AES_PREFIX + token);
		//清空缓存中的信息
		new AppUserRealmConfig().getAppClearAllCache(token);
		return MyResponseEntity.ok().build();
	}

	/**
	 * 校验验证码
	 */
	private void checkCode(String phone, String code) {
		if (!StringUtils.hasText(code) || !StringUtils.hasText(phone)) {
			throw new MyException("验证码校验失败");
		}
		//获取redis里面的验证码并校验
		String redisImageCode = jedis.get(PropertyUtils.VALCODE_PRIFAX + phone);
		if (!StringUtils.hasText(redisImageCode)) {
			throw new MyException("验证码超时，请重新验证");
		}
		if (!code.equals(redisImageCode)) {
			throw new MyException("验证码错误，请重新输入");
		}
	}

}