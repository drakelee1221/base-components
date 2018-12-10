/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.zuul.exception;

/**
 * ResponseWriteException
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-12 11:23
 */
public class ResponseWriteException extends RuntimeException{
  private static final long serialVersionUID = 8743960047452305289L;

  public ResponseWriteException() {
  }

  public ResponseWriteException(String message) {
    super(message);
  }

  public ResponseWriteException(String message, Throwable cause) {
    super(message, cause);
  }

  public ResponseWriteException(Throwable cause) {
    super(cause);
  }

  public ResponseWriteException(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
