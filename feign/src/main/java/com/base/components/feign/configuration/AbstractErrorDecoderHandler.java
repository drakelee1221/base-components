/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration;

import com.base.components.common.constants.sys.Dates;
import com.base.components.common.exception.ExceptionResponseEntity;
import com.base.components.common.exception.auth.AuthException;
import com.base.components.common.exception.auth.AuthenticationException;
import com.base.components.common.exception.hystrix.HystrixBadRequestWrapperException;
import com.base.components.common.exception.hystrix.HystrixErrorWrapperException;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;

import java.net.URL;

/**
 * 用于处理feign调用时服务端的异常信息解析
 * <pre>
 *   注意: 在 Hystrix 环境中，
 *         远程调用的返回状态大致分为：2XX = Success，4XX = Bad Request，5XX = Failure，
 *
 *         Hystrix 会在 Failure 调用次数达到阈值时(circuitBreaker.requestVolumeThreshold=默认20)，
 *         打开熔断机制(Circuit = Open)，
 *         并在回路时间(circuitBreaker.sleepWindowInMilliseconds=默认5秒)内拒绝该服务方法的所有请求，
 *         回路时间之后，所有非 Success 的返回状态都会继续被熔断（含Bad Request），再次打开回路时间，
 *         直到请求该远程服务返回一次2XX，才会关闭熔断机制(Circuit = Closed)；
 *
 *         由于 Hystrix 对远程服务返回的非 2XX 和 404 的 Response，都会视为 Failure；
 *         所以想要将 4XX 状态处理为 Bad Request，需要使用 HystrixBadRequestException 做 ErrorDecode 的返回值。
 * </pre>
 *  @see feign.SynchronousMethodHandler#executeAndDecode
 *  @see com.netflix.hystrix.AbstractCommand#executeCommandAndObserve
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-12-09 22:13
 */
public abstract class AbstractErrorDecoderHandler implements ErrorDecoder {

  /**
   * Decode exception.
   *
   * @param methodKey the method key
   * @param response the response
   *
   * @return the exception
   */
  @Override
  public Exception decode(String methodKey, Response response) {
    try {
      String body;
      if (response != null) {
        int status = response.status();
        if (response.body() != null && (body = Util.toString(response.body().asReader())).length() >= 1) {
          ExceptionResponseEntity entity = customBuild(methodKey, response, body);
          if(entity == null){
            entity = ExceptionResponseEntity.create(body);
          }
          if (entity != null) {
            entity.setMethod(response.request().method());
            entity.setInvokeStack(FeignClientDynamicHystrixConcurrencyStrategy.FEIGN_CLIENT_STACK_THREAD_LOCAL.get());
            if(entity.getStatus() == null){
              entity.setStatus(status);
            }
            if(StringUtils.isBlank(entity.getPath())){
              try {
                entity.setPath(new URL(response.request().url()).getPath());
              } catch (Exception ignore) {}
            }
            if(StringUtils.isBlank(entity.getTimestamp())){
              entity.setTimestamp(DateTime.now().toString(Dates.DATE_TIME_FORMATTER_PATTERN));
            }
            // 401
            if (status == HttpStatus.UNAUTHORIZED.value()) {
              if (StringUtils.isBlank(entity.getException())) {
                entity.setException(AuthException.class.getName());
              }
              entity.setCustom(true);
              return new HystrixBadRequestWrapperException(entity.getMessage(), entity);
            }
            // s >= 400 && s < 500
            else if (status >= HttpStatus.BAD_REQUEST.value() && status < HttpStatus.INTERNAL_SERVER_ERROR.value()) {
              entity.setCustom(true);
              return new HystrixBadRequestWrapperException(entity.getMessage(), entity);
            }
            else {
              return new HystrixErrorWrapperException(entity.getMessage(), entity);
            }
          }
        }
        if (status == HttpStatus.UNAUTHORIZED.value()) {
          ExceptionResponseEntity entity = new ExceptionResponseEntity(AuthException.class.getName(), true,
                                                                       AuthenticationException.UNAUTHORIZED_MSG,
                                                                       HttpStatus.UNAUTHORIZED.value()
          );
          return new HystrixBadRequestWrapperException(entity.getMessage(), entity);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return FeignException.errorStatus(methodKey, response);
  }

  /**
   * 通过返回值构建自定义ExceptionResponseEntity
   * @param methodKey
   * @param response
   * @param body
   *
   * @return
   */
  protected abstract ExceptionResponseEntity customBuild(String methodKey, Response response, String body);


  public static class DefaultHandler extends AbstractErrorDecoderHandler {
    @Override
    protected ExceptionResponseEntity customBuild(String methodKey, Response response, String body) {
      return null;
    }
  }
}
