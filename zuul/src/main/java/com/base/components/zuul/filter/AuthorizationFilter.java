/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.zuul.filter;

import com.base.components.common.dto.auth.AuthorizationProperties;
import com.base.components.common.token.TokenCacheObj;
import com.base.components.common.token.TokenManager;
import com.base.components.common.token.TokenTypes;
import com.base.components.common.util.NonBlankObjectMapper;
import com.base.components.zuul.exception.ResponseWriteException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * 权限过滤器
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-11 15:19
 */
@Component
@RefreshScope
@ConfigurationProperties("base.filters.authorization")
@ConditionalOnProperty(name = "base.filters.authorization.enabled", havingValue = "true", matchIfMissing = true)
public class AuthorizationFilter extends ZuulFilter {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

  @Autowired
  private TokenManager tokenManager;

  private List<String> accessControlRequestHeaders = Lists.newArrayList();

  private Map<String, AuthorizationProperties> authMap = Maps.newHashMap();

  private int order;

  @Override
  public String filterType() {
    return PRE_TYPE;
  }

  @Override
  public int filterOrder() {
    return order;
  }

  @Override
  public boolean shouldFilter() {
    RequestContext context = RequestContext.getCurrentContext();
    HttpServletRequest request = context.getRequest();
    String uri = getUri(context);
    if(request.getMethod().equals(HttpMethod.OPTIONS.toString())){
      String header = request.getHeader("Access-Control-Request-Headers");
      if(StringUtils.isNotBlank(header)){
        String[] headers = StringUtils.split(header, ",");
        for (String h : headers) {
          if(!accessControlRequestHeaders.contains(h.trim().toLowerCase())){
            unauthorized(uri);
            break;
          }
        }
      }
      return false;
    }
    Object proxy = context.get(FilterConstants.PROXY_KEY);
    if(proxy != null){
      AuthorizationProperties properties = authMap.get(proxy.toString());
      if(properties != null){
        AuthorizationProperties.CheckType checkTokenType = properties.checkTokenType(uri);
        if(checkTokenType != AuthorizationProperties.CheckType.STRICT){
          if(checkTokenType == AuthorizationProperties.CheckType.ONLY_TRANSFER){
            checkTokens(false);
          }
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public Object run() {
    checkTokens(true);
    return null;
  }

  /**
   * @param checkAll true=至少有一种登录类型token有效，否则401；false=只把有效的登录token对象传入下游服务，不抛异常
   */
  private void checkTokens(boolean checkAll){
    RequestContext ctx = RequestContext.getCurrentContext();
    HttpServletRequest req = ctx.getRequest();
    boolean hasToken = false;
    Map<String, TokenCacheObj> tokens = Maps.newHashMap();
    for (TokenTypes tokenType : TokenTypes.getAllTokenTypes()) {
      Serializable t = TokenTypes.getTokenInRequest(req, tokenType);
      String token = (t == null ? null : t.toString());
      if (StringUtils.isNotBlank(token)) {
        TokenCacheObj tokenObj = tokens.get(tokenType.getTokenKey() + token);
        if(tokenObj == null){
          try {
            tokenObj = tokenManager.getByToken(token);
          } catch (Exception e) {
            logger.error("", e);
          }
          if(tokenObj == null || !tokenType.getTypeClass().equals(tokenObj.getClass())){
            continue;
          }
          tokens.put(tokenType.getTokenKey() + token, tokenObj);
        }
        else{
          //一个tokenKey + tokenValue 只有一个类型会被取出
          continue;
        }
        try {
          ctx.addZuulRequestHeader(tokenType.getReceiveJsonKey(),
                                   URLEncoder.encode(NonBlankObjectMapper.INSTANCE.writeValueAsString(tokenObj), "UTF-8"));
          ctx.addZuulRequestHeader(TokenCacheObj.CLASS_RECEIVE_PREFIX_KEY + tokenType.getReceiveJsonKey(),
                                   URLEncoder.encode(tokenObj.getClass().getName(), "UTF-8"));
          hasToken = true;
        } catch (Exception e) {
          logger.error("缓存对象转成json字符串异常", e);
        }
      }
    }
    if(checkAll && !hasToken){
      unauthorized(getUri(ctx));
    }
  }

  private void unauthorized(String uri) {
    RequestContext ctx = RequestContext.getCurrentContext();
    ctx.setSendZuulResponse(false);
    ObjectNode node = NonBlankObjectMapper.INSTANCE.createObjectNode();
    node.put("path", uri);
    node.put("timestamp", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
    node.put("isCustom", Boolean.TRUE);
    node.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
    node.put("message", HttpStatus.UNAUTHORIZED.getReasonPhrase());
    node.put("status", HttpStatus.UNAUTHORIZED.value());
    HttpServletResponse resp = ctx.getResponse();
    resp.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    resp.setStatus(HttpStatus.UNAUTHORIZED.value());
    try (Writer out = resp.getWriter()) {
      out.append(node.toString());
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResponseWriteException(e);
    }
  }

  private String getUri(RequestContext context){
    String uri = (String) context.get(FilterConstants.REQUEST_URI_KEY);
    if(uri == null){
      uri = context.getRequest().getRequestURI();
    }
    return uri;
  }

  public List<String> getAccessControlRequestHeaders() {
    return accessControlRequestHeaders;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public Map<String, AuthorizationProperties> getAuthMap() {
    return authMap;
  }

  public void setAuthMap(Map<String, AuthorizationProperties> authMap) {
    this.authMap = authMap;
  }
}

