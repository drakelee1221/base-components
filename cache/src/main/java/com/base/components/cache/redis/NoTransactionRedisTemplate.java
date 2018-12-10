package com.base.components.cache.redis;

import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 无事务 RedisTemplate
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-08 16:26
 */
public class NoTransactionRedisTemplate extends RedisTemplate<String, Object> {
  public NoTransactionRedisTemplate() {
    setKeySerializer(new StringRedisSerializer());
    super.setEnableTransactionSupport(false);
  }
  public NoTransactionRedisTemplate(RedisConnectionFactory connectionFactory) {
    this();
    setConnectionFactory(connectionFactory);
    afterPropertiesSet();
  }

  @Override
  protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
    return new DefaultStringRedisConnection(connection);
  }

  @Override
  public void setEnableTransactionSupport(boolean enableTransactionSupport) {
    throw new IllegalArgumentException("this RedisTemplate can not enable transaction support !");
  }
}
