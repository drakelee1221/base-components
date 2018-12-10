
package com.base.components.cache.configuration;

import com.base.components.cache.redis.DefaultRedisCacheManager;
import com.base.components.cache.redis.NoTransactionConnector;
import com.base.components.cache.redis.RedisConnector;
import com.google.common.collect.Maps;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * redis 配置
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-10 11:33
 */
@Configuration
@ConditionalOnProperty({"spring.redis.host", "base.common.cache.enable"})
public class CustomRedisConfiguration {

  @Bean
  public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
    RedisTemplate<String, ?> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(DefaultRedisCacheManager.REDIS_KEY_SERIALIZER);
    redisTemplate.setValueSerializer(DefaultRedisCacheManager.REDIS_VALUE_SERIALIZER);
    redisTemplate.setEnableTransactionSupport(true);
    return redisTemplate;
  }

//  @Bean
//  public NoTransactionRedisTemplate cacheLockRedisTemplate(RedisConnectionFactory redisConnectionFactory){
//    NoTransactionRedisTemplate cacheLockRedisTemplate = new NoTransactionRedisTemplate(redisConnectionFactory);
//    cacheLockRedisTemplate.setValueSerializer(DefaultRedisCacheManager.REDIS_SERIALIZER);
//    return cacheLockRedisTemplate;
//  }

  @Bean
  public RedisConnector redisConnector(RedisConnectionFactory redisConnectionFactory){
    return new NoTransactionConnector(redisConnectionFactory);
  }


  @ConfigurationProperties("base.common.cache")
  @Component("cacheNameExpires")
  @RefreshScope
  public static class CacheNameExpires{
    private Map<String, Long> expires = Maps.newHashMap();

    public Map<String, Long> getExpires() {
      return expires;
    }

    public long getExpire(String cacheName){
      Long val = expires.get(cacheName);
      return val == null || val <= 0 ? 0 : val;
    }
  }
}
