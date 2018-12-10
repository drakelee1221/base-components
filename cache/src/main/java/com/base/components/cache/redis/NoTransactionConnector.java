
package com.base.components.cache.redis;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * NoTransactionConnector
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-05-17 15:01
 */
public class NoTransactionConnector implements RedisConnector {
  private RedisSerializer keySerializer = DefaultRedisCacheManager.REDIS_KEY_SERIALIZER;
  private RedisSerializer<Object> valueSerializer = DefaultRedisCacheManager.REDIS_VALUE_SERIALIZER;
  private RedisConnectionFactory redisConnectionFactory;

  public NoTransactionConnector(RedisConnectionFactory redisConnectionFactory) {
    this.redisConnectionFactory = redisConnectionFactory;
  }

  @Override
  @SuppressWarnings("unchecked")
  public byte[] serialKey(Object key) {
    return keySerializer.serialize(key);
  }

  @Override
  public byte[] serialVal(Object value) {
    return valueSerializer.serialize(value);
  }

  @Override
  public Object deserialKey(byte[] key) {
    return keySerializer.deserialize(key);
  }

  @Override
  public Object deserialVal(byte[] value) {
    return valueSerializer.deserialize(value);
  }

  @Override
  public <R> R execute(RedisExecutor<R> redisExecutor) {
    RedisConnection connection = redisConnectionFactory.getConnection();
    try {
      return redisExecutor.execute(connection, this);
    } finally {
      RedisConnectionUtils.releaseConnection(connection, redisConnectionFactory);
    }
  }
}
