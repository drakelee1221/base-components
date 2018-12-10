/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.exception;

import com.base.components.common.exception.hystrix.HystrixWrapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class CustomExceptionPrinter {
  /**
   * 业务异常打印logger
   */
  private static final Logger logger = LoggerFactory.getLogger(CustomExceptionPrinter.class);


  /**
   * 用 SCHExceptionPrinter.logger 打印异常
   * @param exceptionId
   * @param requestPath
   * @param e
   */
  public static void printStackTrace(String exceptionId, String requestPath, String requestMethod, Throwable e) {
    printStackTrace(exceptionId, requestPath, requestMethod, e, logger);
  }

  /**
   * 用传入的logger打印异常
   * @param exceptionId
   * @param requestPath
   * @param e
   * @param logger
   */
  public static void printStackTrace(String exceptionId, String requestPath, String requestMethod, Throwable e,
                                     Logger logger) {
    List<String> list = Lists.newArrayList();
    StringBuffer sb = new StringBuffer();
    if(StringUtils.isNotBlank(exceptionId)){
      list.add("ExceptionId: " + exceptionId);
    }
    if(StringUtils.isNotBlank(requestPath)){
      list.add("Path: " + requestPath);
    }
    if(StringUtils.isNotBlank(requestMethod)){
      list.add("Method: " + requestMethod);
    }
    boolean hasStack = false;
    if(e instanceof HystrixWrapper) {
      ExceptionResponseEntity entity = ((HystrixWrapper) e).getExceptionResponseEntity();
      list.add("TargetPath: " + entity.getPath());
      list.add("TargetMethod: " + entity.getMethod());
      list.add("TargetModule: " + entity.getModule());
      list.add("TargetException: " + entity.getException());
      if(entity.getInvokeStack() != null) {
        hasStack = true;
        list.add("InvokeStack: at " + entity.getInvokeStack());
      }
    }
    if(!hasStack){
      String stack = GlobalExceptionHandler.FEIGN_INVOKE_STACK.get();
      if(stack != null){
        list.add("InvokeStack: at " + stack);
      }
    }
    logger.error(StringUtils.join(list, "\r\n"), e);
  }
}
