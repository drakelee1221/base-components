
package com.base.components.cache;


import org.springframework.lang.NonNull;

import java.util.Collection;

/**
 * 统一缓存管理接口，当前接口已支持事务，请用{@link org.springframework.transaction.annotation.Transactional} 声明
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-06 10:08
 *
 */
public interface CacheManager extends org.springframework.cache.CacheManager {

  /**
   * 得到默认缓存对象(缓存时间为永久)
   * @return 默认缓存接口
   */
  Cache getDefaultCache();

  /**
   * 获取缓存接口
   * @param cacheName 缓存名
   * @return 缓存接口
   */
  @NonNull
  Cache getCache(@NonNull Nameable cacheName);

  /**
   * 获取缓存接口
   * @param name 缓存名
   * @return 缓存接口
   */
  @NonNull
  @Override
  Cache getCache(@NonNull String name);

  /**
   * 获取缓存名集合
   * @return 缓存名集合
   */
  @NonNull
  @Override
  Collection<String> getCacheNames();

}
