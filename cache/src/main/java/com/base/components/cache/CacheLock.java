package com.base.components.cache;

import com.base.components.common.util.Callable;

/**
 * 分布式缓存锁
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-24 10:38
 */
public interface CacheLock {
  /**
   * 默认上锁失效时间，单位毫秒
   */
  long LOCK_MAX_TIMEOUT = 60 * 1000;

  /**
   * 加锁，锁默认失效时间为{@link #LOCK_MAX_TIMEOUT}，如果当前线程开启事物则解锁会在事物完成后执行
   *
   * @param key     - Nonnull  -  key
   * @param lockRunnable 被加锁后的执行代码块，代码块执行成功后会立即尝试解锁
   *
   */
  void lock(Object key, Runnable lockRunnable);

  /**
   * 加锁，如果当前线程开启事物则解锁会在事物完成后执行
   *
   * @param key             - Nonnull  -  key
   * @param lockRunnable 被加锁后的执行代码块，代码块执行成功后会立即尝试解锁
   * @param lockMaxTimeout  - Nullable -  锁默认失效时间，单位毫秒，大于等于1000，如：60000 即一分钟失效
   *
   */
  void lock(Object key, Runnable lockRunnable, long lockMaxTimeout);


  /**
   * callable加锁，如果当前线程开启事物则解锁会在事物完成后执行
   *
   * @param key             - Nonnull  -  key
   * @param callable 被加锁后的执行代码块，代码块执行成功后会立即尝试解锁
   *
   */
  <T> T lock(Object key, Callable<T> callable);

  /**
   * callable加锁，如果当前线程开启事物则解锁会在事物完成后执行
   *
   * @param key             - Nonnull  -  key
   * @param callable 被加锁后的执行代码块，代码块执行成功后会立即尝试解锁
   * @param lockMaxTimeout  - Nullable -  锁默认失效时间，单位毫秒，大于等于1000，如：60000 即一分钟失效
   *
   */
  <T> T lock(Object key, Callable<T> callable, long lockMaxTimeout);

}
