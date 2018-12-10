
package com.base.components.cache.redis;

/**
 * CacheSerializer
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-05-17 15:38
 */
public interface CacheSerializer {

  byte[] serialKey(Object key);

  byte[] serialVal(Object value);

  Object deserialKey(byte[] key);

  Object deserialVal(byte[] value);
}
