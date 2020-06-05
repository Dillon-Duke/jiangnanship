package com.caidao.controller.back;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.caidao.entity.SysUser;
import com.caidao.exception.MyException;
import com.caidao.param.Menu;
import com.caidao.param.UserParam;
import com.caidao.service.SysMenuService;
import com.caidao.service.SysUserService;
import com.caidao.util.PropertyUtils;
import com.caidao.util.UserLoginTokenUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author tom
 */

@RestController
@Slf4j
public class LoginController {

	public static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
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
	public void validataCode(@RequestParam(required = true)String uuid, HttpServletResponse response) throws IOException {
		//生成验证码
		 CircleCaptcha createCircleCaptcha = CaptchaUtil.createCircleCaptcha(200, 50, 1, 2);
		ServletOutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			createCircleCaptcha.write(outputStream);
			redis.opsForValue().set(PropertyUtils.VALCODE_PRIFAX+uuid, createCircleCaptcha.getCode(), Duration.ofSeconds(60));
		} catch (IOException e) {
			throw new IOException(e.getMessage());
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
	@ApiOperation("后台登录接口")
	@PostMapping("/login")
	public ResponseEntity<String> backlogin(@RequestBody UserParam userParam) {
		
		Subject subject = SecurityUtils.getSubject();
		UserLoginTokenUtils userLoginTokenUtils = new UserLoginTokenUtils(userParam.getPrincipal(), userParam.getCredentials(),PropertyUtils.BACK_USER_REALM);
		String token = null;

		//校验验证码
		checkCode(userParam.getSessionUUID(),userParam.getImageCode());
		//校验登录信息
		subject.login(userLoginTokenUtils);
		token = subject.getSession().getId().toString();

		//设置token30分钟过期
		redis.opsForValue().set(PropertyUtils.USER_LOGIN_SESSION_ID+userParam.getPrincipal(), token,30, TimeUnit.MINUTES);

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

		Assert.notNull(user,"用户信息不能为空");
		log.info("{}用户登录之后查询菜单",user.getUsername());

		Map<String, Object> result = new HashMap<String,Object>(2);
		List<Menu> menuList = sysMenuService.getMenuListByUserId(user);

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
	 * 用户退出登录
	 * @return
	 */
	@PostMapping("/sys/logout")
	@ApiOperation("退出账号")
	public ResponseEntity<Void> logout(){

		//删除用户在redis 里面的token
		SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();

		//获取对应的登录用户session
		String sessionKey = redis.opsForValue().get(PropertyUtils.USER_LOGIN_SESSION_ID+sysUser.getUsername());
		redis.delete(PropertyUtils.USER_SESSION + sessionKey);
		redis.delete(PropertyUtils.USER_LOGIN_SESSION_ID + sysUser.getUsername());
		return ResponseEntity.ok().build();
	}

	/**
	 * 更新自己的密码
	 * @param userParam
	 * @return
	 */
	@PostMapping("/sys/user/password")
	@ApiOperation("更新自己的密码")
	public ResponseEntity<String> uptedaPass(@RequestBody UserParam userParam){
		SysUser sysUser = (SysUser)SecurityUtils.getSubject().getPrincipal();
		int updatePass = sysUserService.updatePass(sysUser, userParam);
		if (updatePass == 1){
			return ResponseEntity.ok("更新密码成功");
		}
		return ResponseEntity.ok("更新密码失败");
	}

	/**
	 * 校验验证码
	 */
	private void checkCode(String sessionUuid, String imageCode) {
		if (!StringUtils.hasText(imageCode) ||!StringUtils.hasText(sessionUuid) ) {
			throw new MyException("0007","验证码校验失败");
		}
		//获取redis里面的验证码并校验
		String redisImageCode = redis.opsForValue().get(PropertyUtils.VALCODE_PRIFAX+sessionUuid);
		if (!StringUtils.hasText(redisImageCode)) {
			throw new MyException("0005","验证码超时，请重新验证");
		}
		if (!imageCode.equals(redisImageCode)) {
			throw new MyException("0006","验证码错误，请重新输入");
		}
		//验证码使用之后 删除
		redis.delete(PropertyUtils.VALCODE_PRIFAX+sessionUuid);
	}


}