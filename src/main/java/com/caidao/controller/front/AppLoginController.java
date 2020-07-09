package com.caidao.controller.front;


import com.caidao.param.UserParam;
import com.caidao.pojo.AppBaseMsg;
import com.caidao.service.AppBaseMsgService;
import com.caidao.service.DeptUserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author tom
 */

@RestController
@Slf4j
public class AppLoginController {

	public static final Logger logger =  LoggerFactory.getLogger(AppLoginController.class);

	@Autowired
	private DeptUserService deptUserService;

	@Autowired
	private AppBaseMsgService appBaseMsgService;


	/**
	 * app lunch
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	@ApiOperation("获取用户登录的公钥 获取对应的手机基本信息")
	@PostMapping("/lunch")
	public ResponseEntity<Map<String,String>> appLunch(@RequestBody AppBaseMsg appBaseMsg) throws NoSuchAlgorithmException {
		Map<String, String> appLunch = appBaseMsgService.appLunch(appBaseMsg);
		return ResponseEntity.ok(appLunch);
	}

	/**
	 * app用户登录
	 * @param userParam
	 * @return
	 */
	@ApiOperation("前台用户进行登录")
	@PostMapping("/appLogin")
	public ResponseEntity<Map<String,String>> login(@RequestBody UserParam userParam) {
		Map<String, String> login = deptUserService.login(userParam);
		return ResponseEntity.ok(login);

	}

	/**
	 * 登录首页
	 * @return
	 */
	@ApiOperation("登录首页")
	@GetMapping("/app/getHomePage/{userId}")
	public ResponseEntity<Map<String, Object>> getHomePage(@PathVariable("userId") Integer userId){
		Map<String, Object> homePage = deptUserService.getHomePage(userId);
		return ResponseEntity.ok(homePage);
	}

	/**
	 * 根据用户名和手机判断是否有这个人
	 * @param userParam
	 * @return
	 */
	@PostMapping("/app/checkNameAndPhone")
	@ApiOperation("检查用户名和手机是否正确")
	public ResponseEntity<Boolean> checkNameAndPhone(@RequestBody UserParam userParam)  {
		boolean result = deptUserService.checkNameAndPhone(userParam);
		if (result) {
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.badRequest().build();
	}

	/**
	 * 发送短信验证码
	 * @param phone
	 * @return
	 */
	@GetMapping("/app/sendCheckCode/{phone}")
	@ApiOperation("调用三方接口想用户发送验证码")
	public ResponseEntity<Void> sendCheckCode(@PathVariable("phone") String phone){
		deptUserService.sendCheckCode(phone);
		return ResponseEntity.ok().build();
	}

	/**
	 * 更新用户的密码
	 * @param userParam
	 * @return
	 */
	@ApiOperation("忘记密码，更新用户的密码")
	@PutMapping("/app/updatePass")
	public ResponseEntity<Void> updateUserPassword(@RequestBody UserParam userParam){
		boolean result = deptUserService.updateUserPassword(userParam);
		if (result) {
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.badRequest().build();
	}

	/**
	 * app用户退出登录
	 * @return
	 */
	@GetMapping("/app/logout")
	@ApiOperation("app退出账号")
	public ResponseEntity<Void> logout(){
		deptUserService.logout();
		return ResponseEntity.ok().build();
	}

}