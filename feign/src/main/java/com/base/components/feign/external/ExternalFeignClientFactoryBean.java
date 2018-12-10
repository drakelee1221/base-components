/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.external;

import com.base.components.common.constants.rpc.RpcHeaders;
import com.base.components.common.exception.ExternalFeignClientServices;
import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * ExternalFeignClientFactoryBean
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-27 14:15
 */
@SuppressWarnings("all")
@RefreshScope
public class ExternalFeignClientFactoryBean implements FactoryBean<Object>, ApplicationContextAware {
  private ApplicationContext applicationContext;
  private ExternalFeignClientServices externalFeignClientServices;
  private Class<?> type;
  private String name;
  private ErrorDecoder errorDecoder;

  @Nullable
  @Override
  @RefreshScope
  public Object getObject() throws Exception {
    FeignContext feignContext = applicationContext.getBean(FeignContext.class);
    FeignLoggerFactory loggerFactory = get(feignContext, FeignLoggerFactory.class);
    Logger logger = loggerFactory.create(this.type);
    Feign.Builder builder = get(feignContext, Feign.Builder.class)
      // required values
      .logger(logger)
      .encoder(get(feignContext, Encoder.class))
      .decoder(get(feignContext, Decoder.class))
      .contract(get(feignContext, Contract.class));
    Client client = feignContext.getInstance(name, Client.class);
    if (client != null) {
      if (client instanceof LoadBalancerFeignClient) {
        // not lod balancing because we have a url,
        // but ribbon is on the classpath, so unwrap
        client = ((LoadBalancerFeignClient) client).getDelegate();
      }
    }
    configureFeign(feignContext, builder);
    if(this.errorDecoder != null){
      builder.errorDecoder(errorDecoder);
    }
    return builder.client(client).target(
      new Target.HardCodedTarget(type, name, "-") {
        @Override
        public String url() {
          String host = externalFeignClientServices.getHost(name);
          Assert.hasText(host, name + " > host is empty !");
          return host;
        }
        @Override
        public Request apply(RequestTemplate input) {
          if (input.url().indexOf("http") != 0) {
            input.insert(0, url());
          }
          input.header("Content-Length", "0");
          input.header(RpcHeaders.CLIENT_MARK_KEY, name);
          return input.request();
        }
      });
  }

  protected <T> T getOptional(FeignContext context, Class<T> type) {
    return context.getInstance(this.name, type);
  }

  protected <T> T get(FeignContext context, Class<T> type) {
    T instance = context.getInstance(this.name, type);
    if (instance == null) {
      throw new IllegalStateException("No bean found of type " + type + " for " + this.name);
    }
    return instance;
  }

  private <T> T getOrInstantiate(Class<T> tClass) {
    try {
      return applicationContext.getBean(tClass);
    } catch (NoSuchBeanDefinitionException e) {
      return BeanUtils.instantiateClass(tClass);
    }
  }

  protected void configureFeign(FeignContext context, Feign.Builder builder) {
    FeignClientProperties properties = applicationContext.getBean(FeignClientProperties.class);
    if (properties != null) {
      if (properties.isDefaultToProperties()) {
        configureUsingConfiguration(context, builder);
        configureUsingProperties(properties.getConfig().get(properties.getDefaultConfig()), builder);
        configureUsingProperties(properties.getConfig().get(this.name), builder);
      } else {
        configureUsingProperties(properties.getConfig().get(properties.getDefaultConfig()), builder);
        configureUsingProperties(properties.getConfig().get(this.name), builder);
        configureUsingConfiguration(context, builder);
      }
    } else {
      configureUsingConfiguration(context, builder);
    }
  }

  protected void configureUsingConfiguration(FeignContext context, Feign.Builder builder) {
    Logger.Level level = getOptional(context, Logger.Level.class);
    if (level != null) {
      builder.logLevel(level);
    }
    Retryer retryer = getOptional(context, Retryer.class);
    if (retryer != null) {
      builder.retryer(retryer);
    }
    ErrorDecoder errorDecoder = getOptional(context, ErrorDecoder.class);
    if (errorDecoder != null) {
      builder.errorDecoder(errorDecoder);
    }
    Request.Options options = getOptional(context, Request.Options.class);
    if (options != null) {
      builder.options(options);
    }
    Map<String, RequestInterceptor> requestInterceptors = context.getInstances(
      this.name, RequestInterceptor.class);
    if (requestInterceptors != null) {
      builder.requestInterceptors(requestInterceptors.values());
    }
  }

  protected void configureUsingProperties(FeignClientProperties.FeignClientConfiguration config, Feign.Builder builder) {
    if (config == null) {
      return;
    }

    if (config.getLoggerLevel() != null) {
      builder.logLevel(config.getLoggerLevel());
    }

    if (config.getConnectTimeout() != null && config.getReadTimeout() != null) {
      builder.options(new Request.Options(config.getConnectTimeout(), config.getReadTimeout()));
    }

    if (config.getRetryer() != null) {
      Retryer retryer = getOrInstantiate(config.getRetryer());
      builder.retryer(retryer);
    }

    if (config.getErrorDecoder() != null) {
      ErrorDecoder errorDecoder = getOrInstantiate(config.getErrorDecoder());
      builder.errorDecoder(errorDecoder);
    }

    if (config.getRequestInterceptors() != null && !config.getRequestInterceptors().isEmpty()) {
      // this will add request interceptor to builder, not replace existing
      for (Class<RequestInterceptor> bean : config.getRequestInterceptors()) {
        RequestInterceptor interceptor = getOrInstantiate(bean);
        builder.requestInterceptor(interceptor);
      }
    }

    if (config.getDecode404() != null) {
      if (config.getDecode404()) {
        builder.decode404();
      }
    }
  }

  @Nullable
  @Override
  public Class<?> getObjectType() {
    return type;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public ExternalFeignClientServices getExternalFeignClientServices() {
    return externalFeignClientServices;
  }

  public void setExternalFeignClientServices(ExternalFeignClientServices externalFeignClientServices) {
    this.externalFeignClientServices = externalFeignClientServices;
  }

  public Class<?> getType() {
    return type;
  }

  public void setType(Class<?> type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ErrorDecoder getErrorDecoder() {
    return errorDecoder;
  }

  public void setErrorDecoder(ErrorDecoder errorDecoder) {
    this.errorDecoder = errorDecoder;
  }
}
