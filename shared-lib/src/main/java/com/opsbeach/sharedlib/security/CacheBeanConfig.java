package com.opsbeach.sharedlib.security;

import com.opsbeach.sharedlib.utils.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
public class CacheBeanConfig {

    /*private final ApplicationConfig applicationConfig;

    public CacheBeanConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        var redisStandaloneConfiguration = new RedisStandaloneConfiguration(applicationConfig.getRedis().get(Constants.HOST), Integer.parseInt(applicationConfig.getRedis().get(Constants.PORT)));
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Object.class));
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        return redisTemplate;
    }*/
}