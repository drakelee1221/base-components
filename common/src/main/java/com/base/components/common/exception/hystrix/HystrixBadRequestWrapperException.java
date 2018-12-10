/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.exception.hystrix;

import com.base.components.common.exception.ExceptionResponseEntity;
import com.netflix.hystrix.exception.HystrixBadRequestException;

/**
 * custom hystrix exception
 * 继承自 HystrixBadRequestException 用于处理Hystrix的4XX状态
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-12 14:36
 */
public class HystrixBadRequestWrapperException extends HystrixBadRequestException implements HystrixWrapper{
  private static final long serialVersionUID = 1L;

  private ExceptionResponseEntity exceptionResponseEntity;

  public HystrixBadRequestWrapperException(String message, ExceptionResponseEntity exceptionResponseEntity) {
    super(message);
    this.exceptionResponseEntity = exceptionResponseEntity;
  }

  public HystrixBadRequestWrapperException(String message, Throwable cause,
                                           ExceptionResponseEntity exceptionResponseEntity) {
    super(message, cause);
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
