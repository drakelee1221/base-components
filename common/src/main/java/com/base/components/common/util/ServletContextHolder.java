package com.base.components.common.util;

import com.base.components.common.constants.rpc.RpcHeaders;
import com.base.components.common.exception.other.InternalServerException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ServletContextHolder，【在 Servlet 环境下使用】
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-16 11:01
 */
@SuppressWarnings("all")
public abstract class ServletContextHolder {

  public static final String REDIRECT_PREFIX = UrlBasedViewResolver.REDIRECT_URL_PREFIX;

  @NonNull
  public static HttpServletRequest getRequestNonNull(){
    HttpServletRequest request = getRequest();
    if(request != null) {
      return request;
    }
    throw new NullPointerException("There is no HttpServletRequest in this Thread: " + Thread.currentThread());
  }

  @Nullable
  public static HttpServletRequest getRequestNullable(){
    return getRequest();
  }

  @NonNull
  public static HttpServletResponse getResponseNonNull(){
    ServletRequestAttributes attributes = getAttributes();
    if(attributes != null){
      HttpServletResponse response = attributes.getResponse();
      if(response != null){
        return response;
      }
    }
    throw new NullPointerException("There is no HttpServletResponse in this Thread: " + Thread.currentThread());
  }

  @Nullable
  public static HttpServletResponse getResponseNullable(){
    return getResponse();
  }

  /** 检查重定向地址 */
  public static String sendRedirectString(String location){
    if(location.startsWith(REDIRECT_PREFIX)){
      checkExternalRedirect(location.substring(REDIRECT_PREFIX.length(), location.length()));
    }
    else{
      checkExternalRedirect(location);
    }
    return location;
  }


  /** 直接重定向 */
  public static void sendRedirect(String location){
    checkExternalRedirect(location);
    try {
      getResponseNonNull().sendRedirect(location);
    } catch (Exception e) {
      throw new InternalServerException(e);
    }
  }

  /** 检测是否为重定向外部地址 */
  private static void checkExternalRedirect(String location){
    HttpServletResponse response = getResponseNonNull();
    try {

      UriComponentsBuilder.fromHttpUrl(location);
      response.setHeader(RpcHeaders.EXTERNAL_REDIRECT_HEADER, Boolean.TRUE.toString());
    } catch (Exception ignore) {
    }
  }

  private static HttpServletRequest getRequest(){
    ServletRequestAttributes attributes = getAttributes();
    if(attributes != null){
      return attributes.getRequest();
    }
    return null;
  }

  private static HttpServletResponse getResponse(){
    ServletRequestAttributes attributes = getAttributes();
    if(attributes != null){
      return attributes.getResponse();
    }
    return null;
  }

  private static ServletRequestAttributes getAttributes(){
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if(requestAttributes != null && requestAttributes instanceof ServletRequestAttributes){
      return (ServletRequestAttributes)requestAttributes;
    }
    return null;
  }
}
