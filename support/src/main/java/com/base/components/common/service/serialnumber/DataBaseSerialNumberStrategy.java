package com.base.components.common.service.serialnumber;

/**
 * DataBaseSerialNumberStrategy
 * 数据库接口
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-06-13 17:09
 */
public interface DataBaseSerialNumberStrategy<T> {

  T selectCurrentMaxSerialNumber(String cacheKey);

  T increment(String cacheKey, T currentNumber);

}
