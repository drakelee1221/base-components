package com.base.components.common.service.serialnumber;

/**
 * SerialNumberBuilderService
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-06-13 17:09
 */
public interface SerialNumberBuilderService<T, DataBaseStrategy extends DataBaseSerialNumberStrategy<T>> {

  /**
   * 自增长
   * @param cacheKey  -  Nonnull - 缓存key
   * @return 增长后的值
   */
  T increment(String cacheKey);


  /**
   * 获取当前值
   * @param cacheKey  -  Nonnull - 缓存key
   * @return 当前值
   */
  T current(String cacheKey);
}
