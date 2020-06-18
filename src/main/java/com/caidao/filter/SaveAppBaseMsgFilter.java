package com.caidao.filter;

import com.alibaba.fastjson.JSONObject;
import com.caidao.exception.MyException;
import com.caidao.filter.wrapper.SaveAppBaseMsgJsonRequestWrapper;
import com.caidao.pojo.AppBaseMsg;
import com.caidao.service.AppBaseMsgService;
import com.caidao.util.PropertyUtils;
import com.caidao.util.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author tom
 */
@Slf4j
@WebFilter
public class SaveAppBaseMsgFilter implements Filter {

    /** 需要过滤的地址 */
    private static List<String> urlList = Arrays.asList("/app");

    /** 需要注入的实体类 */
    private static StringRedisTemplate stringRedisTemplate;
    private static AppBaseMsgService appBaseMsgService;

    @Override
    public void init(FilterConfig config) {
        ServletContext servletContext = config.getServletContext();
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        this.setStringRedisTemplate(ctx.getBean(StringRedisTemplate.class));
        this.setAppBaseMsgService(ctx.getBean(AppBaseMsgService.class));
    }

    private synchronized void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        SaveAppBaseMsgFilter.stringRedisTemplate = stringRedisTemplate;
    }

    private synchronized void setAppBaseMsgService(AppBaseMsgService appBaseMsgService) {
        SaveAppBaseMsgFilter.appBaseMsgService = appBaseMsgService;
    }

    /** 是否需要过滤 */
    public boolean isPast(String requestUrl) {
        for (String url : urlList) {
            String substring = requestUrl.substring(0, 4);
            if (substring.contains(url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) req;

        // 获取请求地址
        String url = (servletRequest).getRequestURI().substring((servletRequest).getContextPath().length());
        if (isPast(url)) {
            //处理json报文请求
            SaveAppBaseMsgJsonRequestWrapper requestWrapper = new SaveAppBaseMsgJsonRequestWrapper(servletRequest);
            // 读取请求内容
            BufferedReader br;
            br = requestWrapper.getReader();
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            // 将json字符串转换为json对象
            JSONObject jsonObject = JSONObject.parseObject(sb.toString());

            //将字段设置到基础表字段中
            //TODO 还没有写存储基础表数据的方法
            AppBaseMsg appBaseMsg = new AppBaseMsg();
            appBaseMsg.setInterfaceVersion(jsonObject.getString("interfaceVersion"));
            appBaseMsg.setAppVersion(jsonObject.getString("appVersion"));
            appBaseMsg.setClientBrand(jsonObject.getString("clientBrand"));
            appBaseMsg.setClientModel(jsonObject.getString("clientModel"));
            appBaseMsg.setClientOs(jsonObject.getString("clientOs"));
            appBaseMsg.setClientOsVersion(jsonObject.getString("clientOsVersion"));
            appBaseMsg.setClientScreenSize(jsonObject.getDouble("clientScreenSize"));
            appBaseMsg.setData(jsonObject.getString("data"));
            String userId = jsonObject.getString("userId");
            appBaseMsg.setUserId(userId);
            appBaseMsg.setSubmitTime(jsonObject.getLong("submitTime"));

            //解密一下加密内容
            String encryption = jsonObject.getString("encryption");
            String decrypt ;
            String json ;
            if ("".equals(userId) ||userId == null){

                //获得用户登录的uuid
                String uuid = jsonObject.getString("uuid");

                //替换解密内容
                String primaryKey = stringRedisTemplate.opsForValue().get(PropertyUtils.APP_USER_PRIVATE_KEY + uuid);
                try {
                    decrypt = RsaUtils.decrypt(encryption, primaryKey);
                } catch (Exception e) {
                    throw new MyException("登录解密错误，请联系管理员");
                }
                appBaseMsg.setEncryption(decrypt);
                stringRedisTemplate.delete(PropertyUtils.APP_USER_PRIVATE_KEY + uuid);
                stringRedisTemplate.delete(PropertyUtils.APP_USER_PUBLIC_KEY + uuid);
                // 把参数转换之后放到我们的body里面
                JSONObject parseObject = JSONObject.parseObject(decrypt);
                json = parseObject.toJSONString();
            } else {

                if (encryption == null || encryption.isEmpty()){
                    throw new MyException("数据中加密数据没有传值");
                }

                //获得用户token
                String token = SecurityUtils.getSubject().getSession().getId().toString();
                //替换解密内容
                try {
                    decrypt = RsaUtils.decrypt(appBaseMsg.getEncryption(), stringRedisTemplate.opsForValue().get(PropertyUtils.APP_USER_PRIVATE_KEY + token));
                } catch (Exception e) {
                    throw new MyException("信息解密错误，请联系管理员");
                }
                appBaseMsg.setEncryption(decrypt);
                //将未解密的内容放在里面，如果需要解密，则使用注解解密
                JSONObject parseObject = JSONObject.parseObject(encryption);
                json = parseObject.toJSONString();
            }

            //将信息保存再基本表中
            appBaseMsgService.save(appBaseMsg);

            //设置json参数格式
            requestWrapper.setBody(json.getBytes("UTF-8"));
            // 放行
            chain.doFilter(requestWrapper, resp);
        } else {
            chain.doFilter(req, resp);
        }
    }

}
