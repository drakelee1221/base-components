/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.exception;

import com.base.components.common.boot.Profiles;
import com.base.components.common.constants.rpc.RpcHeaders;
import com.base.components.common.constants.sys.Dates;
import com.base.components.common.exception.auth.AuthException;
import com.base.components.common.exception.auth.AuthTokenException;
import com.base.components.common.exception.auth.AuthenticationException;
import com.base.components.common.exception.auth.OssRedirectAuthException;
import com.base.components.common.exception.business.BusinessException;
import com.base.components.common.exception.hystrix.HystrixBadRequestWrapperException;
import com.base.components.common.exception.hystrix.HystrixErrorWrapperException;
import com.base.components.common.exception.hystrix.HystrixWrapper;
import com.base.components.common.exception.other.ForbiddenException;
import com.base.components.common.exception.other.InternalServerException;
import com.google.common.base.Throwables;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 统一异常处理，【在 Servlet 环境下使用】
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-03-02 11:56
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RestControllerAdvice
@Component
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /** feign调用异常栈，一般在 DynamicHeaderRegistry#getAllHeaders 中设置 */
  public static final ThreadLocal<String> FEIGN_INVOKE_STACK = new ThreadLocal<>();

  /** 是否忽略 isSimpleResult ，强制抛出异常，默认不忽略 */
  private static final ThreadLocal<Boolean> CURRENT_IGNORE_SIMPLE_RESULT = new ThreadLocal<>();

  private Pattern feignExPattern = Pattern.compile("(.*http)?((://(.*?)/)|(s://(.*?)/))");

  @Autowired(required = false)
  private ExternalFeignClientServices externalFeignClientServices;

  @Autowired
  private ExceptionIdCreator exceptionIdCreator;

  @Autowired(required = false)
  private ExceptionViewPathHandler exceptionViewPathHandler;

  private boolean isProduction;

  @Value("${spring.application.name}")
  private String appName;

  /** 是否直接将response的状态码改为200，用于一些状态区分不细的管理端页面 */
  @Value("${base.isSimpleResult:false}")
  private boolean isSimpleResult;

  /**
   * 自定义处理类型
   */
  public static final String IS_CUSTOM = "custom";

  @PostConstruct
  public void init(){
    isProduction = Profiles.contains(Profiles.PROD);
  }

  @ExceptionHandler({AuthException.class, AuthTokenException.class, OssRedirectAuthException.class})
  @Order(0)
  public Object authExceptionHandler(Exception e, HttpServletResponse response, HttpServletRequest request) {
    ExceptionResponseEntity entity = buildEntity(e, request, true);
    //因为hystrix会将401异常的body清空，所以如果当前请求是被RPC调用，则将401改为400
    if (request.getHeader(RpcHeaders.CLIENT_MARK_KEY) != null) {
      return realReturn(buildResponse(entity, HttpStatus.BAD_REQUEST), request);
    }
    return realReturn(buildResponse(entity, HttpStatus.UNAUTHORIZED), request);
  }

  @ExceptionHandler({BusinessException.class})
  @Order(0)
  public Object businessExceptionHandler(Exception e, HttpServletResponse response, HttpServletRequest request) {
    ExceptionResponseEntity entity = buildEntity(e, request, true);
    entity.setErrorCode(((BusinessException) e).getErrorCode());
    return realReturn(buildResponse(entity, HttpStatus.BAD_REQUEST), request);
  }

  /**
   * Custom exception handler response entity.
   *
   * @param e the e
   * @param response the response
   * @param request the request
   *
   * @return the response entity
   */
  @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class})
  @Order(0)
  public Object customExceptionHandler(Exception e, HttpServletResponse response, HttpServletRequest request) {
    ExceptionResponseEntity entity = buildEntity(e, request, true);
    return realReturn(buildResponse(entity, HttpStatus.BAD_REQUEST), request);
  }

  /**
   * Method argument type mismatch exception handler response entity.
   *
   * @param e the e
   * @param response the response
   * @param request the request
   *
   * @return the response entity
   */
  @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
  @Order(1)
  public Object methodArgumentTypeMismatchExceptionHandler(Exception e, HttpServletResponse response,
                                                           HttpServletRequest request) {
    ExceptionResponseEntity entity = buildEntity(e, request, false);
    entity.setMessage("请求参数类型错误");
    return realReturn(buildResponse(entity, HttpStatus.BAD_REQUEST), request);
  }

  /**
   * Spring mvc exception handler response entity.
   *
   * @param e the e
   * @param response the response
   * @param request the request
   *
   * @return the response entity
   */
  @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
  @Order(2)
  public Object springMVCExceptionHandler(Exception e, HttpServletResponse response, HttpServletRequest request) {
    ExceptionResponseEntity entity = buildEntity(e, request, false);
    entity.setMessage("请求类型错误");
    return realReturn(buildResponse(entity, HttpStatus.BAD_REQUEST), request);
  }

  /**
   * Http media type not supported exception handler response entity.
   *
   * @param e the e
   * @param response the response
   * @param request the request
   *
   * @return the response entity
   */
  @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
  @Order(3)
  public Object httpMediaTypeNotSupportedExceptionHandler(Exception e, HttpServletResponse response,
                                                          HttpServletRequest request) {
    ExceptionResponseEntity entity = buildEntity(e, request, false);
    entity.setMessage("请求MediaType类型错误");
    return realReturn(buildResponse(entity, HttpStatus.BAD_REQUEST), request);
  }

  @ExceptionHandler({ForbiddenException.class})
  @Order(4)
  public Object forbiddenExceptionHandler(Exception e, HttpServletResponse response, HttpServletRequest request) {
    ExceptionResponseEntity entity = buildEntity(e, request, true);
    String message = entity.getMessage();
    if (StringUtils.isBlank(message)) {
      entity.setMessage(HttpStatus.FORBIDDEN.getReasonPhrase());
    }
    return realReturn(buildResponse(entity, HttpStatus.FORBIDDEN), request);
  }

  @ExceptionHandler({EmptyResultDataAccessException.class})
  @Order(7)
  public Object emptyResultDataAccessExceptionHandler(Exception e, HttpServletResponse response,
                                                      HttpServletRequest request) {
    ExceptionResponseEntity entity = buildEntity(e, request, false);
    entity.setMessage(Throwables.getRootCause(e).getMessage());
    return realReturn(buildResponse(entity, HttpStatus.BAD_REQUEST), request);
  }

  /**
   * Hystrix runtime exception handler response entity.
   *
   * @param e the e
   * @param response the response
   * @param request the request
   *
   * @return the response entity
   */
  @ExceptionHandler({HystrixRuntimeException.class, HystrixBadRequestWrapperException.class,
    HystrixErrorWrapperException.class})
  @Order(8)
  public Object hystrixRuntimeExceptionHandler(Exception e, HttpServletResponse response, HttpServletRequest request) {
    Object result = unWrapperHystrixException(e, request);
    if (result == null) {
      result = exceptionHandler((Exception) e.getCause(), response, request);
    } else {
      CURRENT_IGNORE_SIMPLE_RESULT.remove();
    }
    return result;
  }

  /**
   * Exception handler response entity.
   *
   * @param e the e
   * @param response the response
   * @param request the request
   *
   * @return the response entity
   */
  @ExceptionHandler({InternalServerException.class})
  @Order(9)
  public Object internalServerExceptionHandler(Exception e, HttpServletResponse response, HttpServletRequest request) {
    ExceptionResponseEntity entity = buildEntity(e, request, false);
    return realReturn(buildResponse(entity, HttpStatus.INTERNAL_SERVER_ERROR), request);
  }

  @ExceptionHandler({Exception.class})
  @Order(10)
  public Object exceptionHandler(Exception e, HttpServletResponse response, HttpServletRequest request) {
    ResponseEntity<ExceptionResponseEntity> responseEntity = feignExceptionHandler(e, request);
    if (responseEntity != null) {
      return realReturn(responseEntity, request);
    }
    ExceptionResponseEntity entity = buildEntity(e, request, false);
    if (isProduction) {
      entity.setMessage("服务器异常");
    }
    return realReturn(buildResponse(entity, HttpStatus.INTERNAL_SERVER_ERROR), request);
  }

  /**
   * 构建异常实体
   *
   * @param e -
   * @param request -
   * @param isCustom 是否是自定义异常
   * @param isRootException true=会循环获取到实际异常的堆栈；false=就当前异常进行返回
   *
   * @return ExceptionResponseEntity
   */
  private ExceptionResponseEntity buildEntity(Throwable e, HttpServletRequest request, boolean isCustom,
                                              boolean isRootException) {
    ExceptionResponseEntity entity = new ExceptionResponseEntity();
    Throwable rootCause = e;
    if (!isRootException) {
      rootCause = Throwables.getRootCause(e);
    }
    if (rootCause == null) {
      rootCause = e;
    }
    entity.setPath(request.getRequestURI());
    entity.setTimestamp(DateTime.now().toString(Dates.DATE_TIME_FORMATTER));
    entity.setMessage(rootCause.getMessage() == null ? "" : rootCause.getMessage());
    entity.setException(rootCause.getClass().getName());
    String exceptionId = exceptionIdCreator.getCurrentTraceId();
    entity.setExceptionId(exceptionId);
    entity.setModule(appName);
    entity.setCustom(isCustom ? true : null);
    if (!(e instanceof WithoutPrintException)) {
      if (isCustom) {
        CustomExceptionPrinter.printStackTrace(exceptionId, request.getRequestURI(), request.getMethod(), rootCause);
      } else {
        CustomExceptionPrinter
          .printStackTrace(exceptionId, request.getRequestURI(), request.getMethod(), rootCause, logger);
      }
    }
    return entity;
  }

  /**
   * 构建异常实体，会循环获取到实际异常的堆栈
   *
   * @param e -
   * @param request -
   * @param isCustom 是否是自定义异常
   *
   * @return ExceptionResponseEntity
   */
  private ExceptionResponseEntity buildEntity(Throwable e, HttpServletRequest request, boolean isCustom) {
    return buildEntity(e, request, isCustom, false);
  }

  /**
   * 构建 ResponseEntity
   *
   * @param entity 异常实体
   * @param status -
   *
   * @return ResponseEntity
   */
  private ResponseEntity<ExceptionResponseEntity> buildResponse(ExceptionResponseEntity entity, HttpStatus status) {
    entity.setStatus(status.value());
    entity.setError(status.getReasonPhrase());
    ResponseEntity<ExceptionResponseEntity> result;
    //    if(isSimpleResult && (CURRENT_IGNORE_SIMPLE_RESULT.get() == null || !CURRENT_IGNORE_SIMPLE_RESULT.get()) ){
    //      result = new ResponseEntity<>(map, HttpStatus.OK);
    //    }
    //    else{
    result = new ResponseEntity<>(entity, status);
    //    }
    return result;
  }

  /**
   * 解析断路器封装异常类
   *
   * @param e -
   * @param request -
   *
   * @return ResponseEntity
   */
  private ResponseEntity<ExceptionResponseEntity> unWrapperHystrixException(Exception e, HttpServletRequest request) {
    Throwable ex = e.getCause() == null ? e : e.getCause();
    if (ex instanceof HystrixWrapper) {
      ExceptionResponseEntity entity = ((HystrixWrapper) ex).getExceptionResponseEntity();
      HttpStatus status = HttpStatus.valueOf(entity.getStatus());
      if (StringUtils.isBlank(entity.getExceptionId())) {
        entity.setExceptionId(exceptionIdCreator.getCurrentTraceId());
      }
      if (entity.getCustom() != null && entity.getCustom()) {
        CustomExceptionPrinter
          .printStackTrace(entity.getExceptionId(), request.getRequestURI(), request.getMethod(), ex);
      } else {
        CustomExceptionPrinter
          .printStackTrace(entity.getExceptionId(), request.getRequestURI(), request.getMethod(), ex, logger);
      }
      //      if(isSimpleResult && (CURRENT_IGNORE_SIMPLE_RESULT.get() == null || !CURRENT_IGNORE_SIMPLE_RESULT.get())){
      //        return buildResponse(entity.toMap(), HttpStatus.OK);
      //      }
      //如果当前服务不是被远程调用的
      if (request.getHeader(RpcHeaders.CLIENT_MARK_KEY) == null) {
        //如果权限异常，就返回401
        if (AuthenticationException.checkIsAuthorizationException(entity.getException())) {
          status = HttpStatus.UNAUTHORIZED;
        }
        return buildResponse(entity, status);
      }

      return buildResponse(entity, status);
    }
    return null;
  }

  /**
   * 当解析断路器封装异常类失败时，则为Feign调用端的环境（网络等）异常
   *
   * @param e -
   * @param request -
   *
   * @return ResponseEntity
   */
  private ResponseEntity<ExceptionResponseEntity> feignExceptionHandler(Exception e, HttpServletRequest request) {
    Throwable t = e;
    boolean isFeignEx = false;
    if (e.getClass().getName().contains("feign")) {
      isFeignEx = true;
    }
    if (!isFeignEx) {
      Throwable rootCause = Throwables.getRootCause(e);
      if ("com.netflix.client.ClientException".equals(rootCause.getClass().getName())) {
        isFeignEx = true;
        t = rootCause;
      }
    }
    if (isFeignEx) {
      ExceptionResponseEntity entity = buildEntity(t, request, false, true);
      entity.setMessage("微服务调用异常");
      ResponseEntity<ExceptionResponseEntity> responseEntity = buildResponse(entity, HttpStatus.INTERNAL_SERVER_ERROR);
      entity.setError(feignExceptionMessageCheck(t.getMessage()));
      FEIGN_INVOKE_STACK.remove();
      return responseEntity;
    }
    return null;
  }

  /** 隐藏Feign调用端异常的敏感信息 */
  private String feignExceptionMessageCheck(String message) {
    Matcher m = feignExPattern.matcher(message);
    String host = m.find() ? "http" + m.group(2) : null;
    if (host != null) {
      if (externalFeignClientServices != null) {
        for (Map.Entry<String, String> entry : externalFeignClientServices.getAllExternalServices().entrySet()) {
          String v = entry.getValue().endsWith("/") ? entry.getValue() : entry.getValue() + "/";
          if (v.equals(host)) {
            return message.replace(host, "/" + entry.getKey() + "/");
          }
        }
      }
      return message.replace(host, "/");
    }
    return message;
  }

  /**
   * 根据请求的MediaType，返回真实的解析对象
   *
   * @param returnEntity -
   * @param request -
   *
   * @return
   */
  private Object realReturn(ResponseEntity<ExceptionResponseEntity> returnEntity, HttpServletRequest request) {
    boolean returnOk = false;
    //用于只返回200的请求
    if (isSimpleResult && (CURRENT_IGNORE_SIMPLE_RESULT.get() == null || !CURRENT_IGNORE_SIMPLE_RESULT.get())) {
      returnOk = true;
    }
    CURRENT_IGNORE_SIMPLE_RESULT.remove();
    //返回异常页面
    if (exceptionViewPathHandler != null && checkIsView(request)) {
      String path = null;
      switch (returnEntity.getStatusCode().value()) {
        case 500:
          path = exceptionViewPathHandler.internalServerError();
          break;
        case 400:
          path = exceptionViewPathHandler.badRequest();
          break;
        case 401:
          path = exceptionViewPathHandler.unauthorized();
          break;
        case 403:
          path = exceptionViewPathHandler.forbidden();
          break;
        case 404:
          path = exceptionViewPathHandler.notFound();
          break;
        default:
      }
      if (path != null) {
        ModelAndView modelAndView = new ModelAndView(path, returnOk ? HttpStatus.OK : returnEntity.getStatusCode());
        modelAndView.getModelMap().put("errorMap", returnEntity.getBody());
        return modelAndView;
      }
    }
    if (returnOk) {
      return new ResponseEntity<>(returnEntity.getBody(), HttpStatus.OK);
    }
    return returnEntity;
  }

  /** 检查是否为页面请求（非 AJAX） */
  private boolean checkIsView(HttpServletRequest request) {
    String accept = request.getHeader(HttpHeaders.ACCEPT);
    if (StringUtils.isNotBlank(accept) && accept.toLowerCase().contains(MediaType.TEXT_HTML_VALUE)) {
      return true;
    }
    return false;
  }



  /** 当前方法，是否忽略 isSimpleResult ，强制抛出异常，true = 强制抛出异常 */
  public static void ignoreSimpleResult(boolean ignore) {
    CURRENT_IGNORE_SIMPLE_RESULT.set(ignore);
  }

}