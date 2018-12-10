
package com.base.components.cache.redis;

import com.base.components.cache.Cache;
import com.base.components.cache.Nameable;
import com.base.components.common.dto.page.DataPage;
import com.base.components.transaction.TransactionManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RedisCacheWrapper
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-08 11:07
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class RedisCacheWrapper implements Cache {

  private static final Logger logger = LoggerFactory.getLogger(RedisCacheWrapper.class);

  private org.springframework.cache.Cache delegate;

  private ConversionService conversionService;
  /**
   * 默认失效时间，单位秒，不失效为 0
   */
  private final AtomicLong defaultExpiresSecond = new AtomicLong();

  private RedisTemplate redisTemplate;

  RedisCacheWrapper(org.springframework.cache.Cache delegate, RedisTemplate redisTemplate,
                    ConversionService conversionService, long defaultExpiresSecond) {
    this.delegate = delegate;
    this.redisTemplate = redisTemplate;
    this.conversionService = conversionService;
    this.defaultExpiresSecond.set(defaultExpiresSecond);
  }

  @Override
  public Nameable getCacheName() {
    return Nameable.parse(delegate.getName());
  }

  @Override
  @NonNull
  public String getName() {
    return delegate.getName();
  }

  @NonNull
  @Override
  public Object getNativeCache() {
    return delegate.getNativeCache();
  }

  @NonNull
  @Override
  public Cache.ValueWrapper get(@NonNull Object key) {
    ValueWrapper wrapper = delegate.get(key);
    return new SimpleValueWrapper(wrapper == null ? null : wrapper.get());
  }

  @Nullable
  @Override
  public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
    return delegate.get(key, type);
  }

  @Nullable
  @Override
  public <T> T get(@NonNull Object key, @Nullable Callable<T> valueLoader) {
    if (valueLoader != null) {
      return delegate.get(key, valueLoader);
    }else {
      return (T) get(key).get();
    }
  }

  @Override
  public void put(@NonNull Object key, @Nullable Object value) {
    delegate.put(key, value);
  }

  @NonNull
  @Override
  public ValueWrapper putIfAbsent(@NonNull Object key, @Nullable Object value) {
    ValueWrapper wrapper = delegate.putIfAbsent(key, value);
    return new SimpleValueWrapper(wrapper == null ? null : wrapper.get());
  }

  @Nullable
  @Override
  public <T> T putIfAbsentReturnValue(@NonNull Object key, @Nullable Object value) {
    ValueWrapper wrapper = delegate.putIfAbsent(key, value);
    return wrapper == null ? null : (T) wrapper.get();
  }

  @Override
  public void evict(@NonNull Object key) {
    delegate.evict(key);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public Long getExpire(Object key, TimeUnit timeUnit) {
    return redisTemplate.getExpire(getTargetKey(key), timeUnit);
  }

  @Override
  public Boolean expire(Object key, long timeout, TimeUnit timeUnit) {
    return redisTemplate.expire(getTargetKey(key), timeout, timeUnit);
  }

  @Override
  public Boolean expireAt(Object key, Date expireDate) {
    return redisTemplate.expireAt(getTargetKey(key), expireDate);
  }

  @Override
  public Set<String> keys(String pattern) {
    if(StringUtils.isNotBlank(pattern)){
      RedisSerializer keySerializer = redisTemplate.getKeySerializer();
      byte[] rawKey = keySerializer.serialize(getTargetKey(pattern));
      if(rawKey != null){
        Set<byte[]> keys = (Set<byte[]>) redisTemplate.execute(connection -> connection.keys(rawKey), true);
        if(keys != null && !keys.isEmpty()){
          return keys.stream().map(bytes -> clearCacheName(keySerializer.deserialize(bytes))).collect(Collectors.toSet());
        }
      }
    }
    return Collections.emptySet();
  }

  @Override
  public Set<String> keys() {
    return keys("*");
  }

  @Override
  public <T> List<T> mget(Collection keys) {
    return redisTemplate.opsForValue().multiGet(getTargetKeys(keys));
  }

  @Override
  public <T> DataPage<T> listsOfPage(Object key, int pageNum, int pageSize) {
    pageNum = pageNum < 1 ? 1 : pageNum;
    pageSize = pageSize < 1 ? 0 : pageSize;
    Object targetKey = getTargetKey(key);
    Long total = redisTemplate.opsForList().size(targetKey);
    if(total == null){
      total = 0L;
    }
    if (total == 0) {
      return DataPage.getEmpty();
    }
    long start = (pageNum - 1) * pageSize;
    long end = pageSize == 0 ? -1 : (start + pageSize - 1);
    DataPage<T> page = new DataPage<>();
    page.setList(redisTemplate.opsForList().range(getTargetKey(key), start, end));
    page.setTotal(total);
    page.setPageSize(pageSize);
    page.setPageNum(pageNum);
    page.setPages(pageSize == 0 ? 1 : (int) Math.ceil((double) total / (double) pageSize));
    return page;
  }

  @Override
  public <T> List<T> listsOfList(Object key, int pageNum, int pageSize) {
    pageNum = pageNum < 1 ? 1 : pageNum;
    pageSize = pageSize < 1 ? 0 : pageSize;
    long start = (pageNum - 1) * pageSize;
    long end = pageSize == 0 ? -1 : (start + pageSize - 1);
    return redisTemplate.opsForList().range(getTargetKey(key), start, end);
  }

  @Override
  public <T> T listsOfIndex(Object key, long index) {
    return (T) redisTemplate.opsForList().index(getTargetKey(key), index);
  }

  @Override
  public long listsOfSize(Object key) {
    Long re = redisTemplate.opsForList().size(getTargetKey(key));
    return re == null ? 0 : re;
  }

  @Override
  public <T> void listsOfSet(Object key, long index, boolean refreshExpireTime, T value) {
    String targetKey = getTargetKey(key);
    multi(() -> redisTemplate.opsForList().set(targetKey, index, value), targetKey, refreshExpireTime);
  }

  @Override
  public <T> void listsOfAppend(Object key, boolean refreshExpireTime, T value) {
    listsOfAdd(key, refreshExpireTime, true, value);
  }

  @Override
  public <T> void listsOfAppend(Object key, boolean refreshExpireTime, T[] values) {
    listsOfAdd(key, refreshExpireTime, true, values);
  }

  @Override
  public <T> void listsOfPrepend(Object key, boolean refreshExpireTime, T value) {
    listsOfAdd(key, refreshExpireTime, false, value);
  }

  @Override
  public <T> void listsOfPrepend(Object key, boolean refreshExpireTime, T[] values) {
    listsOfAdd(key, refreshExpireTime, false, values);
  }

  @Override
  public <T> void listsOfRemove(Object key, long count, boolean refreshExpireTime, T value) {
    String targetKey = getTargetKey(key);
    multi(() -> redisTemplate.opsForList().remove(targetKey, count, value), targetKey, refreshExpireTime);
  }

  private <T> void listsOfAdd(Object key, boolean refreshExpireTime, boolean append, T... value) {
    String targetKey = getTargetKey(key);
    multi(() -> {
      if (append) {
        redisTemplate.opsForList().rightPushAll(targetKey, value);
      } else {
        redisTemplate.opsForList().leftPushAll(targetKey, value);
      }
    }, targetKey, refreshExpireTime);
  }



  @Override
  public <T> void setsOfAdd(Object key, boolean refreshExpireTime, T value) {
    String targetKey = getTargetKey(key);
    multi(() -> redisTemplate.opsForSet().add(targetKey, value), targetKey, refreshExpireTime);
  }

  @Override
  public <T> void setsOfAdd(Object key, boolean refreshExpireTime, T[] values) {
    String targetKey = getTargetKey(key);
    multi(() -> redisTemplate.opsForSet().add(targetKey, values), targetKey, refreshExpireTime);
  }

  @Override
  public void setsOfRemove(Object key, boolean refreshExpireTime, Object value) {
    String targetKey = getTargetKey(key);
    multi(() -> redisTemplate.opsForSet().remove(targetKey, value), targetKey, refreshExpireTime);
  }

  @Override
  public void setsOfRemove(Object key, boolean refreshExpireTime, Object[] values) {
    String targetKey = getTargetKey(key);
    multi(() -> redisTemplate.opsForSet().remove(targetKey, values), targetKey, refreshExpireTime);
  }

  @Override
  public long setsOfSize(Object key) {
    Long re = redisTemplate.opsForSet().size(getTargetKey(key));
    return re == null ? 0 : re;
  }

  @Override
  public <T> boolean setsOfIsMember(Object key, T value) {
    Boolean re = redisTemplate.opsForSet().isMember(getTargetKey(key), value);
    return re != null && re;
  }

  @Override
  public <T> Set<T> setsOfMembers(Object key) {
    return redisTemplate.opsForSet().members(getTargetKey(key));
  }

  @Override
  public <T> Set<T> setsOfMembers(Object key, long count) {
    if (count < 1) {
      return Collections.emptySet();
    }
    return redisTemplate.opsForSet().distinctRandomMembers(getTargetKey(key), count);
  }

  @Override
  public <T> List<T> setsOfRandomMembers(Object key, long count) {
    return redisTemplate.opsForSet().randomMembers(getTargetKey(key), count);
  }

  @Override
  public <T> Set<T> setsOfDifference(Object key, Collection<T> otherKeys) {
    return redisTemplate.opsForSet().difference(getTargetKey(key), getTargetKeys(otherKeys));
  }

  @Override
  public <T> Set<T> setsOfIntersect(Object key, Collection<T> otherKeys) {
    return redisTemplate.opsForSet().intersect(getTargetKey(key), getTargetKeys(otherKeys));
  }

  @Override
  public <T> Set<T> setsOfUnion(Object key, Collection<T> otherKeys) {
    return redisTemplate.opsForSet().union(getTargetKey(key), getTargetKeys(otherKeys));
  }

  private String clearCacheName(Object key){
    if(key == null){
      return null;
    }
    String convertedKey = convertKey(key);
    if(key.equals(convertedKey)){
      return convertedKey.replace(Nameable.getPrefix(getName()), "");
    }
    else{
      return key.toString();
    }
  }

  private void multi(Runnable runnable, String targetKey, boolean refreshExpireTime) {
    Boolean exist = redisTemplate.hasKey(targetKey);
    if(TransactionManager.isSynchronizationActive()){
      runnable.run();
      if (defaultExpiresSecond.get() > 0 && (exist == null || !exist || refreshExpireTime)) {
        redisTemplate.expire(targetKey, defaultExpiresSecond.get(), TimeUnit.SECONDS);
      }
    }
    else{
      redisTemplate.execute(new SessionCallback() {
        @Override
        public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
          operations.multi();
          runnable.run();
          if (defaultExpiresSecond.get() > 0 && (exist == null || !exist || refreshExpireTime)) {
            operations.expire(targetKey, defaultExpiresSecond.get(), TimeUnit.SECONDS);
          }
          return operations.exec();
        }
      });
    }
  }

  private Collection getTargetKeys(Collection keys) {
    Assert.notNull(keys, "Keys must not be null");
    Stream<String> stream = keys.stream().map(this::getTargetKey);
    return stream.collect(Collectors.toSet());
  }

  RedisTemplate getRedisTemplate() {
    return redisTemplate;
  }

  @Override
  public long getDefaultExpiresSecond() {
    return defaultExpiresSecond.get();
  }

  void setDefaultExpiresSecond(long defaultExpiresSecond) {
    if (this.defaultExpiresSecond.get() != defaultExpiresSecond) {
      this.defaultExpiresSecond.set(defaultExpiresSecond);
      this.updateConfig();
    }
  }
  private void updateConfig(){
    org.springframework.cache.Cache cache = delegate;
    if(cache instanceof TransactionAwareCacheDecorator){
      TransactionAwareCacheDecorator tx = (TransactionAwareCacheDecorator) cache;
      cache = tx.getTargetCache();
    }
    if(cache instanceof RedisCache){
      RedisCache redisCache = (RedisCache)cache;
      if(!DefaultRedisCacheManager.DEFAULT_CACHE_CONFIG.equals(redisCache.getCacheConfiguration())){
        try {
          Field cacheConfigField = RedisCache.class.getDeclaredField("cacheConfig");
          cacheConfigField.setAccessible(true);
          RedisCacheConfiguration newConfig =
            redisCache.getCacheConfiguration().entryTtl(Duration.ofSeconds(this.defaultExpiresSecond.get()));
          cacheConfigField.set(redisCache, newConfig);
        } catch (Exception e) {
          logger.error("更新 RedisCacheConfig ttl 失败", e);
        }
      }
      else{
        throw new IllegalArgumentException("DefaultRedisCacheManager.DEFAULT_CACHE_CONFIG can not be change !");
      }
    }
  }

  private String getTargetKey(Object key) {
    String convertedKey = convertKey(key);
    return prefixCacheKey(convertedKey);
  }

  private String convertKey(Object key) {

    TypeDescriptor source = TypeDescriptor.forObject(key);
    if (conversionService.canConvert(source, TypeDescriptor.valueOf(String.class))) {
      return conversionService.convert(key, String.class);
    }

    Method toString = ReflectionUtils.findMethod(key.getClass(), "toString");

    if (toString != null && !Object.class.equals(toString.getDeclaringClass())) {
      return key.toString();
    }

    throw new IllegalStateException(
      String.format("Cannot convert %s to String. Register a Converter or override toString().", source));
  }

  private String prefixCacheKey(String key) {
    return Nameable.getPrefixCacheKey(getName(), key);
  }

}
