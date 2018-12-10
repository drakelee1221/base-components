/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.exception.hystrix;

import com.base.components.common.exception.ExceptionResponseEntity;

/**
 * custom hystrix exception
 * 用于处理Hystrix的5XX状态
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-12 14:36
 */
public class HystrixErrorWrapperException extends RuntimeException implements HystrixWrapper{
  private static final long serialVersionUID = 1L;

  private ExceptionResponseEntity exceptionResponseEntity;

  public HystrixErrorWrapperException(ExceptionResponseEntity exceptionResponseEntity) {
    this.exceptionResponseEntity = exceptionResponseEntity;
  }

  public HystrixErrorWrapperException(String message, ExceptionResponseEntity exceptionResponseEntity) {
    super(message);
    this.exceptionResponseEntity = exceptionResponseEntity;
  }

  public HystrixErrorWrapperException(String message, Throwable cause,
                                      ExceptionResponseEntity exceptionResponseEntity) {
    super(message, cause);
    this.exceptionResponseEntity = exceptionResponseEntity;
  }

  public HystrixErrorWrapperException(Throwable cause, ExceptionResponseEntity exceptionResponseEntity) {
    super(cause);
    this.exceptionResponseEntity = exceptionResponseEntity;
  }

  public HystrixErrorWrapperException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace, ExceptionResponseEntity exceptionResponseEntity) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.exceptionResponseEntity = exceptionResponseEntity;
  }

  @Override
  public ExceptionResponseEntity getExceptionResponseEntity() {
    return exceptionResponseEntity;
  }

  public void setExceptionResponseEntity(ExceptionResponseEntity exceptionResponseEntity) {
    this.exceptionResponseEntity = exceptionResponseEntity;
  }
}
