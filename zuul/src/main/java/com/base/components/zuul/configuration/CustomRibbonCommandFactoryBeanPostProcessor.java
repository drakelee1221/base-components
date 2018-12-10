/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.zuul.configuration;

import com.base.components.common.constants.InvasiveRewrite;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.cloud.netflix.zuul.filters.route.support.AbstractRibbonCommandFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * ZuulRibbonCommandFactoryBeanPostProcessor
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-04-27 15:58
 */
@InvasiveRewrite
@Component
@ConditionalOnProperty(name = "zuul.simple-hystrix-command-key", havingValue = "false")
public class CustomRibbonCommandFactoryBeanPostProcessor implements BeanPostProcessor {
  @Autowired
  private SpringClientFactory clientFactory;
  @Autowired
  private ZuulProperties zuulProperties;
  @Autowired(required = false)
  private Set<FallbackProvider> zuulFallbackProviders = Collections.emptySet();

  @Value("${zuul.custom-hystrix-command-key-uri-prefix:}")
  private String uriPrefix;

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName)
    throws BeansException {
    if(bean instanceof AbstractRibbonCommandFactory
      && !(bean instanceof CustomRibbonCommandFactory)){
      return new CustomRibbonCommandFactory(clientFactory,
                                            zuulProperties,
                                            zuulFallbackProviders,
                                            (AbstractRibbonCommandFactory) bean,
                                            uriPrefix);
    }
    return bean;
  }
}
