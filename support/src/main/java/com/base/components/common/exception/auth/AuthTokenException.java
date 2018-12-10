package com.base.components.common.exception.auth;

/**
 * 权限异常
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-13 13:59
 */
public class AuthTokenException extends AuthenticationException {
  private static final long serialVersionUID = -4906974114374511080L;
  public AuthTokenException() {
    this(UNAUTHORIZED_MSG);
  }

  public AuthTokenException(String message) {
    super(message);
  }

  public AuthTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthTokenException(Throwable cause) {
    super(cause);
  }

  public AuthTokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
