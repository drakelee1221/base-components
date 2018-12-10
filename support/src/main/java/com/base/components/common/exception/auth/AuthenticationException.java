package com.base.components.common.exception.auth;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * AuthorizationExceptions
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-12 16:18
 */
public abstract class AuthenticationException extends RuntimeException {
  public static final String UNAUTHORIZED_MSG = "您尚未登录或登录信息已失效, 请先登录！";

  public AuthenticationException() {
  }

  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthenticationException(Throwable cause) {
    super(cause);
  }

  public AuthenticationException(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  private static final Set<String> AUTHORIZATION_EXCEPTION_CLASSES = Sets
    .newHashSet(AuthException.class.getName(), AuthTokenException.class.getName());

  /**
   * 判断异常是否是未受权异常
   * @param className
   * @return
   */
  public static boolean checkIsAuthorizationException(String className){
    if(className != null){
      return AUTHORIZATION_EXCEPTION_CLASSES.contains(className);
    }
    return false;
  }

}
