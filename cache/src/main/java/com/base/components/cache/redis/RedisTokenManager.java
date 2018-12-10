
package com.base.components.cache.redis;

import com.base.components.cache.Nameable;
import com.base.components.common.token.TokenCacheObj;
import com.base.components.common.token.TokenManager;
import com.base.components.common.util.Callable;
import com.base.components.transaction.TransactionManager;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 管理用户会话
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-10 13:49
 */
@Component
@RefreshScope
@ConditionalOnProperty({"spring.redis.host", "base.common.cache.enable"})
@ConfigurationProperties("base.common.cache")
public class RedisTokenManager implements TokenManager {

  private static final String TOKEN_PREFIX = "TOKEN" + Nameable.PREFIX_DELIMITER;

  private static final String CACHE_OBJ_ID_PREFIX = "TOKEN_REF" + Nameable.PREFIX_DELIMITER;

  private static final String TOKEN_AND_OBJ_ID_SPLIT = "|";

  private static final String WILDCARD = "*";

  @Autowired
  private RedisTemplate redisTemplate;
  @Autowired
  private RedisConnector redisConnector;

  private Map<String, Long> tokenExpireMap = Maps.newHashMap();

  /**
   * token 默认失效时间  - 默认 1800 秒
   */
  @Value("${base.common.cache.tokenExpireDefault:1800}")
  private long tokenExpireSecondsDefault;

  /**
   * 非唯一性登录，是否支持多处登录
   */
  @Value("${base.common.cache.tokenMultipleLogin:true}")
  private boolean multipleLogin;

  @Override
  @SuppressWarnings("unchecked")
  public TokenCacheObj getByToken(String token) {
    if (StringUtils.isBlank(token)) {
      return null;
    }
    BoundValueOperations<String, TokenCacheObj> ops = redisTemplate.boundValueOps(buildToken(token));
    TokenCacheObj obj = ops.get();
    if (obj != null) {
      call(new Callable<TokenCacheObj>() {
        @Override
        public TokenCacheObj call() {
          long expire = getExpire(obj);
          //刷新token时间
          ops.expire(expire, TimeUnit.SECONDS);
          //刷新关联关系时间
          redisTemplate.expire(buildCacheObjId(obj.objId() + TOKEN_AND_OBJ_ID_SPLIT + token), expire, TimeUnit.SECONDS);
          return obj;
        }
      });
    }
    return obj;
  }


  @Override
  @SuppressWarnings("unchecked")
  public TokenCacheObj cacheToken(TokenCacheObj cacheObj) {
    Assert.notNull(cacheObj, "cacheObj can not be null !");
    Assert.hasText(cacheObj.getToken(), "token can not be null !");
    Serializable cacheObjId = cacheObj.objId();
    Assert.notNull(cacheObjId, "cacheObjId can not be null !");
    ValueOperations ops = redisTemplate.opsForValue();
    call(new Callable<Object>() {
      @Override
      public Object call() {
        long expire = getExpire(cacheObj);
        ops.set(buildToken(cacheObj.getToken()), cacheObj, expire, TimeUnit.SECONDS);
        //登录唯一性
        if (!multipleLogin) {
          cleanToken(keys(cacheObjId));
        }
        //保存关联关系： userId | token
        ops.set(buildCacheObjId(cacheObjId + TOKEN_AND_OBJ_ID_SPLIT + cacheObj.getToken()), "", expire, TimeUnit.SECONDS);
        return null;
      }
    });
    return cacheObj;
  }

  @Override
  @SuppressWarnings("unchecked")
  public TokenCacheObj removeToken(String token) {
    if (StringUtils.isBlank(token)) {
      return null;
    }
    ValueOperations<String, TokenCacheObj> ops = redisTemplate.opsForValue();
    String buildToken = buildToken(token);
    TokenCacheObj obj = ops.get(buildToken);
    if (obj != null) {
      call(new Callable<TokenCacheObj>() {
        @Override
        public TokenCacheObj call() {
          redisTemplate.delete(buildToken);
          redisTemplate.delete(buildCacheObjId(obj.objId() + TOKEN_AND_OBJ_ID_SPLIT + token));
          return obj;
        }
      });
    }
    return obj;
  }


  @Override
  public void cleanTokenWithObjId(Serializable objId){
    Set<Object> keys = keys(objId);
    call(() -> {
      cleanToken(keys);
      return null;
    });
  }

  /** 根据 objId 删除所有 token */
  @SuppressWarnings("unchecked")
  private void cleanToken(Set<Object> keys){
    if (keys != null && !keys.isEmpty()) {
      for (Object key : keys) {
        if(key != null){
          redisTemplate.delete(key);
          String[] arr = StringUtils.split(key.toString(), TOKEN_AND_OBJ_ID_SPLIT);
          if(arr.length > 1){
            redisTemplate.delete(buildToken(arr[1]));
          }
        }
      }
    }
  }

  private Set<Object> keys(Serializable objId){
    return redisConnector.execute(new RedisExecutor<Set<Object>>() {
      @Override
      public Set<Object> execute(RedisConnection connection, CacheSerializer serial) {
        Set<byte[]> keys = connection.keys(serial.serialKey(buildCacheObjId(objId + WILDCARD)));
        return keys == null
               ? Collections.emptySet()
               : keys.stream().map(serial::deserialKey).collect(Collectors.toSet());
      }
    });
  }

  private String buildToken(String token) {
    return TOKEN_PREFIX + token;
  }

  private String buildCacheObjId(Serializable cacheObjId) {
    return CACHE_OBJ_ID_PREFIX + cacheObjId;
  }

  RedisTemplate getRedisTemplate() {
    return redisTemplate;
  }


  @SuppressWarnings("unchecked")
  private <T> T call(Callable<T> callable) {
    if (TransactionManager.isSynchronizationActive()) {
      return callable.call();
    } else {
      return (T) redisTemplate.execute(new SessionCallback() {
        @Nullable
        @Override
        public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
          operations.multi();
          Object o = callable.call();
          operations.exec();
          return o;
        }
      });
    }
  }

  public Map<String, Long> getTokenExpireMap() {
    return tokenExpireMap;
  }

  private long getExpire(TokenCacheObj tokenCacheObj) {
    Long e = getTokenExpireMap().get(tokenCacheObj.getClass().getSimpleName());
    if (e != null) {
      return e;
    }
    return tokenExpireSecondsDefault;
  }
}
