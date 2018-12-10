package com.base.components.common.token;

import java.io.Serializable;

/**
 * 管理用户会话
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-10 13:39
 */
public interface TokenManager {

  /**
   * 根据token值获取缓存对象，会刷新失效时间
   * @param token token值
   * @return
   */
  TokenCacheObj getByToken(String token);

  /**
   * 缓存token对象，一般用于登录
   * @param tokenCacheObj 缓存对象
   * @return
   */
  TokenCacheObj cacheToken(TokenCacheObj tokenCacheObj);

  /**
   * 删除token对象，一般用于退出
   * @param token token值
   * @return
   */
  TokenCacheObj removeToken(String token);

  /**
   * 根据 objId 删除所有 token
   * @param objId token对象ID
   */
  void cleanTokenWithObjId(Serializable objId);
}
