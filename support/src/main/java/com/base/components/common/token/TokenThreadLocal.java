/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.token;

import com.base.components.common.exception.auth.AuthTokenException;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 接收到网关传输过来的当前请求线程token对象管理工具
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-12 14:14
 */
public class TokenThreadLocal {

  /**
   * 请求线程保存的登录对象
   */
  private static final ThreadLocal<Map<TokenTypes, TokenCacheObj>> LOCAL_TOKENS =
    ThreadLocal.withInitial(Maps::newHashMap);

  public static void cleanThread() {
    LOCAL_TOKENS.get().clear();
  }

  public static void putTokenCacheObj(TokenTypes type, TokenCacheObj obj) {
    if (obj != null && type != null) {
      LOCAL_TOKENS.get().put(type, obj);
    }
  }

  /**
   * 获取保存在请求线程中的token缓存对象
   *
   * @param typeClass token缓存对象类型
   *
   * @return token缓存对象
   */
  @SuppressWarnings("unchecked")
  public static <T extends TokenCacheObj> T getTokenObj(Class<T> typeClass) {
    if (typeClass != null) {
      TokenTypes type = TokenTypes.getType(typeClass);
      if(type != null){
        return (T) LOCAL_TOKENS.get().get(type);
      }
    }
    return null;
  }

  /**
   * 获取保存在请求线程中的 token 缓存对象,
   * 当缓存对象为空时，向上抛权限异常 {@link AuthTokenException}
   *
   * @param typeClass token缓存对象类型
   *
   * @return token 缓存对象
   */
  @SuppressWarnings("unchecked")
  public static <T extends TokenCacheObj> T getTokenObjNonNull(Class<T> typeClass) {
    TokenCacheObj token = getTokenObj(typeClass);
    if (token == null) {
      throw new AuthTokenException();
    }
    return (T) token;
  }

  /**
   * 当不确定当前登录对象时，
   * 获取保存在请求线程中的token缓存对象
   * @return token缓存对象
   */
  public static TokenCacheObj getTokenObj() {
    Map<TokenTypes, TokenCacheObj> map = LOCAL_TOKENS.get();
    if (map == null || map.isEmpty()) {
      return null;
    }
    return map.values().iterator().next();
  }

  /**
   * 当不确定当前登录对象时，
   * 获取保存在请求线程中的 token 缓存对象,
   * 当缓存对象为空时，向上抛权限异常 {@link AuthTokenException}
   *
   * @return token 缓存对象
   */
  public static TokenCacheObj getTokenObjNonNull() {
    TokenCacheObj token = getTokenObj();
    if(token == null){
      throw new AuthTokenException();
    }
    return token;
  }

  /**
   * 获取保存在请求线程中的所有token缓存对象
   * @return Map
   */
  public static Map<TokenTypes, TokenCacheObj> getAllTokenCacheObj() {
    return Maps.newHashMap(LOCAL_TOKENS.get());
  }

}
