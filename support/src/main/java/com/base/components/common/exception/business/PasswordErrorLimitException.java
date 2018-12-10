package com.base.components.common.exception.business;


/**
 * JiangWen
 * 密码错误超过限制异常
 */
public class PasswordErrorLimitException extends BusinessException{
  private static final long serialVersionUID = 7629214326385290104L;

  public PasswordErrorLimitException(String message, Integer errorCode) {
    super(message, errorCode);
  }
}
