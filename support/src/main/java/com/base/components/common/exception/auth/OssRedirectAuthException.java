package com.base.components.common.exception.auth;

import com.base.components.common.exception.WithoutPrintException;

/**
 * AuthException
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-12 17:17
 */
public class OssRedirectAuthException extends AuthException implements WithoutPrintException {

  public OssRedirectAuthException() {
    this(UNAUTHORIZED_MSG);
  }

  public OssRedirectAuthException(String message) {
    super(message);
  }

  public OssRedirectAuthException(String message, Throwable cause) {
    super(message, cause);
  }

  public OssRedirectAuthException(Throwable cause) {
    super(cause);
  }

  public OssRedirectAuthException(String message, Throwable cause, boolean enableSuppression,
                                  boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
