/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.interceptors;

import com.base.components.common.exception.auth.AuthException;
import com.base.components.common.token.RequireToken;
import com.base.components.common.token.TokenCacheObj;
import com.base.components.common.token.TokenThreadLocal;
import com.base.components.common.token.TokenTypes;
import com.base.components.common.util.JsonUtils;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Set;

/**
 * 接收由网关层传输过来的token对象，【在 Servlet 环境下使用】
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-12 14:03
 */
public class TokenObjReceiveInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    TokenThreadLocal.cleanThread();
    RequireToken requireToken = null;
    if(handler instanceof HandlerMethod){
      HandlerMethod h = (HandlerMethod)handler;
      requireToken = h.getMethodAnnotation(RequireToken.class);
    }
    Set<Class<? extends TokenCacheObj>> hasTokenClasses = Sets.newHashSet();
    for (TokenTypes tokenType : TokenTypes.getAllTokenTypes()) {
      String json = request.getHeader(tokenType.getReceiveJsonKey());
      String className = request.getHeader(TokenCacheObj.CLASS_RECEIVE_PREFIX_KEY + tokenType.getReceiveJsonKey());
      hasTokenClasses.add(putTokenObj(json, className, tokenType));
    }
    checkRequire(hasTokenClasses, requireToken);
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
    ModelAndView modelAndView) throws Exception {

  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    throws Exception {
    TokenThreadLocal.cleanThread();
  }


  private Class<? extends TokenCacheObj> putTokenObj(String json, String className, TokenTypes tokenType){
    Class<? extends TokenCacheObj> tokenClass = null;
    if(StringUtils.isNotBlank(json) && tokenType.getTypeClass().getName().equals(className)){
      try {
        TokenCacheObj obj = JsonUtils.reader(URLDecoder.decode(json, "UTF-8"), tokenType.getTypeClass());
        tokenClass = tokenType.getTypeClass();
        if(obj != null && obj.objId() != null){
          TokenThreadLocal.putTokenCacheObj(tokenType, obj);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return tokenClass;
  }

  private void checkRequire(Set<Class<? extends TokenCacheObj>> hasTokenClasses, RequireToken requireToken){
    boolean check = false;
    if(requireToken != null && hasTokenClasses != null && !hasTokenClasses.isEmpty()){
      for (Class<? extends TokenCacheObj> clazz : requireToken.value()) {
        if(hasTokenClasses.contains(clazz)){
          check = true;
          break;
        }
      }
      if(!check){
        throw new AuthException();
      }
    }
  }

}
