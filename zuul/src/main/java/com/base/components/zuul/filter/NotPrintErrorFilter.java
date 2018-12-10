package com.base.components.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.zuul.filters.post.SendErrorFilter;
import org.springframework.stereotype.Component;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;

/**
 * NotPrintErrorFilter
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-12 16:16
 */
@Component
@ConditionalOnProperty(name = "base.filters.not-print-error.enabled", havingValue = "true", matchIfMissing = true)
public class NotPrintErrorFilter extends ZuulFilter {
  private static final String NOT_PRINT_ERROR = "_NOT_PRINT_ERROR_";

  @Autowired
  private SendErrorFilter sendErrorFilter;

  @Override
  public String filterType() {
    return ERROR_TYPE;
  }

  @Override
  public int filterOrder() {
    //需在 SendErrorFilter 之前
    return sendErrorFilter.filterOrder() - 1;
  }

  @Override
  public boolean shouldFilter() {
    RequestContext ctx = RequestContext.getCurrentContext();
    Object val = ctx.get(NOT_PRINT_ERROR);
    return Boolean.TRUE == val;
  }

  @Override
  public Object run() throws ZuulException {
    RequestContext ctx = RequestContext.getCurrentContext();
    ctx.remove("throwable");
    return null;
  }

  public static void notPrintError(){
    RequestContext ctx = RequestContext.getCurrentContext();
    ctx.set(NOT_PRINT_ERROR, Boolean.TRUE);
  }

}
