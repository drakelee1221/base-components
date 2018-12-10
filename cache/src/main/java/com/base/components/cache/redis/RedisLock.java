package com.base.components.cache.redis;

import com.base.components.common.util.Callable;
import com.base.components.common.util.UUIDUtils;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.base.components.cache.CacheLock;
import com.base.components.transaction.TransactionEvent;
import com.base.components.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis 锁
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-24 10:08
 */
@Component
@SuppressWarnings("all")
public class RedisLock implements CacheLock {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /** 记录当前事物已经锁定过的key值，如果事物未完成再次使用已经锁定过的key则不会被锁定 */
  private static final ThreadLocal<Set<String>> TRANSCATION_KEYS = new ThreadLocal<>();
  private static final String LOCK_PREFIX = "LOCK::";
  private RedisSerializer serializer = new StringRedisSerializer();
  @Autowired
  private RedisConnectionFactory redisConnectionFactory;

  @Override
  public void lock(Object key, Runnable lockRunnable){
    lock(key, lockRunnable, LOCK_MAX_TIMEOUT);
  }

  /**
   * 锁定一个key，使用一个非事务支持的新连接下加锁
   *
   * @param key - Nonnull  -  key
   * @param lockRunnable
   * @param lockMaxTimeout - Nullable -  锁默认失效时间，单位毫秒，大于等于1000，如：60000 即一分钟失效
   */
  @Override
  public void lock(Object key, Runnable lockRunnable, long lockMaxTimeout) {
    Assert.notNull(key, "key is null");
    Assert.isTrue(lockMaxTimeout >= 1000, "lockMaxTimeout must be larger than 1000 !");
    String lockId = UUIDUtils.generateKey();
    String targetKey = LOCK_PREFIX + key;
    boolean txActive = TransactionManager.isSynchronizationActive();
    boolean locked = false;
    try {
      //如果当前无事物，或当前线程事物中没有锁定过 targetKey
      if(!txActive || !contaisTranscationKeys(targetKey)){
        doLock(targetKey, lockId, lockMaxTimeout);
        //有事物则记录锁定值
        if(txActive){
          getTranscationKeys().add(targetKey);
        }
        locked = true;
      }
      lockRunnable.run();
    } finally {
      if(locked){
        doUnlock(key, lockId, lockMaxTimeout);
      }
    }
  }

  /**
   * 锁定一个key，使用一个非事务支持的新连接下加锁
   *
   * @param key - Nonnull  -  key
   * @param excute
   */
  @Override
  public <T> T lock(Object key, Callable<T> callable) {
    return lock(key, callable, LOCK_MAX_TIMEOUT);
  }


  /**
   * 锁定一个key，使用一个非事务支持的新连接下加锁
   *
   * @param key - Nonnull  -  key
   * @param excute
   * @param lockMaxTimeout - Nullable -  锁默认失效时间，单位毫秒，大于等于1000，如：60000 即一分钟失效
   */
  @Override
  public <T> T lock(Object key, Callable<T> callable, long lockMaxTimeout) {
    Assert.notNull(key, "key is null");
    Assert.isTrue(lockMaxTimeout >= 1000, "lockMaxTimeout must be larger than 1000 !");
    String lockId = UUIDUtils.generateKey();
    String targetKey = LOCK_PREFIX + key;

    boolean txActive = TransactionManager.isSynchronizationActive();
    boolean locked = false;
    T t;
    try {
      //如果当前无事物，或当前线程事物中没有锁定过 targetKey， 则进行锁定和解锁操作
      if(!txActive || !contaisTranscationKeys(targetKey)){
        doLock(targetKey, lockId, lockMaxTimeout);
        //有事物则记录锁定值
        if(txActive){
          getTranscationKeys().add(targetKey);
        }
        locked = true;
      }
      t = callable.call();
    } finally {
      if(locked){
        doUnlock(key, lockId, lockMaxTimeout);
      }
    }
    return t;
  }

  private void doLock(Object key, String lockId, long lockMaxTimeout){
    long timeout = System.currentTimeMillis() + lockMaxTimeout;
    RedisConnection connection = redisConnectionFactory.getConnection();
    try {
      byte[] k = serializer.serialize(key);
      byte[] v = serializer.serialize(lockId);
      while (true){
        Boolean re = connection.setNX(k, v);
        if(re != null && re){
          Boolean e = connection.pExpire(k, lockMaxTimeout);
          logger.debug(key + " > locked = " + lockId);
          return;
        }
        Uninterruptibles.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
        Assert.isTrue(System.currentTimeMillis() < timeout, "[" + key + "] lock timeout!");
      }
    } finally {
      RedisConnectionUtils.releaseConnection(connection, redisConnectionFactory);
    }
  }


  /**
   * 解锁，事务是否开起，执行方式有所区别
   * @param key
   * @param lockId
   */
  private void doUnlock(Object key, String lockId, long lockMaxTimeout) {
    Object targetKey = LOCK_PREFIX + key;
    if(TransactionManager.isSynchronizationActive()){
      TransactionManager.addTransactionEvent(new TransactionEvent() {
        @Override
        public void afterCompletion(int status) {
          unlock(targetKey, lockId, lockMaxTimeout);
        }
      });
    }
    else{
      unlock(targetKey, lockId, lockMaxTimeout);
    }
  }

  private void unlock(Object key, String lockId, long lockMaxTimeout){
    long timeout = System.currentTimeMillis() + lockMaxTimeout;
    RedisConnection connection = redisConnectionFactory.getConnection();
    byte[] k = serializer.serialize(key);
    try {
      while (true) {
        byte[] val = connection.get(k);
        if (val != null) {
          if (lockId.equals(serializer.deserialize(val))) {
            connection.del(k);
            logger.debug(key + " > unlock = " + lockId);
            return;
          }
        }
        else{
          logger.debug(key + " > unlocked");
          return;
        }
        Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
        Assert.isTrue(System.currentTimeMillis() < timeout, "[" + key + "] unlock timeout!");
      }
    } finally {
      removeTranscationKeys(key);
      RedisConnectionUtils.releaseConnection(connection, redisConnectionFactory);
    }
  }

  private Set<String> getTranscationKeys(){
    Set<String> keys = TRANSCATION_KEYS.get();
    if(keys == null){
      keys = Sets.newHashSet();
      TRANSCATION_KEYS.set(keys);
    }
    return keys;
  }

  private boolean contaisTranscationKeys(String key){
    Set<String> keys = TRANSCATION_KEYS.get();
    return keys != null && keys.contains(key);
  }

  private void removeTranscationKeys(Object key){
    Set<String> keys = TRANSCATION_KEYS.get();
    if(keys != null){
      keys.remove(key);
      if(keys.isEmpty()){
        TRANSCATION_KEYS.remove();
      }
    }
  }
}
