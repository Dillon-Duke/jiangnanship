package com.caidao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author tom
 */
@Configuration
public class JedisPoolConfigs {

    /** 自动注入redis配置属性文件 */
    @Autowired
    private RedisProperties properties;

    @Bean
    public JedisPool getJedisPool(){
        //获取最大等待连接数
        int maxIdle = properties.getJedis().getPool().getMaxIdle();
        //获取最小连接数
        int minIdle = properties.getJedis().getPool().getMinIdle();
        //获取最大存活连接数
        int maxActive = properties.getJedis().getPool().getMaxActive();
        //获取最大等待时长
        long millis = properties.getJedis().getPool().getMaxWait().toMillis();
        //获取Ip地址
        String host = properties.getHost();
        //获取端口
        int port = properties.getPort();
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setMaxTotal(maxActive);
        config.setMaxWaitMillis(millis);
        JedisPool pool = new JedisPool(config, host, port,100);
        return pool;
    }

    @Bean
    public Jedis getJedis(JedisPool jedisPool){
        return jedisPool.getResource();
    }

}
