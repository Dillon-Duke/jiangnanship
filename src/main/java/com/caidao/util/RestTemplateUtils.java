package com.caidao.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 远程调用工具类
 *
 * @author tom
 * @createDate 2020-05-21
 *
 */
public class RestTemplateUtils {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateUtils.class);

    private static class SingletonRestTemplate {
        static final RestTemplate INSTANCE = new RestTemplate();
    }

    private RestTemplateUtils() {
    }

    public static RestTemplate getInstance() {
        return SingletonRestTemplate.INSTANCE;
    }

    /**
     * post 请求
     * @param url 请求路径
     * @param data body数据
     * @return
     */
    public static String post(String url, String data) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Content-Encoding", "UTF-8");
        headers.add("Content-Type", "application/json; charset=UTF-8");
        HttpEntity<String> requestEntity = new HttpEntity<>(data, headers);
        return RestTemplateUtils.getInstance().postForObject(url, requestEntity, String.class);
    }

    /**s
     * get 请求
     * @param url 请求路径
     * @return
     */
    public static  String get(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Content-Encoding", "UTF-8");
        headers.add("Content-Type", "application/json; charset=UTF-8");
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = RestTemplateUtils.getInstance().exchange(url, HttpMethod.GET, requestEntity, String.class);
        String responseBody = response.getBody();
        return responseBody;
    }

}
