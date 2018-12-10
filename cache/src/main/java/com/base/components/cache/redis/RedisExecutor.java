
package com.base.components.cache.redis;

import org.springframework.data.redis.connection.RedisConnection;

/**
 * RedisExecutor
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-05-17 15:08
 */
public interface RedisExecutor<R> {

  R execute(RedisConnection connection, CacheSerializer serial);

}
