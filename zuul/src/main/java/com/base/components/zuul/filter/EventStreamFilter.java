package com.base.components.zuul.filter;

import com.base.components.zuul.dto.stream.EventStreamHttpServletResponse;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;

/**
 * EventStreamFilter
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-12 9:26
 */
@Component
@ConditionalOnProperty(name = "base.filters.event-stream.enabled", havingValue = "true", matchIfMissing = true)
public class EventStreamFilter extends ZuulFilter {

  @Autowired
  private SendResponseFilter sendResponseFilter;

  @Override
  public String filterType() {
    return POST_TYPE;
  }

  @Override
  public int filterOrder() {
    //需要在 SendResponseFilter 之前执行
    return sendResponseFilter.filterOrder() - 1;
  }

  @Override
  public boolean shouldFilter() {
    RequestContext context = RequestContext.getCurrentContext();
    return context.getThrowable() == null
      && (!context.getZuulResponseHeaders().isEmpty()
      || context.getResponseDataStream() != null
      || context.getResponseBody() != null);
  }

  @Override
  public Object run() throws ZuulException {
    RequestContext context = RequestContext.getCurrentContext();
    HttpServletRequest request = context.getRequest();
    String mediaType = request.getHeader(HttpHeaders.ACCEPT);
    if(StringUtils.isBlank(mediaType)){
      mediaType = request.getHeader(HttpHeaders.CONTENT_TYPE);
    }
    context.setResponse(EventStreamHttpServletResponse.build(context.getResponse(), mediaType));
    return null;
  }
}
