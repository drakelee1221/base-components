
package com.base.components.cache.redis;

import com.base.components.cache.Cache;
import com.base.components.cache.CacheManager;
import com.base.components.cache.Nameable;
import com.base.components.cache.configuration.CustomRedisConfiguration;
import com.base.components.cache.util.JdkSerializationRedisSerializerRewrite;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * DefaultRedisCacheManager
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-08 11:53
 */
@Component("cacheManager")
@RefreshScope
@ConditionalOnProperty({"spring.redis.host", "base.common.cache.enable"})
public class DefaultRedisCacheManager implements CacheManager, InitializingBean {

  public static final RedisSerializer<String> REDIS_KEY_SERIALIZER = new StringRedisSerializer();
  public static final RedisSerializer<Object> REDIS_VALUE_SERIALIZER = new JdkSerializationRedisSerializerRewrite();

  @Autowired
  private CustomRedisConfiguration.CacheNameExpires cacheNameExpires;
  @Autowired
  private RedisConnectionFactory redisConnectionFactory;
  @Autowired
  private RedisTemplate redisTemplate;

  private RedisCacheManager redisCacheManager;

  private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

  private final ConcurrentMap<String, RedisCacheConfiguration> configMap = new ConcurrentHashMap<>(16);

  static final RedisCacheConfiguration DEFAULT_CACHE_CONFIG = RedisCacheConfiguration.defaultCacheConfig()
                                                                                     .serializeValuesWith(
                                                                                       RedisSerializationContext.SerializationPair
                                                                                         .fromSerializer(
                                                                                           REDIS_VALUE_SERIALIZER));

  @Override
  public void afterPropertiesSet() {
    buildRedisCacheManager();
  }

  @NonNull
  @Override
  public Collection<String> getCacheNames() {
    return redisCacheManager.getCacheNames();
  }

  @Override
  public Cache getDefaultCache() {
    return getCache(Nameable.DEFAULT_CACHE_NAME);
  }

  @NonNull
  @Override
  public Cache getCache(@NonNull String cacheNameStr) {
    Nameable cacheName = Nameable.parse(cacheNameStr);
    Assert.notNull(cacheName, "["+cacheNameStr+"] can not parse cacheName !");
    Cache cache = this.cacheMap.get(cacheNameStr);
    if (cache != null) {
      RedisCacheWrapper wrapper = (RedisCacheWrapper) cache;
      wrapper.setDefaultExpiresSecond(cacheNameExpires.getExpire(cacheNameStr));
      return cache;
    } else {
      // 避免重复创建
      synchronized (this.cacheMap) {
        cache = this.cacheMap.get(cacheNameStr);
        if (cache == null) {
          org.springframework.cache.Cache springCache = redisCacheManager.getCache(cacheNameStr);
          if (springCache != null) {
            RedisCacheConfiguration config = getConfig(cacheNameStr);
            RedisCacheWrapper wrapper = new RedisCacheWrapper(
              springCache, redisTemplate, config.getConversionService(), cacheNameExpires.getExpire(cacheNameStr));
            this.cacheMap.put(cacheNameStr, wrapper);
            cache = wrapper;
          }
        }
        Assert.notNull(cache, "can not create spring RedisCache !");
        return cache;
      }
    }
  }

  @NonNull
  @Override
  public Cache getCache(@NonNull Nameable cacheName) {
    Assert.notNull(cacheName, "cacheName can not null !");
    return getCache(cacheName.name());
  }

  @SuppressWarnings("unchecked")
  private void buildRedisCacheManager() {
    for (Nameable name : Nameable.getAll()) {
      long expire = cacheNameExpires.getExpire(name.name());
      RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
      config = config.prefixKeysWith(name.getPrefix());
      config = config.entryTtl(Duration.ofSeconds(expire));
      config = config.serializeKeysWith(
        RedisSerializationContext.SerializationPair.fromSerializer(REDIS_KEY_SERIALIZER));
      config = config.serializeValuesWith(
        RedisSerializationContext.SerializationPair.fromSerializer(REDIS_VALUE_SERIALIZER));
      configMap.put(name.name(), config);
    }
    RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory);
    builder.cacheDefaults(DEFAULT_CACHE_CONFIG);
    builder.initialCacheNames(Nameable.getAllNames());
    builder.withInitialCacheConfigurations(configMap);
    builder.transactionAware();
    redisCacheManager = builder.build();
    redisCacheManager.afterPropertiesSet();
  }


  private RedisCacheConfiguration getConfig(String cacheName) {
    RedisCacheConfiguration cacheConfiguration = configMap.get(cacheName);
    if (cacheConfiguration == null) {
      cacheConfiguration = DEFAULT_CACHE_CONFIG;
    }
    return cacheConfiguration;
  }
}
