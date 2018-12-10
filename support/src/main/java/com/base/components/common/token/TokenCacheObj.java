
package com.base.components.common.token;

import java.io.Serializable;

/**
 * 使用tokenManager做登录验证的对象，需要此接口
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-10 15:25
 */
public interface TokenCacheObj extends Serializable{
  /**
   * 传递token对象ClassName对应的key 前缀，如 CLASS_RECEIVE_PREFIX_KEY + {XXX}_TOKEN_RECEIVE_KEY
   */
  String CLASS_RECEIVE_PREFIX_KEY = "_class_name_";

  /**
   * 返回序列化对象的id, 以便做双向关联
   * @return
   */
  Serializable objId();

  /**
   * 得到缓存时的token值
   */
  String getToken();

}
