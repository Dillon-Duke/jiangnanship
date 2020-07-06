package com.caidao.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caidao.exception.MyException;
import com.caidao.filter.wrapper.SaveAppBaseMsgRequestWrapper;
import com.caidao.util.Md5Utils;
import com.caidao.util.PropertyUtils;
import com.caidao.util.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;
import redis.clients.jedis.Jedis;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 前端联调的时候将这两个注解放开就行
 * @author tom
 * @Configuration
 * @WebFilter(filterName = "SaveAppBaseMsgFilter",urlPatterns  = {"/*"})
 */
@Slf4j
public class SaveAppBaseMsgFilter implements Filter {

    private static final String LOGIN_USER_ID_VOUCHER = "0";

    /** 需要过滤的地址 */
    private static List<String> urlList = Arrays.asList("/app");

    /** 需要注入的实体类 */
    private static Jedis jedis;

    @Override
    public void init(FilterConfig config) {
        ServletContext servletContext = config.getServletContext();
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        this.setJedis(ctx.getBean(Jedis.class));
    }
    private synchronized void setJedis(Jedis jedis) {
        SaveAppBaseMsgFilter.jedis = jedis;
    }

    /** 是否需要过滤 */
    private boolean isPast(String requestUrl) {
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
            SaveAppBaseMsgRequestWrapper requestWrapper = new SaveAppBaseMsgRequestWrapper(servletRequest);
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
            //获取传入用户Id
            String userId = jsonObject.getString("userId");
            //获取加签内容
            String encryption = jsonObject.getString("encryption");
            //获取date内容
            String date = jsonObject.getString("date");
            String json ;
            if (LOGIN_USER_ID_VOUCHER.equals(userId)){
                String decrypt;
                //获得用户登录的uuid
                String uuid = jsonObject.getString("uuid");
                //替换解密内容
                String primaryKey = jedis.get(PropertyUtils.APP_USER_PRIVATE_KEY + uuid);
                try {
                    decrypt = RsaUtils.decryptByPrivateKey(primaryKey, date);
                } catch (Exception e) {
                    throw new MyException("登录解密错误，请联系管理员");
                }
                // 把参数转换之后放到我们的body里面
                JSONObject parseObject = JSONObject.parseObject(decrypt);
                json = parseObject.toJSONString();
            } else {
                if (encryption == null || encryption.isEmpty()){
                    throw new MyException("加签内容为空");
                }
                //获得用户token
                String token = SecurityUtils.getSubject().getSession().getId().toString();
                if (!isJson(date)) {
                    String salt = jedis.get(PropertyUtils.MD5_PREFIX + token);
                    String encrypt = Md5Utils.getMd5EncryptWithSalt(date, salt);
                    if (!encryption.equals(encrypt)) {
                        throw new MyException("加签验证失败");
                    }
                }
                //将字符串传到后端
                JSONObject parseObject = JSONObject.parseObject(date);
                json = parseObject.toJSONString();
            }
            //设置json参数格式
            requestWrapper.setBody(json.getBytes("UTF-8"));
            // 放行
            chain.doFilter(requestWrapper, resp);
        } else {
            chain.doFilter(req, resp);
        }
    }

    /**
     * 判断是否为json字符串
     * @param content
     * @return
     */
    private static boolean isJson(String content) {
        try {
            JSON.parse(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
