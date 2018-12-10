package com.base.components.common.exception.other;

/**
 * 资源未授权，403 异常
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-25 14:07
 */
public class ForbiddenException extends RuntimeException{
  private static final long serialVersionUID = 4392302502728471336L;
  public static final String FORBIDDEN_MSG = "您没有权限访问该资源！";

  public ForbiddenException() {
    this(FORBIDDEN_MSG);
  }

  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }

  public ForbiddenException(Throwable cause) {
    super(cause);
  }

  public ForbiddenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
