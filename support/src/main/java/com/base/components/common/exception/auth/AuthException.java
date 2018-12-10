package com.base.components.common.exception.auth;

/**
 * AuthException
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-12 17:17
 *
 */
public class AuthException extends AuthenticationException {

  public AuthException() {
    this(UNAUTHORIZED_MSG);
  }

  public AuthException(String message) {
    super(message);
  }

  public AuthException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthException(Throwable cause) {
    super(cause);
  }

  public AuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
