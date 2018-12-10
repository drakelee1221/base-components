package com.base.components.common.exception;

import com.base.components.common.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Throwables;
import com.base.components.common.constants.sys.Dates;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ExceptionResponseEntity 在Feign decode 的时候用于解析异常的返回值
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-12-08 15:50
 */
public class ExceptionResponseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  private String exception;

  private String exceptionId;

  private String path;

  private String method;

  private String module;

  private Boolean isCustom;

  private String message;

  private String error;

  private String timestamp;

  private Integer status;

  private Integer errorCode;

  @JsonIgnore
  private String invokeStack;

  public ExceptionResponseEntity() {
  }

  public ExceptionResponseEntity(String exception, Boolean isCustom, String message, Integer status) {
    this.exception = exception;
    this.isCustom = isCustom;
    this.message = message;
    this.status = status;
  }

  public ExceptionResponseEntity(Throwable exception, Boolean isCustom, String message, String module,
                                 String exceptionId, HttpStatus status, HttpServletRequest request) {
    Throwable rootCause = Throwables.getRootCause(exception);
    this.exception = rootCause.getClass().getName();
    this.exceptionId = exceptionId;
    if(request != null){
      this.path = request.getRequestURI();
      this.method = request.getMethod();
    }
    this.module = module;
    this.isCustom = isCustom;
    this.message = StringUtils.isBlank(message)?rootCause.getMessage() : message;
    if(status != null){
      this.error = status.getReasonPhrase();
      this.status = status.value();
    }
    this.timestamp = DateTime.now().toString(Dates.DATE_TIME_FORMATTER_PATTERN);
  }

  public static ExceptionResponseEntity create(String json) {
    try {
      return JsonUtils.mapper.readValue(json, ExceptionResponseEntity.class);
    } catch (Exception e) {
      return null;
    }
  }

  public Map<String, Object> toMap() {
    return JsonUtils.convert(this, new TypeReference<LinkedHashMap<String, Object>>() {
    });
  }

  public String getException() {
    return exception;
  }

  public void setException(String exception) {
    this.exception = exception;
  }

  public String getExceptionId() {
    return exceptionId;
  }

  public void setExceptionId(String exceptionId) {
    this.exceptionId = exceptionId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public Boolean getCustom() {
    return isCustom;
  }

  public void setCustom(Boolean custom) {
    isCustom = custom;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Integer getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  public String getInvokeStack() {
    return invokeStack;
  }

  public void setInvokeStack(String invokeStack) {
    this.invokeStack = invokeStack;
  }

}

