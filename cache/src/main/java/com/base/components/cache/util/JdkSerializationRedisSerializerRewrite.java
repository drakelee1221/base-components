package com.base.components.cache.util;

import com.base.components.cache.redis.DefaultRedisCacheManager;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

/**
 * JdkSerializationRedisSerializerRewrite
 *
 * @author <a href="morse.jiang@foxmail.com">JiangWen</a>
 * @version 1.0.0, 2018/5/30 0030 10:19
 */
public class JdkSerializationRedisSerializerRewrite extends JdkSerializationRedisSerializer {

  private static final GenericJackson2JsonRedisSerializerRewrite JACKSON_2_JSON_SERIALIZER
    = new GenericJackson2JsonRedisSerializerRewrite();

  @Override
  public Object deserialize(byte[] bytes) {
    try {
      return super.deserialize(bytes);
    } catch (Exception e) {
      try {
        return JACKSON_2_JSON_SERIALIZER.deserialize(bytes);
      } catch (Exception e1) {
        return DefaultRedisCacheManager.REDIS_KEY_SERIALIZER.deserialize(bytes);
      }
    }
  }

  @Override
  public byte[] serialize(Object object) {
    try {
      return super.serialize(object);
    } catch (Exception e) {
      return JACKSON_2_JSON_SERIALIZER.serialize(object);
    }
  }
}
