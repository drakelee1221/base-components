/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.zuul.filter;

import com.google.common.collect.Lists;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * 重试过滤器
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-11 15:19
 */
@Component
@RefreshScope
@ConfigurationProperties("base.filters.retry")
@ConditionalOnProperty(name = "base.filters.retry.enabled", havingValue = "true", matchIfMissing = true)
public class RetryFilter extends ZuulFilter{

  private static final Logger logger = LoggerFactory.getLogger(RetryFilter.class);

  private List<String> notRetryMethods = Lists.newArrayList();

  private List<String> notRetryUrls = Lists.newArrayList();

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
    return true;
  }

  @Override
  public Object run() {
    RequestContext ctx = RequestContext.getCurrentContext();
    boolean notRetryMethod = notRetryMethods.contains(ctx.getRequest().getMethod().toUpperCase());
    ctx.set(FilterConstants.RETRYABLE_KEY, !notRetryMethod);
    String uri = ctx.getRequest().getRequestURI();
    if(!notRetryMethod && StringUtils.isNotBlank(uri)){
      for (String notRetryUrl : notRetryUrls) {
        if(uri.startsWith(notRetryUrl)){
          ctx.set(FilterConstants.RETRYABLE_KEY, false);
          break;
        }
      }
    }
    return null;
  }

  public List<String> getNotRetryMethods() {
    return notRetryMethods;
  }

  public List<String> getNotRetryUrls() {
    return notRetryUrls;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public int getOrder() {
    return order;
  }
}
