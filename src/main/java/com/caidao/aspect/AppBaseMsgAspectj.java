package com.caidao.aspect;

import com.caidao.entity.AppBaseMsg;
import com.caidao.exception.MyException;
import com.caidao.service.AppBaseMsgService;
import com.caidao.util.RsaUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author tom
 * @since 2020-06-08
 */
@Aspect
@Component
public class AppBaseMsgAspectj {

    @Autowired
    private AppBaseMsgService appBaseMsgService;

    @Before("@annotation(com.caidao.anno.AppBaseMsgs)")
    @Transactional(rollbackFor = RuntimeException.class)
    public void appBaseMassageConnBeforeAspect(JoinPoint joinPoint) {

        //实例化基本对象
        AppBaseMsg appBaseMsg = new AppBaseMsg();

        //通过反射获取方法里面的值 将值存在对象里面
        try {
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                AppBaseMsg baseMsg = (AppBaseMsg) arg;
                appBaseMsg.setAppVersion(baseMsg.getAppVersion());
                appBaseMsg.setClientBrand(baseMsg.getClientBrand());
                appBaseMsg.setClientModel(baseMsg.getClientModel());
                appBaseMsg.setClientOs(baseMsg.getClientOs());
                appBaseMsg.setClientOsVersion(baseMsg.getClientOsVersion());
                appBaseMsg.setClientScreenSize(baseMsg.getClientScreenSize());
                appBaseMsg.setUserId(baseMsg.getUserId());
                appBaseMsg.setSubmitTime(baseMsg.getSubmitTime());
                appBaseMsg.setData(baseMsg.getData());
                if (baseMsg.getUserId() != null && baseMsg.getUserId() != ""){
                    //获取解密之后的数据
                    String decrypt = RsaUtils.decrypt(baseMsg.getEncryption(), RsaUtils.genKeyPair().get(1));
                    appBaseMsg.setEncryption(decrypt);
                }
                continue;
            }
            appBaseMsgService.save(appBaseMsg);
        } catch (Exception e) {
            throw  new MyException("基本参数为空");
        }
    }
}
