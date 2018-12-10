/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.zuul.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * Url编码
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-08-02 16:19
 */
//@Component
//@RefreshScope
//@ConfigurationProperties("he.filters.encode")
public class EncodeFilter extends ZuulFilter {

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
    //只对GET方法编码
    return RequestContext.getCurrentContext().getRequest().getMethod().equalsIgnoreCase(HttpMethod.GET.toString());
  }

  @Override
  public Object run() {
    Map<String, List<String>> params = RequestContext.getCurrentContext().getRequestQueryParams();
    if(params == null){
      params = Maps.newHashMap();
    }
    HttpServletRequest req = RequestContext.getCurrentContext().getRequest();
    Map<String, String[]> map = req.getParameterMap();
    for (Map.Entry<String, String[]> entry : map.entrySet()) {
      List<String> values = Lists.newArrayList();
      for (String s : entry.getValue()) {
        try {
          values.add(URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
      params.put(entry.getKey(), values);
    }
    RequestContext.getCurrentContext().setRequestQueryParams(params);
    return null;
  }
}
