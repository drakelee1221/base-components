/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.zuul.configuration;

import com.base.components.common.constants.InvasiveRewrite;
import com.netflix.niws.client.http.RestClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.netflix.ribbon.apache.RibbonLoadBalancingHttpClient;
import org.springframework.cloud.netflix.ribbon.okhttp.OkHttpLoadBalancingClient;
import org.springframework.cloud.netflix.ribbon.support.RibbonCommandContext;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.cloud.netflix.zuul.filters.route.RestClientRibbonCommand;
import org.springframework.cloud.netflix.zuul.filters.route.RestClientRibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommand;
import org.springframework.cloud.netflix.zuul.filters.route.apache.HttpClientRibbonCommand;
import org.springframework.cloud.netflix.zuul.filters.route.apache.HttpClientRibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.okhttp.OkHttpRibbonCommand;
import org.springframework.cloud.netflix.zuul.filters.route.okhttp.OkHttpRibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.support.AbstractRibbonCommandFactory;

import java.util.Set;

/**
 * CustomRibbonCommandFactory
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-04-27 16:02
 *
 * @since Spring Cloud (Finchley.RC1)
 */
@InvasiveRewrite
public class CustomRibbonCommandFactory extends AbstractRibbonCommandFactory {
  private SpringClientFactory clientFactory;
  private ZuulProperties zuulProperties;
  private AbstractRibbonCommandFactory delegate;
  private String uriPrefix;

  public CustomRibbonCommandFactory(SpringClientFactory clientFactory,
                                    ZuulProperties zuulProperties,
                                    Set<FallbackProvider> zuulFallbackProviders,
                                    AbstractRibbonCommandFactory delegate,
                                    String uriPrefix) {
    super(zuulFallbackProviders);
    this.clientFactory = clientFactory;
    this.zuulProperties = zuulProperties;
    this.delegate = delegate;
    this.uriPrefix = StringUtils.isBlank(uriPrefix) ? null : uriPrefix;
  }

  @Override
  public RibbonCommand create(RibbonCommandContext context) {
    RibbonCommand command;
    if(delegate instanceof OkHttpRibbonCommandFactory){
      command = createOkHttp(context);
    }
    else if(delegate instanceof HttpClientRibbonCommandFactory){
      command = createHttpClient(context);
    }
    else if(delegate instanceof RestClientRibbonCommandFactory){
      command = createRestClient(context);
    }
    else {
      command = delegate.create(context);
    }
    return command;
  }

  /** {@link OkHttpRibbonCommandFactory#create(RibbonCommandContext)} */
  private RibbonCommand createOkHttp(RibbonCommandContext context) {
    final String serviceId = context.getServiceId();
    FallbackProvider fallbackProvider = getFallbackProvider(serviceId);
    final OkHttpLoadBalancingClient client = this.clientFactory.getClient(
      serviceId, OkHttpLoadBalancingClient.class);
    client.setLoadBalancer(this.clientFactory.getLoadBalancer(serviceId));

    return new OkHttpRibbonCommand(commandKey(context), client, context, zuulProperties, fallbackProvider,
                                   clientFactory.getClientConfig(serviceId));
  }

  /** {@link RestClientRibbonCommandFactory#create(RibbonCommandContext)} */
  private RibbonCommand createRestClient(RibbonCommandContext context) {
    String serviceId = context.getServiceId();
    FallbackProvider fallbackProvider = getFallbackProvider(serviceId);
    RestClient restClient = this.clientFactory.getClient(serviceId,
                                                         RestClient.class);
    return new RestClientRibbonCommand(commandKey(context), restClient, context,
                                       this.zuulProperties, fallbackProvider, clientFactory.getClientConfig(serviceId));
  }

  /** {@link HttpClientRibbonCommandFactory#create(RibbonCommandContext)} */
  private RibbonCommand createHttpClient(final RibbonCommandContext context) {
    FallbackProvider zuulFallbackProvider = getFallbackProvider(context.getServiceId());
    final String serviceId = context.getServiceId();
    final RibbonLoadBalancingHttpClient client = this.clientFactory.getClient(
      serviceId, RibbonLoadBalancingHttpClient.class);
    client.setLoadBalancer(this.clientFactory.getLoadBalancer(serviceId));

    return new HttpClientRibbonCommand(commandKey(context), client, context, zuulProperties, zuulFallbackProvider,
                                       clientFactory.getClientConfig(serviceId)){
    };
  }

  private String commandKey(RibbonCommandContext context){
    if(uriPrefix != null && context.getUri().startsWith(uriPrefix)){
      return context.getServiceId() + " | " + context.getMethod() + " | " + context.getUri();
    }else{
      return context.getServiceId();
    }
  }
}
