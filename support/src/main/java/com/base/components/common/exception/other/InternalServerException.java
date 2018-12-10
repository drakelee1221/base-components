package com.base.components.common.exception.other;

/**
 * 服务器异常
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-25 14:51
 */
public class InternalServerException extends RuntimeException{

  private static final long serialVersionUID = -7560949071398653900L;

  public InternalServerException() {
  }

  public InternalServerException(String message) {
    super(message);
  }

  public InternalServerException(String message, Throwable cause) {
    super(message, cause);
  }

  public InternalServerException(Throwable cause) {
    super(cause);
  }

  public InternalServerException(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
