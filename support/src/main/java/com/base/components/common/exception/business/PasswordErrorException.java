package com.base.components.common.exception.business;

/**
 * 密码错误异常
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-12 16:41
 */
public class PasswordErrorException extends BusinessException{
  private static final long serialVersionUID = 7629214326385290104L;

  public PasswordErrorException() {
  }

  public PasswordErrorException(String message) {
    super(message);
  }

  public PasswordErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  public PasswordErrorException(Throwable cause) {
    super(cause);
  }

  public PasswordErrorException(String message, Throwable cause, boolean enableSuppression,
    boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
